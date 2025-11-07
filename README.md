# Infraestrutura Docker para www.macromv.com

Este repositório contém a infraestrutura Docker utilizada para publicar o backend Spring Boot, o frontend Vite e os serviços auxiliares (MariaDB, Ollama) da aplicação **www.macromv.com**. O tráfego externo passa por um proxy reverso Nginx com TLS, mas mantemos um túnel Ngrok para cenários de testes ou contingência.

## 📦 Serviços

O `docker-compose.yml` orquestra os seguintes containers:

| Serviço    | Descrição                                                                 |
|------------|----------------------------------------------------------------------------|
| `backend`  | Aplicação Spring Boot exposta internamente na porta 8080.                 |
| `frontend` | Build de produção do app Vite servido via Nginx interno.                  |
| `db`       | Banco de dados MariaDB.                                                   |
| `ollama`   | Runtime dos modelos utilizados pela aplicação.                            |
| `nginx`    | Proxy reverso público com HTTPS para `www.macromv.com` e `macromv.com`.   |
| `certbot`  | Utilitário para emitir e renovar certificados Let's Encrypt.              |
| `ngrok`    | Túnel opcional que expõe o frontend para uso emergencial ou de testes.    |

## ✅ Pré-requisitos

1. **DNS**: Crie registros `A` para `www.macromv.com` e `macromv.com` apontando para o IP do servidor.
2. **Firewall**: Libere as portas **80** e **443** TCP.
3. **Docker** e **Docker Compose v2** instalados na máquina host.

## 🚀 Passo a passo

### 1. Clone o repositório

```bash
git clone https://github.com/seu-usuario/seu-projeto.git
cd seu-projeto
```

### 2. Configure variáveis de ambiente (opcional)

Crie um arquivo `.env` na raiz para sobrescrever portas ou credenciais, se necessário. Exemplo:

```dotenv
BACKEND_PORT=8080
DB_PASSWORD=minha-senha
```

### 3. Emita os certificados TLS

A primeira emissão pode ser feita com o modo `standalone` do Certbot (Nginx não pode estar rodando nesse momento):

```bash
docker compose run --rm --service-ports certbot \
  certonly --standalone \
  -d www.macromv.com -d macromv.com \
  --email seu-email@dominio.com --agree-tos --non-interactive
```

> Use `--staging` durante os testes para evitar limites da Let's Encrypt.

Os certificados ficarão persistidos no volume `certbot-etc`, compartilhado com o container `nginx`.

### 4. Suba toda a stack

```bash
docker compose up -d
```

O container `nginx` aguarda pelos certificados válidos antes de inicializar e então publica a aplicação com HTTPS em `https://www.macromv.com`.

### 5. Renovação de certificados

Agende (via cron, por exemplo) a renovação manualmente:

```bash
docker compose run --rm certbot renew --webroot -w /var/www/certbot
docker compose exec nginx nginx -s reload
```

A renovação utiliza o desafio HTTP: o Certbot grava os arquivos na pasta compartilhada `certbot-www`, que é servida pelo Nginx na rota `/.well-known/acme-challenge/`.

### 6. Logs e manutenção

- Nginx: `docker compose logs -f nginx`
- Certbot: `docker compose run --rm certbot certificates`
- Atualizar certificados manualmente: repita o passo 3.

## 🌐 Acesso alternativo via Ngrok (opcional)

O serviço `ngrok` continua disponível para situações em que seja necessário compartilhar rapidamente uma URL pública sem ajustar DNS. Para habilitá-lo:

1. Crie ou recupere o token em [dashboard.ngrok.com](https://dashboard.ngrok.com/get-started/setup).
2. Defina a variável `NGROK_AUTHTOKEN` no `.env` ou no ambiente antes de subir os containers.
3. Execute `docker compose up -d ngrok` (ou suba toda a stack). O painel local ficará acessível em `http://localhost:4040`.

O túnel publicará o frontend (`frontend:80`). Mesmo utilizando o domínio oficial, o Ngrok pode ser útil para validar integrações externas ou demonstrar funcionalidades de forma temporária.

## 🔧 Personalizações

- **Domínio diferente**: atualize `server_name` em `nginx/conf.d/default.conf` e ajuste o comando do Certbot.
- **Portas internas**: modifique o mapeamento em `docker-compose.yml` conforme necessário.
- **Headers/rotas**: edite o bloco correspondente no arquivo de configuração do Nginx.

## 🧪 Verificação pós-deploy

1. Acesse `https://www.macromv.com/health` para validar a resposta `ok` do proxy.
2. Confira o certificado emitido pela Let's Encrypt no navegador.
3. Exercite `https://www.macromv.com/api/...` para garantir o roteamento ao backend.

Com essa configuração, a aplicação passa a operar diretamente no domínio oficial, sem a necessidade de túneis temporários do Ngrok.
