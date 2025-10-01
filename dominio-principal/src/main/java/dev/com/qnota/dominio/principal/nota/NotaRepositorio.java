/* Título da análise: QNota - Repositório do Agregado Nota (assinaturas usadas pelos serviços) */
package dev.com.qnota.dominio.principal.nota;

import java.util.List;
import java.util.Optional;

import dev.com.qnota.dominio.principal.aluno.AlunoId;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;

public interface NotaRepositorio {
    void salvar(Nota n);
    Optional<Nota> porId(NotaId id);
    Optional<Nota> porChave(AlunoId aluno, SimuladoId simulado, DisciplinaId disciplina);
    List<Nota> porSimulado(SimuladoId simulado);

    boolean simuladoEstaEmEdicao(SimuladoId simulado);
}
