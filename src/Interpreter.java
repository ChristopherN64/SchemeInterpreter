import fh.scheme.parser.ASTPrinter;
import fh.scheme.parser.Entry;
import fh.scheme.parser.TokenType;

import java.util.LinkedList;
import java.util.List;

public class Interpreter {
    public static List<String> KEYWORDS = List.of(new String[]{"define","list","cond","if","cons","car","cdr","list"});
    public static List<String> PRIMITIVE_PROCEDURES = List.of(new String[]{"car","cdr"});
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
        if(isDefinition(entries.get(0))) {
            env.variables.put(entries.get(1).getToken().getText(), eval(new Expression(entries.get(2)),env));
            return "Saved!";
        }

        //variable?
        if(isVariable(entries.get(0))) return lookupVarValue(entries.get(0), env);

        if(isIfCondition(entries.get(0))){
            if(eval(new Expression(entries.get(1)),env).equals("#t")) return eval(new Expression(entries.get(2)),env);
            return eval(new Expression(entries.get(3)),env);
        }

        //quoted?
        if (isQuoted(entries.get(0)))
            return ASTPrinter.getEntryAsString(entries.get(1)) + ")";

        if (isList(entries.get(0))) {
            expression.getEntry().getChildren().get(1).getToken().setText(eval(new Expression(entries.get(1)),env));
            expression.getEntry().getChildren().get(2).getToken().setText(eval(new Expression(entries.get(2)),env));
            return "("+expression.getEntry().getChildren().get(1).getToken().getText()+" "+expression.getEntry().getChildren().get(2).getToken().getText()+")";
        }

        if (entries.get(0).getToken().getText().equals("car"))
            return eval(new Expression(entries.get(1).getChildren().get(1)),env);

        if (entries.get(0).getToken().getText().equals("cdr"))
            return eval(new Expression(entries.get(1).getChildren().get(2)),env);

        //Application?
        if (isApplication(entries.get(0))) {
            List<Entry> arguments = entries.subList(1, entries.size());
            arguments.forEach((arg) -> {
                arg.getToken().setText(eval(new Expression(arg), env));
                arg.getToken().setType(TokenType.NUMBER);
            });
            return apply(new Procedure(entries.get(0)),
                    arguments,
                    env);
        }
        return "EVAL - ERROR";
    }

    public static String apply(Procedure procedure, List<Entry> arguments,Environment environment) {
        if (primitiveProcedure(procedure, arguments)) return applyPrimitive(procedure, arguments);
        return apply(procedure, arguments,environment);
    }

    private static boolean isList(Entry entry) {
        return entry.getToken().getText().equals("list") || entry.getToken().getText().equals("cons");
    }

    private static boolean isApplication(Entry entry) {
        return entry.getToken().getType() == TokenType.OPERATOR || PRIMITIVE_PROCEDURES.contains(entry.getToken().getText());
    }

    private static boolean isIfCondition(Entry entry) {
        return entry.getToken().getText().equals("if");
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
