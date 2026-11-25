package br.unicamp.ic.mc322;

import br.unicamp.ic.mc322.exception.SaldoInsuficienteException;
import br.unicamp.ic.mc322.model.Carroceria;
import br.unicamp.ic.mc322.model.Componente;
import br.unicamp.ic.mc322.model.Jogador;
import br.unicamp.ic.mc322.model.Motor;
import br.unicamp.ic.mc322.model.Roda;
import br.unicamp.ic.mc322.model.Suspensao;
import br.unicamp.ic.mc322.model.Veiculo;
import br.unicamp.ic.mc322.service.LojaAutoPecas;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para as regras de negócio de {@link Jogador} e {@link LojaAutoPecas},
 * cobrindo economia, progressão de XP/nível, inventário, reparos, recordes e exceções.
 *
 * @author Luis Gustavo
 */
class JogadorLojaTest {

    private Jogador criarJogadorPadrao() {
        Carroceria fusca = new Carroceria("Fusca Inicial", "Fusca", 100, 1, 800.0);
        Motor motor = new Motor("Boxer 1300", 46, 120, 1, 110.0);
        Roda roda = new Roda("Pingão 13", "Urbano", 13, 80, 1);
        Suspensao susp = new Suspensao("Original", 1.0, 90, 1);
        Veiculo veiculo = new Veiculo(fusca, motor, roda, susp);

        Jogador jogador = new Jogador("Luis", veiculo);
        jogador.adicionarComponente(fusca);
        jogador.adicionarComponente(motor);
        jogador.adicionarComponente(roda);
        jogador.adicionarComponente(susp);
        return jogador;
    }

    @Test
    void construtorDePilotoValidaNomeEVeiculo() {
        Jogador jogadorValido = criarJogadorPadrao();
        Veiculo veiculo = jogadorValido.getVeiculoAtivo();

        assertThrows(IllegalArgumentException.class, () -> new Jogador(null, veiculo));
        assertThrows(IllegalArgumentException.class, () -> new Jogador("   ", veiculo));
        assertThrows(IllegalArgumentException.class, () -> new Jogador("Piloto", null));
        assertThrows(IllegalArgumentException.class, () -> jogadorValido.setVeiculoAtivo(null));
    }

    @Test
    void economiaDeMoedasPermiteGanhoEGastoNosLimitesEProtegeSaldo() {
        Jogador jogador = criarJogadorPadrao();

        jogador.adicionarMoedas(150);
        assertEquals(150, jogador.getMoedas());

        // Gasto menor que o saldo
        jogador.gastarMoedas(50);
        assertEquals(100, jogador.getMoedas());

        // Gasto maior que o saldo lança exceção e preserva o valor atual
        assertThrows(SaldoInsuficienteException.class, () -> jogador.gastarMoedas(101));
        assertEquals(100, jogador.getMoedas());

        // Gasto igual ao saldo zera moedas
        jogador.gastarMoedas(100);
        assertEquals(0, jogador.getMoedas());

        // Valores não positivos
        assertThrows(IllegalArgumentException.class, () -> jogador.adicionarMoedas(0));
        assertThrows(IllegalArgumentException.class, () -> jogador.adicionarMoedas(-10));
        assertThrows(IllegalArgumentException.class, () -> jogador.gastarMoedas(0));
        assertThrows(IllegalArgumentException.class, () -> jogador.gastarMoedas(-5));
    }

    @Test
    void adicionarXpRecalculaNivelAutomaticamente() {
        Jogador jogador = criarJogadorPadrao();
        assertEquals(1, jogador.getNivel());

        jogador.adicionarXp(99);
        assertEquals(1, jogador.getNivel());

        jogador.adicionarXp(1);
        assertEquals(100, jogador.getXp());
        assertEquals(2, jogador.getNivel());

        jogador.adicionarXp(250);
        assertEquals(350, jogador.getXp());
        assertEquals(4, jogador.getNivel());

        assertThrows(IllegalArgumentException.class, () -> jogador.adicionarXp(0));
        assertThrows(IllegalArgumentException.class, () -> jogador.adicionarXp(-20));
    }

    @Test
    void registrarRecordeAtualizaApenasQuandoTempoForMenor() {
        Jogador jogador = criarJogadorPadrao();

        jogador.registrarRecorde("Interlagos", 85.4);
        assertEquals(85.4, jogador.getRecordesPistas().get("Interlagos"), 0.0001);

        // Tempo pior não substitui o recorde
        jogador.registrarRecorde("Interlagos", 90.2);
        assertEquals(85.4, jogador.getRecordesPistas().get("Interlagos"), 0.0001);

        // Tempo melhor atualiza o recorde
        jogador.registrarRecorde("Interlagos", 79.8);
        assertEquals(79.8, jogador.getRecordesPistas().get("Interlagos"), 0.0001);

        assertThrows(IllegalArgumentException.class, () -> jogador.registrarRecorde("", 50.0));
        assertThrows(IllegalArgumentException.class, () -> jogador.registrarRecorde("Sertão", 0.0));
        assertThrows(IllegalArgumentException.class, () -> jogador.registrarRecorde("Sertão", -12.0));
    }

    @Test
    void colecoesRetornadasPorJogadorELojaSaoImutaveis() {
        Jogador jogador = criarJogadorPadrao();
        LojaAutoPecas loja = new LojaAutoPecas();
        Motor motorExtra = new Motor("Extra", 80, 200, 1, 120.0);

        assertThrows(UnsupportedOperationException.class, () -> jogador.getInventarioComponentes().add(motorExtra));
        assertThrows(UnsupportedOperationException.class, () -> jogador.getRecordesPistas().put("Sertão", 50.0));
        assertThrows(UnsupportedOperationException.class, () -> loja.listarDisponiveis().clear());
    }

    @Test
    void lojaValidaNivelMinimoESaldoAoComprarComponente() {
        Jogador jogador = criarJogadorPadrao();
        LojaAutoPecas loja = new LojaAutoPecas();

        // Item de nível 1 (Carroceria Fusca 1300, custo 100)
        Componente itemNivel1 = loja.listarDisponiveis().stream()
                .filter(c -> c.getNivelMinimo() == 1)
                .findFirst()
                .orElseThrow();

        // Sem moedas suficientes
        assertThrows(SaldoInsuficienteException.class, () -> loja.comprarComponente(jogador, itemNivel1));

        // Com moedas suficientes, compra e equipa com sucesso
        jogador.adicionarMoedas(itemNivel1.getCusto());
        loja.comprarComponente(jogador, itemNivel1);
        assertEquals(0, jogador.getMoedas());
        assertTrue(jogador.getInventarioComponentes().contains(itemNivel1));
        assertDoesNotThrow(() -> jogador.equiparComponente(itemNivel1));

        // Item de nível alto (nível mínimo >= 4) deve falhar mesmo com dinheiro sobrando
        Componente itemNivelAlto = loja.listarDisponiveis().stream()
                .filter(c -> c.getNivelMinimo() >= 4)
                .findFirst()
                .orElseThrow();

        jogador.adicionarMoedas(5000);
        assertThrows(SaldoInsuficienteException.class, () -> loja.comprarComponente(jogador, itemNivelAlto));
        assertFalse(jogador.getInventarioComponentes().contains(itemNivelAlto));
        assertEquals(5000, jogador.getMoedas());
    }

    @Test
    void lojaAplicaMelhoriaAteNivelMaximoEDebitaMoedas() {
        Jogador jogador = criarJogadorPadrao();
        LojaAutoPecas loja = new LojaAutoPecas();
        Componente alvo = jogador.getVeiculoAtivo().getMotor();

        // Sem moedas para o upgrade nível 0 -> 1 (custo 100)
        assertEquals(100, loja.calcularCustoMelhoria(alvo));
        assertThrows(SaldoInsuficienteException.class, () -> loja.comprarMelhoria(jogador, alvo));
        assertEquals(0, alvo.getNivelMelhoria());

        // Compra todos os 5 níveis (100 + 200 + 400 + 750 + 1200 = 2650 moedas)
        jogador.adicionarMoedas(2650);
        for (int nivelEsperado = 1; nivelEsperado <= 5; nivelEsperado++) {
            loja.comprarMelhoria(jogador, alvo);
            assertEquals(nivelEsperado, alvo.getNivelMelhoria());
        }
        assertEquals(0, jogador.getMoedas());

        // Após nível máximo, lança IllegalStateException
        assertThrows(IllegalStateException.class, () -> loja.calcularCustoMelhoria(alvo));
        assertThrows(IllegalStateException.class, () -> loja.comprarMelhoria(jogador, alvo));
    }

    @Test
    void repararComponenteDesgastadoCobraMoedasERestauraDurabilidade() {
        Jogador jogador = criarJogadorPadrao();
        Componente motor = jogador.getVeiculoAtivo().getMotor();
        double durabilidadeInicial = motor.getDurabilidade();

        motor.desgastar(30.0);
        assertTrue(motor.getDurabilidade() < durabilidadeInicial);

        int custoReparo = motor.calcularCustoReparo();
        if (custoReparo > 0) {
            // Sem saldo suficiente não repara
            assertThrows(SaldoInsuficienteException.class, () -> jogador.repararComponente(motor));
            assertTrue(motor.getDurabilidade() < durabilidadeInicial);

            jogador.adicionarMoedas(custoReparo);
        }

        jogador.repararComponente(motor);
        assertEquals(0, jogador.getMoedas());
        assertEquals(durabilidadeInicial, motor.getDurabilidade(), 0.001);
    }

    @Test
    void equiparOuRepararComponenteForaDoInventarioLancaExcecao() {
        Jogador jogador = criarJogadorPadrao();
        Motor motorExterno = new Motor("Externo", 120, 300, 1, 130.0);

        assertThrows(IllegalArgumentException.class, () -> jogador.equiparComponente(motorExterno));
        assertThrows(IllegalArgumentException.class, () -> jogador.repararComponente(motorExterno));
    }
}