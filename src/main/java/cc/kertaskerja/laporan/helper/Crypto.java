package cc.kertaskerja.laporan.helper;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class Crypto {
    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY = "mySuperSecretKey"; // Must be 16 characters for AES-128

    public static String encrypt(String plainText) {
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted); // 🔁 use URL-safe encoding
        } catch (Exception e) {
            throw new RuntimeException("Error while encrypting: " + e.getMessage());
        }
    }

    public static String decrypt(String encryptedText) {
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] original = cipher.doFinal(Base64.getUrlDecoder().decode(encryptedText)); // 🔁 match with UrlDecoder
            return new String(original);
        } catch (Exception e) {
            return encryptedText;
        }
    }

    public static boolean isEncrypted(String input) {
        try {
            String decrypted = Crypto.decrypt(input);
            // Optional: Add further validation here (e.g., regex for NIP format)
            return !decrypted.equals(input); // If decrypt works and result is different, likely encrypted
        } catch (Exception e) {
            return false;
        }
    }
}
