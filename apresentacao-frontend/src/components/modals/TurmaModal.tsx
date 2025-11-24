import React from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from '../ui/dialog';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/select';

interface Turma {
  id: string;
  nome: string;
}

interface Professor {
  id: string;
  nome: string;
}

interface TurmaModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  isEdit?: boolean;
  formData: {
    nome: string;
    anoLetivo: number;
    professorId: string;
  };
  professores: Professor[];
  onFormDataChange: (data: { nome: string; anoLetivo: number; professorId: string }) => void;
  onSave: () => void;
  onCancel: () => void;
}

export function TurmaModal({
  open,
  onOpenChange,
  isEdit = false,
  formData,
  professores,
  onFormDataChange,
  onSave,
  onCancel
}: TurmaModalProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Editar Turma' : 'Nova Turma'}</DialogTitle>
          <DialogDescription>
            {isEdit ? 'Atualize as informações da turma' : 'Cadastre uma nova turma no sistema'}
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <div>
            <Label>Nome da Turma *</Label>
            <Input
              value={formData.nome}
              onChange={(e) => onFormDataChange({ ...formData, nome: e.target.value })}
              placeholder="Ex: 1º A – 2025"
            />
            <p className="text-muted-foreground mt-1">O nome deve ser único por ano letivo</p>
          </div>
          <div>
            <Label>Ano Letivo *</Label>
            <Input
              type="number"
              value={formData.anoLetivo}
              onChange={(e) => onFormDataChange({ ...formData, anoLetivo: parseInt(e.target.value) || new Date().getFullYear() })}
              min="2020"
              max="2099"
            />
          </div>
          <div>
            <Label>Professor Responsável *</Label>
            <Select value={formData.professorId} onValueChange={(v) => onFormDataChange({ ...formData, professorId: v })}>
              <SelectTrigger>
                <SelectValue placeholder="Selecione um professor" />
              </SelectTrigger>
              <SelectContent>
                {professores.map(prof => (
                  <SelectItem key={prof.id} value={prof.id}>{prof.nome}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={onCancel}>Cancelar</Button>
          <Button onClick={onSave}>Salvar</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}


