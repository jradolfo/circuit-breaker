package br.dodo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;


/**
 * O método anotado tera sua execução encapsulada dentro de um Circuit Breaker.
 * 
 * @author jradolfo
 */
@Inherited
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface CircuitBreaker {

	@Nonbinding
	long timeOut() default 0;
	
	@Nonbinding
	long errorsThreshold() default 5;
	
	@Nonbinding
	long sleepWindow() default 10000;
	
	@Nonbinding
	Class<Throwable>[] ignoreExceptions() default {};
	
}
