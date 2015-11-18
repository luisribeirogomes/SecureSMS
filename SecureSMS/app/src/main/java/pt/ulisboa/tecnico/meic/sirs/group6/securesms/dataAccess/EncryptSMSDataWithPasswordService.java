package pt.ulisboa.tecnico.meic.sirs.group6.securesms.dataAccess;

import org.spongycastle.crypto.BufferedBlockCipher;
import org.spongycastle.crypto.CipherParameters;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.BlockCipherPadding;
import org.spongycastle.crypto.paddings.PKCS7Padding;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import pt.ulisboa.tecnico.meic.sirs.group6.securesms.dataAccess.exceptions.FailedToEncryptSMSException;
import pt.ulisboa.tecnico.meic.sirs.group6.securesms.dataAccess.exceptions.FailedToGetResultException;

/**
 * Created by lribeirogomes on 17/11/15.
 */
public class EncryptSMSDataWithPasswordService implements EncryptionService {
    private String _password, _data;
    private byte[] _result;
    private Charset _charset = StandardCharsets.UTF_8;

    public EncryptSMSDataWithPasswordService(String password, String data) {
        _password = password;
        _data = data;
        _result = null;
    }

    public byte[] getResult () throws FailedToGetResultException {
        if (_result == null) {
            throw new FailedToGetResultException();
        }
        return _result;
    }

    public void Execute () throws FailedToEncryptSMSException {
        try {
            int saltLen = 32, ivLen = 32;
            byte[] seed = _password.getBytes(_charset);
            SecureRandom random = new SecureRandom(seed);

            byte[] salt = new byte[saltLen];
            byte[] iv = new byte[ivLen];
            byte[] data = _data.getBytes(_charset);
            char[] password = _password.toCharArray();

            random.nextBytes(salt);
            random.nextBytes(iv);

            // Define encryption key
            PBEKeySpec pbeKeySpec = new PBEKeySpec(
                    password,
                    salt,
                    100,
                    256);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithSHA256And256BitAES-CBC-BC");
            SecretKey secretKey = keyFactory.generateSecret(pbeKeySpec);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");
            byte[] key = secretKeySpec.getEncoded();

            // Define cipher parameters with key and IV
            KeyParameter keyParam = new KeyParameter(key);
            CipherParameters params = new ParametersWithIV(keyParam, iv);

            // Define AES cipher in CBC mode with PKCS7 padding
            BlockCipherPadding padding = new PKCS7Padding();
            CBCBlockCipher cbcBlockCipher = new CBCBlockCipher(new AESEngine());
            BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(cbcBlockCipher, padding);
            cipher.reset();
            cipher.init(true, params);

            // Encrypt data
            byte[] encryptedData = new byte[cipher.getOutputSize(data.length)];
            int dataLen = cipher.processBytes(data, 0, data.length, encryptedData, 0);
            cipher.doFinal(encryptedData, dataLen);

            // Add salt and IV
            byte[] result = new byte[saltLen + ivLen + encryptedData.length];
            System.arraycopy(salt, 0, result, 0, saltLen);
            System.arraycopy(iv, 0, result, saltLen, saltLen + ivLen);
            System.arraycopy(encryptedData, 0, result, saltLen + ivLen, result.length);

            _result = result;
        } catch (
                NullPointerException |
                NoSuchAlgorithmException |
                InvalidKeySpecException |
                InvalidCipherTextException exception) {
            throw new FailedToEncryptSMSException(exception);
        }
    }
}