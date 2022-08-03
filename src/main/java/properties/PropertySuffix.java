package properties;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PropertySuffix {
    /**
     * The property path/suffix of the specified properties class.
     * ex. test.peroperty.length=1 -> suffix=test.peroperty.
     */
    String value();
}
