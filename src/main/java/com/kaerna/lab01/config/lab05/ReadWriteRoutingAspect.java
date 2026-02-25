package com.kaerna.lab01.config.lab05;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

@Aspect
@Component
@Profile("lab05")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ReadWriteRoutingAspect {

    @Around("@annotation(org.springframework.transaction.annotation.Transactional) || @within(org.springframework.transaction.annotation.Transactional)")
    public Object routeByReadOnly(ProceedingJoinPoint pjp) throws Throwable {
        Transactional transactional = findTransactional(pjp);
        boolean readOnly = transactional != null && transactional.readOnly();
        DataSourceType previous = DataSourceContextHolder.get();
        try {
            DataSourceContextHolder.set(readOnly ? DataSourceType.REPLICA : DataSourceType.PRIMARY);
            return pjp.proceed();
        } finally {
            if (previous != null) {
                DataSourceContextHolder.set(previous);
            } else {
                DataSourceContextHolder.clear();
            }
        }
    }

    private Transactional findTransactional(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Transactional tx = method.getAnnotation(Transactional.class);
        if (tx != null) {
            return tx;
        }
        return pjp.getTarget().getClass().getAnnotation(Transactional.class);
    }
}
