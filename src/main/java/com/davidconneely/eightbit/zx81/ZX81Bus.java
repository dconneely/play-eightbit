package com.davidconneely.eightbit.zx81;

import com.davidconneely.eightbit.IBus;

public class ZX81Bus implements IBus {
    private final TerminalKeyboard keyboard;
    private final int[] rom;
    private final int[] ram;

    ZX81Bus(TerminalKeyboard keyboard) {
        this.keyboard = keyboard;
        this.rom = new int[0x2000]; // 8 kiB
        this.ram = new int[0x4000]; // 16 kiB
    }

    @Override
    public int rawReadMemByte(int address) {
        address &= 0x7FFF; // ignore bit 15 of address.
        if (address < 0x4000) {
            return rom[address & 0x1FFF] & 0xFF;
        } else {
            return ram[address - 0x4000] & 0xFF;
        }
    }

    @Override
    public int cpuReadMemInstr(int address) {
        boolean lomem = (address & 0x8000) == 0;
        int n = rawReadMemByte(address);
        return (lomem || (n & 0x40) == 0x40) ? (n & 0xFF) : 0;
    }

    @Override
    public void rawWriteMemByte(int address, int data) {
        address &= 0x7FFF; // ignore bit 15 of address.
        if (address < 0x4000) {
            rom[address & 0x1FFF] = (byte) data;
        } else {
            ram[address - 0x4000] = (byte) data;
        }
    }

    @Override
    public void cpuWriteMemByte(int address, int data) {
        // don't write to ROM, shadow ROM or shadow RAM.
        if (address >= 0x4000 && address < 0x8000) {
            rawWriteMemByte(address, data);
        }
    }

    @Override
    public int cpuReadPortByte(int portNum) {
        return keyboard.readKeyPortByte(portNum);
    }
}
