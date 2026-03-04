package de.upteams.tasktracker.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Class that contains pointcuts and advices for AOP-logging
 */
@Aspect
@Component
public class AspectLogging {

    /**
     * Instance of Logger
     */
    private final Logger logger = LoggerFactory.getLogger(AspectLogging.class);

    /**
     * Pointcut for all methods of all services
     */
    @Pointcut("execution(* *..*Service*.*(..))")
    public void allServiceMethods() {
    }

    /**
     * Advice that executed before all services methods
     *
     * @param joinPoint instance that contains information about called method
     */
    @Before("allServiceMethods()")
    public void beforeAllServiceMethods(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        logger.info("Method {} of the class {} called with arguments: {}.", methodName, className, args);
    }

    /**
     * Advice that executed after all services methods
     *
     * @param joinPoint instance that contains information about called method
     */
    @After("allServiceMethods()")
    public void afterAllServiceMethods(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        logger.info("Method {} of the class {} finished its job.", methodName, className);
    }

    /**
     * Advice that executed when service method successfully returns a result
     *
     * @param joinPoint instance that contains information about called method
     * @param result    result returned by method
     */
    @AfterReturning(pointcut = "allServiceMethods()", returning = "result")
    public void afterReturningForAllServiceMethods(JoinPoint joinPoint, Object result) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        logger.info("Method {} of the class {} successfully returned result: {}.", methodName, className, result);
    }

    /**
     * Advice that executed when service method throws an exception
     *
     * @param joinPoint instance that contains information about called method
     * @param e         exception threw by method
     */
    @AfterThrowing(pointcut = "allServiceMethods()", throwing = "e")
    public void afterThrowingForAllServiceMethods(JoinPoint joinPoint, Exception e) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        logger.warn("Method {} of the class {} threw {} with message: {}.",
                methodName, className, e.getClass().getSimpleName(), e.getMessage());
    }
}
