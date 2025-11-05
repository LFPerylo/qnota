package dev.com.qnota.aplicacao.principal.turma;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.List;

public class TurmaServicoAplicacao {
	private TurmaRepositorioAplicacao repositorio;

	public TurmaServicoAplicacao(TurmaRepositorioAplicacao repositorio) {
		notNull(repositorio, "O repositório não pode ser nulo");

		this.repositorio = repositorio;
	}

	public List<TurmaResumo> pesquisarResumos() {
		return repositorio.pesquisarResumos();
	}
}

