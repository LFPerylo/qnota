import React, { useState } from 'react';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Badge } from '../components/ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../components/ui/table';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../components/ui/select';
import { EmptyState } from '../components/EmptyState';
import { Plus, Search, Users, Eye, Edit, Ban, Trash2 } from 'lucide-react';
import {
  AlunoModal,
  AlunoDetailModal,
  AlunoDeleteModal
} from '../components/modals';

interface Aluno {
  id: string;
  nome: string;
  dataNascimento: string | Date;
  turmaId: string;
  status: string;
  responsaveis: Array<{ responsavelId: string; principal: boolean }>;
}

interface Turma {
  id: string;
  nome: string;
  status: string;
}

interface Responsavel {
  id: string;
  nome: string;
  email: string;
  inadimplente: boolean;
}

interface AlunosProps {
  alunos: Aluno[];
  turmas: Turma[];
  responsaveis: Responsavel[];
  formatDate: (date: string | Date) => string;
  onSave?: (dto: { nome: string; dataNascimento: string; turmaId: number; responsaveis?: number[]; responsavelPrincipalId?: number }) => Promise<void>;
  onUpdate?: (id: number, dto: { nome?: string; dataNascimento?: string; turmaId?: number }) => Promise<void>;
  onViewDetail?: (aluno: Aluno) => void;
  onInativar?: (aluno: Aluno) => void;
  onDelete?: (aluno: Aluno) => void;
}

export function Alunos({
  alunos,
  turmas,
  responsaveis,
  formatDate,
  onSave,
  onUpdate,
  onViewDetail,
  onInativar,
  onDelete
}: AlunosProps) {
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<'TODOS' | 'ATIVO' | 'INATIVO'>('TODOS');
  const [turmaFilter, setTurmaFilter] = useState<string>('TODAS');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [detailDialogOpen, setDetailDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [selectedAluno, setSelectedAluno] = useState<Aluno | null>(null);
  
  const [formData, setFormData] = useState({
    nome: '',
    dataNascimento: '',
    turmaId: '',
    responsaveis: [] as { responsavelId: string; principal: boolean }[]
  });

  const filteredAlunos = alunos.filter(aluno => {
    const matchesSearch = aluno.nome.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = statusFilter === 'TODOS' || aluno.status === statusFilter;
    const matchesTurma = turmaFilter === 'TODAS' || aluno.turmaId === turmaFilter;
    return matchesSearch && matchesStatus && matchesTurma;
  });

  const handleOpenDialog = (aluno?: Aluno) => {
    if (aluno) {
      setSelectedAluno(aluno);
      setFormData({
        nome: aluno.nome,
        dataNascimento: formatDate(aluno.dataNascimento),
        turmaId: aluno.turmaId,
        responsaveis: aluno.responsaveis
      });
    } else {
      setSelectedAluno(null);
      setFormData({
        nome: '',
        dataNascimento: '',
        turmaId: '',
        responsaveis: []
      });
    }
    setDialogOpen(true);
  };

  const handleViewDetail = (aluno: Aluno) => {
    setSelectedAluno(aluno);
    setDetailDialogOpen(true);
    onViewDetail?.(aluno);
  };

  const handleOpenDeleteDialog = (aluno: Aluno) => {
    setSelectedAluno(aluno);
    setDeleteDialogOpen(true);
  };

  const handleInativar = (aluno: Aluno) => {
    onInativar?.(aluno);
  };

  const handleDelete = () => {
    if (selectedAluno) {
      onDelete?.(selectedAluno);
      setDeleteDialogOpen(false);
    }
  };

  const toggleResponsavel = (responsavelId: string) => {
    const existing = formData.responsaveis.find(r => r.responsavelId === responsavelId);
    if (existing) {
      setFormData({
        ...formData,
        responsaveis: formData.responsaveis.filter(r => r.responsavelId !== responsavelId)
      });
    } else {
      if (formData.responsaveis.length >= 3) return;
      setFormData({
        ...formData,
        responsaveis: [...formData.responsaveis, { responsavelId, principal: formData.responsaveis.length === 0 }]
      });
    }
  };

  const setPrincipal = (responsavelId: string) => {
    setFormData({
      ...formData,
      responsaveis: formData.responsaveis.map(r => ({
        ...r,
        principal: r.responsavelId === responsavelId
      }))
    });
  };

  const handleSave = async () => {
    // Validações
    if (!formData.nome.trim()) {
      alert('Nome do aluno é obrigatório');
      return;
    }
    if (!formData.dataNascimento) {
      alert('Data de nascimento é obrigatória');
      return;
    }
    if (!formData.turmaId) {
      alert('Turma é obrigatória');
      return;
    }
    
    // Validar responsáveis (máximo 3, pelo menos 1 principal)
    if (formData.responsaveis.length > 3) {
      alert('Máximo de 3 responsáveis permitidos');
      return;
    }
    const hasPrincipal = formData.responsaveis.some(r => r.principal);
    if (formData.responsaveis.length > 0 && !hasPrincipal) {
      alert('Deve haver pelo menos um responsável principal');
      return;
    }

    try {
      // Converter data de DD/MM/YYYY para YYYY-MM-DD
      const [dia, mes, ano] = formData.dataNascimento.split('/');
      const dataNascimentoISO = `${ano}-${mes}-${dia}`;
      
      const turmaIdNum = parseInt(formData.turmaId);
      if (isNaN(turmaIdNum)) {
        alert('Turma inválida');
        return;
      }

      if (selectedAluno) {
        // Editar - apenas nome e data de nascimento podem ser editados
        const updateDto: { nome?: string; dataNascimento?: string } = {};
        if (formData.nome.trim() !== selectedAluno.nome) {
          updateDto.nome = formData.nome.trim();
        }
        // Nota: Edição de data de nascimento pode não ser permitida pelo backend
        // Por enquanto, apenas atualizamos se necessário
        await onUpdate?.(parseInt(selectedAluno.id), updateDto);
      } else {
        // Criar
        const responsaveisIds = formData.responsaveis.map(r => parseInt(r.responsavelId));
        const principalId = formData.responsaveis.find(r => r.principal)?.responsavelId;
        
        await onSave?.({
          nome: formData.nome.trim(),
          dataNascimento: dataNascimentoISO,
          turmaId: turmaIdNum,
          responsaveis: responsaveisIds.length > 0 ? responsaveisIds : undefined,
          responsavelPrincipalId: principalId ? parseInt(principalId) : undefined
        });
      }
      
      setDialogOpen(false);
      // Limpar formulário
      setFormData({
        nome: '',
        dataNascimento: '',
        turmaId: '',
        responsaveis: []
      });
      setSelectedAluno(null);
    } catch (error: any) {
      console.error('Erro ao salvar aluno:', error);
      const errorMessage = error.message || 'Erro desconhecido';
      alert('Erro ao salvar aluno: ' + errorMessage);
    }
  };

  const turmaNome = selectedAluno ? turmas.find(t => t.id === selectedAluno.turmaId)?.nome || '' : '';
  const alunoResponsaveis = selectedAluno 
    ? responsaveis.filter(r => selectedAluno.responsaveis.some(resp => resp.responsavelId === r.id))
    : [];

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1>Alunos</h1>
          <p className="text-muted-foreground">Gerencie os alunos cadastrados</p>
        </div>
        <Button onClick={() => handleOpenDialog()}>
          <Plus className="size-4 mr-2" />
          Novo Aluno
        </Button>
      </div>

      <div className="bg-card rounded-lg border p-4 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <Label>Buscar</Label>
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-muted-foreground" />
              <Input
                placeholder="Nome do aluno..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-9"
              />
            </div>
          </div>
          <div>
            <Label>Status</Label>
            <Select value={statusFilter} onValueChange={(v: any) => setStatusFilter(v)}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="TODOS">Todos</SelectItem>
                <SelectItem value="ATIVO">Ativos</SelectItem>
                <SelectItem value="INATIVO">Inativos</SelectItem>
              </SelectContent>
            </Select>
          </div>
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
        </div>
      </div>

      {filteredAlunos.length === 0 ? (
        <EmptyState
          icon={Users}
          title="Nenhum aluno encontrado"
          description="Comece criando um novo aluno ou ajuste os filtros de busca"
          actionLabel="Novo Aluno"
          onAction={() => handleOpenDialog()}
        />
      ) : (
        <div className="bg-card rounded-lg border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Nome</TableHead>
                <TableHead>Data Nascimento</TableHead>
                <TableHead>Turma</TableHead>
                <TableHead>Responsáveis</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Ações</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredAlunos.map(aluno => {
                const turma = turmas.find(t => t.id === aluno.turmaId);
                const responsaveisAluno = responsaveis.filter(r => 
                  aluno.responsaveis.some(resp => resp.responsavelId === r.id)
                );

                return (
                  <TableRow key={aluno.id}>
                    <TableCell>{aluno.nome}</TableCell>
                    <TableCell>{formatDate(aluno.dataNascimento)}</TableCell>
                    <TableCell>{turma?.nome || '-'}</TableCell>
                    <TableCell>
                      <div className="flex flex-wrap gap-1">
                        {responsaveisAluno.map(resp => (
                          <Badge key={resp.id} variant="outline">
                            {resp.nome}
                            {aluno.responsaveis.find(r => r.responsavelId === resp.id)?.principal && ' (Principal)'}
                          </Badge>
                        ))}
                      </div>
                    </TableCell>
                    <TableCell>
                      <Badge variant={aluno.status === 'ATIVO' ? 'default' : 'secondary'}>
                        {aluno.status === 'ATIVO' ? 'Ativo' : 'Inativo'}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex items-center justify-end gap-2">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleViewDetail(aluno)}
                        >
                          <Eye className="size-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleOpenDialog(aluno)}
                        >
                          <Edit className="size-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleInativar(aluno)}
                        >
                          <Ban className="size-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleOpenDeleteDialog(aluno)}
                        >
                          <Trash2 className="size-4" />
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

      <AlunoModal
        open={dialogOpen}
        onOpenChange={setDialogOpen}
        isEdit={!!selectedAluno}
        formData={formData}
        turmas={turmas}
        responsaveis={responsaveis}
        onFormDataChange={setFormData}
        onToggleResponsavel={toggleResponsavel}
        onSetPrincipal={setPrincipal}
        onSave={handleSave}
        onCancel={() => setDialogOpen(false)}
      />

      {selectedAluno && (
        <>
          <AlunoDetailModal
            open={detailDialogOpen}
            onOpenChange={setDetailDialogOpen}
            aluno={{
              nome: selectedAluno.nome,
              dataNascimento: formatDate(selectedAluno.dataNascimento),
              turma: turmaNome,
              status: selectedAluno.status,
              responsaveis: alunoResponsaveis.map(resp => ({
                nome: resp.nome,
                email: resp.email,
                principal: selectedAluno.responsaveis.find(r => r.responsavelId === resp.id)?.principal || false,
                inadimplente: resp.inadimplente
              })),
              notas: []
            }}
          />

          <AlunoDeleteModal
            open={deleteDialogOpen}
            onOpenChange={setDeleteDialogOpen}
            alunoNome={selectedAluno.nome}
            onConfirm={handleDelete}
          />
        </>
      )}
    </div>
  );
}

