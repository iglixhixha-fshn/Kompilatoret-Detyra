import java.io.*;
import java.util.*;

public class parserFile {
    enum InstrType {
        ASSIGN,   // var = expr
        PRINT,    // afishimi
        READ,     // leximi
        EMPTY     // rreshta bosh/ komente
    }

    static class Instruction {
        InstrType type;
        String target;    // emri variablit
        String exprLeft;  // ana e majte e operatorit
        String operator;  // operatori artimetik
        String exprRight; // ana e djathe e operatorit

        //Konstruktor per leximin ose afishimin
        Instruction(InstrType type, String target) {
            this.type   = type;
            this.target = target;
        }

        //Konstruktor per deklarimet, caktimin e variablave
        Instruction(InstrType type, String target, String exprLeft) {
            this.type     = type;
            this.target   = target;
            this.exprLeft = exprLeft;
        }

        //Konstruktor per operacionet aritmetike +, -, *, /
        Instruction(InstrType type, String target,
                    String exprLeft, String operator, String exprRight) {
            this.type      = type;
            this.target    = target;
            this.exprLeft  = exprLeft;
            this.operator  = operator;
            this.exprRight = exprRight;
        }

        //Konstruktori bosh
        Instruction() {
            this.type = InstrType.EMPTY;
        }
    }

    private static final Map<String, Double> variables = new LinkedHashMap<>();
    private static final Scanner keyboard  = new Scanner(System.in);

    static Instruction parse(String line) {

        //Komandat parse - duhen vendosur ketu dy pjeset

        //Ne rast se nuk gjendet nje komande e vlefshme
        throw new RuntimeException(
            "Sintaksa gabim: Rreshti '" + line +
            "' nuk njihet. Komandat e vlefshme: afisho, lexo, =");
    }

    static void exec(Instruction instr) {

        //komandat e ekzekutimit

    }

    //Funksione ndihmese per disa raste:
    //ensureDefined - Duhet kontrolluar nese variabli i kerkuar eshte inicializuar
    //isValidIdentifier - Duhet kontrolluar nese emri i vairablave eshte i sakte
    //                  - Duhet te nisi me nje shkronje ose mund te lejojme edhe _
    //                  - Me pas pjesa tjeter e karaktereve mund te permbaje shkronja, numra, _
    //resolveOperand - Pasi numrat lexohen, duhen konvertuar nga string ne double
    //formatNumber - Nese numri eshte i plote, afishimi duhet bere pa .0 ne fund, pasi ruhet si double
    
    //Ketu do te vendosen funksionet ndihmese

    public static void main(String[] args) {
        String filename = "input.gg";
        List<String> sourceLines = new ArrayList<>();

        //Leximi i input.gg
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

        //Parsing i rreshtave te lexuar dhe ekzekutimi i tyre
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