import java.io.FileInputStream;
import java.util.Properties;

// Lê as chaves das APIs FORA do código.
// 1º procura uma variável de ambiente (usado no servidor, ex.: Render).
// 2º procura no arquivo config.properties (usado no seu PC; está no .gitignore).
public class Config {

    // private: nenhuma outra classe acessa o arquivo carregado (encapsulamento)
    private static final Properties arquivo = carregarArquivo();

    private static Properties carregarArquivo() {
        Properties props = new Properties();
        try (FileInputStream entrada = new FileInputStream("config.properties")) {
            props.load(entrada);
        } catch (Exception e) {
            // Sem arquivo: segue só com variáveis de ambiente
        }
        return props;
    }

    public static String ler(String nome) {
        String valor = System.getenv(nome);
        if (valor == null || valor.isBlank()) {
            valor = arquivo.getProperty(nome);
        }
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
