package com.davidconneely.eightbit.zx81;

import com.davidconneely.eightbit.IBus;

class ZX81Bus implements IBus {
  private static final int LAST_K = 0x4025;

  private final TerminalKeyboard keyboard;
  private final int[] rom;
  private final int[] ram;

  private boolean nmiGeneratorOn;
  private boolean vsync;
  private boolean romRunning;

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
  public int cpuReadMemByte(final int address) {
    if ((address == LAST_K || address == LAST_K + 1) && !romRunning) {
      keyboard.ask(); // the program is looking for a key
    }
    return rawReadMemByte(address);
  }

  /**
   * Reading port $FE gives the keys in bits 4-0, 1 in bit 5, 1 in bit 6 (a UK machine, with 50 Hz
   * TV) and the tape input in bit 7, which is 0 as there is no tape signal.
   */
  @Override
  public int cpuReadPortByte(final int portNum) {
    if ((portNum & 0x01) != 0) {
      return keyboard.readKeyPortByte(portNum);
    }
    vsync = true; // reading port $FE starts the vertical sync pulse, which starts a TV frame
    if (!romRunning) {
      keyboard.ask(); // the program is reading the keyboard itself
    }
    return keyboard.readKeyPortByte(portNum) & 0x7F;
  }

  /** Set whether the ROM is running, rather than the program. */
  void romRunning(final boolean romRunning) {
    this.romRunning = romRunning;
  }

  /** OUT to port $FE turns the NMI generator on, and OUT to port $FD turns it off. */
  @Override
  public void cpuWritePortByte(final int portNum, final int data) {
    if ((portNum & 0x01) == 0) {
      nmiGeneratorOn = true;
    } else if ((portNum & 0x02) == 0) {
      nmiGeneratorOn = false;
    }
  }

  /** Whether there has been a vertical sync pulse since the last call, so a new TV frame. */
  boolean takeVsync() {
    final boolean vsync = this.vsync;
    this.vsync = false;
    return vsync;
  }

  /** Whether the NMI generator is on, so there is a non-maskable interrupt every scan line. */
  boolean isNmiGeneratorOn() {
    return nmiGeneratorOn;
  }
}
