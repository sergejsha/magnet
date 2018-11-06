package magnet.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.CLASS;

/** Subject to change. For internal use only. */
@Retention(CLASS)
@Target({ElementType.TYPE})
public @interface Index {

    Class factoryType();
    Class factoryClass();
    String instanceType();
    String classifier();

}
