package dev.com.qnota.apresentacao.principal.aluno;

import java.time.LocalDate;
import java.util.List;

public class AlunoFormulario {
	public AlunoDto aluno;
	public List<dev.com.qnota.aplicacao.principal.responsavel.ResponsavelResumo> responsaveis;
	public List<dev.com.qnota.aplicacao.principal.turma.TurmaResumo> turmas;

	public AlunoFormulario(AlunoDto aluno, 
	                      List<dev.com.qnota.aplicacao.principal.responsavel.ResponsavelResumo> responsaveis,
	                      List<dev.com.qnota.aplicacao.principal.turma.TurmaResumo> turmas) {
		this.aluno = aluno;
		this.responsaveis = responsaveis;
		this.turmas = turmas;
	}

	public static class AlunoDto {
		public Integer id;
		public String nome;
		public LocalDate dataNascimento;
		public Integer turmaId;
		public List<Integer> responsaveis;
		public Integer responsavelPrincipalId;
	}

	public static class VinculoDto {
		public Integer responsavelId;
		public boolean principal;
	}

	public static class NotaDto {
		public Integer simuladoId;
		public Integer disciplinaId;
		public double valor;
	}

	public static class RetificacaoDto {
		public Integer simuladoId;
		public Integer disciplinaId;
		public Integer professorId;
		public double novoValor;
		public String justificativa;
	}

	public static class NomeDto {
		public String nome;
	}
}







