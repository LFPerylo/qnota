package dev.com.qnota.dominio.principal.coordenador;

import java.util.Optional;

public interface CoordenadorRepositorio {

    /**
     * Persiste o coordenador. Se {@code getId()==null}, a infraestrutura deve
     * gerar um novo ID, chamar {@code atribuirIdSeAusente(novoId)} e retornar o ID.
     * Se já houver ID, apenas atualiza e retorna o mesmo ID.
     */
    CoordenadorId salvar(Coordenador c);

    Optional<Coordenador> porId(CoordenadorId id);
    Optional<Coordenador> porEmail(String email);

    boolean emailExiste(String email);

    void excluir(CoordenadorId id);
}
