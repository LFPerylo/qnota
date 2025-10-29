package dev.com.qnota.dominio.principal.responsavel;

import dev.com.qnota.dominio.principal.aluno.AlunoId;

/**
 * Interface para quebrar dependência circular entre ResponsavelServico e AlunoServico.
 * Permite que ResponsavelServico verifique vínculos sem depender diretamente de AlunoServico.
 */
public interface ResponsavelVinculoService {
    
    /**
     * Verifica se o responsável possui vínculos ativos com alunos.
     */
    boolean possuiVinculosAtivos(ResponsavelId responsavelId);
    
    /**
     * Remove todos os vínculos do responsável com alunos.
     */
    void removerVinculos(ResponsavelId responsavelId);
    
    /**
     * Vincula responsável a um aluno.
     */
    void vincularResponsavel(ResponsavelId responsavelId, AlunoId alunoId, boolean principal);
    
    /**
     * Desvincula responsável de um aluno.
     */
    void desvincularResponsavel(ResponsavelId responsavelId, AlunoId alunoId);
}
