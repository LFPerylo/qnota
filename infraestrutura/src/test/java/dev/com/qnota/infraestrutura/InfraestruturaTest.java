package dev.com.qnota.infraestrutura;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import dev.com.qnota.dominio.principal.aluno.AlunoRepositorio;
import dev.com.qnota.dominio.principal.coordenador.CoordenadorRepositorio;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaRepositorio;
import dev.com.qnota.dominio.principal.professor.ProfessorRepositorio;
import dev.com.qnota.dominio.principal.ranking.RankingRepositorio;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelRepositorio;
import dev.com.qnota.dominio.principal.simulado.SimuladoRepositorio;
import dev.com.qnota.dominio.principal.turma.TurmaRepositorio;

/**
 * Teste de integração para verificar se a infraestrutura está funcionando.
 * 
 * COMO USAR:
 * 1. Execute: mvn test -Dtest=InfraestruturaTest -pl infraestrutura
 * 2. Ou execute a aplicação: mvn spring-boot:run -pl apresentacao-backend
 * 
 * Este teste verifica se:
 * 1. O Spring Boot consegue carregar o contexto
 * 2. Os repositórios JPA estão sendo injetados corretamente
 * 3. A conexão com o banco de dados está funcionando
 */
@SpringBootTest(classes = TestConfig.class)
@TestPropertySource(locations = "classpath:application-test.properties")
class InfraestruturaTest {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired(required = false)
	private AlunoRepositorio alunoRepositorio;

	@Autowired(required = false)
	private CoordenadorRepositorio coordenadorRepositorio;

	@Autowired(required = false)
	private DisciplinaRepositorio disciplinaRepositorio;

	@Autowired(required = false)
	private ProfessorRepositorio professorRepositorio;

	@Autowired(required = false)
	private RankingRepositorio rankingRepositorio;

	@Autowired(required = false)
	private ResponsavelRepositorio responsavelRepositorio;

	@Autowired(required = false)
	private SimuladoRepositorio simuladoRepositorio;

	@Autowired(required = false)
	private TurmaRepositorio turmaRepositorio;

	@Test
	void contextoSpringDeveEstarCarregado() {
		assertNotNull(applicationContext, "O contexto do Spring deve estar carregado");
		assertTrue(applicationContext.getBeanDefinitionCount() > 0, 
			"Deve haver pelo menos um bean no contexto");
	}

	@Test
	void repositoriosDevemEstarDisponiveis() {
		// Verifica se os repositórios estão sendo injetados
		// Se algum estiver null, significa que não foi encontrado pelo Spring
		assertNotNull(alunoRepositorio, "AlunoRepositorio deve estar disponível");
		assertNotNull(coordenadorRepositorio, "CoordenadorRepositorio deve estar disponível");
		assertNotNull(disciplinaRepositorio, "DisciplinaRepositorio deve estar disponível");
		assertNotNull(professorRepositorio, "ProfessorRepositorio deve estar disponível");
		assertNotNull(rankingRepositorio, "RankingRepositorio deve estar disponível");
		assertNotNull(responsavelRepositorio, "ResponsavelRepositorio deve estar disponível");
		assertNotNull(simuladoRepositorio, "SimuladoRepositorio deve estar disponível");
		assertNotNull(turmaRepositorio, "TurmaRepositorio deve estar disponível");
	}

	@Test
	void verificarBeansDeRepositorio() {
		// Verifica se os beans estão registrados no contexto
		String[] beanNames = applicationContext.getBeanNamesForType(AlunoRepositorio.class);
		assertTrue(beanNames.length > 0, "Deve haver pelo menos uma implementação de AlunoRepositorio");
		
		beanNames = applicationContext.getBeanNamesForType(ProfessorRepositorio.class);
		assertTrue(beanNames.length > 0, "Deve haver pelo menos uma implementação de ProfessorRepositorio");
	}
}

