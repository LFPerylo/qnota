package dev.com.qnota.apresentacao.principal.responsavel;

public class ResponsavelFormulario {
	public ResponsavelDto responsavel;

	public ResponsavelFormulario(ResponsavelDto responsavel) {
		this.responsavel = responsavel;
	}

	public static class ResponsavelDto {
		public Integer id;
		public String nome;
		public String cpf;
		public String email;
	}
}


