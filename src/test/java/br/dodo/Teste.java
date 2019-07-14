package br.dodo;

import br.dodo.circuitbreaker.annotation.CircuitBreaker;

class Teste {
			
	@CircuitBreaker
	public void rodar() {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			//parando a thread
		}

	}
			
}