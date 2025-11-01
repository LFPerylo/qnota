package dev.com.qnota.infraestrutura.persistencia.jpa;

import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration.AccessLevel;
import org.springframework.stereotype.Component;

import dev.com.qnota.dominio.principal.aluno.Aluno;
import dev.com.qnota.dominio.principal.aluno.AlunoId;
import dev.com.qnota.dominio.principal.coordenador.Coordenador;
import dev.com.qnota.dominio.principal.coordenador.CoordenadorId;
import dev.com.qnota.dominio.principal.disciplina.Disciplina.AreaConhecimento;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.dominio.principal.professor.Professor;
import dev.com.qnota.dominio.principal.professor.ProfessorId;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;
import dev.com.qnota.dominio.principal.turma.TurmaId;

@Component
class JpaMapeador extends ModelMapper {

    JpaMapeador() {
        var cfg = getConfiguration();
        cfg.setFieldMatchingEnabled(true);
        cfg.setFieldAccessLevel(AccessLevel.PRIVATE);

        // ----- Converters Coordenador -----

        // Domínio -> JPA
		addConverter(new AbstractConverter<Coordenador, CoordenadorJpa>() {
		@Override
		protected CoordenadorJpa convert(Coordenador src) {
			if (src == null) return null;
			var j = new CoordenadorJpa();
			j.id                 = (src.getId() != null) ? src.getId().value() : null; // <-- value()
			j.nome               = src.getNome();
			j.enderecoEletronico = src.getEmail();
			j.senhaHash          = src.getSenhaHash();
			j.ativo              = src.isAtivo();
			return j;
		}
		});

		// JPA -> Domínio
		addConverter(new AbstractConverter<CoordenadorJpa, Coordenador>() {
		@Override
		protected Coordenador convert(CoordenadorJpa src) {
			if (src == null) return null;
			var c = new Coordenador(
				src.nome,
				src.enderecoEletronico,
				src.senhaHash,
				src.ativo != null ? src.ativo : Boolean.TRUE
			);
			if (src.id != null) {
			c.atribuirIdSeAusente(new CoordenadorId(src.id)); // <-- constrói VO
			}
			return c;
		}
		});

		// Integer -> CoordenadorId
		addConverter(new AbstractConverter<Integer, CoordenadorId>() {
		@Override
		protected CoordenadorId convert(Integer src) {
			return (src == null) ? null : new CoordenadorId(src);
		}
		});

		// No seu JpaMapeador (o mesmo arquivo onde você configurou coordenador)
		addConverter(new org.modelmapper.AbstractConverter<Integer, dev.com.qnota.dominio.principal.aluno.AlunoId>() {
		@Override
		protected dev.com.qnota.dominio.principal.aluno.AlunoId convert(Integer source) {
			return source == null ? null : new dev.com.qnota.dominio.principal.aluno.AlunoId(source);
		}
		});

		addConverter(new org.modelmapper.AbstractConverter<Integer, dev.com.qnota.dominio.principal.simulado.SimuladoId>() {
		@Override
		protected dev.com.qnota.dominio.principal.simulado.SimuladoId convert(Integer source) {
			return source == null ? null : new dev.com.qnota.dominio.principal.simulado.SimuladoId(source);
		}
		});

		// DisciplinaId
		addConverter(new org.modelmapper.AbstractConverter<Integer, dev.com.qnota.dominio.principal.disciplina.DisciplinaId>() {
		@Override protected dev.com.qnota.dominio.principal.disciplina.DisciplinaId convert(Integer source) {
			return source == null ? null : new dev.com.qnota.dominio.principal.disciplina.DisciplinaId(source);
		}
		});

		// AreaConhecimento ⇄ AreaConhecimentoJpa (se preferir via mapper)
		addConverter(new org.modelmapper.AbstractConverter<AreaConhecimentoJpa, AreaConhecimento>() {
		@Override protected AreaConhecimento convert(AreaConhecimentoJpa source) {
			return source == null ? null : new AreaConhecimento(source.id, source.nome);
		}
		});

				// ProfessorJpa -> Professor (domínio)
		addConverter(new AbstractConverter<ProfessorJpa, Professor>() {
			@Override
			protected Professor convert(ProfessorJpa src) {
				var p = new Professor(
					src.nome,
					src.cpf,
					src.email,
					new java.util.ArrayList<>(src.especialidades)
				);
				p.atribuirIdSeAusente(new ProfessorId(src.id));
				return p;
			}
		});

		// Professor (domínio) -> ProfessorJpa
		addConverter(new AbstractConverter<Professor, ProfessorJpa>() {
			@Override
			protected ProfessorJpa convert(Professor src) {
				var j = new ProfessorJpa();
				if (src.getId() != null) j.id = src.getId().value();
				j.nome = src.getNome();
				j.cpf = src.getCpf();
				j.email = src.getEmail();
				j.especialidades = new java.util.ArrayList<>(src.getEspecialidades());
				return j;
			}
		});

		// Integer -> ProfessorId
		addConverter(new AbstractConverter<Integer, ProfessorId>() {
			@Override
			protected ProfessorId convert(Integer src) {
				return new ProfessorId(src);
			}
		});

		// ProfessorId -> Integer
		addConverter(new AbstractConverter<ProfessorId, Integer>() {
			@Override
			protected Integer convert(ProfessorId src) {
				return src != null ? src.value() : null;
			}
		});

				// IDs
		addConverter(new org.modelmapper.AbstractConverter<Integer, AlunoId>() {
		@Override protected AlunoId convert(Integer s){ return s == null ? null : new AlunoId(s); }
		});
		addConverter(new org.modelmapper.AbstractConverter<Integer, TurmaId>() {
		@Override protected TurmaId convert(Integer s){ return s == null ? null : new TurmaId(s); }
		});
		addConverter(new org.modelmapper.AbstractConverter<Integer, SimuladoId>() {
		@Override protected SimuladoId convert(Integer s){ return s == null ? null : new SimuladoId(s); }
		});
		addConverter(new org.modelmapper.AbstractConverter<Integer, DisciplinaId>() {
		@Override protected DisciplinaId convert(Integer s){ return s == null ? null : new DisciplinaId(s); }
		});
		addConverter(new org.modelmapper.AbstractConverter<Integer, dev.com.qnota.dominio.principal.responsavel.ResponsavelId>() {
		@Override protected dev.com.qnota.dominio.principal.responsavel.ResponsavelId convert(Integer s){
			return s == null ? null : new dev.com.qnota.dominio.principal.responsavel.ResponsavelId(s);
		}
		});

		// JPA -> Domínio (Aluno)
		addConverter(new org.modelmapper.AbstractConverter<AlunoJpa, Aluno>() {
		@Override protected Aluno convert(AlunoJpa src){
			if (src == null) return null;

			// vínculos
			java.util.List<dev.com.qnota.dominio.principal.responsavel.ResponsavelId> rs = new java.util.ArrayList<>();
			dev.com.qnota.dominio.principal.responsavel.ResponsavelId principal = null;
			if (src.responsaveis != null){
			for (var v: src.responsaveis){
				var rid = map(v.id.responsavelId, dev.com.qnota.dominio.principal.responsavel.ResponsavelId.class);
				rs.add(rid);
				if (v.principal) principal = rid;
			}
			}

			var aluno = new Aluno(
				map(src.id, AlunoId.class),
				src.nome,
				src.dataNascimento,
				src.ativo,
				map(src.turmaId, TurmaId.class),
				rs,
				principal
			);

			// Hidratar notas (opcional; exige método de apoio no domínio — aqui via reflexão como fallback)
			if (src.notas != null){
			try {
				var addNota = Aluno.class.getDeclaredMethod("adicionarNotaInterna", SimuladoId.class, DisciplinaId.class, double.class);
				addNota.setAccessible(true);

				var addJust = Aluno.class.getDeclaredMethod("adicionarJustificativaInterna",
								SimuladoId.class, DisciplinaId.class, dev.com.qnota.dominio.principal.aluno.Justificativa.class);
				addJust.setAccessible(true);

				for (var n : src.notas){
				var sim = map(n.id.simuladoId, SimuladoId.class);
				var dis = map(n.id.disciplinaId, DisciplinaId.class);
				addNota.invoke(aluno, sim, dis, n.valor);

				if (n.justificativas != null){
					for (var j : n.justificativas){
					var jj = new dev.com.qnota.dominio.principal.aluno.Justificativa(
						j.notaAnterior, j.notaCorrigida, j.texto, j.dataHora,
						new dev.com.qnota.dominio.principal.professor.ProfessorId(j.professorId)
					);
					addJust.invoke(aluno, sim, dis, jj);
					}
				}
				}
			} catch (Exception ignore) { /* se não quiser reflexão, exponha método público no domínio */ }
			}
			return aluno;
		}
		});

		// Domínio -> JPA (Aluno)
		addConverter(new org.modelmapper.AbstractConverter<Aluno, AlunoJpa>() {
		@Override protected AlunoJpa convert(Aluno src){
			if (src == null) return null;
			var j = new AlunoJpa();
			j.id             = (src.getId() != null ? src.getId().value() : null);
			j.nome           = src.getNome();
			j.dataNascimento = src.getDataNascimento();
			j.ativo          = src.isAtivo();
			j.turmaId        = (src.getTurma() != null ? src.getTurma().value() : null);

			// vínculos
			j.responsaveis = new java.util.LinkedHashSet<>();
			for (var v : src.getVinculos()){
			var x = new AlunoResponsavelJpa();
			x.aluno = j;
			var id = new AlunoRespIdJpa();
			id.alunoId = j.id; // MapsId ajusta quando null em insert
			id.responsavelId = v.responsavel().value();
			x.id = id;
			x.principal = v.principal();
			j.responsaveis.add(x);
			}

			// notas
			j.notas = new java.util.LinkedHashSet<>();
			for (var n : src.getNotas()){
			var nj = new NotaAlunoJpa();
			nj.aluno = j;
			var nid = new NotaIdJpa();
			nid.alunoId = j.id;
			nid.simuladoId = n.getSimuladoId().value();
			nid.disciplinaId = n.getDisciplinaId().value();
			nj.id = nid;
			nj.valor = n.getValor();
			nj.dataLancamento = n.getDataLancamento();

			nj.justificativas = new java.util.LinkedHashSet<>();
			if (n.getJustificativas() != null){
				for (var jj : n.getJustificativas()){
				var jpaJ = new JustificativaJpa();
				jpaJ.nota = nj;
				jpaJ.professorId = jj.getProfessor().value();
				jpaJ.notaAnterior = jj.getNotaAnterior();
				jpaJ.notaCorrigida = jj.getNotaCorrigida();
				jpaJ.texto = jj.getTexto();
				jpaJ.dataHora = jj.getDataHora();
				nj.justificativas.add(jpaJ);
				}
			}
			j.notas.add(nj);
			}
			return j;
		}
		});


	}

    @Override
    public <D> D map(Object source, Class<D> destinationType) {
        return source != null ? super.map(source, destinationType) : null;
    }
}
