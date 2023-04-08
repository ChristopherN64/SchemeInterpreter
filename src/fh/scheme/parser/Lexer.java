package fh.scheme.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public abstract class Lexer {
	private Reader input;
	protected int p;
	protected char c;
	protected boolean eof;
	
	
	public Lexer() {
		p = 0;
		eof = false;
	}
	
	public Lexer(InputStream input) {
		this();
		this.input = new InputStreamReader(input);
		try {
			consume();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected boolean isWhitespace() {
		return (c == '\n') || (c == '\r') || (c == '\t') || (c == ' ');
	}
	
	public void consume() throws IOException {
		int i = input.read();
		if (i == -1) {
			eof = true;
		}
		c = (char) i;
		p++;
	}
	
	public void consumeWS() throws IOException{
		do {
		  consume();
		  
		} while (isWhitespace());
		
	}
	
	
	
	public abstract Token nextToken() throws IOException;
	
	public abstract String getTokenName(int type);

}
