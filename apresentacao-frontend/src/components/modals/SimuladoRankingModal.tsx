import React from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '../ui/dialog';
import { Alert, AlertDescription } from '../ui/alert';
import { AlertCircle, Trophy } from 'lucide-react';

interface RankingItem {
  aluno: {
    id: string;
    nome: string;
    dataNascimento: string;
  };
  media: number;
}

interface SimuladoRankingModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  turmaNome: string;
  ranking: RankingItem[];
  formatDate: (date: string | Date) => string;
  formatNumber: (value: number, decimals: number) => string;
}

export function SimuladoRankingModal({
  open,
  onOpenChange,
  turmaNome,
  ranking,
  formatDate,
  formatNumber
}: SimuladoRankingModalProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>
            Ranking - {turmaNome}
          </DialogTitle>
          <DialogDescription>
            Classificação dos alunos com base nas médias ponderadas
          </DialogDescription>
        </DialogHeader>
        
        <Alert>
          <AlertCircle className="size-4" />
          <AlertDescription>
            Desempate por idade (mais velho primeiro)
          </AlertDescription>
        </Alert>

        <div className="space-y-3">
          {ranking.map((item, index) => (
            <div 
              key={item.aluno.id} 
              className={`flex items-center gap-4 p-4 rounded-lg border ${
                index === 0 ? 'bg-yellow-50 border-yellow-200' :
                index === 1 ? 'bg-gray-50 border-gray-200' :
                index === 2 ? 'bg-orange-50 border-orange-200' :
                'bg-card'
              }`}
            >
              <div className="flex items-center justify-center size-12 rounded-full bg-muted">
                {index === 0 && <Trophy className="size-6 text-yellow-600" />}
                {index === 1 && <Trophy className="size-6 text-gray-600" />}
                {index === 2 && <Trophy className="size-6 text-orange-600" />}
                {index > 2 && <span>{index + 1}º</span>}
              </div>
              <div className="flex-1">
                <p>{item.aluno.nome}</p>
                <p className="text-muted-foreground">
                  Nascimento: {formatDate(item.aluno.dataNascimento)}
                </p>
              </div>
              <div className="text-right">
                <div className="text-2xl">{formatNumber(item.media, 2)}</div>
                <p className="text-muted-foreground">Média</p>
              </div>
            </div>
          ))}
        </div>
      </DialogContent>
    </Dialog>
  );
}


