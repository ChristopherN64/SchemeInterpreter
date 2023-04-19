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
        while (lookahead.getType() != TokenType.EOF) {
            form();
            progast.add(ast);
            ast = null;
            currententry = null;
        }
        return progast;
    }

    @Override
    public void match(TokenType type) {
        if ((type != TokenType.LPARENTHESIS) && (type != TokenType.RPARENTHESIS)) {
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
            case LPARENTHESIS:
                liste();
                break;
            case ELEMENT:
                match(TokenType.ELEMENT);
                break;
            case NUMBER:
                match(TokenType.NUMBER);
                break;
            case BOOLEAN:
                match(TokenType.BOOLEAN);
                break;
            case STRING:
                match(TokenType.STRING);
                break;
            case OPERATOR:
                match(TokenType.OPERATOR);
                break;
            case QUOTE:
                lookahead.setType(TokenType.ELEMENT);
                quoted();
                break;
            default:
                throw new RuntimeException("SchemeParser: no valid element; read" + lookahead);

        }
        if (lookahead.getType() == TokenType.LPARENTHESIS) {
            liste();
        }
    }

    public void elements() {
        while (lookahead.getType() != TokenType.RPARENTHESIS) {
            element();
        }
    }

    public void quoted() {
        Entry save = currententry;

        currententry = new Entry(new Token(TokenType.LPARENTHESIS, "("));
        match(TokenType.ELEMENT);
        switch (lookahead.getType()) {
            case LPARENTHESIS:
                liste();
                break;
            case ELEMENT:
                element();
                break;
            case NUMBER:
                element();
                break;
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
        match(TokenType.LPARENTHESIS);
        elements();
        match(TokenType.RPARENTHESIS);
        if (save != null) {
            save.addChildren(currententry);
            currententry = save;
        } else {
            ast = currententry;
        }
    }

    public void form() {
        switch (lookahead.getType()) {
            case LPARENTHESIS:
                liste();
                break;
            case ELEMENT:
                element();
                break;
            case NUMBER:
                element();
                break;
            case EOF:
                break;
            case BOOLEAN:
                element();
                break;
            case QUOTE:
                element();
                break;
            case STRING:
                element();
                break;
            default:
                throw new RuntimeException("SchemeParser: invalid form; read " + lookahead);
        }
    }
}
