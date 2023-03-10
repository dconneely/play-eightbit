package eightbit.z80core;

import eightbit.IBus;
import eightbit.ICore;

import java.io.PrintWriter;

public class SimpleCPMMachine implements IMachine {
    @Override
    public void runProgram(byte[] program, PrintWriter pw) {
        IBus bus = new SimpleBus();
        bus.writeMemory((short) 0x100, program, 0, program.length);
        Z80Core z80 = new Z80Core(bus);
        while (true) {
            z80.runOneInstruction();
            if (z80.getPC() == 0x0005) {
                int de = z80.getDE();
            }
        }
    }
}
