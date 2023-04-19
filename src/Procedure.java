import fh.scheme.parser.Entry;

import java.util.List;

public class Procedure {
    public static List<String> PRIMITIVE_OPERATORS = List.of(new String[]{"car","cdr","+", "-", "*", "/","<","<=",">",">=","=","cons"});
    public Entry entry;
    public String operator;

    public Procedure(Entry entry) {
        this.entry = entry;
        operator = entry.getToken().getText();
    }

    public Entry getEntry() {
        return entry;
    }

    public boolean primitiveProcedure() {
        return PRIMITIVE_OPERATORS.contains(entry.getToken().getText());
    }
}
