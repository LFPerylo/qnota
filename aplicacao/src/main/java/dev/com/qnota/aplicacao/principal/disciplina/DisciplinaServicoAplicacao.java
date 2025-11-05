package dev.com.qnota.aplicacao.principal.disciplina;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.List;

public class DisciplinaServicoAplicacao {
	private DisciplinaRepositorioAplicacao repositorio;

	public DisciplinaServicoAplicacao(DisciplinaRepositorioAplicacao repositorio) {
		notNull(repositorio, "O repositório não pode ser nulo");

		this.repositorio = repositorio;
	}

	public List<DisciplinaResumo> pesquisarResumos() {
		return repositorio.pesquisarResumos();
	}
}

