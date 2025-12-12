import React, { useEffect, useState } from 'react';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Badge } from '../components/ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../components/ui/table';
import { EmptyState } from '../components/EmptyState';
import { Plus, Search, ClipboardList, Edit, Trash, Trophy } from 'lucide-react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../components/ui/tabs';
import {
  SimuladoModal,
  SimuladoGradingModal,
  SimuladoRankingModal,
  SimuladoRetificacaoModal,
  SimuladoAlteracoesLoteModal
} from '../components/modals';

interface Simulado {
  id: string;
  turmaId: string;
  data: string | Date;
  status: string;
  disciplinas: Array<{ disciplinaId: string; peso: number }>;
}

interface Turma {
  id: string;
  nome: string;
}

interface Disciplina {
  id: string;
  nome: string;
}

interface Aluno {
  id: string;
  nome: string;
  turmaId: string;
  status: string;
  dataNascimento: string | Date;
}

interface Retificacao {
  id: string;
  valorOriginal: number;
  valorNovo: number;
  justificativa: string;
  autor?: string;
  criadoEm?: string | Date;
}

interface Nota {
  id: string;
  simuladoId: string;
  alunoId: string;
  disciplinaId: string;
  valor: number;
  retificacoes?: Array<Retificacao>;
}

interface SimuladosProps {
  simulados: Simulado[];
  turmas: Turma[];
  disciplinas: Disciplina[];
  alunos: Aluno[];
  notas: Nota[];
  professores: any[];
  formatDate: (date: string | Date) => string;
  formatNumber: (value: number, decimals: number) => string;
  calcularMedia: (alunoId: string, simuladoId: string) => number;
  fetchSimuladoDetalhe: (id: number) => Promise<any>;
  onSave?: (dto: { dataAplicacao: string; turmaId: number; disciplinas: Array<{ disciplinaId: number; peso: number }> }) => Promise<void>;
  onUpdate?: (id: number, dto: { disciplinas: Array<{ disciplinaId: number; peso: number }> }) => Promise<void>;
  onDelete?: (simulado: Simulado) => void;
  onLancarNotas?: (simulado: Simulado) => void;
  onVerRanking?: (simulado: Simulado) => void;
  onRetificarNota?: (nota: Nota) => void;
  onFinalizar?: (id: number) => Promise<void>;
  onLancarNota?: (alunoId: number, dto: any) => Promise<void>;
  onRetificarNotaFunc?: (alunoId: number, dto: any) => Promise<void>;
}

export function Simulados({
  simulados,
  turmas,
  disciplinas,
  alunos,
  notas,
  professores,
  formatDate,
  formatNumber,
  calcularMedia,
  fetchSimuladoDetalhe,
  onSave,
  onUpdate,
  onDelete,
  onLancarNotas,
  onVerRanking,
  onRetificarNota: onRetificarNotaProp,
  onFinalizar,
  onLancarNota,
  onRetificarNota: onRetificarNotaFunc
}: SimuladosProps) {
  const [activeTab, setActiveTab] = useState('lista');
  const [searchTerm, setSearchTerm] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [gradingDialogOpen, setGradingDialogOpen] = useState(false);
  const [rankingDialogOpen, setRankingDialogOpen] = useState(false);
  const [retificacaoDialogOpen, setRetificacaoDialogOpen] = useState(false);
  const [alteracoesDialogOpen, setAlteracoesDialogOpen] = useState(false);
  const [selectedSimulado, setSelectedSimulado] = useState<Simulado | null>(null);
  const [selectedNota, setSelectedNota] = useState<Nota | null>(null);
  
  const [formData, setFormData] = useState({
    turmaId: '',
    data: '',
    disciplinas: [] as { disciplinaId: string; peso: number }[]
  });

  const [retificacaoData, setRetificacaoData] = useState({
    valorNovo: '',
    justificativa: ''
  });

  const [gradeData, setGradeData] = useState<{ [key: string]: string }>({});
  const [alteracoesPendentes, setAlteracoesPendentes] = useState<Array<{
    nota: Nota;
    aluno: string;
    disciplina: string;
    valorOriginal: number;
    valorNovo: number;
    justificativa: string;
  }>>([]);
  const [notasOverride, setNotasOverride] = useState<{ [key: string]: number }>({});

  const buildGradeKey = (alunoId: string, disciplinaId: string) => `${alunoId}-${disciplinaId}`;
  const buildOverrideKey = (simuladoId: string, alunoId: string, disciplinaId: string) =>
    `${simuladoId}::${alunoId}::${disciplinaId}`;

  const formatValorParaInput = (valor: number) => {
    return formatNumber(valor, 2);
  };

  const atualizarGradeComValor = (simuladoId: string, alunoId: string, disciplinaId: string, valorNovo: number) => {
    const gradeKey = buildGradeKey(alunoId, disciplinaId);
    const overrideKey = buildOverrideKey(simuladoId, alunoId, disciplinaId);
    setGradeData((prev) => ({
      ...prev,
      [gradeKey]: formatValorParaInput(valorNovo)
    }));
    setNotasOverride((prev) => ({
      ...prev,
      [overrideKey]: valorNovo
    }));
  };

  const parseValorNota = (valor?: string): number | null => {
    if (!valor || !valor.trim()) return null;
    const normalizado = valor.replace(/\./g, '').replace(',', '.');
    const numero = parseFloat(normalizado);
    if (isNaN(numero)) return null;
    if (numero < 0 || numero > 10) return null;
    return numero;
  };

  const obterNotaAtual = (simuladoId: string, alunoId: string, disciplinaId: string): number | null => {
    const overrideValor = notasOverride[buildOverrideKey(simuladoId, alunoId, disciplinaId)];
    if (overrideValor !== undefined) {
      return overrideValor;
    }

    const notaBase = notas.find(n =>
      n.simuladoId === simuladoId &&
      n.alunoId === alunoId &&
      n.disciplinaId === disciplinaId
    );

    return notaBase ? notaBase.valor : null;
  };

  const calcularMediaLocal = (alunoId: string, simuladoId: string) => {
    const simulado =
      simulados.find(s => s.id === simuladoId) ||
      (selectedSimulado && selectedSimulado.id === simuladoId ? selectedSimulado : null);

    if (!simulado) {
      return calcularMedia(alunoId, simuladoId);
    }

    let somaNotas = 0;
    let somaPesos = 0;

    (simulado.disciplinas || []).forEach(disc => {
      const valor = obterNotaAtual(simuladoId, alunoId, disc.disciplinaId);
      if (valor !== null && valor !== undefined) {
        somaNotas += valor * disc.peso;
        somaPesos += disc.peso;
      }
    });

    if (somaPesos === 0) {
      return calcularMedia(alunoId, simuladoId);
    }

    return somaNotas / somaPesos;
  };

  const buildAlteracoesParaJustificar = (): typeof alteracoesPendentes => {
    if (!selectedSimulado) return [];

    const alunosDaTurmaAtual = alunos.filter(a => a.turmaId === selectedSimulado.turmaId && a.status === 'ATIVO');
    const notasDoSimulado = notas.filter(n => n.simuladoId === selectedSimulado.id);

    const lista: typeof alteracoesPendentes = [];

    alunosDaTurmaAtual.forEach(aluno => {
      (selectedSimulado.disciplinas || []).forEach(disc => {
        const key = buildGradeKey(aluno.id, disc.disciplinaId);
        const valorNovo = parseValorNota(gradeData[key]);
        if (valorNovo === null) return;

        const notaExistente = notasDoSimulado.find(n =>
          n.alunoId === aluno.id &&
          n.disciplinaId === disc.disciplinaId
        );

        if (notaExistente && Math.abs(notaExistente.valor - valorNovo) > 0.001) {
          const disciplinaNome = disciplinas.find(d => d.id === disc.disciplinaId)?.nome || '';
          lista.push({
            nota: notaExistente,
            aluno: alunos.find(a => a.id === aluno.id)?.nome || aluno.nome,
            disciplina: disciplinaNome,
            valorOriginal: notaExistente.valor,
            valorNovo,
            justificativa: ''
          });
        }
      });
    });

    return lista;
  };

  const processarSalvarNotas = async () => {
    if (!selectedSimulado) return;

    const alunosDaTurmaAtual = alunos.filter(a => a.turmaId === selectedSimulado.turmaId && a.status === 'ATIVO');
    const notasDoSimulado = notas.filter(n => n.simuladoId === selectedSimulado.id);
    const novosLancamentos: Array<{ simuladoId: string; alunoId: string; disciplinaId: string; valor: number }> = [];

    try {
      const promessas: Promise<void>[] = [];

      alunosDaTurmaAtual.forEach(aluno => {
        (selectedSimulado.disciplinas || []).forEach(disc => {
          const key = buildGradeKey(aluno.id, disc.disciplinaId);
          const valorNovo = parseValorNota(gradeData[key]);
          if (valorNovo === null) return;

          const notaExistente = notasDoSimulado.find(n =>
            n.alunoId === aluno.id &&
            n.disciplinaId === disc.disciplinaId
          );

          if (!notaExistente && onLancarNota) {
            const alunoIdNum = parseInt(aluno.id);
            const disciplinaIdNum = parseInt(disc.disciplinaId);
            const simuladoIdNum = parseInt(selectedSimulado.id);
            if (isNaN(alunoIdNum) || isNaN(disciplinaIdNum) || isNaN(simuladoIdNum)) {
              return;
            }
            promessas.push(onLancarNota(alunoIdNum, {
              simuladoId: simuladoIdNum,
              disciplinaId: disciplinaIdNum,
              valor: valorNovo
            }));
            novosLancamentos.push({
              simuladoId: selectedSimulado.id,
              alunoId: aluno.id,
              disciplinaId: disc.disciplinaId,
              valor: valorNovo
            });
          }
        });
      });

      await Promise.all(promessas);
      if (novosLancamentos.length > 0) {
        setNotasOverride((prev) => {
          const atualizado = { ...prev };
          novosLancamentos.forEach(lanc => {
            const key = buildOverrideKey(lanc.simuladoId, lanc.alunoId, lanc.disciplinaId);
            atualizado[key] = lanc.valor;
          });
          return atualizado;
        });
      }
      alert('Notas salvas com sucesso!');
      setGradeData({});
      setGradingDialogOpen(false);
    } catch (error: any) {
      console.error('Erro ao salvar notas:', error);
      alert('Erro ao salvar notas: ' + (error?.message || 'Erro desconhecido'));
    }
  };

  const handleSaveGrades = async () => {
    if (!selectedSimulado) return;

    const alteracoes = buildAlteracoesParaJustificar();
    if (alteracoes.length > 0) {
      setAlteracoesPendentes(alteracoes);
      setAlteracoesDialogOpen(true);
      return;
    }

    await processarSalvarNotas();
  };

  const handleConfirmarAlteracoes = async () => {
    if (!selectedSimulado) return;

    const justificativasInvalidas = alteracoesPendentes.some(a => a.justificativa.trim().length < 20);
    if (justificativasInvalidas) {
      alert('Todas as justificativas devem conter pelo menos 20 caracteres');
      return;
    }

    if (!onRetificarNotaFunc) {
      alert('Função de retificação indisponível');
      return;
    }

    const professorId = professores.length > 0 ? parseInt(professores[0].id) : 1;
    const alteracoesExecutadas = [...alteracoesPendentes];

    try {
      const promessas = alteracoesExecutadas.map(alteracao => {
        const alunoIdNum = parseInt(alteracao.nota.alunoId);
        const simuladoIdNum = parseInt(alteracao.nota.simuladoId);
        const disciplinaIdNum = parseInt(alteracao.nota.disciplinaId);
        if (isNaN(alunoIdNum) || isNaN(simuladoIdNum) || isNaN(disciplinaIdNum)) {
          return Promise.resolve();
        }

        return onRetificarNotaFunc(alunoIdNum, {
          simuladoId: simuladoIdNum,
          disciplinaId: disciplinaIdNum,
          professorId,
          novoValor: alteracao.valorNovo,
          justificativa: alteracao.justificativa.trim()
        });
      });

      await Promise.all(promessas);
      setGradeData((prev) => {
        const atualizado = { ...prev };
        alteracoesExecutadas.forEach((alteracao) => {
          const key = buildGradeKey(alteracao.nota.alunoId, alteracao.nota.disciplinaId);
          atualizado[key] = formatValorParaInput(alteracao.valorNovo);
        });
        return atualizado;
      });
      setNotasOverride((prev) => {
        const atualizado = { ...prev };
        alteracoesExecutadas.forEach((alteracao) => {
          const key = buildOverrideKey(alteracao.nota.simuladoId, alteracao.nota.alunoId, alteracao.nota.disciplinaId);
          atualizado[key] = alteracao.valorNovo;
        });
        return atualizado;
      });
      alert(`${alteracoesPendentes.length} nota(s) retificada(s) com sucesso!`);
      setAlteracoesDialogOpen(false);
      setAlteracoesPendentes([]);
      await processarSalvarNotas();
    } catch (error: any) {
      console.error('Erro ao retificar notas:', error);
      alert('Erro ao retificar notas: ' + (error?.message || 'Erro desconhecido'));
    }
  };

  const handleCancelarAlteracoes = () => {
    setAlteracoesDialogOpen(false);
    setAlteracoesPendentes([]);
  };

  const closeRetificacaoModal = () => {
    setRetificacaoDialogOpen(false);
    setRetificacaoData({ valorNovo: '', justificativa: '' });
    setSelectedNota(null);
  };

  useEffect(() => {
    setNotasOverride((prev) => {
      if (Object.keys(prev).length === 0) return prev;
      const atualizado = { ...prev };
      Object.keys(atualizado).forEach((key) => {
        const [simId, alunoId, disciplinaId] = key.split('::');
        const notaAtual = notas.find(n =>
          n.simuladoId === simId &&
          n.alunoId === alunoId &&
          n.disciplinaId === disciplinaId
        );
        if (notaAtual && Math.abs(notaAtual.valor - atualizado[key]) < 0.0001) {
          delete atualizado[key];
        }
      });
      return atualizado;
    });
  }, [notas]);

  const normalizeDisciplinas = (lista: any[] | undefined | null) => {
    if (!Array.isArray(lista)) return [];
    return lista.map((d: any) => ({
      disciplinaId: (d.disciplinaId ?? d.id ?? d.disciplina?.id)?.toString() || '',
      peso: d.peso ?? d.pesoPercentual ?? 0
    }));
  };

  const ensureSimuladoDetalhado = async (sim: Simulado): Promise<Simulado> => {
    if (sim.disciplinas && sim.disciplinas.length > 0) {
      return sim;
    }

    try {
      const detalhe = await fetchSimuladoDetalhe(parseInt(sim.id));
      return {
        ...sim,
        disciplinas: normalizeDisciplinas(detalhe.disciplinas)
      };
    } catch (error) {
      console.error('Erro ao carregar disciplinas do simulado:', error);
      alert('Não foi possível carregar as disciplinas deste simulado. Tente novamente.');
      return sim;
    }
  };

  const filteredSimulados = simulados.filter(sim => {
    const turma = turmas.find(t => t.id === sim.turmaId);
    return turma?.nome.toLowerCase().includes(searchTerm.toLowerCase());
  });

  const handleOpenDialog = async (sim?: Simulado) => {
    if (sim) {
      const detalhado = await ensureSimuladoDetalhado(sim);
      setSelectedSimulado(detalhado);
      setFormData({
        turmaId: detalhado.turmaId,
        data: formatDate(detalhado.data),
        disciplinas: detalhado.disciplinas
      });
    } else {
      setSelectedSimulado(null);
      setFormData({
        turmaId: '',
        data: '',
        disciplinas: []
      });
    }
    setDialogOpen(true);
  };

  const handleOpenGrading = async (sim: Simulado) => {
    const detalhado = await ensureSimuladoDetalhado(sim);
    setSelectedSimulado(detalhado);

    const alunosDaTurmaAtual = alunos.filter(a => a.turmaId === detalhado.turmaId && a.status === 'ATIVO');
    const initialGradeData: { [key: string]: string } = {};

    alunosDaTurmaAtual.forEach(aluno => {
      (detalhado.disciplinas || []).forEach(disc => {
        const key = buildGradeKey(aluno.id, disc.disciplinaId);
        const overrideValor = notasOverride[buildOverrideKey(detalhado.id, aluno.id, disc.disciplinaId)];
        const notaExistente = notas.find(n =>
          n.simuladoId === detalhado.id &&
          n.alunoId === aluno.id &&
          n.disciplinaId === disc.disciplinaId
        );
        const valorAtual = overrideValor !== undefined
          ? overrideValor
          : notaExistente
            ? notaExistente.valor
            : null;
        initialGradeData[key] = valorAtual !== null && valorAtual !== undefined
          ? formatValorParaInput(valorAtual)
          : '';
      });
    });

    setGradeData(initialGradeData);
    setAlteracoesPendentes([]);
    setAlteracoesDialogOpen(false);
    setGradingDialogOpen(true);
    onLancarNotas?.(detalhado);
  };

  const handleOpenRanking = async (sim: Simulado) => {
    const detalhado = await ensureSimuladoDetalhado(sim);
    setSelectedSimulado(detalhado);
    setRankingDialogOpen(true);
    onVerRanking?.(detalhado);
  };

  const handleOpenRetificacao = (nota: Nota) => {
    setSelectedNota(nota);
    setRetificacaoDialogOpen(true);
    onRetificarNota?.(nota);
  };

  const handleDelete = (sim: Simulado) => {
    onDelete?.(sim);
  };

  const addDisciplinaToForm = () => {
    setFormData({
      ...formData,
      disciplinas: [...formData.disciplinas, { disciplinaId: '', peso: 0 }]
    });
  };

  const removeDisciplinaFromForm = (index: number) => {
    setFormData({
      ...formData,
      disciplinas: formData.disciplinas.filter((_, i) => i !== index)
    });
  };

  const updateDisciplina = (index: number, disciplinaId: string, peso: number) => {
    const updated = [...formData.disciplinas];
    updated[index] = { disciplinaId, peso };
    setFormData({ ...formData, disciplinas: updated });
  };

  const totalPeso = formData.disciplinas.reduce((sum, d) => sum + (d.peso || 0), 0);

  const handleSave = async () => {
    // Validações
    if (!formData.turmaId) {
      alert('Turma é obrigatória');
      return;
    }
    if (!formData.data) {
      alert('Data de aplicação é obrigatória');
      return;
    }
    if (formData.disciplinas.length < 2) {
      alert('Mínimo de 2 disciplinas é obrigatório');
      return;
    }
    
    // Validar que todas as disciplinas estão preenchidas
    const disciplinasInvalidas = formData.disciplinas.some(d => !d.disciplinaId || d.peso <= 0);
    if (disciplinasInvalidas) {
      alert('Todas as disciplinas devem ter um ID e peso maior que zero');
      return;
    }
    
    // Validar soma dos pesos = 10
    if (Math.abs(totalPeso - 10) > 0.01) {
      alert(`A soma dos pesos deve ser exatamente 10. Atual: ${totalPeso.toFixed(2)}`);
      return;
    }

    try {
      // Converter data de DD/MM/YYYY para YYYY-MM-DD
      const [dia, mes, ano] = formData.data.split('/');
      const dataAplicacaoISO = `${ano}-${mes}-${dia}`;
      
      const turmaIdNum = parseInt(formData.turmaId);
      if (isNaN(turmaIdNum)) {
        alert('Turma inválida');
        return;
      }

      const disciplinasDto = formData.disciplinas.map(d => ({
        disciplinaId: parseInt(d.disciplinaId),
        peso: d.peso
      }));

      if (selectedSimulado) {
        // Editar - apenas disciplinas podem ser editadas
        await onUpdate?.(parseInt(selectedSimulado.id), {
          disciplinas: disciplinasDto
        });
      } else {
        // Criar
        await onSave?.({
          dataAplicacao: dataAplicacaoISO,
          turmaId: turmaIdNum,
          disciplinas: disciplinasDto
        });
      }
      
      setDialogOpen(false);
      // Limpar formulário
      setFormData({
        turmaId: '',
        data: '',
        disciplinas: []
      });
      setSelectedSimulado(null);
    } catch (error: any) {
      console.error('Erro ao salvar simulado:', error);
      const errorMessage = error.message || 'Erro desconhecido';
      alert('Erro ao salvar simulado: ' + errorMessage);
    }
  };

  const getRanking = () => {
    if (!selectedSimulado) return [];
    
    const alunosDaTurma = alunos.filter(a => a.turmaId === selectedSimulado.turmaId && a.status === 'ATIVO');
    
    return alunosDaTurma
      .map(aluno => ({
        aluno,
        media: calcularMediaLocal(aluno.id, selectedSimulado.id)
      }))
      .sort((a, b) => {
        if (b.media !== a.media) return b.media - a.media;
        // Desempate por idade (mais velho primeiro)
        return new Date(a.aluno.dataNascimento).getTime() - new Date(b.aluno.dataNascimento).getTime();
      });
  };

  const alunosDaTurma = selectedSimulado 
    ? alunos.filter(a => a.turmaId === selectedSimulado.turmaId && a.status === 'ATIVO')
    : [];

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1>Simulados</h1>
          <p className="text-muted-foreground">Gerencie os simulados e notas</p>
        </div>
        <Button onClick={() => handleOpenDialog()}>
          <Plus className="size-4 mr-2" />
          Novo Simulado
        </Button>
      </div>

      <div className="bg-card rounded-lg border p-4 mb-6">
        <Label>Buscar</Label>
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-muted-foreground" />
          <Input
            placeholder="Turma..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pl-9"
          />
        </div>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList>
          <TabsTrigger value="lista">Lista</TabsTrigger>
          <TabsTrigger value="detalhes">Detalhes</TabsTrigger>
        </TabsList>

        <TabsContent value="lista">
          {filteredSimulados.length === 0 ? (
            <EmptyState
              icon={ClipboardList}
              title="Nenhum simulado encontrado"
              description="Comece criando um novo simulado ou ajuste o filtro de busca"
              actionLabel="Novo Simulado"
              onAction={() => handleOpenDialog()}
            />
          ) : (
            <div className="bg-card rounded-lg border">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Turma</TableHead>
                    <TableHead>Data</TableHead>
                    <TableHead>Disciplinas</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className="text-right">Ações</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredSimulados.map(sim => {
                    const turma = turmas.find(t => t.id === sim.turmaId);
                    const disciplinasSim = sim.disciplinas.map(d => 
                      disciplinas.find(dis => dis.id === d.disciplinaId)?.nome
                    ).filter(Boolean).join(', ');

                    return (
                      <TableRow key={sim.id}>
                        <TableCell>{turma?.nome || '-'}</TableCell>
                        <TableCell>{formatDate(sim.data)}</TableCell>
                        <TableCell>{disciplinasSim}</TableCell>
                        <TableCell>
                          <Badge variant={sim.status === 'FINALIZADO' ? 'default' : 'secondary'}>
                            {sim.status === 'EM_EDICAO' ? 'Em Edição' : 'Finalizado'}
                          </Badge>
                        </TableCell>
                        <TableCell className="text-right">
                          <div className="flex items-center justify-end gap-2">
                            {sim.status === 'EM_EDICAO' && (
                              <>
                                <Button
                                  variant="ghost"
                                  size="sm"
                                  onClick={() => handleOpenDialog(sim)}
                                  title="Editar"
                                >
                                  <Edit className="size-4" />
                                </Button>
                                <Button
                                  variant="default"
                                  size="sm"
                                  onClick={async () => {
                                    if (confirm('Tem certeza que deseja finalizar este simulado? Após finalizar, não será possível editá-lo.')) {
                                      try {
                                        await onFinalizar?.(parseInt(sim.id));
                                      } catch (error: any) {
                                        alert('Erro ao finalizar simulado: ' + (error.message || 'Erro desconhecido'));
                                      }
                                    }
                                  }}
                                  title="Finalizar Simulado"
                                >
                                  Finalizar
                                </Button>
                              </>
                            )}
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => handleOpenGrading(sim)}
                              title="Lançar Notas"
                            >
                              <Edit className="size-4" />
                            </Button>
                            {sim.status === 'FINALIZADO' && (
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => handleOpenRanking(sim)}
                                title="Ver Ranking"
                              >
                                <Trophy className="size-4" />
                              </Button>
                            )}
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => handleDelete(sim)}
                              title="Excluir"
                            >
                              <Trash className="size-4" />
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            </div>
          )}
        </TabsContent>

        <TabsContent value="detalhes">
          <div className="bg-card rounded-lg border p-6">
            <p className="text-muted-foreground">Selecione um simulado da lista para ver os detalhes</p>
          </div>
        </TabsContent>
      </Tabs>

      <SimuladoModal
        open={dialogOpen}
        onOpenChange={setDialogOpen}
        isEdit={!!selectedSimulado}
        formData={formData}
        turmas={turmas}
        disciplinas={disciplinas}
        onFormDataChange={setFormData}
        onAddDisciplina={addDisciplinaToForm}
        onRemoveDisciplina={removeDisciplinaFromForm}
        onUpdateDisciplina={updateDisciplina}
        totalPeso={totalPeso}
        onSave={handleSave}
        onCancel={() => setDialogOpen(false)}
      />

      {selectedSimulado && (
        <>
          <SimuladoGradingModal
            open={gradingDialogOpen}
            onOpenChange={setGradingDialogOpen}
            simulado={selectedSimulado}
            turmaNome={turmas.find(t => t.id === selectedSimulado.turmaId)?.nome || ''}
            alunos={alunosDaTurma}
            disciplinas={disciplinas}
            notas={notas.filter(n => n.simuladoId === selectedSimulado.id)}
            gradeData={gradeData}
            onGradeDataChange={setGradeData}
            onOpenRetificacao={handleOpenRetificacao}
            calcularMedia={calcularMediaLocal}
            formatNumber={formatNumber}
            isEditMode={selectedSimulado.status === 'EM_EDICAO'}
            onSave={handleSaveGrades}
            onFinalizar={async () => {
              if (!selectedSimulado) return;
              if (window.confirm('Tem certeza que deseja finalizar este simulado? Após finalizar, não será possível editá-lo.')) {
                try {
                  await onFinalizar?.(parseInt(selectedSimulado.id));
                  alert('Simulado finalizado com sucesso!');
                  setGradingDialogOpen(false);
                } catch (error: any) {
                  console.error('Erro ao finalizar simulado:', error);
                  alert('Erro ao finalizar simulado: ' + (error.message || 'Erro desconhecido'));
                }
              }
            }}
            onClose={() => {
              setGradingDialogOpen(false);
              setGradeData({});
            }}
          />

          <SimuladoRankingModal
            open={rankingDialogOpen}
            onOpenChange={setRankingDialogOpen}
            turmaNome={turmas.find(t => t.id === selectedSimulado.turmaId)?.nome || ''}
            ranking={getRanking()}
            formatDate={formatDate}
            formatNumber={formatNumber}
          />
        </>
      )}

      {selectedNota && (
        <SimuladoRetificacaoModal
          open={retificacaoDialogOpen}
          onOpenChange={(open) => {
            if (open) {
              setRetificacaoDialogOpen(true);
            } else {
              closeRetificacaoModal();
            }
          }}
          notaOriginal={selectedNota.valor}
          historico={selectedNota.retificacoes || []}
          formData={retificacaoData}
          onFormDataChange={setRetificacaoData}
          formatNumber={formatNumber}
          onSave={async () => {
            if (!selectedNota || !selectedSimulado) return;
            
            // Validações
            const valorNovo = parseFloat(retificacaoData.valorNovo.replace(',', '.'));
            if (isNaN(valorNovo) || valorNovo < 0 || valorNovo > 10) {
              alert('Valor da nota deve estar entre 0 e 10');
              return;
            }
            
            if (retificacaoData.justificativa.trim().length < 20) {
              alert('Justificativa deve conter pelo menos 20 caracteres');
              return;
            }
            
            // Buscar primeiro professor disponível (em produção, usar o professor logado)
            const professorId = professores.length > 0 ? parseInt(professores[0].id) : 1;
            
            try {
              if (onRetificarNotaFunc) {
                await onRetificarNotaFunc(parseInt(selectedNota.alunoId), {
                  simuladoId: parseInt(selectedNota.simuladoId),
                  disciplinaId: parseInt(selectedNota.disciplinaId),
                  professorId: professorId,
                  novoValor: valorNovo,
                  justificativa: retificacaoData.justificativa.trim()
                });
                atualizarGradeComValor(selectedNota.simuladoId, selectedNota.alunoId, selectedNota.disciplinaId, valorNovo);
                alert('Nota retificada com sucesso!');
                closeRetificacaoModal();
              }
            } catch (error: any) {
              console.error('Erro ao retificar nota:', error);
              alert('Erro ao retificar nota: ' + (error.message || 'Erro desconhecido'));
            }
          }}
          onCancel={closeRetificacaoModal}
        />
      )}

      <SimuladoAlteracoesLoteModal
        open={alteracoesDialogOpen}
        onOpenChange={(open) => {
          setAlteracoesDialogOpen(open);
          if (!open) {
            setAlteracoesPendentes([]);
          }
        }}
        alteracoesPendentes={alteracoesPendentes}
        onAlteracoesChange={setAlteracoesPendentes}
        formatNumber={formatNumber}
        onSave={handleConfirmarAlteracoes}
        onCancel={handleCancelarAlteracoes}
      />
    </div>
  );
}

