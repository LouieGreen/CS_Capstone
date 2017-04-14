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

    //encrypts message, returns initilization vector + encrypted message, encoded in Base64
    public static String encrypt(byte[] key, byte[] initVector, String value) {
    	byte[] encrypted = null;
    	try {
            //create initVector and AES key
        	IvParameterSpec iv = new IvParameterSpec(initVector);
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

            //create and initialize the cipher
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            //encrypt the string using the cipher
            encrypted = cipher.doFinal(value.getBytes());
    	}
    	catch(NoSuchAlgorithmException e) {System.out.println("NoSuchAlgorithmException, AES encryption failed.");}
    	catch(NoSuchPaddingException e) {System.out.println("NoSuchPaddingException, AES encryption failed.");}
    	catch(InvalidKeyException e) {System.out.println("InvalidKeyException, AES encryption failed.");}
    	catch(InvalidAlgorithmParameterException e) {System.out.println("InvalidAlgorithmParameterException, AES encryption failed.");}
    	catch(IllegalBlockSizeException e) {System.out.println("IllegalBlockSizeException, AES encryption failed.");}
    	catch(BadPaddingException e) {System.out.println("BadPaddingException, AES encryption failed.");}

        //return the initVector appened with the encrypted string
        return Base64.getEncoder().encodeToString(initVector) + Base64.getEncoder().encodeToString(encrypted);
    }

    //decrypts message, returns plainText, initilization vector is part of the message, the first 24 characters to be exact, the rest is the message
    public static String decrypt(byte[] key, String encrypted) {
    	byte[] plainText = null;
    	try {
            //gets the initVector from the message and creates a
    		IvParameterSpec iv = new IvParameterSpec(Base64.getDecoder().decode(encrypted.substring(0, 24)));
    		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

            //create and initialize the cipher
	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
	        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

	        plainText = cipher.doFinal(Base64.getDecoder().decode(encrypted.substring(24)));
		}
    	catch(NoSuchAlgorithmException e) {System.out.println("NoSuchAlgorithmException, AES encryption failed.");}
    	catch(NoSuchPaddingException e) {System.out.println("NoSuchPaddingException, AES encryption failed.");}
    	catch(InvalidKeyException e) {System.out.println("InvalidKeyException, AES encryption failed.");}
    	catch(InvalidAlgorithmParameterException e) {System.out.println("InvalidAlgorithmParameterException, AES encryption failed.");}
    	catch(IllegalBlockSizeException e) {System.out.println("IllegalBlockSizeException, AES encryption failed.");}
    	catch(BadPaddingException e) {System.out.println("BadPaddingException, AES encryption failed.");}

        //returns plainText String
        return new String(plainText);
    }

    //generates and returns 128 bit key
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

    //since we will be generating a new init vector every message I made a function for it
    public static byte[] getInitVector(SecureRandom random) {
		byte iv[] = new byte[16];
		random.nextBytes(iv);

		return iv;
    }
}
