package br.unicamp.ic.mc322;

import br.unicamp.ic.mc322.exception.PersistenciaException;
import br.unicamp.ic.mc322.model.Carroceria;
import br.unicamp.ic.mc322.model.Configuracao;
import br.unicamp.ic.mc322.model.Jogador;
import br.unicamp.ic.mc322.model.Motor;
import br.unicamp.ic.mc322.model.Roda;
import br.unicamp.ic.mc322.model.Suspensao;
import br.unicamp.ic.mc322.model.Veiculo;
import br.unicamp.ic.mc322.persistence.GerenciadorSave;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para {@link GerenciadorSave}, validando a persistência binária de
 * {@link Configuracao}, a persistência em XML de múltiplos perfis de {@link Jogador}
 * e o tratamento de falhas com {@link PersistenciaException}.
 *
 * @author Luis Gustavo
 */
class GerenciadorSaveTest {

    @TempDir
    Path pastaTemporaria;

    private GerenciadorSave criarGerenciador() {
        String dirSaves = pastaTemporaria.resolve("saves").toString();
        String arqConfig = pastaTemporaria.resolve("config.dat").toString();
        return new GerenciadorSave(dirSaves, arqConfig);
    }

    private Jogador criarJogadorCompleto(String nome) {
        Carroceria carroceria = new Carroceria("Carroceria Opala Diplomata", "Opala", 750, 4, 1250.0);
        Motor motor = new Motor("Motor 4.1 6 Cilindros", 171, 650, 4, 230.0);
        motor.aplicarMelhoria();
        motor.desgastar(15.0);
        Roda roda = new Roda("Roda Orbital Aro 15", "Esportivo", 15, 220, 2);
        Suspensao suspensao = new Suspensao("Suspensão Esportiva", 1.6, 260, 2);

        Veiculo veiculo = new Veiculo(carroceria, motor, roda, suspensao);
        Jogador jogador = new Jogador(nome, veiculo);
        jogador.adicionarMoedas(840);
        jogador.adicionarXp(320); // Nível 4
        jogador.adicionarComponente(carroceria);
        jogador.adicionarComponente(motor);
        jogador.adicionarComponente(roda);
        jogador.adicionarComponente(suspensao);

        // Peça reserva no inventário (não equipada)
        Carroceria uno = new Carroceria("Carroceria Uno com Escada", "Uno com Escada", 450, 2, 840.0);
        jogador.adicionarComponente(uno);

        jogador.registrarRecorde("Interlagos", 74.25);
        jogador.registrarRecorde("Sertão", 91.80);
        return jogador;
    }

    @Test
    void construtorValidaCaminhosNulosOuVazios() {
        assertThrows(IllegalArgumentException.class, () -> new GerenciadorSave(null, "config.dat"));
        assertThrows(IllegalArgumentException.class, () -> new GerenciadorSave("   ", "config.dat"));
        assertThrows(IllegalArgumentException.class, () -> new GerenciadorSave("saves", null));
        assertThrows(IllegalArgumentException.class, () -> new GerenciadorSave("saves", "  "));
    }

    @Test
    void salvarECarregarConfiguracaoBinarioPreservaPreferencias() {
        GerenciadorSave gerenciador = criarGerenciador();
        Configuracao original = new Configuracao();
        original.setVolumeMusica(0.85);
        original.setVolumeEfeitos(0.20);
        original.setExibirVelocimetro(false);
        original.alterarTecla("ACELERAR", "UP");
        original.alterarTecla("FREAR", "DOWN");

        gerenciador.salvarConfigBinario(original);
        Configuracao recuperada = gerenciador.carregarConfigBinario();

        assertEquals(0.85, recuperada.getVolumeMusica(), 0.0001);
        assertEquals(0.20, recuperada.getVolumeEfeitos(), 0.0001);
        assertFalse(recuperada.isExibirVelocimetro());
        assertEquals("UP", recuperada.getTeclaAcelerar());
        assertEquals("DOWN", recuperada.getTeclaFrear());
        assertEquals("A", recuperada.getTeclaEsquerda());
        assertEquals("D", recuperada.getTeclaDireita());
    }

    @Test
    void operacoesInvalidasDeConfiguracaoBinariaLancamPersistenciaException() throws IOException {
        GerenciadorSave gerenciador = criarGerenciador();

        // Salvar nulo
        assertThrows(PersistenciaException.class, () -> gerenciador.salvarConfigBinario(null));

        // Carregar inexistente
        assertThrows(PersistenciaException.class, gerenciador::carregarConfigBinario);

        // Carregar vazio
        File arquivoBin = pastaTemporaria.resolve("config.dat").toFile();
        assertTrue(arquivoBin.createNewFile());
        assertThrows(PersistenciaException.class, gerenciador::carregarConfigBinario);

        // Carregar corrompido
        try (FileWriter writer = new FileWriter(arquivoBin)) {
            writer.write("bytes-corrompidos-nao-serializados");
        }
        assertThrows(PersistenciaException.class, gerenciador::carregarConfigBinario);
    }

    @Test
    void salvarECarregarJogadorXmlPreservaEstadoCompletoERecordes() {
        GerenciadorSave gerenciador = criarGerenciador();
        Jogador original = criarJogadorCompleto("LuisGustavo");

        gerenciador.salvarJogadorTexto(original);
        Jogador recuperado = gerenciador.carregarJogadorTexto("LuisGustavo");

        assertEquals("LuisGustavo", recuperado.getNome());
        assertEquals(840, recuperado.getMoedas());
        assertEquals(320, recuperado.getXp());
        assertEquals(4, recuperado.getNivel());

        // Veículo ativo e melhoria/desgaste do motor
        assertEquals("Opala", recuperado.getVeiculoAtivo().getCarroceria().getModeloBrasileiro());
        assertEquals(1, recuperado.getVeiculoAtivo().getMotor().getNivelMelhoria());
        assertEquals(original.getVeiculoAtivo().getMotor().getDurabilidade(),
                recuperado.getVeiculoAtivo().getMotor().getDurabilidade(), 0.01);
        assertEquals(15, recuperado.getVeiculoAtivo().getRoda().getAro());
        assertEquals(1.6, recuperado.getVeiculoAtivo().getSuspensao().getRigidez(), 0.001);

        // Inventário (4 peças equipadas + 1 carroceria reserva)
        assertEquals(5, recuperado.getInventarioComponentes().size());

        // Recordes de pista
        assertEquals(74.25, recuperado.getRecordesPistas().get("Interlagos"), 0.0001);
        assertEquals(91.80, recuperado.getRecordesPistas().get("Sertão"), 0.0001);
    }

    @Test
    void listarPerfisSalvosRetornaPerfisOrdenadosSemExtensao() {
        GerenciadorSave gerenciador = criarGerenciador();

        // Antes de criar qualquer save, retorna lista vazia
        assertTrue(gerenciador.listarPerfisSalvos().isEmpty());

        gerenciador.salvarJogadorTexto(criarJogadorCompleto("Julio"));
        gerenciador.salvarJogadorTexto(criarJogadorCompleto("Felipe"));
        gerenciador.salvarJogadorTexto(criarJogadorCompleto("Hamada"));

        List<String> perfis = gerenciador.listarPerfisSalvos();
        assertEquals(List.of("Felipe", "Hamada", "Julio"), perfis);
    }

    @Test
    void carregarXmlInexistenteVazioCorrompidoOuInvalidoLancaPersistenciaException() throws IOException {
        GerenciadorSave gerenciador = criarGerenciador();
        File pastaSaves = pastaTemporaria.resolve("saves").toFile();
        assertTrue(pastaSaves.mkdirs());

        // Salvar jogador nulo
        assertThrows(PersistenciaException.class, () -> gerenciador.salvarJogadorTexto(null));

        // Perfil inexistente e nome inválido
        assertThrows(PersistenciaException.class, () -> gerenciador.carregarJogadorTexto("Inexistente"));
        assertThrows(PersistenciaException.class, () -> gerenciador.carregarJogadorTexto("  "));

        // Arquivo vazio
        File vazio = new File(pastaSaves, "Vazio.xml");
        assertTrue(vazio.createNewFile());
        assertThrows(PersistenciaException.class, () -> gerenciador.carregarJogadorTexto("Vazio"));

        // Arquivo XML corrompido
        File corrompido = new File(pastaSaves, "Corrompido.xml");
        try (FileWriter writer = new FileWriter(corrompido)) {
            writer.write("<?xml version=\"1.0\"?><jogador><tagQuebrada></jogador>");
        }
        assertThrows(PersistenciaException.class, () -> gerenciador.carregarJogadorTexto("Corrompido"));

        // Arquivo XML com regra de domínio violada (moedas negativas)
        File dominioInvalido = new File(pastaSaves, "Negativo.xml");
        try (FileWriter writer = new FileWriter(dominioInvalido)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<jogador nome=\"Negativo\" moedas=\"-50\" xp=\"0\" nivel=\"1\">"
                    + "<veiculoAtivo/>"
                    + "</jogador>");
        }
        assertThrows(PersistenciaException.class, () -> gerenciador.carregarJogadorTexto("Negativo"));
    }
}