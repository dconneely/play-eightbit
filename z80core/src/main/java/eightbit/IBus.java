package eightbit;

public interface IBus {
    /**
     * read from memory during the M1 machine cycle which will be the op code of the next instruction.
     * @param address 16-bit memory address.
     * @return 8 bits of data (the op code).
     */
    byte readOpCode(short address);

    /**
     * read from memory of data (i.e. not during the M1 machine cycle of an instruction).
     * @param address 16-bit memory address.
     * @return 8 bits of data.
     */
    byte readMemory(short address);
    
    byte readIoPort(short portnum);
    
    void writeMemory(short address, byte data);
    
    void writeIoPort(short portnum, byte data);
}
