package CarrinhosBrasil;

public class PersistenciaException extends RuntimeException {
    PersistenciaException(String mensagem){
        super(mensagem);
    }
    PersistenciaException(String mensagem, Throwable causa){
        super(mensagem, causa);
    }
}
