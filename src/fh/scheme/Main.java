package fh.scheme;

import fh.scheme.interpreter.Environment;
import fh.scheme.interpreter.Interpreter;
import fh.scheme.parser.Entry;
import fh.scheme.parser.SchemeLexer;
import fh.scheme.parser.SchemeParser;
import fh.scheme.parser.TokenType;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static Environment environment;

    public static void main(String[] args) throws IOException {
        environment = new Environment();
        //Tests
        InterpreterTester.testAll();

        environment = new Environment();
        while (true) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            String in = bufferedReader.readLine();
            processInput(in);
        }
    }

    public static String processInput(String input) {
        String content = "";

        if (input.startsWith("load ")) {
            content = readFile(input);
        } else content = input;

        try {
            SchemeLexer sl = new SchemeLexer(new ByteArrayInputStream(content.getBytes()));
            SchemeParser sp = new SchemeParser(sl);
            List<Entry> entries = sp.program();
            String out = "";
            for (Entry entry : entries) {
                out = processOneEntry(entry);
            }
            return out;
        } catch (Throwable r) {
            System.out.println("Ein Fehler ist aufgetreten\n" + r.getMessage() + "\n" + Arrays.toString(r.getStackTrace()));
        }
        return "";
    }

    private static String processOneEntry(Entry entry) {
        Entry ret = Interpreter.eval(entry, environment);
        String output = formatOutput(ret);
        System.out.println(output);
        return output;
    }

    private static String formatOutput(Entry ret) {
        String out;
        if (ret == null) return "null";
        if (isQuoted(ret)) out = "( " + Interpreter.listToString(ret, false) + ")";
        else if (isList(ret)) out = "( " + Interpreter.listToString(ret, false) + ")";
        else if (ret.getToken().getType() == TokenType.LPARENTHESIS)
            out = Interpreter.listToString(ret.getChildren().get(1), true);
        else out = ret.getToken().getText();
        return out;
    }


    private static String readFile(String input) {
        String content;
        try {
            Path filePath = Path.of(input.split(" ")[1].replace(" ", ""));
            content = Files.readString(filePath);
            content = content.replace("  ", " ");
            content = content.replace("\n\n", "\n");
            environment = new Environment();
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static boolean isQuoted(Entry entry) {
        if (entry.getChildren() == null || entry.getChildren().size() == 0) return false;
        return entry.getChildren().get(0).getToken().getText().equals("quote") || entry.isQoute();
    }

    public static boolean isList(Entry entry) {
        if (entry.getChildren() == null || entry.getChildren().size() == 0) return false;
        return entry.getChildren().get(0).getToken().getText().equals("list")
                || entry.getChildren().get(0).getToken().getText().equals("cons");
    }
}
