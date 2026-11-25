import java.util.ArrayList;

// Guarda as 7 perguntas. O front pede a lista ao servidor e mostra uma por vez.
public class Questionario {

    // Posição de cada resposta no array respostas[]
    public static final int TIPO = 0;
    public static final int STREAMING = 1;
    public static final int GENERO = 2;
    public static final int CLASSIFICACAO = 3;
    public static final int RECENTE = 4;
    public static final int NACIONAL = 5;
    public static final int POPULAR = 6;

    public static final ArrayList<Pergunta> perguntas = new ArrayList<>();

    static {
        // Filtram a busca (peso 0)
        perguntas.add(new Pergunta("Filme ou série?",
                new String[]{"Filme", "Série"}, 0));
        perguntas.add(new Pergunta("Qual streaming você tem?",
                new String[]{"Netflix", "Prime Video", "Disney+", "HBO Max", "Globoplay", "Apple TV+"}, 0));
        perguntas.add(new Pergunta("Qual gênero você quer ver?",
                new String[]{"Ação", "Comédia", "Drama", "Animação", "Crime", "Ficção científica", "Suspense", "Documentário"}, 0));
        perguntas.add(new Pergunta("Classificação máxima?",
                new String[]{"L", "10", "12", "14", "16", "18"}, 0));

        // Pontuam (30 + 20 + 20; os outros 30% vêm da nota do TMDB)
        perguntas.add(new Pergunta("Lançamento recente ou clássico?",
                new String[]{"Recente", "Clássico"}, 30));
        perguntas.add(new Pergunta("Produção nacional ou estrangeira?",
                new String[]{"Nacional", "Estrangeira"}, 20));
        perguntas.add(new Pergunta("Popular ou pouco conhecido?",
                new String[]{"Popular", "Pouco conhecido"}, 20));
    }

    public static int total() {
        return perguntas.size();
    }

    // true se vieram 7 respostas e todas são opções válidas
    public static boolean validar(String[] respostas) {
        if (respostas == null || respostas.length != total()) {
            return false;
        }
        for (int i = 0; i < total(); i++) {
            if (!perguntas.get(i).aceita(respostas[i])) {
                return false;
            }
        }
        return true;
    }
}
