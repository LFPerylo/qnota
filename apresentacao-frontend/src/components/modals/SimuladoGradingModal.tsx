import React from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from '../ui/dialog';
import { Button } from '../ui/button';
import { Badge } from '../ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../ui/table';
import { NumberInputBR } from '../inputs/NumberInputBR';
// Edit removido - simulados finalizados não permitem edição

interface Aluno {
  id: string;
  nome: string;
}

interface Disciplina {
  disciplinaId: string;
  peso: number;
}

interface Retificacao {
  id: string;
  valorOriginal: number;
  valorNovo: number;
  justificativa: string;
  autor?: string;
}

interface Nota {
  id: string;
  simuladoId: string;
  alunoId: string;
  disciplinaId: string;
  valor: number;
  retificacoes?: Retificacao[];
}

interface SimuladoGradingModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  simulado: {
    id: string;
    turmaId: string;
    status: string;
    disciplinas: Disciplina[];
  };
  turmaNome: string;
  alunos: Aluno[];
  disciplinas: Array<{ id: string; nome: string }>;
  notas: Nota[];
  gradeData: { [key: string]: string };
  onGradeDataChange: (data: { [key: string]: string }) => void;
  onOpenRetificacao: (nota: Nota) => void;
  calcularMedia: (alunoId: string, simuladoId: string) => number;
  formatNumber: (value: number, decimals: number) => string;
  isEditMode?: boolean;
  onSave: () => void | Promise<void>;
  onFinalizar: () => void | Promise<void>;
  onClose: () => void;
}

export function SimuladoGradingModal({
  open,
  onOpenChange,
  simulado,
  turmaNome,
  alunos,
  disciplinas,
  notas,
  gradeData,
  onGradeDataChange,
  onOpenRetificacao,
  calcularMedia,
  formatNumber,
  isEditMode = false,
  onSave,
  onFinalizar,
  onClose
}: SimuladoGradingModalProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-6xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>
            {simulado.status === 'FINALIZADO' ? 'Visualizar Notas' : 'Lançar Notas'} - {turmaNome}
          </DialogTitle>
          <DialogDescription>
            {simulado.status === 'FINALIZADO' 
              ? 'Simulado finalizado - notas não podem ser alteradas'
              : 'Lance as notas dos alunos para cada disciplina do simulado'}
          </DialogDescription>
          <Badge variant={simulado.status === 'FINALIZADO' ? 'default' : 'secondary'}>
            {simulado.status === 'EM_EDICAO' ? 'Em Edição' : 'Finalizado'}
          </Badge>
        </DialogHeader>
        
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Aluno</TableHead>
                {simulado.disciplinas.map(disc => {
                  const disciplina = disciplinas.find(d => d.id === disc.disciplinaId);
                  return (
                    <TableHead key={disc.disciplinaId}>
                      {disciplina?.nome} (Peso: {disc.peso})
                    </TableHead>
                  );
                })}
                <TableHead>Média</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {alunos.map(aluno => {
                const media = calcularMedia(aluno.id, simulado.id);
                
                return (
                  <TableRow key={aluno.id}>
                    <TableCell>{aluno.nome}</TableCell>
                    {simulado.disciplinas.map(disc => {
                      const key = `${aluno.id}-${disc.disciplinaId}`;
                      const notaObj = notas.find(n => 
                        n.simuladoId === simulado.id && 
                        n.alunoId === aluno.id && 
                        n.disciplinaId === disc.disciplinaId
                      );

                      const isFinalizado = simulado.status === 'FINALIZADO';
                      
                      return (
                        <TableCell key={disc.disciplinaId}>
                          <div className="flex items-center gap-2">
                            {isFinalizado ? (
                              // Simulado finalizado: apenas exibe o valor
                              <span className="text-sm font-medium px-2 py-1 bg-muted rounded min-w-[60px] text-center">
                                {gradeData[key] || '-'}
                              </span>
                            ) : (
                              // Simulado em edição: permite editar
                              <NumberInputBR
                                value={gradeData[key] || ''}
                                onChange={(v) => onGradeDataChange({ ...gradeData, [key]: v })}
                                placeholder="0,00"
                                max={10}
                              />
                            )}
                            {notaObj && notaObj.retificacoes && notaObj.retificacoes.length > 0 && (
                              <Badge variant="outline" title="Esta nota foi retificada">R</Badge>
                            )}
                          </div>
                        </TableCell>
                      );
                    })}
                    <TableCell>
                      {media > 0 ? formatNumber(media, 2) : '-'}
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </div>
        
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>Fechar</Button>
          {isEditMode && (
            <>
              <Button onClick={onSave}>Salvar Notas</Button>
              <Button variant="default" onClick={onFinalizar}>Finalizar Simulado</Button>
            </>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}


