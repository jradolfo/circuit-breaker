package br.dodo.circuitbreaker.exception;

import br.dodo.circuitbreaker.State;

public class CircuitBreakerException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	
	public CircuitBreakerException(State state) {
		super(state.name());
	}
	
	public CircuitBreakerException(String mensagem) {
		super(mensagem);
	}

	public CircuitBreakerException(String mensagem, Throwable causa) {
		super(mensagem, causa);
	}

}
