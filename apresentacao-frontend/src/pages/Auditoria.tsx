import React, { useState, useEffect } from 'react';
import { Label } from '../components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../components/ui/select';
import { EmptyState } from '../components/EmptyState';
import { FileText, Save, Eye, Trash2, Activity, RefreshCw, Info } from 'lucide-react';
import { Badge } from '../components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { auditoriaAPI, EventoAuditoria, AuditoriaResumo } from '../services/api';

interface AuditoriaProps {
  turmas: Array<{ id: string; nome: string }>;
}

export function Auditoria({
  turmas
}: AuditoriaProps) {
  const [eventos, setEventos] = useState<EventoAuditoria[]>([]);
  const [resumo, setResumo] = useState<AuditoriaResumo | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Filtros
  const [tipoFilter, setTipoFilter] = useState('TODOS');
  const [turmaFilter, setTurmaFilter] = useState('TODAS');
  const [simuladoFilter, setSimuladoFilter] = useState('TODOS');

  // Extrair simulados unicos dos eventos (garante consistencia de IDs)
  const simuladosNosEventos = Array.from(
    new Set(eventos.filter(e => e.simuladoId != null).map(e => e.simuladoId))
  ).sort((a, b) => (a || 0) - (b || 0));

  // Carregar dados
  const carregarDados = async () => {
    setLoading(true);
    setError(null);
    try {
      const [eventosData, resumoData] = await Promise.all([
        auditoriaAPI.listarEventos(200),
        auditoriaAPI.resumo()
      ]);
      setEventos(eventosData);
      setResumo(resumoData);
    } catch (err) {
      setError('Erro ao carregar dados de auditoria. Verifique se o backend esta rodando.');
      console.error('Erro ao carregar auditoria:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    carregarDados();
    // Auto-refresh a cada 30 segundos
    const interval = setInterval(carregarDados, 30000);
    return () => clearInterval(interval);
  }, []);

  // Filtrar eventos
  const eventosFiltrados = eventos.filter(evento => {
    const matchesTipo = tipoFilter === 'TODOS' || evento.tipo === tipoFilter;
    const matchesTurma = turmaFilter === 'TODAS' || 
      (evento.turmaId != null && String(evento.turmaId) === turmaFilter);
    // Comparar simuladoId do evento com o filtro selecionado
    const matchesSimulado = simuladoFilter === 'TODOS' || 
      (evento.simuladoId != null && String(evento.simuladoId) === simuladoFilter);
    return matchesTipo && matchesTurma && matchesSimulado;
  });

  // Icone por tipo
  const getIconByTipo = (tipo: string) => {
    switch (tipo) {
      case 'SALVAR': return <Save className="size-4" />;
      case 'LEITURA': return <Eye className="size-4" />;
      case 'REMOCAO': return <Trash2 className="size-4" />;
      default: return <Activity className="size-4" />;
    }
  };

  // Cor do badge por tipo
  const getBadgeVariant = (tipo: string): 'default' | 'secondary' | 'destructive' | 'outline' => {
    switch (tipo) {
      case 'SALVAR': return 'default';
      case 'LEITURA': return 'secondary';
      case 'REMOCAO': return 'destructive';
      default: return 'outline';
    }
  };

  // Formatar data/hora
  const formatarDataHora = (dataHora: string) => {
    try {
      const date = new Date(dataHora);
      return date.toLocaleString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    } catch {
      return dataHora;
    }
  };

  if (loading) {
    return (
      <div className="p-8 flex items-center justify-center">
        <RefreshCw className="size-8 animate-spin text-muted-foreground" />
      </div>
    );
  }

  return (
    <div className="p-8">
      <div className="mb-8 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Auditoria de Simulados</h1>
          <p className="text-muted-foreground">
            Historico de operacoes em simulados (Padrao Decorator)
          </p>
        </div>
        <Button variant="outline" onClick={carregarDados} disabled={loading}>
          <RefreshCw className={`size-4 mr-2 ${loading ? 'animate-spin' : ''}`} />
          Atualizar
        </Button>
      </div>

      {/* Card informativo sobre o padrao Decorator */}
      <Card className="mb-6 bg-blue-50 dark:bg-blue-950 border-blue-200 dark:border-blue-800">
        <CardContent className="p-4">
          <div className="flex items-start gap-3">
            <Info className="size-5 text-blue-600 dark:text-blue-400 mt-0.5" />
            <div className="text-sm">
              <p className="font-medium text-blue-900 dark:text-blue-100 mb-1">
                Padrao Decorator em Acao
              </p>
              <p className="text-blue-700 dark:text-blue-300">
                Os eventos abaixo sao capturados pelo <code className="bg-blue-100 dark:bg-blue-900 px-1 rounded">SimuladoRepositorioDecorator</code>, 
                que intercepta operacoes do repositorio e registra em <code className="bg-blue-100 dark:bg-blue-900 px-1 rounded">SimuladoAuditoriaArmazenada</code>.
                Isso adiciona auditoria sem modificar o codigo do repositorio original.
              </p>
            </div>
          </div>
        </CardContent>
      </Card>

      {error && (
        <Card className="mb-6 bg-red-50 dark:bg-red-950 border-red-200 dark:border-red-800">
          <CardContent className="p-4">
            <p className="text-red-700 dark:text-red-300">{error}</p>
          </CardContent>
        </Card>
      )}

      {/* Resumo estatistico */}
      {resumo && (
        <div className="grid grid-cols-2 md:grid-cols-5 gap-4 mb-6">
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">Total de Eventos</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-bold">{resumo.totalEventos}</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground flex items-center gap-2">
                <Save className="size-4" /> Salvamentos
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-bold text-green-600">{resumo.salvamentos}</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground flex items-center gap-2">
                <Eye className="size-4" /> Leituras
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-bold text-blue-600">{resumo.leituras}</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground flex items-center gap-2">
                <Trash2 className="size-4" /> Remocoes
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-bold text-red-600">{resumo.remocoes}</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">Ultimo Evento</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-sm">
                {resumo.ultimoEvento ? formatarDataHora(resumo.ultimoEvento) : '-'}
              </p>
            </CardContent>
          </Card>
        </div>
      )}

      {/* Filtros */}
      <div className="bg-card rounded-lg border p-4 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <Label>Tipo de Operacao</Label>
            <Select value={tipoFilter} onValueChange={setTipoFilter}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="TODOS">Todos</SelectItem>
                <SelectItem value="SALVAR">Salvamentos</SelectItem>
                <SelectItem value="LEITURA">Leituras</SelectItem>
                <SelectItem value="REMOCAO">Remocoes</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div>
            <Label>Turma</Label>
            <Select value={turmaFilter} onValueChange={setTurmaFilter}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="TODAS">Todas</SelectItem>
                {turmas.map(turma => (
                  <SelectItem key={turma.id} value={turma.id}>{turma.nome}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div>
            <Label>Simulado</Label>
            <Select value={simuladoFilter} onValueChange={setSimuladoFilter}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="TODOS">Todos</SelectItem>
                {simuladosNosEventos.map(simId => (
                  <SelectItem key={simId} value={String(simId)}>
                    Simulado #{simId}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </div>
      </div>

      {/* Lista de eventos */}
      {eventosFiltrados.length === 0 ? (
        <EmptyState
          icon={FileText}
          title="Nenhum evento de auditoria encontrado"
          description={eventos.length === 0 
            ? "Navegue pelo sistema para gerar eventos. Qualquer operacao em simulados sera registrada aqui."
            : "Nenhum evento corresponde aos filtros selecionados"}
        />
      ) : (
        <div className="space-y-3">
          <p className="text-sm text-muted-foreground mb-4">
            Exibindo {eventosFiltrados.length} de {eventos.length} eventos
          </p>
          {eventosFiltrados.map((evento, index) => {
            const turma = evento.turmaId ? turmas.find(t => t.id === evento.turmaId.toString()) : null;
            
            return (
              <Card key={index} className="hover:bg-muted/50 transition-colors">
                <CardContent className="p-4">
                  <div className="flex items-start justify-between">
                    <div className="flex items-start gap-3">
                      <div className={`p-2 rounded-full ${
                        evento.tipo === 'SALVAR' ? 'bg-green-100 text-green-600 dark:bg-green-900 dark:text-green-400' :
                        evento.tipo === 'LEITURA' ? 'bg-blue-100 text-blue-600 dark:bg-blue-900 dark:text-blue-400' :
                        'bg-red-100 text-red-600 dark:bg-red-900 dark:text-red-400'
                      }`}>
                        {getIconByTipo(evento.tipo)}
                      </div>
                      <div>
                        <div className="flex items-center gap-2 mb-1">
                          <Badge variant={getBadgeVariant(evento.tipo)}>
                            {evento.tipoDescricao}
                          </Badge>
                          {evento.status && (
                            <Badge variant="outline">
                              {evento.status === 'EM_EDICAO' ? 'Em Edicao' : evento.status}
                            </Badge>
                          )}
                        </div>
                        <p className="text-sm font-medium">{evento.descricao}</p>
                        <div className="flex items-center gap-4 mt-2 text-xs text-muted-foreground">
                          {evento.simuladoId && (
                            <span>Simulado #{evento.simuladoId}</span>
                          )}
                          {turma && (
                            <span>Turma: {turma.nome}</span>
                          )}
                        </div>
                      </div>
                    </div>
                    <div className="text-sm text-muted-foreground text-right">
                      {formatarDataHora(evento.dataHora)}
                    </div>
                  </div>
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}
    </div>
  );
}
