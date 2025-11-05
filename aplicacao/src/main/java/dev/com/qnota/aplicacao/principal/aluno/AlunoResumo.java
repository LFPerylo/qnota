package dev.com.qnota.aplicacao.principal.aluno;

import java.time.LocalDate;

public interface AlunoResumo {
	int getId();

	String getNome();

	LocalDate getDataNascimento();

	boolean isAtivo();

	int getTurmaId();

	String getTurmaNome();

	int getQuantidadeResponsaveis();
}

