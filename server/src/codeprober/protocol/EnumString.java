package codeprober.protocol;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that indicates that a "string"-typed field is actually an (ad-hoc) enumeration.
 *
 * The set of options specified here is the total set of all allowed options.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EnumString {
    String[] options();
}
