package co.unus.utils;

public class CodeGenerator {
    public static String get(int codeSize) {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder code = new StringBuilder();
        int currChar = 0;

        while(currChar < codeSize) {
            code.append(getRandomChar(chars));
            currChar += 1;
        }

        return code.toString();
    }

    private static Character getRandomChar(String str) {
        return str.charAt((int) Math.floor(Math.random() * str.length()));
    }
}
