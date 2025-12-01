/* Título da análise: QNota - Padrão Proxy aplicado ao SimuladoRepositorio */
package dev.com.qnota.dominio.principal.simulado;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import dev.com.qnota.dominio.principal.aluno.AlunoId;
import dev.com.qnota.dominio.principal.professor.ProfessorId;
import dev.com.qnota.dominio.principal.turma.TurmaId;

/**
 * Proxy para SimuladoRepositorio.
 *
 * Objetivo:
 * - interceptar chamadas ao repositório de Simulado
 * - adicionar comportamentos transversais (ex.: auditoria, logs)
 * - delegar a execução real para um repositório "alvo"
 *
 * Mantém o mesmo contrato de SimuladoRepositorio.
 */
public class SimuladoRepositorioDecorator implements SimuladoRepositorio {

    private final SimuladoRepositorio alvo;
    private final SimuladoAuditoria auditoria;

    public SimuladoRepositorioDecorator(SimuladoRepositorio alvo,
                                    SimuladoAuditoria auditoria) {
        this.alvo = Objects.requireNonNull(alvo);
        this.auditoria = Objects.requireNonNull(auditoria);
    }

    @Override
    public SimuladoId salvar(Simulado s) {
        auditoria.registrarSalvar(s);
        return alvo.salvar(s);
    }

    @Override
    public Simulado porId(SimuladoId id) {
        auditoria.registrarLeitura(id);
        return alvo.porId(id);
    }

    @Override
    public int contarEmEdicaoPorTurma(TurmaId turmaId) {
        return alvo.contarEmEdicaoPorTurma(turmaId);
    }

    @Override
    public List<Simulado> listarPorTurma(TurmaId turmaId) {
        return alvo.listarPorTurma(turmaId);
    }

    @Override
    public Map<Integer, Double> pesosDoSimulado(SimuladoId id) {
        return alvo.pesosDoSimulado(id);
    }

    @Override
    public boolean todasNotasLancadas(SimuladoId id) {
        return alvo.todasNotasLancadas(id);
    }

    @Override
    public boolean possuiSimuladoFinalizadoParaProfessor(ProfessorId professorId) {
        return alvo.possuiSimuladoFinalizadoParaProfessor(professorId);
    }

    @Override
    public boolean possuiSimuladoFinalizadoParaAluno(AlunoId alunoId) {
        return alvo.possuiSimuladoFinalizadoParaAluno(alunoId);
    }

    @Override
    public boolean temNotasPendentesEmSimuladosEmEdicao(AlunoId alunoId) {
        return alvo.temNotasPendentesEmSimuladosEmEdicao(alunoId);
    }

    @Override
    public void remover(SimuladoId id) {
        auditoria.registrarRemocao(id);
        alvo.remover(id);
    }
}
