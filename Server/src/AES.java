import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {
    public static String encrypt(byte[] key, byte[] initVector, String value) {
    	byte[] encrypted = null;
    	try {    
        	IvParameterSpec iv = new IvParameterSpec(initVector);
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            encrypted = cipher.doFinal(value.getBytes());
    	}
    	catch(NoSuchAlgorithmException e) {System.out.println("NoSuchAlgorithmException, AES encryption failed.");}
    	catch(NoSuchPaddingException e) {System.out.println("NoSuchPaddingException, AES encryption failed.");}
    	catch(InvalidKeyException e) {System.out.println("InvalidKeyException, AES encryption failed.");}
    	catch(InvalidAlgorithmParameterException e) {System.out.println("InvalidAlgorithmParameterException, AES encryption failed.");}
    	catch(IllegalBlockSizeException e) {System.out.println("IllegalBlockSizeException, AES encryption failed.");}
    	catch(BadPaddingException e) {System.out.println("BadPaddingException, AES encryption failed.");}
    	
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(byte[] key, byte[] initVector, String encrypted) {
    	byte[] original = null;
    	try {	
    		IvParameterSpec iv = new IvParameterSpec(initVector);
	        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
	
	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
	        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
	
			original = cipher.doFinal(Base64.getDecoder().decode(encrypted));
		}
    	catch(NoSuchAlgorithmException e) {System.out.println("NoSuchAlgorithmException, AES encryption failed.");}
    	catch(NoSuchPaddingException e) {System.out.println("NoSuchPaddingException, AES encryption failed.");}
    	catch(InvalidKeyException e) {System.out.println("InvalidKeyException, AES encryption failed.");}
    	catch(InvalidAlgorithmParameterException e) {System.out.println("InvalidAlgorithmParameterException, AES encryption failed.");}
    	catch(IllegalBlockSizeException e) {System.out.println("IllegalBlockSizeException, AES encryption failed.");}
    	catch(BadPaddingException e) {System.out.println("BadPaddingException, AES encryption failed.");}
    	
        return new String(original);
    }
    
    public static byte[] generateKey() {
    	byte[] key = null;
    	
    	try {
			KeyGenerator keygen;
			keygen = KeyGenerator.getInstance("AES");
			keygen.init(128);
			key = keygen.generateKey().getEncoded();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
    	
    	return key;
    }
    
    public static byte[] generateInitVector() {
    	
    	SecureRandom random = new SecureRandom();
	    byte seed[] = random.generateSeed(20);
		random.setSeed(seed);
		byte iv[] = new byte[16];
		random.nextBytes(iv);
		
		return iv;
    }
}