package dev.com.qnota.dominio.principal.responsavel;

public record ResponsavelId(int value) {
    public ResponsavelId responsavel() {
        return this;
    }
}
