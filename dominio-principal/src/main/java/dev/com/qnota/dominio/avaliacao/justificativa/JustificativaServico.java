/* Título da análise: QNota - Serviço Justificativa (RN-37..39) */
package dev.com.qnota.dominio.avaliacao.justificativa;

import java.time.LocalDateTime;

import dev.com.qnota.dominio.academico.professor.ProfessorId;
import dev.com.qnota.dominio.avaliacao.nota.NotaId;

public class JustificativaServico {
    private final JustificativaRepositorio repo;

    public JustificativaServico(JustificativaRepositorio repo) { this.repo = repo; }

    public void registrar(JustificativaId id, NotaId nota, double anterior, double corrigida, String texto, ProfessorId professor) {
        var j = new Justificativa(id, nota, anterior, corrigida, texto, LocalDateTime.now(), professor);
        repo.salvar(j);
    }
}
