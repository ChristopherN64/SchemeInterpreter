package fh.scheme.interpreter;

import fh.scheme.parser.Entry;
import fh.scheme.parser.Token;
import fh.scheme.parser.TokenType;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Interpreter {
    public static List<String> KEYWORDS = List.of(new String[]{"define","set!", "list", "cond", "if", "cons", "list", "quote", "'", "lambda","round"});
    public static List<String> PRIMITIVE_OPERATORS = List.of(new String[]{"car", "cdr", "+", "-", "*", "/", "<", "<=", ">", ">=", "=", "cons", "length", "null?","lambda","round"});
    public static String T = "#t";
    public static String F = "#f";

    public static Entry eval(Entry entry, Environment env) {
        if(entry==null) return entry;
        //Unwrap
        List<Entry> entries = new LinkedList<>();
        if (entry.getToken().getType() == TokenType.LPARENTHESIS) {
            entries.addAll(entry.getChildren());
        } else {
            entries.add(entry);
        }

        //Self Evaluating?
        if (entries.size() == 1 && isSelfEvaluating(entries.get(0))) return entries.get(0);
        
        if(isSet(entries.get(0))){
            String varName = getVarName(entries.get(1));
            if(setVariableIfExists(varName,eval(getVarBody(entry),env),env)) return StringToNumberEntry("Saved!");
            return StringToNumberEntry("Variable "+varName+" not found!");
        }

        //definition?
        if (isDefinition(entries.get(0))) {
            putVariable(getVarName(entries.get(1)),eval(getVarBody(entry),env),env);
            return StringToNumberEntry("Saved!");
        }

        //variable?
        if (isVariable(entries.get(0)) && entries.size()==1) {
            Entry value = lookupVarValue(entries.get(0), env);
            if(value !=null) return value;
            return StringToNumberEntry("Variable undefined");
        }

        //If
        if (isIfCondition(entries.get(0))) {
            if (eval(entries.get(1), env).getToken().getText().equals(T))
                return eval(entries.get(2), env);
            return eval(entries.get(3), env);
        }

        //cond
        if (isCondCondition(entries.get(0))) {
            for (int i = 1; i < entries.size(); i++) {
                if (eval(entries.get(i).getChildren().get(0), env).getToken().getText().equals(T))
                    return eval(entries.get(i).getChildren().get(1), env);
            }
            return StringToNumberEntry("");
        }

        //quoted?
        if (isQuoted(entries.get(0))) {
            entry.getChildren().set(1,convertListelementsToCons(entries.get(1).getChildren(),true));
            return entry;
        }

        //isList
        if (isList(entries.get(0))) {
            for (int i = 1; i < entry.getChildren().size(); i++) {
                entry.getChildren().set(i, eval(entries.get(i), env));
            }
            //Rebuild list to nested cons
            if (entry.getChildren().size() > 3) {
                return convertListelementsToCons(entries.subList(1,entries.size()),false);
            }
            if (entry.getChildren().size()==2) entry.getChildren().add(null);
            return entry;
        }

        //isLambda
        if(isLambda(entry)){
            return makeProcedure(entries.get(2),entries.get(1).getChildren(),env);
        }

        //Application?
        if (isApplication(entries.get(0))) {
            Entry proc = eval(entries.get(0),env);
            List<Entry> arguments = entries.subList(1, entries.size());
            arguments.replaceAll(e -> eval(e, env));
            return apply(proc, env, arguments);
        }
        return StringToNumberEntry("EVAL - ERROR");
    }

    private static String getVarName(Entry definition){
        String name;
        if(definition.getToken().getType().equals(TokenType.LPARENTHESIS)){
            name = definition.getChildren().get(0).getToken().getText();
        }
        else name = definition.getToken().getText();
        return name;
    }

    public static Entry getVarBody(Entry entry){
        List<Entry> argumentNames = new LinkedList<>();
        Entry body;

        //Var
        if(entry.getChildren().get(1).getToken().getType().equals(TokenType.LPARENTHESIS)){
            if(entry.getChildren().get(1).getChildren().size()>1) argumentNames = entry.getChildren().get(1).getChildren().subList(1, entry.getChildren().get(1).getChildren().size());
        }
        body = entry.getChildren().get(2);

        if(body.getChildren()!=null && body.getChildren().size()>1 && !argumentNames.isEmpty()){
            Entry args = new Entry(new Token(TokenType.LPARENTHESIS,"("));
            args.setChildren(argumentNames);
            //createLambda
            Entry lambda = new Entry(new Token(TokenType.LPARENTHESIS,"("));
            lambda.setChildren(new LinkedList<>());
            lambda.getChildren().add(new Entry(new Token(TokenType.ELEMENT,"lambda")));
            lambda.getChildren().add(args);
            lambda.getChildren().add(body);

            body = lambda;
        }
        return body;
    }

    public static Entry apply(Entry procedure, Environment environment, List<Entry> arguments) {
        if (primitiveProcedure(procedure)) return applyPrimitive(procedure, environment, arguments);
        else {
            //Extend fh.scheme.interpreter.Environment (Put procedure Arguments in new SubEnvironment
            Environment newEnv = extendEnvironment(getProcedureVars(procedure).stream().map((e)->e.getToken().getText()).collect(Collectors.toList()), arguments,procedure.getProcedureEnvironment());
            //Eval ProcedureBody
            return eval(getProcedureBody(procedure), newEnv);
        }
    }

    public static Environment extendEnvironment(List<String> variables,List<Entry> values, Environment baseEnv){
        Environment newEnv = new Environment();
        newEnv.setParent(baseEnv);
        //Put var Values in env
        for (int i = 0; i < values.size(); i++) {
            putVariable(variables.get(i), values.get(i),newEnv);
        }
        return newEnv;
    }

    private static Entry makeProcedure(Entry body, List<Entry> vars,Environment environment){
        Entry proc = new Entry();
        proc.setToken(new Token(TokenType.PROCEDURE,"procedure"));
        proc.setChildren(new LinkedList<>());
        proc.getChildren().add(body);
        proc.getChildren().addAll(vars);
        proc.setProcedureEnvironment(environment);
        return proc;
    }

    private static void putVariable(String name,Entry var, Environment environment) {
        environment.variables.put(name, var);
    }


    private static boolean setVariableIfExists(String name,Entry var, Environment environment) {
        if(environment==null) return false;
        if(environment.getVariables().get(name) != null){
            environment.getVariables().put(name,var);
            return true;
        }
        return setVariableIfExists(name,var,environment.getParent());
    }

    private static Entry convertListelementsToCons(List<Entry> elements, boolean quote) {
        if(quote) elements.set(0,StringToNumberEntry(listToString(elements.get(0),true)));
        return addElementToCons(elements.get(0), elements.subList(1,elements.size()),quote);
    }

    public static Entry addElementToCons(Entry root, List<Entry> elements, boolean quote) {
        //If list is quoted, don't eval elements but convert them to String
        if(quote) elements.set(0,StringToNumberEntry(listToString(elements.get(0),true)));
        if (elements.size() == 1)
            return createConsEntry(root, createConsEntry(elements.get(0), null));
        return createConsEntry(root, addElementToCons(elements.get(0), elements.subList(1, elements.size()),quote));
    }

    public static Entry createEmptyConsEntry() {
        Entry entry = new Entry();
        entry.setToken(new Token(TokenType.LPARENTHESIS, "("));
        entry.setChildren(new LinkedList<>());
        entry.getChildren().add(new Entry(new Token(TokenType.ELEMENT, "cons")));
        return entry;
    }

    public static Entry createConsEntry(Entry child1, Entry child2) {
        Entry entry = createEmptyConsEntry();
        entry.getChildren().add(child1);
        entry.getChildren().add(child2);
        return entry;
    }

    private static boolean isLambda(Entry entry) {
        return(entry.getChildren() != null
                && entry.getChildren().size() > 0
                && entry.getChildren().get(0).getToken().getText().equals("lambda"));
    }

    public static boolean isList(Entry entry) {
        return entry.getToken().getText().equals("list") || entry.getToken().getText().equals("cons");
    }

    private static boolean isApplication(Entry entry) {
        return true;
    }

    private static boolean isIfCondition(Entry entry) {
        return entry.getToken().getText().equals("if");
    }

    private static boolean isCondCondition(Entry entry) {
        return entry.getToken().getText().equals("cond");
    }

    public static boolean isQuoted(Entry entry) {
        return entry.getToken().getType() == TokenType.QUOTE || entry.getToken().getText().equals("quote");
    }

    private static boolean isVariable(Entry entry) {
        return entry.getToken().getType() == TokenType.ELEMENT
                && !KEYWORDS.contains(entry.getToken().getText())
                && !PRIMITIVE_OPERATORS.contains(entry.getToken().getText());
    }

    private static boolean isDefinition(Entry entry) {
        return entry.getToken().getType() == TokenType.ELEMENT && entry.getToken().getText().equals("define");
    }

    private static boolean isSet(Entry entry) {
        return entry.getToken().getType() == TokenType.ELEMENT && entry.getToken().getText().equals("set!");
    }

    private static boolean isSelfEvaluating(Entry entry) {
        return isNumber(entry) || isBoolean(entry) || PRIMITIVE_OPERATORS.contains(entry.getToken().getText()) || entry.getToken().getType().equals(TokenType.OPERATOR );
    }

    private static boolean isBoolean(Entry entry) {
        return entry.getToken().getType() == TokenType.BOOLEAN;
    }

    private static boolean isNumber(Entry entry) {
        return entry.getToken().getType() == TokenType.NUMBER;
    }

    private static Entry lookupVarValue(Entry entry, Environment env) {
        if(env==null) return null;
        Entry val = env.variables.get(entry.getToken().getText());
        if(val!=null) return val;
        return lookupVarValue(entry,env.getParent());
    }

    private static boolean primitiveProcedure(Entry procedure) {
        return PRIMITIVE_OPERATORS.contains(procedure.getToken().getText());
    }

    public static Entry getProcedureBody(Entry procedure){
        return procedure.getChildren().get(0);
    }

    public static List<Entry> getProcedureVars(Entry procedure){
        return procedure.getChildren().subList(1,procedure.getChildren().size());
    }

    public static int getListLength(Entry list) {
        if(list == null || (list.getChildren()!=null && list.getChildren().size()==1)) return 0;
        if(list.getChildren() == null
                || list.getChildren().size() == 2
                || list.getChildren().get(2)==null) return 1;
        return getListLength(list.getChildren().get(1)) + getListLength(list.getChildren().get(2));
    }

    private static Entry applyPrimitive(Entry procedure, Environment env, List<Entry> arguments) {
        String operator = procedure.getToken().getText();

        if (operator.equals("car")) {
            if (arguments == null || arguments.size() < 1 || arguments.get(0).getChildren() == null || arguments.get(0).getChildren().size() < 2)
                return null;
            //If quoted return car part as unevaluated String
            if(isQuoted(arguments.get(0).getChildren().get(0))) return StringToNumberEntry(arguments.get(0).getChildren().get(1).getChildren().get(1).getToken().getText());
            //return evaluated car-part
            return eval(arguments.get(0).getChildren().get(1), env);
        }

        if (operator.equals("cdr")) {
            if (arguments == null || arguments.size() < 1 || arguments.get(0).getChildren() == null || (arguments.get(0).getChildren().size() < 3 && !isQuoted(arguments.get(0).getChildren().get(0))))
                return null;

            //If param is quoted return all child but the car-part unevaluated
            if(isQuoted(arguments.get(0).getChildren().get(0))){
                return arguments.get(0).getChildren().get(1).getChildren().get(2);
            }
            //Return evaluated cdr-part
            return eval(arguments.get(0).getChildren().get(2), env);
        }

        if (operator.equals("length"))
            return StringToNumberEntry(String.valueOf(getListLength(arguments.get(0))));

        if (operator.equals("null?"))
            return StringToBooleanEntry(arguments.get(0)==null ? T : F);

        if (operator.equals("round"))
            return arguments.get(0);

        if (operator.equals("*"))
            return StringToNumberEntry(arguments.stream().map((e) -> Integer.parseInt(e.getToken().getText())).reduce((a, b) -> a * b).get().toString());
        if (operator.equals("/"))
            return StringToNumberEntry(arguments.stream().map((e) -> Integer.parseInt(e.getToken().getText())).reduce((a, b) -> a / b).get().toString());
        if (operator.equals("+"))
            return StringToNumberEntry(arguments.stream().map((e) -> Integer.parseInt(e.getToken().getText())).reduce((a, b) -> a + b).get().toString());
        if (operator.equals("-"))
            return StringToNumberEntry(arguments.stream().map((e) -> Integer.parseInt(e.getToken().getText())).reduce((a, b) -> a - b).get().toString());

        if (operator.equals(">"))
            return StringToBooleanEntry(Integer.parseInt(arguments.get(0).getToken().getText()) > Integer.parseInt(arguments.get(1).getToken().getText()) ? T : F);
        if (operator.equals(">="))
            return StringToBooleanEntry(Integer.parseInt(arguments.get(0).getToken().getText()) >= Integer.parseInt(arguments.get(1).getToken().getText()) ? T : F);
        if (operator.equals("<"))
            return StringToBooleanEntry(Integer.parseInt(arguments.get(0).getToken().getText()) < Integer.parseInt(arguments.get(1).getToken().getText()) ? T : F);
        if (operator.equals("<="))
            return StringToBooleanEntry(Integer.parseInt(arguments.get(0).getToken().getText()) <= Integer.parseInt(arguments.get(1).getToken().getText()) ? T : F);
        if (operator.equals("="))
            return StringToBooleanEntry(Integer.parseInt(arguments.get(0).getToken().getText()) == Integer.parseInt(arguments.get(1).getToken().getText()) ? T : F);

        return StringToNumberEntry("");
    }

    private static Entry StringToNumberEntry(String s) {
        return new Entry(new Token(TokenType.NUMBER, s));
    }

    private static Entry StringToBooleanEntry(String s) {
        return new Entry(new Token(TokenType.BOOLEAN, s));
    }


    public static String listToString(Entry list, Boolean withListTextElements) {
        StringBuilder ret = new StringBuilder();
        if (list != null) {
            String text = list.getToken().getText();
            if (withListTextElements || (!text.equals("list") && !text.equals("cons") && !text.equals("(")))
                ret.append(text + " ");
            if (list.getChildren() != null) {
                List<Entry> children = list.getChildren();
                for (int i = 0; i < children.size(); i++) {
                    Entry g = children.get(i);
                    ret.append(listToString(g,withListTextElements));
                }
                if(withListTextElements) ret.append(" )");
            }
        }
        return ret.toString().replace("  "," ");
    }

}
