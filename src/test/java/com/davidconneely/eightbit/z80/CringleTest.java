package com.davidconneely.eightbit.z80;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public final class CringleTest {
    private static final String PRELIM_RESOURCE = "/z80/cringle/prelim.com";
    private static final String PRELIM_FINISHED = "Preliminary tests complete";
    private static final String ZEXDOC_RESOURCE = "/z80/cringle/zexdoc.com";
    private static final String ZEXALL_RESOURCE = "/z80/cringle/zexall.com";
    private static final String ZEXANY_FAILURE = "..  ERROR ****";
    private static final String ZEXANY_SUCCESS = "..  OK";

    @Test
    @Order(1)
    public void testPreliminaries() throws IOException {
        var output = run(PRELIM_RESOURCE);
        int failed = 0;
        if (!output.equals(PRELIM_FINISHED)) {
            ++failed;
        }
        System.out.println(output);
        System.out.printf("===> %3$d/%1$d tests passed, %2$d/%1$d tests failed.%n%n", 1, failed, 1 - failed);
        if (failed != 0) {
            fail("there were test failures");
        }
    }

    @Test
    @Order(2)
    public void testDocumentedFlags() throws IOException {
        String output = run(ZEXDOC_RESOURCE);
        var lines = output.split("\\r?\\n\\r?"); // test output uses "\n\r" instead of more standard "\n" or "\r\n"
        int failed = 0, passed = 0;
        for (var line : lines) {
            if (line.contains(ZEXANY_SUCCESS)) {
                ++passed;
            } else if (line.contains(ZEXANY_FAILURE)) {
                ++failed;
            }
        }
        System.out.print(output);
        System.out.printf("===> %3$d/%1$d tests passed, %2$d/%1$d tests failed.%n%n", failed + passed, failed, passed);
        if (failed != 0) {
            fail("there were test failures");
        }
    }

    @Test
    @Order(3)
    @Disabled("Skipped because 37/67 tests pass, 30/67 tests fail - undocumented flags not currently implemented")
    public void testUndocumentedFlags() throws IOException {
        String output = run(ZEXALL_RESOURCE);
        var lines = output.split("\\r?\\n\\r?"); // test output uses "\n\r" instead of more standard "\n" or "\r\n"
        int failed = 0, passed = 0;
        for (var line : lines) {
            if (line.contains(ZEXANY_SUCCESS)) {
                ++passed;
            } else if (line.contains(ZEXANY_FAILURE)) {
                ++failed;
            }
        }
        System.out.print(output);
        System.out.printf("===> %3$d/%1$d tests passed, %2$d/%1$d tests failed.%n%n", failed + passed, failed, passed);
        if (failed != 0) {
            fail("there were test failures");
        }
    }

    private String run(String resource) throws IOException {
        try (var is = new ByteArrayInputStream(new byte[0]);
             var in = new BufferedInputStream(is);
             var os = new ByteArrayOutputStream();
             var out = new PrintStream(os, true, StandardCharsets.UTF_8)) {
            var machine = new CpmVerificationMachine(in, out);
            var data = CringleTest.class.getResourceAsStream(resource).readAllBytes();
            machine.load(0x0100, data);
            machine.run(0x100);
            out.flush();
            return os.toString(StandardCharsets.UTF_8);
        }
    }
}
