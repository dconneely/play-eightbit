package com.davidconneely.eightbit.z80;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public final class CringleTest {
    @Test
    public void testPrelim() throws IOException {
        var output = run("/cringle/prelim.com");
        if (!output.equals("Preliminary tests complete")) {
            System.err.println(output);
            fail("the test did not complete");
        } else {
            System.out.println(output);
        }
    }

    @Test
    public void testDocumented() throws IOException {
        var lines = run("/cringle/zexdoc.com").split("\\r?\\n\\r?");
        int total = 0, error = 0, ok = 0;
        for (var line : lines) {
            if (!line.equals("Z80doc instruction exerciser") && !line.equals("Tests complete") && !line.isBlank()) {
                ++total;
            } else {
                System.out.println(line);
            }
            if (line.contains("..  ERROR ****")) {
                ++error;
                System.err.println(line);
            } else if (line.contains("..  OK")) {
                ++ok;
                System.out.println(line);
            }
        }
        System.out.printf("%3$d/%1$d tests passed, %2$d/%1$d tests failed.\n", total, error, ok);
        if (error != 0) {
            fail("there were not zero test failures");
        }
    }

    private String run(String resource) throws IOException {
        try (var bais = new ByteArrayInputStream(new byte[0]);
             var in = new BufferedInputStream(bais);
             var baos = new ByteArrayOutputStream();
             var out = new PrintStream(baos, true, StandardCharsets.UTF_8)) {
            var machine = new CpmVerificationMachine(in, out);
            var data = CringleTest.class.getResourceAsStream(resource).readAllBytes();
            machine.load(0x0100, data);
            machine.run(0x100);
            out.flush();
            return baos.toString(StandardCharsets.UTF_8);
        }
    }
}
