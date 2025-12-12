package dev.com.qnota.aplicacao.principal.turma;

public interface TurmaResumo {
	int getId();

	String getNome();

	int getAnoLetivo();

	boolean isAtivo();

	int getProfessorId();

	String getProfessorNome();

	int getQuantidadeAlunos();
}

