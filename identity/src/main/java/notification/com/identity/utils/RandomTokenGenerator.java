package notification.com.identity.utils;

import java.util.Random;

public class RandomTokenGenerator {

    public static String generate(int length) {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ$!.1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < length) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();
    }
}