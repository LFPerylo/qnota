package dev.com.qnota.aplicacao.principal.simulado;

import java.util.List;

public interface SimuladoRepositorioAplicacao {
	List<SimuladoResumo> pesquisarResumos();
	
	// TODO: Criar SimuladoResumoExpandido quando necessário
	default List<SimuladoResumo> pesquisarResumosExpandidos() {
		return pesquisarResumos();
	}
}

