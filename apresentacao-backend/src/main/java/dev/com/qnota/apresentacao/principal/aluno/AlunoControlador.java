package dev.com.qnota.apresentacao.principal.aluno;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import dev.com.qnota.aplicacao.principal.aluno.AlunoServicoAplicacao;
import dev.com.qnota.aplicacao.principal.responsavel.ResponsavelServicoAplicacao;
import dev.com.qnota.aplicacao.principal.turma.TurmaServicoAplicacao;
import dev.com.qnota.apresentacao.BackendMapeador;
import dev.com.qnota.dominio.principal.aluno.AlunoId;
import dev.com.qnota.dominio.principal.aluno.AlunoServico;
import dev.com.qnota.dominio.principal.aluno.NotaServico;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelId;
import dev.com.qnota.dominio.principal.turma.TurmaId;

@RestController
@RequestMapping("backend/aluno")
class AlunoControlador {
	private @Autowired AlunoServico alunoServico;
	private @Autowired AlunoServicoAplicacao alunoServicoConsulta;
	private @Autowired ResponsavelServicoAplicacao responsavelServicoConsulta;
	private @Autowired TurmaServicoAplicacao turmaServicoConsulta;
	private @Autowired NotaServico notaServico;

	private @Autowired BackendMapeador mapeador;

	@RequestMapping(method = GET, path = "criacao")
	AlunoFormulario criacao() {
		var aluno = new AlunoFormulario.AlunoDto();
		var responsaveis = responsavelServicoConsulta.pesquisarResumos();
		var turmas = turmaServicoConsulta.pesquisarResumos();
		return new AlunoFormulario(aluno, responsaveis, turmas);
	}

	@RequestMapping(method = GET, path = "pesquisa")
	List<AlunoResumoDto> pesquisa() {
		return alunoServicoConsulta.pesquisarResumos().stream()
			.map(r -> new AlunoResumoDto(r.getId(), r.getNome(), r.getDataNascimento(), r.isAtivo(), r.getTurmaId(), r.getTurmaNome(), r.getQuantidadeResponsaveis()))
			.toList();
	}

	@RequestMapping(method = GET, path = "{id}/detalhes")
	AlunoDetalhesDto detalhes(@PathVariable("id") int id) {
		var alunoId = mapeador.map(id, AlunoId.class);
		var aluno = alunoServico.porId(alunoId);
		return new AlunoDetalhesDto(
			aluno.getId().value(),
			aluno.getNome(),
			aluno.getDataNascimento(),
			aluno.isAtivo(),
			aluno.getTurma().value(),
			aluno.getVinculos().stream()
				.map(v -> new ResponsavelVinculoDto(v.responsavel().value(), v.principal()))
				.toList()
		);
	}
	
	public static record AlunoResumoDto(int id, String nome, java.time.LocalDate dataNascimento, boolean ativo, int turmaId, String turmaNome, int quantidadeResponsaveis) {}
	
	public static record AlunoDetalhesDto(int id, String nome, java.time.LocalDate dataNascimento, boolean ativo, int turmaId, List<ResponsavelVinculoDto> responsaveis) {}
	
	public static record ResponsavelVinculoDto(int responsavelId, boolean principal) {}

	@RequestMapping(method = POST, path = "cadastrar")
	Integer cadastrar(@RequestBody AlunoFormulario.AlunoDto dto) {
		var turmaId = mapeador.map(dto.turmaId, TurmaId.class);
		var responsaveis = dto.responsaveis != null 
			? dto.responsaveis.stream().map(id -> mapeador.map(id, ResponsavelId.class)).toList()
			: List.<ResponsavelId>of();
		var principal = dto.responsavelPrincipalId != null 
			? mapeador.map(dto.responsavelPrincipalId, ResponsavelId.class)
			: null;

		var alunoId = alunoServico.cadastrar(
			dto.nome,
			dto.dataNascimento,
			turmaId,
			responsaveis,
			principal
		);
		return alunoId.value();
	}

	@RequestMapping(method = POST, path = "{id}/renomear")
	void renomear(@PathVariable("id") int id, @RequestBody AlunoFormulario.NomeDto dto) {
		var alunoId = mapeador.map(id, AlunoId.class);
		alunoServico.renomear(alunoId, dto.nome);
	}

	@RequestMapping(method = POST, path = "{id}/transferir")
	void transferir(@PathVariable("id") int id, @RequestBody int novaTurmaId) {
		var alunoId = mapeador.map(id, AlunoId.class);
		var turmaId = mapeador.map(novaTurmaId, TurmaId.class);
		alunoServico.transferir(alunoId, turmaId);
	}

	@RequestMapping(method = POST, path = "{id}/inativar")
	void inativar(@PathVariable("id") int id) {
		var alunoId = mapeador.map(id, AlunoId.class);
		alunoServico.inativar(alunoId);
	}

	@RequestMapping(method = POST, path = "{id}/excluir")
	void excluir(@PathVariable("id") int id) {
		var alunoId = mapeador.map(id, AlunoId.class);
		alunoServico.excluir(alunoId);
	}

	@RequestMapping(method = POST, path = "{id}/vincular-responsavel")
	void vincularResponsavel(@PathVariable("id") int id, @RequestBody AlunoFormulario.VinculoDto dto) {
		var alunoId = mapeador.map(id, AlunoId.class);
		var responsavelId = mapeador.map(dto.responsavelId, ResponsavelId.class);
		alunoServico.vincularResponsavel(alunoId, responsavelId, dto.principal);
	}

	@RequestMapping(method = POST, path = "{id}/desvincular-responsavel")
	void desvincularResponsavel(@PathVariable("id") int id, @RequestBody int responsavelId) {
		var alunoId = mapeador.map(id, AlunoId.class);
		var respId = mapeador.map(responsavelId, ResponsavelId.class);
		alunoServico.desvincularResponsavel(alunoId, respId);
	}

	@RequestMapping(method = POST, path = "{id}/definir-responsavel-principal")
	void definirPrincipal(@PathVariable("id") int id, @RequestBody int responsavelId) {
		var alunoId = mapeador.map(id, AlunoId.class);
		var respId = mapeador.map(responsavelId, ResponsavelId.class);
		alunoServico.definirPrincipal(alunoId, respId);
	}

	@RequestMapping(method = POST, path = "{id}/lancar-nota")
	void lancarNota(@PathVariable("id") int id, @RequestBody AlunoFormulario.NotaDto dto) {
		var alunoId = mapeador.map(id, AlunoId.class);
		var simuladoId = mapeador.map(dto.simuladoId, dev.com.qnota.dominio.principal.simulado.SimuladoId.class);
		var disciplinaId = mapeador.map(dto.disciplinaId, dev.com.qnota.dominio.principal.disciplina.DisciplinaId.class);
		notaServico.lancar(alunoId, simuladoId, disciplinaId, dto.valor);
	}

	@RequestMapping(method = POST, path = "{id}/retificar-nota")
	void retificarNota(@PathVariable("id") int id, @RequestBody AlunoFormulario.RetificacaoDto dto) {
		var alunoId = mapeador.map(id, AlunoId.class);
		var simuladoId = mapeador.map(dto.simuladoId, dev.com.qnota.dominio.principal.simulado.SimuladoId.class);
		var disciplinaId = mapeador.map(dto.disciplinaId, dev.com.qnota.dominio.principal.disciplina.DisciplinaId.class);
		var professorId = mapeador.map(dto.professorId, dev.com.qnota.dominio.principal.professor.ProfessorId.class);
		notaServico.retificarNota(alunoId, simuladoId, disciplinaId, dto.novoValor, dto.justificativa, professorId);
	}
}







