import java.util.ArrayList;

// Calcula a compatibilidade (%) de cada título e escolhe os 5 melhores.
public class Recomendador {

    private static final String[] IDADES = {"L", "10", "12", "14", "16", "18"};

    // Regra: Ano 30% + Idioma 20% + Popularidade 20% + Nota TMDB 30%
    public static void pontuar(ArrayList<Titulo> titulos, String[] r) {
        boolean recente = r[Questionario.RECENTE].equals("Recente");
        boolean nacional = r[Questionario.NACIONAL].equals("Nacional");
        boolean popular = r[Questionario.POPULAR].equals("Popular");

        // Média de votos da lista: acima dela = "popular"
        double soma = 0;
        for (Titulo t : titulos) {
            soma += t.votos;
        }
        double media = titulos.isEmpty() ? 0 : soma / titulos.size();

        for (Titulo t : titulos) {
            double compat = 0;
            if ((t.ano >= 2020) == recente) compat += 30;
            if ("pt".equals(t.idioma) == nacional) compat += 20;
            if ((t.votos >= media) == popular) compat += 20;
            compat += (t.nota / 10.0) * 30;

            t.compat = compat;
            t.percentual = (int) Math.round(compat);
        }

        // Ordena do maior para o menor
        titulos.sort((a, b) -> Double.compare(b.compat, a.compat));
    }

    // Percorre os mais compatíveis, detalha cada um e fica com 5 que respeitam a idade
    public static ArrayList<Titulo> escolherTop5(ArrayList<Titulo> ordenados, String[] r) {
        ArrayList<Titulo> top5 = new ArrayList<>();
        String idadeMax = r[Questionario.CLASSIFICACAO];
        int tentativas = 0;

        for (Titulo t : ordenados) {
            if (top5.size() == 5 || tentativas == 15) {
                break;
            }
            tentativas++;
            try {
                ApiTmdb.detalhar(t);
            } catch (Exception e) {
                continue;
            }
            if (respeitaIdade(t, idadeMax)) {
                top5.add(t);
            }
        }
        return top5;
    }

    // Filmes já vêm filtrados pela API. Séries são conferidas aqui.
    static boolean respeitaIdade(Titulo t, String idadeMax) {
        if (t.tipo.equals("filme") || idadeMax.equals("18")) {
            return true;
        }
        int nivel = posicao(t.classificacao);
        return nivel != -1 && nivel <= posicao(idadeMax);
    }

    private static int posicao(String idade) {
        for (int i = 0; i < IDADES.length; i++) {
            if (IDADES[i].equals(idade)) {
                return i;
            }
        }
        return -1; // classificação desconhecida
    }
}
