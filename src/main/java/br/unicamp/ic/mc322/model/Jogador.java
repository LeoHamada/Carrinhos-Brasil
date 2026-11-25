package br.unicamp.ic.mc322.model;

import br.unicamp.ic.mc322.exception.SaldoInsuficienteException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Representa o perfil do jogador humano, mantendo sua economia (moedas),
 * progressão (XP e nível), inventário polimórfico de componentes na garagem
 * e melhores tempos (recordes) em cada pista.
 *
 * @author Luis Gustavo
 */
public class Jogador extends Piloto implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int XP_POR_NIVEL = 100;

    private int moedas;
    private int xp;
    private int nivel;
    private List<Componente> inventarioComponentes;
    private Map<String, Double> recordesPistas;

    /**
     * Cria um novo perfil de jogador com saldo zerado, nível inicial 1,
     * inventário vazio e veículo inicial configurado.
     *
     * @param nome           Nome do perfil do jogador.
     * @param veiculoInicial Veículo inicial atribuído ao jogador.
     * @throws IllegalArgumentException Se o nome for inválido ou o veículo inicial for nulo.
     */
    public Jogador(String nome, Veiculo veiculoInicial) {
        super(nome, veiculoInicial);
        this.moedas = 0;
        this.xp = 0;
        this.nivel = 1;
        this.inventarioComponentes = new ArrayList<>();
        this.recordesPistas = new HashMap<>();
    }

    /**
     * Adiciona moedas ao saldo do jogador (ex.: premiações de campeonato ou ajuda de custo no treino).
     *
     * @param valor Quantidade positiva de moedas a adicionar.
     * @throws IllegalArgumentException Se o valor for menor ou igual a zero.
     */
    public void adicionarMoedas(int valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("O valor de moedas adicionado deve ser positivo.");
        }
        this.moedas += valor;
    }

    /**
     * Debita moedas do saldo do jogador para compras, melhorias ou reparos.
     *
     * @param valor Quantidade positiva de moedas a debitar.
     * @throws IllegalArgumentException   Se o valor for menor ou igual a zero.
     * @throws SaldoInsuficienteException Se o jogador não possuir moedas suficientes.
     */
    public void gastarMoedas(int valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("O valor de moedas gasto deve ser positivo.");
        }
        if (valor > this.moedas) {
            throw new SaldoInsuficienteException(
                    "Moedas insuficientes. Necessário: " + valor + ", disponível: " + this.moedas + ".");
        }
        this.moedas -= valor;
    }

    /**
     * Adiciona pontos de experiência (XP) ao jogador e recalcula seu nível automaticamente.
     *
     * @param valor Quantidade positiva de XP obtida.
     * @throws IllegalArgumentException Se o valor for menor ou igual a zero.
     */
    public void adicionarXp(int valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("O valor de XP adicionado deve ser positivo.");
        }
        this.xp += valor;
        this.nivel = 1 + (this.xp / XP_POR_NIVEL);
    }

    /**
     * Restaura moedas, XP e nível diretamente ao carregar um perfil persistido.
     *
     * @param moedas Saldo salvo em arquivo (não negativo).
     * @param xp     Experiência acumulada salva em arquivo (não negativa).
     * @param nivel  Nível salvo em arquivo (mínimo 1).
     * @throws IllegalArgumentException Se algum valor violar as regras de domínio.
     */
    public void restaurarProgresso(int moedas, int xp, int nivel) {
        if (moedas < 0 || xp < 0 || nivel < 1) {
            throw new IllegalArgumentException("Dados de progresso inválidos para restauração do jogador.");
        }
        this.moedas = moedas;
        this.xp = xp;
        this.nivel = nivel;
    }

    /**
     * Adiciona um componente adquirido ao inventário da garagem do jogador.
     *
     * @param comp Componente a ser adicionado.
     * @throws IllegalArgumentException Se o componente for nulo.
     */
    public void adicionarComponente(Componente comp) {
        if (comp == null) {
            throw new IllegalArgumentException("O componente adicionado ao inventário não pode ser nulo.");
        }
        if (!this.inventarioComponentes.contains(comp)) {
            this.inventarioComponentes.add(comp);
        }
    }

    /**
     * Equipa no veículo ativo um componente que pertença ao inventário do jogador.
     *
     * @param comp Componente do inventário a ser equipado.
     * @throws IllegalArgumentException Se o componente for nulo, não estiver no inventário
     *                                  ou não corresponder a uma categoria suportada.
     */
    public void equiparComponente(Componente comp) {
        if (comp == null) {
            throw new IllegalArgumentException("O componente a equipar não pode ser nulo.");
        }
        if (!this.inventarioComponentes.contains(comp)) {
            throw new IllegalArgumentException("O componente '" + comp.getNome() + "' não está no inventário do jogador.");
        }

        if (comp instanceof Carroceria) {
            this.veiculoAtivo.trocarCarroceria((Carroceria) comp);
        } else if (comp instanceof Motor) {
            this.veiculoAtivo.trocarMotor((Motor) comp);
        } else if (comp instanceof Roda) {
            this.veiculoAtivo.trocarRoda((Roda) comp);
        } else if (comp instanceof Suspensao) {
            this.veiculoAtivo.trocarSuspensao((Suspensao) comp);
        } else {
            throw new IllegalArgumentException("Categoria de componente não suportada pelo veículo.");
        }
    }

    /**
     * Realiza o reparo de durabilidade de um componente do jogador debitando o custo em moedas.
     *
     * @param comp Componente a ser reparado.
     * @throws IllegalArgumentException   Se o componente for nulo ou não pertencer ao inventário.
     * @throws SaldoInsuficienteException Se o jogador não tiver moedas suficientes para pagar o reparo.
     */
    public void repararComponente(Componente comp) {
        if (comp == null) {
            throw new IllegalArgumentException("O componente a reparar não pode ser nulo.");
        }
        if (!this.inventarioComponentes.contains(comp)) {
            throw new IllegalArgumentException("O componente '" + comp.getNome() + "' não está no inventário do jogador.");
        }

        int custoReparo = comp.calcularCustoReparo();
        if (custoReparo > 0) {
            gastarMoedas(custoReparo);
        }
        comp.reparar();
    }

    /**
     * Registra o tempo concluído em uma pista, atualizando o recorde apenas
     * se for a primeira marca ou se o tempo for menor que o recorde anterior.
     *
     * @param nomePista Nome identificador da pista.
     * @param tempo     Tempo total concluído em segundos (deve ser positivo).
     * @throws IllegalArgumentException Se o nome da pista for inválido ou o tempo não for positivo.
     */
    public void registrarRecorde(String nomePista, double tempo) {
        if (nomePista == null || nomePista.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome da pista não pode ser nulo ou vazio.");
        }
        if (Double.isNaN(tempo) || tempo <= 0.0) {
            throw new IllegalArgumentException("O tempo registrado na pista deve ser positivo.");
        }

        String chavePista = nomePista.trim();
        Double recordeAtual = this.recordesPistas.get(chavePista);
        if (recordeAtual == null || tempo < recordeAtual) {
            this.recordesPistas.put(chavePista, tempo);
        }
    }

    /**
     * Obtém o saldo atual de moedas do jogador.
     *
     * @return Quantidade de moedas disponíveis.
     */
    public int getMoedas() {
        return this.moedas;
    }

    /**
     * Obtém o total de pontos de experiência (XP) acumulados pelo jogador.
     *
     * @return Pontuação de XP atual.
     */
    public int getXp() {
        return this.xp;
    }

    /**
     * Obtém o nível atual do jogador, utilizado como requisito na loja.
     *
     * @return Nível atual do piloto.
     */
    public int getNivel() {
        return this.nivel;
    }

    /**
     * Retorna uma visão imutável dos componentes pertencentes ao inventário do jogador.
     *
     * @return Lista não modificável de {@link Componente}.
     */
    public List<Componente> getInventarioComponentes() {
        return Collections.unmodifiableList(this.inventarioComponentes);
    }

    /**
     * Retorna uma visão imutável dos melhores tempos registrados pelo jogador por pista.
     *
     * @return Mapa não modificável associando nome da pista ao melhor tempo em segundos.
     */
    public Map<String, Double> getRecordesPistas() {
        return Collections.unmodifiableMap(this.recordesPistas);
    }
}