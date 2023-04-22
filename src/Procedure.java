import fh.scheme.parser.Entry;

import java.util.List;

public class Procedure {
    public static List<String> PRIMITIVE_OPERATORS = List.of(new String[]{"car", "cdr", "+", "-", "*", "/", "<", "<=", ">", ">=", "=", "cons", "length", "null?","lambda"});
    public Entry body;

    public String name;
    public List<Entry> variables;

    public Procedure(Entry definition, Entry body) {
        this.name = definition.getChildren().get(0).getToken().getText();
        this.variables = definition.getChildren().subList(1, definition.getChildren().size());
        this.body = body;
    }

    public Procedure(List<Entry> variables, Entry body){
        this.name = "lambda";
        this.variables = variables;
        this.body = body;
    }

    public Entry getEntry() {
        return body;
    }

    public boolean primitiveProcedure() {
        return PRIMITIVE_OPERATORS.contains(body.getToken().getText());
    }

    public static List<String> getPrimitiveOperators() {
        return PRIMITIVE_OPERATORS;
    }

    public Entry getBody() {
        return body;
    }

    public String getName() {
        return name;
    }

    public List<Entry> getVariables() {
        return variables;
    }

    public void setName(String name) {
        this.name=name;
    }
}
