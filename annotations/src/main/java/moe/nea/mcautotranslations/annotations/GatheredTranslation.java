package moe.nea.mcautotranslations.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Repeatable(GatheredTranslations.class)
public @interface GatheredTranslation {
	String key();

	String value();
}
