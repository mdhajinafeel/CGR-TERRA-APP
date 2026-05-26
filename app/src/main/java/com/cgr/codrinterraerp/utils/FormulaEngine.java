package com.cgr.codrinterraerp.utils;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class FormulaEngine {

    public static double evaluate(String formula, Map<String, Double> variables) {

        try {
            Expression expression = new ExpressionBuilder(formula)
                    .variables(variables.keySet())
                    .build();

            for (Map.Entry<String, Double> entry : variables.entrySet()) {
                expression.setVariable(entry.getKey(), entry.getValue());
            }

            return expression.evaluate();

        } catch (Exception e) {
            throw new RuntimeException("Formula error: " + e.getMessage());
        }
    }

    public static double applyRounding(double value, int precision, String type) {

        BigDecimal bd = BigDecimal.valueOf(value);

        bd = switch (type) {
            case "TRUNCATE" -> bd.setScale(precision, RoundingMode.DOWN);
            case "ROUND" -> bd.setScale(precision, RoundingMode.HALF_UP);
            case "CEIL" -> bd.setScale(precision, RoundingMode.CEILING);
            case "FLOOR" -> bd.setScale(precision, RoundingMode.FLOOR);
            default -> bd;
        };

        return bd.doubleValue();
    }
}