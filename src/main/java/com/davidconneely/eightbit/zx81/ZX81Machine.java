package com.davidconneely.eightbit.zx81;

import com.davidconneely.eightbit.IBus;
import com.davidconneely.eightbit.z80.Core;

import java.io.IOException;

public class ZX81Machine {
    private final TerminalSupport terminal;
    private final TerminalDisplay display;
    private final TerminalKeyboard keyboard;
    private final IBus bus;
    private final Core core;

    ZX81Machine() {
        this.terminal = WindowsTerminalSupport.INSTANCE;
        this.display = new TerminalDisplay(terminal);
        this.keyboard = new TerminalKeyboard(terminal);
        this.bus = new ZX81Bus(this.keyboard);
        this.core = new Core(this.bus);
    }

    public static void main(String[] args) throws IOException {
        ZX81Machine zx81 = new ZX81Machine();
        zx81.loadRom();
        try {
            zx81.init();
            zx81.run();
        } catch (ShutdownException ex) {
            // do nothing.
        } finally {
            zx81.reset();
        }
    }

    private void init() {
        terminal.enableRawMode();
        display.init();
    }

    private void reset() {
        display.reset();
        terminal.reset();
    }

    private void run() throws IOException {
        long instructions = 0L;
        long lastRender = System.currentTimeMillis();
        core.state().pc(0);
        while (true) {
            core.step();
            ++instructions;
            int pc = core.state().pc();
            if (pc == 0x0343) {
                if (core.state().cf()) {
                    core.state().pc(0x02F4); // LOAD requires a filename in this emulator.
                } else {
                    loadProgram(core.state().de());
                    core.state().pc(bus.cpuReadMemWord(core.state().spInc2()));
                }
            }
            if (pc > 0x7FFF) { // we're probably waiting on user input.
                long now = System.currentTimeMillis();
                display.renderDFile(bus);
                lastRender = now;
                // effective RET.
                core.state().pc(bus.cpuReadMemWord(core.state().spInc2()));
                continue;
            }
            if ((instructions & 0x1FFFFF) == 0L) { // check about every 2.1 million instructions executed if >= 20ms.
                long now = System.currentTimeMillis();
                if (now - lastRender >= 20L) {
                    display.renderDFile(bus);
                    lastRender = now;
                }
            }
        }
    }

    private void loadRom() throws IOException {
        var rom = getClass().getResourceAsStream("/z80/zx81/zx81.rom").readAllBytes();
        bus.rawWriteMemBytes(0, rom, 0, rom.length);
    }

    private void loadProgram(int addressFilename) throws IOException {
        var program = getClass().getResourceAsStream("/z80/zx81/" + getFilename(addressFilename) + ".p").readAllBytes();
        bus.rawWriteMemBytes(16393, program, 0, program.length);
    }

    private String getFilename(int addressFilename) {
        StringBuilder sb = new StringBuilder();
        int ch07;
        do {
            ch07 = bus.rawReadMemByte(addressFilename++);
            int ch06 = (ch07 & 0x7F);
            if (ch06 >= 38 && ch06 <= 63) { // a-z
                sb.append((char) (ch06 - 38 + 'a'));
            } else if (ch06 >= 28 && ch06 <= 37) { // 0-9
                sb.append((char) (ch06 - 28 + '0'));
            } else {
                sb.append(switch (ch06) {
                    case 16 -> '(';
                    case 17 -> ')';
                    case 22 -> '-';
                    case 27 -> '.';
                    default -> '_';
                });
            }
        } while ((ch07 & 0x80) == 0);
        return sb.toString();
    }
}
