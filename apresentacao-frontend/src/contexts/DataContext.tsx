import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import * as api from '../services/api';

interface DataContextType {
  // Dados
  alunos: any[];
  turmas: any[];
  professores: any[];
  disciplinas: any[];
  responsaveis: any[];
  simulados: any[];
  notas: any[];
  rankings: any[];

  // Loading states
  loading: boolean;
  error: string | null;

  // Funções de atualização
  refreshAlunos: () => Promise<void>;
  refreshTurmas: () => Promise<void>;
  refreshProfessores: () => Promise<void>;
  refreshDisciplinas: () => Promise<void>;
  refreshResponsaveis: () => Promise<void>;
  refreshSimulados: () => Promise<void>;
  refreshRankings: () => Promise<void>;
  refreshAll: () => Promise<void>;

  // Funções de manipulação
  addAluno: (dto: any) => Promise<void>;
  updateAluno: (id: number, dto: any) => Promise<void>;
  deleteAluno: (id: number) => Promise<void>;
  inativarAluno: (id: number) => Promise<void>;

  addTurma: (dto: any) => Promise<void>;
  updateTurma: (id: number, dto: any) => Promise<void>;
  deleteTurma: (id: number) => Promise<void>;
  inativarTurma: (id: number) => Promise<void>;

  addProfessor: (dto: any) => Promise<void>;
  updateProfessor: (id: number, dto: any) => Promise<void>;
  deleteProfessor: (id: number, substitutoId: number) => Promise<void>;

  addDisciplina: (dto: any) => Promise<void>;
  updateDisciplina: (id: number, dto: any) => Promise<void>;
  deleteDisciplina: (id: number) => Promise<void>;

  addResponsavel: (dto: any) => Promise<void>;
  updateResponsavel: (id: number, dto: any) => Promise<void>;
  deleteResponsavel: (id: number) => Promise<void>;

  addSimulado: (dto: any) => Promise<void>;
  updateSimulado: (id: number, dto: any) => Promise<void>;
  deleteSimulado: (id: number) => Promise<void>;
  finalizarSimulado: (id: number) => Promise<void>;

  lancarNota: (alunoId: number, dto: any) => Promise<void>;
  retificarNota: (alunoId: number, dto: any) => Promise<void>;
  calcularMedia: (alunoId: string, simuladoId: string) => number;
  getSimuladoDetalhe: (id: number) => Promise<any>;
}

const DataContext = createContext<DataContextType | undefined>(undefined);

export function DataProvider({ children }: { children: ReactNode }) {
  const [alunos, setAlunos] = useState<any[]>([]);
  const [turmas, setTurmas] = useState<any[]>([]);
  const [professores, setProfessores] = useState<any[]>([]);
  const [disciplinas, setDisciplinas] = useState<any[]>([]);
  const [responsaveis, setResponsaveis] = useState<any[]>([]);
  const [simulados, setSimulados] = useState<any[]>([]);
  const [notas, setNotas] = useState<any[]>([]);
  const [rankings, setRankings] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const refreshAlunos = async () => {
    try {
      const data = await api.alunoAPI.pesquisar();
      setAlunos(data);
    } catch (err: any) {
      setError(err.message);
    }
  };

  const refreshTurmas = async () => {
    try {
      const data = await api.turmaAPI.pesquisar();
      setTurmas(data);
    } catch (err: any) {
      setError(err.message);
    }
  };

  const refreshProfessores = async () => {
    try {
      const data = await api.professorAPI.pesquisar();
      setProfessores(data);
    } catch (err: any) {
      setError(err.message);
    }
  };

  const refreshDisciplinas = async () => {
    try {
      const data = await api.disciplinaAPI.pesquisar();
      setDisciplinas(data);
    } catch (err: any) {
      setError(err.message);
    }
  };

  const refreshResponsaveis = async () => {
    try {
      const data = await api.responsavelAPI.pesquisar();
      setResponsaveis(data);
    } catch (err: any) {
      setError(err.message);
    }
  };

  const refreshSimulados = async () => {
    try {
      const data = await api.simuladoAPI.pesquisar(true);
      const detalhados = await Promise.all(
        data.map(async (sim: any) => {
          try {
            const detalhe = await api.simuladoAPI.detalhar(sim.id);
            return {
              ...sim,
              disciplinas: (detalhe.disciplinas || []).map((d: any) => ({
                disciplinaId: (d.disciplinaId ?? d.id)?.toString() || '',
                peso: d.peso ?? 0
              }))
            };
          } catch {
            return { ...sim, disciplinas: [] };
          }
        })
      );
      setSimulados(detalhados);
    } catch (err: any) {
      setError(err.message);
    }
  };

  const refreshNotas = async () => {
    try {
      const data = await api.notaAPI.pesquisar();
      setNotas(data.map((n: any) => ({
        ...n,
        simuladoId: n.simuladoId?.toString() || '',
        alunoId: n.alunoId?.toString() || '',
        disciplinaId: n.disciplinaId?.toString() || ''
      })));
    } catch (err: any) {
      setError(err.message);
    }
  };

  const refreshRankings = async () => {
    try {
      const data = await api.rankingAPI.pesquisar(true);
      setRankings(data);
    } catch (err: any) {
      setError(err.message);
    }
  };

  const refreshAll = async () => {
    setLoading(true);
    setError(null);
    try {
      await Promise.all([
        refreshAlunos(),
        refreshTurmas(),
        refreshProfessores(),
        refreshDisciplinas(),
        refreshResponsaveis(),
        refreshSimulados(),
        refreshNotas(),
        refreshRankings(),
      ]);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  // Funções de manipulação
  const addAluno = async (dto: any) => {
    await api.alunoAPI.cadastrar(dto);
    await refreshAlunos();
  };

  const updateAluno = async (id: number, dto: any) => {
    if (dto.nome !== undefined) {
      await api.alunoAPI.renomear(id, dto.nome);
    }
    if (dto.turmaId !== undefined) {
      await api.alunoAPI.transferir(id, dto.turmaId);
    }
    await refreshAlunos();
  };

  const deleteAluno = async (id: number) => {
    await api.alunoAPI.excluir(id);
    await refreshAlunos();
  };

  const inativarAluno = async (id: number) => {
    await api.alunoAPI.inativar(id);
    await refreshAlunos();
  };

  const addTurma = async (dto: any) => {
    await api.turmaAPI.criar(dto);
    await refreshTurmas();
  };

  const updateTurma = async (id: number, dto: any) => {
    if (dto.nome !== undefined) {
      await api.turmaAPI.renomear(id, dto.nome);
    }
    if (dto.professorId !== undefined) {
      await api.turmaAPI.trocarProfessor(id, dto.professorId);
    }
    await refreshTurmas();
  };

  const deleteTurma = async (id: number) => {
    await api.turmaAPI.excluir(id);
    await refreshTurmas();
  };

  const inativarTurma = async (id: number) => {
    await api.turmaAPI.inativar(id);
    await refreshTurmas();
  };

  const addProfessor = async (dto: any) => {
    await api.professorAPI.cadastrar(dto);
    await refreshProfessores();
  };

  const updateProfessor = async (id: number, dto: any) => {
    await api.professorAPI.atualizarContato(id, dto);
    await refreshProfessores();
  };

  const deleteProfessor = async (id: number, substitutoId: number) => {
    await api.professorAPI.removerComSubstituto(id, substitutoId);
    await Promise.all([refreshProfessores(), refreshTurmas()]);
  };

  const addDisciplina = async (dto: any) => {
    await api.disciplinaAPI.cadastrar(dto);
    await refreshDisciplinas();
  };

  const updateDisciplina = async (id: number, dto: any) => {
    await api.disciplinaAPI.editar(id, dto);
    await refreshDisciplinas();
  };

  const deleteDisciplina = async (id: number) => {
    await api.disciplinaAPI.excluir(id);
    await refreshDisciplinas();
  };

  const addResponsavel = async (dto: any) => {
    await api.responsavelAPI.cadastrar(dto);
    await refreshResponsaveis();
  };

  const updateResponsavel = async (id: number, dto: any) => {
    await api.responsavelAPI.atualizarContato(id, dto);
    if (dto.inadimplente) {
      await api.responsavelAPI.marcarInadimplente(id);
    } else {
      await api.responsavelAPI.regularizar(id);
    }
    await refreshResponsaveis();
  };

  const deleteResponsavel = async (id: number) => {
    await api.responsavelAPI.excluir(id);
    await refreshResponsaveis();
  };

  const addSimulado = async (dto: any) => {
    await api.simuladoAPI.criar(dto);
    await refreshSimulados();
  };

  const updateSimulado = async (id: number, dto: any) => {
    await api.simuladoAPI.editarDisciplinas(id, dto);
    await refreshSimulados();
  };

  const deleteSimulado = async (id: number) => {
    await api.simuladoAPI.excluir(id);
    await Promise.all([refreshSimulados(), refreshNotas()]);
  };

  const finalizarSimulado = async (id: number) => {
    await api.simuladoAPI.finalizar(id);
    await Promise.all([refreshSimulados(), refreshNotas()]);
  };

  const lancarNota = async (alunoId: number, dto: any) => {
    await api.alunoAPI.lancarNota(alunoId, dto);
    await Promise.all([refreshNotas(), refreshSimulados()]);
  };

  const retificarNota = async (alunoId: number, dto: any) => {
    await api.alunoAPI.retificarNota(alunoId, dto);
    await Promise.all([refreshNotas(), refreshSimulados()]);
  };

  const getSimuladoDetalhe = async (id: number) => {
    return api.simuladoAPI.detalhar(id);
  };

  // Calcular média ponderada do aluno no simulado
  const calcularMedia = (alunoId: string, simuladoId: string): number => {
    const simulado = simulados.find(s => s.id?.toString() === simuladoId);
    if (!simulado || !simulado.disciplinas || simulado.disciplinas.length === 0) {
      return 0;
    }

    const notasDoAluno = notas.filter(n =>
      n.simuladoId?.toString() === simuladoId && n.alunoId?.toString() === alunoId
    );

    if (notasDoAluno.length === 0) {
      return 0;
    }

    let somaPonderada = 0;
    let somaPesos = 0;

    for (const nota of notasDoAluno) {
      const disciplina = simulado.disciplinas.find((d: any) =>
        d.disciplinaId?.toString() === nota.disciplinaId?.toString()
      );

      if (disciplina && nota.valor !== undefined && nota.valor !== null) {
        const peso = disciplina.peso || 0;
        somaPonderada += nota.valor * peso;
        somaPesos += peso;
      }
    }

    return somaPesos > 0 ? somaPonderada / somaPesos : 0;
  };

  useEffect(() => {
    refreshAll();
  }, []);

  const value: DataContextType = {
    alunos,
    turmas,
    professores,
    disciplinas,
    responsaveis,
    simulados,
    notas,
    rankings,
    loading,
    error,
    refreshAlunos,
    refreshTurmas,
    refreshProfessores,
    refreshDisciplinas,
    refreshResponsaveis,
    refreshSimulados,
    refreshRankings,
    refreshAll,
    addAluno,
    updateAluno,
    deleteAluno,
    inativarAluno,
    addTurma,
    updateTurma,
    deleteTurma,
    inativarTurma,
    addProfessor,
    updateProfessor,
    deleteProfessor,
    addDisciplina,
    updateDisciplina,
    deleteDisciplina,
    addResponsavel,
    updateResponsavel,
    deleteResponsavel,
    addSimulado,
    updateSimulado,
    deleteSimulado,
    finalizarSimulado,
    lancarNota,
    retificarNota,
    calcularMedia,
    getSimuladoDetalhe,
  };

  return <DataContext.Provider value={value}>{children}</DataContext.Provider>;
}

export function useData() {
  const context = useContext(DataContext);
  if (!context) {
    throw new Error('useData must be used within DataProvider');
  }
  return context;
}

