package com.ducake.aspect;

import com.ducake.annotation.DataSource;
import com.ducake.config.DataSourceContextHolder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;


/**
 * @author 93477
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DataSourceAspect {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    private static final String DYNAMIC_PREFIX = "#";

    @Pointcut("@annotation(com.ducake.annotation.DataSource) " +
            "|| @within(com.ducake.annotation.DataSource)")
    public void dataSourcePointCut() {

    }

    @Around("dataSourcePointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        //通过SpEL获取接口参数对象属性值
        StandardEvaluationContext standardEvaluationContext = new StandardEvaluationContext(point.getArgs());
        standardEvaluationContext = setContextVariables(standardEvaluationContext, point);

        MethodSignature signature = (MethodSignature) point.getSignature();
        Class targetClass = point.getTarget().getClass();
        Method method = signature.getMethod();

        DataSource targetDataSource = (DataSource) targetClass.getAnnotation(DataSource.class);
        DataSource methodDataSource = method.getAnnotation(DataSource.class);

        String value = "default";
        if (methodDataSource != null) {
            value = methodDataSource.value();
        } else if (targetDataSource != null) {
            value = targetDataSource.value();
        }
        if (value.startsWith(DYNAMIC_PREFIX)) {
            value = getElValue(value, standardEvaluationContext);
        }

        DataSourceContextHolder.setDataSource(value);
        logger.debug("set datasource is {}", value);

        try {
            return point.proceed();
        } finally {
            DataSourceContextHolder.removeDataSource();
            logger.debug("clean datasource");
        }
    }

    private StandardEvaluationContext setContextVariables(StandardEvaluationContext standardEvaluationContext,
                                                          JoinPoint joinPoint) {

        Object[] args = joinPoint.getArgs();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = methodSignature.getMethod();
        LocalVariableTableParameterNameDiscoverer parameterNameDiscoverer
                = new LocalVariableTableParameterNameDiscoverer();
        String[] parametersName = parameterNameDiscoverer.getParameterNames(targetMethod);

        if (args == null || args.length <= 0) {
            return standardEvaluationContext;
        }
        for (int i = 0; i < args.length; i++) {
            standardEvaluationContext.setVariable(parametersName[i], args[i]);
        }
        return standardEvaluationContext;
    }

    /**
     * 通过key SEL表达式获取值
     */
    private String getElValue(String key, StandardEvaluationContext context) {
        if (ObjectUtils.isEmpty(key)) {
            return "";
        }
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(key);
        return exp.getValue(context, String.class);

    }

}