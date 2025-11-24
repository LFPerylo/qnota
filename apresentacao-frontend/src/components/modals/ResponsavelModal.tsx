import React from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from '../ui/dialog';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { CPFInput } from '../inputs/CPFInput';
import { Switch } from '../ui/switch';

interface ResponsavelModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  isEdit?: boolean;
  formData: {
    nome: string;
    email: string;
    cpf: string;
    inadimplente: boolean;
  };
  onFormDataChange: (data: { nome: string; email: string; cpf: string; inadimplente: boolean }) => void;
  onSave: () => void;
  onCancel: () => void;
}

export function ResponsavelModal({
  open,
  onOpenChange,
  isEdit = false,
  formData,
  onFormDataChange,
  onSave,
  onCancel
}: ResponsavelModalProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Editar Responsável' : 'Novo Responsável'}</DialogTitle>
          <DialogDescription>
            {isEdit ? 'Atualize as informações do responsável' : 'Cadastre um novo responsável no sistema'}
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <div>
            <Label>Nome *</Label>
            <Input
              value={formData.nome}
              onChange={(e) => onFormDataChange({ ...formData, nome: e.target.value })}
              placeholder="Nome completo"
            />
          </div>
          <div>
            <Label>E-mail *</Label>
            <Input
              type="email"
              value={formData.email}
              onChange={(e) => onFormDataChange({ ...formData, email: e.target.value })}
              placeholder="email@exemplo.com"
            />
          </div>
          <div>
            <Label>CPF * {isEdit && '(imutável)'}</Label>
            <CPFInput
              value={formData.cpf}
              onChange={(v) => onFormDataChange({ ...formData, cpf: v })}
              disabled={isEdit}
            />
            {isEdit && (
              <p className="text-muted-foreground mt-1">CPF não pode ser alterado após cadastro</p>
            )}
          </div>
          <div className="flex items-center justify-between">
            <Label>Inadimplente</Label>
            <Switch
              checked={formData.inadimplente}
              onCheckedChange={(checked) => onFormDataChange({ ...formData, inadimplente: checked })}
            />
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


