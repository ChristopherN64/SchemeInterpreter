package fh.scheme.interpreter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Keywords {
    DEFINE("define"),
    SET("set!"),
    LIST("list"),
    COND("cond"),
    IF("if"),
    CONS("cons"),
    QUOTE("quote"),
    SHORTQUOTE("'"),
    LAMBDA("lambda"),
    ROUND("round"),
    LET("let"),
    PROCEDURE("procedure"),
    ELSE("else"),
    BEGIN("begin");

    private final String keyword;

    Keywords(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }

    public static Keywords get(String keyword) {
        return Arrays.stream(Keywords.values())
                .filter(env -> env.keyword.equals(keyword))
                .findFirst().orElse(null);
    }

    public static List<String> getStringValues() {
        return Arrays.stream(values()).map(Keywords::getKeyword).collect(Collectors.toList());
    }
}
