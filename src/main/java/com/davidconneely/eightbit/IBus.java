package com.davidconneely.eightbit;

/**
 * Methods named `cpuRead*` or `cpuWrite*` are used by the emulator code to emulate reads and writes done by the
 * emulated CPU itself. Normally these should be implemented as calls to the underlying `rawRead*` and `rawWrite*`
 * methods, with whatever additional logic should apply _inside_ the emulator (e.g. read-only memory, etc.)
 * <p>
 * Methods named `rawRead*` or `rawWrite*` should be the actual implementation of the memory access or port i/o access.
 * These are used by the emulator or other code to directly access the memory or i/o ports for observability or other
 * purposes that are not part of the emulation of the CPU itself.
 * <p>
 * As an example, one might apply memory latency to the `cpuRead*` and `cpuWrite*` methods to emulate the real hardware
 * more completely. However, the `rawRead*` and `rawWrite*` methods would not be subject to these delays so that
 * the debugger was unaffected - only the execution of the emulated CPU and its memory accesses should be affected.
 */
public interface IBus {
    // ------------------------------------------------------------------------------------------------------
    // ---------- Override the `cpuRead*` and `cpuWrite*` methods that apply _inside_ the emulator ----------
    // ------------------------------------------------------------------------------------------------------

    /**
     * Read a byte of data from memory (with CPU side-effects, specifically during M1 machine cycle).
     * To be used by the emulator when emulating CPU memory access.
     * <p>
     * The Z80 CPU has a pinout that indicates that a read is taking place in the M1 machine cycle, so it is a read of
     * an instruction op-code to be decoded by the CPU. Some hardware (e.g. ZX81) uses this to change the memory read
     * behaviour for instructions (but not for data) at some addresses.
     * <p>
     * Although the 6502 does not have a corresponding way for the hardware to differentiate an instruction read from a
     * data read, the emulator still uses this method to read the first byte of each instruction. An implementation of
     * the `IBus` interface for the 6502 should normally rely on the default method provided.
     *
     * @param address 16-bit memory address.
     * @return int 8 bits of data read.
     */
    default int cpuReadMemInstr(int address) {
        return cpuReadMemByte(address);
    }

    /**
     * Read a byte of data from memory (with CPU side-effects, not during M1 machine cycle).
     * To be used by the emulator when emulating CPU memory access.
     *
     * @param address 16-bit memory address.
     * @return int 8 bits of data read.
     */
    default int cpuReadMemByte(int address) {
        return rawReadMemByte(address);
    }

    /**
     * Read a word of data from memory (with CPU side-effects, not during M1 machine cycle).
     * To be used by the emulator when emulating CPU memory access.
     *
     * @param address 16-bit memory address.
     * @return int 16 bits of data read.
     */
    default int cpuReadMemWord(int address) {
        return cpuReadMemByte(address) | (cpuReadMemByte((address + 1) & 0xFFFF) << 8);
    }

    /**
     * Read a byte of data from I/O port (with CPU side-effects).
     * To be used by the emulator when emulating CPU I/O access.
     * <p>
     * Z80 assembly language encourages you to consider only bottom 8 bits, but `IN A, (n)` actually reads from the
     * 16-bit port number `(A << 8) | n` and `IN A, (C)` actually reads form the 16-bit port number `BC`. Of course,
     * the hardware is free to ignore the top 8 bits of the address bus, but the Sinclair and Amstrad machines do not.
     * <p>
     * Conversely, the 6502 CPU does not have separate instructions for accessing specific I/O ports, but maps external
     * I/O to memory reads and writes at specific addresses (memory-mapped I/O). It will never call this method.
     *
     * @param portNum 16-bit port number.
     * @return int 8 bits of data read.
     */
    default int cpuReadPortByte(int portNum) {
        return rawReadPortByte(portNum);
    }

    /**
     * Write a byte of data to memory (with CPU side-effects).
     * To be used by the emulator when emulating CPU memory access.
     *
     * @param address 16-bit memory address.
     * @param data 8 bits of data to write.
     */
    default void cpuWriteMemByte(int address, int data) {
        rawWriteMemByte(address, data);
    }

    /**
     * Write a word of data to memory (with CPU side-effects).
     * To be used by the emulator when emulating CPU memory access.
     *
     * @param address 16-bit memory address.
     * @param data 16 bits of data to write.
     */
    default void cpuWriteMemWord(int address, int data) {
        cpuWriteMemByte(address, data & 0xFF);
        cpuWriteMemByte((address + 1) & 0xFFFF, (data & 0xFF00) >>> 8);
    }

    /**
     * Write a byte of data to an I/O port (with CPU side-effects).
     * To be used by the emulator when emulating CPU I/O access.
     * <p>
     * @param portNum 16-bit port number.
     * @param data 8 bits of data to write.
     */
    default void cpuWritePortByte(int portNum, int data) {
        rawWritePortByte(portNum, data);
    }

    // ----------------------------------------------------------------------------------------------
    // ---------- Override the `rawRead*` and `rawWrite*` methods to change implementation ----------
    // ----------------------------------------------------------------------------------------------

    /**
     * Read a byte of data from memory (without CPU side-effects).
     * To be used by the emulator during debugging, etc., rather than emulating the CPU.
     *
     * @param address 16-bit memory address.
     * @return int 8 bits of data read.
     */
    /* abstract */ int rawReadMemByte(int address);

    /**
     * Read an array of data from memory (without CPU side-effects).
     * To be used by the emulator during debugging, etc., rather than emulating the CPU.
     *
     * @param address 16-bit memory address.
     * @param dest array to read the data into.
     */
    default void rawReadMemBytes(int address, byte[] dest, int offset, int length) {
        for (int i = 0; i < length; ++i) {
            dest[offset+i] = (byte) rawReadMemByte(address+i);
        }
    }

    /**
     * Read a byte of data from I/O port (without CPU side-effects).
     * To be used by the emulator during debugging, etc., rather than emulating the CPU.
     *
     * @param portNum 16-bit port number.
     * @return int 8 bits of data read.
     */
    default int rawReadPortByte(int portNum) {
        return 0;
    }

    /**
     * Write a byte of data into memory (without CPU side-effects).
     * To be used by the emulator during debugging, etc., rather than emulating the CPU.
     *
     * @param address 16-bit memory address.
     * @param data 8 bits of data to write.
     */
    /* abstract */ void rawWriteMemByte(int address, int data);

    /**
     * Write an array of data into memory (without CPU side-effects).
     * To be used by the emulator during debugging, etc., rather than emulating the CPU.
     *
     * @param address 16-bit memory address.
     * @param source array to write the data from.
     */
    default void rawWriteMemBytes(int address, byte[] source, int offset, int length) {
        for (int i = 0; i < length; ++i) {
            rawWriteMemByte(address+i, source[offset+i]);
        }
    }

    /**
     * Write a byte of data to an I/O port (without CPU side-effects).
     * To be used by the emulator during debugging, etc., rather than emulating the CPU.
     * <p>
     * @param portNum 16-bit port number.
     * @param data 8 bits of data to write.
     */
    default void rawWritePortByte(int portNum, int data) {
        // do nothing.
    }
}
