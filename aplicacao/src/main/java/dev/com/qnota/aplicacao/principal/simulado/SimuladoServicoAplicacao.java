package dev.com.qnota.aplicacao.principal.simulado;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.List;

public class SimuladoServicoAplicacao {
	private SimuladoRepositorioAplicacao repositorio;

	public SimuladoServicoAplicacao(SimuladoRepositorioAplicacao repositorio) {
		notNull(repositorio, "O repositório não pode ser nulo");

		this.repositorio = repositorio;
	}

	public List<SimuladoResumo> pesquisarResumos() {
		return repositorio.pesquisarResumos();
	}

	public List<SimuladoResumo> pesquisarResumosExpandidos() {
		return repositorio.pesquisarResumosExpandidos();
	}
}

