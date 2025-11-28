package dev.com.qnota.apresentacao.principal.professor;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.com.qnota.aplicacao.principal.professor.ProfessorServicoAplicacao;
import dev.com.qnota.apresentacao.BackendMapeador;
import dev.com.qnota.dominio.principal.professor.ProfessorId;
import dev.com.qnota.dominio.principal.professor.ProfessorServico;

@RestController
@RequestMapping("backend/professor")
class ProfessorControlador {
	private @Autowired ProfessorServico professorServico;
	private @Autowired ProfessorServicoAplicacao professorServicoConsulta;

	private @Autowired BackendMapeador mapeador;

	@RequestMapping(method = GET, path = "pesquisa")
	List<ProfessorResumoDto> pesquisa() {
		return professorServicoConsulta.pesquisarResumos().stream()
			.map(r -> new ProfessorResumoDto(r.getId(), r.getNome(), r.getCpf(), r.getEmail(), r.getEspecialidades()))
			.toList();
	}
	
	// DTO para serialização JSON
	public static record ProfessorResumoDto(int id, String nome, String cpf, String email, String especialidades) {}

	@RequestMapping(method = POST, path = "cadastrar")
	Integer cadastrar(@RequestBody ProfessorFormulario.ProfessorDto dto) {
		var professorId = professorServico.cadastrar(
			dto.nome,
			dto.cpf,
			dto.email,
			dto.especialidades != null ? dto.especialidades : List.of()
		);
		return professorId.value();
	}

	@RequestMapping(method = POST, path = "{id}/atualizar-contato")
	void atualizarContato(@PathVariable("id") int id, @RequestBody ProfessorFormulario.ProfessorDto dto) {
		var professorId = mapeador.map(id, ProfessorId.class);
		professorServico.atualizarContato(professorId, dto.nome, dto.email);
	}

	@RequestMapping(method = POST, path = "{id}/adicionar-especialidade")
	void adicionarEspecialidade(@PathVariable("id") int id, @RequestBody String area) {
		var professorId = mapeador.map(id, ProfessorId.class);
		professorServico.adicionarEspecialidade(professorId, area);
	}

	@RequestMapping(method = POST, path = "{id}/remover-especialidade")
	void removerEspecialidade(@PathVariable("id") int id, @RequestBody String area) {
		var professorId = mapeador.map(id, ProfessorId.class);
		professorServico.removerEspecialidade(professorId, area);
	}

	@RequestMapping(method = POST, path = "{id}/remover-com-substituto")
	void removerComSubstituto(@PathVariable("id") int id, @RequestBody int substitutoId) {
		var professorId = mapeador.map(id, ProfessorId.class);
		var substituto = mapeador.map(substitutoId, ProfessorId.class);
		professorServico.removerComSubstituto(professorId, substituto);
	}
}







