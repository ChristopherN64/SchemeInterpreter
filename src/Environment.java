import java.util.HashMap;

public class Environment {
    HashMap<String,String> variables;
    Environment parent;
    Environment child;

    public Environment(){
        variables = new HashMap<>();
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

    public HashMap<String, String> getVariables() {
        return variables;
    }
}
