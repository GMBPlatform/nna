package Util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// Generate Message Digest (Hash Value)

public class Cryptos {
	
	// return ByteArray base to SHA-256 hash HexString
	public String Sha256ByteToString(byte[] base) {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			return "Error NoSuchAlgorithmException";
		}
		
		byte[] hash = digest.digest(base);
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < hash.length; i++) {
			String hex = Integer.toHexString(0xff & hash[i]);
			if (hex.length() == 1) 
				hexString.append('0');
			hexString.append(hex);
		}
		digest = null;
		return hexString.toString();
	}
	
	// return String base to SHA-256 hash HexString
	public String Sha256BStringToString(String base) {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			return "Error NoSuchAlgorithmException";
		}
		
		byte[] hash = null;
		try {
			hash = digest.digest(base.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			return "Error UnsupportedEncodingException";
		}
		
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < hash.length; i++) {
			String hex = Integer.toHexString(0xff & hash[i]);
			if (hex.length() == 1)
				hexString.append('0');
			hexString.append(hex);
		}
		digest = null;
		return hexString.toString();
	}
}
