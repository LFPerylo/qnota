import { useState } from 'react';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Badge } from '../components/ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../components/ui/table';
import { EmptyState } from '../components/EmptyState';
import { Plus, Search, UserCircle, Edit, Trash } from 'lucide-react';
import { ResponsavelModal } from '../components/modals';

interface Responsavel {
  id: string;
  nome: string;
  email: string;
  cpf: string;
  inadimplente: boolean;
}

interface ResponsaveisProps {
  responsaveis: Responsavel[];
  onSave?: (dto: { nome: string; email: string; cpf: string }) => Promise<void>;
  onUpdate?: (id: number, dto: { nome: string; email: string; inadimplente?: boolean }) => Promise<void>;
  onDelete?: (responsavel: Responsavel) => void;
}

export function Responsaveis({
  responsaveis,
  onSave,
  onUpdate,
  onDelete
}: ResponsaveisProps) {
  const [searchTerm, setSearchTerm] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedResponsavel, setSelectedResponsavel] = useState<Responsavel | null>(null);
  
  const [formData, setFormData] = useState({
    nome: '',
    email: '',
    cpf: '',
    inadimplente: false
  });

  const filteredResponsaveis = responsaveis.filter(resp =>
    resp.nome.toLowerCase().includes(searchTerm.toLowerCase()) ||
    resp.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
    resp.cpf.includes(searchTerm.replace(/\D/g, ''))
  );

  const handleOpenDialog = (resp?: Responsavel) => {
    if (resp) {
      setSelectedResponsavel(resp);
      setFormData({
        nome: resp.nome,
        email: resp.email,
        cpf: resp.cpf,
        inadimplente: resp.inadimplente
      });
    } else {
      setSelectedResponsavel(null);
      setFormData({
        nome: '',
        email: '',
        cpf: '',
        inadimplente: false
      });
    }
    setDialogOpen(true);
  };

  const handleDelete = (resp: Responsavel) => {
    onDelete?.(resp);
  };

  const handleSave = async () => {
    // Validações
    if (!formData.nome.trim()) {
      alert('Nome do responsável é obrigatório');
      return;
    }
    if (!formData.email.trim()) {
      alert('E-mail é obrigatório');
      return;
    }
    const cpfLimpo = formData.cpf.replace(/\s+/g, '');
    if (!cpfLimpo) {
      alert('CPF é obrigatório');
      return;
    }

    try {
      if (selectedResponsavel) {
        // Editar
        await onUpdate?.(parseInt(selectedResponsavel.id), {
          nome: formData.nome.trim(),
          email: formData.email.trim(),
          inadimplente: formData.inadimplente
        });
      } else {
        // Criar
        await onSave?.({
          nome: formData.nome.trim(),
          email: formData.email.trim(),
          cpf: formData.cpf.trim()
        });
      }
      setDialogOpen(false);
      // Limpar formulário
      setFormData({
        nome: '',
        email: '',
        cpf: '',
        inadimplente: false
      });
      setSelectedResponsavel(null);
    } catch (error: any) {
      console.error('Erro ao salvar responsável:', error);
      const errorMessage = error.message || 'Erro desconhecido';
      alert('Erro ao salvar responsável: ' + errorMessage);
    }
  };

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1>Responsáveis</h1>
          <p className="text-muted-foreground">Gerencie os responsáveis cadastrados</p>
        </div>
        <Button onClick={() => handleOpenDialog()}>
          <Plus className="size-4 mr-2" />
          Novo Responsável
        </Button>
      </div>

      <div className="bg-card rounded-lg border p-4 mb-6">
        <Label>Buscar</Label>
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-muted-foreground" />
          <Input
            placeholder="Nome, e-mail ou CPF..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pl-9"
          />
        </div>
      </div>

      {filteredResponsaveis.length === 0 ? (
        <EmptyState
          icon={UserCircle}
          title="Nenhum responsável encontrado"
          description="Comece criando um novo responsável ou ajuste o filtro de busca"
          actionLabel="Novo Responsável"
          onAction={() => handleOpenDialog()}
        />
      ) : (
        <div className="bg-card rounded-lg border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Nome</TableHead>
                <TableHead>E-mail</TableHead>
                <TableHead>CPF</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Ações</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredResponsaveis.map(resp => (
                <TableRow key={resp.id}>
                  <TableCell>{resp.nome}</TableCell>
                  <TableCell>{resp.email}</TableCell>
                  <TableCell>{resp.cpf}</TableCell>
                  <TableCell>
                    {resp.inadimplente && (
                      <Badge variant="destructive">Inadimplente</Badge>
                    )}
                    {!resp.inadimplente && (
                      <Badge variant="outline">Regular</Badge>
                    )}
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex items-center justify-end gap-2">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleOpenDialog(resp)}
                      >
                        <Edit className="size-4" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleDelete(resp)}
                      >
                        <Trash className="size-4" />
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}

      <ResponsavelModal
        open={dialogOpen}
        onOpenChange={setDialogOpen}
        isEdit={!!selectedResponsavel}
        formData={formData}
        onFormDataChange={setFormData}
        onSave={handleSave}
        onCancel={() => setDialogOpen(false)}
      />
    </div>
  );
}

