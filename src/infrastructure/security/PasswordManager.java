package infrastructure.security;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordManager {

    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(12));
    }

    public static boolean verifyPassword(String plainTextPassword, String hashedPasswordFromDB) {
        try {
            return BCrypt.checkpw(plainTextPassword, hashedPasswordFromDB);
        } catch (IllegalArgumentException e) {
            org.slf4j.LoggerFactory.getLogger(PasswordManager.class).error("Failed to send print job: {}", e.getMessage());
            return false;
        }
    }
}

