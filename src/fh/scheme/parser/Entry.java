package fh.scheme.parser;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Entry {
	private Token token;
	private List<Entry> children;
	private boolean leaf;
	private Iterator<Entry> it;
	
	
	public Entry() {
		children = null;
		leaf=true;
		it = null;
	}
	
	
	public Entry(Token token) {
		this();
		this.token = token;
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

	public void addChildren(Entry child) {
		if (children==null) {
			children = new LinkedList<>();
			leaf = false;
		}
		children.add(child);
	}
	
	public Entry next() {
		if (children == null) {
			return null;
		}
		if (it == null) {
			it = children.iterator();
		}
		if (it.hasNext()) {
			return it.next();
		}
		return null;
	}

	@Override
	public String toString() {
		return getToken().getType() + " - " +getToken().getText();
	}
}
