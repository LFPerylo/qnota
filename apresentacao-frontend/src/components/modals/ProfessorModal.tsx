import React from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from '../ui/dialog';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { CPFInput } from '../inputs/CPFInput';

interface ProfessorModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  isEdit?: boolean;
  formData: {
    nome: string;
    email: string;
    cpf: string;
    especialidades: string[];
    novaEspecialidade: string;
  };
  areasDisponiveis: string[];
  onFormDataChange: (data: ProfessorModalProps['formData']) => void;
  onSave: () => void;
  onCancel: () => void;
}

export function ProfessorModal({
  open,
  onOpenChange,
  isEdit = false,
  formData,
  areasDisponiveis,
  onFormDataChange,
  onSave,
  onCancel
}: ProfessorModalProps) {
  const toggleEspecialidade = (area: string) => {
    const jaSelecionada = formData.especialidades.includes(area);
    const especialidades = jaSelecionada
      ? formData.especialidades.filter(e => e !== area)
      : [...formData.especialidades, area];
    onFormDataChange({ ...formData, especialidades });
  };

  const adicionarEspecialidadeManual = () => {
    const valor = formData.novaEspecialidade.trim();
    if (!valor) return;
    if (!formData.especialidades.includes(valor)) {
      onFormDataChange({ ...formData, especialidades: [...formData.especialidades, valor], novaEspecialidade: '' });
    } else {
      onFormDataChange({ ...formData, novaEspecialidade: '' });
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Editar Professor' : 'Novo Professor'}</DialogTitle>
          <DialogDescription>
            {isEdit ? 'Atualize as informações do professor' : 'Cadastre um novo professor no sistema'}
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
              placeholder="email@escola.com"
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
          {!isEdit && (
            <div>
              <Label>Especialidades *</Label>
              {areasDisponiveis.length ? (
                <div className="grid grid-cols-2 gap-2 mt-2">
                  {areasDisponiveis.map(area => (
                    <label key={area} className="flex items-center gap-2 text-sm">
                      <input
                        type="checkbox"
                        checked={formData.especialidades.includes(area)}
                        onChange={() => toggleEspecialidade(area)}
                      />
                      {area}
                    </label>
                  ))}
                </div>
              ) : (
                <p className="text-muted-foreground text-sm mt-1">Cadastre disciplinas para sugerir áreas automaticamente.</p>
              )}
              <div className="mt-3">
                <Label>Outra especialidade</Label>
                <div className="flex gap-2">
                  <Input
                    value={formData.novaEspecialidade}
                    placeholder="Informe e clique em adicionar"
                    onChange={(e) => onFormDataChange({ ...formData, novaEspecialidade: e.target.value })}
                  />
                  <Button type="button" variant="secondary" onClick={adicionarEspecialidadeManual}>Adicionar</Button>
                </div>
              </div>
              {formData.especialidades.length > 0 && (
+                <p className="text-sm text-muted-foreground mt-2">
                  Selecionadas: {formData.especialidades.join(', ')}
                </p>
              )}
            </div>
          )}
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={onCancel}>Cancelar</Button>
          <Button onClick={onSave}>Salvar</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}


