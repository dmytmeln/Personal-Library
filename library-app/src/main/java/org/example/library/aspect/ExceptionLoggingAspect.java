package org.example.library.aspect;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Aspect
@Component
@Slf4j
public class ExceptionLoggingAspect {

    @AfterThrowing(
            pointcut = "(@within(org.springframework.stereotype.Service) || @within(org.springframework.web.bind.annotation.RestController))",
            throwing = "ex"
    )
    public void logException(JoinPoint joinPoint, Exception ex) {
        var signature = joinPoint.getSignature();
        var className = signature.getDeclaringTypeName();
        var methodName = signature.getName();
        var exceptionType = ex.getClass().getSimpleName();

        if (isValidationException(ex)) {
            log.warn("Validation exception in {}.{}: [{}] {}",
                    className, methodName, exceptionType, ex.getMessage(), ex);
        } else {
            log.error("Exception in {}.{}: [{}] {}",
                    className, methodName, exceptionType, ex.getMessage(), ex);
        }
    }

    private boolean isValidationException(Exception ex) {
        return ex instanceof ValidationException || ex instanceof MethodArgumentNotValidException;
    }

}
