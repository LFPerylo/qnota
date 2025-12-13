package dev.com.qnota;

import static org.springframework.boot.SpringApplication.run;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import dev.com.qnota.aplicacao.principal.aluno.AlunoRepositorioAplicacao;
import dev.com.qnota.aplicacao.principal.aluno.AlunoServicoAplicacao;
import dev.com.qnota.aplicacao.principal.coordenador.CoordenadorRepositorioAplicacao;
import dev.com.qnota.aplicacao.principal.coordenador.CoordenadorServicoAplicacao;
import dev.com.qnota.aplicacao.principal.disciplina.DisciplinaRepositorioAplicacao;
import dev.com.qnota.aplicacao.principal.disciplina.DisciplinaServicoAplicacao;
import dev.com.qnota.aplicacao.principal.professor.ProfessorRepositorioAplicacao;
import dev.com.qnota.aplicacao.principal.professor.ProfessorServicoAplicacao;
import dev.com.qnota.aplicacao.principal.ranking.RankingRepositorioAplicacao;
import dev.com.qnota.aplicacao.principal.ranking.RankingServicoAplicacao;
import dev.com.qnota.aplicacao.principal.responsavel.ResponsavelRepositorioAplicacao;
import dev.com.qnota.aplicacao.principal.responsavel.ResponsavelServicoAplicacao;
import dev.com.qnota.aplicacao.principal.simulado.SimuladoRepositorioAplicacao;
import dev.com.qnota.aplicacao.principal.simulado.SimuladoServicoAplicacao;
import dev.com.qnota.aplicacao.principal.turma.TurmaRepositorioAplicacao;
import dev.com.qnota.aplicacao.principal.turma.TurmaServicoAplicacao;
import dev.com.qnota.dominio.principal.aluno.AlunoRepositorio;
import dev.com.qnota.dominio.principal.aluno.AlunoServico;
import dev.com.qnota.dominio.principal.aluno.NotaServico;
import dev.com.qnota.dominio.principal.coordenador.CoordenadorRepositorio;
import dev.com.qnota.dominio.principal.coordenador.CoordenadorServico;
import dev.com.qnota.dominio.principal.coordenador.HashService;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaRepositorio;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaServico;
import dev.com.qnota.dominio.principal.professor.ProfessorRepositorio;
import dev.com.qnota.dominio.principal.professor.ProfessorServico;
import dev.com.qnota.dominio.principal.ranking.CalculoRankingMediaAritmetica;
import dev.com.qnota.dominio.principal.ranking.CalculoRankingMediaPonderada;
import dev.com.qnota.dominio.principal.ranking.RankingRepositorio;
import dev.com.qnota.dominio.principal.ranking.RankingServico;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelRepositorio;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelServico;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelVinculoService;
import dev.com.qnota.dominio.principal.simulado.SimuladoAuditoriaArmazenada;
import dev.com.qnota.dominio.principal.simulado.SimuladoRepositorio;
import dev.com.qnota.dominio.principal.simulado.SimuladoRepositorioDecorator;
import dev.com.qnota.dominio.principal.simulado.SimuladoServico;
import dev.com.qnota.dominio.principal.turma.TurmaRepositorio;
import dev.com.qnota.dominio.principal.turma.TurmaServico;

@SpringBootApplication
public class AplicacaoBackend {

	// ===== HashService (para Coordenador) =====
	@Bean
	public HashService hashService(PasswordEncoder passwordEncoder) {
		return new HashService() {
			@Override
			public String hash(String rawPassword) {
				return passwordEncoder.encode(rawPassword);
			}

			@Override
			public boolean matches(String rawPassword, String hashedPassword) {
				return passwordEncoder.matches(rawPassword, hashedPassword);
			}
		};
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// ===== Serviços de Domínio =====

	@Bean
	public CoordenadorServico coordenadorServico(CoordenadorRepositorio repositorio, HashService hashService) {
		return new CoordenadorServico(repositorio, hashService);
	}

	@Bean
	public ResponsavelServico responsavelServico(ResponsavelRepositorio repositorio, @Qualifier("responsavelVinculoService") ResponsavelVinculoService vinculoService) {
		return new ResponsavelServico(repositorio, vinculoService);
	}

	@Bean
	public ProfessorServico professorServico(ProfessorRepositorio repositorio,
	                                         AlunoRepositorio alunoRepositorio,
	                                         SimuladoRepositorio simuladoRepositorio) {
		return new ProfessorServico(repositorio, alunoRepositorio, simuladoRepositorio);
	}

	@Bean
	public DisciplinaServico disciplinaServico(DisciplinaRepositorio repositorio) {
		return new DisciplinaServico(repositorio);
	}

	@Bean
	public TurmaServico turmaServico(TurmaRepositorio repositorio, ProfessorRepositorio professorRepositorio) {
		return new TurmaServico(repositorio, professorRepositorio);
	}

	@Bean
	public AlunoServico alunoServico(AlunoRepositorio repositorio,
	                                 ResponsavelRepositorio responsavelRepositorio,
	                                 TurmaRepositorio turmaRepositorio,
	                                 SimuladoRepositorio simuladoRepositorio) {
		return new AlunoServico(repositorio, responsavelRepositorio, turmaRepositorio, simuladoRepositorio);
	}

	@Bean
	public NotaServico notaServico(AlunoRepositorio alunoRepositorio,
	                               SimuladoRepositorio simuladoRepositorio,
	                               TurmaRepositorio turmaRepositorio,
	                               DisciplinaRepositorio disciplinaRepositorio) {
		return new NotaServico(alunoRepositorio, simuladoRepositorio, turmaRepositorio, disciplinaRepositorio);
	}

	@Bean
	public CalculoRankingMediaPonderada calculoRankingPonderada(NotaServico notaServico) {
		return new CalculoRankingMediaPonderada(notaServico);
	}

	@Bean
	public CalculoRankingMediaAritmetica calculoRankingAritmetica(NotaServico notaServico) {
		return new CalculoRankingMediaAritmetica(notaServico);
	}

	@Bean
	public RankingServico rankingServico(AlunoRepositorio alunoRepositorio,
	                                     SimuladoRepositorio simuladoRepositorio,
	                                     RankingRepositorio rankingRepositorio,
	                                     CalculoRankingMediaPonderada calculoPonderada,
	                                     CalculoRankingMediaAritmetica calculoAritmetica) {
		return new RankingServico(alunoRepositorio, simuladoRepositorio, rankingRepositorio, calculoPonderada, calculoAritmetica);
	}

	// ===== Auditoria + Decorator para SimuladoRepositorio (Padrao Decorator) =====
	// 
	// O padrao Decorator e aplicado aqui para adicionar comportamento de auditoria
	// ao SimuladoRepositorio sem modificar sua implementacao base.
	//
	// Fluxo:
	// 1. SimuladoServico chama metodos do SimuladoRepositorio (interface)
	// 2. O Decorator intercepta essas chamadas e registra eventos de auditoria
	// 3. O Decorator delega para o repositorio real (JPA)
	// 4. Os eventos ficam disponiveis via /backend/auditoria/eventos
	//
	// Beneficios:
	// - Separacao de responsabilidades (auditoria desacoplada da persistencia)
	// - Open/Closed: adiciona auditoria sem modificar codigo existente
	// - Testabilidade: pode testar com ou sem auditoria

	@Bean
	public SimuladoAuditoriaArmazenada simuladoAuditoria() {
		// Usa a implementacao que armazena eventos em memoria
		// Os eventos sao expostos via AuditoriaControlador
		return new SimuladoAuditoriaArmazenada();
	}

	@Bean
	public SimuladoServico simuladoServico(SimuladoRepositorio repositorio,
	                                       RankingServico rankingServico,
	                                       TurmaRepositorio turmaRepositorio,
	                                       ProfessorRepositorio professorRepositorio,
	                                       DisciplinaRepositorio disciplinaRepositorio,
	                                       AlunoRepositorio alunoRepositorio,
	                                       SimuladoAuditoriaArmazenada simuladoAuditoria) {

		// Envolve o repositorio real com o Decorator para adicionar auditoria
		SimuladoRepositorio decorator = new SimuladoRepositorioDecorator(repositorio, simuladoAuditoria);

		return new SimuladoServico(decorator, rankingServico,
		                           turmaRepositorio, professorRepositorio,
		                           disciplinaRepositorio, alunoRepositorio);
	}

	// ===== ResponsavelVinculoService (implementado por AlunoServico) =====
	@Bean
	public ResponsavelVinculoService responsavelVinculoService(AlunoServico alunoServico) {
		return alunoServico; // AlunoServico implementa ResponsavelVinculoService
	}

	// ===== Serviços de Aplicação =====

	@Bean
	public CoordenadorServicoAplicacao coordenadorServicoAplicacao(CoordenadorRepositorioAplicacao repositorio) {
		return new CoordenadorServicoAplicacao(repositorio);
	}

	@Bean
	public ResponsavelServicoAplicacao responsavelServicoAplicacao(ResponsavelRepositorioAplicacao repositorio) {
		return new ResponsavelServicoAplicacao(repositorio);
	}

	@Bean
	public ProfessorServicoAplicacao professorServicoAplicacao(ProfessorRepositorioAplicacao repositorio) {
		return new ProfessorServicoAplicacao(repositorio);
	}

	@Bean
	public DisciplinaServicoAplicacao disciplinaServicoAplicacao(DisciplinaRepositorioAplicacao repositorio) {
		return new DisciplinaServicoAplicacao(repositorio);
	}

	@Bean
	public TurmaServicoAplicacao turmaServicoAplicacao(TurmaRepositorioAplicacao repositorio) {
		return new TurmaServicoAplicacao(repositorio);
	}

	@Bean
	public AlunoServicoAplicacao alunoServicoAplicacao(AlunoRepositorioAplicacao repositorio) {
		return new AlunoServicoAplicacao(repositorio);
	}

	@Bean
	public SimuladoServicoAplicacao simuladoServicoAplicacao(SimuladoRepositorioAplicacao repositorio) {
		return new SimuladoServicoAplicacao(repositorio);
	}

	@Bean
	public RankingServicoAplicacao rankingServicoAplicacao(RankingRepositorioAplicacao repositorio) {
		return new RankingServicoAplicacao(repositorio);
	}

	public static void main(String[] args) {
		run(AplicacaoBackend.class, args);
	}
}
