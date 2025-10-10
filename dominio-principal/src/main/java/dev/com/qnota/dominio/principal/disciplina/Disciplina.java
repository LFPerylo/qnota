package dev.com.qnota.dominio.principal.disciplina;

import java.util.Objects;

public class Disciplina {

    private final DisciplinaId id;
    private String nome;
    private int versao;                 // >= 1
    private Integer idVersaoOrigem;     // nulo na v1; nas novas versões guarda o id da origem
    private boolean ativo;
    private AreaConhecimento area;

    public Disciplina(DisciplinaId id,
                      String nome,
                      int versao,
                      Integer idVersaoOrigem,
                      boolean ativo,
                      AreaConhecimento area) {

        this.id = Objects.requireNonNull(id, "'id' não pode ser nulo");
        this.nome = requireNonBlank(nome, "'nome' não pode ser vazio");
        if (versao < 1) throw new IllegalArgumentException("'versao' deve ser >= 1");
        this.versao = versao;
        this.idVersaoOrigem = idVersaoOrigem; // pode ser nulo na primeira versão
        this.ativo = ativo;
        this.area = Objects.requireNonNull(area, "'area' não pode ser nula").normalize();
    }

    // ===== getters =====
    public DisciplinaId getId()            { return id; }
    public String getNome()                { return nome; }
    public int getVersao()                 { return versao; }
    public Integer getIdVersaoOrigem()     { return idVersaoOrigem; }
    public boolean isAtivo()               { return ativo; }
    public AreaConhecimento getArea()      { return area; }

    // ===== comportamentos internos (apenas estado local) =====
    public void renomear(String novoNome) {
        this.nome = requireNonBlank(novoNome, "'nome' não pode ser vazio");
    }

    public void mudarArea(AreaConhecimento novaArea) {
        this.area = Objects.requireNonNull(novaArea, "'area' não pode ser nula").normalize();
    }

    public void ativar()   { this.ativo = true;  }
    public void inativar() { this.ativo = false; }

    /** Cria uma nova versão (v+1) preservando o vínculo com a origem (RN-62). */
    public Disciplina novaVersao(DisciplinaId novoId, String novoNome, AreaConhecimento novaArea) {
        var origem = (this.idVersaoOrigem == null) ? this.id.value() : this.idVersaoOrigem;
        return new Disciplina(
                Objects.requireNonNull(novoId),
                requireNonBlank(novoNome, "'nome' não pode ser vazio"),
                this.versao + 1,
                origem,
                true,
                Objects.requireNonNull(novaArea, "'area' não pode ser nula").normalize()
        );
    }

    // ===== helpers =====
    private static String requireNonBlank(String s, String msg) {
        if (s == null || s.trim().isEmpty()) throw new IllegalArgumentException(msg);
        return s.trim();
    }

    /** VO simples para área, definido dentro do agregado. */
    public record AreaConhecimento(int id, String nome) {
        public AreaConhecimento {
            if (nome == null || nome.trim().isEmpty())
                throw new IllegalArgumentException("'area.nome' não pode ser vazio");
        }
        private AreaConhecimento normalize() {
            return new AreaConhecimento(id, nome.trim());
        }
    }
}
