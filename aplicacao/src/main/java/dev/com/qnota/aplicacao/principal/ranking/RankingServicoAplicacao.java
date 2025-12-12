package dev.com.qnota.aplicacao.principal.ranking;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.List;

public class RankingServicoAplicacao {
	private RankingRepositorioAplicacao repositorio;

	public RankingServicoAplicacao(RankingRepositorioAplicacao repositorio) {
		notNull(repositorio, "O repositório não pode ser nulo");

		this.repositorio = repositorio;
	}

	public List<RankingResumo> pesquisarResumos() {
		return repositorio.pesquisarResumos();
	}

	public List<RankingResumo> pesquisarResumosExpandidos() {
		return repositorio.pesquisarResumosExpandidos();
	}
}

