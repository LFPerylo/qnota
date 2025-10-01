package dev.com.qnota.dominio.avaliacao.disciplina;

public class Disciplina {
    private final DisciplinaId id;
    private String nome;
    private int versao;
    private Integer idVersaoOrigem;
    private boolean ativo;
    private AreaConhecimento area;

    public Disciplina(DisciplinaId id, String nome, int versao, Integer idVersaoOrigem, boolean ativo, AreaConhecimento area) {
        this.id = id;
        this.nome = nome;
        this.versao = versao;
        this.idVersaoOrigem = idVersaoOrigem;
        this.ativo = ativo;
        this.area = area;
    }

    public DisciplinaId getId() { return id; }
    public String getNome() { return nome; }
    public int getVersao() { return versao; }
    public Integer getIdVersaoOrigem() { return idVersaoOrigem; }
    public boolean isAtivo() { return ativo; }
    public AreaConhecimento getArea() { return area; }

    public record AreaConhecimento(int id, String nome) {}
}
