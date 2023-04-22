import fh.scheme.parser.ASTPrinter;
import fh.scheme.parser.Entry;
import fh.scheme.parser.Token;
import fh.scheme.parser.TokenType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Interpreter {
    public static List<String> KEYWORDS = List.of(new String[]{"define","list","cond","if","cons","list","quote","'"});
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
            //procedure
            if(entries.get(1).getToken().getType().equals(TokenType.LPARENTHESIS)){
                Procedure procedure = new Procedure(entries.get(2),
                        entries.get(1),
                        entries.get(2),
                        new ArrayList<>());
                env.procedure.put(procedure.name,procedure);
            }
            //Variable
            else putVarValue(env,entries.get(1).getToken().getText(),entries.get(2));
            return StringToNumberEntry("Saved!");
        }

        //variable?
        if(isVariable(entries.get(0))){
            Entry variable = lookupVarValue(entries.get(0), env);
            if(variable!=null) return eval(new Expression(variable),env);
            if(lookupProValue(entries.get(0),env)==null) return StringToNumberEntry("null");
        }

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
        Procedure procedure = lookupProValue(entries.get(0),env);
        if (isApplication(entries.get(0)) || procedure!=null) {
            List<Entry> arguments = entries.subList(1, entries.size());
            arguments.replaceAll(entry -> eval(new Expression(entry), env));
            if(procedure!=null){
                //Put var Values in env
                for(int i=0;i<arguments.size();i++){
                    putVarValue(env,procedure.getVariables().get(i).getToken().getText(),arguments.get(i));
                }
                return eval(new Expression(procedure.getBody()),env);
            }
            return apply(new Procedure(entries.get(0),expression.getEntry(),entries.get(0),arguments), env);
        }
        return StringToNumberEntry("EVAL - ERROR");
    }

    private static void putVarValue(Environment env,String name,Entry value) {
        env.variables.put(name, eval(new Expression(value),env));
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

    public static Entry apply(Procedure procedure, Environment environment) {
        if (primitiveProcedure(procedure)) return applyPrimitive(procedure,environment);
        else {
            return eval(new Expression(procedure.getBody()),environment);
        }
    }

    public static boolean isList(Entry entry) {
        return entry.getToken().getText().equals("list") || entry.getToken().getText().equals("cons");
    }

    private static boolean isApplication(Entry entry) {
        return entry.getToken().getType() == TokenType.OPERATOR || Procedure.PRIMITIVE_OPERATORS.contains(entry.getToken().getText());
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
        return entry.getToken().getType() == TokenType.ELEMENT
                && !KEYWORDS.contains(entry.getToken().getText())
                && !Procedure.PRIMITIVE_OPERATORS.contains(entry.getToken().getText());
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

    private static Procedure lookupProValue(Entry entry, Environment env) {
        return env.procedure.get(entry.getToken().getText());
    }


    private static boolean primitiveProcedure(Procedure procedure) {
        return procedure.primitiveProcedure();
    }

    public static int getListLength(Entry list,int length){
        if(list.getChildren()!=null) {
            if(list.getChildren().size()==1) return 0;
            if(list.getChildren().size()==2) return 1;
            return getListLength(list.getChildren().get(1),length)+getListLength(list.getChildren().get(2),length);
        }
        else return length;
    }

    private static Entry applyPrimitive(Procedure procedure,Environment env) {
        List<Entry> arguments = procedure.getArguments();
        if (procedure.operator.equals("car"))
            return eval(new Expression(arguments.get(0).getChildren().get(1)),env);
        if (procedure.operator.equals("cdr"))
            return eval(new Expression(arguments.get(0).getChildren().get(2)),env);

        if(procedure.operator.equals("length"))
            return StringToNumberEntry(String.valueOf(getListLength(arguments.get(0),1)));

        if(procedure.operator.equals("null?"))
            return StringToBooleanEntry("null".equals(arguments.get(0).getToken().getText()) ? "#t" : "#f");

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
