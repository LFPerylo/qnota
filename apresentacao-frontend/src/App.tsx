import React, { useState } from 'react';
import { DataProvider, useData } from './contexts/DataContext';
import { Dashboard } from './pages/Dashboard';
import { Alunos } from './pages/Alunos';
import { Turmas } from './pages/Turmas';
import { Professores } from './pages/Professores';
import { Disciplinas } from './pages/Disciplinas';
import { Responsaveis } from './pages/Responsaveis';
import { Simulados } from './pages/Simulados';
import { Auditoria } from './pages/Auditoria';
import { Configuracoes } from './pages/Configuracoes';
import { 
  LayoutDashboard, 
  Users, 
  UserCircle, 
  GraduationCap, 
  BookOpen, 
  BookText, 
  ClipboardList,
  FileText,
  Settings,
  Menu,
  X
} from 'lucide-react';
import { Button } from './components/ui/button';
import { formatDate, formatDateTime, formatNumber } from './lib/utils-br';

type Page = 
  | 'dashboard' 
  | 'alunos' 
  | 'responsaveis' 
  | 'professores' 
  | 'turmas' 
  | 'disciplinas' 
  | 'simulados'
  | 'auditoria'
  | 'configuracoes';

const menuItems = [
  { id: 'dashboard' as Page, label: 'Dashboard', icon: LayoutDashboard },
  { id: 'alunos' as Page, label: 'Alunos', icon: Users },
  { id: 'responsaveis' as Page, label: 'Responsáveis', icon: UserCircle },
  { id: 'professores' as Page, label: 'Professores', icon: GraduationCap },
  { id: 'turmas' as Page, label: 'Turmas', icon: BookOpen },
  { id: 'disciplinas' as Page, label: 'Disciplinas', icon: BookText },
  { id: 'simulados' as Page, label: 'Simulados', icon: ClipboardList },
  { id: 'auditoria' as Page, label: 'Auditoria', icon: FileText },
  { id: 'configuracoes' as Page, label: 'Configurações', icon: Settings },
];

function AppContent() {
  const [currentPage, setCurrentPage] = useState<Page>('dashboard');
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const data = useData();

  const renderPage = () => {
    switch (currentPage) {
      case 'dashboard': {
        const dashboardAlunos = data.alunos.map((a: any) => ({
          ...a,
          status: a.ativo ? 'ATIVO' : 'INATIVO',
          turmaId: a.turmaId?.toString() || ''
        }));

        const dashboardTurmas = data.turmas.map((t: any) => ({
          ...t,
          status: t.ativo ? 'ATIVO' : 'INATIVO'
        }));

        return <Dashboard 
          alunos={dashboardAlunos}
          responsaveis={data.responsaveis}
          professores={data.professores}
          turmas={dashboardTurmas}
          simulados={data.simulados}
          notas={data.notas}
          formatDate={formatDate}
        />;
      }
      case 'alunos':
        return <Alunos 
          alunos={data.alunos.map((a: any) => ({
            id: a.id?.toString() || '',
            nome: a.nome || '',
            dataNascimento: a.dataNascimento || '',
            turmaId: a.turmaId?.toString() || '',
            status: a.ativo ? 'ATIVO' : 'INATIVO',
            responsaveis: [] // Será preenchido pelo backend se necessário
          }))}
          turmas={data.turmas.map((t: any) => ({
            id: t.id?.toString() || '',
            nome: t.nome || '',
            status: t.ativo ? 'ATIVO' : 'INATIVO'
          }))}
          responsaveis={data.responsaveis.map((r: any) => ({
            id: r.id?.toString() || '',
            nome: r.nome || '',
            email: r.email || '',
            inadimplente: r.status === 'INADIMPLENTE'
          }))}
          formatDate={formatDate}
          onSave={async (dto) => {
            await data.addAluno(dto);
          }}
          onUpdate={async (id, dto) => {
            await data.updateAluno(id, dto);
          }}
          onViewDetail={(aluno) => {}}
          onInativar={(aluno) => data.inativarAluno(parseInt(aluno.id), aluno.status === 'ATIVO')}
          onDelete={(aluno) => data.deleteAluno(parseInt(aluno.id))}
        />;
      case 'turmas':
        return <Turmas 
          turmas={data.turmas.map((t: any) => ({
            id: t.id?.toString() || '',
            nome: t.nome || '',
            anoLetivo: t.anoLetivo || new Date().getFullYear(),
            professorId: t.professorId?.toString() || '',
            status: t.ativo ? 'ATIVO' : 'INATIVO'
          }))}
          professores={data.professores.map((p: any) => ({
            id: p.id?.toString() || '',
            nome: p.nome || ''
          }))}
          onSave={async (dto) => {
            await data.addTurma(dto);
          }}
          onUpdate={async (id, dto) => {
            await data.updateTurma(id, dto);
          }}
          onInativar={(turma) => data.inativarTurma(parseInt(turma.id), turma.status === 'ATIVO')}
          onDelete={(turma) => data.deleteTurma(parseInt(turma.id))}
        />;
      case 'professores': {
        const areaOptions = Array.from(
          new Set(
            data.disciplinas
              .map((d: any) => d.areaNome)
              .filter((nome: any) => typeof nome === 'string' && nome.trim().length > 0)
          )
        );

        const professores = data.professores.map((p: any) => {
          // Especialidades agora vem como array do backend
          let especialidades: string[] = [];
          if (Array.isArray(p.especialidades)) {
            especialidades = p.especialidades.filter((e: any) => typeof e === 'string');
          }
          return {
            id: p.id?.toString() || '',
            nome: p.nome || '',
            email: p.email || '',
            cpf: p.cpf || '',
            especialidades
          };
        });

        return <Professores 
          professores={professores}
          areasDisponiveis={areaOptions}
          onSave={async (dto) => {
            await data.addProfessor(dto);
          }}
          onUpdate={async (id, dto) => {
            await data.updateProfessor(id, dto);
          }}
          onDelete={async (id, substitutoId) => {
            await data.deleteProfessor(id, substitutoId);
          }}
        />;
      }
      case 'disciplinas':
        return <Disciplinas 
          disciplinas={data.disciplinas.map((d: any) => ({
            id: d.id?.toString() || '',
            nome: d.nome || '',
            area: d.areaNome || ''
          }))}
          onSave={async (dto) => {
            await data.addDisciplina(dto);
          }}
          onUpdate={async (id, dto) => {
            await data.updateDisciplina(id, dto);
          }}
          onDelete={(disc) => data.deleteDisciplina(parseInt(disc.id))}
        />;
      case 'responsaveis':
        return <Responsaveis 
          responsaveis={data.responsaveis.map((r: any) => ({
            id: r.id?.toString() || '',
            nome: r.nome || '',
            email: r.email || '',
            cpf: r.cpf || '',
            inadimplente: r.status === 'INADIMPLENTE'
          }))}
          onSave={async (dto) => {
            await data.addResponsavel(dto);
          }}
          onUpdate={async (id, dto) => {
            await data.updateResponsavel(id, dto);
          }}
          onDelete={(resp) => data.deleteResponsavel(parseInt(resp.id))}
        />;
      case 'simulados':
        return <Simulados 
          simulados={data.simulados.map((s: any) => ({
            id: s.id?.toString() || '',
            turmaId: s.turmaId?.toString() || '',
            data: s.dataAplicacao || '',
            status: s.status || 'EM_EDICAO',
            disciplinas: (s.disciplinas || []).map((d: any) => ({
              disciplinaId: (d.disciplinaId ?? d.id)?.toString() || '',
              peso: d.peso ?? d.pesoPercentual ?? 0
            }))
          }))}
          fetchSimuladoDetalhe={async (id) => data.getSimuladoDetalhe(id)}
          turmas={data.turmas.map((t: any) => ({
            id: t.id?.toString() || '',
            nome: t.nome || ''
          }))}
          disciplinas={data.disciplinas.map((d: any) => ({
            id: d.id?.toString() || '',
            nome: d.nome || ''
          }))}
          alunos={data.alunos.map((a: any) => ({
            id: a.id?.toString() || '',
            nome: a.nome || '',
            turmaId: a.turmaId?.toString() || '',
            status: a.ativo ? 'ATIVO' : 'INATIVO',
            dataNascimento: a.dataNascimento || ''
          }))}
          notas={data.notas}
          formatDate={formatDate}
          formatNumber={formatNumber}
          calcularMedia={data.calcularMedia}
          onSave={async (dto) => {
            await data.addSimulado(dto);
          }}
          onUpdate={async (id, dto) => {
            await data.updateSimulado(id, dto);
          }}
          onDelete={(sim) => data.deleteSimulado(parseInt(sim.id))}
          professores={data.professores}
          onLancarNotas={(sim) => {}}
          onVerRanking={(sim) => {}}
          onRetificarNota={(nota) => {}}
          onFinalizar={async (id) => {
            await data.finalizarSimulado(id);
          }}
          onLancarNota={async (alunoId, dto) => {
            await data.lancarNota(alunoId, dto);
          }}
          onRetificarNotaFunc={async (alunoId, dto) => {
            await data.retificarNota(alunoId, dto);
          }}
        />;
      case 'auditoria':
        return <Auditoria 
          notas={data.notas}
          alunos={data.alunos}
          turmas={data.turmas}
          simulados={data.simulados}
          disciplinas={data.disciplinas}
          formatDateTime={formatDateTime}
          formatNumber={formatNumber}
        />;
      case 'configuracoes':
        return <Configuracoes />;
      default: {
        const dashboardAlunos = data.alunos.map((a: any) => ({
          ...a,
          status: a.ativo ? 'ATIVO' : 'INATIVO',
          turmaId: a.turmaId?.toString() || ''
        }));

        const dashboardTurmas = data.turmas.map((t: any) => ({
          ...t,
          status: t.ativo ? 'ATIVO' : 'INATIVO'
        }));

        return <Dashboard 
          alunos={dashboardAlunos}
          responsaveis={data.responsaveis}
          professores={data.professores}
          turmas={dashboardTurmas}
          simulados={data.simulados}
          notas={data.notas}
          formatDate={formatDate}
        />;
      }
    }
  };

  return (
    <div className="flex h-screen bg-background">
      {/* Sidebar */}
      <div className={`${sidebarOpen ? 'w-64' : 'w-0'} transition-all duration-300 border-r bg-card overflow-hidden`}>
        <div className="p-4">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-xl font-bold">QNota</h2>
            <Button variant="ghost" size="icon" onClick={() => setSidebarOpen(false)}>
              <X className="size-4" />
            </Button>
          </div>
          <nav className="space-y-1">
            {menuItems.map((item) => {
              const Icon = item.icon;
              return (
                <button
                  key={item.id}
                  onClick={() => setCurrentPage(item.id)}
                  className={`w-full flex items-center gap-3 px-3 py-2 rounded-md text-sm transition-colors ${
                    currentPage === item.id
                      ? 'bg-primary text-primary-foreground'
                      : 'hover:bg-accent'
                  }`}
                >
                  <Icon className="size-4" />
                  <span>{item.label}</span>
                </button>
              );
            })}
          </nav>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 overflow-auto relative">
        {/* Botão para abrir sidebar quando fechada */}
        {!sidebarOpen && (
          <Button 
            variant="outline" 
            size="icon" 
            onClick={() => setSidebarOpen(true)}
            className="fixed top-4 left-4 z-50 shadow-md"
          >
            <Menu className="size-4" />
          </Button>
        )}
        {renderPage()}
      </div>
    </div>
  );
}

function App() {
  return (
    <DataProvider>
      <AppContent />
    </DataProvider>
  );
}

export default App;

