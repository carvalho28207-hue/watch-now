import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

// Faz as chamadas GET para as APIs externas e guarda as respostas (cache).
// Com o cache, se 80 pessoas pedirem a mesma coisa, a API é chamada uma vez só.
public class Http {

    private static final HttpClient cliente = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    // ConcurrentHashMap: um HashMap seguro para vários usuários ao mesmo tempo
    private static final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    public static String get(String url) throws Exception {
        String guardado = cache.get(url);
        if (guardado != null) {
            return guardado;
        }

        HttpRequest pedido = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        HttpResponse<String> resposta = cliente.send(pedido, HttpResponse.BodyHandlers.ofString());

        if (resposta.statusCode() != 200) {
            throw new Exception("API respondeu " + resposta.statusCode());
        }
        if (cache.size() < 5000) {
            cache.put(url, resposta.body());
        }
        return resposta.body();
    }
}
