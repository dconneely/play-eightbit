package eightbit;

public interface IBus {
    /**
     * read from memory during the M1 machine cycle which will be the op code of the next instruction.
     * @param address 16-bit memory address.
     * @return 8 bits of data (the op code).
     */
    int readOpCode(int address);

    /**
     * read from memory of data (i.e. not during the M1 machine cycle of an instruction).
     * @param address 16-bit memory address.
     * @return 8 bits of data.
     */
    int readMemory(int address);
    
    int readIoPort(int portnum);
    
    void writeMemory(int address, int data);
    
    void writeIoPort(int portnum, int data);

    void writeMemory(int address, byte[] source, int offset, int length);
}
