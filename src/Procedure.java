import fh.scheme.parser.Entry;

import java.util.List;

public class Procedure {
    public static List<String> PRIMITIVE_OPERATORS = List.of(new String[]{"car", "cdr", "+", "-", "*", "/", "<", "<=", ">", ">=", "=", "cons", "length", "null?"});
    public String operator;
    public Entry body;

    public String name;
    public List<Entry> variables;
    public List<Entry> arguments;

    public Procedure(Entry operator, Entry definition, Entry body, List<Entry> arguments) {
        this.operator = operator.getToken().getText();
        this.name = definition.getChildren().get(0).getToken().getText();
        this.variables = definition.getChildren().subList(1, definition.getChildren().size());
        this.body = body;
        this.arguments = arguments;
    }

    public Entry getEntry() {
        return body;
    }

    public boolean primitiveProcedure() {
        return PRIMITIVE_OPERATORS.contains(body.getToken().getText());
    }

    public List<Entry> getArguments() {
        return arguments;
    }

    public static List<String> getPrimitiveOperators() {
        return PRIMITIVE_OPERATORS;
    }

    public String getOperator() {
        return operator;
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
}
