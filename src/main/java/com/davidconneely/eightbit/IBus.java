package com.davidconneely.eightbit;

public interface IBus {
    /**
     * read from memory during the M1 machine cycle which will be the op code of the next instruction.
     *
     * @param address 16-bit memory address.
     * @return 8 bits of data (the op code).
     */
    int readInstruction(int address);

    /**
     * Z80 assembly language encourages you to consider only bottom 8 bits, but `IN A, (n)` actually reads from the
     * 16-bit port number `(A << 8) | n` and `IN B, (C)` actually reads form the 16-bit port number `BC`. Of course,
     * the hardware is free to ignore the top 8 bits of the address bus, but the Sinclar and Amstrad machines do not.
     *
     * @param portNum 16-bit port number (although hardware may only consider some address bits significant).
     * @return the byte read from the specified port.
     */
    int readIoPort(int portNum);

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
    void writeIoPort(int portNum, int data);

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
