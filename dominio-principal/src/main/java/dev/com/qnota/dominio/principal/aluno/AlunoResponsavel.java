package dev.com.qnota.dominio.principal.aluno;

import java.util.Objects;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelId;

/** VO que representa o vínculo responsável<-aluno. */
public record AlunoResponsavel(ResponsavelId responsavel, boolean principal) {
    public AlunoResponsavel {
        Objects.requireNonNull(responsavel, "'responsavel' não pode ser nulo");
    }
}