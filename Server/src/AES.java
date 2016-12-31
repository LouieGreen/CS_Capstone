import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
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
/*
    public static void main(String[] args) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, 
    												InvalidKeyException, InvalidAlgorithmParameterException {
    	// generate random key value
    	KeyGenerator keygen = KeyGenerator.getInstance("AES");
		keygen.init(128);
		byte[] key = keygen.generateKey().getEncoded();
		
		// generate random initialization vector
		SecureRandom random = new SecureRandom();
		byte iv[] = new byte[16];
		byte seed[] = random.generateSeed(20);
		random.setSeed(seed);
		
        String message = "This is a text message for testing.";
        
        for(int i=0; i<50; i++) {
        	random.nextBytes(iv);
        	
	        String encryptedMessage = encrypt(key, iv, message);
	        String decryptedMessage = decrypt(key, iv, encryptedMessage);
	        
	        System.out.println(encryptedMessage);
	        System.out.println(decryptedMessage);
	        String a = Base64.getEncoder().encodeToString(iv) + encryptedMessage;
	        System.out.println(a);
	        System.out.println(decrypt(key, Base64.getDecoder().decode(a.substring(0, 24)), encryptedMessage));
	        System.out.println("");
        }
    } */
}