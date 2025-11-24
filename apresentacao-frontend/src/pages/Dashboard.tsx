import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/card';
import { Alert, AlertDescription } from '../components/ui/alert';
import { Users, GraduationCap, BookOpen, AlertCircle, Calendar } from 'lucide-react';
import { Badge } from '../components/ui/badge';

interface DashboardProps {
  alunos: Array<{ status: string; turmaId?: string }>;
  responsaveis: Array<any>;
  professores: Array<any>;
  turmas: Array<{ status: string }>;
  simulados: Array<{ status: string; data: string | Date; turmaId?: string; disciplinas?: Array<any> }>;
  notas: Array<any>;
  formatDate: (date: string | Date) => string;
}

export function Dashboard({
  alunos,
  responsaveis,
  professores,
  turmas,
  simulados,
  notas,
  formatDate
}: DashboardProps) {
  const alunosAtivos = alunos.filter(a => a.status === 'ATIVO').length;
  const turmasAtivas = turmas.filter(t => t.status === 'ATIVO').length;
  
  const proximosSimulados = simulados
    .filter(s => s.status === 'EM_EDICAO' && new Date(s.data) >= new Date())
    .sort((a, b) => new Date(a.data).getTime() - new Date(b.data).getTime())
    .slice(0, 3);

  const simuladosFinalizados = simulados.filter(s => s.status === 'FINALIZADO');
  const totalNotasEsperadas = simuladosFinalizados.reduce((acc, sim) => {
    const simulado = sim as any;
    const alunosDaTurma = alunos.filter(a => a.turmaId === simulado.turmaId && a.status === 'ATIVO');
    return acc + (alunosDaTurma.length * (simulado.disciplinas?.length || 0));
  }, 0);
  const notasLancadas = notas.length;
  const notasPendentes = totalNotasEsperadas - notasLancadas;

  return (
    <div className="p-8">
      <div className="mb-8">
        <h1>Dashboard</h1>
        <p className="text-muted-foreground">Visão geral do sistema QNota</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle>Alunos Ativos</CardTitle>
            <Users className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl">{alunosAtivos}</div>
            <p className="text-muted-foreground mt-1">de {alunos.length} total</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle>Responsáveis</CardTitle>
            <Users className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl">{responsaveis.length}</div>
            <p className="text-muted-foreground mt-1">cadastrados</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle>Professores</CardTitle>
            <GraduationCap className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl">{professores.length}</div>
            <p className="text-muted-foreground mt-1">ativos</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle>Turmas Ativas</CardTitle>
            <BookOpen className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl">{turmasAtivas}</div>
            <p className="text-muted-foreground mt-1">de {turmas.length} total</p>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle>Próximos Simulados</CardTitle>
          </CardHeader>
          <CardContent>
            {proximosSimulados.length === 0 ? (
              <p className="text-muted-foreground">Nenhum simulado agendado</p>
            ) : (
              <div className="space-y-3">
                {proximosSimulados.map((sim, index) => (
                  <div key={index} className="flex items-center justify-between p-3 border rounded-lg">
                    <div className="flex items-center gap-3">
                      <Calendar className="size-4 text-muted-foreground" />
                      <div>
                        <p className="font-medium">{formatDate(sim.data)}</p>
                        <p className="text-sm text-muted-foreground">Em edição</p>
                      </div>
                    </div>
                    <Badge variant="secondary">Em Edição</Badge>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Notas Pendentes</CardTitle>
          </CardHeader>
          <CardContent>
            {notasPendentes > 0 ? (
              <Alert>
                <AlertCircle className="size-4" />
                <AlertDescription>
                  {notasPendentes} nota(s) pendente(s) de lançamento
                </AlertDescription>
              </Alert>
            ) : (
              <p className="text-muted-foreground">Todas as notas foram lançadas</p>
            )}
            <div className="mt-4">
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm text-muted-foreground">Progresso</span>
                <span className="text-sm font-medium">
                  {notasLancadas} / {totalNotasEsperadas}
                </span>
              </div>
              <div className="w-full bg-muted rounded-full h-2">
                <div
                  className="bg-primary h-2 rounded-full transition-all"
                  style={{
                    width: `${totalNotasEsperadas > 0 ? (notasLancadas / totalNotasEsperadas) * 100 : 0}%`
                  }}
                />
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

