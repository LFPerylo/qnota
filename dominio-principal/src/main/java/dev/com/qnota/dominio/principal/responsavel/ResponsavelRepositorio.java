package dev.com.qnota.dominio.principal.responsavel;

import java.util.Optional;

import dev.com.qnota.dominio.principal.aluno.AlunoId;

public interface ResponsavelRepositorio {

    /**
     * Persiste o responsável. Se {@code getId()==null}, a infraestrutura deve
     * gerar um novo ID e chamar {@code atribuirIdSeAusente(novoId)} antes de concluir.
     */
    void salvar(Responsavel r);

    Optional<Responsavel> porId(ResponsavelId id);

    boolean cpfExiste(String cpf);

    // Edição/exclusão
    void atualizarContato(ResponsavelId id, String novoNome, String novoEmail); // opcional; salvar(r) também pode persistir
    void excluir(ResponsavelId id);

    // Vínculos com aluno
    boolean estaVinculadoAAlgumAluno(ResponsavelId id);
    boolean vinculadoAoAluno(ResponsavelId id, AlunoId alunoId);
    void vincular(ResponsavelId id, AlunoId alunoId);
    void desvincular(ResponsavelId id, AlunoId alunoId);
    int quantidadeResponsaveisDoAluno(AlunoId alunoId);
    void definirPrincipal(ResponsavelId id, AlunoId alunoId);

    // RN-136
    boolean estaInadimplente(ResponsavelId id);
}
