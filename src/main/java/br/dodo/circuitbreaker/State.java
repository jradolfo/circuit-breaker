package br.dodo.circuitbreaker;

public enum State {
	
	/**
	 * Indica que as requisições ao serviço protegido pelo circuito estão ocorrerendo 
	 * normalmente. 
	 */
	CLOSED, 
	
	/**
	 * Indica a ocorrência de problemas no circuito protegido e poucas requisições serão
	 * liberadas para averiguar se realmente o cicuito deve abrir ou retomar pra fechado.  
	 */
	HALFOPEN,
	
	/**
	 * Indica que as requisições estão sendo bloquedas instantâneamente por um tempo por que 
	 * está ocorrendo muita lentidão ou erros no serviço alvo.
	 */
	OPEN,
}
