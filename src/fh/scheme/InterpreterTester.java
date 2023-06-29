package fh.scheme;

import fh.scheme.interpreter.Environment;

import static fh.scheme.Main.processInput;

public class InterpreterTester {
    private static String errMsg = "-----------------------ERROR in Test------------------------------";
    public static void testAll(){
        runSingleTests();
        //runTestFiles();
    }

    public static void runTestFiles(){
        //Test files
        System.out.println("\nSimple");
        processInput("load C:\\Users\\nieder\\Downloads\\sbltest\\sbltest\\simple.scm");
        System.out.println("\nrecursion");
        processInput("load C:\\Users\\nieder\\Downloads\\sbltest\\sbltest\\recursion.scm");
        System.out.println("\nliste");
        processInput("load C:\\Users\\nieder\\Downloads\\sbltest\\sbltest\\liste.scm");
        System.out.println("\nlet");
        processInput("load C:\\Users\\nieder\\Downloads\\sbltest\\sbltest\\let.scm");
        System.out.println("\ncurry");
        processInput("load C:\\Users\\nieder\\Downloads\\sbltest\\sbltest\\curry.scm");
        System.out.println("\nclosure");
        processInput("load C:\\Users\\nieder\\Downloads\\sbltest\\sbltest\\closure.scm");
        System.out.println("\nabstraction");
        processInput("load C:\\Users\\nieder\\Downloads\\sbltest\\sbltest\\abstraction.scm");
        System.out.println("\nmergesort");
        processInput("load C:\\Users\\nieder\\Downloads\\sbltest\\sbltest\\mergesort.scm");
    }

    public static void runSingleTests(){
        Main.environment = new Environment();


        //Self evaluating
        System.out.println("\nSelf evaluating Tests");
        if(!processInput("7").equals("7")) System.out.println(errMsg);
        if(!processInput("#t").equals("#t")) System.out.println(errMsg);

        //Variable definition
        System.out.println("\nVariable Tests");
        Main.environment = new Environment();
        if(!processInput("(define var 12)").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("var").equals("12")) System.out.println(errMsg);

        //Set!
        System.out.println("\nSet! Tests");
        Main.environment = new Environment();
        if(!processInput("notDef").equals("null")) System.out.println(errMsg);
        if(!processInput("(set! notDef 12)").equals("Variable notDef not found!")) System.out.println(errMsg);
        if(!processInput("(define notDef 12)").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(set! notDef 42)").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("notDef").equals("42")) System.out.println(errMsg);

        //let
        System.out.println("\nLet Tests");
        Main.environment = new Environment();
        if(!processInput("(let ((a (list 1 2 3))    (b (list 4 5 6)))         (cons a b))").equals("( 1 2 3 4 5 6 )")) System.out.println(errMsg);
        if(!processInput("((lambda (a b) (cons a b)) (list 1 2 3) (list 4 5 6))").equals("( 1 2 3 4 5 6 )")) System.out.println(errMsg);
        if(!processInput("(define (foo x y) (+ x y))").equals("Saved!")) System.out.println(errMsg);
        //if(!processInput("(define (foo x y) (- 12 2) (+ x y))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(let ((x 10) (y 20))(foo x y))").equals("30")) System.out.println(errMsg);

        //Primitive Functions
        System.out.println("\nPrimitive functions Tests");
        Main.environment = new Environment();
        if(!processInput("(+ 1 2)").equals("3")) System.out.println(errMsg);
        if(!processInput("(define var 12)").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(+ (+ 1 var) (* 10 5))").equals("63")) System.out.println(errMsg);

        //comparison operators
        System.out.println("\ncomparison operators Tests");
        Main.environment = new Environment();
        if(!processInput("(#t)").equals("#t")) System.out.println(errMsg);
        if(!processInput("(#f)").equals("#f")) System.out.println(errMsg);
        if(!processInput("(< 1 2)").equals("#t")) System.out.println(errMsg);
        if(!processInput("(> 1 2)").equals("#f")) System.out.println(errMsg);
        if(!processInput("(<= 1 1)").equals("#t")) System.out.println(errMsg);
        if(!processInput("(>= 1 2)").equals("#f")) System.out.println(errMsg);
        if(!processInput("(define var 12)").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(= 12 var)").equals("#t")) System.out.println(errMsg);

        //quote
        System.out.println("\nQuote Tests");
        Main.environment = new Environment();
        if(!processInput("(quote (+ 1 2))").equals("( + 1 2 )")) System.out.println(errMsg);
        if(!processInput("'(+ 1 2)").equals("( + 1 2 )")) System.out.println(errMsg);
        if(!processInput("(define quoteVar '(+ 12 3))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("quoteVar").equals("( + 12 3 )")) System.out.println(errMsg);
        if(!processInput("(car (quote (lambda (x) (x+1))))").equals("lambda ")) System.out.println(errMsg);
        if(!processInput("(cdr (quote (lambda (x) (x+1))))").equals("( ( x ) ( x + 1 ) )")) System.out.println(errMsg);
        if(!processInput("(cdr (quote (lambda (x) (+ 1 (* 3 4)))))").equals("( ( x ) ( + 1 ( * 3 4 ) ) )")) System.out.println(errMsg);
        if(!processInput("(define qx (quote (list 1 2 3 4)))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("qx").equals("( list 1 2 3 4 )")) System.out.println(errMsg);
        if(!processInput("(car qx)").equals("list ")) System.out.println(errMsg);
        if(!processInput("(cdr qx)").equals("( 1 2 3 4 )")) System.out.println(errMsg);

        //Lists
        System.out.println("\nList Tests");
        Main.environment = new Environment();
        if(!processInput("(define var 12)").equals("Saved!")) System.out.println(errMsg);
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

        if(!processInput("(list )").equals("( )")) System.out.println(errMsg);
        if(!processInput("(define x 3)").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(define l (list 1 2 x 4))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(car l)").equals("1")) System.out.println(errMsg);
        if(!processInput("(cdr l)").equals("( 2 3 4 )")) System.out.println(errMsg);
        if(!processInput("(cdr (list 1))").equals("null")) System.out.println(errMsg);
        if(!processInput("(car (list ))").equals("null")) System.out.println(errMsg);
        if(!processInput("(car (cdr l))").equals("2")) System.out.println(errMsg);
        if(!processInput("(cdr (cdr (cdr l)))").equals("( 4 )")) System.out.println(errMsg);

        if(!processInput("'()").equals("( )")) System.out.println(errMsg);
        if(!processInput("'(1 2 3 4 5)").equals("( 1 2 3 4 5 )")) System.out.println(errMsg);
        if(!processInput("(car '(1 2 3 4 5))").equals("1 ")) System.out.println(errMsg);
        if(!processInput("(cdr '(1 2 3 4 5))").equals("( 2 3 4 5 )")) System.out.println(errMsg);

        //if and cond
        System.out.println("\nIf / Cond Tests");
        Main.environment = new Environment();
        if(!processInput("(if (< 1 2) 1234 5678)").equals("1234")) System.out.println(errMsg);
        if(!processInput("(if (> 1 2) 1234 5678)").equals("5678")) System.out.println(errMsg);
        if(!processInput("(cond ((< 12 1)(12)) ((= 34 44) 44 ))").equals("")) System.out.println(errMsg);
        if(!processInput("(cond (#f 12) (#f 11) (#t 42))").equals("42")) System.out.println(errMsg);
        if(!processInput("(cond (#f 12) (#t 11) (#t 42))").equals("11")) System.out.println(errMsg);
        if(!processInput("(define var 12)").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(cond ((= var 12) 123) (#t 11) (#t 42))").equals("123")) System.out.println(errMsg);
        if(!processInput("(cond (#f 12) (#f 11) (else (+ 1 2)))").equals("3")) System.out.println(errMsg);
        if(!processInput("(cond (#f 12) (#f 11) (else (+ 1 2) (* 1 2)))").equals("2")) System.out.println(errMsg);
        if(!processInput("(cond (#f 12) (#f 11) (else (define var 20) (* 2 var)))").equals("40")) System.out.println(errMsg);

        //Length
        System.out.println("\nLength Tests");
        Main.environment = new Environment();
        if(!processInput("(define l (cons 1 (cons 2 (cons 3 4))))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(length l)").equals("4")) System.out.println(errMsg);
        if(!processInput("(define l (list 1 2 3 4))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(length l)").equals("4")) System.out.println(errMsg);
        if(!processInput("(define l (list 1 2 3 4 5))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(length l)").equals("5")) System.out.println(errMsg);
        if(!processInput("(define l (list 1))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(length l)").equals("1")) System.out.println(errMsg);
        if(!processInput("(define l (list ))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(length l)").equals("0")) System.out.println(errMsg);

        if(!processInput("(define l (list 1 2 3))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(define u (list 4 5))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(define x (list l u))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(length x)").equals("2")) System.out.println(errMsg);

        //null?
        System.out.println("\nnull? Tests");
        Main.environment = new Environment();
        if(!processInput("(define x 42)").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(null? x)").equals("#f")) System.out.println(errMsg);
        if(!processInput("p").equals("null")) System.out.println(errMsg);
        if(!processInput("(null? p)").equals("#t")) System.out.println(errMsg);

        //begin?
        System.out.println("\nbegin Tests");
        Main.environment = new Environment();
        if(!processInput("(begin (define x 12) (set! x 48)(* x 2))").equals("96")) System.out.println(errMsg);

        //round
        System.out.println("\nround Tests");
        Main.environment = new Environment();
        if(!processInput("(define var 12)").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(round (cond ((= var 12) 123) (#t 11) (#t 42)))").equals("123")) System.out.println(errMsg);

        //procedure
        System.out.println("\nprocedure Tests");
        Main.environment = new Environment();
        if(!processInput("(define (add x y) (+ x y))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(define (minus x y) (- x y))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(add 1 2)").equals("3")) System.out.println(errMsg);
        if(!processInput("(add 5 6)").equals("11")) System.out.println(errMsg);
        if(!processInput("(add (minus 4 2) 6)").equals("8")) System.out.println(errMsg);
        if(!processInput("(define pro (lambda (x y) (+ x y)))").equals("Saved!")) System.out.println(errMsg);

        //Lambda
        System.out.println("\nlambda Tests");
        Main.environment = new Environment();
        if(!processInput("(lambda (x y) (+ x y))").equals("procedure")) System.out.println(errMsg);
        if(!processInput("((lambda (x y) (+ x y)) 1 4)").equals("5")) System.out.println(errMsg);
        if(!processInput("(+ 100 ((lambda (x y) (+ x y)) 1 4))").equals("105")) System.out.println(errMsg);
        if(!processInput("(define pro (lambda (x y) (+ x y)))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(pro 1 2)").equals("3")) System.out.println(errMsg);

        //Environment test
        System.out.println("\nEnvironment Functions");
        Main.environment = new Environment();
        if(!processInput("(define (erzeuge-konto-abheben saldo) (lambda (betrag) (set! saldo (- saldo betrag)) saldo))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(define konto (erzeuge-konto-abheben 1100))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(konto 100)").equals("1000")) System.out.println(errMsg);
        if(!processInput("(konto 100)").equals("900")) System.out.println(errMsg);
        if(!processInput("(konto 66)").equals("834")) System.out.println(errMsg);


        //Complex functions
        System.out.println("\nComplex Functions");
        Main.environment = new Environment();
        if(!processInput("(define (laenge l)(if(null? l) 0 (if(null? (cdr l)) 1 (+ 1 (laenge (cdr l))))))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(laenge (list 2 4 3))").equals("3")) System.out.println(errMsg);
        if(!processInput("(laenge '(2 4 3))").equals("3")) System.out.println(errMsg);

        if(!processInput("(define (last l)(if(null? (cdr l)) (car l) (last (cdr l))))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(last (list 1 2 2 3 11))").equals("11")) System.out.println(errMsg);
        if(!processInput("(last '(1 2 2 3 11))").equals("11 ")) System.out.println(errMsg);

        if(!processInput("(define (anhaengen l r)(if(null? l) r (cons (car l) (anhaengen (cdr l) r))))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(anhaengen (list 1 2) (list 3))").equals("( 1 2 3 )")) System.out.println(errMsg);
        if(!processInput("(anhaengen (list 1 2) (list ))").equals("( 1 2 )")) System.out.println(errMsg);
        if(!processInput("(anhaengen (list 1) (list 3 4))").equals("( 1 3 4 )")) System.out.println(errMsg);
        if(!processInput("(anhaengen (list ) (list 3 4))").equals("( 3 4 )")) System.out.println(errMsg);
        if(!processInput("(anhaengen (list ) (list ))").equals("( )")) System.out.println(errMsg);
        if(!processInput("(anhaengen '(1 2) '(3 4))").equals("( 1 2 3 4 )")) System.out.println(errMsg);
        if(!processInput("(anhaengen '(1) '(3 4))").equals("( 1 3 4 )")) System.out.println(errMsg);
        if(!processInput("(anhaengen '() '(3 4))").equals("( 3 4 )")) System.out.println(errMsg);
        if(!processInput("(anhaengen '(3 4) '())").equals("( 3 4 )")) System.out.println(errMsg);
        if(!processInput("(anhaengen '(3) '())").equals("( 3 )")) System.out.println(errMsg);
        if(!processInput("(anhaengen '() '())").equals("( )")) System.out.println(errMsg);

        if(!processInput("(define (merge L M)(if(null? L) M (if(null? M) L(if(< (car L) (car M))(cons (car L) (merge (cdr L) M))(cons (car M) (merge (cdr M) L))))))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(merge (list 1 3 5 7 8 9 10) (list 2 4 6))").equals("( 1 2 3 4 5 6 7 8 9 10 )")) System.out.println(errMsg);
        if(!processInput("(merge '(1 3 5 7 8 9 10) '(2 4 6))").equals("( 1 2 3 4 5 6 7 8 9 10 )")) System.out.println(errMsg);

        if(!processInput("(define (odd L)     (if(null? L) '()     (if(null? (cdr L)) (list (car L))     (cons (car L) (odd (cdr (cdr L)))))))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(odd (list 1 2 6))").equals("( 1 6 )")) System.out.println(errMsg);
        if(!processInput("(odd '(1 2 3))").equals("( 1 3 )")) System.out.println(errMsg);


        if(!processInput("(define (even L)     (if(null? L) '()          (if(null? (cdr L)) '()               (cons (car (cdr L)) (even (cdr (cdr L)))))))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(even (list 1 2 3))").equals("( 2 )")) System.out.println(errMsg);
        if(!processInput("(even (list 1 2 3))").equals("( 2 )")) System.out.println(errMsg);
        if(!processInput("(even (list 1 2 3))").equals("( 2 )")) System.out.println(errMsg);
        if(!processInput("(even '(1 2 3))").equals("( 2 )")) System.out.println(errMsg);
        if(!processInput("(even '(1 2 3))").equals("( 2 )")) System.out.println(errMsg);
        if(!processInput("(even '(1 2 3))").equals("( 2 )")) System.out.println(errMsg);

        if(!processInput("(define (split L)     (cons (odd L) (cons (even L) (list ))))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(split (list 1 2 8 4 5))").equals("( 1 8 5 2 4 )")) System.out.println(errMsg);

        if(!processInput("(define (mergesort L)     (if(null? L) L          (if(null? (cdr L)) L     (merge     (mergesort (car (split L)))     (mergesort (car (cdr (split L))))))))").equals("Saved!")) System.out.println(errMsg);
        if(!processInput("(mergesort '(8 1 3 9 6 5 7 2 4 10))").equals("( 1 2 3 4 5 6 7 8 9 10 )")) System.out.println(errMsg);
    }
}
