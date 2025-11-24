import React, { useState } from 'react';
import { Label } from '../components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../components/ui/select';
import { EmptyState } from '../components/EmptyState';
import { FileText, AlertCircle } from 'lucide-react';
import { Badge } from '../components/ui/badge';
import { Card, CardContent } from '../components/ui/card';

interface Retificacao {
  valorOriginal: number;
  valorNovo: number;
  justificativa: string;
  criadoEm: string | Date;
  nota: {
    alunoId: string;
    simuladoId: string;
    disciplinaId: string;
  };
  aluno?: {
    nome: string;
  };
  disciplina?: {
    nome: string;
  };
  simulado?: {
    turmaId: string;
    data: string | Date;
  };
}

interface AuditoriaProps {
  notas: Array<{
    alunoId: string;
    simuladoId: string;
    disciplinaId: string;
    retificacoes: Retificacao[];
  }>;
  alunos: Array<{ id: string; nome: string }>;
  turmas: Array<{ id: string; nome: string }>;
  simulados: Array<{ id: string; turmaId: string; data: string | Date }>;
  disciplinas: Array<{ id: string; nome: string }>;
  formatDateTime: (date: string | Date) => string;
  formatNumber: (value: number, decimals: number) => string;
}

export function Auditoria({
  notas,
  alunos,
  turmas,
  simulados,
  disciplinas,
  formatDateTime,
  formatNumber
}: AuditoriaProps) {
  const [alunoFilter, setAlunoFilter] = useState('TODOS');
  const [turmaFilter, setTurmaFilter] = useState('TODAS');
  const [simuladoFilter, setSimuladoFilter] = useState('TODOS');

  // Coletar todas as retificações
  const todasRetificacoes = notas.flatMap(nota => 
    nota.retificacoes.map(ret => ({
      ...ret,
      nota,
      aluno: alunos.find(a => a.id === nota.alunoId),
      disciplina: disciplinas.find(d => d.id === nota.disciplinaId),
      simulado: simulados.find(s => s.id === nota.simuladoId)
    }))
  );

  const filteredRetificacoes = todasRetificacoes.filter(ret => {
    const matchesAluno = alunoFilter === 'TODOS' || ret.nota.alunoId === alunoFilter;
    const matchesSimulado = simuladoFilter === 'TODOS' || ret.nota.simuladoId === simuladoFilter;
    const matchesTurma = turmaFilter === 'TODAS' || ret.simulado?.turmaId === turmaFilter;
    return matchesAluno && matchesSimulado && matchesTurma;
  });

  // Ordenar por data (mais recente primeiro)
  const sortedRetificacoes = [...filteredRetificacoes].sort(
    (a, b) => new Date(b.criadoEm).getTime() - new Date(a.criadoEm).getTime()
  );

  return (
    <div className="p-8">
      <div className="mb-8">
        <h1>Auditoria</h1>
        <p className="text-muted-foreground">Histórico de alterações de notas (retificações)</p>
      </div>

      <div className="bg-card rounded-lg border p-4 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <Label>Turma</Label>
            <Select value={turmaFilter} onValueChange={setTurmaFilter}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="TODAS">Todas</SelectItem>
                {turmas.map(turma => (
                  <SelectItem key={turma.id} value={turma.id}>{turma.nome}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div>
            <Label>Simulado</Label>
            <Select value={simuladoFilter} onValueChange={setSimuladoFilter}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="TODOS">Todos</SelectItem>
                {simulados.map(sim => {
                  const turma = turmas.find(t => t.id === sim.turmaId);
                  return (
                    <SelectItem key={sim.id} value={sim.id}>
                      {turma?.nome} - {new Date(sim.data).toLocaleDateString('pt-BR')}
                    </SelectItem>
                  );
                })}
              </SelectContent>
            </Select>
          </div>
          <div>
            <Label>Aluno</Label>
            <Select value={alunoFilter} onValueChange={setAlunoFilter}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="TODOS">Todos</SelectItem>
                {alunos.map(aluno => (
                  <SelectItem key={aluno.id} value={aluno.id}>{aluno.nome}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </div>
      </div>

      {sortedRetificacoes.length === 0 ? (
        <EmptyState
          icon={FileText}
          title="Nenhuma retificação encontrada"
          description="Não há alterações de notas registradas no sistema"
        />
      ) : (
        <div className="space-y-4">
          {sortedRetificacoes.map((ret, index) => {
            const turma = ret.simulado ? turmas.find(t => t.id === ret.simulado.turmaId) : null;
            const diferenca = ret.valorNovo - ret.valorOriginal;

            return (
              <Card key={index}>
                <CardContent className="p-6">
                  <div className="flex items-start justify-between mb-4">
                    <div>
                      <h3 className="font-semibold">{ret.aluno?.nome || 'Aluno desconhecido'}</h3>
                      <p className="text-sm text-muted-foreground">
                        {ret.disciplina?.nome || 'Disciplina desconhecida'} - {turma?.nome || 'Turma desconhecida'}
                      </p>
                    </div>
                    <Badge variant={diferenca > 0 ? 'default' : 'destructive'}>
                      {diferenca > 0 ? '+' : ''}{formatNumber(diferenca, 2)}
                    </Badge>
                  </div>

                  <div className="grid grid-cols-2 gap-4 mb-4">
                    <div>
                      <p className="text-sm text-muted-foreground">Valor Original</p>
                      <p className="text-lg font-semibold">{formatNumber(ret.valorOriginal, 2)}</p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground">Novo Valor</p>
                      <p className="text-lg font-semibold text-primary">{formatNumber(ret.valorNovo, 2)}</p>
                    </div>
                  </div>

                  <div className="mb-4">
                    <p className="text-sm text-muted-foreground mb-2">Justificativa</p>
                    <p className="text-sm">{ret.justificativa}</p>
                  </div>

                  <div className="flex items-center gap-2 text-sm text-muted-foreground">
                    <AlertCircle className="size-4" />
                    <span>Alterado em {formatDateTime(ret.criadoEm)}</span>
                  </div>
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}
    </div>
  );
}


