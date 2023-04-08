package fh.scheme.parser;

import java.io.IOException;
import java.io.InputStream;

public class SchemeLexer extends Lexer {

	public SchemeLexer(InputStream input) {
		super(input);
	}

	private boolean isStartLetter() {
		return ((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'));
	}

	private boolean isLetter() {
		return isStartLetter() || (c == '!') || (c == '?') || (c == '-');
	}

	private boolean isOperator() {
		return (c == '+') || (c == '-') || (c == '/') || (c == '*') || (c == '=') || (c == '>') || (c == '<');
	}

	private boolean isSeperator() {
		return (c == '(') || (c == ')') || (c == ' ');
	}

	private boolean isBoolean() {
		return (c == '#');
	}

	private boolean isString() {
		return (c == '\"');
	}

	protected Token nextVariable() throws IOException {
		StringBuffer sb = new StringBuffer();

		do {
			sb.append(c);
			consume();
		} while (!eof && isLetter() && !isSeperator());
		Token result = null;

		result = new Token(TokenType.ELEMENT, sb.toString());

		return result;
	}

	protected Token nextNumber() throws IOException {
		StringBuffer sb = new StringBuffer();

		do {
			sb.append(c);
			consume();
		} while (!eof && Character.isDigit(c));

		return new Token(TokenType.NUMBER, sb.toString());
	}

	protected Token nextOperator() throws IOException {
		StringBuffer sb = new StringBuffer();

		do {
			sb.append(c);
			consume();
		} while (!eof && isOperator());

		return new Token(TokenType.OPERATOR, sb.toString());
	}

	protected Token nextBoolean() throws IOException {
		StringBuffer sb = new StringBuffer();
		sb.append(c);
		consume();
		sb.append(c);
		consume();

		return new Token(TokenType.BOOLEAN, sb.toString());
	}

	protected Token nextString() throws IOException {
		StringBuffer sb = new StringBuffer();
		consume();
		while (!isString()) {
			sb.append(c);
			consume();
		}
		consume();
		return new Token(TokenType.STRING, sb.toString());
	}

	public Token nextToken() throws IOException {
		while (!eof) {
			switch (c) {
			case ' ':
			case '\n':
			case '\t':
			case '\r':
				consumeWS();
				continue;
			case '(':
				consume();
				return new Token(TokenType.LPARENTHESIS, "(");
			case ')':
				consume();
				return new Token(TokenType.RPARENTHESIS, ")");
			case '\'':
				consume();
				return new Token(TokenType.QUOTE, "'");
			default:
				if (isStartLetter()) {
					return nextVariable();
				}
				if (Character.isDigit(c)) {
					return nextNumber();
				}
				if (isOperator()) {
					return nextOperator();
				}
				if (isBoolean()) {
					return nextBoolean();
				}
				if (isString()) {
					return nextString();
				}
			}
		}
		return new Token(TokenType.EOF, "EOF");
	}
}
