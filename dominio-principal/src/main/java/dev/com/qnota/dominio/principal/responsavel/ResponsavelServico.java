/* Título da análise: QNota - Serviço de Aplicação para Responsavel (RNs 17, 21, 24 implícita CPF imutável, 136) */
package dev.com.qnota.dominio.principal.responsavel;

public class ResponsavelServico {
    private final ResponsavelRepositorio repo;

    public ResponsavelServico(ResponsavelRepositorio repo) { this.repo = repo; }

    public void cadastrar(Responsavel r) {
        if (repo.cpfExiste(r.getCpf())) throw new IllegalArgumentException("CPF já cadastrado.");
        repo.salvar(r);
    }

    public void marcarInadimplente(ResponsavelId id) {
        var r = repo.porId(id).orElseThrow();
        var atualizado = new Responsavel(r.getId(), r.getNome(), r.getCpf(), r.getEmail(), Responsavel.Status.INADIMPLENTE);
        repo.salvar(atualizado);
    }

    public void regularizar(ResponsavelId id) {
        var r = repo.porId(id).orElseThrow();
        var atualizado = new Responsavel(r.getId(), r.getNome(), r.getCpf(), r.getEmail(), Responsavel.Status.ATIVO);
        repo.salvar(atualizado);
    }

    public void excluir(ResponsavelId id) {
        if (repo.estaVinculadoAAlgumAluno(id)) throw new IllegalStateException("RN-21: Responsável vinculado a aluno.");
        // remoção física ou lógica conforme a implementação do repositório
    }
}
