package pt.ulisboa.tecnico.meic.sirs.securesms.service;

import android.telephony.SmsManager;

import java.util.ArrayList;

import pt.ulisboa.tecnico.meic.sirs.securesms.dataAccess.ContactManager;
import pt.ulisboa.tecnico.meic.sirs.securesms.dataAccess.SessionManager;
import pt.ulisboa.tecnico.meic.sirs.securesms.dataAccess.SmsMessageManager;
import pt.ulisboa.tecnico.meic.sirs.securesms.dataAccess.exceptions.FailedToCreateSessionException;
import pt.ulisboa.tecnico.meic.sirs.securesms.dataAccess.exceptions.FailedToSendSessionAcknowledgeException;
import pt.ulisboa.tecnico.meic.sirs.securesms.dataAccess.exceptions.FailedToSendSessionRequestException;
import pt.ulisboa.tecnico.meic.sirs.securesms.dataAccess.exceptions.FailedToUpdateSessionException;
import pt.ulisboa.tecnico.meic.sirs.securesms.domain.Contact;
import pt.ulisboa.tecnico.meic.sirs.securesms.domain.Cryptography;
import pt.ulisboa.tecnico.meic.sirs.securesms.domain.Session;
import pt.ulisboa.tecnico.meic.sirs.securesms.domain.SmsMessage;
import pt.ulisboa.tecnico.meic.sirs.securesms.domain.exceptions.FailedToCreateSmsMessageException;
import pt.ulisboa.tecnico.meic.sirs.securesms.domain.exceptions.FailedToEncryptSmsMessageException;
import pt.ulisboa.tecnico.meic.sirs.securesms.domain.exceptions.FailedToRetrieveContactException;
import pt.ulisboa.tecnico.meic.sirs.securesms.domain.exceptions.SMSSizeExceededException;
import pt.ulisboa.tecnico.meic.sirs.securesms.service.exceptions.FailedServiceException;

/**
 * Created by lribeirogomes on 15/11/15.
 */

public class SendSmsMessageService extends SecureSmsService {
    private String _phoneNumber;
    private String _plainTextSms;

    public SendSmsMessageService(String phoneNumber, String plainTextSms) {
        _phoneNumber = phoneNumber;
        _plainTextSms = plainTextSms;
    }


    public void execute() throws FailedServiceException {
        try {
            Contact contact = ContactManager.retrieveContactByPhoneNumber(_phoneNumber);
            Session.Status sessionStatus = SessionManager.checkSessionStatus(contact);

            switch (sessionStatus) {
                case Established: {
                    SmsMessage smsMessage = SmsMessageManager.createSmsMessage(contact, _plainTextSms);
                    SmsMessageManager.sendSms(_phoneNumber, smsMessage.encryptToSend());
                    //TODO Catch size exceeded exception and split the sms
                    break;
                }
                case AwaitingAck: {
                    //TODO: THROW EXCEPTION
                    return;
                }
                case NonExistent: {
                    Session session = SessionManager.create(contact);
                    ArrayList<byte[]> partialSessionRequests = SmsMessageManager.createReqSmsMessage(contact);
                    for (byte[] partialSessionRequest : partialSessionRequests) {
                        SmsMessageManager.sendSms(_phoneNumber, partialSessionRequest);
                    }
                    //set pending message
                    SmsMessage pendingSmsMessage = SmsMessageManager.createSmsMessage(contact, _plainTextSms);
                    session.setPendingSmsId(pendingSmsMessage.getId());
                    SessionManager.update(contact, session);
                }
                default:
                    return; //never happens
            }
        } catch (IllegalArgumentException
                | FailedToRetrieveContactException
                | FailedToCreateSmsMessageException
                | FailedToSendSessionRequestException
                | FailedToCreateSessionException
                | SMSSizeExceededException
                | FailedToUpdateSessionException
                | FailedToEncryptSmsMessageException exception) {
            throw new FailedServiceException("send sms message", exception);
        }
    }

}