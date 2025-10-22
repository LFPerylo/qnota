package dev.com.qnota.dominio.principal.aluno;

import java.util.Objects;

/**
 * Value Object que representa o ID único de uma nota.
 */
public class NotaId {
    
    private final String valor;
    
    public NotaId(String valor) {
        this.valor = Objects.requireNonNull(valor, "'valor' não pode ser nulo");
    }
    
    public String getValor() {
        return valor;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotaId notaId = (NotaId) o;
        return Objects.equals(valor, notaId.valor);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }
    
    @Override
    public String toString() {
        return valor;
    }
}
