package co.unus.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodeGeneratorTest {
    @Test
    @DisplayName("Code is valid from size 0 to 100")
    public void validCode() {
        String code = "";
        for (int i = 0; i <= 100; i++) {
            // The code size is okay
            code = CodeGenerator.get(i);
            assertEquals(i, code.length());

            // Only the characters from this list are being used
            String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
            for (Character c : chars.toCharArray()) {
                assertTrue(chars.indexOf(c) != -1);
            }
        }
    }
}