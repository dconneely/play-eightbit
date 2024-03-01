package com.davidconneely.eightbit;

public class SimpleBus implements IBus {
    private static final int MEMORY_SIZE = 0x10000;
    private final byte[] memory = new byte[MEMORY_SIZE];

    @Override
    public int readMemory(int address) {
        checkAddress(address, 1, 0, 1);
        return memory[address] & 0xFF;
    }

    @Override
    public void readMemory(int address, byte[] dest, int offset, int length) {
        checkAddress(address, dest.length, offset, length);
        System.arraycopy(memory, address, dest, offset, length);
    }

    @Override
    public void writeMemory(int address, int data) {
        checkAddress(address, 1, 0, 1);
        memory[address] = (byte) (data & 0xFF);
    }

    @Override
    public void writeMemory(int address, byte[] source, int offset, int length) {
        checkAddress(address, source.length, offset, length);
        System.arraycopy(source, offset, memory, address, length);
    }

    private void checkAddress(int address, int arrayLen, int offset, int length) {
        if (address < 0 || offset < 0 || length < 0
                || address >= MEMORY_SIZE || length > MEMORY_SIZE || address + length > MEMORY_SIZE
                || offset >= arrayLen || offset + length > arrayLen) {
            throw new IndexOutOfBoundsException();
        }
    }
}
