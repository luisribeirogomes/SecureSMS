package pt.ulisboa.tecnico.meic.sirs.group6.securesms.service.exceptions;

/**
 * Created by lribeirogomes on 17/11/15.
 */
public class FailedToReceiveSMSException extends Exception {
    public FailedToReceiveSMSException(Throwable throwable){
        super("Failed do receive sms.", throwable);
    }
}