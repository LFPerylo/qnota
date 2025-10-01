/* Título da análise: QNota - Serviço de Aplicação para Aluno (RNs 02, 03, 04, 31..33, 57, 58, 67) */
package dev.com.qnota.dominio.academico.aluno;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import dev.com.qnota.dominio.academico.aluno.Aluno.AlunoResponsavel;
import dev.com.qnota.dominio.academico.responsavel.Responsavel;
import dev.com.qnota.dominio.academico.responsavel.ResponsavelId;
import dev.com.qnota.dominio.academico.responsavel.ResponsavelRepositorio;
import dev.com.qnota.dominio.academico.turma.TurmaId;

public class AlunoServico {

    private final AlunoRepositorio repo;
    private final ResponsavelRepositorio responsavelRepo;

    public AlunoServico(AlunoRepositorio repo, ResponsavelRepositorio responsavelRepo) {
        this.repo = repo;
        this.responsavelRepo = responsavelRepo;
    }

    public void cadastrar(AlunoId id, String nome, LocalDate nascimento, TurmaId turma, List<AlunoResponsavel> responsaveis) {
        if (repo.existeOutroComMesmoNomeENascimentoNaTurma(nome, nascimento, turma)) {
            throw new IllegalArgumentException("RN-03: Já existe aluno com mesmo nome e nascimento na turma.");
        }
        if (responsaveis == null || responsaveis.isEmpty()) {
            throw new IllegalArgumentException("RN-19: Aluno deve ter ao menos um responsável.");
        }
        if (responsaveis.size() > 3) {
            throw new IllegalArgumentException("RN-02: Máximo de 3 responsáveis por aluno.");
        }
        long qtdPrincipais = responsaveis.stream().filter(AlunoResponsavel::principal).count();
        if (qtdPrincipais != 1) {
            throw new IllegalArgumentException("RN-58: Deve haver exatamente um responsável principal.");
        }
        // RN-136: não permitir vínculo inicial com responsável inadimplente
        for (AlunoResponsavel ar : responsaveis) {
            var r = responsavelRepo.porId(ar.responsavel()).orElseThrow();
            if (r.getStatus() == Responsavel.Status.INADIMPLENTE) {
                throw new IllegalStateException("RN-136: Responsável inadimplente não pode ser vinculado.");
            }
        }
        var aluno = new Aluno(id, nome, nascimento, true, turma, responsaveis);
        repo.salvar(aluno);
    }

    public void transferir(AlunoId id, TurmaId novaTurma, boolean possuiSimuladoFinalizadoNaAtual,
                           int anoLetivoAtual, int anoLetivoNova) {
        if (possuiSimuladoFinalizadoNaAtual) {
            throw new IllegalStateException("RN-57.1: Não pode transferir com simulados finalizados.");
        }
        if (anoLetivoAtual != anoLetivoNova) {
            throw new IllegalStateException("RN-57.2: Nova turma deve ser do mesmo ano letivo.");
        }
        var aluno = repo.porId(id).orElseThrow();
        var atualizado = new Aluno(aluno.getId(), aluno.getNome(), aluno.getDataNascimento(),
                aluno.isAtivo(), novaTurma, aluno.getResponsaveis());
        repo.salvar(atualizado);
    }

    public void inativar(AlunoId id) {
        // RN-67: não pode inativar com notas pendentes em simulados em edição
        if (repo.temNotasPendentesEmSimuladosEmEdicao(id)) {
            throw new IllegalStateException("RN-67: Não é possível inativar com notas pendentes em simulados em edição.");
        }
        var aluno = repo.porId(id).orElseThrow();
        var atualizado = new Aluno(aluno.getId(), aluno.getNome(), aluno.getDataNascimento(),
                false, aluno.getTurma(), aluno.getResponsaveis());
        repo.salvar(atualizado);
    }

    public void excluir(AlunoId id) {
        // RN-04: só excluir se não tiver notas
        if (repo.temNotas(id)) {
            throw new IllegalStateException("RN-04: Aluno não pode ser excluído pois possui notas registradas.");
        }
        repo.remover(id);
    }

    public void vincularResponsavel(AlunoId id, ResponsavelId resp, String grauParentesco, boolean principal) {
        var aluno = repo.porId(id).orElseThrow();

        var r = responsavelRepo.porId(resp).orElseThrow();
        if (r.getStatus() == Responsavel.Status.INADIMPLENTE) {
            throw new IllegalStateException("RN-136: Responsável inadimplente não pode ser vinculado.");
        }

        var novaLista = new ArrayList<>(aluno.getResponsaveis());
        if (novaLista.size() >= 3) throw new IllegalStateException("RN-02: Máximo de 3 responsáveis.");
        boolean jaVinculado = novaLista.stream().anyMatch(ar -> ar.responsavel().equals(resp));
        if (jaVinculado) throw new IllegalStateException("RN-20: Vínculo de responsável duplicado para este aluno.");
        if (principal && novaLista.stream().anyMatch(AlunoResponsavel::principal)) {
            throw new IllegalStateException("RN-58: Já existe responsável principal.");
        }
        novaLista.add(new AlunoResponsavel(resp, grauParentesco, principal));
        repo.salvar(new Aluno(aluno.getId(), aluno.getNome(), aluno.getDataNascimento(), aluno.isAtivo(), aluno.getTurma(), novaLista));
    }
}
