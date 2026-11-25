import java.util.ArrayList;

// Modo demonstração: dados ILUSTRATIVOS usados quando não há chave do TMDB
// ou a API está fora do ar. Garante que a apresentação não fique sem resultado.
public class Demo {

    public static ArrayList<Titulo> titulos(String[] r) {
        ArrayList<Titulo> todos = new ArrayList<>();
        todos.add(filme("Vingadores: Ultimato", 2019, "en", 8.2, 26000, "12", "8.4", "94%"));
        todos.add(filme("Ainda Estou Aqui", 2024, "pt", 8.0, 900, "14", null, null));
        todos.add(filme("Duna: Parte Dois", 2024, "en", 8.1, 7000, "14", null, null));
        todos.add(filme("Divertida Mente 2", 2024, "en", 7.6, 5500, "L", null, null));
        todos.add(filme("O Auto da Compadecida", 2000, "pt", 8.4, 900, "12", null, null));
        todos.add(filme("Interestelar", 2014, "en", 8.4, 36000, "10", null, null));
        todos.add(filme("Central do Brasil", 1998, "pt", 7.9, 700, "12", null, null));
        todos.add(serie("Stranger Things", 2016, "en", 8.6, 18000, "16"));
        todos.add(serie("Sintonia", 2019, "pt", 7.8, 300, "16"));
        todos.add(serie("DNA do Crime", 2023, "pt", 7.4, 150, "16"));
        todos.add(serie("Wandinha", 2022, "en", 8.5, 9000, "14"));
        todos.add(serie("Arcane", 2021, "en", 8.7, 4500, "14"));

        String tipo = r[Questionario.TIPO].equals("Série") ? "serie" : "filme";
        ArrayList<Titulo> filtrados = new ArrayList<>();
        for (Titulo t : todos) {
            boolean idadeOk = Recomendador.respeitaIdade(comoSerie(t), r[Questionario.CLASSIFICACAO]);
            if (t.tipo.equals(tipo) && idadeOk) {
                filtrados.add(t);
            }
        }
        return filtrados;
    }

    private static Titulo filme(String nome, int ano, String idioma, double nota, int votos,
                                String idade, String imdb, String rotten) {
        Titulo t = new Titulo(0, nome, "filme", nota, ano, votos, idioma);
        t.classificacao = idade;
        t.notaImdb = imdb;
        t.notaRotten = rotten;
        return t;
    }

    private static Titulo serie(String nome, int ano, String idioma, double nota, int votos, String idade) {
        Titulo t = new Titulo(0, nome, "serie", nota, ano, votos, idioma);
        t.classificacao = idade;
        return t;
    }

    // Na demo não há filtro da API, então a idade é conferida para filmes também
    private static Titulo comoSerie(Titulo t) {
        Titulo copia = new Titulo(0, t.nome, "serie", t.nota, t.ano, t.votos, t.idioma);
        copia.classificacao = t.classificacao;
        return copia;
    }
}
