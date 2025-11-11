package dev.com.qnota.aplicacao.principal.ranking;

import java.util.List;

public interface RankingRepositorioAplicacao {
	List<RankingResumo> pesquisarResumos();
	
	// TODO: Criar RankingResumoExpandido quando necessário
	default List<RankingResumo> pesquisarResumosExpandidos() {
		return pesquisarResumos();
	}
}

