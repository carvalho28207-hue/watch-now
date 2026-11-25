import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;

// O "garçom": recebe os pedidos do celular, chama o back e devolve a resposta.
// Rotas:
//   GET  /                -> arquivos do front (pasta web)
//   GET  /api/perguntas   -> lista de perguntas (JSON)
//   POST /api/recomendar  -> recebe as 7 respostas e devolve o Top 5 (JSON)
//   GET  /api/status      -> "ok" (usado para manter o servidor acordado)
public class Servidor {

    private static final Gson gson = new Gson();
    private static final Path PASTA_WEB = Path.of("web").toAbsolutePath().normalize();

    public static void iniciar(int porta) throws Exception {
        HttpServer servidor = HttpServer.create(new InetSocketAddress("0.0.0.0", porta), 0);

        servidor.createContext("/api/perguntas", Servidor::perguntas);
        servidor.createContext("/api/recomendar", Servidor::recomendar);
        servidor.createContext("/api/status", ex -> responder(ex, 200, "{\"status\":\"ok\"}", "application/json"));
        servidor.createContext("/", Servidor::arquivo);

        // 20 "atendentes" trabalhando ao mesmo tempo
        servidor.setExecutor(Executors.newFixedThreadPool(20));
        servidor.start();
    }

    private static void perguntas(HttpExchange ex) throws java.io.IOException {
        responder(ex, 200, gson.toJson(Questionario.perguntas), "application/json");
    }

    private static void recomendar(HttpExchange ex) throws java.io.IOException {
        if (!ex.getRequestMethod().equals("POST")) {
            responder(ex, 405, erro("Use POST"), "application/json");
            return;
        }

        // Lê o corpo do pedido (no máximo 2 KB, para evitar abuso)
        String corpo;
        try (InputStream entrada = ex.getRequestBody()) {
            corpo = new String(entrada.readNBytes(2048), StandardCharsets.UTF_8);
        }

        String[] respostas;
        try {
            respostas = gson.fromJson(corpo, String[].class);
        } catch (Exception e) {
            respostas = null;
        }
        if (!Questionario.validar(respostas)) {
            responder(ex, 400, erro("Respostas inválidas"), "application/json");
            return;
        }

        HashMap<String, Object> resultado = new HashMap<>();
        try {
            ArrayList<Titulo> top5;
            if (ApiTmdb.configurada()) {
                ArrayList<Titulo> titulos = ApiTmdb.buscar(respostas);
                Recomendador.pontuar(titulos, respostas);
                top5 = Recomendador.escolherTop5(titulos, respostas);
                for (Titulo t : top5) {
                    ApiOmdb.buscarNotas(t);
                }
                resultado.put("modo", "real");
            } else {
                top5 = demo(respostas);
                resultado.put("modo", "demo");
            }
            resultado.put("titulos", top5);
        } catch (Exception e) {
            // API fora do ar: cai para a demonstração (redundância)
            System.out.println("Falha na API, usando demo: " + e.getMessage());
            resultado.put("titulos", demo(respostas));
            resultado.put("modo", "demo");
        }
        responder(ex, 200, gson.toJson(resultado), "application/json");
    }

    private static ArrayList<Titulo> demo(String[] respostas) {
        ArrayList<Titulo> titulos = Demo.titulos(respostas);
        Recomendador.pontuar(titulos, respostas);
        return new ArrayList<>(titulos.subList(0, Math.min(5, titulos.size())));
    }

    // Entrega index.html, style.css e app.js
    private static void arquivo(HttpExchange ex) throws java.io.IOException {
        String caminho = ex.getRequestURI().getPath();
        if (caminho.equals("/")) {
            caminho = "/index.html";
        }
        Path alvo = PASTA_WEB.resolve(caminho.substring(1)).normalize();

        // Segurança: impede pedidos como /../config.properties
        if (!alvo.startsWith(PASTA_WEB) || !Files.isRegularFile(alvo)) {
            responder(ex, 404, "Não encontrado", "text/plain");
            return;
        }
        byte[] conteudo = Files.readAllBytes(alvo);
        ex.getResponseHeaders().set("Content-Type", tipoArquivo(caminho));
        ex.sendResponseHeaders(200, conteudo.length);
        ex.getResponseBody().write(conteudo);
        ex.close();
    }

    private static String tipoArquivo(String caminho) {
        if (caminho.endsWith(".html")) return "text/html; charset=utf-8";
        if (caminho.endsWith(".css")) return "text/css; charset=utf-8";
        if (caminho.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (caminho.endsWith(".svg")) return "image/svg+xml";
        if (caminho.endsWith(".png")) return "image/png";
        return "application/octet-stream";
    }

    private static String erro(String mensagem) {
        return "{\"erro\":\"" + mensagem + "\"}";
    }

    private static void responder(HttpExchange ex, int codigo, String texto, String tipo) throws java.io.IOException {
        byte[] bytes = texto.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", tipo + (tipo.contains("charset") ? "" : "; charset=utf-8"));
        ex.sendResponseHeaders(codigo, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }
}
