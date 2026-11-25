import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;

// Conversa com a API do TMDB: busca filmes/séries e depois detalha os melhores.
public class ApiTmdb {

    // A chave vem de fora do código (Config) e fica private, sem getter
    private static final String CHAVE = Config.ler("TMDB_KEY");
    private static final String BASE = "https://api.themoviedb.org/3";
    private static final String IMAGEM = "https://image.tmdb.org/t/p/w342";

    public static boolean configurada() {
        return CHAVE != null;
    }

    // 1) Busca títulos usando as respostas de filtro (tipo, streaming, gênero, classificação)
    public static ArrayList<Titulo> buscar(String[] r) throws Exception {
        boolean serie = r[Questionario.TIPO].equals("Série");
        String tipoApi = serie ? "tv" : "movie";

        String url = BASE + "/discover/" + tipoApi
                + "?api_key=" + CHAVE
                + "&language=pt-BR"
                + "&watch_region=BR"
                + "&with_watch_providers=" + idStreaming(r[Questionario.STREAMING])
                + "&with_genres=" + idGenero(r[Questionario.GENERO], serie)
                + "&vote_count.gte=30"
                + "&sort_by=popularity.desc";

        // Filtro de idade direto na API só existe para filmes
        if (!serie) {
            url += "&certification_country=BR&certification.lte=" + r[Questionario.CLASSIFICACAO];
        }

        ArrayList<Titulo> titulos = new ArrayList<>();
        for (int pagina = 1; pagina <= 2; pagina++) {
            JsonObject json = JsonParser.parseString(Http.get(url + "&page=" + pagina)).getAsJsonObject();
            JsonArray resultados = json.getAsJsonArray("results");
            for (JsonElement item : resultados) {
                titulos.add(criarTitulo(item.getAsJsonObject(), serie));
            }
        }
        return titulos;
    }

    // Transforma um item do JSON em um objeto Titulo
    private static Titulo criarTitulo(JsonObject o, boolean serie) {
        String nome = texto(o, serie ? "name" : "title");
        String data = texto(o, serie ? "first_air_date" : "release_date");
        int ano = (data != null && data.length() >= 4) ? Integer.parseInt(data.substring(0, 4)) : 0;

        Titulo t = new Titulo(
                o.get("id").getAsInt(),
                nome,
                serie ? "serie" : "filme",
                o.get("vote_average").getAsDouble(),
                ano,
                o.get("vote_count").getAsInt(),
                texto(o, "original_language"));

        String caminhoPoster = texto(o, "poster_path");
        if (caminhoPoster != null) {
            t.poster = IMAGEM + caminhoPoster;
        }
        t.sinopse = texto(o, "overview");
        return t;
    }

    // 2) Detalha um título: classificação indicativa no Brasil e id do IMDb
    public static void detalhar(Titulo t) throws Exception {
        boolean serie = t.tipo.equals("serie");
        String extra = serie ? "content_ratings,external_ids" : "release_dates,external_ids";
        String url = BASE + "/" + (serie ? "tv" : "movie") + "/" + t.id
                + "?api_key=" + CHAVE
                + "&language=pt-BR"
                + "&append_to_response=" + extra;

        JsonObject o = JsonParser.parseString(Http.get(url)).getAsJsonObject();
        t.imdbId = texto(o.getAsJsonObject("external_ids"), "imdb_id");
        t.classificacao = serie ? classificacaoSerie(o) : classificacaoFilme(o);
    }

    private static String classificacaoFilme(JsonObject o) {
        JsonArray paises = o.getAsJsonObject("release_dates").getAsJsonArray("results");
        for (JsonElement p : paises) {
            JsonObject pais = p.getAsJsonObject();
            if ("BR".equals(texto(pais, "iso_3166_1"))) {
                for (JsonElement lancamento : pais.getAsJsonArray("release_dates")) {
                    String c = texto(lancamento.getAsJsonObject(), "certification");
                    if (c != null) {
                        return c;
                    }
                }
            }
        }
        return null;
    }

    private static String classificacaoSerie(JsonObject o) {
        JsonArray paises = o.getAsJsonObject("content_ratings").getAsJsonArray("results");
        for (JsonElement p : paises) {
            JsonObject pais = p.getAsJsonObject();
            if ("BR".equals(texto(pais, "iso_3166_1"))) {
                return texto(pais, "rating");
            }
        }
        return null;
    }

    // Lê um campo de texto; devolve null se não existir ou estiver vazio
    private static String texto(JsonObject o, String campo) {
        if (o == null || !o.has(campo) || o.get(campo).isJsonNull()) {
            return null;
        }
        String valor = o.get(campo).getAsString();
        return valor.isBlank() ? null : valor;
    }

    // IDs de streaming do TMDB (conferir com /watch/providers/movie?watch_region=BR)
    private static int idStreaming(String nome) {
        if (nome.equals("Netflix")) return 8;
        if (nome.equals("Prime Video")) return 119;
        if (nome.equals("Disney+")) return 337;
        if (nome.equals("HBO Max")) return 1899;
        if (nome.equals("Globoplay")) return 307;
        return 350; // Apple TV+
    }

    // Filmes e séries usam números de gênero diferentes no TMDB
    private static int idGenero(String nome, boolean serie) {
        if (nome.equals("Ação")) return serie ? 10759 : 28;
        if (nome.equals("Comédia")) return 35;
        if (nome.equals("Drama")) return 18;
        if (nome.equals("Animação")) return 16;
        if (nome.equals("Crime")) return 80;
        if (nome.equals("Ficção científica")) return serie ? 10765 : 878;
        if (nome.equals("Suspense")) return serie ? 9648 : 53;
        return 99; // Documentário
    }
}
