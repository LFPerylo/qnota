import React from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from '../ui/dialog';
import { Button } from '../ui/button';
import { Label } from '../ui/label';
import { Textarea } from '../ui/textarea';
import { Alert, AlertDescription } from '../ui/alert';
import { AlertCircle } from 'lucide-react';

interface Alteracao {
  nota: any;
  aluno: string;
  disciplina: string;
  valorOriginal: number;
  valorNovo: number;
  justificativa: string;
}

interface SimuladoAlteracoesLoteModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  alteracoesPendentes: Alteracao[];
  onAlteracoesChange: (alteracoes: Alteracao[]) => void;
  formatNumber: (value: number, decimals: number) => string;
  onSave: () => void | Promise<void>;
  onCancel: () => void | Promise<void>;
}

export function SimuladoAlteracoesLoteModal({
  open,
  onOpenChange,
  alteracoesPendentes,
  onAlteracoesChange,
  formatNumber,
  onSave,
  onCancel
}: SimuladoAlteracoesLoteModalProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Justificar Alterações de Notas</DialogTitle>
          <DialogDescription>
            Você está alterando {alteracoesPendentes.length} nota(s) existente(s). Forneça uma justificativa para cada alteração.
          </DialogDescription>
        </DialogHeader>

        <Alert>
          <AlertCircle className="size-4" />
          <AlertDescription>
            Todas as alterações serão registradas na Auditoria com as justificativas fornecidas. Mínimo de 20 caracteres por justificativa.
          </AlertDescription>
        </Alert>

        <div className="space-y-4">
          {alteracoesPendentes.map((alteracao, index) => (
            <div key={index} className="border rounded-lg p-4 space-y-3">
              <div className="flex items-start justify-between">
                <div>
                  <p className="font-medium">{alteracao.aluno}</p>
                  <p className="text-muted-foreground">{alteracao.disciplina}</p>
                </div>
                <div className="text-right">
                  <div className="flex items-center gap-2">
                    <span className="text-lg">{formatNumber(alteracao.valorOriginal, 2)}</span>
                    <span className="text-muted-foreground">→</span>
                    <span className="text-lg text-primary">{formatNumber(alteracao.valorNovo, 2)}</span>
                  </div>
                  <span className={`text-sm ${alteracao.valorNovo > alteracao.valorOriginal ? 'text-green-600' : 'text-red-600'}`}>
                    {alteracao.valorNovo > alteracao.valorOriginal ? '+' : ''}
                    {formatNumber(alteracao.valorNovo - alteracao.valorOriginal, 2)}
                  </span>
                </div>
              </div>

              <div>
                <Label>Justificativa * (mín. 20 caracteres)</Label>
                <Textarea
                  value={alteracao.justificativa}
                  onChange={(e) => {
                    const updated = [...alteracoesPendentes];
                    updated[index].justificativa = e.target.value;
                    onAlteracoesChange(updated);
                  }}
                  placeholder="Descreva o motivo da alteração..."
                  rows={3}
                />
                <p className={`text-sm mt-1 ${alteracao.justificativa.length < 20 ? 'text-destructive' : 'text-muted-foreground'}`}>
                  {alteracao.justificativa.length}/20 caracteres
                </p>
              </div>
            </div>
          ))}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={onCancel}>
            Cancelar
          </Button>
          <Button onClick={onSave}>
            Salvar Todas as Alterações
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}


