import fh.scheme.parser.Entry;

import java.util.HashMap;

public class Environment {
    HashMap<String, Entry> variables;
    HashMap<String, Procedure> procedure;
    Environment parent;
    Environment child;

    public Environment(){
        variables = new HashMap<>();
        procedure = new HashMap<>();
    }

    public Environment getParent() {
        return parent;
    }

    public void setParent(Environment parent) {
        this.parent = parent;
    }

    public Environment getChild() {
        return child;
    }

    public void setChild(Environment child) {
        this.child = child;
    }

    public HashMap<String, Entry> getVariables() {
        return variables;
    }
}
