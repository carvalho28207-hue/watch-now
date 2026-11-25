// Um filme ou uma série. Cada resultado da API vira um objeto Titulo.
public class Titulo {
    int id;                 // id no TMDB
    String nome;
    String tipo;            // "filme" ou "serie"
    double nota;            // nota TMDB, de 0 a 10
    int ano;
    int votos;
    String idioma;          // ex.: "pt", "en"
    String classificacao;   // ex.: "12" (preenchida no detalhamento)
    String poster;          // link completo da imagem
    String sinopse;
    String imdbId;          // ex.: "tt4154796" (usado na OMDb)
    String notaImdb;        // ex.: "8.4"
    String notaRotten;      // ex.: "94%"
    double compat;          // compatibilidade calculada
    int percentual;         // compatibilidade arredondada (0 a 100)

    public Titulo(int id, String nome, String tipo, double nota, int ano, int votos, String idioma) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.nota = nota;
        this.ano = ano;
        this.votos = votos;
        this.idioma = idioma;
    }
}
