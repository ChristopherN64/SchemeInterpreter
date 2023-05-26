package fh.scheme.interpreter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum PrimitiveOperator {

    CAR("car"),
    CDR("cdr"),
    PLUS("+"),
    MINUS("-"),
    MULT("*"),
    DIV("/"),
    LESS("<"),
    LESSEQ("<="),
    GREATER(">"),
    GREATEREQ(">="),
    EQ("="),
    AND("and"),
    OR("or"),
    CONS("cons"),
    LENGTH("length"),
    NULL("null?"),
    LAMBDA("lambda"),
    ROUND("round");

    private final String operator;

    PrimitiveOperator(String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }

    public static PrimitiveOperator get(String operator) {
        return Arrays.stream(PrimitiveOperator.values())
                .filter(env -> env.operator.equals(operator))
                .findFirst().orElse(null);
    }

    public static List<String> getStringValues(){
        return Arrays.stream(values()).map(PrimitiveOperator::getOperator).collect(Collectors.toList());
    }
}
