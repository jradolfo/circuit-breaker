package br.dodo;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import br.dodo.annotation.CircuitBreaker;
import br.dodo.circuitbreaker.State;
import br.dodo.circuitbreaker.exception.CircuitBreakerException;

@Interceptor
@CircuitBreaker
public class CircuirBreakerInterceptor {
	
	private static final Logger LOGGER = LogManager.getLogger(CircuirBreakerInterceptor.class);

	private static final Map<String, State> stateMap = new ConcurrentHashMap<>();
	private static final Map<String, Long> stateChangeTimeMap = new ConcurrentHashMap<>();
	private static final Map<String, AtomicInteger> errorCountMap = new ConcurrentHashMap<>();
	
	@Resource(lookup = "java:jboss/ee/concurrency/executor/circuit-breaker")	
	private ManagedExecutorService mes;
	
			
	@AroundInvoke
	public Object executar(InvocationContext ctx) throws Exception {			
		String circuitBreakerId = getCircuirBreakerId(ctx);
		CircuitBreaker cbAnnotation = getCircuitBreakerAnnotation(ctx);
				
		configCircuitBreaker(circuitBreakerId);
		
		State state = verificarEstadoCircuito(circuitBreakerId, cbAnnotation.sleepWindow());
				
		Future<Object> task = mes.submit(() ->  ctx.proceed());				
		
		try {
			long timeout = cbAnnotation.timeOut();
			
			Object resultado = null;
			
			//se foi especificado um timeout			
			if (timeout > 0) {
				resultado = task.get(timeout, TimeUnit.MILLISECONDS);

			}else {
				resultado = task.get();
			}
		
			resetCircuitBreaker(circuitBreakerId);			
			
			return resultado;
			
		}catch (Exception e) {
			task.cancel(true);
			throw verificarExcecao(e, circuitBreakerId, state, cbAnnotation.ignoreExceptions(), cbAnnotation.errorsThreshold());	
		}
		
	}
	
	
	private void resetCircuitBreaker(String circuitBreakerId) {
		
		if (stateMap.get(circuitBreakerId) != State.CLOSED) {
			
			stateMap.put(circuitBreakerId, State.CLOSED);
			stateChangeTimeMap.put(circuitBreakerId, System.currentTimeMillis());
			errorCountMap.put(circuitBreakerId, new AtomicInteger(0));		
			
			LOGGER.info("Circuit breaker {} voltou para o estado fechado!", circuitBreakerId);
			
		}
	}


	private void configCircuitBreaker(String circuitBreakerId) {
		State state = stateMap.putIfAbsent(circuitBreakerId, State.CLOSED);
		Long time = stateChangeTimeMap.putIfAbsent(circuitBreakerId, System.currentTimeMillis());
		AtomicInteger error = errorCountMap.putIfAbsent(circuitBreakerId, new AtomicInteger(0));		
		
		LOGGER.info("Circuit breaker {}, state={}, lastChange={}, errorCount={}.", circuitBreakerId, state, time, error.get());
				
	}

	private State verificarEstadoCircuito(final String circuitBreakerId, long sleepWindow) {
		
		State state = stateMap.get(circuitBreakerId);
		
		if (state == State.OPEN) {
			
			//Se já passou o tempo configurado desde que o circuito abriu, vai pro estado de meio-aberto
			if (getTempoDesdeUltimaMudanca(circuitBreakerId) > sleepWindow) {				
				updateState(circuitBreakerId, State.HALFOPEN);
				LOGGER.info("Circuit breaker {} mudou para o estado meio-aberto!", circuitBreakerId);
				
			} else {
				throw new CircuitBreakerException(state);
				
			}
		
		}
		
		return state;
	}

	private Exception verificarExcecao(Exception e, String circuitBreakerId, State state, Class<Throwable>[] ignoredExceptions, long errorsThreshold) {

		//Exceções ignoradas não contam para abertura do circuito, serão apenas relançadas
		if (Arrays.stream(ignoredExceptions).anyMatch(ignoredEx -> ignoredEx.isAssignableFrom(e.getClass()))) {
			return e;
			
		}else {
			int numeroErros = errorCountMap.get(circuitBreakerId).incrementAndGet();
			
			if (numeroErros > errorsThreshold) {
				updateState(circuitBreakerId, State.OPEN);
				LOGGER.info("Circuit breaker {} mudou para o estado aberto!", circuitBreakerId);
			}						
			
			return e;
		}
		
	}
	
	private void updateState(String circuitBreakerId, State state) {
		stateChangeTimeMap.put(circuitBreakerId, System.currentTimeMillis());
		stateMap.put(circuitBreakerId, state);
	}
	
	
	private CircuitBreaker getCircuitBreakerAnnotation(InvocationContext ctx) {
		return ctx.getMethod().getAnnotation(CircuitBreaker.class);	
	}
	
	
	private String getCircuirBreakerId(InvocationContext ctx) {
		return ctx.getTarget().getClass().getSimpleName() + "$" + ctx.getMethod().getName();
	}
	
	private Long getTempoDesdeUltimaMudanca(final String circuitBreakerId) {
		Long horaEvento = stateChangeTimeMap.get(circuitBreakerId);		
		return System.currentTimeMillis() - horaEvento;
	}
	

}
