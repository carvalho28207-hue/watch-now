// Uma pergunta do questionário.
// peso = 0  -> a resposta FILTRA a busca na API (não vale pontos)
// peso > 0  -> a resposta PONTUA no cálculo de compatibilidade (%)
public class Pergunta {
    String texto;
    String[] opcoes;
    int peso;

    public Pergunta(String texto, String[] opcoes, int peso) {
        this.texto = texto;
        this.opcoes = opcoes;
        this.peso = peso;
    }

    // Confere se a resposta recebida é uma das opções (evita dados inválidos)
    public boolean aceita(String resposta) {
        for (String opcao : opcoes) {
            if (opcao.equals(resposta)) {
                return true;
            }
        }
        return false;
    }
}
