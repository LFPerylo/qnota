import React from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from '../ui/dialog';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Textarea } from '../ui/textarea';
import { Alert, AlertDescription } from '../ui/alert';
import { AlertCircle } from 'lucide-react';
import { NumberInputBR } from '../inputs/NumberInputBR';
import { Badge } from '../ui/badge';

interface Retificacao {
  id: string;
  valorOriginal: number;
  valorNovo: number;
  justificativa: string;
  autor?: string;
}

interface SimuladoRetificacaoModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  notaOriginal: number;
  historico?: Retificacao[];
  formData: {
    valorNovo: string;
    justificativa: string;
  };
  onFormDataChange: (data: { valorNovo: string; justificativa: string }) => void;
  formatNumber: (value: number, decimals: number) => string;
  onSave: () => void | Promise<void>;
  onCancel: () => void | Promise<void>;
}

export function SimuladoRetificacaoModal({
  open,
  onOpenChange,
  notaOriginal,
  historico = [],
  formData,
  onFormDataChange,
  formatNumber,
  onSave,
  onCancel
}: SimuladoRetificacaoModalProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Retificar Nota</DialogTitle>
          <DialogDescription>
            Altere a nota com justificativa. A alteração será registrada na auditoria.
          </DialogDescription>
        </DialogHeader>
        
        <Alert>
          <AlertCircle className="size-4" />
          <AlertDescription>
            A nota original será preservada no histórico de retificações. Todas as alterações ficam registradas na Auditoria com a justificativa informada.
          </AlertDescription>
        </Alert>

        <div className="space-y-4">
          <div>
            <Label>Valor Original (somente leitura)</Label>
            <Input 
              value={formatNumber(notaOriginal, 2)} 
              disabled 
            />
          </div>

          <div>
            <Label>Novo Valor * (0,00 - 10,00)</Label>
            <NumberInputBR
              value={formData.valorNovo}
              onChange={(v) => onFormDataChange({ ...formData, valorNovo: v })}
              placeholder="0,00"
              max={10}
            />
          </div>

          <div>
            <Label>Justificativa * (mín. 20 caracteres)</Label>
            <Textarea
              value={formData.justificativa}
              onChange={(e) => onFormDataChange({ ...formData, justificativa: e.target.value })}
              placeholder="Descreva o motivo da alteração..."
              rows={4}
            />
            <p className={`text-sm mt-1 ${formData.justificativa.length < 20 ? 'text-destructive' : 'text-muted-foreground'}`}>
              {formData.justificativa.length}/20 caracteres
            </p>
          </div>

          {historico.length > 0 && (
            <div>
              <Label>Histórico de Retificações</Label>
              <div className="space-y-2 mt-2 max-h-40 overflow-y-auto">
                {historico.map((ret) => (
                  <div key={ret.id} className="p-3 border rounded-lg text-sm">
                    <div className="flex justify-between mb-1">
                      <span>{formatNumber(ret.valorOriginal, 2)} → {formatNumber(ret.valorNovo, 2)}</span>
                      {ret.autor && <Badge variant="outline">{ret.autor}</Badge>}
                    </div>
                    <p className="text-muted-foreground">{ret.justificativa}</p>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={onCancel}>Cancelar</Button>
          <Button onClick={onSave}>Salvar Retificação</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

