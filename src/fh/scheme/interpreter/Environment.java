package fh.scheme.interpreter;

import fh.scheme.parser.Entry;

import java.util.HashMap;

public class Environment {
    HashMap<String, Entry> variables;
    Environment parent;

    public Environment() {
        variables = new HashMap<>();
    }

    public Environment getParent() {
        return parent;
    }

    public void setParent(Environment parent) {
        this.parent = parent;
    }

    public HashMap<String, Entry> getVariables() {
        return variables;
    }
}
