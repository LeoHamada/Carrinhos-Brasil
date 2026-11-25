package br.unicamp.ic.mc322.exception;

/**
 * Exceção lançada quando ocorre uma falha durante a leitura, gravação
 * ou validação dos arquivos de persistência do jogo (perfis e configurações).
 *
 * @author Luis Gustavo
 */
public class PersistenciaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constrói uma nova exceção de persistência com a mensagem descritiva do erro.
     *
     * @param mensagem Descrição da falha ocorrida na operação de arquivo.
     */
    public PersistenciaException(String mensagem) {
        super(mensagem);
    }

    /**
     * Constrói uma nova exceção de persistência encapsulando a causa original do erro.
     *
     * @param mensagem Descrição da falha ocorrida na operação de arquivo.
     * @param causa    Exceção original capturada durante a leitura ou escrita.
     */
    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}