package com.davidconneely.eightbit.z80;

import com.davidconneely.eightbit.IBus;
import com.davidconneely.eightbit.SimpleBus;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * Some of the tests use a simple CPM BDOS call interface to produce test output.
 * This class supports tests that work in that way.
 */
final class CpmVerificationMachine implements AutoCloseable {
    static class CpmVerificationBus extends SimpleBus implements AutoCloseable {
        private final InputStream in;
        private final PrintStream out;

        CpmVerificationBus(final InputStream in, final PrintStream out) {
            this.in = in;
            this.out = out;
            intWriteMem8(0x0000, 0x76); // `HALT`
            intWriteMem8(0x0001, 0x00); // address of start of memory (0x0000)
            intWriteMem8(0x0002, 0x00); //    "
            intWriteMem8(0x0005, 0xC9); // `RET` (from BDOS `CALL 0x0005`)
            intWriteMem8(0x0006, 0x00); // address of high memory (used to set `SP`, 0xFF00)
            intWriteMem8(0x0007, 0xFF); //    "
            intWriteMem8(0xFF00, 0x76); // `HALT`
        }

        @Override
        public void cpuWriteMem8(final int address, final int data) {
            if (address > 0x0007 && address != 0xFF00) { // read-only memory addresses.
                super.cpuWriteMem8(address, data);
            }
        }

        @Override
        public int intReadPort8(final int portNum) {
            if ((portNum & 0xFF) != 0x01) {
                return super.intReadPort8(portNum);
            }
            try {
                return in.read();
            } catch (IOException e) {
                return -1;
            }
        }

        @Override
        public void intWritePort8(final int portNum, final int data) {
            if ((portNum & 0xFF) != 0x01) {
                super.intWritePort8(portNum, data);
            }
            out.write((byte) data);
        }

        @Override
        public void close() throws IOException {
            try {
                in.close();
            } finally {
                out.close();
            }
        }
    }

    private boolean terminated;
    private final IBus bus;

    CpmVerificationMachine(final InputStream in, final PrintStream out) {
        this.terminated = false;
        this.bus = new CpmVerificationBus(in, out);
    }

    @Override
    public void close() throws Exception {
        if (bus instanceof AutoCloseable ac) {
            ac.close();
        }
    }

    void load(final int address, final byte[] program) {
        bus.intWriteMemBlock(address, program, 0, program.length);
    }

    /**
     * Run the loaded program.
     */
    void run(final int address) {
        final Core z80 = new Core(bus);
        z80.state().pc(address);
        while (!terminated) {
            boolean stepped = z80.step();
            if (!stepped) { // `HALT` occurred.
                break;
            }
            int pc = z80.state().pc();
            if (pc == 0x0000) { // `RST 0`
                terminated = true;
            } else if (pc == 0x0005) { // CPM BDOS call
                cpmBdosCall(bus, z80.state());
            }
        }
    }

    private String formattedStack(final IBus bus, final State state) {
        final int sp = state.sp();
        final int[] stackTrace = new int[4];
        for (int i = sp; i < sp + stackTrace.length * 2 && i <= 0xFFFF; i += 2) {
            stackTrace[(i - sp) / 2] = bus.cpuReadMem16(i);
        }
        return String.format("stack=0x%04x,0x%04x,0x%04x,0x%04x", stackTrace[0], stackTrace[1], stackTrace[2], stackTrace[3]);
    }

    /**
     * Emulates the simplest of the console input-output BDOS calls in CP/M (function number in `C` register, parameter
     * in the `E` or `DE` register). Note that the console input-output uses the `bus#readIoPort` and `bus#writeIoPort`
     * methods,so this (and `#writeString` and `#readString`) could be converted into Z80 instructions to run inside the
     * emulator, rather than externally in Java.
     *
     * @param bus The machine bus (needed for memory and I/O access to implement console input-output).
     * @param state The machine CPU state (needed to access registers).
     */
    private void cpmBdosCall(final IBus bus, final State state) {
        final int func = state.c();
        switch (func) {
            case 0x00 -> terminated = true;
            case 0x01 -> {
                int ch = bus.cpuReadPort8(0x0001);
                state.a(ch);
                state.l(ch);
            }
            case 0x02 -> bus.cpuWritePort8(0x0001, state.e());
            case 0x09 -> writeString(bus, state.de());
            case 0x10 -> state.de(readString(bus, state.de()));
            default -> throw new UnsupportedOperationException("CPM BDOS func 0x%02x not implemented".formatted(func));
        }
    }

    private void writeString(final IBus bus, int address) {
        int ch = (address <= 0xFFFF) ? bus.cpuReadMem8(address) : '$';
        while (ch != '$') {
            bus.cpuWritePort8(0x0001, ch);
            ++address;
            ch = (address <= 0xFFFF) ? bus.cpuReadMem8(address) : '$'; // don't wrap around past 0xFFFF
        }
    }

    private int readString(final IBus bus, int address) {
        final int capacity = (address <= 0xFFFF) ? bus.cpuReadMem8(address) : 0;
        if (capacity <= 0 || capacity > 0xFF) {
            return 0;
        }
        final byte[] buffer = new byte[capacity+1];
        int index = 1;
        int ch = bus.cpuReadPort8(0x0001);
        while (ch != '\n' && index < capacity) {
            if (ch != '\r') {
                buffer[index++] = (byte) ch;
            }
            ch = bus.cpuReadPort8(0x0001);
        }
        buffer[0] = (byte) (index-1);
        bus.intWriteMemBlock(address+1, buffer, 0, index);
        return index-1;
    }
}
