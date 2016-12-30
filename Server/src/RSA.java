

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
	
    public static String encrypt(RSAPublicKey pubkey, String message) {
    	byte[] enc = null;
    	
        try {
			Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, pubkey);
			
			enc = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
		} 
        catch (IllegalBlockSizeException e) {
			System.out.println("IllegalBlockSizeException, encryption failed.");
		}
        catch (BadPaddingException e) {
			System.out.println("BadPaddingException, encryption failed.");
		}
        catch (NoSuchAlgorithmException e) {
			System.out.println("NoSuchAlgorithmException, encryption failed.");
		}
        catch (NoSuchPaddingException e) {
			System.out.println("NoSuchPaddingException, encryption failed.");
		}
        catch (InvalidKeyException e) {
			System.out.println("InvalidKeyException, encryption failed.");
		}
        
        return Base64.getEncoder().encodeToString(enc);
    }
    
    public static String decrypt(RSAPrivateKey privateKey, String encryptedMessage) {
    	byte[] plainText = null;
    	
    	try {
			Cipher oaepFromInit = Cipher.getInstance("RSA/ECB/OAEPPadding");
			OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-1"), PSpecified.DEFAULT);
			oaepFromInit.init(Cipher.DECRYPT_MODE, privateKey, oaepParams);
			
			plainText = oaepFromInit.doFinal(Base64.getDecoder().decode(encryptedMessage));
		} 
    	catch (NoSuchAlgorithmException e) {
			System.out.println("NoSuchAlgorithmException, encryption failed.");
		}
    	catch (NoSuchPaddingException e) {
			System.out.println("NoSuchPaddingException, encryption failed.");
		}
    	catch (InvalidKeyException e) {
			System.out.println("InvalidKeyException, encryption failed.");
		}
    	catch (InvalidAlgorithmParameterException e) {
			System.out.println("InvalidAlgorithmParameterException, encryption failed.");
		}
    	catch (IllegalBlockSizeException e) {
			System.out.println("IllegalBlockSizeException, encryption failed.");
		}
    	catch (BadPaddingException e) {
			System.out.println("BadPaddingException, encryption failed.");
		}
        
        return new String(plainText, StandardCharsets.UTF_8);
    }
}