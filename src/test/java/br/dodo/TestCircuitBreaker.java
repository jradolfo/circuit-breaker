package br.dodo;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class TestCircuitBreaker {
	
	@Inject
	private Teste testeBean;

	@Test
	public void testar() {

		testeBean.rodar();
		
	}
	
}
