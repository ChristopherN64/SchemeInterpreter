package fh.scheme.parser;

import java.util.LinkedList;
import java.util.List;

public class SchemeParser extends Parser {
	private List<Entry> progast;
	private Entry ast;
	private Entry currententry;

	public SchemeParser(Lexer input) {
		super(input);
		progast = new LinkedList<>();
		ast = null;
		currententry = null;
	}

	public List<Entry> program() {
		while (lookahead.getType() != SchemeLexer.EOF) {
			form();
			progast.add(ast);
			ast = null;
			currententry = null;
		}
		return progast;
	}

	@Override
	public void match(int type) {
		if ((type != SchemeLexer.LPARENTHESIS) && (type != SchemeLexer.RPARENTHESIS)) {
			Entry neu = new Entry(lookahead);
			if (currententry != null) {
				currententry.addChildren(neu);
			} else {
				ast = neu;
			}
		}
		super.match(type);
	}

	public void element() {
		switch (lookahead.getType()) {
		case SchemeLexer.LPARENTHESIS:
			liste();
			break;
		case SchemeLexer.ELEMENT:
			match(SchemeLexer.ELEMENT);
			break;
		case SchemeLexer.NUMBER:
			match(SchemeLexer.NUMBER);
			break;
		case SchemeLexer.BOOLEAN:
			match(SchemeLexer.BOOLEAN);
			break;
		case SchemeLexer.STRING:
			match(SchemeLexer.STRING);
			break;
		case SchemeLexer.OPERATOR:
			match(SchemeLexer.OPERATOR);
			break;
		case SchemeLexer.QUOTE:
			quoted();
			break;
		default:
			throw new RuntimeException("SchemeParser: no valid element; read" + lookahead);

		}
		if (lookahead.getType() == SchemeLexer.LPARENTHESIS) {
			liste();
		}
	}

	public void elements() {
		while (lookahead.getType() != SchemeLexer.RPARENTHESIS) {
			element();
		}
	}
	
	public void quoted() {
		Entry save = currententry;
		
		currententry = new Entry(new Token(SchemeLexer.LPARENTHESIS, "("));
		match(SchemeLexer.QUOTE);
		switch(lookahead.getType()) {
		case SchemeLexer.LPARENTHESIS: liste(); break;
		case SchemeLexer.ELEMENT: element(); break;
		}
		if (save != null) {
			save.addChildren(currententry);
			currententry = save;
		} else {
			ast = currententry;
		}
	}

	public void liste() {

		Entry save = currententry;

		currententry = new Entry(lookahead);
		match(SchemeLexer.LPARENTHESIS);
		elements();
		match(SchemeLexer.RPARENTHESIS);
		if (save != null) {
			save.addChildren(currententry);
			currententry = save;
		} else {
			ast = currententry;
		}
	}

	public void form() {
		switch (lookahead.getType()) {
		case SchemeLexer.LPARENTHESIS:
			liste();
			break;
		case SchemeLexer.ELEMENT:
			element();
			break;
		case SchemeLexer.NUMBER:
		    element();
		    break;
		case SchemeLexer.EOF:
			break;
		default:
			throw new RuntimeException("SchemeParser: invalid form; read " + lookahead);
		}
	}
}
