package dev.com.qnota.apresentacao.principal.professor;

import java.util.List;

public class ProfessorFormulario {
	public ProfessorDto professor;
	public List<String> areasDisponiveis;

	public ProfessorFormulario(ProfessorDto professor, List<String> areasDisponiveis) {
		this.professor = professor;
		this.areasDisponiveis = areasDisponiveis;
	}

	public static class ProfessorDto {
		public Integer id;
		public String nome;
		public String cpf;
		public String email;
		public List<String> especialidades;
	}
}







