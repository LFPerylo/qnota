package dev.com.qnota.apresentacao.principal.simulado;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.com.qnota.aplicacao.principal.simulado.SimuladoServicoAplicacao;
import dev.com.qnota.apresentacao.BackendMapeador;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.dominio.principal.simulado.Simulado;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;
import dev.com.qnota.dominio.principal.simulado.SimuladoServico;
import dev.com.qnota.dominio.principal.turma.TurmaId;

@RestController
@RequestMapping("backend/simulado")
class SimuladoControlador {
	private @Autowired SimuladoServico simuladoServico;
	private @Autowired SimuladoServicoAplicacao simuladoServicoConsulta;

	private @Autowired BackendMapeador mapeador;

	@RequestMapping(method = GET, path = "pesquisa")
	List<? extends Object> pesquisar(@RequestParam(required = false, defaultValue = "false") boolean expandir) {
		if (expandir) {
			return simuladoServicoConsulta.pesquisarResumosExpandidos();
		} else {
			return simuladoServicoConsulta.pesquisarResumos();
		}
	}

	@RequestMapping(method = POST, path = "criar")
	Integer criar(@RequestBody SimuladoFormulario.SimuladoDto dto) {
		var turmaId = mapeador.map(dto.turmaId, TurmaId.class);
		var disciplinas = dto.disciplinas != null
			? dto.disciplinas.stream()
				.map(dp -> new Simulado.DisciplinaPeso(
					mapeador.map(dp.disciplinaId, DisciplinaId.class),
					dp.peso))
				.toList()
			: List.<Simulado.DisciplinaPeso>of();

		var simuladoId = simuladoServico.criar(dto.dataAplicacao, turmaId, disciplinas);
		return simuladoId.value();
	}

	@RequestMapping(method = POST, path = "{id}/editar-disciplinas")
	void editarDisciplinas(@PathVariable("id") int id, @RequestBody SimuladoFormulario.SimuladoDto dto) {
		var simuladoId = mapeador.map(id, SimuladoId.class);
		var disciplinas = dto.disciplinas != null
			? dto.disciplinas.stream()
				.map(dp -> new Simulado.DisciplinaPeso(
					mapeador.map(dp.disciplinaId, DisciplinaId.class),
					dp.peso))
				.toList()
			: List.<Simulado.DisciplinaPeso>of();
		simuladoServico.editarDisciplinas(simuladoId, disciplinas);
	}

	@RequestMapping(method = POST, path = "{id}/finalizar")
	void finalizar(@PathVariable("id") int id) {
		var simuladoId = mapeador.map(id, SimuladoId.class);
		simuladoServico.finalizar(simuladoId);
	}

	@RequestMapping(method = POST, path = "{id}/excluir")
	void excluir(@PathVariable("id") int id) {
		var simuladoId = mapeador.map(id, SimuladoId.class);
		simuladoServico.excluir(simuladoId);
	}
}







