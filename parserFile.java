import java.io.*;
import java.util.*;

public class parserFile {
    enum InstrType {
        ASSIGN, // var = expr
        PRINT, // afishimi
        READ, // leximi
        EMPTY // rreshta bosh/ komente
    }

    static class Instruction {
        InstrType type;
        String target; // emri variablit
        String exprLeft; // ana e majte e operatorit
        String operator; // operatori artimetik
        String exprRight; // ana e djathe e operatorit

        // Konstruktor per leximin ose afishimin
        Instruction(InstrType type, String target) {
            this.type = type;
            this.target = target;
        }

        // Konstruktor per deklarimet, caktimin e variablave
        Instruction(InstrType type, String target, String exprLeft) {
            this.type = type;
            this.target = target;
            this.exprLeft = exprLeft;
        }

        // Konstruktor per operacionet aritmetike +, -, *, /
        Instruction(InstrType type, String target,
                String exprLeft, String operator, String exprRight) {
            this.type = type;
            this.target = target;
            this.exprLeft = exprLeft;
            this.operator = operator;
            this.exprRight = exprRight;
        }

        // Konstruktori bosh
        Instruction() {
            this.type = InstrType.EMPTY;
        }
    }

    private static final Map<String, Double> variables = new LinkedHashMap<>();
    private static final Scanner keyboard = new Scanner(System.in);

    static Instruction parse(String line) {

        // Komandat parse - duhen vendosur ketu dy pjeset
        // pjesa e pare e komandave parse - ky koment duhet fshire

        // Nese rreshti permban #, do te jete nje koment
        int hashIndex = line.indexOf('#');
        if (hashIndex >= 0)
            line = line.substring(0, hashIndex);
        line = line.trim();

        if (line.isEmpty()) {
            return new Instruction();
        }

        String[] tokens = line.split("\\s+");
        String keyword = tokens[0];

        // Nese rreshti permban afisho, do behet instruksioni i afishimit
        if (keyword.equalsIgnoreCase("afisho")) {
            if (tokens.length < 2)
                throw new RuntimeException(
                        "Sintaksa gabim: 'afisho' kerkon saktesisht nje argument. " +
                                "Shembull: afisho x");
            if (tokens.length > 2)
                throw new RuntimeException(
                        "Sintaksa gabim: 'afisho' merr vetem nje argument, jo " +
                                (tokens.length - 1) + ".");
            return new Instruction(InstrType.PRINT, tokens[1]);
        }

        // Nese rreshti permban lexo, do behet instruksioni i leximit
        if (keyword.equalsIgnoreCase("lexo")) {
            if (tokens.length < 2)
                throw new RuntimeException(
                        "Sintaksa gabim: 'lexo' kerkon saktesisht nje argument. " +
                                "Shembull: lexo x");
            if (tokens.length > 2)
                throw new RuntimeException(
                        "Sintaksa gabim: 'lexo' merr vetem nje argument (emrin e variablit), " +
                                "jo " + (tokens.length - 1) + ".");
            return new Instruction(InstrType.READ, tokens[1]);
        }

        // Kontrolli per shenjen e barazimit - verpimet aritmetike/deklarimet
        if (tokens.length >= 3 && tokens[1].equals("=")) {

            if (!isValidIdentifier(tokens[0]))
                throw new RuntimeException(
                        "Sintaksa gabim: '" + tokens[0] +
                                "' nuk eshte emer i vlefshëm variabel.");

            String varName = tokens[0];

            // Marrim shprehjen ne anen e djathte te barazimit
            String rhs = line.substring(line.indexOf('=') + 1).trim();

            if (rhs.isEmpty())
                throw new RuntimeException(
                        "Sintaksa gabim: Mungon vlera pas '=' per variablen '" +
                                varName + "'.");

            // Kontrollojme nese ana e djatha e barazmit permban nje shenje veprimi
            int opIndex = -1;
            String opStr = null;
            for (int i = 1; i < rhs.length(); i++) {
                char c = rhs.charAt(i);
                if (c == '+' || c == '-' || c == '*' || c == '/') {
                    opIndex = i;
                    opStr = String.valueOf(c);
                    break;
                }
            }

            // Nese kushtet plotesohen per nje verpim aritmetik
            if (opIndex > 0) {
                String left = rhs.substring(0, opIndex).trim();
                String right = rhs.substring(opIndex + 1).trim();

                if (left.isEmpty())
                    throw new RuntimeException(
                            "Sintaksa gabim: Mungon operandi i majte per '" +
                                    opStr + "' te variabla '" + varName + "'.");
                if (right.isEmpty())
                    throw new RuntimeException(
                            "Sintaksa gabim: Mungon operandi i djathte per '" +
                                    opStr + "' te variabla '" + varName + "'.");

                return new Instruction(InstrType.ASSIGN, varName, left, opStr, right);
            }

            // Nese nuk u gjend nje veprim aritmetik, kemi nje deklarim
            return new Instruction(InstrType.ASSIGN, varName, rhs);
        }

        // Ne rast se nuk gjendet nje komande e vlefshme
        throw new RuntimeException(
                "Sintaksa gabim: Rreshti '" + line +
                        "' nuk njihet. Komandat e vlefshme: afisho, lexo, =");
    }

    static void exec(Instruction instr) {

        // komandat e ekzekutimit

    }

    // Funksione ndihmese per disa raste:
    // ensureDefined - Duhet kontrolluar nese variabli i kerkuar eshte inicializuar
    // isValidIdentifier - Duhet kontrolluar nese emri i vairablave eshte i sakte
    // - Duhet te nisi me nje shkronje ose mund te lejojme edhe _
    // - Me pas pjesa tjeter e karaktereve mund te permbaje shkronja, numra, _
    // resolveOperand - Pasi numrat lexohen, duhen konvertuar nga string ne double
    // formatNumber - Nese numri eshte i plote, afishimi duhet bere pa .0 ne fund,
    // pasi ruhet si double

   private static void ensureDefined(String name) {
    if (!variables.containsKey(name))
        throw new RuntimeException(
    "Gabim: Variabla '" + name +
    "' nuk eshte deklaruar para perdorimit te saj.");
}

private static boolean isValidIdentifier(String s) {
    if (s == null || s.isEmpty()) return false;
    if (!Character.isLetter(s.charAt(0)) && s.charAt(0) != '_') return false;
    for (char c : s.toCharArray())
        if (!Character.isLetterOrDigit(c) && c != '_') return false;
    return true;
}

private static double resolveOperand(String token) {
    try {
        return Double.parseDouble(token);
    } catch (NumberFormatException e) {
        //Sigurojme referencen e variablit
        ensureDefined(token);
        return variables.get(token);
    }
}

private static String formatNumber(double val) {
    if (val == Math.floor(val) && !Double.isInfinite(val))
        return String.valueOf((long) val);
    return String.valueOf(val);
}

    public static void main(String[] args) {
        String filename = "input.gg";
        List<String> sourceLines = new ArrayList<>();

        // Leximi i input.gg
        try (BufferedReader reader = new BufferedReader(new FileReader("input.gg"))) {
            String line;
            while ((line = reader.readLine()) != null)
                sourceLines.add(line);
        } catch (FileNotFoundException e) {
            System.err.println("Gabim: Skedari '" + filename + "' nuk u gjet.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Gabim I/O: " + e.getMessage());
            System.exit(1);
        }

        // Parsing i rreshtave te lexuar dhe ekzekutimi i tyre
        int lineNumber = 0;
        for (String rawLine : sourceLines) {
            lineNumber++;
            try {
                Instruction instr = parse(rawLine);
                exec(instr);
            } catch (RuntimeException e) {
                System.err.println("[Rreshti " + lineNumber + "] GABIM: " + e.getMessage());
                System.err.println("Ekzekutimi u ndal.");
                System.exit(1);
            }
        }

        System.out.println("\n=== Ekzekutimi perfundoi me sukses. ===");
    }
}
