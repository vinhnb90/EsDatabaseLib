package esolutions.com.esdatabaselib.baseSqlite.anonation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by VinhNB on 10/10/2017.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface Collumn {
    public String name();

    public TYPE type() default TYPE.TEXT;

    public String other() default "";
}
