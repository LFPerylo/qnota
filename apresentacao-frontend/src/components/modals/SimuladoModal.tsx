import React from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from '../ui/dialog';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/select';
import { DateInput } from '../inputs/DateInput';
import { NumberInputBR } from '../inputs/NumberInputBR';
import { Trash2 } from 'lucide-react';

interface Disciplina {
  id: string;
  nome: string;
}

interface Turma {
  id: string;
  nome: string;
}

interface SimuladoModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  isEdit?: boolean;
  formData: {
    turmaId: string;
    data: string;
    disciplinas: { disciplinaId: string; peso: number }[];
  };
  turmas: Turma[];
  disciplinas: Disciplina[];
  onFormDataChange: (data: {
    turmaId: string;
    data: string;
    disciplinas: { disciplinaId: string; peso: number }[];
  }) => void;
  onAddDisciplina: () => void;
  onRemoveDisciplina: (index: number) => void;
  onUpdateDisciplina: (index: number, disciplinaId: string, peso: number) => void;
  totalPeso: number;
  onSave: () => void;
  onCancel: () => void;
}

export function SimuladoModal({
  open,
  onOpenChange,
  isEdit = false,
  formData,
  turmas,
  disciplinas,
  onFormDataChange,
  onAddDisciplina,
  onRemoveDisciplina,
  onUpdateDisciplina,
  totalPeso,
  onSave,
  onCancel
}: SimuladoModalProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Editar Simulado' : 'Novo Simulado'}</DialogTitle>
          <DialogDescription>
            {isEdit ? 'Atualize as informações do simulado' : 'Cadastre um novo simulado no sistema'}
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <div>
            <Label>Turma *</Label>
            <Select value={formData.turmaId} onValueChange={(v) => onFormDataChange({ ...formData, turmaId: v })}>
              <SelectTrigger>
                <SelectValue placeholder="Selecione uma turma" />
              </SelectTrigger>
              <SelectContent>
                {turmas.map(turma => (
                  <SelectItem key={turma.id} value={turma.id}>{turma.nome}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div>
            <Label>Data de Aplicação *</Label>
            <DateInput
              value={formData.data}
              onChange={(v) => onFormDataChange({ ...formData, data: v })}
            />
          </div>

          <div>
            <div className="flex items-center justify-between mb-2">
              <Label>Disciplinas * (mín. 2, soma dos pesos = 10)</Label>
              <Button type="button" variant="outline" size="sm" onClick={onAddDisciplina}>
                Adicionar Disciplina
              </Button>
            </div>
            <div className="space-y-3 border rounded-lg p-4">
              {formData.disciplinas.map((disc, index) => (
                <div key={index} className="flex items-center gap-3">
                  <Select
                    value={disc.disciplinaId}
                    onValueChange={(v) => onUpdateDisciplina(index, v, disc.peso)}
                  >
                    <SelectTrigger className="flex-1">
                      <SelectValue placeholder="Selecione uma disciplina" />
                    </SelectTrigger>
                    <SelectContent>
                      {disciplinas.map(disciplina => (
                        <SelectItem key={disciplina.id} value={disciplina.id}>
                          {disciplina.nome}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <div className="w-32">
                    <NumberInputBR
                      value={disc.peso.toString().replace('.', ',')}
                      onChange={(v) => {
                        const peso = parseFloat(v.replace(',', '.')) || 0;
                        onUpdateDisciplina(index, disc.disciplinaId, peso);
                      }}
                      placeholder="Peso"
                      max={10}
                    />
                  </div>
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    onClick={() => onRemoveDisciplina(index)}
                  >
                    <Trash2 className="size-4" />
                  </Button>
                </div>
              ))}
              {formData.disciplinas.length === 0 && (
                <p className="text-sm text-muted-foreground text-center py-4">
                  Adicione pelo menos 2 disciplinas
                </p>
              )}
            </div>
            <div className="mt-2 flex items-center justify-between">
              <p className="text-sm text-muted-foreground">
                Total dos pesos: <span className={totalPeso === 10 ? 'text-green-600 font-semibold' : 'text-destructive font-semibold'}>{totalPeso.toFixed(1)}</span> / 10
              </p>
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


