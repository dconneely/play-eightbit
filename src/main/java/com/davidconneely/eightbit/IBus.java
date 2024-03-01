package com.davidconneely.eightbit;

public interface IBus {
    /**
     * Read a byte from memory specifically to decode as the first byte of an instruction. The Z80 CPU has a pinout that
     * indicates that a read is taking place in the M1 machine cycle. Some hardware (e.g. ZX81) uses this to change the
     * memory read behaviour for instructions (but not for data) at some addresses.
     * <p>
     * Although the 6502 does not have a corresponding way for the hardware to differentiate an instruction read from a
     * data read, the emulator still uses this method to read the first byte of each instruction. An implementation of
     * the `IBus` interface for the 6502 should normally rely on the default method provided.
     *
     * @param address 16-bit memory address.
     * @return 8 bits of data (the op code).
     */
    default int readInstruction(int address) {
        return readMemory(address);
    }

    /**
     * Z80 assembly language encourages you to consider only bottom 8 bits, but `IN A, (n)` actually reads from the
     * 16-bit port number `(A << 8) | n` and `IN A, (C)` actually reads form the 16-bit port number `BC`. Of course,
     * the hardware is free to ignore the top 8 bits of the address bus, but the Sinclair and Amstrad machines do not.
     * <p>
     * Conversely, the 6502 CPU does not have separate instructions for accessing specific I/O ports, but maps external
     * I/O to memory reads and writes at specific addresses (memory-mapped I/O). It will never call this method.
     *
     * @param portNum 16-bit port number (although hardware may only consider some address bits significant).
     * @return the byte read from the specified port.
     */
    default int readIoPort(int portNum) {
        return 0;
    }

    /**
     * read from memory of data (i.e. not during the M1 machine cycle of an instruction).
     *
     * @param address 16-bit memory address.
     * @return 8 bits of data.
     */
    int readMemory(int address);

    /**
     * For saving programs or data from memory.
     */
    void readMemory(int address, byte[] dest, int offset, int length);

    default int readWord(int address) {
        return (readMemory((address + 1) & 0xFFFF) << 8) | readMemory(address);
    }

    /**
     * @param portNum 16-bit port number (although hardware may only consider some address bits significant).
     * @param data the byte written to the specified port.
     */
    default void writeIoPort(int portNum, int data) {
        /* do nothing. */
    }

    void writeMemory(int address, int data);

     /**
     * For loading programs or data into memory.
     */
    void writeMemory(int address, byte[] source, int offset, int length);

    default void writeWord(int address, int data) {
        writeMemory(address, data & 0xFF);
        writeMemory((address + 1) & 0xFFFF, (data & 0xFF00) >>> 8);
    }
}
