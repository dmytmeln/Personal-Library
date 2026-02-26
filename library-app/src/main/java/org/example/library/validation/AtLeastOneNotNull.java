package org.example.library.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AtLeastOneNotNullValidator.class)
@Documented
public @interface AtLeastOneNotNull {
    String message() default "{validation.update.at_least_one_required}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String[] fieldNames();
}
