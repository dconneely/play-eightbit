package com.davidconneely.z80core;

import java.io.IOException;
import java.io.PrintStream;

public interface IMachine {
    /**
     * Load and run the supplied program in a machine-specific way.
     * @param program the binary image of the program
     * @return the output of the program (gathered in a machine-specific way).
     */
    String run(byte[] program) throws IOException;
}
