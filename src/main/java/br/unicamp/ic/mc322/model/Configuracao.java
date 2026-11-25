package br.unicamp.ic.mc322.model;

import java.io.Serializable;

/**
 * Representa as preferências globais do jogo, incluindo volumes de áudio,
 * exibição de HUD (velocímetro) e mapeamento personalizado das teclas de pilotagem.
 * Implementa {@link Serializable} para persistência em arquivo binário.
 *
 * @author Luis Gustavo
 */
public class Configuracao implements Serializable {

    private static final long serialVersionUID = 1L;

    private double volumeMusica;
    private double volumeEfeitos;
    private boolean exibirVelocimetro;
    private String teclaAcelerar;
    private String teclaFrear;
    private String teclaEsquerda;
    private String teclaDireita;

    /**
     * Constrói uma nova instância de configuração inicializada com os valores padrão.
     */
    public Configuracao() {
        restaurarPadrao();
    }

    /**
     * Restaura todas as preferências de áudio, interface e controles para os valores padrão do sistema.
     */
    public void restaurarPadrao() {
        this.volumeMusica = 0.5;
        this.volumeEfeitos = 0.5;
        this.exibirVelocimetro = true;
        this.teclaAcelerar = "W";
        this.teclaFrear = "S";
        this.teclaEsquerda = "A";
        this.teclaDireita = "D";
    }

    /**
     * Altera a tecla associada a um comando de pilotagem, impedindo teclas vazias
     * ou conflitos com teclas já atribuídas a outras ações.
     *
     * @param acao      Nome da ação a ser alterada ({@code "ACELERAR"}, {@code "FREAR"},
     *                  {@code "ESQUERDA"} ou {@code "DIREITA"}).
     * @param novaTecla Nova tecla desejada para o comando.
     * @throws IllegalArgumentException Se a ação ou a tecla forem nulas/vazias,
     *                                  se a ação for desconhecida ou se a tecla já estiver
     *                                  em uso por outro comando.
     */
    public void alterarTecla(String acao, String novaTecla) {
        if (acao == null || acao.trim().isEmpty() || novaTecla == null || novaTecla.trim().isEmpty()) {
            throw new IllegalArgumentException("Os argumentos de ação e nova tecla não podem ser nulos ou vazios.");
        }

        String acaoFormatada = acao.trim().toUpperCase();
        String teclaFormatada = novaTecla.trim().toUpperCase();

        switch (acaoFormatada) {
            case "ACELERAR":
                if (teclaFormatada.equals(this.teclaFrear)
                        || teclaFormatada.equals(this.teclaEsquerda)
                        || teclaFormatada.equals(this.teclaDireita)) {
                    throw new IllegalArgumentException("A tecla '" + teclaFormatada + "' já está associada a outra ação.");
                }
                this.teclaAcelerar = teclaFormatada;
                break;

            case "FREAR":
                if (teclaFormatada.equals(this.teclaAcelerar)
                        || teclaFormatada.equals(this.teclaEsquerda)
                        || teclaFormatada.equals(this.teclaDireita)) {
                    throw new IllegalArgumentException("A tecla '" + teclaFormatada + "' já está associada a outra ação.");
                }
                this.teclaFrear = teclaFormatada;
                break;

            case "ESQUERDA":
                if (teclaFormatada.equals(this.teclaAcelerar)
                        || teclaFormatada.equals(this.teclaFrear)
                        || teclaFormatada.equals(this.teclaDireita)) {
                    throw new IllegalArgumentException("A tecla '" + teclaFormatada + "' já está associada a outra ação.");
                }
                this.teclaEsquerda = teclaFormatada;
                break;

            case "DIREITA":
                if (teclaFormatada.equals(this.teclaAcelerar)
                        || teclaFormatada.equals(this.teclaFrear)
                        || teclaFormatada.equals(this.teclaEsquerda)) {
                    throw new IllegalArgumentException("A tecla '" + teclaFormatada + "' já está associada a outra ação.");
                }
                this.teclaDireita = teclaFormatada;
                break;

            default:
                throw new IllegalArgumentException("Ação desconhecida: " + acao);
        }
    }

    /**
     * Define o volume da trilha sonora do jogo.
     *
     * @param volume Valor decimal entre {@code 0.0} (mudo) e {@code 1.0} (máximo).
     * @throws IllegalArgumentException Se o volume estiver fora do intervalo {@code [0.0, 1.0]}.
     */
    public void setVolumeMusica(double volume) {
        if (Double.isNaN(volume) || volume < 0.0 || volume > 1.0) {
            throw new IllegalArgumentException("O volume da música deve estar entre 0.0 e 1.0.");
        }
        this.volumeMusica = volume;
    }

    /**
     * Define o volume dos efeitos sonoros do jogo.
     *
     * @param volume Valor decimal entre {@code 0.0} (mudo) e {@code 1.0} (máximo).
     * @throws IllegalArgumentException Se o volume estiver fora do intervalo {@code [0.0, 1.0]}.
     */
    public void setVolumeEfeitos(double volume) {
        if (Double.isNaN(volume) || volume < 0.0 || volume > 1.0) {
            throw new IllegalArgumentException("O volume dos efeitos deve estar entre 0.0 e 1.0.");
        }
        this.volumeEfeitos = volume;
    }

    /**
     * Define se o velocímetro deve ser exibido na tela durante as corridas.
     *
     * @param exibir {@code true} para exibir o velocímetro, {@code false} para ocultá-lo.
     */
    public void setExibirVelocimetro(boolean exibir) {
        this.exibirVelocimetro = exibir;
    }

    /**
     * Obtém o volume atual da música.
     *
     * @return Volume da música entre {@code 0.0} e {@code 1.0}.
     */
    public double getVolumeMusica() {
        return this.volumeMusica;
    }

    /**
     * Obtém o volume atual dos efeitos sonoros.
     *
     * @return Volume dos efeitos entre {@code 0.0} e {@code 1.0}.
     */
    public double getVolumeEfeitos() {
        return this.volumeEfeitos;
    }

    /**
     * Indica se o velocímetro está configurado para ser exibido durante a corrida.
     *
     * @return {@code true} se o velocímetro estiver ativado, {@code false} caso contrário.
     */
    public boolean isExibirVelocimetro() {
        return this.exibirVelocimetro;
    }

    /**
     * Obtém a tecla configurada para acelerar o veículo.
     *
     * @return Representação textual da tecla de aceleração.
     */
    public String getTeclaAcelerar() {
        return this.teclaAcelerar;
    }

    /**
     * Obtém a tecla configurada para frear ou dar ré no veículo.
     *
     * @return Representação textual da tecla de freio.
     */
    public String getTeclaFrear() {
        return this.teclaFrear;
    }

    /**
     * Obtém a tecla configurada para esterçar o veículo à esquerda.
     *
     * @return Representação textual da tecla de curva à esquerda.
     */
    public String getTeclaEsquerda() {
        return this.teclaEsquerda;
    }

    /**
     * Obtém a tecla configurada para esterçar o veículo à direita.
     *
     * @return Representação textual da tecla de curva à direita.
     */
    public String getTeclaDireita() {
        return this.teclaDireita;
    }
}