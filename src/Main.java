// Ponto de partida: liga o servidor.
public class Main {
    public static void main(String[] args) throws Exception {
        // No Render a porta vem da variável PORT; no seu PC usa 8080
        String portaTexto = System.getenv("PORT");
        int porta = (portaTexto == null) ? 8080 : Integer.parseInt(portaTexto);

        Servidor.iniciar(porta);

        System.out.println("Watch Now rodando em http://localhost:" + porta);
        if (!ApiTmdb.configurada()) {
            System.out.println("Sem TMDB_KEY: usando o modo demonstracao (dados ilustrativos).");
        }
    }
}
