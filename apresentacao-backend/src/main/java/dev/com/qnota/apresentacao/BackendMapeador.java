package dev.com.qnota.apresentacao;

import org.springframework.stereotype.Component;

import dev.com.qnota.dominio.principal.aluno.AlunoId;
import dev.com.qnota.dominio.principal.coordenador.CoordenadorId;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.dominio.principal.professor.ProfessorId;
import dev.com.qnota.dominio.principal.ranking.RankingId;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelId;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;
import dev.com.qnota.dominio.principal.turma.TurmaId;

/**
 * Mapeador manual para conversão entre DTOs da apresentação e objetos de domínio.
 * Usa mapeamento manual, seguindo o padrão da infraestrutura.
 */
@Component
public class BackendMapeador {

	/**
	 * Método genérico para mapear Integer -> Value Object ou Value Object -> Integer.
	 */
	@SuppressWarnings("unchecked")
	public <D> D map(Object source, Class<D> destinationType) {
		if (source == null) {
			return null;
		}

		// Integer -> Value Object
		if (source instanceof Integer integer) {
			if (destinationType == AlunoId.class) {
				return (D) new AlunoId(integer);
			}
			if (destinationType == ProfessorId.class) {
				return (D) new ProfessorId(integer);
			}
			if (destinationType == TurmaId.class) {
				return (D) new TurmaId(integer);
			}
			if (destinationType == SimuladoId.class) {
				return (D) new SimuladoId(integer);
			}
			if (destinationType == DisciplinaId.class) {
				return (D) new DisciplinaId(integer);
			}
			if (destinationType == ResponsavelId.class) {
				return (D) new ResponsavelId(integer);
			}
			if (destinationType == CoordenadorId.class) {
				return (D) new CoordenadorId(integer);
			}
			if (destinationType == RankingId.class) {
				return (D) new RankingId(integer);
			}
		}

		// Value Object -> Integer
		if (destinationType == Integer.class) {
			if (source instanceof AlunoId alunoId) {
				return (D) Integer.valueOf(alunoId.value());
			}
			if (source instanceof ProfessorId professorId) {
				return (D) Integer.valueOf(professorId.value());
			}
			if (source instanceof TurmaId turmaId) {
				return (D) Integer.valueOf(turmaId.value());
			}
			if (source instanceof SimuladoId simuladoId) {
				return (D) Integer.valueOf(simuladoId.value());
			}
			if (source instanceof DisciplinaId disciplinaId) {
				return (D) Integer.valueOf(disciplinaId.value());
			}
			if (source instanceof ResponsavelId responsavelId) {
				return (D) Integer.valueOf(responsavelId.value());
			}
			if (source instanceof CoordenadorId coordenadorId) {
				return (D) Integer.valueOf(coordenadorId.value());
			}
			if (source instanceof RankingId rankingId) {
				return (D) Integer.valueOf(rankingId.value());
			}
		}

		throw new IllegalArgumentException("Tipo de mapeamento não suportado: " + source.getClass() + " -> " + destinationType);
	}
}
