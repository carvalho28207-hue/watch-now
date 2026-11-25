# Watch Now · Assista Agora

Recomendador de filmes e séries de streaming. Responda 7 perguntas e receba os 5 títulos mais compatíveis, com classificação indicativa, notas IMDb e Rotten Tomatoes e pôster.

Projeto acadêmico de Lógica de Programação (2026) · João Pedro dos Santos Oliveira Carneiro e Enzo Carvalho de Andrade.

## Como funciona

```
Celular (HTML/CSS/JS) ──► Servidor Java ──► TMDB (filmes, séries, pôsteres)
                                    └────► OMDb (notas IMDb e Rotten Tomatoes)
```

1. O front pede as perguntas (`GET /api/perguntas`) e mostra uma por vez.
2. Envia as 7 respostas (`POST /api/recomendar`).
3. O Java busca títulos na TMDB, calcula a compatibilidade e detalha os melhores.
4. A OMDb fornece as notas dos 5 finalistas.
5. O servidor devolve o Top 5 em JSON e o front desenha os cards.

### Compatibilidade

| Critério | Peso |
|---|---|
| Ano (recente = 2020 ou depois) | 30% |
| Idioma (nacional = português) | 20% |
| Popularidade (votos acima da média) | 20% |
| Nota TMDB (0 a 10) | 30% |

Tipo, streaming, gênero e classificação máxima são **filtros** da busca e não valem pontos.

## Estrutura

```
src/   Java: Main, Servidor, Questionario, Pergunta, Titulo, ApiTmdb, ApiOmdb, Recomendador, Http, Config, Demo
web/   index.html, style.css, app.js
lib/   gson-2.13.1.jar (biblioteca do Google para JSON, licença Apache 2.0)
```

## Segurança das chaves

- As chaves **não estão no código**. A classe `Config` lê primeiro uma variável de ambiente e, se não houver, o arquivo `config.properties`.
- `config.properties` está no `.gitignore`, então nunca vai para o GitHub.
- Dentro do Java, a chave fica em um atributo `private static final`, sem getter (encapsulamento).
- O navegador nunca fala direto com a TMDB/OMDb, então a chave não aparece para o usuário.
- O servidor valida as respostas recebidas e bloqueia acesso a arquivos fora da pasta `web/`.

## Rodar no seu computador

Requisito: JDK 21 ou mais recente.

1. Crie chaves grátis em https://www.themoviedb.org/settings/api e https://www.omdbapi.com/apikey.aspx
2. Copie `config.exemplo.properties` como `config.properties` e cole as chaves.
3. Rode pelo VS Code (botão **Run** em `src/Main.java`) ou pelo terminal, na pasta do projeto:

```powershell
javac -encoding UTF-8 -cp lib/gson-2.13.1.jar -d out src/*.java
java -cp "out;lib/gson-2.13.1.jar" Main
```

(No Linux/Mac, troque `;` por `:`.)

4. Abra http://localhost:8080

Sem chaves, o app roda em **modo demonstração**, com dados ilustrativos.

## Publicar (Render)

1. New → Web Service → conectar este repositório (o Render usa o `Dockerfile`).
2. Em **Environment**, criar `TMDB_KEY` e `OMDB_KEY`.
3. O plano grátis "dorme" após 15 min sem acesso. Para manter acordado, agende um acesso a `/api/status` a cada 10 min (ex.: cron-job.org).

## Redundância

1. **Rota A:** site publicado no Render.
2. **Rota B:** servidor rodando no notebook + túnel público (ngrok ou similar).
3. **Rota C:** modo demonstração (se as APIs falharem, o servidor responde com dados ilustrativos) e vídeo gravado.

## Créditos

Este produto usa a API do TMDB, mas não é endossado nem certificado pelo TMDB. Notas via OMDb API. Visual inspirado na Netflix.
