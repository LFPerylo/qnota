import React from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from '../ui/dialog';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/select';
import { DateInput } from '../inputs/DateInput';
import { Checkbox } from '../ui/checkbox';
import { Badge } from '../ui/badge';

interface Responsavel {
  id: string;
  nome: string;
  email: string;
  inadimplente: boolean;
}

interface Turma {
  id: string;
  nome: string;
  status: string;
}

interface AlunoModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  isEdit?: boolean;
  formData: {
    nome: string;
    dataNascimento: string;
    turmaId: string;
    responsaveis: { responsavelId: string; principal: boolean }[];
  };
  turmas: Turma[];
  responsaveis: Responsavel[];
  onFormDataChange: (data: {
    nome: string;
    dataNascimento: string;
    turmaId: string;
    responsaveis: { responsavelId: string; principal: boolean }[];
  }) => void;
  onToggleResponsavel: (responsavelId: string) => void;
  onSetPrincipal: (responsavelId: string) => void;
  onSave: () => void;
  onCancel: () => void;
}

export function AlunoModal({
  open,
  onOpenChange,
  isEdit = false,
  formData,
  turmas,
  responsaveis,
  onFormDataChange,
  onToggleResponsavel,
  onSetPrincipal,
  onSave,
  onCancel
}: AlunoModalProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Editar Aluno' : 'Novo Aluno'}</DialogTitle>
          <DialogDescription>
            {isEdit ? 'Atualize as informações do aluno' : 'Cadastre um novo aluno no sistema'}
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <div>
            <Label>Nome *</Label>
            <Input
              value={formData.nome}
              onChange={(e) => onFormDataChange({ ...formData, nome: e.target.value })}
              placeholder="Nome completo do aluno"
            />
          </div>
          <div>
            <Label>Data de Nascimento *</Label>
            <DateInput
              value={formData.dataNascimento}
              onChange={(v) => onFormDataChange({ ...formData, dataNascimento: v })}
            />
          </div>
          <div>
            <Label>Turma *</Label>
            <Select value={formData.turmaId} onValueChange={(v) => onFormDataChange({ ...formData, turmaId: v })}>
              <SelectTrigger>
                <SelectValue placeholder="Selecione uma turma" />
              </SelectTrigger>
              <SelectContent>
                {turmas.filter(t => t.status === 'ATIVO').map(turma => (
                  <SelectItem key={turma.id} value={turma.id}>{turma.nome}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div>
            <Label>Responsáveis * (1-3, sendo 1 principal)</Label>
            <div className="border rounded-lg p-4 space-y-2 max-h-60 overflow-y-auto">
              {responsaveis.map(resp => {
                const isSelected = formData.responsaveis.find(r => r.responsavelId === resp.id);
                const isPrincipal = isSelected?.principal;

                return (
                  <div key={resp.id} className="flex items-center justify-between p-2 hover:bg-accent rounded">
                    <div className="flex items-center gap-3">
                      <Checkbox
                        checked={!!isSelected}
                        onCheckedChange={() => onToggleResponsavel(resp.id)}
                      />
                      <div>
                        <p>{resp.nome}</p>
                        <p className="text-muted-foreground">{resp.email}</p>
                      </div>
                      {resp.inadimplente && (
                        <Badge variant="destructive">Inadimplente</Badge>
                      )}
                    </div>
                    {isSelected && (
                      <Button
                        variant={isPrincipal ? 'default' : 'outline'}
                        size="sm"
                        onClick={() => onSetPrincipal(resp.id)}
                      >
                        {isPrincipal ? 'Principal' : 'Tornar Principal'}
                      </Button>
                    )}
                  </div>
                );
              })}
            </div>
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


