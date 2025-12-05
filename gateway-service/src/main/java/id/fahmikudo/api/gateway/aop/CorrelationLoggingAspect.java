package id.fahmikudo.api.gateway.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CorrelationLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(CorrelationLoggingAspect.class);

    @Before("execution(* id.fahmikudo.api.gateway.controller..*(..)) || execution(* id.fahmikudo.api.gateway.service..*(..))")
    public void logCorrelation(JoinPoint joinPoint) {
        String correlationId = MDC.get("X-Correlation-Id");
        if (log.isDebugEnabled()) {
            log.debug("{} correlationId={}", joinPoint.getSignature(), correlationId);
        }
    }
}
