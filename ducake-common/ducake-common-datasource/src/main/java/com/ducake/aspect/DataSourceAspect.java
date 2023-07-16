package com.ducake.aspect;

import com.ducake.annotation.DataSource;
import com.ducake.config.DataSourceContextHolder;
import com.ducake.tools.expression.ExpressionEvaluator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.core.env.Environment;
import org.springframework.expression.EvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

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
    private static final String CONFIG_PREFIX = "$";

    @Autowired
    private Environment environment;


    @Pointcut("@annotation(com.ducake.annotation.DataSource) " +
            "|| @within(com.ducake.annotation.DataSource)")
    public void dataSourcePointCut() {

    }

    @Around("dataSourcePointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Class<?> targetClass = point.getTarget().getClass();
        Method method = signature.getMethod();

        DataSource targetDataSource = ObjectUtils.isEmpty(method.getAnnotation(DataSource.class))
                ? targetClass.getAnnotation(DataSource.class) : method.getAnnotation(DataSource.class);

        // SpEL表达式的方式读取对应参数值
        EvaluationContext evaluationContext = evaluator.createEvaluationContext(point.getTarget(), targetClass, method, point.getArgs());
        AnnotatedElementKey methodKey = new AnnotatedElementKey(method, targetClass);

        String dataSourceName = targetDataSource.value();

        if (dataSourceName.startsWith(DYNAMIC_PREFIX)) {
            dataSourceName = evaluator.getValueByConditionExpression(dataSourceName, methodKey, evaluationContext, String.class);
        }
        if(dataSourceName.startsWith(CONFIG_PREFIX)){
            dataSourceName = environment.resolvePlaceholders(dataSourceName);
        }

        if (DataSourceContextHolder.getDatasourceMap().containsKey(dataSourceName)) {
            DataSourceContextHolder.setDataSource(dataSourceName);
            logger.info("set datasource is {}", dataSourceName);
        } else {
            logger.info("failed to set the datasource because the datasource {} does not exist", dataSourceName);
        }


        try {
            return point.proceed();
        } finally {
            DataSourceContextHolder.removeDataSource();
            logger.info("clean datasource");
        }
    }
}