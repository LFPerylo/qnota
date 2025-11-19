package dev.com.qnota.dominio.principal.disciplina;

import java.util.Objects;

public class Disciplina {

    // ID agora pode começar nulo e é atribuído pelo repositório
    private DisciplinaId id;

    private String nome;
    private int versao;                 // >= 1
    private Integer idVersaoOrigem;     // nulo na v1; nas novas versões guarda o id (int) da origem
    private boolean ativo;
    private AreaConhecimento area;

    /** Constrói a v1 (ativa) sem ID. O repositório atribuirá o ID. */
    public Disciplina(String nome, AreaConhecimento area) {
        this.nome = requireNonBlank(nome, "'nome' não pode ser vazio");
        this.area = Objects.requireNonNull(area, "'area' não pode ser nula").normalize();
        this.versao = 1;
        this.idVersaoOrigem = null;
        this.ativo = true;
    }

    /** Construtor interno para versões subsequentes. */
    private Disciplina(String nome, AreaConhecimento area, int versao, Integer idVersaoOrigem, boolean ativo) {
        this.nome = requireNonBlank(nome, "'nome' não pode ser vazio");
        this.area = Objects.requireNonNull(area, "'area' não pode ser nula").normalize();
        if (versao < 1) throw new IllegalArgumentException("'versao' deve ser >= 1");
        this.versao = versao;
        this.idVersaoOrigem = idVersaoOrigem;
        this.ativo = ativo;
    }

    // ===== atribuição de ID (infra chama após inserir) =====
    public void atribuirIdSeAusente(DisciplinaId novoId) {
        Objects.requireNonNull(novoId, "'id' não pode ser nulo");
        if (this.id != null) {
            // evitar sobrescrita acidental
            if (!this.id.equals(novoId)) {
                throw new IllegalStateException("ID já atribuído para esta disciplina");
            }
            return;
        }
        this.id = novoId;
    }

    // ===== getters =====
    public DisciplinaId getId()            { return id; }
    public String getNome()                { return nome; }
    public int getVersao()                 { return versao; }
    public Integer getIdVersaoOrigem()     { return idVersaoOrigem; }
    public boolean isAtivo()               { return ativo; }
    public AreaConhecimento getArea()      { return area; }

    // ===== comportamentos =====
    public void renomear(String novoNome) {
        this.nome = requireNonBlank(novoNome, "'nome' não pode ser vazio");
    }

    public void mudarArea(AreaConhecimento novaArea) {
        this.area = Objects.requireNonNull(novaArea, "'area' não pode ser nula").normalize();
    }

    public void ativar()   { this.ativo = true;  }
    public void inativar() { this.ativo = false; }

    /** Cria uma nova versão (v+1) preservando o vínculo com a origem (RN-62). */
    public Disciplina novaVersao(String novoNome, AreaConhecimento novaArea) {
        if (this.id == null) {
            throw new IllegalStateException("Não é possível versionar sem ID atribuído na disciplina atual");
        }
        int origem = (this.idVersaoOrigem == null) ? this.id.value() : this.idVersaoOrigem;
        var nova = new Disciplina(
            requireNonBlank(novoNome, "'nome' não pode ser vazio"),
            Objects.requireNonNull(novaArea, "'area' não pode ser nula"),
            this.versao + 1,
            origem,
            true
        );
        return nova; // ID da nova versão será atribuído pelo repositório
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
