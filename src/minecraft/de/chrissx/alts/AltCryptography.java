package de.chrissx.alts;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class AltCryptography {

	static byte[] generateKey(String s) throws NoSuchAlgorithmException
	{
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] b = pad(s.getBytes(StandardCharsets.UTF_8), 6);
		Random r = newRand(b);
		byte[] s1 = new byte[r.nextInt(100)];
		byte[] s2 = new byte[r.nextInt(100)];
		r.nextBytes(s1);
		r.nextBytes(s2);
		return md.digest(doubleSalt(md.digest(doubleSalt(b, s1, b)), s2, b));
	}

	static Random newRand(byte[] b)
	{
		return new Random((b[0]<<40)|(b[1]<<32)|(b[2]<<24)|(b[3]<<16)|(b[4]<<8)|b[5]);
	}

	static byte[] pad(byte[] b, int l)
	{
		if(b.length >= l)
			return b;
		byte[] c = new byte[l];
		for(int i = 0; i < b.length; i++)
			b[i] = b[i];
		for(int i = b.length; i < l; i++)
			b[i] = (byte)(i ^ l);
		return c;
	}

	static byte[] doubleSalt(byte[] b, byte[] s1, byte[] s2)
	{
		byte[] c = new byte[b.length + s1.length + s2.length];
		for(int i = 0; i < b.length; i++)
			c[i] = b[i];
		for(int i = 0; i < s1.length; i++)
			c[i + b.length] = s1[i];
		for(int i = 0; i < s2.length; i++)
			c[i + b.length + s1.length] = s2[i];
		return c;
	}

	static byte[] makeSaveHash(byte[] b) throws NoSuchAlgorithmException
	{
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		Random r = newRand(b);
		byte[] s1 = new byte[r.nextInt(100)];
		byte[] s2 = new byte[r.nextInt(100)];
		byte[] s3 = new byte[r.nextInt(100)];
		byte[] s4 = new byte[r.nextInt(100)];
		byte[] s5 = new byte[r.nextInt(100)];
		byte[] s6 = new byte[r.nextInt(100)];
		byte[] s7 = new byte[r.nextInt(100)];
		byte[] s8 = new byte[r.nextInt(100)];
		byte[] s9 = new byte[r.nextInt(100)];
		r.nextBytes(s1);
		r.nextBytes(s2);
		r.nextBytes(s3);
		r.nextBytes(s4);
		r.nextBytes(s5);
		r.nextBytes(s6);
		r.nextBytes(s7);
		r.nextBytes(s8);
		r.nextBytes(s9);
		byte[] c = md.digest(doubleSalt(b, s1, b));
		c = md.digest(doubleSalt(c, s2, b));
		c = md.digest(doubleSalt(c, s3, b));
		c = md.digest(doubleSalt(c, s4, b));
		c = md.digest(doubleSalt(c, s5, b));
		c = md.digest(doubleSalt(c, s6, b));
		c = md.digest(doubleSalt(c, s7, b));
		c = md.digest(doubleSalt(c, s8, b));
		c = md.digest(doubleSalt(c, s9, b));
		return c;
	}

	public static void saveKey(SecretKeySpec key, Path f) throws NoSuchAlgorithmException, IOException
	{
		f.toFile().createNewFile();
		Files.write(f, makeSaveHash(key.getEncoded()));
	}

	public static boolean checkKey(SecretKeySpec key, Path f) throws IOException, NoSuchAlgorithmException
	{
		byte[] h1 = Files.readAllBytes(f);
		byte[] h2 = makeSaveHash(key.getEncoded());
		for(int i = 0; i < 64; i++)
			if(h1[i] != h2[i])
				return false;
		return true;
	}

	/**
	 * 
	 * @param a random string to use for key-creation
	 * @return The key
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 */
	public static SecretKeySpec createKey(String s) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		return new SecretKeySpec(generateKey(s), "AES");
	}

	static String altToCsv(Alt a) {
		return a.getEmail() + ";:;" + a.getPassword();
	}

	static Alt csvToAlt(String csv) {
		String[] splt = csv.split(";:;");
		return new Alt(splt[0], splt[1]);
	}

	static byte[] altsToBase64(List<Alt> alts) {
		if(alts.size() == 0)
			return new byte[0];
		String s = altToCsv(alts.get(0));
		for(int i = 1; i < alts.size(); i++)
			s += "\u000b" + alts.get(i);
		return Base64.getEncoder().encode(s.getBytes(StandardCharsets.UTF_8));
	}

	static List<Alt> base64ToAlts(byte[] base64) {
		if(base64.length == 0)
			return new ArrayList<Alt>();
		String[] strs = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8).replace("\r\n", "\u000b").split("\u000b");
		List<Alt> alts = new ArrayList<Alt>();
		for(String s : strs)
			alts.add(csvToAlt(s));
		return alts;
	}

	/**
	 * Loads key from the given file.
	 * @param file to load key from
	 * @return the loaded key
	 * @throws IOException
	 */
	public static SecretKeySpec aesLoadKey(Path file) throws IOException {
		return new SecretKeySpec(Base64.getDecoder().decode(Files.readAllBytes(file)), "AES");
	}

	/**
	 * Saves key to the given file.
	 * @param key to save
	 * @param file to save the key to
	 * @throws IOException
	 */
	public static void aesSaveKey(SecretKeySpec key, Path file) throws IOException {
		if(!file.toFile().exists())
			Files.createFile(file);
		Files.write(file, Base64.getEncoder().encode(key.getEncoded()));
	}

	/**
	 * Encrypts and saves the alts using the given key and file.
	 * @param key to use in the encryption
	 * @param alts to encrypt and save
	 * @param file to save to
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IOException
	 */
	public static void aesEncrypt(SecretKeySpec key, List<Alt> alts, Path file) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
		if(!file.toFile().exists())
			Files.createFile(file);
		Cipher ci = Cipher.getInstance("AES");
		ci.init(Cipher.ENCRYPT_MODE, key);
		Files.write(file, ci.doFinal(altsToBase64(alts)));
	}

	/**
	 * Loads and decrypts the alts using the given file and key.
	 * @param key to use in the decryption
	 * @param file to load from
	 * @return the decrypted alts
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 */
	public static List<Alt> aesDecrypt(SecretKeySpec key, Path file) throws IllegalBlockSizeException, BadPaddingException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
		Cipher ci = Cipher.getInstance("AES");
		ci.init(Cipher.DECRYPT_MODE, key);
		return base64ToAlts(ci.doFinal(Files.readAllBytes(file)));
	}
}