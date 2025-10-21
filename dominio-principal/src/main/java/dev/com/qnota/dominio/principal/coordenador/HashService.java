package dev.com.qnota.dominio.principal.coordenador;

/** Porta para hash/validação de senha (BCrypt/Argon/etc. na infraestrutura). */
public interface HashService {
    String hash(String rawPassword);
    boolean matches(String rawPassword, String hashedPassword);
}
