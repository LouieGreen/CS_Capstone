package application;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource.PSpecified;

public class RSA {

	//encrypts message, returns encrypted message, encoded in Base64
    public static String encrypt(RSAPublicKey pubkey, String message) {
    	byte[] enc = null;
        try {
            //create RSA cipher with ECB (sadly) and initilize it
			Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, pubkey);

			enc = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
		}
        catch (IllegalBlockSizeException e) {System.out.println("IllegalBlockSizeException, RSA encryption failed.");}
        catch (BadPaddingException e) {System.out.println("BadPaddingException, RSA encryption failed.");}
        catch (NoSuchAlgorithmException e) {System.out.println("NoSuchAlgorithmException, RSA encryption failed.");}
        catch (NoSuchPaddingException e) {System.out.println("NoSuchPaddingException, RSA encryption failed.");}
        catch (InvalidKeyException e) {System.out.println("InvalidKeyException, RSA encryption failed.");}

        //return encrypted string string Base64 encoded
        return Base64.getEncoder().encodeToString(enc);
    }

	//decrypts message, returns plainText message, encoded in UTF_8
    public static String decrypt(RSAPrivateKey privateKey, String encryptedMessage) {
    	byte[] plainText = null;
    	try {
            //create decryption cipher and initilize it
			Cipher oaepFromInit = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
			OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-1"), PSpecified.DEFAULT);
			oaepFromInit.init(Cipher.DECRYPT_MODE, privateKey, oaepParams);

            //decode the Base64 string to original bytes and decrypt them
			plainText = oaepFromInit.doFinal(Base64.getDecoder().decode(encryptedMessage));
		}
    	catch (NoSuchAlgorithmException e) {System.out.println("NoSuchAlgorithmException, RSA encryption failed.");}
    	catch (NoSuchPaddingException e) {System.out.println("NoSuchPaddingException, RSA encryption failed.");}
    	catch (InvalidKeyException e) {System.out.println("InvalidKeyException, RSA encryption failed.");}
    	catch (InvalidAlgorithmParameterException e) {System.out.println("InvalidAlgorithmParameterException, RSA encryption failed.");}
    	catch (IllegalBlockSizeException e) {System.out.println("IllegalBlockSizeException, RSA encryption failed.");}
    	catch (BadPaddingException e) {System.out.println("BadPaddingException, RSA encryption failed.");}

        //return plainText UTF_8 encoded
        return new String(plainText, StandardCharsets.UTF_8);
    }
}
