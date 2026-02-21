package com.davidconneely.eightbit.zx81;

import com.davidconneely.eightbit.IBus;

class ZX81Bus implements IBus {
  private final TerminalKeyboard keyboard;
  private final int[] rom;
  private final int[] ram;

  ZX81Bus(final TerminalKeyboard keyboard) {
    this.keyboard = keyboard;
    this.rom = new int[0x2000]; // 8 kiB
    this.ram = new int[0x4000]; // 16 kiB
  }

  @Override
  public int rawReadMemByte(final int rawAddress) {
    final int address = rawAddress & 0x7FFF; // ignore bit 15 of address.
    if (address < 0x4000) {
      return rom[address & 0x1FFF] & 0xFF;
    } else {
      return ram[address - 0x4000] & 0xFF;
    }
  }

  @Override
  public int cpuReadMemInstr(final int address) {
    final boolean lomem = (address & 0x8000) == 0;
    final int n = rawReadMemByte(address);
    return (lomem || (n & 0x40) == 0x40) ? (n & 0xFF) : 0;
  }

  @Override
  public void rawWriteMemByte(final int rawAddress, final int data) {
    final int address = rawAddress & 0x7FFF; // ignore bit 15 of address.
    if (address < 0x4000) {
      rom[address & 0x1FFF] = (byte) data;
    } else {
      ram[address - 0x4000] = (byte) data;
    }
  }

  @Override
  public void cpuWriteMemByte(final int address, final int data) {
    // don't write to ROM, shadow ROM or shadow RAM.
    if (address >= 0x4000 && address < 0x8000) {
      rawWriteMemByte(address, data);
    }
  }

  @Override
  public int cpuReadPortByte(final int portNum) {
    return keyboard.readKeyPortByte(portNum);
  }
}
