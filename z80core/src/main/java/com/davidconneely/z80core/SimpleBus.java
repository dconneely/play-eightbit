package com.davidconneely.z80core;

public class SimpleBus implements IBus {
    private final byte[] memory = new byte[0x10000];

    @Override
    public int readInstruction(int address) {
        return memory[address & 0xFFFF] & 0xFF;
    }

    @Override
    public int readMemory(int address) {
        return memory[address & 0xFFFF] & 0xFF;
    }

    /**
     * For saving programs or data from memory.
     */
    @Override
    public void readMemory(int address, byte[] dest, int offset, int length) {
        if (offset < 0 || length < 0 || offset >= dest.length || offset + length > dest.length) {
            throw new IndexOutOfBoundsException();
        }
        int safeaddress = address & 0xFFFF;
        if (safeaddress + length > 0x10000) {
            throw new IndexOutOfBoundsException();
        }
        System.arraycopy(memory, safeaddress, dest, offset, length);
    }

    @Override
    public int readIoPort(int portnum) {
        return 0;
    }

    @Override
    public void writeMemory(int address, int data) {
        memory[address & 0xFFFF] = (byte) (data & 0xFF);
    }

    /**
     * For loading programs or data into memory.
     */
    @Override
    public void writeMemory(int address, byte[] source, int offset, int length) {
        if (offset < 0 || length < 0 || offset >= source.length || offset + length > source.length) {
            throw new IndexOutOfBoundsException();
        }
        int safeaddress = address & 0xFFFF;
        if (safeaddress + length > 0x10000) {
            throw new IndexOutOfBoundsException();
        }
        System.arraycopy(source, offset, memory, safeaddress, length);
    }

    @Override
    public void writeIoPort(int portnum, int data) {
        // do nothing.
    }
}
