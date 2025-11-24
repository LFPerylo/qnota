import React, { useState } from 'react';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../components/ui/table';
import { EmptyState } from '../components/EmptyState';
import { Plus, Search, GraduationCap, Edit, Trash } from 'lucide-react';
import { ProfessorModal } from '../components/modals';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from '../components/ui/dialog';

interface Professor {
  id: string;
  nome: string;
  email: string;
  cpf: string;
  especialidades?: string[];
}

interface ProfessoresProps {
  professores: Professor[];
  areasDisponiveis: string[];
  onSave?: (dto: { nome: string; email: string; cpf: string; especialidades?: string[] }) => Promise<void>;
  onUpdate?: (id: number, dto: { nome: string; email: string }) => Promise<void>;
  onDelete?: (id: number, substitutoId: number) => Promise<void>;
}

export function Professores({
  professores,
  areasDisponiveis,
  onSave,
  onUpdate,
  onDelete
}: ProfessoresProps) {
  const [searchTerm, setSearchTerm] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedProfessor, setSelectedProfessor] = useState<Professor | null>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [professorToDelete, setProfessorToDelete] = useState<Professor | null>(null);
  const [substituteId, setSubstituteId] = useState('');
  
  const [formData, setFormData] = useState({
    nome: '',
    email: '',
    cpf: '',
    especialidades: [] as string[],
    novaEspecialidade: ''
  });

  const filteredProfessores = professores.filter(prof =>
    prof.nome.toLowerCase().includes(searchTerm.toLowerCase()) ||
    prof.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
    prof.cpf.includes(searchTerm.replace(/\D/g, '')) ||
    (prof.especialidades || []).some(esp => esp.toLowerCase().includes(searchTerm.toLowerCase()))
  );

  const handleOpenDialog = (prof?: Professor) => {
    if (prof) {
      setSelectedProfessor(prof);
      // CPF já vem apenas com números do backend, mas o CPFInput formata na exibição
      setFormData({
        nome: prof.nome,
        email: prof.email,
        cpf: prof.cpf.replace(/\D/g, ''),
        especialidades: prof.especialidades || [],
        novaEspecialidade: ''
      });
    } else {
      setSelectedProfessor(null);
      setFormData({
        nome: '',
        email: '',
        cpf: '',
        especialidades: [],
        novaEspecialidade: ''
      });
    }
    setDialogOpen(true);
  };

  const handleDelete = (prof: Professor) => {
    const substitutes = professores.filter(p => p.id !== prof.id);
    if (substitutes.length === 0) {
      alert('Cadastre outro professor para servir como substituto antes de remover este.');
      return;
    }
    setProfessorToDelete(prof);
    setSubstituteId(substitutes[0].id);
    setDeleteDialogOpen(true);
  };

  const confirmDelete = async () => {
    if (!professorToDelete || !substituteId) return;
    try {
      await onDelete?.(parseInt(professorToDelete.id), parseInt(substituteId));
      setDeleteDialogOpen(false);
      setProfessorToDelete(null);
      setSubstituteId('');
    } catch (error: any) {
      console.error('Erro ao remover professor:', error);
      alert('Erro ao remover professor: ' + (error.message || 'Erro desconhecido'));
    }
  };

  const handleSave = async () => {
    // Validações básicas
    if (!formData.nome.trim()) {
      alert('Nome é obrigatório');
      return;
    }
    if (!formData.email.trim()) {
      alert('E-mail é obrigatório');
      return;
    }
    const cpfLimpo = formData.cpf.replace(/\D/g, '');
    if (!cpfLimpo || cpfLimpo.length !== 11) {
      alert('CPF inválido');
      return;
    }

    try {
      const especialidadesSelecionadas = formData.especialidades.length > 0
        ? formData.especialidades
        : (formData.novaEspecialidade.trim() ? [formData.novaEspecialidade.trim()] : []);

      if (!selectedProfessor && especialidadesSelecionadas.length === 0) {
        alert('Selecione ou informe ao menos uma especialidade compatível com as disciplinas.');
        return;
      }

      if (selectedProfessor) {
        // Editar
        await onUpdate?.(parseInt(selectedProfessor.id), {
          nome: formData.nome.trim(),
          email: formData.email.trim()
        });
      } else {
        // Criar - formato esperado pelo backend
        const dto = {
          nome: formData.nome.trim(),
          email: formData.email.trim(),
          cpf: cpfLimpo,
          especialidades: especialidadesSelecionadas
        };
        await onSave?.(dto);
      }
      setDialogOpen(false);
      // Limpar formulário
      setFormData({
        nome: '',
        email: '',
        cpf: '',
        especialidades: [],
        novaEspecialidade: ''
      });
      setSelectedProfessor(null);
    } catch (error: any) {
      console.error('Erro ao salvar professor:', error);
      const errorMessage = error.message || 'Erro desconhecido';
      alert('Erro ao salvar professor: ' + errorMessage);
    }
  };

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1>Professores</h1>
          <p className="text-muted-foreground">Gerencie os professores cadastrados</p>
        </div>
        <Button onClick={() => handleOpenDialog()}>
          <Plus className="size-4 mr-2" />
          Novo Professor
        </Button>
      </div>

      <div className="bg-card rounded-lg border p-4 mb-6">
        <Label>Buscar</Label>
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-muted-foreground" />
          <Input
            placeholder="Nome, e-mail ou CPF..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pl-9"
          />
        </div>
      </div>

      {filteredProfessores.length === 0 ? (
        <EmptyState
          icon={GraduationCap}
          title="Nenhum professor encontrado"
          description="Comece criando um novo professor ou ajuste o filtro de busca"
          actionLabel="Novo Professor"
          onAction={() => handleOpenDialog()}
        />
      ) : (
        <div className="bg-card rounded-lg border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Nome</TableHead>
                <TableHead>E-mail</TableHead>
                <TableHead>CPF</TableHead>
            <TableHead>Especialidades</TableHead>
            <TableHead className="text-right">Ações</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredProfessores.map(prof => (
                <TableRow key={prof.id}>
                  <TableCell>{prof.nome}</TableCell>
                  <TableCell>{prof.email}</TableCell>
                  <TableCell>{prof.cpf}</TableCell>
                  <TableCell>
                    {prof.especialidades?.length ? prof.especialidades.join(', ') : '—'}
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex items-center justify-end gap-2">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleOpenDialog(prof)}
                      >
                        <Edit className="size-4" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleDelete(prof)}
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

      <ProfessorModal
        open={dialogOpen}
        onOpenChange={setDialogOpen}
        isEdit={!!selectedProfessor}
        formData={formData}
        areasDisponiveis={areasDisponiveis}
        onFormDataChange={setFormData}
        onSave={handleSave}
        onCancel={() => setDialogOpen(false)}
      />

      <Dialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Remover professor</DialogTitle>
            <DialogDescription>
              Escolha um professor substituto para assumir as turmas de {professorToDelete?.nome}.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-3">
            <Label>Professor substituto</Label>
            <select
              className="w-full border rounded-md px-3 py-2 bg-background"
              value={substituteId}
              onChange={(e) => setSubstituteId(e.target.value)}
            >
              {professores
                .filter(p => p.id !== professorToDelete?.id)
                .map(p => (
                  <option key={p.id} value={p.id}>{p.nome}</option>
                ))}
            </select>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDeleteDialogOpen(false)}>Cancelar</Button>
            <Button variant="destructive" onClick={confirmDelete}>Confirmar remoção</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

