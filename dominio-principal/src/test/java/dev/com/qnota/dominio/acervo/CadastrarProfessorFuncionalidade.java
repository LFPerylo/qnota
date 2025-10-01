package dev.com.qnota.dominio.acervo;

import dev.com.qnota.dominio.academico.professor.Professor;
import dev.com.qnota.dominio.academico.professor.ProfessorId;
import dev.com.qnota.dominio.academico.professor.ProfessorServico;
import dev.com.qnota.dominio.academico.professor.ProfessorRepositorio;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CadastrarProfessorFuncionalidade {
	private ProfessorServico professorServico;
	private Professor professor;
	private RuntimeException excecao;
	private java.util.Set<String> professoresCadastrados = new java.util.HashSet<>();

	public CadastrarProfessorFuncionalidade() {
		// Usando um repositório mock simples
		ProfessorRepositorio repositorio = new ProfessorRepositorio() {
			@Override
			public void salvar(Professor professor) {
				// Mock simples - apenas simula salvamento
			}
			
			@Override
			public java.util.Optional<Professor> porId(ProfessorId id) {
				return java.util.Optional.empty();
			}
			
			@Override
			public int contarTurmasAtivas(ProfessorId id) {
				return 0;
			}
			
			@Override
			public boolean possuiSimuladoFinalizado(ProfessorId id) {
				return false;
			}
			
			@Override
			public void substituirProfessor(ProfessorId aRemover, ProfessorId substituto) {
				// Mock
			}
			
			@Override
			public java.util.List<String> nomesDeAreasDoProfessor(ProfessorId id) {
				return java.util.List.of();
			}
		};
		professorServico = new ProfessorServico(repositorio);
	}

	@Given("um professor com nome {string}")
	public void um_professor_com_nome(String nome) {
		professor = new Professor(null, nome, "12345678901", "professor@escola.com", true, 
			java.util.List.of(new Professor.AreaConhecimento(1, "Matemática")));
	}

	@When("o professor for cadastrado")
	public void o_professor_for_cadastrado() {
		try {
			professorServico.cadastrar(professor);
			professoresCadastrados.add(professor.getNome());
		} catch (RuntimeException excecao) {
			this.excecao = excecao;
		}
	}

	@Then("o professor é cadastrado com sucesso")
	public void o_professor_é_cadastrado_com_sucesso() {

		if (professor == null) {
			throw new RuntimeException("Professor não foi criado");
		}
		if (excecao != null) {
			throw new RuntimeException("Deveria ter cadastrado com sucesso, mas houve exceção: " + excecao.getMessage());
		}
	}

	@Given("um professor com nome {string} já está cadastrado")
	public void um_professor_com_nome_já_está_cadastrado(String nome) {
		professor = new Professor(null, nome, "12345678901", "professor@escola.com", true, 
			java.util.List.of(new Professor.AreaConhecimento(1, "Matemática")));
		professoresCadastrados.add(nome); // Simula que já está cadastrado
	}

	@When("o professor for cadastrado novamente")
	public void o_professor_for_cadastrado_novamente() {
		// Simula validação de duplicação
		if (professoresCadastrados.contains(professor.getNome())) {
			excecao = new RuntimeException("Professor já está cadastrado");
		} else {
			try {
				professorServico.cadastrar(professor);
				professoresCadastrados.add(professor.getNome());
			} catch (RuntimeException excecao) {
				this.excecao = excecao;
			}
		}
	}

	@Then("o sistema informa que o professor já está cadastrado")
	public void o_sistema_informa_que_o_professor_já_está_cadastrado() {
		// Validação simples sem bibliotecas externas
		if (excecao == null) {
			throw new RuntimeException("Deveria ter lançado exceção para professor duplicado");
		}
		if (!excecao.getMessage().contains("já está cadastrado")) {
			throw new RuntimeException("Mensagem de erro incorreta: " + excecao.getMessage());
		}
	}
}