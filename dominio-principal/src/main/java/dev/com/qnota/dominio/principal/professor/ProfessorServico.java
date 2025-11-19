package dev.com.qnota.dominio.principal.professor;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import dev.com.qnota.dominio.principal.aluno.AlunoRepositorio;
import dev.com.qnota.dominio.principal.simulado.SimuladoRepositorio;

public class ProfessorServico {

    private final ProfessorRepositorio repo;
    private final SimuladoRepositorio simuladoRepo;

    public ProfessorServico(ProfessorRepositorio repo, AlunoRepositorio alunoRepo, SimuladoRepositorio simuladoRepo) {
        this.repo = Objects.requireNonNull(repo);
        Objects.requireNonNull(alunoRepo); // mantido por compatibilidade de API, mas não usado
        this.simuladoRepo = Objects.requireNonNull(simuladoRepo);
    }

    /** Cadastro: entidade garante NOT NULL/NOT BLANK, serviço garante RN-84. */
    public ProfessorId cadastrar(String nome, String cpf, String email, List<String> especialidades) {
        // Validações básicas de entrada
        if (nome == null || nome.trim().isEmpty())
            throw new IllegalArgumentException("Nome não pode ser vazio");
        if (cpf == null || cpf.trim().isEmpty())
            throw new IllegalArgumentException("CPF não pode ser vazio");
        if (email == null || email.trim().isEmpty())
            throw new IllegalArgumentException("Email não pode ser vazio");
        if (especialidades == null)
            throw new IllegalArgumentException("Especialidades não podem ser nulas");
        
        // RN-84: professor deve ter ao menos uma especialidade
        if (especialidades.isEmpty()) {
            throw new IllegalArgumentException("RN-84: Professor deve ter ao menos uma especialidade.");
        }
        
        // RN-84: especialidades duplicadas não são permitidas
        Set<String> vistos = new HashSet<>();
        for (String e : especialidades) {
            String key = e.trim().toLowerCase();
            if (key.isEmpty()) {
                throw new IllegalArgumentException("especialidade não pode ser vazia");
            }
            if (!vistos.add(key)) {
                throw new IllegalArgumentException("RN-84: Especialidades duplicadas não são permitidas: " + e);
            }
        }
        
        var p = new Professor(nome, cpf, email, especialidades); // sem ID; ORM atribui
        return repo.salvar(p); // retorna o ProfessorId gerado
    }

    /** Edição de dados de contato (CPF é imutável na entidade). */
    public void atualizarContato(ProfessorId id, String novoNome, String novoEmail) {
        var p = repo.porId(id);
        p.renomear(novoNome);
        p.alterarEmail(novoEmail);
        repo.salvar(p); // ignoramos o retorno aqui
    }

    /** RN-07: no máximo 5 turmas ativas (checagem centralizada no serviço). */
    public void validarLimiteDeTurmas(ProfessorId id) {
        if (repo.contarTurmasAtivas(id) > 5)
            throw new IllegalStateException("RN-07: Até 5 turmas simultâneas.");
    }

    /**
     * Exclusão com substituição:
     * - RN-26A: não pode excluir se houver simulados finalizados associados às turmas do professor.
     * - RN-125: substitui nas turmas e (por política) nos simulados futuros.
     */
    public void removerComSubstituto(ProfessorId aRemover, ProfessorId substituto) {
        if (simuladoRepo.possuiSimuladoFinalizadoParaProfessor(aRemover))
            throw new IllegalStateException("RN-26A: Não pode excluir se houver simulados finalizados vinculados.");
        repo.substituirProfessor(aRemover, substituto); // move vínculos p/ o substituto
    }

    /** Conveniências para especialidades (invariantes ficam na entidade). */
    public void adicionarEspecialidade(ProfessorId id, String area) {
        var p = repo.porId(id);
        p.adicionarEspecialidade(area);
        repo.salvar(p);
    }

    public void removerEspecialidade(ProfessorId id, String area) {
        var p = repo.porId(id);
        
        // RN-84: Verificar se ficará com pelo menos uma especialidade após remoção
        if (p.getEspecialidades().size() <= 1) {
            throw new IllegalStateException("Professor deve ter ao menos uma especialidade");
        }
        
        p.removerEspecialidade(area);
        repo.salvar(p);
    }

    public List<String> areasDoProfessor(ProfessorId id) {
        return repo.nomesDeAreasDoProfessor(id);
    }
}
