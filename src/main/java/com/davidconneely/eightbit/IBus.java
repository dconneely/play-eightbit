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
     * read from memory of data (i.e. not during the M1 machine cycle of an instruction).
     *
     * @param address 16-bit memory address.
     * @return 8 bits of data.
     */
    int readMemory(int address);

    void readMemory(int address, byte[] dest, int offset, int length);

    int readIoPort(int portnum);

    default int readWord(int address) {
        return (readMemory((address + 1) & 0xFFFF) << 8) | readMemory(address);
    }

    void writeMemory(int address, int data);

    void writeMemory(int address, byte[] source, int offset, int length);

    void writeIoPort(int portnum, int data);

    default void writeWord(int address, int data) {
        writeMemory(address, data & 0xFF);
        writeMemory((address + 1) & 0xFFFF, (data & 0xFF00) >>> 8);
    }
}
