package com.ducake.aspect;

import com.ducake.annotation.DataSource;
import com.ducake.config.DataSourceContextHolder;
import com.ducake.tools.expression.ExpressionEvaluator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;


/**
 * @author 93477
 */
@Aspect
@Component
public class DataSourceAspect {
    private ExpressionEvaluator<String> evaluator = new ExpressionEvaluator<>();
    protected Logger logger = LoggerFactory.getLogger(getClass());
    private static final String DYNAMIC_PREFIX = "#";

    @Around("@annotation(targetDataSource)")
    public Object around(ProceedingJoinPoint point, DataSource targetDataSource) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Class<?> targetClass = point.getTarget().getClass();
        Method method = signature.getMethod();

        // SpEL表达式的方式读取对应参数值
        EvaluationContext evaluationContext = evaluator.createEvaluationContext(point.getTarget(), targetClass, method, point.getArgs());
        AnnotatedElementKey methodKey = new AnnotatedElementKey(method, targetClass);

        String dataSourceName = targetDataSource.value();

        if (dataSourceName.startsWith(DYNAMIC_PREFIX)) {
            dataSourceName = evaluator.getValueByConditionExpression(dataSourceName, methodKey, evaluationContext, String.class);
        }

        DataSourceContextHolder.setDataSource(dataSourceName);
        logger.debug("set datasource is {}", dataSourceName);

        try {
            return point.proceed();
        } finally {
            DataSourceContextHolder.removeDataSource();
            logger.debug("clean datasource");
        }
    }
}