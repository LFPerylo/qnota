/* Título da análise: QNota – Repositório do Agregado Responsável (assinaturas usadas pelos serviços e testes) */
package dev.com.qnota.dominio.principal.responsavel;

import java.util.Optional;

import dev.com.qnota.dominio.principal.aluno.AlunoId;

public interface ResponsavelRepositorio {
    void salvar(Responsavel r);
    Optional<Responsavel> porId(ResponsavelId id);

    boolean cpfExiste(String cpf);

    // NOVO (edição de dados sem trocar CPF)
    void atualizarContato(ResponsavelId id, String novoNome, String novoEmail);

    // NOVO (exclusão – RN-21 bloqueia se houver vínculos)
    void excluir(ResponsavelId id);

    // Vínculos com aluno
    boolean estaVinculadoAAlgumAluno(ResponsavelId id);
    // NOVO: vínculo específico com um aluno (RN-20)
    boolean vinculadoAoAluno(ResponsavelId id, AlunoId alunoId);
    // NOVO: operações de vínculo (RN-19/RN-20)
    void vincular(ResponsavelId id, AlunoId alunoId);
    void desvincular(ResponsavelId id, AlunoId alunoId);
    // NOVO: quantidade de responsáveis do aluno (RN-02/RN-19)
    int quantidadeResponsaveisDoAluno(AlunoId alunoId);
    // NOVO: responsável principal obrigatório (RN-58)
    void definirPrincipal(ResponsavelId id, AlunoId alunoId);

    // NOVO: inadimplência bloqueia novo vínculo (RN-136)
    boolean estaInadimplente(ResponsavelId id);
}
