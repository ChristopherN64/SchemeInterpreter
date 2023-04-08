package fh.scheme.parser;

public class Token {
	private int type;
	private String text;
	
	
	public Token(int type, String text) {
		super();
		this.text = text;
		this.type = type;
	}


	public String getText() {
		return text;
	}
	
	public int getType() {
		return type;
	}
	
	public String toString() {
		return "<'" + text + "', " + SchemeLexer.tokenNames[type] + ">";
	}
	

}
