package dev.com.qnota.apresentacao.principal.turma;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.com.qnota.aplicacao.principal.professor.ProfessorServicoAplicacao;
import dev.com.qnota.aplicacao.principal.turma.TurmaServicoAplicacao;
import dev.com.qnota.apresentacao.BackendMapeador;
import dev.com.qnota.dominio.principal.professor.ProfessorId;
import dev.com.qnota.dominio.principal.turma.TurmaId;
import dev.com.qnota.dominio.principal.turma.TurmaServico;

@RestController
@RequestMapping("backend/turma")
class TurmaControlador {
	private @Autowired TurmaServico turmaServico;
	private @Autowired TurmaServicoAplicacao turmaServicoConsulta;
	private @Autowired ProfessorServicoAplicacao professorServicoConsulta;

	private @Autowired BackendMapeador mapeador;

	@RequestMapping(method = GET, path = "criacao")
	TurmaFormulario criacao() {
		var turma = new TurmaFormulario.TurmaDto();
		var professores = professorServicoConsulta.pesquisarResumos();
		return new TurmaFormulario(turma, professores);
	}

	@RequestMapping(method = GET, path = "pesquisa")
	List<TurmaResumoDto> pesquisa() {
		return turmaServicoConsulta.pesquisarResumos().stream()
			.map(r -> new TurmaResumoDto(r.getId(), r.getNome(), r.getAnoLetivo(), r.isAtivo(), r.getProfessorId(), r.getProfessorNome(), r.getQuantidadeAlunos()))
			.toList();
	}
	
	public static record TurmaResumoDto(int id, String nome, int anoLetivo, boolean ativo, int professorId, String professorNome, int quantidadeAlunos) {}

	@RequestMapping(method = POST, path = "criar")
	Integer criar(@RequestBody TurmaFormulario.TurmaDto dto) {
		var professorId = mapeador.map(dto.professorId, ProfessorId.class);
		var turmaId = turmaServico.criar(dto.nome, dto.anoLetivo, professorId);
		return turmaId.value();
	}

	@RequestMapping(method = POST, path = "{id}/renomear")
	void renomear(@PathVariable("id") int id, @RequestBody TurmaFormulario.NomeDto dto) {
		var turmaId = mapeador.map(id, TurmaId.class);
		turmaServico.renomear(turmaId, dto.nome);
	}

	@RequestMapping(method = POST, path = "{id}/trocar-professor")
	void trocarProfessor(@PathVariable("id") int id, @RequestBody int novoProfessorId) {
		var turmaId = mapeador.map(id, TurmaId.class);
		var professorId = mapeador.map(novoProfessorId, ProfessorId.class);
		turmaServico.trocarProfessor(turmaId, professorId);
	}

	@RequestMapping(method = POST, path = "{id}/inativar")
	void inativar(@PathVariable("id") int id) {
		var turmaId = mapeador.map(id, TurmaId.class);
		turmaServico.inativar(turmaId);
	}

	@RequestMapping(method = POST, path = "{id}/ativar")
	void ativar(@PathVariable("id") int id) {
		var turmaId = mapeador.map(id, TurmaId.class);
		turmaServico.ativar(turmaId);
	}

	@RequestMapping(method = POST, path = "{id}/excluir")
	void excluir(@PathVariable("id") int id) {
		var turmaId = mapeador.map(id, TurmaId.class);
		turmaServico.excluir(turmaId);
	}
}







