package br.unicamp.ic.mc322.service;

import br.unicamp.ic.mc322.exception.SaldoInsuficienteException;
import br.unicamp.ic.mc322.model.Carroceria;
import br.unicamp.ic.mc322.model.Componente;
import br.unicamp.ic.mc322.model.Jogador;
import br.unicamp.ic.mc322.model.Motor;
import br.unicamp.ic.mc322.model.Roda;
import br.unicamp.ic.mc322.model.Suspensao;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serviço responsável pelo catálogo da loja Auto-Peças, gerenciando a compra
 * de novas carrocerias/componentes e a aplicação de melhorias (upgrades)
 * mediante validação de moedas e nível mínimo do jogador.
 *
 * @author Luis Gustavo
 */
public class LojaAutoPecas {

    private List<Componente> catalogoComponentes;
    private Map<Integer, Integer> tabelaPrecosUpgrade;

    /**
     * Inicializa a loja com o catálogo padrão de carros e peças brasileiras
     * e a tabela progressiva de custos para melhorias.
     */
    public LojaAutoPecas() {
        this.catalogoComponentes = new ArrayList<>();
        this.tabelaPrecosUpgrade = new HashMap<>();
        inicializarCatalogoPadrao();
        inicializarTabelaUpgrades();
    }

    /**
     * Popula o catálogo inicial com carrocerias icônicas brasileiras e componentes de desempenho.
     */
    private void inicializarCatalogoPadrao() {
        // Carrocerias clássicas brasileiras
        this.catalogoComponentes.add(new Carroceria("Carroceria Fusca 1300", "Fusca", 100, 1, 800.0));
        this.catalogoComponentes.add(new Carroceria("Carroceria Gol Quadrado", "Gol", 250, 1, 910.0));
        this.catalogoComponentes.add(new Carroceria("Carroceria Brasília", "Brasília", 320, 2, 890.0));
        this.catalogoComponentes.add(new Carroceria("Carroceria Uno com Escada", "Uno com Escada", 450, 2, 840.0));
        this.catalogoComponentes.add(new Carroceria("Carroceria Gurgel X12", "Gurgel", 500, 3, 780.0));
        this.catalogoComponentes.add(new Carroceria("Carroceria Opala Diplomata", "Opala", 750, 4, 1250.0));
        this.catalogoComponentes.add(new Carroceria("Carroceria Troller T4", "Troller", 900, 5, 1650.0));

        // Motores
        this.catalogoComponentes.add(new Motor("Motor Boxer 1300", 46, 120, 1, 110.0));
        this.catalogoComponentes.add(new Motor("Motor AP 1.8", 99, 300, 2, 145.0));
        this.catalogoComponentes.add(new Motor("Motor 4.1 6 Cilindros", 171, 650, 4, 230.0));

        // Rodas
        this.catalogoComponentes.add(new Roda("Roda Pingão Aro 13", "Urbano", 13, 80, 1));
        this.catalogoComponentes.add(new Roda("Roda Orbital Aro 15", "Esportivo", 15, 220, 2));
        this.catalogoComponentes.add(new Roda("Roda Off-Road Mud Aro 16", "Off-Road", 16, 380, 3));

        // Suspensões
        this.catalogoComponentes.add(new Suspensao("Suspensão Original", 1.0, 90, 1));
        this.catalogoComponentes.add(new Suspensao("Suspensão Esportiva Rebaixada", 1.6, 260, 2));
        this.catalogoComponentes.add(new Suspensao("Suspensão Reforçada Sertão", 2.0, 420, 3));
    }

    /**
     * Define os custos em moedas para evoluir um componente a partir do seu nível atual de melhoria.
     */
    private void inicializarTabelaUpgrades() {
        this.tabelaPrecosUpgrade.put(0, 100);
        this.tabelaPrecosUpgrade.put(1, 200);
        this.tabelaPrecosUpgrade.put(2, 400);
        this.tabelaPrecosUpgrade.put(3, 750);
        this.tabelaPrecosUpgrade.put(4, 1200);
    }

    /**
     * Lista todos os componentes disponíveis para consulta e compra na Auto-Peças.
     *
     * @return Lista imutável contendo os componentes do catálogo.
     */
    public List<Componente> listarDisponiveis() {
        return Collections.unmodifiableList(this.catalogoComponentes);
    }

    /**
     * Efetua a venda de um componente do catálogo para o jogador, validando
     * requisito de nível mínimo e saldo em moedas antes de debitar.
     *
     * @param jogador Jogador que está realizando a compra.
     * @param item    Componente selecionado do catálogo.
     * @throws IllegalArgumentException   Se jogador/item forem nulos ou se o item não existir no catálogo.
     * @throws SaldoInsuficienteException Se o jogador não atingir o nível mínimo ou não tiver moedas suficientes.
     */
    public void comprarComponente(Jogador jogador, Componente item) {
        if (jogador == null || item == null) {
            throw new IllegalArgumentException("Jogador e componente não podem ser nulos.");
        }
        if (!this.catalogoComponentes.contains(item)) {
            throw new IllegalArgumentException("O item '" + item.getNome() + "' não está disponível no catálogo da loja.");
        }
        if (jogador.getNivel() < item.getNivelMinimo()) {
            throw new SaldoInsuficienteException(
                    "Nível insuficiente para comprar '" + item.getNome()
                            + "'. Nível mínimo: " + item.getNivelMinimo()
                            + ", nível atual: " + jogador.getNivel() + ".");
        }

        jogador.gastarMoedas(item.getCusto());
        jogador.adicionarComponente(item);
    }

    /**
     * Aplica uma melhoria (upgrade) em um componente pertencente ao jogador,
     * debitando o valor correspondente ao nível atual de melhoria da peça.
     *
     * @param jogador Jogador proprietário do componente.
     * @param alvo    Componente que receberá o upgrade.
     * @throws IllegalArgumentException   Se jogador/alvo forem nulos ou se o componente não pertencer ao jogador.
     * @throws IllegalStateException      Se o componente já estiver no nível máximo de melhoria.
     * @throws SaldoInsuficienteException Se o jogador não tiver moedas suficientes para o upgrade.
     */
    public void comprarMelhoria(Jogador jogador, Componente alvo) {
        if (jogador == null || alvo == null) {
            throw new IllegalArgumentException("Jogador e componente alvo não podem ser nulos.");
        }
        if (!jogador.getInventarioComponentes().contains(alvo)) {
            throw new IllegalArgumentException("O componente '" + alvo.getNome() + "' não pertence ao inventário do jogador.");
        }

        int custoUpgrade = calcularCustoMelhoria(alvo);
        jogador.gastarMoedas(custoUpgrade);
        alvo.aplicarMelhoria();
    }

    /**
     * Calcula o custo em moedas necessário para aprimorar um componente para o próximo nível.
     *
     * @param alvo Componente a ser avaliado.
     * @return Valor inteiro em moedas exigido para o próximo upgrade.
     * @throws IllegalArgumentException Se o componente alvo for nulo.
     * @throws IllegalStateException    Se o componente já tiver atingido o nível máximo de melhoria.
     */
    public int calcularCustoMelhoria(Componente alvo) {
        if (alvo == null) {
            throw new IllegalArgumentException("O componente alvo não pode ser nulo.");
        }

        Integer preco = this.tabelaPrecosUpgrade.get(alvo.getNivelMelhoria());
        if (preco == null) {
            throw new IllegalStateException(
                    "O componente '" + alvo.getNome() + "' já atingiu o nível máximo de melhoria.");
        }
        return preco;
    }
}