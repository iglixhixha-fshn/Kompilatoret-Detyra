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

        switch (instr.type) {
    //Rreshti bosh ose komenti
    case EMPTY:
        break;

    //Rreshti i afishimit
    case PRINT: {
        ensureDefined(instr.target);
        double val = variables.get(instr.target);
        System.out.println(instr.target + " = " + formatNumber(val));
        break;
    }

    //Rreshti i leximit
    case READ: {
        System.out.print("Jep vleren per '" + instr.target + "': ");
        while (!keyboard.hasNextDouble()) {
            System.out.println("  Gabim: Duhet te jete nje numer. Provo perseri.");
            System.out.print("Jep vleren per '" + instr.target + "': ");
            keyboard.next();
        }
        double val = keyboard.nextDouble();
        variables.put(instr.target, val);
        break;
    }

    //Rreshti i deklarimit ose veprimeve aritmetike
    case ASSIGN: {
        double leftVal = resolveOperand(instr.exprLeft);

        double result;
        if (instr.operator == null) {
            result = leftVal;
        } else {
            double rightVal = resolveOperand(instr.exprRight);

            switch (instr.operator) {
                case "+": result = leftVal + rightVal; break;
                case "-": result = leftVal - rightVal; break;
                case "*": result = leftVal * rightVal; break;
                case "/":
                    if (rightVal == 0.0)
                        throw new RuntimeException(
                            "Gabim matematik: Pjesetim me zero -- " +
                            instr.target + " = " + instr.exprLeft +
                            " / " + instr.exprRight + ".");
                    result = leftVal / rightVal;
                    break;
                default:
                    throw new RuntimeException(
                        "Operator i panjohur: '" + instr.operator + "'.");
            }
        }

        variables.put(instr.target, result);
        break;
    }
}
    }

    // Funksione ndihmese per disa raste:
    // ensureDefined - Duhet kontrolluar nese variabli i kerkuar eshte inicializuar
    // isValidIdentifier - Duhet kontrolluar nese emri i vairablave eshte i sakte
    // - Duhet te nisi me nje shkronje ose mund te lejojme edhe _
    // - Me pas pjesa tjeter e karaktereve mund te permbaje shkronja, numra, _
    // resolveOperand - Pasi numrat lexohen, duhen konvertuar nga string ne double
    // formatNumber - Nese numri eshte i plote, afishimi duhet bere pa .0 ne fund,
    // pasi ruhet si double

    // Ketu do te vendosen funksionet ndihmese

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
