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
        //Tests
        runTests();

        environment = new Environment();
        while (true) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            String in = bufferedReader.readLine();
            processInput(in);
        }
    }

    private static void runTests() {
        String errMsg = "ERROR in Test";
        environment = new Environment();
        if(!processInput("7").equals("7")) System.out.println(errMsg);

        if(!processInput("(define var 12)").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("var").equals("12")) System.out.println(errMsg);

        if(!processInput("(+ 1 2)").equals("3")) System.out.println(errMsg);
        if(!processInput("(+ (+ 1 var) (* 10 5))").equals("63")) System.out.println(errMsg);

        if(!processInput("(#t)").equals("#t")) System.out.println(errMsg);
        if(!processInput("(#f)").equals("#f")) System.out.println(errMsg);
        if(!processInput("(< 1 2)").equals("#t")) System.out.println(errMsg);
        if(!processInput("(> 1 2)").equals("#f")) System.out.println(errMsg);
        if(!processInput("(<= 1 1)").equals("#t")) System.out.println(errMsg);
        if(!processInput("(>= 1 2)").equals("#f")) System.out.println(errMsg);
        if(!processInput("(= 12 var)").equals("#t")) System.out.println(errMsg);

        if(!processInput("(define quoteVar '(+ 12 3))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("quoteVar").equals("( + 12 3 )")) System.out.println(errMsg);


        if(!processInput("(cons 1 2)").equals("(1 2)")) System.out.println(errMsg);
        if(!processInput("(cons 1 (cons 2 3))").equals("(1 (2 3))")) System.out.println(errMsg);
        if(!processInput("(cons 1 (cons 2 var))").equals("(1 (2 12))")) System.out.println(errMsg);
        if(!processInput("(car (cons 2 3))").equals("2")) System.out.println(errMsg);
        if(!processInput("(cdr (cons 2 3))").equals("3")) System.out.println(errMsg);

        if(!processInput("(cond (#f 12) (#f 11) (#t 42))").equals("42")) System.out.println(errMsg);
        if(!processInput("(cond (#f 12) (#t 11) (#t 42))").equals("11")) System.out.println(errMsg);
        if(!processInput("(cond ((= var 12) 123) (#t 11) (#t 42))").equals("123")) System.out.println(errMsg);
        environment = new Environment();
    }

    private static String processInput(String input) {
        try {
            SchemeLexer sl = new SchemeLexer(new ByteArrayInputStream(input.getBytes()));
            SchemeParser sp = new SchemeParser(sl);
            List<Entry> entrys = sp.program();
            String out = Interpreter.eval(new Expression(entrys.get(0)), environment);
            System.out.println(out);
            return out;
        } catch (Throwable r) {
            System.out.println("Ein Fehler ist aufgetreten\n" + Arrays.toString(r.getStackTrace()));
        }
        return "";
    }
}
