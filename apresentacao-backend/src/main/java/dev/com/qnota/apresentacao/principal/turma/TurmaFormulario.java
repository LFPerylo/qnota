package dev.com.qnota.apresentacao.principal.turma;

import java.util.List;

public class TurmaFormulario {
	public TurmaDto turma;
	public List<dev.com.qnota.aplicacao.principal.professor.ProfessorResumo> professores;

	public TurmaFormulario(TurmaDto turma, 
	                       List<dev.com.qnota.aplicacao.principal.professor.ProfessorResumo> professores) {
		this.turma = turma;
		this.professores = professores;
	}

	public static class TurmaDto {
		public Integer id;
		public String nome;
		public Integer anoLetivo;
		public Integer professorId;
	}
}







