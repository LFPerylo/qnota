import React, { useState } from 'react';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../components/ui/table';
import { EmptyState } from '../components/EmptyState';
import { Plus, Search, BookText, Edit, Trash } from 'lucide-react';
import { DisciplinaModal } from '../components/modals';

interface Disciplina {
  id: string;
  nome: string;
  area: string;
}

interface DisciplinasProps {
  disciplinas: Disciplina[];
  onSave?: (dto: { nome: string; area: string }) => Promise<void>;
  onUpdate?: (id: number, dto: { nome: string; area: string }) => Promise<void>;
  onDelete?: (disciplina: Disciplina) => void;
  getDisciplinaUsage?: (disciplina: Disciplina) => number;
}

export function Disciplinas({
  disciplinas,
  onSave,
  onUpdate,
  onDelete,
  getDisciplinaUsage
}: DisciplinasProps) {
  const [searchTerm, setSearchTerm] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedDisciplina, setSelectedDisciplina] = useState<Disciplina | null>(null);
  
  const [formData, setFormData] = useState({
    nome: '',
    area: ''
  });

  const filteredDisciplinas = disciplinas.filter(disc =>
    disc.nome.toLowerCase().includes(searchTerm.toLowerCase()) ||
    disc.area.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleOpenDialog = (disc?: Disciplina) => {
    if (disc) {
      setSelectedDisciplina(disc);
      setFormData({
        nome: disc.nome,
        area: disc.area
      });
    } else {
      setSelectedDisciplina(null);
      setFormData({
        nome: '',
        area: ''
      });
    }
    setDialogOpen(true);
  };

  const handleDelete = (disc: Disciplina) => {
    onDelete?.(disc);
  };

  const handleSave = async () => {
    // Validações
    if (!formData.nome.trim()) {
      alert('Nome da disciplina é obrigatório');
      return;
    }
    if (!formData.area.trim()) {
      alert('Área de conhecimento é obrigatória');
      return;
    }

    try {
      if (selectedDisciplina) {
        // Editar
        await onUpdate?.(parseInt(selectedDisciplina.id), {
          nome: formData.nome.trim(),
          area: formData.area.trim()
        });
      } else {
        // Criar
        await onSave?.({
          nome: formData.nome.trim(),
          area: formData.area.trim()
        });
      }
      setDialogOpen(false);
      // Limpar formulário
      setFormData({
        nome: '',
        area: ''
      });
      setSelectedDisciplina(null);
    } catch (error: any) {
      console.error('Erro ao salvar disciplina:', error);
      const errorMessage = error.message || 'Erro desconhecido';
      alert('Erro ao salvar disciplina: ' + errorMessage);
    }
  };

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1>Disciplinas</h1>
          <p className="text-muted-foreground">Gerencie as disciplinas cadastradas</p>
        </div>
        <Button onClick={() => handleOpenDialog()}>
          <Plus className="size-4 mr-2" />
          Nova Disciplina
        </Button>
      </div>

      <div className="bg-card rounded-lg border p-4 mb-6">
        <Label>Buscar</Label>
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-muted-foreground" />
          <Input
            placeholder="Nome ou área..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pl-9"
          />
        </div>
      </div>

      {filteredDisciplinas.length === 0 ? (
        <EmptyState
          icon={BookText}
          title="Nenhuma disciplina encontrada"
          description="Comece criando uma nova disciplina ou ajuste o filtro de busca"
          actionLabel="Nova Disciplina"
          onAction={() => handleOpenDialog()}
        />
      ) : (
        <div className="bg-card rounded-lg border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Nome</TableHead>
                <TableHead>Área</TableHead>
                <TableHead className="text-right">Ações</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredDisciplinas.map(disc => (
                <TableRow key={disc.id}>
                  <TableCell>{disc.nome}</TableCell>
                  <TableCell>{disc.area}</TableCell>
                  <TableCell className="text-right">
                    <div className="flex items-center justify-end gap-2">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleOpenDialog(disc)}
                      >
                        <Edit className="size-4" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleDelete(disc)}
                      >
                        <Trash className="size-4" />
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}

      <DisciplinaModal
        open={dialogOpen}
        onOpenChange={setDialogOpen}
        isEdit={!!selectedDisciplina}
        showWarning={selectedDisciplina ? (getDisciplinaUsage?.(selectedDisciplina) || 0) > 0 : false}
        formData={formData}
        onFormDataChange={setFormData}
        onSave={handleSave}
        onCancel={() => setDialogOpen(false)}
      />
    </div>
  );
}

