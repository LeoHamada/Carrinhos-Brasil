package br.unicamp.ic.mc322.model;

import java.io.Serializable;

/**
 * Superclasse abstrata que representa um piloto participante das corridas
 * (seja o jogador humano ou um bot controlado por inteligência artificial).
 *
 * @author Luis Gustavo
 */
public abstract class Piloto implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String nome;
    protected Veiculo veiculoAtivo;

    /**
     * Constrói um piloto com nome e veículo ativo obrigatoriamente válidos.
     *
     * @param nome         Nome identificador do piloto.
     * @param veiculoAtivo Veículo inicialmente equipado pelo piloto.
     * @throws IllegalArgumentException Se o nome for nulo/vazio ou se o veículo for nulo.
     */
    public Piloto(String nome, Veiculo veiculoAtivo) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do piloto não pode ser nulo ou vazio.");
        }
        if (veiculoAtivo == null) {
            throw new IllegalArgumentException("O veículo ativo do piloto não pode ser nulo.");
        }
        this.nome = nome.trim();
        this.veiculoAtivo = veiculoAtivo;
    }

    /**
     * Obtém o nome do piloto.
     *
     * @return Nome do piloto.
     */
    public String getNome() {
        return this.nome;
    }

    /**
     * Obtém o veículo atualmente equipado pelo piloto.
     *
     * @return Instância do {@link Veiculo} ativo.
     */
    public Veiculo getVeiculoAtivo() {
        return this.veiculoAtivo;
    }

    /**
     * Substitui o veículo ativo do piloto.
     *
     * @param veiculo Novo veículo a ser utilizado pelo piloto.
     * @throws IllegalArgumentException Se o veículo informado for nulo.
     */
    public void setVeiculoAtivo(Veiculo veiculo) {
        if (veiculo == null) {
            throw new IllegalArgumentException("O veículo ativo não pode ser nulo.");
        }
        this.veiculoAtivo = veiculo;
    }
}