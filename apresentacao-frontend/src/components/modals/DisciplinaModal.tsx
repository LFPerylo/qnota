import React from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '../ui/dialog';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Alert, AlertDescription } from '../ui/alert';
import { AlertCircle } from 'lucide-react';

interface DisciplinaModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  isEdit?: boolean;
  showWarning?: boolean;
  formData: {
    nome: string;
    area: string;
  };
  onFormDataChange: (data: { nome: string; area: string }) => void;
  onSave: () => void;
  onCancel: () => void;
}

export function DisciplinaModal({
  open,
  onOpenChange,
  isEdit = false,
  showWarning = false,
  formData,
  onFormDataChange,
  onSave,
  onCancel
}: DisciplinaModalProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Editar Disciplina' : 'Nova Disciplina'}</DialogTitle>
        </DialogHeader>
        
        {isEdit && showWarning && (
          <Alert>
            <AlertCircle className="size-4" />
            <AlertDescription>
              Esta disciplina já foi utilizada em simulados finalizados. Ao salvar, uma nova versão será criada e a anterior será preservada.
            </AlertDescription>
          </Alert>
        )}

        <div className="space-y-4">
          <div>
            <Label>Nome *</Label>
            <Input
              value={formData.nome}
              onChange={(e) => onFormDataChange({ ...formData, nome: e.target.value })}
              placeholder="Ex: Matemática"
            />
          </div>
          <div>
            <Label>Área *</Label>
            <Input
              value={formData.area}
              onChange={(e) => onFormDataChange({ ...formData, area: e.target.value })}
              placeholder="Ex: Exatas"
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


