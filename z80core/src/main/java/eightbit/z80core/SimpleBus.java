package eightbit.z80core;

import eightbit.IBus;

public class SimpleBus implements IBus {
    private byte[] memory = new byte[0x10000];

    @Override
    public int readOpCode(int address) {
        return memory[address & 0xFFFF] & 0xFF;
    }

    @Override
    public int readMemory(int address) {
        return memory[address & 0xFFFF] & 0xFF;
    }

    @Override
    public int readIoPort(int portnum) {
        return 0;
    }

    @Override
    public void writeMemory(int address, int data) {
        memory[address & 0xFFFF] = (byte) (data & 0xFF);
    }

    @Override
    public void writeIoPort(int portnum, int data) {
        // do nothing.
    }
    
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
}
