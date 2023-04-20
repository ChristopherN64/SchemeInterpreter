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
            env.variables.put(entries.get(1).getToken().getText(), eval(new Expression(entries.get(2)),env));
            return StringToNumberEntry("Saved!");
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
            return StringToNumberEntry("");
        }

        //quoted?
        if (isQuoted(entries.get(0)))
            return StringToNumberEntry(ASTPrinter.getEntryAsString(entries.get(1)) + ")");

        if (isList(entries.get(0))) {
            for(int i=1; i<expression.getEntry().getChildren().size();i++){
                expression.getEntry().getChildren().set(i, eval(new Expression(entries.get(i)),env));
            }
            //Rebuild list to nested cons
            if(expression.getEntry().getChildren().size()>3){
                Entry root = expression.getEntry().getChildren().get(1);
                List<Entry> elements = expression.getEntry().getChildren().subList(2,expression.getEntry().getChildren().size());
                return addElementToCons(root,elements);
            }
            return expression.getEntry();
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

    public static Entry addElementToCons(Entry root, List<Entry> elements){
        if(elements.size()==2)return createConsEntry(root,createConsEntry(elements.get(0),elements.get(1)));
        return createConsEntry(root,addElementToCons(elements.get(0),elements.subList(1,elements.size())));
    }

    public static Entry createConsEntry(Entry child1, Entry child2){
        Entry entry = new Entry();
        entry.setToken(new Token(TokenType.LPARENTHESIS,"("));
        entry.setChildren(new LinkedList<>());
        entry.getChildren().add(new Entry(new Token(TokenType.ELEMENT,"cons")));
        entry.getChildren().add(child1);
        entry.getChildren().add(child2);
        return entry;
    }

    public static Entry apply(Procedure procedure, List<Entry> arguments,Environment environment) {
        if (primitiveProcedure(procedure, arguments)) return applyPrimitive(procedure, arguments,environment);
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
        return procedure.primitiveProcedure();
    }

    private static Entry applyPrimitive(Procedure procedure, List<Entry> arguments,Environment env) {
        if (procedure.operator.equals("car")) {
            return eval(new Expression(arguments.get(0).getChildren().get(1)),env);
        }

        if (procedure.operator.equals("cdr")) {
            return eval(new Expression(arguments.get(0).getChildren().get(2)),env);
        }

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
