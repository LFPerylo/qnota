package dev.com.qnota.aplicacao.principal.ranking;

public interface RankingResumo {
	int getId();

	int getSimuladoId();

	String getSimuladoDataAplicacao();

	String getSimuladoTurmaNome();

	boolean isCongelado();

	int getQuantidadeLinhas();
}

