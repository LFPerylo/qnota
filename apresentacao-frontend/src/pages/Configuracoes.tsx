import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/card';
import { Alert, AlertDescription } from '../components/ui/alert';
import { Info } from 'lucide-react';

export function Configuracoes() {
  return (
    <div className="p-8">
      <div className="mb-8">
        <h1>Configurações</h1>
        <p className="text-muted-foreground">Informações sobre o sistema QNota</p>
      </div>

      <div className="max-w-3xl space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>Sobre o Sistema</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <h4>QNota - Sistema de Gestão Educacional</h4>
              <p className="text-muted-foreground mt-2">
                Sistema completo para gestão de alunos, responsáveis, professores, turmas, disciplinas, 
                simulados, notas e rankings. Desenvolvido com foco em preservação de histórico e 
                rastreabilidade de mudanças.
              </p>
            </div>

            <div>
              <h4>Versão</h4>
              <p className="text-muted-foreground mt-2">1.0.0</p>
            </div>

            <div>
              <h4>Perfil de Acesso</h4>
              <p className="text-muted-foreground mt-2">Coordenador</p>
            </div>
          </CardContent>
        </Card>

        <Alert>
          <Info className="size-4" />
          <AlertDescription>
            <strong>Características do Sistema:</strong>
            <ul className="list-disc list-inside mt-2 space-y-1">
              <li>Preservação de histórico de todas as alterações</li>
              <li>Rastreabilidade completa de mudanças</li>
              <li>Auditoria de retificações de notas</li>
              <li>Gestão completa de simulados e rankings</li>
            </ul>
          </AlertDescription>
        </Alert>

        <Card>
          <CardHeader>
            <CardTitle>Regras de Negócio</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div>
              <h4>Alunos</h4>
              <ul className="list-disc list-inside text-muted-foreground mt-2 space-y-1">
                <li>Requer turma obrigatória</li>
                <li>1 a 3 responsáveis (sendo 1 principal)</li>
                <li>Não pode mudar de turma se tiver simulados finalizados</li>
                <li>Inativação preserva histórico</li>
              </ul>
            </div>

            <div>
              <h4>Responsáveis</h4>
              <ul className="list-disc list-inside text-muted-foreground mt-2 space-y-1">
                <li>CPF único e imutável</li>
                <li>Inadimplentes não podem vincular novos alunos</li>
              </ul>
            </div>

            <div>
              <h4>Professores</h4>
              <ul className="list-disc list-inside text-muted-foreground mt-2 space-y-1">
                <li>Máximo 3 turmas ativas por professor</li>
                <li>Não pode excluir se tiver turmas ativas ou simulados finalizados</li>
              </ul>
            </div>

            <div>
              <h4>Simulados</h4>
              <ul className="list-disc list-inside text-muted-foreground mt-2 space-y-1">
                <li>Mínimo 2 disciplinas</li>
                <li>Soma dos pesos deve ser exatamente 10</li>
                <li>Máximo 2 simulados em edição por turma</li>
                <li>Todas as notas devem ser lançadas antes de finalizar</li>
              </ul>
            </div>

            <div>
              <h4>Notas</h4>
              <ul className="list-disc list-inside text-muted-foreground mt-2 space-y-1">
                <li>Valores de 0,00 a 10,00</li>
                <li>Retificações requerem justificativa mínima de 20 caracteres</li>
                <li>Todas as alterações são registradas na auditoria</li>
              </ul>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}


