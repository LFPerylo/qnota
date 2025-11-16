package dev.com.qnota.apresentacao.principal.disciplina;

import java.util.List;

public class DisciplinaFormulario {
	public DisciplinaDto disciplina;
	public List<String> areasDisponiveis;

	public DisciplinaFormulario(DisciplinaDto disciplina, List<String> areasDisponiveis) {
		this.disciplina = disciplina;
		this.areasDisponiveis = areasDisponiveis;
	}

	public static class DisciplinaDto {
		public Integer id;
		public String nome;
		public String area; // MATEMATICA, PORTUGUES, CIENCIAS, etc.
	}
}

