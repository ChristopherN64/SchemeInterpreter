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
        //Self evaluating
        if(!processInput("7").equals("7")) System.out.println(errMsg);

        //Variable
        if(!processInput("(define var 12)").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("var").equals("12")) System.out.println(errMsg);

        //Primitive Functions
        if(!processInput("(+ 1 2)").equals("3")) System.out.println(errMsg);
        if(!processInput("(+ (+ 1 var) (* 10 5))").equals("63")) System.out.println(errMsg);

        //comparison operators
        if(!processInput("(#t)").equals("#t")) System.out.println(errMsg);
        if(!processInput("(#f)").equals("#f")) System.out.println(errMsg);
        if(!processInput("(< 1 2)").equals("#t")) System.out.println(errMsg);
        if(!processInput("(> 1 2)").equals("#f")) System.out.println(errMsg);
        if(!processInput("(<= 1 1)").equals("#t")) System.out.println(errMsg);
        if(!processInput("(>= 1 2)").equals("#f")) System.out.println(errMsg);
        if(!processInput("(= 12 var)").equals("#t")) System.out.println(errMsg);

        //quote
        if(!processInput("(quote (+ 1 2))").equals("( + 1 2 )")) System.out.println(errMsg);
        if(!processInput("'(+ 1 2)").equals("( + 1 2 )")) System.out.println(errMsg);
        if(!processInput("(define quoteVar '(+ 12 3))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("quoteVar").equals("( + 12 3 )")) System.out.println(errMsg);

        //Lists
        if(!processInput("(cons 1 2)").equals("( 1 2 )")) System.out.println(errMsg);
        if(!processInput("(cons 1 (cons 2 3))").equals("( 1 2 3 )")) System.out.println(errMsg);
        if(!processInput("(cons 1 (cons 2 var))").equals("( 1 2 12 )")) System.out.println(errMsg);
        if(!processInput("(car (cons 2 3))").equals("2")) System.out.println(errMsg);
        if(!processInput("(cdr (cons 2 3))").equals("3")) System.out.println(errMsg);
        if(!processInput("(define varList (cons 2 3))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(car varList)").equals("2")) System.out.println(errMsg);
        if(!processInput("(cdr varList)").equals("3")) System.out.println(errMsg);
        if(!processInput("(define varListDeepCar (cons (cons 1 2) 3))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(car varListDeepCar)").equals("( 1 2 )")) System.out.println(errMsg);
        if(!processInput("(define varListDeepCdr (cons 1 (cons 2 3)))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(cdr varListDeepCdr)").equals("( 2 3 )")) System.out.println(errMsg);
        if(!processInput("(car (cdr varListDeepCdr))").equals("2")) System.out.println(errMsg);
        if(!processInput("(define treeVar (cons 1 varList))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(car (cdr treeVar))").equals("2")) System.out.println(errMsg);


        //if and cond
        if(!processInput("(if (< 1 2) 1234 5678)").equals("1234")) System.out.println(errMsg);
        if(!processInput("(if (> 1 2) 1234 5678)").equals("5678")) System.out.println(errMsg);
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
            Entry ret = Interpreter.eval(new Expression(entrys.get(0)), environment);
            String out = ret.getToken().getText();
            if(isList(ret)){
                out ="( "+listToString(ret)+ ")";
            }
            System.out.println(out);
            return out;
        } catch (Throwable r) {
            System.out.println("Ein Fehler ist aufgetreten\n" + Arrays.toString(r.getStackTrace()));
        }
        return "";
    }

    public static boolean isList(Entry entry) {
        if(entry.getChildren()==null || entry.getChildren().size()==0) return false;
        return entry.getChildren().get(0).getToken().getText().equals("list")
                || entry.getChildren().get(0).getToken().getText().equals("cons");
    }
    private static String listToString(Entry list){
            StringBuilder ret = new StringBuilder();
            if (list != null) {
                String text = list.getToken().getText();
                if(!text.equals("list") &&!text.equals("cons") &&!text.equals("(") &&!text.equals(")"))
                    ret.append(text+" ");
                if (list.getChildren() != null) {
                    List<Entry> children = list.getChildren();
                    for (int i = 0; i < children.size(); i++) {
                        Entry g = children.get(i);
                        ret.append(listToString(g));
                    }
                }
            }
            return ret.toString();
    }
}
