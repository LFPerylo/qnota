package dev.com.qnota.apresentacao.principal.disciplina;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.com.qnota.aplicacao.principal.disciplina.DisciplinaServicoAplicacao;
import dev.com.qnota.apresentacao.BackendMapeador;
import dev.com.qnota.dominio.principal.disciplina.Disciplina;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaServico;

@RestController
@RequestMapping("backend/disciplina")
class DisciplinaControlador {
	private @Autowired DisciplinaServico disciplinaServico;
	private @Autowired DisciplinaServicoAplicacao disciplinaServicoConsulta;

	private @Autowired BackendMapeador mapeador;

	@RequestMapping(method = GET, path = "pesquisa")
	List<DisciplinaResumoDto> pesquisa() {
		return disciplinaServicoConsulta.pesquisarResumos().stream()
			.map(r -> new DisciplinaResumoDto(r.getId(), r.getNome(), r.getVersao(), r.isAtivo(), r.getAreaNome()))
			.toList();
	}
	
	public static record DisciplinaResumoDto(int id, String nome, int versao, boolean ativo, String areaNome) {}

	@RequestMapping(method = POST, path = "cadastrar")
	Integer cadastrar(@RequestBody DisciplinaFormulario.DisciplinaDto dto) {
		// AreaConhecimento é um record que precisa de (id, nome)
		// Por enquanto, usamos id=0 para novas áreas (o domínio pode validar)
		// TODO: Implementar busca de área existente ou criar área se não existir
		var area = new Disciplina.AreaConhecimento(0, dto.area);
		var disciplinaId = disciplinaServico.cadastrar(dto.nome, area);
		return disciplinaId.value();
	}

	@RequestMapping(method = POST, path = "{id}/editar")
	void editar(@PathVariable("id") int id, @RequestBody DisciplinaFormulario.DisciplinaDto dto) {
		var disciplinaId = mapeador.map(id, DisciplinaId.class);
		// AreaConhecimento é um record que precisa de (id, nome)
		// Por enquanto, usamos id=0 para novas áreas (o domínio pode validar)
		// TODO: Implementar busca de área existente ou criar área se não existir
		var area = new Disciplina.AreaConhecimento(0, dto.area);
		disciplinaServico.editar(disciplinaId, dto.nome, area);
	}

	@RequestMapping(method = POST, path = "{id}/ativar")
	void ativar(@PathVariable("id") int id) {
		var disciplinaId = mapeador.map(id, DisciplinaId.class);
		disciplinaServico.ativar(disciplinaId);
	}

	@RequestMapping(method = POST, path = "{id}/inativar")
	void inativar(@PathVariable("id") int id) {
		var disciplinaId = mapeador.map(id, DisciplinaId.class);
		disciplinaServico.inativar(disciplinaId);
	}

	@RequestMapping(method = POST, path = "{id}/excluir")
	void excluir(@PathVariable("id") int id) {
		var disciplinaId = mapeador.map(id, DisciplinaId.class);
		disciplinaServico.excluir(disciplinaId);
	}
}

