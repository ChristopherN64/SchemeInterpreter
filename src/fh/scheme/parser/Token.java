package fh.scheme.parser;

public class Token {
	private TokenType type;
	private String text;
	
	
	public Token(TokenType type, String text) {
		super();
		this.text = text;
		this.type = type;
	}


	public String getText() {
		return text;
	}
	
	public TokenType getType() {
		return type;
	}
	
	public String toString() {
		return "<'" + text + "', " + type.name() + ">";
	}
	

}
