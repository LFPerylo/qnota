package dev.com.qnota.aplicacao.principal.coordenador;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.List;

public class CoordenadorServicoAplicacao {
	private CoordenadorRepositorioAplicacao repositorio;

	public CoordenadorServicoAplicacao(CoordenadorRepositorioAplicacao repositorio) {
		notNull(repositorio, "O repositório não pode ser nulo");

		this.repositorio = repositorio;
	}

	public List<CoordenadorResumo> pesquisarResumos() {
		return repositorio.pesquisarResumos();
	}
}

