package dev.com.qnota.apresentacao.principal.nota;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("backend/nota")
class NotaControlador {

	private final JdbcTemplate jdbcTemplate;

	NotaControlador(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	record NotaDto(
		int simuladoId,
		int alunoId,
		String alunoNome,
		int disciplinaId,
		String disciplinaNome,
		double valor,
		LocalDateTime dataLancamento
	) {}

	@GetMapping("pesquisa")
	List<NotaDto> listarTodas() {
		return consultar(null);
	}

	@GetMapping("simulado/{id}")
	List<NotaDto> listarPorSimulado(@PathVariable("id") int id) {
		return consultar(id);
	}

	private List<NotaDto> consultar(Integer simuladoId) {
		StringBuilder sql = new StringBuilder("""
			SELECT
				n.simulado_id,
				n.aluno_id,
				a.nome AS aluno_nome,
				n.disciplina_id,
				d.nome AS disciplina_nome,
				n.valor,
				n.datalancamento
			FROM notas_do_aluno n
			LEFT JOIN alunos a ON a.id = n.aluno_id
			LEFT JOIN disciplinas d ON d.id = n.disciplina_id
		""");

		List<Object> params = new ArrayList<>();
		if (simuladoId != null) {
			sql.append(" WHERE n.simulado_id = ?");
			params.add(simuladoId);
		}
		sql.append(" ORDER BY n.simulado_id, a.nome, d.nome");

		return jdbcTemplate.query(
			sql.toString(),
			params.toArray(),
			(rs, rowNum) -> new NotaDto(
				rs.getInt("simulado_id"),
				rs.getInt("aluno_id"),
				rs.getString("aluno_nome"),
				rs.getInt("disciplina_id"),
				rs.getString("disciplina_nome"),
				rs.getDouble("valor"),
				rs.getTimestamp("datalancamento").toLocalDateTime()
			)
		);
	}
}

