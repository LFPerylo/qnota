package dev.com.qnota.apresentacao.principal.ranking;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.com.qnota.aplicacao.principal.ranking.RankingServicoAplicacao;
import dev.com.qnota.apresentacao.BackendMapeador;
import dev.com.qnota.dominio.principal.ranking.RankingServico;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;

@RestController
@RequestMapping("backend/ranking")
class RankingControlador {
	private @Autowired RankingServico rankingServico;
	private @Autowired RankingServicoAplicacao rankingServicoConsulta;

	private @Autowired BackendMapeador mapeador;

	@RequestMapping(method = GET, path = "pesquisa")
	List<? extends Object> pesquisar(@RequestParam(required = false, defaultValue = "false") boolean expandir) {
		if (expandir) {
			return rankingServicoConsulta.pesquisarResumosExpandidos();
		} else {
			return rankingServicoConsulta.pesquisarResumos();
		}
	}

	@RequestMapping(method = POST, path = "simulado/{simuladoId}/recalcular")
	List<dev.com.qnota.dominio.principal.ranking.Ranking.Linha> recalcular(@PathVariable("simuladoId") int simuladoId) {
		var id = mapeador.map(simuladoId, SimuladoId.class);
		return rankingServico.recalcular(id);
	}

	@RequestMapping(method = POST, path = "simulado/{simuladoId}/congelar")
	void congelar(@PathVariable("simuladoId") int simuladoId) {
		var id = mapeador.map(simuladoId, SimuladoId.class);
		rankingServico.congelar(id);
	}
}







