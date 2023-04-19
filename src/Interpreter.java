import fh.scheme.parser.ASTPrinter;
import fh.scheme.parser.Entry;
import fh.scheme.parser.Token;
import fh.scheme.parser.TokenType;

import java.util.LinkedList;
import java.util.List;

public class Interpreter {
    public static List<String> KEYWORDS = List.of(new String[]{"define","list","cond","if","cons","car","cdr","list","quote","'"});
    public static List<String> PRIMITIVE_PROCEDURES = List.of(new String[]{"car","cdr"});
    public static Entry eval(Expression expression, Environment env) {
        List<Entry> entries = new LinkedList<>();
        if(expression.getEntry().getToken().getType()==TokenType.LPARENTHESIS){
            entries.addAll(expression.getEntry().getChildren());
        }
        else {
            entries.add(expression.getEntry());
        }

        //Self Evaluating?
        if(entries.size()==1 && isSelfEvaluating(entries.get(0))) return entries.get(0);

        //definition?
        if(isDefinition(entries.get(0))) {
            env.variables.put(entries.get(1).getToken().getText(), entries.get(2));
            return new Entry(new Token(TokenType.STRING,"Saved!"));
        }

        //variable?
        if(isVariable(entries.get(0))) return eval(new Expression(lookupVarValue(entries.get(0), env)),env);

        if(isIfCondition(entries.get(0))){
            if(eval(new Expression(entries.get(1)),env).getToken().getText().equals("#t")) return eval(new Expression(entries.get(2)),env);
            return eval(new Expression(entries.get(3)),env);
        }

        if(isCondCondition(entries.get(0))){
            for(int i=1; i<entries.size();i++){
                if(eval(new Expression(entries.get(i).getChildren().get(0)),env).getToken().getText().equals("#t")) return eval(new Expression(entries.get(i).getChildren().get(1)),env);
            }
            return new Entry(new Token(TokenType.STRING,""));
        }

        //quoted?
        if (isQuoted(entries.get(0)))
            return new Entry(new Token(TokenType.STRING,ASTPrinter.getEntryAsString(entries.get(1)) + ")"));

        if (isList(entries.get(0))) {
            expression.getEntry().getChildren().set(1, eval(new Expression(entries.get(1)),env));
            expression.getEntry().getChildren().set(2, eval(new Expression(entries.get(2)),env));
            return expression.getEntry();
        }

        if (entries.get(0).getToken().getText().equals("car")) {
            Entry list = eval(new Expression(entries.get(1)),env);
            return eval(new Expression(list.getChildren().get(1)),env);
        }

        if (entries.get(0).getToken().getText().equals("cdr")) {
            Entry list = eval(new Expression(entries.get(1)),env);
            return eval(new Expression(list.getChildren().get(2)),env);
        }

        //Application?
        if (isApplication(entries.get(0))) {
            List<Entry> arguments = entries.subList(1, entries.size());
            arguments.replaceAll(entry -> eval(new Expression(entry), env));
            return apply(new Procedure(entries.get(0)),
                    arguments,
                    env);
        }
        return StringToNumberEntry("EVAL - ERROR");
    }

    public static Entry apply(Procedure procedure, List<Entry> arguments,Environment environment) {
        if (primitiveProcedure(procedure, arguments)) return applyPrimitive(procedure, arguments);
        return apply(procedure, arguments,environment);
    }

    public static boolean isList(Entry entry) {
        return entry.getToken().getText().equals("list") || entry.getToken().getText().equals("cons");
    }

    private static boolean isApplication(Entry entry) {
        return entry.getToken().getType() == TokenType.OPERATOR || PRIMITIVE_PROCEDURES.contains(entry.getToken().getText());
    }

    private static boolean isIfCondition(Entry entry) {
        return entry.getToken().getText().equals("if");
    }

    private static boolean isCondCondition(Entry entry) {
        return entry.getToken().getText().equals("cond");
    }

    private static boolean isQuoted(Entry entry) {
        return entry.getToken().getType() == TokenType.QUOTE || entry.getToken().getText().equals("quote");
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

    private static Entry lookupVarValue(Entry entry, Environment env) {
        return env.variables.get(entry.getToken().getText());
    }


    private static boolean primitiveProcedure(Procedure procedure, List<Entry> arguments) {
        return procedure.primitiveProcedure() && arguments.stream().allMatch((arg) -> arg.getToken().getType() == TokenType.NUMBER);
    }

    private static Entry applyPrimitive(Procedure procedure, List<Entry> arguments) {
        if (procedure.operator.equals("*"))
            return StringToNumberEntry(arguments.stream().map((e) -> Integer.parseInt(e.getToken().getText())).reduce((a, b) -> a * b).get().toString());
        if (procedure.operator.equals("/"))
            return StringToNumberEntry(arguments.stream().map((e) -> Integer.parseInt(e.getToken().getText())).reduce((a, b) -> a / b).get().toString());
        if (procedure.operator.equals("+"))
            return StringToNumberEntry(arguments.stream().map((e) -> Integer.parseInt(e.getToken().getText())).reduce((a, b) -> a + b).get().toString());
        if (procedure.operator.equals("-"))
            return StringToNumberEntry(arguments.stream().map((e) -> Integer.parseInt(e.getToken().getText())).reduce((a, b) -> a - b).get().toString());

        if (procedure.operator.equals(">"))
            return StringToBooleanEntry(Integer.parseInt(arguments.get(0).getToken().getText()) > Integer.parseInt(arguments.get(1).getToken().getText()) ? "#t" : "#f");
        if (procedure.operator.equals(">="))
            return StringToBooleanEntry(Integer.parseInt(arguments.get(0).getToken().getText()) >= Integer.parseInt(arguments.get(1).getToken().getText()) ? "#t" : "#f");
        if (procedure.operator.equals("<"))
            return StringToBooleanEntry(Integer.parseInt(arguments.get(0).getToken().getText()) < Integer.parseInt(arguments.get(1).getToken().getText()) ? "#t" : "#f");
        if (procedure.operator.equals("<="))
            return StringToBooleanEntry(Integer.parseInt(arguments.get(0).getToken().getText()) <= Integer.parseInt(arguments.get(1).getToken().getText()) ? "#t" : "#f");
        if (procedure.operator.equals("="))
            return StringToBooleanEntry(Integer.parseInt(arguments.get(0).getToken().getText()) == Integer.parseInt(arguments.get(1).getToken().getText()) ? "#t" : "#f");

        return StringToNumberEntry("");
    }

    private static Entry StringToNumberEntry(String s){
        return new Entry(new Token(TokenType.NUMBER,s));
    }

    private static Entry StringToBooleanEntry(String s){
        return new Entry(new Token(TokenType.BOOLEAN,s));
    }

}
