package fh.scheme.parser;

import fh.scheme.interpreter.Environment;

import java.util.LinkedList;
import java.util.List;

public class Entry {
	public void setToken(Token token) {
		this.token = token;
	}
    private EntryType entryType;
	private Token token;
	private List<Entry> children;
    private Environment procedureEnvironment;
    private boolean qoute;

	
	public Entry() {
		children = null;
        qoute=false;
        entryType=EntryType.TOKEN_ENTRY;
	}
	
	
	public Entry(Token token) {
		this();
		this.token = token;
        qoute=false;
        entryType = EntryType.TOKEN_ENTRY;
	}

    public boolean isQoute() {
        return qoute;
    }

    public void setQoute(boolean qoute) {
        this.qoute = qoute;
        if(children!=null) children.forEach((c)-> {
            if(c!=null) c.setQoute(true);
        });
    }
	
	public Token getToken() {
		return token;
	}

	public List< Entry> getChildren(){
		return children;
	}

	public void setChildren(List<Entry> children) {
		this.children = children;
	}

	public void addChildren(Entry child) {
		if (children==null) {
			children = new LinkedList<>();
		}
		children.add(child);
	}

    public Environment getProcedureEnvironment() {
        return procedureEnvironment;
    }

    public void setProcedureEnvironment(Environment procedureEnvironment) {
        this.procedureEnvironment = procedureEnvironment;
    }

    public EntryType getEntryType() {
        return entryType;
    }

    public void setEntryType(EntryType entryType) {
        this.entryType = entryType;
    }

    @Override
	public String toString() {
		return getToken().getType() + " - " +getToken().getText();
	}
}
