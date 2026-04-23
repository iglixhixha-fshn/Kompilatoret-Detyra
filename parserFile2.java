import java.io.*;
import java.util.*;

enum InstrType {
    ASSIGN,    // deklarime/veprime
    PRINT,     // afishimi
    READ,      // leximi
    EMPTY,     // rreshta bosh/ komente
    POST_INC,  // c = a++
    POST_DEC,  // c = a--
    PRE_INC,   // d = ++a
    PRE_DEC,   // e = --a
    COMPOUND   // f += b, g -= a, h *= e, i /= g
}

class Instruction {
    InstrType type;
    String target;
    String exprLeft;
    String operator;
    String exprRight;

    Instruction(InstrType type, String target) {
        this.type = type;
        this.target = target;
    }

    Instruction(InstrType type, String target, String exprLeft) {
        this.type = type;
        this.target = target;
        this.exprLeft = exprLeft;
    }

    Instruction(InstrType type, String target,
                String exprLeft, String operator, String exprRight) {
        this.type = type;
        this.target = target;
        this.exprLeft = exprLeft;
        this.operator = operator;
        this.exprRight = exprRight;
    }

    Instruction() {
        this.type = InstrType.EMPTY;
    }
}

public class parserFileRecursive {

    private static final Map<String, Double> variables = new LinkedHashMap<>();
    private static final Scanner keyboard = new Scanner(System.in);

    // -------------------------------------------------------------------------
    // PARSE - recursive helpers replace all loops
    // -------------------------------------------------------------------------

    static Instruction parse(String line) {
        int hashIndex = line.indexOf('#');
        if (hashIndex >= 0)
            line = line.substring(0, hashIndex);
        line = line.trim();

        if (line.isEmpty())
            return new Instruction();

        // Try compound operators recursively over the list ["+=","-=","*=","/="]
        Instruction compound = tryParseCompound(line, new String[]{"+=", "-=", "*=", "/="}, 0);
        if (compound != null)
            return compound;

        String[] tokens = line.split("\\s+");
        String keyword = tokens[0];

        if (keyword.equalsIgnoreCase("afisho")) {
            if (tokens.length < 2)
                throw new RuntimeException(
                    "Sintaksa gabim: 'afisho' kerkon saktesisht nje argument. Shembull: afisho x");
            if (tokens.length > 2)
                throw new RuntimeException(
                    "Sintaksa gabim: 'afisho' merr vetem nje argument, jo " + (tokens.length - 1) + ".");
            return new Instruction(InstrType.PRINT, tokens[1]);
        }

        if (keyword.equalsIgnoreCase("lexo")) {
            if (tokens.length < 2)
                throw new RuntimeException(
                    "Sintaksa gabim: 'lexo' kerkon saktesisht nje argument. Shembull: lexo x");
            if (tokens.length > 2)
                throw new RuntimeException(
                    "Sintaksa gabim: 'lexo' merr vetem nje argument (emrin e variablit), jo "
                    + (tokens.length - 1) + ".");
            return new Instruction(InstrType.READ, tokens[1]);
        }

        if (tokens.length >= 3 && tokens[1].equals("=")) {
            if (!isValidIdentifier(tokens[0]))
                throw new RuntimeException(
                    "Sintaksa gabim: '" + tokens[0] + "' nuk eshte emer i vlefshem variabel.");

            String varName = tokens[0];
            String rhs = line.substring(line.indexOf('=') + 1).trim();

            if (rhs.isEmpty())
                throw new RuntimeException(
                    "Sintaksa gabim: Mungon vlera pas '=' per variablen '" + varName + "'.");

            if (rhs.startsWith("++")) {
                String operand = rhs.substring(2).trim();
                if (!isValidIdentifier(operand))
                    throw new RuntimeException(
                        "Sintaksa gabim: '" + operand + "' nuk eshte emer i vlefshem variabel per '++' para.");
                return new Instruction(InstrType.PRE_INC, varName, operand);
            }

            if (rhs.startsWith("--")) {
                String operand = rhs.substring(2).trim();
                if (!isValidIdentifier(operand))
                    throw new RuntimeException(
                        "Sintaksa gabim: '" + operand + "' nuk eshte emer i vlefshem variabel per '--' para.");
                return new Instruction(InstrType.PRE_DEC, varName, operand);
            }

            if (rhs.endsWith("++")) {
                String operand = rhs.substring(0, rhs.length() - 2).trim();
                if (!isValidIdentifier(operand))
                    throw new RuntimeException(
                        "Sintaksa gabim: '" + operand + "' nuk eshte emer i vlefshem variabel per '++' pas.");
                return new Instruction(InstrType.POST_INC, varName, operand);
            }

            if (rhs.endsWith("--")) {
                String operand = rhs.substring(0, rhs.length() - 2).trim();
                if (!isValidIdentifier(operand))
                    throw new RuntimeException(
                        "Sintaksa gabim: '" + operand + "' nuk eshte emer i vlefshem variabel per '--' pas.");
                return new Instruction(InstrType.POST_DEC, varName, operand);
            }

            // Scan for binary operator recursively starting at index 1
            int opIndex = findOperatorIndex(rhs, 1);
            if (opIndex > 0) {
                String left = rhs.substring(0, opIndex).trim();
                String right = rhs.substring(opIndex + 1).trim();
                String opStr = String.valueOf(rhs.charAt(opIndex));

                if (left.isEmpty())
                    throw new RuntimeException(
                        "Sintaksa gabim: Mungon operandi i majte per '" + opStr + "' te variabla '" + varName + "'.");
                if (right.isEmpty())
                    throw new RuntimeException(
                        "Sintaksa gabim: Mungon operandi i djathte per '" + opStr + "' te variabla '" + varName + "'.");

                return new Instruction(InstrType.ASSIGN, varName, left, opStr, right);
            }

            return new Instruction(InstrType.ASSIGN, varName, rhs);
        }

        throw new RuntimeException(
            "Sintaksa gabim: Rreshti '" + line + "' nuk njihet. Komandat e vlefshme: afisho, lexo, =");
    }

    /**
     * Recursive scan: tries each compound operator in the array one at a time.
     * Base case: index >= array length -> nothing matched, return null.
     * Recursive case: check ops[index]; if found return the instruction,
     * otherwise recurse with index+1.
     */
    private static Instruction tryParseCompound(String line, String[] ops, int index) {
        // Base case - exhausted all operators
        if (index >= ops.length)
            return null;

        String compOp = ops[index];
        int idx = line.indexOf(compOp);
        if (idx > 0) {
            String lhs = line.substring(0, idx).trim();
            String rhs = line.substring(idx + 2).trim();

            if (!isValidIdentifier(lhs))
                throw new RuntimeException(
                    "Sintaksa gabim: '" + lhs +
                    "' nuk eshte emer i vlefshem variabel per operatorin '" + compOp + "'.");
            if (rhs.isEmpty())
                throw new RuntimeException(
                    "Sintaksa gabim: Mungon operandi i djathte per '" + compOp +
                    "' te variabla '" + lhs + "'.");

            String baseOp = String.valueOf(compOp.charAt(0));
            return new Instruction(InstrType.COMPOUND, lhs, lhs, baseOp, rhs);
        }

        // Recursive case - try next operator
        return tryParseCompound(line, ops, index + 1);
    }

    /**
     * Recursive scan for a binary arithmetic operator inside a string.
     * Replaces the for-loop in the original parse().
     * Base case: index >= length -> not found, return -1.
     * Recursive case: check character at index; if operator return index,
     * otherwise recurse with index+1.
     */
    private static int findOperatorIndex(String s, int index) {
        // Base case - reached end of string without finding an operator
        if (index >= s.length())
            return -1;

        char c = s.charAt(index);
        if (c == '+' || c == '-' || c == '*' || c == '/')
            return index;

        // Recursive case - advance to next character
        return findOperatorIndex(s, index + 1);
    }

    /**
     * Recursive identifier validation.
     * Replaces the for-each loop in isValidIdentifier().
     * Base case: index >= length -> all characters valid, return true.
     * Recursive case: check character at index; if invalid return false,
     * otherwise recurse with index+1.
     */
    private static boolean isValidIdentifierChars(String s, int index) {
        // Base case - all characters have been checked
        if (index >= s.length())
            return true;

        char c = s.charAt(index);
        if (!Character.isLetterOrDigit(c) && c != '_')
            return false;

        // Recursive case - check remaining characters
        return isValidIdentifierChars(s, index + 1);
    }

    private static boolean isValidIdentifier(String s) {
        if (s == null || s.isEmpty())
            return false;
        if (!Character.isLetter(s.charAt(0)) && s.charAt(0) != '_')
            return false;
        return isValidIdentifierChars(s, 1); // start from index 1, head already checked
    }

    // -------------------------------------------------------------------------
    // EXEC - recursive method replaces the main for-loop in main()
    // -------------------------------------------------------------------------

    /**
     * Processes lines from sourceLines recursively.
     * Base case: index >= list size -> execution complete.
     * Recursive case: parse and exec line at index, then recurse with index+1.
     */
    static void execLines(List<String> sourceLines, int index) {
        // Base case - no more lines to process
        if (index >= sourceLines.size())
            return;

        int lineNumber = index + 1;
        try {
            Instruction instr = parse(sourceLines.get(index));
            exec(instr);
        } catch (RuntimeException e) {
            System.err.println("[Rreshti " + lineNumber + "] GABIM: " + e.getMessage());
            System.err.println("Ekzekutimi u ndal.");
            System.exit(1);
        }

        // Recursive case - process remaining lines
        execLines(sourceLines, index + 1);
    }

    static void exec(Instruction instr) {
        switch (instr.type) {
            case EMPTY:
                break;

            case PRINT: {
                ensureDefined(instr.target);
                double val = variables.get(instr.target);
                System.out.println(instr.target + " = " + formatNumber(val));
                break;
            }

            case READ: {
                System.out.print("Jep vleren per '" + instr.target + "': ");
                readValidDouble(instr.target);
                break;
            }

            case ASSIGN: {
                double leftVal = resolveOperand(instr.exprLeft);
                double result;
                if (instr.operator == null) {
                    result = leftVal;
                } else {
                    double rightVal = resolveOperand(instr.exprRight);
                    result = applyOperator(instr.operator, leftVal, rightVal,
                                           instr.target, instr.exprLeft, instr.exprRight);
                }
                variables.put(instr.target, result);
                break;
            }

            case POST_INC: {
                ensureDefined(instr.exprLeft);
                double current = variables.get(instr.exprLeft);
                variables.put(instr.target, current);
                variables.put(instr.exprLeft, current + 1.0);
                break;
            }

            case POST_DEC: {
                ensureDefined(instr.exprLeft);
                double current = variables.get(instr.exprLeft);
                variables.put(instr.target, current);
                variables.put(instr.exprLeft, current - 1.0);
                break;
            }

            case PRE_INC: {
                ensureDefined(instr.exprLeft);
                double incremented = variables.get(instr.exprLeft) + 1.0;
                variables.put(instr.exprLeft, incremented);
                variables.put(instr.target, incremented);
                break;
            }

            case PRE_DEC: {
                ensureDefined(instr.exprLeft);
                double decremented = variables.get(instr.exprLeft) - 1.0;
                variables.put(instr.exprLeft, decremented);
                variables.put(instr.target, decremented);
                break;
            }

            case COMPOUND: {
                ensureDefined(instr.target);
                double leftVal = variables.get(instr.target);
                double rightVal = resolveOperand(instr.exprRight);
                double result = applyOperator(instr.operator, leftVal, rightVal,
                                              instr.target, instr.target, instr.exprRight);
                variables.put(instr.target, result);
                break;
            }
        }
    }

    /**
     * Recursive input validation for READ - replaces the while-loop.
     * Base case: a valid double is entered -> store it and return.
     * Recursive case: invalid input -> print error and recurse to try again.
     */
    private static void readValidDouble(String varName) {
        if (keyboard.hasNextDouble()) {
            // Base case - valid number entered
            variables.put(varName, keyboard.nextDouble());
            return;
        }
        // Recursive case - invalid input, ask again
        System.out.println("  Gabim: Duhet te jete nje numer. Provo perseri.");
        System.out.print("Jep vleren per '" + varName + "': ");
        keyboard.next();
        readValidDouble(varName);
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    private static double applyOperator(String op, double left, double right,
                                        String varName, String lhsStr, String rhsStr) {
        switch (op) {
            case "+":
                return left + right;
            case "-":
                return left - right;
            case "*":
                return left * right;
            case "/":
                if (right == 0.0)
                    throw new RuntimeException(
                        "Gabim matematik: Pjesetim me zero -- " +
                        varName + " = " + lhsStr + " / " + rhsStr + ".");
                return left / right;
            default:
                throw new RuntimeException("Operator i panjohur: '" + op + "'.");
        }
    }

    private static void ensureDefined(String name) {
        if (!variables.containsKey(name))
            throw new RuntimeException(
                "Gabim: Variabla '" + name + "' nuk eshte deklaruar para perdorimit te saj.");
    }

    private static double resolveOperand(String token) {
        try {
            return Double.parseDouble(token);
        } catch (NumberFormatException e) {
            ensureDefined(token);
            return variables.get(token);
        }
    }

    private static String formatNumber(double val) {
        if (val == Math.floor(val) && !Double.isInfinite(val))
            return String.valueOf((long) val);
        return String.valueOf(val);
    }

    // -------------------------------------------------------------------------
    // MAIN
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        String filename = "input.gg";
        List<String> sourceLines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
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

        // Recursive execution
        execLines(sourceLines, 0);

        System.out.println("\n=== Ekzekutimi perfundoi me sukses. ===");
    }
}