package dev.com.qnota.dominio.principal.disciplina;

import java.util.Objects;

public class DisciplinaServico {

    private final DisciplinaRepositorio repo;

    public DisciplinaServico(DisciplinaRepositorio repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    /** Cadastra v1 ativa, garantindo unicidade por área (RN-121). */
    public void cadastrar(String nome, Disciplina.AreaConhecimento area) {
        if (repo.existeNomeNaArea(nome, area.nome()))
            throw new IllegalArgumentException("RN-121: Nome único por área.");
        var id = repo.proximoId();
        var nova = new Disciplina(id, nome, 1, null, true, area);
        repo.salvar(nova);
    }

    /**
     * Editar nome/área.
     * Se já foi usada em simulado FINALIZADO (RN-62) → cria nova versão.
     * Sempre respeita unicidade por área quando há mudança de nome/área (RN-121).
     */
    public void editar(DisciplinaId id, String novoNome, Disciplina.AreaConhecimento novaArea) {
        var atual = repo.porId(id).orElseThrow(() ->
                new IllegalStateException("Disciplina não encontrada"));

        boolean mudouNome = !atual.getNome().equalsIgnoreCase(novoNome);
        boolean mudouArea = !atual.getArea().nome().equalsIgnoreCase(novaArea.nome());
        if (mudouNome || mudouArea) {
            if (repo.existeNomeNaArea(novoNome, novaArea.nome()))
                throw new IllegalArgumentException("RN-121: Nome único por área.");
        }

        if (repo.foiUsadaEmSimuladoFinalizado(id)) {
            var nova = atual.novaVersao(repo.proximoId(), novoNome, novaArea); // RN-62
            repo.salvar(nova);
        } else {
            atual.renomear(novoNome);
            atual.mudarArea(novaArea);
            repo.salvar(atual);
        }
    }

    public void ativar(DisciplinaId id) {
        var d = repo.porId(id).orElseThrow(() ->
                new IllegalStateException("Disciplina não encontrada"));
        d.ativar();
        repo.salvar(d);
    }

    public void inativar(DisciplinaId id) {
        var d = repo.porId(id).orElseThrow(() ->
                new IllegalStateException("Disciplina não encontrada"));
        d.inativar();
        repo.salvar(d);
    }

    /** Não excluir se já foi usada em qualquer simulado (RN-44). */
    public void excluir(DisciplinaId id) {
        if (repo.foiUsadaEmAlgumSimulado(id))
            throw new IllegalStateException("RN-44: Disciplina já utilizada.");
        repo.remover(id);
    }
}
