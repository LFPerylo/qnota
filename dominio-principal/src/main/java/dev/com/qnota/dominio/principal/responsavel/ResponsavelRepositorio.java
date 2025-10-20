package dev.com.qnota.dominio.principal.responsavel;

import java.util.Optional;

public interface ResponsavelRepositorio {

    /**
     * Persiste o responsável. Se {@code getId()==null}, a infraestrutura deve
     * gerar um novo ID, chamar {@code atribuirIdSeAusente(novoId)} e retornar esse ID.
     * Se já houver ID, apenas atualiza e retorna o mesmo ID.
     */
    ResponsavelId salvar(Responsavel r);

    Optional<Responsavel> porId(ResponsavelId id);

    boolean cpfExiste(String cpf);

    // Edição/exclusão
    void atualizarContato(ResponsavelId id, String novoNome, String novoEmail);
    void excluir(ResponsavelId id);

    // Consulta para RN-21 (se houver implementação)
    boolean estaVinculadoAAlgumAluno(ResponsavelId id);

    // RN-136 (helper)
    default boolean estaInadimplente(ResponsavelId id) {
        return porId(id)
            .map(r -> r.getStatus() == Responsavel.Status.INADIMPLENTE)
            .orElse(false);
    }
}
