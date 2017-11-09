package esolutions.com.esdatabaselib.baseSharedPref.anonation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by VinhNB on 11/9/2017.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface KeyType {
    public String name();
    public TYPE TYPE() default TYPE.STRING;
}
