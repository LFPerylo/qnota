/* Título da análise: QNota - Serviço Disciplina (RN-121, RN-43, RN-62, RN-44) */
package dev.com.qnota.dominio.principal.disciplina;

public class DisciplinaServico {
    private final DisciplinaRepositorio repo;

    public DisciplinaServico(DisciplinaRepositorio repo) { this.repo = repo; }

    public void cadastrar(Disciplina d) {
        if (repo.existeNomeNaArea(d.getNome(), d.getArea().nome()))
            throw new IllegalArgumentException("RN-121: Nome único por área.");
        repo.salvar(d);
    }

    public void editar(DisciplinaId id, String novoNome, String novaArea) {
        var d = repo.porId(id).orElseThrow();
        if (repo.foiUsadaEmSimuladoFinalizado(id)) {
            // RN-62: criar nova versão preservando a antiga
            var novaVersao = new Disciplina(new DisciplinaId(0), novoNome, d.getVersao() + 1, d.getIdVersaoOrigem() == null ? d.getId().value() : d.getIdVersaoOrigem(), true,
                    new Disciplina.AreaConhecimento(d.getArea().id(), novaArea));
            cadastrar(novaVersao);
        } else {
            var atualizada = new Disciplina(d.getId(), novoNome, d.getVersao(), d.getIdVersaoOrigem(), d.isAtivo(),
                    new Disciplina.AreaConhecimento(d.getArea().id(), novaArea));
            repo.salvar(atualizada);
        }
    }

    public void excluir(DisciplinaId id) {
        if (repo.foiUsadaEmAlgumSimulado(id)) throw new IllegalStateException("RN-44: Disciplina já utilizada.");
        // remoção física na implementação
    }
}
