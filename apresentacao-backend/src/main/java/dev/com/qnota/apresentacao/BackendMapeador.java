package dev.com.qnota.apresentacao;

import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
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
 * Mapeador para conversão entre DTOs da apresentação e objetos de domínio.
 * Segue o mesmo padrão do SGB, adaptado para o domínio do Qnota.
 */
@Component
public class BackendMapeador extends ModelMapper {

	BackendMapeador() {
		// Conversores para Value Objects (IDs) - Integer -> Value Object
		addConverter(new AbstractConverter<Integer, AlunoId>() {
			@Override
			protected AlunoId convert(Integer source) {
				return source != null ? new AlunoId(source) : null;
			}
		});

		addConverter(new AbstractConverter<Integer, ProfessorId>() {
			@Override
			protected ProfessorId convert(Integer source) {
				return source != null ? new ProfessorId(source) : null;
			}
		});

		addConverter(new AbstractConverter<Integer, TurmaId>() {
			@Override
			protected TurmaId convert(Integer source) {
				return source != null ? new TurmaId(source) : null;
			}
		});

		addConverter(new AbstractConverter<Integer, SimuladoId>() {
			@Override
			protected SimuladoId convert(Integer source) {
				return source != null ? new SimuladoId(source) : null;
			}
		});

		addConverter(new AbstractConverter<Integer, DisciplinaId>() {
			@Override
			protected DisciplinaId convert(Integer source) {
				return source != null ? new DisciplinaId(source) : null;
			}
		});

		addConverter(new AbstractConverter<Integer, ResponsavelId>() {
			@Override
			protected ResponsavelId convert(Integer source) {
				return source != null ? new ResponsavelId(source) : null;
			}
		});

		addConverter(new AbstractConverter<Integer, CoordenadorId>() {
			@Override
			protected CoordenadorId convert(Integer source) {
				return source != null ? new CoordenadorId(source) : null;
			}
		});

		addConverter(new AbstractConverter<Integer, RankingId>() {
			@Override
			protected RankingId convert(Integer source) {
				return source != null ? new RankingId(source) : null;
			}
		});

		// Conversores reversos (Value Object -> Integer)
		addConverter(new AbstractConverter<AlunoId, Integer>() {
			@Override
			protected Integer convert(AlunoId source) {
				return source != null ? source.value() : null;
			}
		});

		addConverter(new AbstractConverter<ProfessorId, Integer>() {
			@Override
			protected Integer convert(ProfessorId source) {
				return source != null ? source.value() : null;
			}
		});

		addConverter(new AbstractConverter<TurmaId, Integer>() {
			@Override
			protected Integer convert(TurmaId source) {
				return source != null ? source.value() : null;
			}
		});

		addConverter(new AbstractConverter<SimuladoId, Integer>() {
			@Override
			protected Integer convert(SimuladoId source) {
				return source != null ? source.value() : null;
			}
		});

		addConverter(new AbstractConverter<DisciplinaId, Integer>() {
			@Override
			protected Integer convert(DisciplinaId source) {
				return source != null ? source.value() : null;
			}
		});

		addConverter(new AbstractConverter<ResponsavelId, Integer>() {
			@Override
			protected Integer convert(ResponsavelId source) {
				return source != null ? source.value() : null;
			}
		});

		addConverter(new AbstractConverter<CoordenadorId, Integer>() {
			@Override
			protected Integer convert(CoordenadorId source) {
				return source != null ? source.value() : null;
			}
		});

		addConverter(new AbstractConverter<RankingId, Integer>() {
			@Override
			protected Integer convert(RankingId source) {
				return source != null ? source.value() : null;
			}
		});
	}

	@Override
	public <D> D map(Object source, Class<D> destinationType) {
		return source != null ? super.map(source, destinationType) : null;
	}
}
