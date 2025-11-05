package dev.com.qnota.aplicacao.principal.professor;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.List;

public class ProfessorServicoAplicacao {
	private ProfessorRepositorioAplicacao repositorio;

	public ProfessorServicoAplicacao(ProfessorRepositorioAplicacao repositorio) {
		notNull(repositorio, "O repositório não pode ser nulo");

		this.repositorio = repositorio;
	}

	public List<ProfessorResumo> pesquisarResumos() {
		return repositorio.pesquisarResumos();
	}
}

