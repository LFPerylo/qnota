package dev.com.qnota.dominio.principal.nota;

import java.util.List;
import java.util.Optional;

import dev.com.qnota.dominio.principal.aluno.AlunoId;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;

public interface NotaRepositorio {

    /**
     * Persiste a nota. Se {@code getId()==null}, gera/atribui um novo ID
     * (via {@code atribuirIdSeAusente}) e retorna esse ID. Caso contrário,
     * atualiza e retorna o mesmo ID.
     */
    NotaId salvar(Nota n);

    Optional<Nota> porId(NotaId id);

    /** Busca por chave natural (aluno+simulado+disciplina) para evitar duplicidade — RN-33. */
    Optional<Nota> porChave(AlunoId aluno, SimuladoId simulado, DisciplinaId disciplina);

    List<Nota> porSimulado(SimuladoId simulado);

    /** RN-32/39: indica se o simulado está EM_EDICAO. */
    boolean simuladoEstaEmEdicao(SimuladoId simulado);
}
