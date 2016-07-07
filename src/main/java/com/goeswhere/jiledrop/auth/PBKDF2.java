package com.goeswhere.jiledrop.auth;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/*
 * based on work by havoc AT defuse.ca
 */
public class PBKDF2 {
    public static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";

    public static final int SALT_BYTES = 16;
    public static final int HASH_BYTES = 24;
    public static final int PBKDF2_ITERATIONS = 5_000;

    public static final int ITERATION_INDEX = 0;
    public static final int SALT_INDEX = 1;
    public static final int PBKDF2_INDEX = 2;

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String createHash(char[] password) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);

        byte[] hash = pbkdf2(password, salt, PBKDF2_ITERATIONS, HASH_BYTES);
        return PBKDF2_ITERATIONS + ":" + encode(salt) + ":" + encode(hash);
    }

    public static boolean validatePassword(char[] password, String goodHash) {
        final String[] params = goodHash.split(":", 3);

        int iterations = Integer.parseInt(params[ITERATION_INDEX]);
        byte[] salt = decode(params[SALT_INDEX]);
        byte[] hash = decode(params[PBKDF2_INDEX]);

        byte[] testHash = pbkdf2(password, salt, iterations, hash.length);

        return slowEquals(hash, testHash);
    }

    private static boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++)
            diff |= a[i] ^ b[i];
        return diff == 0;
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bytes) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] decode(String hex) {
        return Base64.getDecoder().decode(hex);
    }

    private static String encode(byte[] array) {
        return Base64.getEncoder().encodeToString(array);
    }

    public static char[] randomChars(int length) {
        final char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = (char) (RANDOM.nextInt('z' - 'a' + 1) + 'a');
        }

        return chars;
    }
}