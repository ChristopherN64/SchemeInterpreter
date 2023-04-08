package fh.scheme.parser;

import java.io.IOException;

public class Parser {
   private Lexer input;
   protected Token lookahead;
   
   public Parser(Lexer input) {
	   this.input = input;
	   consume();
   }
   
   public void consume() {
	   try {
		lookahead = input.nextToken();
	} catch (IOException e) {
		e.printStackTrace();
	}
   }
   
   public void match(int type) {
	   if(lookahead.getType() == type) {
		   consume();
	   } else throw new RuntimeException("parsing error: expecting " + input.getTokenName(type) 
	   + "; found " + lookahead);
   }
}
