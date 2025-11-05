package dev.com.qnota.aplicacao.principal.simulado;

import java.time.LocalDate;

import dev.com.qnota.dominio.principal.simulado.Simulado;

public interface SimuladoResumo {
	int getId();

	LocalDate getDataAplicacao();

	Simulado.Status getStatus();

	int getTurmaId();

	String getTurmaNome();

	int getQuantidadeDisciplinas();
}

