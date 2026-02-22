package cellar.fixture.java;

import java.lang.annotation.*;

/**
 * A Java annotation type for testing annotation symbol extraction.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface CellarJavaAnnotation {
    String value();
    String description() default "";
    int priority() default 0;
}
