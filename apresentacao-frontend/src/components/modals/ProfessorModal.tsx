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
  // Normaliza string removendo aspas extras e convertendo para lowercase
  const normalizar = (s: string) => s.replace(/^"|"$/g, '').toLowerCase().trim();
  
  const toggleEspecialidade = (area: string) => {
    const areaNorm = normalizar(area);
    const jaSelecionada = formData.especialidades.some(e => normalizar(e) === areaNorm);
    const especialidades = jaSelecionada
      ? formData.especialidades.filter(e => normalizar(e) !== areaNorm)
      : [...formData.especialidades, area];
    onFormDataChange({ ...formData, especialidades });
  };

  const adicionarEspecialidadeManual = () => {
    const valor = formData.novaEspecialidade.trim();
    if (!valor) return;
    // Verifica duplicata (normalizado)
    const valorNorm = normalizar(valor);
    const jáExiste = formData.especialidades.some(e => normalizar(e) === valorNorm);
    if (!jáExiste) {
      onFormDataChange({ ...formData, especialidades: [...formData.especialidades, valor], novaEspecialidade: '' });
    } else {
      alert('Esta especialidade já foi adicionada.');
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
              <p className="text-xs text-muted-foreground mt-1">CPF não pode ser alterado após cadastro</p>
            )}
          </div>
          <div>
            <Label>Especialidades *</Label>
            {areasDisponiveis.length ? (
              <div className="grid grid-cols-2 gap-2 mt-2">
                {areasDisponiveis.map(area => (
                  <label key={area} className="flex items-center gap-2 text-sm">
                    <input
                      type="checkbox"
                      checked={formData.especialidades.some(e => normalizar(e) === normalizar(area))}
                      onChange={() => toggleEspecialidade(area)}
                    />
                    {area}
                  </label>
                ))}
              </div>
            ) : (
              <p className="text-muted-foreground text-sm mt-1">Cadastre disciplinas para sugerir áreas automaticamente.</p>
            )}
            {/* Mostrar especialidades que não estão nas áreas disponíveis */}
            {formData.especialidades.filter(esp => 
              !areasDisponiveis.some(area => normalizar(area) === normalizar(esp))
            ).length > 0 && (
              <div className="mt-2">
                <p className="text-xs text-muted-foreground mb-1">Outras especialidades do professor:</p>
                <div className="flex flex-wrap gap-2">
                  {formData.especialidades
                    .filter(esp => !areasDisponiveis.some(area => normalizar(area) === normalizar(esp)))
                    .map(esp => (
                      <span key={esp} className="inline-flex items-center gap-1 bg-secondary px-2 py-1 rounded text-sm">
                        {esp.replace(/^"|"$/g, '')}
                        <button
                          type="button"
                          className="text-muted-foreground hover:text-foreground"
                          onClick={() => toggleEspecialidade(esp)}
                        >
                          ×
                        </button>
                      </span>
                    ))}
                </div>
              </div>
            )}
            <div className="mt-3">
              <Label>Outra especialidade</Label>
              <div className="flex gap-2">
                <Input
                  value={formData.novaEspecialidade}
                  placeholder="Informe e clique em adicionar"
                  onChange={(e) => onFormDataChange({ ...formData, novaEspecialidade: e.target.value })}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      e.preventDefault();
                      adicionarEspecialidadeManual();
                    }
                  }}
                />
                <Button type="button" variant="secondary" onClick={adicionarEspecialidadeManual}>Adicionar</Button>
              </div>
            </div>
            {formData.especialidades.length > 0 && (
              <p className="text-sm text-muted-foreground mt-2">
                Selecionadas: {formData.especialidades.join(', ')}
              </p>
            )}
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


