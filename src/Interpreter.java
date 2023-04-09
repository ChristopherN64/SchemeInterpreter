import fh.scheme.parser.ASTPrinter;
import fh.scheme.parser.Entry;
import fh.scheme.parser.TokenType;

import java.util.LinkedList;
import java.util.List;

public class Interpreter {
    public static List<String> KEYWORDS = List.of(new String[]{"define","list","cond","if"});
    public static String eval(Expression expression, Environment env) {
        List<Entry> entries = new LinkedList<>();
        if(expression.getEntry().getToken().getType()==TokenType.LPARENTHESIS){
            entries.addAll(expression.getEntry().getChildren());
        }
        else {
            entries.add(expression.getEntry());
        }

        //Self Evaluating?
        if(entries.size()==1 && isSelfEvaluating(entries.get(0))) return entries.get(0).getToken().getText();

        //definition?
        if (isDefinition(entries.get(0))) {
            env.variables.put(entries.get(1).getToken().getText(), eval(new Expression(entries.get(2)),env));
            return "";
        }

        //variable?
        if (isVariable(entries.get(0))) return lookupVarValue(entries.get(0), env);

        //quoted?
        if (isQuoted(entries.get(0)))
            return ASTPrinter.getEntryAsString(entries.get(0)).substring(1) + ")";

        //Application?
        if (isApplication(entries.get(0))) {
            return apply(new Procedure(entries.get(0)),
                    entries.subList(1, entries.size()),
                    env);
        }


        return null;
    }

    private static boolean isApplication(Entry entry) {
        return entry.getToken().getType() == TokenType.OPERATOR;
    }

    private static boolean isQuoted(Entry entry) {
        return entry.getToken().getType() == TokenType.QUOTE;
    }

    private static boolean isVariable(Entry entry) {
        return entry.getToken().getType() == TokenType.ELEMENT && !KEYWORDS.contains(entry.getToken().getText());
    }

    private static boolean isDefinition(Entry entry) {
        return entry.getToken().getType() == TokenType.ELEMENT && entry.getToken().getText().equals("define");
    }

    private static boolean isSelfEvaluating(Entry entry) {
        return isNumber(entry) || isBoolean(entry);
    }

    private static boolean isBoolean(Entry entry) {
        return entry.getToken().getType()==TokenType.BOOLEAN;
    }

    private static boolean isNumber(Entry entry) {
        return entry.getToken().getType()==TokenType.NUMBER;
    }

    private static String lookupVarValue(Entry entry, Environment env) {
        return env.variables.get(entry.getToken().getText());
    }

    public static String apply(Procedure procedure, List<Entry> arguments,Environment environment) {
        if (primitiveProcedure(procedure, arguments)) return applyPrimitive(procedure, arguments);
        arguments.forEach((arg) -> {
            arg.getToken().setText(eval(new Expression(arg), environment));
            arg.getToken().setType(TokenType.NUMBER);
        });
        return apply(procedure, arguments,environment);
    }

    private static boolean primitiveProcedure(Procedure procedure, List<Entry> arguments) {
        return procedure.primitiveProcedure() && arguments.stream().allMatch((arg) -> arg.getToken().getType() == TokenType.NUMBER);
    }

    private static String applyPrimitive(Procedure procedure, List<Entry> arguments) {
        if (procedure.operator.equals("*"))
            return arguments.stream().map((e) -> Integer.parseInt(e.getToken().getText())).reduce((a, b) -> a * b).get().toString();
        if (procedure.operator.equals("/"))
            return arguments.stream().map((e) -> Integer.parseInt(e.getToken().getText())).reduce((a, b) -> a / b).get().toString();
        if (procedure.operator.equals("+"))
            return arguments.stream().map((e) -> Integer.parseInt(e.getToken().getText())).reduce((a, b) -> a + b).get().toString();
        if (procedure.operator.equals("-"))
            return arguments.stream().map((e) -> Integer.parseInt(e.getToken().getText())).reduce((a, b) -> a - b).get().toString();

        if (procedure.operator.equals(">"))
            return Integer.parseInt(arguments.get(0).getToken().getText()) > Integer.parseInt(arguments.get(1).getToken().getText()) ? "#t" : "#f";
        if (procedure.operator.equals(">="))
            return Integer.parseInt(arguments.get(0).getToken().getText()) >= Integer.parseInt(arguments.get(1).getToken().getText()) ? "#t" : "#f";
        if (procedure.operator.equals("<"))
            return Integer.parseInt(arguments.get(0).getToken().getText()) < Integer.parseInt(arguments.get(1).getToken().getText()) ? "#t" : "#f";
        if (procedure.operator.equals("<="))
            return Integer.parseInt(arguments.get(0).getToken().getText()) <= Integer.parseInt(arguments.get(1).getToken().getText()) ? "#t" : "#f";
        if (procedure.operator.equals("="))
            return Integer.parseInt(arguments.get(0).getToken().getText()) == Integer.parseInt(arguments.get(1).getToken().getText()) ? "#t" : "#f";

        return "";
    }

}
