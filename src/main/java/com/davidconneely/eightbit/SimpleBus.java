package com.davidconneely.eightbit;

public class SimpleBus implements IBus {
    private static final int MEMORY_SIZE = 0x10000;
    private final byte[] memory = new byte[MEMORY_SIZE];

    @Override
    public int rawReadMemByte(final int address) {
        checkAddress(address, 1, 0, 1);
        return memory[address] & 0xFF;
    }

    @Override
    public void rawReadMemBytes(final int address, final byte[] dest, final int offset, final int length) {
        checkAddress(address, dest.length, offset, length);
        System.arraycopy(memory, address, dest, offset, length);
    }

    @Override
    public void rawWriteMemByte(final int address, final int data) {
        checkAddress(address, 1, 0, 1);
        memory[address] = (byte) (data & 0xFF);
    }

    @Override
    public void rawWriteMemBytes(final int address, final byte[] source, final int offset, final int length) {
        checkAddress(address, source.length, offset, length);
        System.arraycopy(source, offset, memory, address, length);
    }

    private void checkAddress(final int address, final int arrayLen, final int offset, final int length) {
        if (address < 0 || offset < 0 || length < 0
                || address >= MEMORY_SIZE || length > MEMORY_SIZE || address + length > MEMORY_SIZE
                || offset >= arrayLen || offset + length > arrayLen) {
            throw new IndexOutOfBoundsException();
        }
    }
}
