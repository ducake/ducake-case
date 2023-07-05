package com.ducake.console.expression;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class SpELCase {
    public static void main(String[] args) {
        ExpressionParser parser = new SpelExpressionParser();
        Expression expression =parser.parseExpression("('Hello'+'World').concat(#end)");
        EvaluationContext context=new StandardEvaluationContext();
        context.setVariable("end","!");
        System.out.println(expression.getValue(context));
    }
}
