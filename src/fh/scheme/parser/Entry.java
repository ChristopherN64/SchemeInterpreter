package fh.scheme.parser;

import fh.scheme.interpreter.Environment;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Entry {
	public void setToken(Token token) {
		this.token = token;
	}

	private Token token;
	private List<Entry> children;
	private boolean leaf;
	private Iterator<Entry> it;
    private Environment procedureEnvironment;
	
	
	public Entry() {
		children = null;
		leaf=true;
		it = null;
	}
	
	
	public Entry(Token token) {
		this();
		this.token = token;
		leaf = token.getType() != TokenType.LPARENTHESIS;
	}
	
	public boolean isLeaf() {
		return leaf;
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
			leaf = false;
		}
		children.add(child);
	}

    public Environment getProcedureEnvironment() {
        return procedureEnvironment;
    }

    public void setProcedureEnvironment(Environment procedureEnvironment) {
        this.procedureEnvironment = procedureEnvironment;
    }

	@Override
	public String toString() {
		return getToken().getType() + " - " +getToken().getText();
	}
}
