import fh.scheme.parser.ASTPrinter;
import fh.scheme.parser.Entry;
import fh.scheme.parser.SchemeLexer;
import fh.scheme.parser.SchemeParser;

import java.io.ByteArrayInputStream;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String source = "(define (last l)" +
                "  (if (null? (cdr l))" +
                "      (car l)" +
                "      (last (cdr l)))" +
                "  )";
        SchemeLexer sl = new SchemeLexer(new ByteArrayInputStream(source.getBytes()));
        SchemeParser sp = new SchemeParser(sl);
        List<Entry> entrys = sp.program();
        ASTPrinter.traverse("",entrys.get(0));
    }
}
