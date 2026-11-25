// Front-end do Watch Now: mostra as perguntas, envia as respostas ao servidor
// Java e desenha o Top 5. Não conhece nenhuma chave de API.

let perguntas = [];
let respostas = [];
let atual = 0;

const $ = (id) => document.getElementById(id);

// Mostra só uma tela por vez
function mostrar(idTela) {
  document.querySelectorAll(".tela").forEach((t) => t.classList.remove("ativa"));
  $(idTela).classList.add("ativa");
  window.scrollTo(0, 0);
}

// 1) Pede as perguntas ao servidor
async function carregarPerguntas() {
  const resposta = await fetch("/api/perguntas");
  if (!resposta.ok) throw new Error("Falha ao carregar perguntas");
  perguntas = await resposta.json();
}

async function comecar() {
  try {
    if (perguntas.length === 0) await carregarPerguntas();
    respostas = [];
    atual = 0;
    desenharPergunta();
    mostrar("quiz");
  } catch (e) {
    mostrarErro("Não foi possível carregar as perguntas. Verifique a conexão.");
  }
}

// 2) Desenha a pergunta atual com um botão por opção
function desenharPergunta() {
  const p = perguntas[atual];
  $("contador").textContent = `Pergunta ${atual + 1} de ${perguntas.length}`;
  $("barra").style.width = `${(atual / perguntas.length) * 100}%`;
  $("pergunta").textContent = p.texto;
  $("voltar").style.visibility = atual === 0 ? "hidden" : "visible";

  const caixa = $("opcoes");
  caixa.innerHTML = "";
  p.opcoes.forEach((opcao) => {
    const botao = document.createElement("button");
    botao.type = "button";
    botao.className = "opcao";
    botao.textContent = opcao; // textContent evita injeção de HTML
    botao.addEventListener("click", () => escolher(opcao));
    caixa.appendChild(botao);
  });
}

function escolher(opcao) {
  respostas[atual] = opcao;
  atual++;
  if (atual < perguntas.length) {
    desenharPergunta();
  } else {
    enviar();
  }
}

function voltar() {
  if (atual > 0) {
    atual--;
    desenharPergunta();
  }
}

// 3) Envia as respostas e espera o Top 5
async function enviar() {
  mostrar("carregando");
  try {
    const resposta = await fetch("/api/recomendar", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(respostas),
    });
    if (!resposta.ok) throw new Error("Servidor respondeu " + resposta.status);
    const dados = await resposta.json();
    desenharResultado(dados);
  } catch (e) {
    mostrarErro("Não foi possível gerar as recomendações. Tente de novo.");
  }
}

// 4) Desenha os cards do Top 5
function desenharResultado(dados) {
  $("aviso").hidden = dados.modo !== "demo";
  const lista = $("lista");
  lista.innerHTML = "";

  if (!dados.titulos || dados.titulos.length === 0) {
    const vazio = document.createElement("li");
    vazio.className = "texto";
    vazio.textContent = "Nenhum título encontrado com esses filtros. Tente outro gênero ou streaming.";
    lista.appendChild(vazio);
  }

  dados.titulos.forEach((t, i) => lista.appendChild(criarCard(t, i + 1)));
  mostrar("resultado");
}

function criarCard(t, posicao) {
  const li = document.createElement("li");
  li.className = "card";

  // Pôster (ou um quadro vermelho com o nome, se não houver imagem)
  if (t.poster) {
    const img = document.createElement("img");
    img.className = "poster";
    img.src = t.poster;
    img.alt = `Pôster de ${t.nome}`;
    img.loading = "lazy";
    img.onerror = () => img.replaceWith(posterVazio(t.nome));
    li.appendChild(img);
  } else {
    li.appendChild(posterVazio(t.nome));
  }

  const info = elemento("div", "info");
  info.appendChild(elemento("div", "posicao", `#${posicao}`));
  info.appendChild(elemento("div", "nome", t.nome));

  const meta = elemento("div", "meta");
  meta.appendChild(elemento("span", "", t.tipo === "serie" ? "Série" : "Filme"));
  if (t.ano) meta.appendChild(elemento("span", "", `· ${t.ano}`));
  const idade = t.classificacao || "?";
  const classe = ["L", "10", "12", "14", "16", "18"].includes(idade) ? idade : "x";
  meta.appendChild(elemento("span", `idade idade-${classe}`, idade));
  info.appendChild(meta);

  info.appendChild(elemento("div", "compat", `${t.percentual}% compatível`));
  const barra = elemento("div", "compat-barra");
  const preenchido = document.createElement("div");
  preenchido.style.width = `${t.percentual}%`;
  barra.appendChild(preenchido);
  info.appendChild(barra);

  const notas = elemento("div", "notas");
  notas.appendChild(nota("imdb", "IMDb", t.notaImdb));
  notas.appendChild(nota("rotten", "Rotten", t.notaRotten));
  info.appendChild(notas);

  if (t.sinopse) info.appendChild(elemento("p", "sinopse", t.sinopse));

  li.appendChild(info);
  return li;
}

function nota(classe, rotulo, valor) {
  const span = elemento("span", `nota ${classe}`, `${rotulo} `);
  span.appendChild(elemento("b", "", valor || "—"));
  return span;
}

function posterVazio(nome) {
  return elemento("div", "poster-vazio", nome);
}

function elemento(tag, classe, texto) {
  const el = document.createElement(tag);
  if (classe) el.className = classe;
  if (texto !== undefined) el.textContent = texto;
  return el;
}

function mostrarErro(mensagem) {
  $("erro-texto").textContent = mensagem;
  mostrar("erro");
}

// Botões
$("comecar").addEventListener("click", comecar);
$("voltar").addEventListener("click", voltar);
$("refazer").addEventListener("click", comecar);
$("tentar").addEventListener("click", comecar);
$("logo").addEventListener("click", () => mostrar("inicio"));
