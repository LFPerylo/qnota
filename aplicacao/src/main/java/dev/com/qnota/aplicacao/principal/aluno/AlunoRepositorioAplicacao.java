package dev.com.qnota.aplicacao.principal.aluno;

import java.util.List;

public interface AlunoRepositorioAplicacao {
	List<AlunoResumo> pesquisarResumos();
	
	// TODO: Criar AlunoResumoExpandido quando necessário
	default List<AlunoResumo> pesquisarResumosExpandidos() {
		return pesquisarResumos();
	}
}

