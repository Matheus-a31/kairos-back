# Kairós - Backend (API)

O **Kairós** é uma plataforma moderna e intuitiva de gerenciamento de projetos e tarefas baseada no modelo Kanban (semelhante ao Trello ou Jira). O sistema permite aos usuários criar projetos, convidar membros da equipe com diferentes níveis de acesso (Gerente, Desenvolvedor, Visualizador), organizar tarefas em colunas customizáveis, adicionar tags, definir prioridades e prazos, além de gerenciar todo o fluxo de trabalho de forma colaborativa e ágil.

Este é o backend da aplicação, construído utilizando **Java 21**, **Spring Boot 3.4.1**, e banco de dados **PostgreSQL**. A API fornece toda a lógica de negócio, autenticação JWT, disparo de e-mails para convites e controle completo do Kanban.

## Pré-requisitos

- **Java Development Kit (JDK)**: Versão 21 ou superior.
- **Maven**: Para gerenciar as dependências e build.
- **PostgreSQL**: Rodando localmente ou em contêiner Docker (Porta padrão 5433 no script de dev).

## Configuração do Ambiente (.env)

O backend possui suporte nativo à injeção de variáveis de ambiente através de um arquivo \`.env\`.
Para começar, crie um arquivo \`.env\` na raiz da pasta \`backend/\` contendo as seguintes chaves de configuração:

\`\`\`env
# Banco de Dados
DB_URL=jdbc:postgresql://127.0.0.1:5433/kairos_db
DB_USERNAME=kairos
DB_PASSWORD=kairos

# JWT Secret (Recomenda-se gerar uma chave hash forte com no mínimo 64 caracteres)
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

# Integração SendGrid (Envio de Emails)
SENDGRID_API_KEY=sua-chave-api-do-sendgrid
SENDGRID_FROM_EMAIL=seuemailverificado@dominio.com

# URL do Frontend (Usado para os links nos e-mails)
FRONTEND_URL=http://localhost:4200
\`\`\`

> **Nota:** Se a variável \`SENDGRID_API_KEY\` não for fornecida, o sistema entra em modo de Fallback e os e-mails (como links de convite) serão logados no console, em vez de serem disparados por e-mail, ideal para testes de desenvolvimento rápidos.

## Como rodar o projeto localmente

1. Abra o terminal na pasta do backend:
   \`cd backend\`

2. Suba o banco de dados (Você pode usar o Docker Compose disponibilizado para instanciar rapidamente):
   \`docker-compose up -d\`

3. Inicie a aplicação com o Maven:
   Pode-se iniciar garantindo a leitura das variáveis (exemplo no PowerShell):
   \`\`\`powershell
   Get-Content .env | Where-Object { $_ -match "^[^#]" } | ForEach-Object {
       $name, $value = $_.Split('=', 2)
       Set-Item "env:$name" $value
   }
   .\mvnw spring-boot:run
   \`\`\`

O servidor inciará em \`http://localhost:8080\`. A migração das tabelas será feita automaticamente pelo **Flyway** assim que o banco se conectar pela primeira vez.

## Swagger / Documentação da API

Quando a aplicação estiver rodando localmente, você poderá acessar a documentação detalhada interativa de todos os endpoints pela rota:
\`http://localhost:8080/docs\`

## Arquitetura e Domínios

A estrutura está subdividida em pacotes seguindo um modelo orientado ao domínio (*Domain Driven Design*):
- \`com.kairos.auth\`: Lógica de gerenciamento de Segurança, Login, Registro e JWT.
- \`com.kairos.project\`: Entidades relacionadas ao projeto (projetos, membros e convites).
- \`com.kairos.task\`: Gerenciamento de tarefas e quadros Kanban (colunas, tags).
- \`com.kairos.core\`: Classes de serviço globais, tratamento genérico de erros e disparos de e-mail.

## Licença

Este projeto está licenciado sob a [GNU General Public License v3.0] (LICENSE).
