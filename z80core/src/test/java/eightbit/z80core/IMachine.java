package eightbit.z80core;

import java.io.PrintWriter;

public interface IMachine {
    void runProgram(byte[] program, PrintWriter pw);
}
