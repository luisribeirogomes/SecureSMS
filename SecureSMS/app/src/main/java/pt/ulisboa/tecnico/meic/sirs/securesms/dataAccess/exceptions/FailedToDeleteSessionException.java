package pt.ulisboa.tecnico.meic.sirs.securesms.dataAccess.exceptions;

/**
 * Created by joao on 11/14/15.
 */
public class FailedToDeleteSessionException extends Exception{
    public FailedToDeleteSessionException(String message){
        super(message);
    }
}
