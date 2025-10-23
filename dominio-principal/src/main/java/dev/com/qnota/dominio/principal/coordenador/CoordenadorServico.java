package dev.com.qnota.dominio.principal.coordenador;

import java.util.Objects;

public class CoordenadorServico {

    private final CoordenadorRepositorio repo;
    private final HashService hash;

    public CoordenadorServico(CoordenadorRepositorio repo, HashService hash) {
        this.repo = Objects.requireNonNull(repo);
        this.hash = Objects.requireNonNull(hash);
    }

    /** Cadastro: e-mail único + senha com hash (ORM atribui o ID). */
    public CoordenadorId cadastrar(String nome, String email, String senhaPlano) {
        if (repo.emailExiste(email))
            throw new IllegalArgumentException("já existe coordenador com esse e-mail");
        String h = hash.hash(senhaPlano);
        var c = new Coordenador(nome, email, h, true);
        return repo.salvar(c);
    }

    /** Login/autenticação: valida credenciais e se está ativo; retorna o ID. */
    public CoordenadorId autenticar(String email, String senhaPlano) {
        var c = repo.porEmail(email).orElseThrow(() -> new IllegalStateException("coordenador não encontrado"));
        if (!c.isAtivo()) throw new IllegalStateException("coordenador inativo");
        if (!hash.matches(senhaPlano, c.getSenhaHash()))
            throw new IllegalArgumentException("email ou senha inválidos");
        return c.getId();
    }

    /** Troca de senha: exige senha atual válida. */
    public void alterarSenha(CoordenadorId id, String senhaAtual, String novaSenha) {
        var c = repo.porId(id);
        if (!hash.matches(senhaAtual, c.getSenhaHash()))
            throw new IllegalArgumentException("senha atual inválida");
        c.alterarSenhaHash(hash.hash(novaSenha));
        repo.salvar(c);
    }

    /** Atualiza nome e/ou e-mail (checando unicidade do e-mail). */
    public void atualizarContato(CoordenadorId id, String novoNome, String novoEmail) {
        var c = repo.porId(id);
        if (!c.getEmail().equalsIgnoreCase(novoEmail) && repo.emailExiste(novoEmail))
            throw new IllegalArgumentException("já existe coordenador com esse e-mail");
        c.renomear(novoNome);
        c.alterarEmail(novoEmail);
        repo.salvar(c);
    }

    public void inativar(CoordenadorId id)  { var c = repo.porId(id); c.inativar(); repo.salvar(c); }
    public void ativar(CoordenadorId id)    { var c = repo.porId(id); c.ativar();   repo.salvar(c); }
    public void excluir(CoordenadorId id)   { repo.excluir(id); }
}
