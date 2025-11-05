package dev.com.qnota.aplicacao.principal.responsavel;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.List;

public class ResponsavelServicoAplicacao {
	private ResponsavelRepositorioAplicacao repositorio;

	public ResponsavelServicoAplicacao(ResponsavelRepositorioAplicacao repositorio) {
		notNull(repositorio, "O repositório não pode ser nulo");

		this.repositorio = repositorio;
	}

	public List<ResponsavelResumo> pesquisarResumos() {
		return repositorio.pesquisarResumos();
	}
}

