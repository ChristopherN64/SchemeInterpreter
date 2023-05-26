package fh.scheme.interpreter;

import fh.scheme.parser.Entry;
import fh.scheme.parser.EntryType;
import fh.scheme.parser.Token;
import fh.scheme.parser.TokenType;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Interpreter {
    public static List<String> KEYWORDS = List.of(new String[]{"define","set!", "list", "cond", "if", "cons", "list", "quote", "'", "lambda","round","let"});
    public static List<String> PRIMITIVE_OPERATORS = List.of(new String[]{"car", "cdr", "+", "-", "*", "/", "<", "<=", ">", ">=", "=","and","or", "cons", "length", "null?","lambda","round"});
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

        //quoted?
        if (isQuoted(entries.get(0))) {
            return entry;
        }

        if (isPreQuoted(entries.get(0))) {
            Entry internalQuote;
            if (entries.get(1).getChildren() == null) internalQuote = convertListelementsToCons(null, true);
            else internalQuote = convertListelementsToCons(entries.get(1).getChildren(), true);
            internalQuote.setQoute(true);
            return internalQuote;
        }

        //Set
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

        //let?
        if (isLet(entries.get(0))){
            return eval(buildLambdaCallFromLet(entries),env);
        }

        //variable?
        if (isVariable(entries.get(0)) && entries.size()==1) {
            Entry value = lookupVarValue(entries.get(0), env);
            return value;
        }

        //If
        if (isIfCondition(entries.get(0))) {
            if (eval(entries.get(1), env).getToken().getText().equals(T))
                return eval(entries.get(2), env);
            return eval(entries.get(3), env);
        }

        //cond
        if (isCondCondition(entries.get(0))) {
            Entry elseEntry = null;
            if(entries.get(entries.size()-1).getChildren().get(0).getToken().getText().equals("else")){
                elseEntry = entries.get(entries.size()-1);
                entries = entries.subList(0,entries.size()-1);
            }
            for (int i = 1; i < entries.size(); i++) {
                if (eval(entries.get(i).getChildren().get(0), env).getToken().getText().equals(T))
                    return eval(entries.get(i).getChildren().get(1), env);
            }
            if(elseEntry!=null) return evalSequence(elseEntry.getChildren().subList(1,elseEntry.getChildren().size()),env);
            return StringToNumberEntry("");
        }

        //quoted?
        if (isQuoted(entries.get(0))) {
            return entry;
        }

        //isCons
        if(isCons(entries.get(0))){
            return createConsEntry(eval(entries.get(1),env),eval(entries.get(2),env));
        }

        //isList
        if (isList(entries.get(0))) {
            Entry evalList = getLParenthesisEntry();
            evalList.setChildren(new LinkedList<>());
            evalList.addChildren(new Entry(new Token(TokenType.ELEMENT,"list")));
            for (int i = 1; i < entry.getChildren().size(); i++) {
                evalList.getChildren().add(i, eval(entry.getChildren().get(i), env));
            }
            //Rebuild list to nested cons
            return convertListelementsToCons(evalList.getChildren().subList(1,evalList.getChildren().size()),false);
        }

        //isLambda
        if(isLambda(entry)){
            return makeProcedure(entries.subList(2,entries.size()),entries.get(1).getChildren(),env);
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

    public static Entry evalSequence(List<Entry> entries,Environment env){
        entries.subList(0,entries.size()-1).forEach((entry)->eval(entry,env));
        return eval(entries.get(entries.size()-1), env);
    }

    private static Entry buildLambdaCallFromLet(List<Entry> entries) {
        Entry argDefinition = entries.get(1);
        List<Entry> argNames = new LinkedList<>();
        List<Entry> argValues = new LinkedList<>();

        argDefinition.getChildren().forEach((c)->{
            argNames.add(c.getChildren().get(0));
            argValues.add(c.getChildren().get(1));
        });


        Entry lambdaBody = entries.get(2);

        Entry lambdaVars = getLParenthesisEntry();
        lambdaVars.setChildren(new LinkedList<>());
        argNames.forEach((argName)-> lambdaVars.getChildren().add(argName));

        Entry lambda = getLParenthesisEntry();
        lambda.setChildren(new LinkedList<>());
        lambda.getChildren().add(new Entry(new Token(TokenType.ELEMENT,"lambda")));

        lambda.getChildren().add(lambdaVars);
        lambda.getChildren().add(lambdaBody);

        //build Lambda call
        Entry lambdaCall = getLParenthesisEntry();
        lambdaCall.setChildren(new LinkedList<>());
        lambdaCall.getChildren().add(lambda);
        argValues.forEach((argValue)->lambdaCall.getChildren().add(argValue));
        return lambdaCall;
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
            List<Entry> bodys = entry.getChildren().subList(2,entry.getChildren().size());
            Entry args = getLParenthesisEntry();
            args.setChildren(argumentNames);
            //createLambda
            Entry lambda = getLParenthesisEntry();
            lambda.setChildren(new LinkedList<>());
            lambda.getChildren().add(new Entry(new Token(TokenType.ELEMENT,"lambda")));
            lambda.getChildren().add(args);
            lambda.getChildren().addAll(bodys);

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
            List<Entry> bodies = getProcedureBody(procedure);
            return evalSequence(bodies,newEnv);
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

    private static Entry makeProcedure(List<Entry> bodies, List<Entry> vars,Environment environment){
        Entry proc = new Entry();
        proc.setEntryType(EntryType.PROCEDURE);
        proc.setToken(new Token(TokenType.ELEMENT,"procedure"));
        proc.setChildren(new LinkedList<>());
        bodies.forEach(b->b.setEntryType(EntryType.PROCEDURE_BODY));
        proc.getChildren().addAll(bodies);
        vars.forEach((v)->v.setEntryType(EntryType.PROCEDURE_VAR));
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
        if(elements==null || elements.isEmpty()) return createConsEntry(null,null);
        if(quote) elements.set(0,StringToNumberEntry(listToString(elements.get(0),true)));
        return addElementToCons(elements.get(0), elements.subList(1,elements.size()),quote);
    }

    public static Entry addElementToCons(Entry root, List<Entry> elements, boolean quote) {
        if(elements==null || elements.isEmpty()) return createConsEntry(root,null);
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
        return entry.getToken().getText().equals("list");
    }


    private static boolean isCons(Entry entry) {
        return entry.getToken().getText().equals("cons");
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

    public static boolean isPreQuoted(Entry entry) {
        return entry.getToken().getType() == TokenType.QUOTE || entry.getToken().getText().equals("quote");
    }

    public static boolean isQuoted(Entry entry) {
        return entry.isQoute();
    }

    private static boolean isVariable(Entry entry) {
        return entry!=null && entry.getToken().getType() == TokenType.ELEMENT
                && !KEYWORDS.contains(entry.getToken().getText())
                && !PRIMITIVE_OPERATORS.contains(entry.getToken().getText());
    }

    private static boolean isDefinition(Entry entry) {
        return entry.getToken().getType() == TokenType.ELEMENT && entry.getToken().getText().equals("define");
    }

    private static boolean isSet(Entry entry) {
        return entry.getToken().getType() == TokenType.ELEMENT && entry.getToken().getText().equals("set!");
    }

    private static boolean isLet(Entry entry) {
        return entry.getToken().getType() == TokenType.ELEMENT && entry.getToken().getText().equals("let");
    }

    private static boolean isSelfEvaluating(Entry entry) {
        return isNumber(entry) || isBoolean(entry) || PRIMITIVE_OPERATORS.contains(entry.getToken().getText()) || entry.getToken().getType().equals(TokenType.OPERATOR);
    }

    private static boolean isBoolean(Entry entry) {
        return entry.getToken().getType() == TokenType.BOOLEAN;
    }

    private static boolean isNumber(Entry entry) {
        return entry.getToken().getType() == TokenType.NUMBER;
    }

    private static boolean isEmptyList(Entry entry){
        return (entry.getToken().getType()==TokenType.LPARENTHESIS && entry.getChildren()!=null && entry.getChildren().get(1) == null && entry.getChildren().get(2) ==null);
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

    public static List<Entry> getProcedureBody(Entry procedure){
        return procedure.getChildren().stream().filter((e)->e.getEntryType()==EntryType.PROCEDURE_BODY).collect(Collectors.toList());
    }

    public static List<Entry> getProcedureVars(Entry procedure){
        return procedure.getChildren().stream().filter((e)->e.getEntryType()==EntryType.PROCEDURE_VAR).collect(Collectors.toList());
    }

    public static int getListLength(Entry list) {
        if(list == null ||isEmptyList(list)|| (list.getChildren()!=null && list.getChildren().size()==1)) return 0;
        if(list.getChildren() == null) return 1;
        return 1 + getListLength(list.getChildren().get(2));
    }

    private static Entry applyPrimitive(Entry procedure, Environment env, List<Entry> arguments) {
        String operator = procedure.getToken().getText();

        if (operator.equals("car")) {
            if (arguments == null || arguments.size() < 1 || arguments.get(0).getChildren() == null || arguments.get(0).getChildren().size() < 2)
                return null;
            //If quoted return car part as unevaluated String
            if(isQuoted(arguments.get(0))) return StringToNumberEntry(arguments.get(0).getChildren().get(1).getToken().getText());
            //return evaluated car-part
            return eval(arguments.get(0).getChildren().get(1), env);
        }

        if (operator.equals("cdr")) {
            if (arguments == null || arguments.size() < 1 || arguments.get(0).getChildren() == null || arguments.get(0).getChildren().size() < 3)
                return null;

            //If param is quoted return all child but the car-part unevaluated
            if(isQuoted(arguments.get(0))){
                return arguments.get(0).getChildren().get(2);
            }
            //Return evaluated cdr-part
            return eval(arguments.get(0).getChildren().get(2), env);
        }

        if (operator.equals("length"))
            return StringToNumberEntry(String.valueOf(getListLength(arguments.get(0))));

        if (operator.equals("null?"))
            return StringToBooleanEntry(arguments.get(0)==null || isEmptyList(arguments.get(0)) ? T : F);

        if (operator.equals("round"))
            return arguments.get(0);

        if (operator.equals("*"))
            return StringToNumberEntry(arguments.stream().map((e) -> Integer.parseInt(e.getToken().getText().replace(" ",""))).reduce((a, b) -> a * b).get().toString());
        if (operator.equals("/"))
            return StringToNumberEntry(arguments.stream().map((e) -> Integer.parseInt(e.getToken().getText().replace(" ",""))).reduce((a, b) -> a / b).get().toString());
        if (operator.equals("+"))
            return StringToNumberEntry(arguments.stream().map((e) -> Integer.parseInt(e.getToken().getText().replace(" ",""))).reduce((a, b) -> a + b).get().toString());
        if (operator.equals("-"))
            return StringToNumberEntry(arguments.stream().map((e) -> Integer.parseInt(e.getToken().getText().replace(" ",""))).reduce((a, b) -> a - b).get().toString());

        if (operator.equals(">"))
            return StringToBooleanEntry(Integer.parseInt(arguments.get(0).getToken().getText().replace(" ","")) > Integer.parseInt(arguments.get(1).getToken().getText().replace(" ","")) ? T : F);
        if (operator.equals(">="))
            return StringToBooleanEntry(Integer.parseInt(arguments.get(0).getToken().getText().replace(" ","")) >= Integer.parseInt(arguments.get(1).getToken().getText().replace(" ","")) ? T : F);
        if (operator.equals("<"))
            return StringToBooleanEntry(Integer.parseInt(arguments.get(0).getToken().getText().replace(" ","")) < Integer.parseInt(arguments.get(1).getToken().getText().replace(" ","")) ? T : F);
        if (operator.equals("<="))
            return StringToBooleanEntry(Integer.parseInt(arguments.get(0).getToken().getText().replace(" ","")) <= Integer.parseInt(arguments.get(1).getToken().getText().replace(" ","")) ? T : F);
        if (operator.equals("="))
            return StringToBooleanEntry(Integer.parseInt(arguments.get(0).getToken().getText().replace(" ","")) == Integer.parseInt(arguments.get(1).getToken().getText().replace(" ","")) ? T : F);

        if (operator.equals("or")){
            for(Entry arg : arguments){
                if(arg.getToken().getText().replace(" ","").equals(T))  return StringToBooleanEntry(T);
            }
            return StringToBooleanEntry(F);
        }

        if (operator.equals("and")){
            for(Entry arg : arguments){
                if(arg.getToken().getText().replace(" ","").equals(F))  return StringToBooleanEntry(F);
            }
            return StringToBooleanEntry(T);
        }
        return StringToNumberEntry("");
    }

    private static Entry StringToNumberEntry(String s) {
        return new Entry(new Token(TokenType.NUMBER, s));
    }

    private static Entry StringToBooleanEntry(String s) {
        return new Entry(new Token(TokenType.BOOLEAN, s));
    }


    public static String listToString(Entry list, Boolean withListTextElements) {
        if(list==null) return "";
        StringBuilder ret = new StringBuilder();
        if (list != null) {
            String text = list.getToken().getText();
            if(text==null) text = "";
            if (withListTextElements || (!text.equals("list") && !text.equals("cons") && !text.equals("(")))
                ret.append(text).append(" ");
            if (list.getChildren() != null) {
                List<Entry> children = list.getChildren();
                for (int i = 0; i < children.size(); i++) {
                    Entry g = children.get(i);
                    if(g!=null) ret.append(listToString(g,withListTextElements));
                }
                if(withListTextElements) ret.append(" )");
            }
        }
        return ret.toString().replace("  "," ");
    }


    public static Entry getLParenthesisEntry(){
        return new Entry(new Token(TokenType.LPARENTHESIS,"("));
    }
}
