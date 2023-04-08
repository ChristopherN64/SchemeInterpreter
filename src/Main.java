import fh.scheme.parser.ASTPrinter;
import fh.scheme.parser.Entry;
import fh.scheme.parser.SchemeLexer;
import fh.scheme.parser.SchemeParser;

import java.io.ByteArrayInputStream;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String source = "(+ (+ 30 2) 10)";
        SchemeLexer sl = new SchemeLexer(new ByteArrayInputStream(source.getBytes()));
        SchemeParser sp = new SchemeParser(sl);
        List<Entry> entrys = sp.program();
        ASTPrinter.traverse("",entrys.get(0));
    }
}
