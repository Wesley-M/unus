package co.unus.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodeGeneratorTest {
    @Test
    @DisplayName("Code is valid")
    public void validCode() {
       String code = CodeGenerator.get(7);
       assertEquals(7, code.length());

       String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
       for (Character c : chars.toCharArray()) {
           assertTrue(chars.indexOf(c) != -1);
       }
    }

    @Test
    @DisplayName("Size: 0")
    public void emptyCode() {
        String code = CodeGenerator.get(0);
        assertEquals(0, code.length());
    }
}