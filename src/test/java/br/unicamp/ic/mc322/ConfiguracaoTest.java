package br.unicamp.ic.mc322;

import br.unicamp.ic.mc322.model.Configuracao;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe {@link Configuracao}, validando valores padrão,
 * limites de volume, remapeamento de teclas sem conflito e restauração de fábrica.
 *
 * @author Luis Gustavo
 */
class ConfiguracaoTest {

    @Test
    void configuracaoNasceComValoresPadraoValidos() {
        Configuracao config = new Configuracao();

        assertEquals(0.5, config.getVolumeMusica(), 0.0001);
        assertEquals(0.5, config.getVolumeEfeitos(), 0.0001);
        assertTrue(config.isExibirVelocimetro());
        assertEquals("W", config.getTeclaAcelerar());
        assertEquals("S", config.getTeclaFrear());
        assertEquals("A", config.getTeclaEsquerda());
        assertEquals("D", config.getTeclaDireita());
    }

    @Test
    void alterarVolumesNosLimitesValidosAtualizaAtributos() {
        Configuracao config = new Configuracao();

        config.setVolumeMusica(0.0);
        config.setVolumeEfeitos(1.0);
        assertEquals(0.0, config.getVolumeMusica(), 0.0001);
        assertEquals(1.0, config.getVolumeEfeitos(), 0.0001);

        config.setVolumeMusica(0.75);
        config.setVolumeEfeitos(0.25);
        assertEquals(0.75, config.getVolumeMusica(), 0.0001);
        assertEquals(0.25, config.getVolumeEfeitos(), 0.0001);
    }

    @Test
    void alterarVolumesForaDoIntervaloLancaIllegalArgumentException() {
        Configuracao config = new Configuracao();

        assertThrows(IllegalArgumentException.class, () -> config.setVolumeMusica(-0.01));
        assertThrows(IllegalArgumentException.class, () -> config.setVolumeMusica(1.01));
        assertThrows(IllegalArgumentException.class, () -> config.setVolumeMusica(Double.NaN));

        assertThrows(IllegalArgumentException.class, () -> config.setVolumeEfeitos(-0.5));
        assertThrows(IllegalArgumentException.class, () -> config.setVolumeEfeitos(1.5));
        assertThrows(IllegalArgumentException.class, () -> config.setVolumeEfeitos(Double.NaN));
    }

    @Test
    void alterarTeclasNormalizaTextoEAtualizaCorretamente() {
        Configuracao config = new Configuracao();

        config.alterarTecla(" acelerar ", " up ");
        config.alterarTecla("frear", "down");
        config.alterarTecla("ESQUERDA", "left");
        config.alterarTecla("Direita", "right");

        assertEquals("UP", config.getTeclaAcelerar());
        assertEquals("DOWN", config.getTeclaFrear());
        assertEquals("LEFT", config.getTeclaEsquerda());
        assertEquals("RIGHT", config.getTeclaDireita());
    }

    @Test
    void manterMesmaTeclaNaMesmaAcaoNaoGeraConflito() {
        Configuracao config = new Configuracao();

        assertDoesNotThrow(() -> config.alterarTecla("acelerar", "w"));
        assertEquals("W", config.getTeclaAcelerar());
    }

    @Test
    void alterarTeclaParaTeclaJaEmUsoLancaIllegalArgumentException() {
        Configuracao config = new Configuracao();

        assertThrows(IllegalArgumentException.class, () -> config.alterarTecla("ACELERAR", "S"));
        assertThrows(IllegalArgumentException.class, () -> config.alterarTecla("FREAR", "a"));
        assertThrows(IllegalArgumentException.class, () -> config.alterarTecla("ESQUERDA", "D"));
        assertThrows(IllegalArgumentException.class, () -> config.alterarTecla("DIREITA", "W"));
    }

    @Test
    void alterarTeclaComArgumentosInvalidosOuAcaoDesconhecidaLancaExcecao() {
        Configuracao config = new Configuracao();

        assertThrows(IllegalArgumentException.class, () -> config.alterarTecla(null, "Q"));
        assertThrows(IllegalArgumentException.class, () -> config.alterarTecla("   ", "Q"));
        assertThrows(IllegalArgumentException.class, () -> config.alterarTecla("ACELERAR", null));
        assertThrows(IllegalArgumentException.class, () -> config.alterarTecla("ACELERAR", "  "));
        assertThrows(IllegalArgumentException.class, () -> config.alterarTecla("NITRO", "SPACE"));
    }

    @Test
    void restaurarPadraoReverteAlteracoesDeAudioHudETeclas() {
        Configuracao config = new Configuracao();
        config.setVolumeMusica(0.9);
        config.setVolumeEfeitos(0.1);
        config.setExibirVelocimetro(false);
        config.alterarTecla("ACELERAR", "UP");

        config.restaurarPadrao();

        assertEquals(0.5, config.getVolumeMusica(), 0.0001);
        assertEquals(0.5, config.getVolumeEfeitos(), 0.0001);
        assertTrue(config.isExibirVelocimetro());
        assertEquals("W", config.getTeclaAcelerar());
    }
}