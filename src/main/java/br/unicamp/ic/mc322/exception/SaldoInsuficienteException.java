package br.unicamp.ic.mc322.exception;

/**
 * Exceção de domínio lançada quando o jogador não possui moedas suficientes
 * ou não atingiu o nível mínimo exigido para concluir uma compra, melhoria ou reparo.
 *
 * @author Luis Gustavo
 */
public class SaldoInsuficienteException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constrói uma nova exceção indicando insuficiência de recursos ou nível.
     *
     * @param mensagem Descrição detalhada do motivo do bloqueio da operação.
     */
    public SaldoInsuficienteException(String mensagem) {
        super(mensagem);
    }
}