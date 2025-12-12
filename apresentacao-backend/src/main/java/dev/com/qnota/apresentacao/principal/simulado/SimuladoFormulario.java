package dev.com.qnota.apresentacao.principal.simulado;

import java.time.LocalDate;
import java.util.List;

public class SimuladoFormulario {
	public SimuladoDto simulado;
	public List<dev.com.qnota.aplicacao.principal.disciplina.DisciplinaResumo> disciplinas;
	public List<dev.com.qnota.aplicacao.principal.turma.TurmaResumo> turmas;

	public SimuladoFormulario(SimuladoDto simulado,
	                         List<dev.com.qnota.aplicacao.principal.disciplina.DisciplinaResumo> disciplinas,
	                         List<dev.com.qnota.aplicacao.principal.turma.TurmaResumo> turmas) {
		this.simulado = simulado;
		this.disciplinas = disciplinas;
		this.turmas = turmas;
	}

	public static class SimuladoDto {
		public Integer id;
		public LocalDate dataAplicacao;
		public Integer turmaId;
		public List<DisciplinaPesoDto> disciplinas;
	}

	public static class DisciplinaPesoDto {
		public Integer disciplinaId;
		public double peso;
	}
}







