import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

// Busca as notas do IMDb e do Rotten Tomatoes na OMDb.
// Só é chamada para os 5 finalistas (limite grátis: 1.000 consultas por dia).
public class ApiOmdb {

    private static final String CHAVE = Config.ler("OMDB_KEY");

    public static void buscarNotas(Titulo t) {
        if (CHAVE == null || t.imdbId == null) {
            return;
        }
        try {
            String url = "https://www.omdbapi.com/?apikey=" + CHAVE + "&i=" + t.imdbId;
            JsonObject o = JsonParser.parseString(Http.get(url)).getAsJsonObject();

            if (o.has("imdbRating") && !o.get("imdbRating").getAsString().equals("N/A")) {
                t.notaImdb = o.get("imdbRating").getAsString();
            }
            if (o.has("Ratings")) {
                for (JsonElement e : o.getAsJsonArray("Ratings")) {
                    JsonObject nota = e.getAsJsonObject();
                    if (nota.get("Source").getAsString().equals("Rotten Tomatoes")) {
                        t.notaRotten = nota.get("Value").getAsString();
                    }
                }
            }
        } catch (Exception e) {
            // Se a OMDb falhar, o título aparece sem essas notas (o app continua)
            System.out.println("OMDb indisponivel: " + e.getMessage());
        }
    }
}
