import React, { useState } from 'react';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Badge } from '../components/ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../components/ui/table';
import { EmptyState } from '../components/EmptyState';
import { Plus, Search, BookOpen, Edit, Ban, Trash, Check } from 'lucide-react';
import { TurmaModal } from '../components/modals';

interface Turma {
  id: string;
  nome: string;
  anoLetivo: number;
  professorId: string;
  status: string;
}

interface Professor {
  id: string;
  nome: string;
}

interface TurmasProps {
  turmas: Turma[];
  professores: Professor[];
  onSave?: (dto: { nome: string; anoLetivo: number; professorId: number }) => Promise<void>;
  onUpdate?: (id: number, dto: { nome?: string; professorId?: number }) => Promise<void>;
  onInativar?: (turma: Turma) => void;
  onDelete?: (turma: Turma) => void;
}

export function Turmas({
  turmas,
  professores,
  onSave,
  onUpdate,
  onInativar,
  onDelete
}: TurmasProps) {
  const [searchTerm, setSearchTerm] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedTurma, setSelectedTurma] = useState<Turma | null>(null);
  
  const [formData, setFormData] = useState({
    nome: '',
    anoLetivo: new Date().getFullYear(),
    professorId: ''
  });

  const filteredTurmas = turmas.filter(turma =>
    turma.nome.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleOpenDialog = (turma?: Turma) => {
    if (turma) {
      setSelectedTurma(turma);
      setFormData({
        nome: turma.nome,
        anoLetivo: turma.anoLetivo,
        professorId: turma.professorId
      });
    } else {
      setSelectedTurma(null);
      setFormData({
        nome: '',
        anoLetivo: new Date().getFullYear(),
        professorId: ''
      });
    }
    setDialogOpen(true);
  };

  const handleInativar = (turma: Turma) => {
    onInativar?.(turma);
  };

  const handleDelete = (turma: Turma) => {
    onDelete?.(turma);
  };

  const handleSave = async () => {
    // Validações
    if (!formData.nome.trim()) {
      alert('Nome da turma é obrigatório');
      return;
    }
    if (!formData.professorId || formData.professorId === '') {
      alert('Professor responsável é obrigatório');
      return;
    }
    if (formData.anoLetivo < 2020 || formData.anoLetivo > 2099) {
      alert('Ano letivo inválido');
      return;
    }

    try {
      const professorIdNum = typeof formData.professorId === 'string' 
        ? parseInt(formData.professorId) 
        : formData.professorId;
      
      if (isNaN(professorIdNum)) {
        alert('Professor inválido');
        return;
      }

      if (selectedTurma) {
        // Editar
        const updateDto: { nome?: string; professorId?: number } = {};
        if (formData.nome.trim() !== selectedTurma.nome) {
          updateDto.nome = formData.nome.trim();
        }
        if (professorIdNum !== parseInt(selectedTurma.professorId)) {
          updateDto.professorId = professorIdNum;
        }
        
        if (Object.keys(updateDto).length > 0) {
          await onUpdate?.(parseInt(selectedTurma.id), updateDto);
        }
      } else {
        // Criar
        await onSave?.({
          nome: formData.nome.trim(),
          anoLetivo: formData.anoLetivo,
          professorId: professorIdNum
        });
      }
      setDialogOpen(false);
      // Limpar formulário
      setFormData({
        nome: '',
        anoLetivo: new Date().getFullYear(),
        professorId: ''
      });
      setSelectedTurma(null);
    } catch (error: any) {
      console.error('Erro ao salvar turma:', error);
      const errorMessage = error.message || 'Erro desconhecido';
      alert('Erro ao salvar turma: ' + errorMessage);
    }
  };

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1>Turmas</h1>
          <p className="text-muted-foreground">Gerencie as turmas da instituição</p>
        </div>
        <Button onClick={() => handleOpenDialog()}>
          <Plus className="size-4 mr-2" />
          Nova Turma
        </Button>
      </div>

      <div className="bg-card rounded-lg border p-4 mb-6">
        <Label>Buscar</Label>
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-muted-foreground" />
          <Input
            placeholder="Nome da turma..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pl-9"
          />
        </div>
      </div>

      {filteredTurmas.length === 0 ? (
        <EmptyState
          icon={BookOpen}
          title="Nenhuma turma encontrada"
          description="Comece criando uma nova turma ou ajuste o filtro de busca"
          actionLabel="Nova Turma"
          onAction={() => handleOpenDialog()}
        />
      ) : (
        <div className="bg-card rounded-lg border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Nome</TableHead>
                <TableHead>Ano Letivo</TableHead>
                <TableHead>Professor</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Ações</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredTurmas.map(turma => {
                const professor = professores.find(p => p.id === turma.professorId);

                return (
                  <TableRow key={turma.id}>
                    <TableCell>{turma.nome}</TableCell>
                    <TableCell>{turma.anoLetivo}</TableCell>
                    <TableCell>{professor?.nome || '-'}</TableCell>
                    <TableCell>
                      <Badge variant={turma.status === 'ATIVO' ? 'default' : 'secondary'}>
                        {turma.status === 'ATIVO' ? 'Ativa' : 'Inativa'}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex items-center justify-end gap-2">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleOpenDialog(turma)}
                        >
                          <Edit className="size-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleInativar(turma)}
                          title={turma.status === 'ATIVO' ? 'Inativar turma' : 'Ativar turma'}
                        >
                          {turma.status === 'ATIVO' ? <Ban className="size-4" /> : <Check className="size-4" />}
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleDelete(turma)}
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

      <TurmaModal
        open={dialogOpen}
        onOpenChange={setDialogOpen}
        isEdit={!!selectedTurma}
        formData={formData}
        professores={professores}
        onFormDataChange={setFormData}
        onSave={handleSave}
        onCancel={() => setDialogOpen(false)}
      />
    </div>
  );
}

