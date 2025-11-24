import React from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '../ui/dialog';
import { Badge } from '../ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../ui/table';

interface AlunoDetailModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  aluno: {
    nome: string;
    dataNascimento: string;
    turma: string;
    status: string;
    responsaveis: Array<{
      nome: string;
      email: string;
      principal: boolean;
      inadimplente: boolean;
    }>;
    notas?: Array<{
      simulado: string;
      disciplina: string;
      nota: number;
      data: string;
    }>;
  };
}

export function AlunoDetailModal({
  open,
  onOpenChange,
  aluno
}: AlunoDetailModalProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Detalhes do Aluno</DialogTitle>
          <DialogDescription>
            Informações completas do aluno e seu histórico
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-6">
          <div>
            <h3 className="font-semibold mb-2">Informações Pessoais</h3>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-muted-foreground">Nome</p>
                <p className="font-medium">{aluno.nome}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Data de Nascimento</p>
                <p className="font-medium">{aluno.dataNascimento}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Turma</p>
                <p className="font-medium">{aluno.turma}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Status</p>
                <Badge variant={aluno.status === 'ATIVO' ? 'default' : 'secondary'}>
                  {aluno.status === 'ATIVO' ? 'Ativo' : 'Inativo'}
                </Badge>
              </div>
            </div>
          </div>

          <div>
            <h3 className="font-semibold mb-2">Responsáveis</h3>
            <div className="space-y-2">
              {aluno.responsaveis.map((resp, index) => (
                <div key={index} className="flex items-center justify-between p-2 border rounded">
                  <div>
                    <p className="font-medium">{resp.nome}</p>
                    <p className="text-sm text-muted-foreground">{resp.email}</p>
                  </div>
                  <div className="flex gap-2">
                    {resp.principal && (
                      <Badge variant="default">Principal</Badge>
                    )}
                    {resp.inadimplente && (
                      <Badge variant="destructive">Inadimplente</Badge>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {aluno.notas && aluno.notas.length > 0 && (
            <div>
              <h3 className="font-semibold mb-2">Histórico de Notas</h3>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Simulado</TableHead>
                    <TableHead>Disciplina</TableHead>
                    <TableHead>Nota</TableHead>
                    <TableHead>Data</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {aluno.notas.map((nota, index) => (
                    <TableRow key={index}>
                      <TableCell>{nota.simulado}</TableCell>
                      <TableCell>{nota.disciplina}</TableCell>
                      <TableCell>{nota.nota.toFixed(2)}</TableCell>
                      <TableCell>{nota.data}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
}


