package Infrastructure.DbController;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordManager {

    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(12));
    }

    public static boolean verifyPassword(String plainTextPassword, String hashedPasswordFromDB) {
        try {
            return BCrypt.checkpw(plainTextPassword, hashedPasswordFromDB);
        } catch (IllegalArgumentException _) {
            return false;
        }
    }
}
