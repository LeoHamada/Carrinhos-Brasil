package CarrinhosBrasil;

public class SaldoInsufisienteException extends RuntimeException {
    SaldoInsufisienteException(String mensagem){
        super(mensagem);
    }
}
