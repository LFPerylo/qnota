package dev.com.qnota.apresentacao.principal.responsavel;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.com.qnota.aplicacao.principal.responsavel.ResponsavelResumo;
import dev.com.qnota.aplicacao.principal.responsavel.ResponsavelServicoAplicacao;
import dev.com.qnota.apresentacao.BackendMapeador;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelId;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelServico;

@RestController
@RequestMapping("backend/responsavel")
class ResponsavelControlador {
	private @Autowired ResponsavelServico responsavelServico;
	private @Autowired ResponsavelServicoAplicacao responsavelServicoConsulta;

	private @Autowired BackendMapeador mapeador;

	@RequestMapping(method = GET, path = "pesquisa")
	List<ResponsavelResumo> pesquisa() {
		return responsavelServicoConsulta.pesquisarResumos();
	}

	@RequestMapping(method = POST, path = "cadastrar")
	Integer cadastrar(@RequestBody ResponsavelFormulario.ResponsavelDto dto) {
		var responsavelId = responsavelServico.cadastrar(dto.nome, dto.cpf, dto.email);
		return responsavelId.value();
	}

	@RequestMapping(method = POST, path = "{id}/atualizar-contato")
	void atualizarContato(@PathVariable("id") int id, @RequestBody ResponsavelFormulario.ResponsavelDto dto) {
		var responsavelId = mapeador.map(id, ResponsavelId.class);
		responsavelServico.atualizarContato(responsavelId, dto.nome, dto.email);
	}

	@RequestMapping(method = POST, path = "{id}/marcar-inadimplente")
	void marcarInadimplente(@PathVariable("id") int id) {
		var responsavelId = mapeador.map(id, ResponsavelId.class);
		responsavelServico.marcarInadimplente(responsavelId);
	}

	@RequestMapping(method = POST, path = "{id}/regularizar")
	void regularizar(@PathVariable("id") int id) {
		var responsavelId = mapeador.map(id, ResponsavelId.class);
		responsavelServico.regularizar(responsavelId);
	}

	@RequestMapping(method = POST, path = "{id}/inativar")
	void inativar(@PathVariable("id") int id) {
		var responsavelId = mapeador.map(id, ResponsavelId.class);
		responsavelServico.inativar(responsavelId);
	}

	@RequestMapping(method = POST, path = "{id}/excluir")
	void excluir(@PathVariable("id") int id) {
		var responsavelId = mapeador.map(id, ResponsavelId.class);
		responsavelServico.excluir(responsavelId);
	}
}


