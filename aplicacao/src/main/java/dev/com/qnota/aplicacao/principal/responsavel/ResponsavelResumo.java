package dev.com.qnota.aplicacao.principal.responsavel;

import dev.com.qnota.dominio.principal.responsavel.Responsavel;

public interface ResponsavelResumo {
	int getId();

	String getNome();

	String getEmail();

	String getCpf();

	int getQuantidadeAlunos();

	Responsavel.Status getStatus();
}

