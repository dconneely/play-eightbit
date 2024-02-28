package com.davidconneely.eightbit.z80;

import com.davidconneely.eightbit.IBus;
import com.davidconneely.eightbit.SimpleBus;

import java.io.IOException;

/**
 * Some of the tests use a simple CPM BDOS call interface to produce test output.
 * This class supports tests that work in that way.
 */
final class CpmVerificationMachine {
    /**
     * Load and run the supplied program.
     *
     * @param program the binary image of the program
     * @return the output of the program.
     */
    String run(final byte[] program) throws IOException {
        final IBus bus = new SimpleBus();
        bus.writeMemory(0x0005, 0xC9); // BDOS RET
        bus.writeMemory(0x0100, program, 0, program.length);
        final StringBuilder out = new StringBuilder();
        Core z80 = new Core(bus);
        z80.state().pc(0x0100);
        boolean terminated = false;
        while (!terminated) {
            z80.step();
            int pc = z80.state().pc();
            if (pc == 0x0000) { // RST 0
                terminated = true;
            } else if (pc == 0x0005 && bus.readMemory(pc) == 0xC9) { // CPM BDOS
                cpmBdosCall(bus, z80.state().c(), z80.state().de(), out);
            }
        }
        return out.toString();
    }

    private void cpmBdosCall(final IBus bus, final int func, final int param, final Appendable out) throws IOException {
        switch (func) {
            case 0x02 -> out.append((char) (param & 0xFF));
            case 0x09 -> appendString(bus, param, out);
            default ->
                    throw new UnsupportedOperationException("CPM BDOS func 0x" + Integer.toHexString(func) + " not implemented");
        }
    }

    private void appendString(final IBus bus, int address, final Appendable out) throws IOException {
        int ch = bus.readMemory(address);
        while (ch != '$') {
            out.append((char) ch);
            ++address;
            ch = (address <= 0xFFFF ? bus.readMemory(address) : '$'); // don't wrap around past 0xFFFF
        }
    }
}
