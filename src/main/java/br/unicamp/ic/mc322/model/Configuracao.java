package br.unicamp.ic.mc322.model;
import java.io.Serializable;

public class Configuracao implements Serializable {
    private static final long serialVersionUID = 1L;
    private double volumeMusica;
    private double volumeEfeitos;
    private boolean exibirVelocimetro;
    private String teclaAcelerar;
    private String teclaFrear;
    private String teclaEsquerda;
    private String teclaDireita;

    public void restaurarPadrao(){
        this.volumeMusica=0.5;
        this.volumeEfeitos=0.5;
        this.exibirVelocimetro=true;
        this.teclaAcelerar="W";
        this.teclaFrear="S";
        this.teclaEsquerda="A";
        this.teclaDireita="D";
    }

    Configuracao(){
        restaurarPadrao();
    }

    public void alterarTecla(String acao, String novaTecla){
        if(acao.trim().isEmpty() || novaTecla.trim().isEmpty()){
            throw new IllegalArgumentException("Argumentos de acao ou novaTecla vazios");
        } 
        String teclaFormatada = novaTecla.trim().toUpperCase();
        if(teclaFormatada.trim().equals(teclaAcelerar) || teclaFormatada.trim().equals(teclaDireita) || teclaFormatada.trim().equals(teclaEsquerda) || teclaFormatada.trim().equals(teclaFrear)){
            throw new IllegalArgumentException("Tecla ja possui outra ação");
        }
        switch(acao.trim().toUpperCase()){
            case "ACELERAR":
                teclaAcelerar=teclaFormatada;
                break;
            case "FREAR":
                teclaFrear=teclaFormatada;
                break;
            case "DIREITA":
                teclaFormatada=teclaDireita;
                break;
            case "ESQUERDA":
                teclaFormatada=teclaEsquerda;
                break;
            default:
                throw new lllegalArgumentException("Ação desconhecida: "+acao);
                break;
        }
    }

    public void setVolumeMusica(double volume){
        if(volume<0.00 || volume>1.0){
            throw new IllegalArgumentException("Volume setado fora do intervalo 0 e 1");
        }
        this.volumeMusica=volume;
    }

    public void setVolumeEfeitos(double volume){
        if(volume<0.00 || volume>1.0){
            throw new IllegalArgumentException("Volume setado fora do intervalo 0 e 1");
        }
        this.volumeEfeitos=volume;
    }

    public void setExibirVelocimetro(boolean exibir){
        this.exibirVelocimetro=exibir;
    }

    public double getVolumeMusica(){
        return volumeMusica;
    }

    public double getVolumeEfeitos(){
        return volumeEfeitos;
    }

    public boolean isExibirVelocimetro(){
        return exibirVelocimetro;
    }

    public String getTeclaAcelerar(){
        return teclaAcelerar;
    }

    public String getTeclaFrear(){
        return teclaFrear;
    }

    public String getTeclaDireita(){
        return teclaDireita;
    }

    public String getTeclaesquerda(){
        return teclaEsquerda;
    }
}