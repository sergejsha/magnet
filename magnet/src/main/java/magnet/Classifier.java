package magnet;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(SOURCE)
@Target({ ElementType.PARAMETER })
public @interface Classifier {

    String NONE = "";
    String value();

}