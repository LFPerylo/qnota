package dev.com.qnota.dominio.principal.ranking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dev.com.qnota.dominio.principal.aluno.AlunoId;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;

/** Agregado de Ranking por simulado (linhas + estado de congelamento). */
public class Ranking {
    /** Atribuído pelo repositório na primeira persistência. Pode ser nulo enquanto transient. */
    private RankingId id;

    private final SimuladoId simulado;
    private boolean congelado;
    private final List<Linha> linhas;

    /** Cria um ranking "novo" (sem id) para um simulado. O repositório atribui o id ao salvar. */
    public Ranking(SimuladoId simulado, List<Linha> linhas) {
        this.simulado = Objects.requireNonNull(simulado, "'simulado' não pode ser nulo");
        this.linhas = copiarValidar(linhas);
        this.congelado = false;
    }

    public RankingId getId() { return id; }
    public SimuladoId getSimulado() { return simulado; }
    public boolean isCongelado() { return congelado; }
    public List<Linha> getLinhas() { return Collections.unmodifiableList(linhas); }

    /** Exclusivo do repositório: atribui o id quando persistir pela primeira vez. */
    public void atribuirIdSeAusente(RankingId novoId) {
        Objects.requireNonNull(novoId, "'id' não pode ser nulo");
        if (this.id != null && !this.id.equals(novoId)) {
            throw new IllegalStateException("ID já atribuído para este ranking");
        }
        this.id = novoId;
    }

    public void substituirLinhas(List<Linha> novas) {
        var tmp = copiarValidar(novas);
        linhas.clear();
        linhas.addAll(tmp);
    }

    public void congelar() { this.congelado = true; }

    // ===== tipos de linha =====
    public record Linha(AlunoId aluno, double media, int posicao) {
        public Linha {
            Objects.requireNonNull(aluno, "'aluno' não pode ser nulo");
            if (posicao < 1) throw new IllegalArgumentException("'posicao' deve ser >= 1");
            if (Double.isNaN(media) || Double.isInfinite(media)) {
                throw new IllegalArgumentException("'media' inválida");
            }
        }
    }

    // ===== helpers =====
    private static List<Linha> copiarValidar(List<Linha> origem) {
        if (origem == null) return new ArrayList<>();
        var tmp = new ArrayList<Linha>(origem.size());
        for (var l : origem) {
            if (l == null) throw new IllegalArgumentException("Linha do ranking não pode ser nula");
            tmp.add(l);
        }
        return tmp;
    }
}
