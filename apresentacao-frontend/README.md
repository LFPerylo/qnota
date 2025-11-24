# QNota Frontend

Frontend do sistema QNota - Sistema de Gestão Educacional

## Instalação

1. Instale as dependências:
```bash
npm install
```

2. Configure a variável de ambiente (opcional):
```bash
cp .env.example .env
```

3. Inicie o servidor de desenvolvimento:
```bash
npm run dev
```

O frontend estará disponível em `http://localhost:3000`

## Configuração

O backend deve estar rodando em `http://localhost:8080` (ou configure a variável `VITE_API_BASE_URL` no arquivo `.env`).

## Estrutura

- `/src/components` - Componentes reutilizáveis (modais, inputs, UI)
- `/src/pages` - Páginas principais da aplicação
- `/src/contexts` - Contextos React (DataContext)
- `/src/services` - Serviços de API
- `/src/lib` - Utilitários e helpers

## Scripts

- `npm run dev` - Inicia o servidor de desenvolvimento
- `npm run build` - Cria build de produção
- `npm run preview` - Preview do build de produção
- `npm run lint` - Executa o linter


