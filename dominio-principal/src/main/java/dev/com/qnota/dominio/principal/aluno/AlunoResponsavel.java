package dev.com.qnota.dominio.principal.aluno;

import dev.com.qnota.dominio.principal.responsavel.ResponsavelId;

public class AlunoResponsavel {
    private final ResponsavelId responsavel;
    private final boolean principal;
    
    public AlunoResponsavel(ResponsavelId responsavel, boolean principal) {
        this.responsavel = responsavel;
        this.principal = principal;
    }
    
    public ResponsavelId getResponsavel() {
        return responsavel;
    }
    
    public boolean isPrincipal() {
        return principal;
    }
}
