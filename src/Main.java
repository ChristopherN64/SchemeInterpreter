import fh.scheme.parser.Entry;
import fh.scheme.parser.SchemeLexer;
import fh.scheme.parser.SchemeParser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class Main {
    private static Environment environment;
    public static void main(String[] args) throws IOException {
        environment = new Environment();
        while (true) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            String in = bufferedReader.readLine();
            processInput(in);
        }
        //(+ (+ 1 2) (* 10 5))
    }

    private static void processInput(String input) {
        try {
            SchemeLexer sl = new SchemeLexer(new ByteArrayInputStream(input.getBytes()));
            SchemeParser sp = new SchemeParser(sl);
            List<Entry> entrys = sp.program();
            System.out.println(Interpreter.eval(new Expression(entrys.get(0)), environment));
        } catch (Throwable r) {
            System.out.println("Ein Fehler ist aufgetreten\n" + Arrays.toString(r.getStackTrace()));
        }

    }
}
