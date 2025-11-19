package dev.com.qnota.apresentacao.principal.coordenador;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.com.qnota.aplicacao.principal.coordenador.CoordenadorResumo;
import dev.com.qnota.aplicacao.principal.coordenador.CoordenadorServicoAplicacao;
import dev.com.qnota.apresentacao.BackendMapeador;
import dev.com.qnota.dominio.principal.coordenador.CoordenadorId;
import dev.com.qnota.dominio.principal.coordenador.CoordenadorServico;

@RestController
@RequestMapping("backend/coordenador")
class CoordenadorControlador {
	private @Autowired CoordenadorServico coordenadorServico;
	private @Autowired CoordenadorServicoAplicacao coordenadorServicoConsulta;

	private @Autowired BackendMapeador mapeador;

	@RequestMapping(method = GET, path = "pesquisa")
	List<CoordenadorResumo> pesquisa() {
		return coordenadorServicoConsulta.pesquisarResumos();
	}

	@RequestMapping(method = POST, path = "cadastrar")
	Integer cadastrar(@RequestBody CoordenadorFormulario.CoordenadorDto dto) {
		var coordenadorId = coordenadorServico.cadastrar(dto.nome, dto.email, dto.senha);
		return coordenadorId.value();
	}

	@RequestMapping(method = POST, path = "autenticar")
	Integer autenticar(@RequestBody CoordenadorFormulario.LoginDto dto) {
		var coordenadorId = coordenadorServico.autenticar(dto.email, dto.senha);
		return coordenadorId.value();
	}

	@RequestMapping(method = POST, path = "{id}/alterar-senha")
	void alterarSenha(@PathVariable("id") int id, @RequestBody CoordenadorFormulario.SenhaDto dto) {
		var coordenadorId = mapeador.map(id, CoordenadorId.class);
		coordenadorServico.alterarSenha(coordenadorId, dto.senhaAtual, dto.novaSenha);
	}

	@RequestMapping(method = POST, path = "{id}/atualizar-contato")
	void atualizarContato(@PathVariable("id") int id, @RequestBody CoordenadorFormulario.CoordenadorDto dto) {
		var coordenadorId = mapeador.map(id, CoordenadorId.class);
		coordenadorServico.atualizarContato(coordenadorId, dto.nome, dto.email);
	}

	@RequestMapping(method = POST, path = "{id}/inativar")
	void inativar(@PathVariable("id") int id) {
		var coordenadorId = mapeador.map(id, CoordenadorId.class);
		coordenadorServico.inativar(coordenadorId);
	}

	@RequestMapping(method = POST, path = "{id}/ativar")
	void ativar(@PathVariable("id") int id) {
		var coordenadorId = mapeador.map(id, CoordenadorId.class);
		coordenadorServico.ativar(coordenadorId);
	}

	@RequestMapping(method = POST, path = "{id}/excluir")
	void excluir(@PathVariable("id") int id) {
		var coordenadorId = mapeador.map(id, CoordenadorId.class);
		coordenadorServico.excluir(coordenadorId);
	}
}







