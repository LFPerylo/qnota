const API_BASE_URL = (import.meta as any).env?.VITE_API_BASE_URL || 'http://localhost:8080';

async function fetchAPI<T>(endpoint: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}/${endpoint}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
  });

  if (!response.ok) {
    let errorMessage = `API Error: ${response.statusText}`;
    try {
      const errorBody = await response.text();
      if (errorBody) {
        try {
          // Tenta parsear como JSON
          const errorJson = JSON.parse(errorBody);
          if (errorJson.message) {
            errorMessage = errorJson.message;
          } else if (errorJson.error) {
            errorMessage = errorJson.error;
          } else {
            errorMessage = errorBody;
          }
        } catch {
          // Se não for JSON, usa o texto direto
          errorMessage = errorBody;
        }
      }
    } catch (e) {
      // Ignore parsing errors
    }
    const error = new Error(errorMessage);
    (error as any).status = response.status;
    throw error;
  }

  // Handle empty responses
  const text = await response.text();
  if (!text) {
    return undefined as T;
  }
  
  return JSON.parse(text);
}

// Alunos
export const alunoAPI = {
  pesquisar: () => fetchAPI<any[]>('backend/aluno/pesquisa'),
  cadastrar: (dto: any) => fetchAPI<number>('backend/aluno/cadastrar', {
    method: 'POST',
    body: JSON.stringify(dto),
  }),
  transferir: (id: number, novaTurmaId: number) => fetchAPI<void>(`backend/aluno/${id}/transferir`, {
    method: 'POST',
    body: JSON.stringify(novaTurmaId),
  }),
  inativar: (id: number) => fetchAPI<void>(`backend/aluno/${id}/inativar`, {
    method: 'POST',
  }),
  excluir: (id: number) => fetchAPI<void>(`backend/aluno/${id}/excluir`, {
    method: 'POST',
  }),
  vincularResponsavel: (id: number, dto: any) => fetchAPI<void>(`backend/aluno/${id}/vincular-responsavel`, {
    method: 'POST',
    body: JSON.stringify(dto),
  }),
  desvincularResponsavel: (id: number, responsavelId: number) => fetchAPI<void>(`backend/aluno/${id}/desvincular-responsavel`, {
    method: 'POST',
    body: JSON.stringify(responsavelId),
  }),
  definirPrincipal: (id: number, responsavelId: number) => fetchAPI<void>(`backend/aluno/${id}/definir-responsavel-principal`, {
    method: 'POST',
    body: JSON.stringify(responsavelId),
  }),
  lancarNota: (id: number, dto: any) => fetchAPI<void>(`backend/aluno/${id}/lancar-nota`, {
    method: 'POST',
    body: JSON.stringify(dto),
  }),
  retificarNota: (id: number, dto: any) => fetchAPI<void>(`backend/aluno/${id}/retificar-nota`, {
    method: 'POST',
    body: JSON.stringify(dto),
  }),
};

// Turmas
export const turmaAPI = {
  pesquisar: () => fetchAPI<any[]>('backend/turma/pesquisa'),
  criar: (dto: any) => fetchAPI<number>('backend/turma/criar', {
    method: 'POST',
    body: JSON.stringify(dto),
  }),
  renomear: (id: number, novoNome: string) => fetchAPI<void>(`backend/turma/${id}/renomear`, {
    method: 'POST',
    body: JSON.stringify(novoNome),
  }),
  trocarProfessor: (id: number, novoProfessorId: number) => fetchAPI<void>(`backend/turma/${id}/trocar-professor`, {
    method: 'POST',
    body: JSON.stringify(novoProfessorId),
  }),
  inativar: (id: number) => fetchAPI<void>(`backend/turma/${id}/inativar`, {
    method: 'POST',
  }),
  excluir: (id: number) => fetchAPI<void>(`backend/turma/${id}/excluir`, {
    method: 'POST',
  }),
};

// Professores
export const professorAPI = {
  pesquisar: () => fetchAPI<any[]>('backend/professor/pesquisa'),
  cadastrar: (dto: any) => fetchAPI<number>('backend/professor/cadastrar', {
    method: 'POST',
    body: JSON.stringify(dto),
  }),
  atualizarContato: (id: number, dto: any) => fetchAPI<void>(`backend/professor/${id}/atualizar-contato`, {
    method: 'POST',
    body: JSON.stringify(dto),
  }),
  adicionarEspecialidade: (id: number, area: string) => fetchAPI<void>(`backend/professor/${id}/adicionar-especialidade`, {
    method: 'POST',
    body: JSON.stringify(area),
  }),
  removerEspecialidade: (id: number, area: string) => fetchAPI<void>(`backend/professor/${id}/remover-especialidade`, {
    method: 'POST',
    body: JSON.stringify(area),
  }),
  removerComSubstituto: (id: number, substitutoId: number) => fetchAPI<void>(`backend/professor/${id}/remover-com-substituto`, {
    method: 'POST',
    body: JSON.stringify(substitutoId),
  }),
};

// Disciplinas
export const disciplinaAPI = {
  pesquisar: () => fetchAPI<any[]>('backend/disciplina/pesquisa'),
  cadastrar: (dto: any) => fetchAPI<number>('backend/disciplina/cadastrar', {
    method: 'POST',
    body: JSON.stringify(dto),
  }),
  editar: (id: number, dto: any) => fetchAPI<void>(`backend/disciplina/${id}/editar`, {
    method: 'POST',
    body: JSON.stringify(dto),
  }),
  ativar: (id: number) => fetchAPI<void>(`backend/disciplina/${id}/ativar`, {
    method: 'POST',
  }),
  inativar: (id: number) => fetchAPI<void>(`backend/disciplina/${id}/inativar`, {
    method: 'POST',
  }),
  excluir: (id: number) => fetchAPI<void>(`backend/disciplina/${id}/excluir`, {
    method: 'POST',
  }),
};

// Responsáveis
export const responsavelAPI = {
  pesquisar: () => fetchAPI<any[]>('backend/responsavel/pesquisa'),
  cadastrar: (dto: any) => fetchAPI<number>('backend/responsavel/cadastrar', {
    method: 'POST',
    body: JSON.stringify(dto),
  }),
  atualizarContato: (id: number, dto: any) => fetchAPI<void>(`backend/responsavel/${id}/atualizar-contato`, {
    method: 'POST',
    body: JSON.stringify(dto),
  }),
  marcarInadimplente: (id: number) => fetchAPI<void>(`backend/responsavel/${id}/marcar-inadimplente`, {
    method: 'POST',
  }),
  regularizar: (id: number) => fetchAPI<void>(`backend/responsavel/${id}/regularizar`, {
    method: 'POST',
  }),
  inativar: (id: number) => fetchAPI<void>(`backend/responsavel/${id}/inativar`, {
    method: 'POST',
  }),
  excluir: (id: number) => fetchAPI<void>(`backend/responsavel/${id}/excluir`, {
    method: 'POST',
  }),
};

// Simulados
export const simuladoAPI = {
  pesquisar: (expandir = false) => fetchAPI<any[]>(`backend/simulado/pesquisa?expandir=${expandir}`),
  detalhar: (id: number) => fetchAPI<any>(`backend/simulado/${id}`),
  criar: (dto: any) => fetchAPI<number>('backend/simulado/criar', {
    method: 'POST',
    body: JSON.stringify(dto),
  }),
  editarDisciplinas: (id: number, dto: any) => fetchAPI<void>(`backend/simulado/${id}/editar-disciplinas`, {
    method: 'POST',
    body: JSON.stringify(dto),
  }),
  finalizar: (id: number) => fetchAPI<void>(`backend/simulado/${id}/finalizar`, {
    method: 'POST',
  }),
  excluir: (id: number) => fetchAPI<void>(`backend/simulado/${id}/excluir`, {
    method: 'POST',
  }),
};

// Ranking
export const rankingAPI = {
  pesquisar: (expandir = false) => fetchAPI<any[]>(`backend/ranking/pesquisa?expandir=${expandir}`),
  recalcular: (simuladoId: number) => fetchAPI<any[]>(`backend/ranking/simulado/${simuladoId}/recalcular`, {
    method: 'POST',
  }),
  congelar: (simuladoId: number) => fetchAPI<void>(`backend/ranking/simulado/${simuladoId}/congelar`, {
    method: 'POST',
  }),
};

// Notas
export const notaAPI = {
  pesquisar: () => fetchAPI<any[]>('backend/nota/pesquisa'),
  porSimulado: (id: number) => fetchAPI<any[]>(`backend/nota/simulado/${id}`),
};

