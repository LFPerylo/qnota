package dev.com.qnota.apresentacao.principal.coordenador;

public class CoordenadorFormulario {
	public CoordenadorDto coordenador;

	public CoordenadorFormulario(CoordenadorDto coordenador) {
		this.coordenador = coordenador;
	}

	public static class CoordenadorDto {
		public Integer id;
		public String nome;
		public String email;
		public String senha;
	}

	public static class LoginDto {
		public String email;
		public String senha;
	}

	public static class SenhaDto {
		public String senhaAtual;
		public String novaSenha;
	}
}


