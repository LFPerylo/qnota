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

import dev.com.qnota.aplicacao.principal.disciplina.DisciplinaServicoAplicacao;
import dev.com.qnota.aplicacao.principal.simulado.SimuladoServicoAplicacao;
import dev.com.qnota.aplicacao.principal.turma.TurmaServicoAplicacao;
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
	private @Autowired DisciplinaServicoAplicacao disciplinaServicoConsulta;
	private @Autowired TurmaServicoAplicacao turmaServicoConsulta;

	private @Autowired BackendMapeador mapeador;

	@RequestMapping(method = GET, path = "criacao")
	SimuladoFormulario criacao() {
		var simulado = new SimuladoFormulario.SimuladoDto();
		var disciplinas = disciplinaServicoConsulta.pesquisarResumos();
		var turmas = turmaServicoConsulta.pesquisarResumos();
		return new SimuladoFormulario(simulado, disciplinas, turmas);
	}

	@RequestMapping(method = GET, path = "pesquisa")
	List<SimuladoResumoDto> pesquisar(@RequestParam(required = false, defaultValue = "false") boolean expandir) {
		var resumos = expandir 
			? simuladoServicoConsulta.pesquisarResumosExpandidos()
			: simuladoServicoConsulta.pesquisarResumos();
		return resumos.stream()
			.map(r -> new SimuladoResumoDto(r.getId(), r.getDataAplicacao(), r.getStatus().name(), r.getTurmaId(), r.getTurmaNome(), r.getQuantidadeDisciplinas()))
			.toList();
	}
	
	public static record SimuladoResumoDto(int id, java.time.LocalDate dataAplicacao, String status, int turmaId, String turmaNome, int quantidadeDisciplinas) {}

	@RequestMapping(method = GET, path = "{id}")
	SimuladoDetalheDto detalhar(@PathVariable("id") int id) {
		var simuladoId = mapeador.map(id, SimuladoId.class);
		var simulado = simuladoServico.detalhar(simuladoId);
		var disciplinas = simulado.getDisciplinas().stream()
			.map(dp -> new DisciplinaDto(dp.disciplina().value(), dp.peso()))
			.toList();
		return new SimuladoDetalheDto(
			simulado.getId().value(),
			simulado.getDataAplicacao(),
			simulado.getStatus().name(),
			simulado.getTurma().value(),
			disciplinas,
			simulado.getFormulaCalculo().name()
		);
	}

	public static record SimuladoDetalheDto(int id, java.time.LocalDate dataAplicacao, String status, int turmaId, List<DisciplinaDto> disciplinas, String formulaCalculo) {}
	public static record DisciplinaDto(int disciplinaId, double peso) {}

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

		var formula = dto.formulaCalculo != null 
			? Simulado.FormulaCalculo.valueOf(dto.formulaCalculo)
			: Simulado.FormulaCalculo.PONDERADA;

		var simuladoId = simuladoServico.criar(dto.dataAplicacao, turmaId, disciplinas, formula);
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

	@RequestMapping(method = POST, path = "{id}/editar-formula-calculo")
	void editarFormulaCalculo(@PathVariable("id") int id, @RequestBody SimuladoFormulario.SimuladoDto dto) {
		var simuladoId = mapeador.map(id, SimuladoId.class);
		var formula = dto.formulaCalculo != null 
			? Simulado.FormulaCalculo.valueOf(dto.formulaCalculo)
			: Simulado.FormulaCalculo.PONDERADA;
		simuladoServico.editarFormulaCalculo(simuladoId, formula);
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







