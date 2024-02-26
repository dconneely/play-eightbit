package com.davidconneely.eightbit.z80;

import com.davidconneely.eightbit.IBus;

final class Core {
    private final IBus bus;
    private final State state;

    public Core(final IBus bus) {
        this.bus = bus;
        this.state = new State();
    }

    State state() {
        return state;
    }

    void step() {
        if (state.halted()) {
            // execute an effective NOP
            state.cyclesInc(4);
            return;
        }
        int pc = state.pc();
        int opCode = bus.readInstruction(pc);
        state.pcInc1();
        decode(opCode);
    }

    private void decode(int opCode) {
        //int x = (opCode & 0xC0); // values 0x00,0x40,0x80 or 0x80
        //int y = (opCode & 0x38); // not shifting here to avoid unnecessary work
        //int z = (opCode & 0x07);
        switch (opCode) {
            case 0x00 -> { /*NOP*/
            }
            case 0x01 -> { /* LD BC, imm16 */
                state.bc(bus.readWord(state.pc()));
                state.pcInc2();
            }
            case 0x02 -> { /* LD (BC), A */
                bus.writeMemory(state.bc(), state.a());
            }
            case 0x03 -> { /* INC BC */
                state.bc(state.bc() + 1);
            }
            case 0x04 -> { /* INC B */
                state.b(state.b() + 1);
            }
            case 0x05 -> { /* DEC B */
                state.b(state.b() - 1);
            }
            case 0x06 -> { /* LD B, imm8> */
                state.b(bus.readMemory(state.pc()));
                state.pcInc1();
            }
            case 0x07 -> { /* RLCA */
                //state.rlca();
            }
            case 0x08 -> { /* EX AF, AF' */
                //state.exAfAf_();
            }
            case 0x09 -> { /* ADD HL, BC */
                state.hl(state.hl() + state.bc());
            }
            case 0x0A -> { /* LD A, (BC) */
                state.a(bus.readMemory(state.bc()));
            }
            case 0x0B -> { /* DEC BC */
                state.bc(state.bc() - 1);
            }
            case 0x0C -> { /* INC C */
                state.c(state.c() + 1);
            }
            case 0x0D -> { /* DEC C */
                state.c(state.c() - 1);
            }
            case 0x0E -> { /* LD C, imm8 */
                state.c(bus.readMemory(state.pc()));
                state.pcInc1();
            }
            case 0x0F -> { /* RRCA */
                //state.rrca();
            }
            case 0x10 -> { /* DJNZ imm8 */
                state.b(state.b() - 1);
                if (state.b() != 0) {
                    int offset = bus.readMemory(state.pc());
                    state.pc(state.pc() + (offset < 0x80 ? offset : offset - 0x100) + 1);
                } else {
                    state.pcInc1();
                }
            }
            case 0x11 -> { /* LD DE, imm16 */
                state.de(bus.readWord(state.pc()));
                state.pcInc2();
            }
            case 0x12 -> { /* LD (DE), A */
                bus.writeMemory(state.de(), state.a());
            }
            case 0x13 -> { /* INC DE */
                state.de(state.de() + 1);
            }
            case 0x14 -> { /* INC D */
                state.d(state.d() + 1);
            }
            case 0x15 -> { /* DEC D */
                state.d(state.d() - 1);
            }
            case 0x16 -> { /* LD D, imm8 */
                state.d(bus.readMemory(state.pc()));
                state.pcInc1();
            }
            case 0x17 -> { /* RLA */
                //state.rla();
            }
            case 0x18 -> { /* JR imm8 */
                int offset = bus.readMemory(state.pc());
                state.pc(state.pc() + (offset < 0x80 ? offset : offset - 0x100) + 1);
            }
            case 0x19 -> { /* ADD HL, DE */
                state.hl(state.hl() + state.de());
            }
            case 0x1A -> { /* LD A, (DE) */
                state.a(bus.readMemory(state.de()));
            }
            case 0x1B -> { /* DEC DE */
                state.de(state.de() - 1);
            }
            case 0x1C -> { /* INC E */
                state.e(state.e() + 1);
            }
            case 0x1D -> { /* DEC E */
                state.e(state.e() - 1);
            }
            case 0x1E -> { /* LD E, imm8 */
                state.e(bus.readMemory(state.pc()));
                state.pcInc1();
            }
            case 0x1F -> { /* RRA */
               // state.rra();
            }
            case 0x20 -> { /* JR NZ, imm8 */
                if (!state.zf()) {
                    int offset = bus.readMemory(state.pc());
                    state.pc(state.pc() + (offset < 0x80 ? offset : offset - 0x100) + 1);
                } else {
                    state.pcInc1();
                }
            }
            case 0x21 -> { /* LD HL, imm16 */
                state.hl(bus.readWord(state.pc()));
                state.pcInc2();
            }
            case 0x22 -> { /* LD (imm16), HL */
                int address = bus.readWord(state.pc());
                bus.writeWord(address, state.hl());
                state.pcInc2();
            }
            case 0x23 -> { /* INC HL */
                state.hl(state.hl() + 1);
            }
            case 0x24 -> { /* INC H */
                state.h(state.h() + 1);
            }
            case 0x25 -> { /* DEC H *
                state.h(state.h() - 1);
            }
            case 0x26 -> { /* LD H, imm8 */
                state.h(bus.readMemory(state.pc()));
                state.pcInc1();
            }
            case 0x27 -> { /* DAA */
                //state.daa();
            }
            case 0x28 -> { /* JR Z, imm8 */
                if (state.zf()) {
                    int offset = bus.readMemory(state.pc());
                    state.pc(state.pc() + (offset < 0x80 ? offset : offset - 0x100) + 1);
                } else {
                    state.pcInc1();
                }
            }
            case 0x29 -> { /* ADD HL, HL */
                state.hl(state.hl() + state.hl());
            }
            case 0x2A -> { /* LD HL, (imm16) */
                int address = bus.readWord(state.pc());
                state.hl(bus.readWord(address));
                state.pcInc2();
            }
            case 0x2B -> { /* DEC HL */
                state.hl(state.hl() - 1);
            }
            case 0x2C -> { /* INC L */
                state.l(state.l() + 1);
            }
            case 0x2D -> { /* DEC L */
                state.l(state.l() - 1);
            }
            case 0x2E -> { /* LD L, imm8 */
                state.l(bus.readMemory(state.pc()));
                state.pcInc1();
            }
            case 0x2F -> { /* CPL */
                //state.cpl();
            }
            case 0x30 -> { /* JR NC, imm8 */
                if (!state.cf()) {
                    int offset = bus.readMemory(state.pc());
                    state.pc(state.pc() + (offset < 0x80 ? offset : offset - 0x100) + 1);
                } else {
                    state.pcInc1();
                }
            }
            case 0x31 -> { /* LD SP, imm16 */
                state.sp(bus.readWord(state.pc()));
                state.pcInc2();
            }
            case 0x32 -> { /* LD (imm16), A */
                int address = bus.readWord(state.pc());
                bus.writeMemory(address, state.a());
                state.pcInc2();
            }
            case 0x33 -> { /* INC SP */
                state.sp(state.sp() + 1);
            }
            case 0x34 -> { /* INC (HL) */
                int value = bus.readMemory(state.hl());
                bus.writeMemory(state.hl(), value + 1);
            }
            case 0x35 -> { /* DEC (HL) */
                int value = bus.readMemory(state.hl());
                bus.writeMemory(state.hl(), value - 1);
            }
            case 0x36 -> { /* LD (HL), imm8 */
                bus.writeMemory(state.hl(), bus.readMemory(state.pc()));
                state.pcInc1();
            }
            case 0x37 -> { /* SCF */
                //state.scf();
            }
            case 0x38 -> { /* JR C, imm8 */
                if (state.cf()) {
                    int offset = bus.readMemory(state.pc());
                    state.pc(state.pc() + (offset < 0x80 ? offset : offset - 0x100) + 1);
                } else {
                    state.pcInc1();
                }
            }
            case 0x39 -> { /* ADD HL, SP */
                state.hl(state.hl() + state.sp());
            }
            case 0x3A -> { /* LD A, (imm16) */
                int address = bus.readWord(state.pc());
                state.a(bus.readMemory(address));
                state.pcInc2();
            }
            case 0x3B -> { /* DEC SP */
                state.sp(state.sp() - 1);
            }
            case 0x3C -> { /* INC A */
                state.a(state.a() + 1);
            }
            case 0x3D -> { /* DEC A */
                state.a(state.a() - 1);
            }
            case 0x3E -> { /* LD A, imm8 */
                state.a(bus.readMemory(state.pc()));
                state.pcInc1();
            }
            case 0x3F -> { /* CCF */
                //state.ccf();
            }
            case 0x40 -> { /* LD B, B */
                //state.b(state.b()):
            }
            case 0x41 -> { /* LD B, C */
                state.b(state.c());
            }
            case 0x42 -> { /* LD B, D */
                state.b(state.d());
            }
            case 0x43 -> { /* LD B, E */
                state.b(state.e());
            }
            case 0x44 -> { /* LD B, H */
                state.b(state.h());
            }
            case 0x45 -> { /* LD B, L */
                state.b(state.l());
            }
            case 0x46 -> { /* LD B, (HL) */
                state.b(bus.readMemory(state.hl()));
            }
            case 0x47 -> { /* LD B, A */
                state.b(state.a());
            }
            case 0x48 -> { /* LD C, B */
                state.c(state.b());
            }
            case 0x49 -> { /* LD C, C */
                //state.c(state.c());
            }
            case 0x4A -> { /* LD C, D */
                state.c(state.d());
            }
            case 0x4B -> { /* LD C, E */
                state.c(state.e());
            }
            case 0x4C -> { /* LD C, H */
                state.c(state.h());
            }
            case 0x4D -> { /* LD C, L */
                state.c(state.l());
            }
            case 0x4E -> { /* LD C, (HL) */
                state.c(bus.readMemory(state.hl()));
            }
            case 0x4F -> { /* LD C, A */
                state.c(state.a());
            }
            case 0x50 -> { /* LD D, B */
                state.d(state.b());
            }
            case 0x51 -> { /* LD D, C */
                state.d(state.c());
            }
            case 0x52 -> { /* LD D, D */
                //state.d(state.d());
            }
            case 0x53 -> { /* LD D, E */
                state.d(state.e());
            }
            case 0x54 -> { /* LD D, H */
                state.d(state.h());
            }
            case 0x55 -> { /* LD D, L */
                state.d(state.l());
            }
            case 0x56 -> { /* LD D, (HL) */
                state.d(bus.readMemory(state.hl()));
            }
            case 0x57 -> { /* LD D, A */
                state.d(state.a());
            }
            case 0x58 -> { /* LD E, B */
                state.e(state.b());
            }
            case 0x59 -> { /* LD E, C */
                state.e(state.c());
            }
            case 0x5A -> { /* LD E, D */
                state.e(state.d());
            }
            case 0x5B -> { /* LD E, E */
                //state.e(state.e());
            }
            case 0x5C -> { /* LD E, H */
                state.e(state.h());
            }
            case 0x5D -> { /* LD E, L */
                state.e(state.l());
            }
            case 0x5E -> { /* LD E, (HL) */
                state.e(bus.readMemory(state.hl()));
            }
            case 0x5F -> { /* LD E, A */
                state.e(state.a());
            }
            case 0x60 -> { /* LD H, B */
                state.h(state.b());
            }
            case 0x61 -> { /* LD H, C */
                state.h(state.c());
            }
            case 0x62 -> { /* LD H, D */
                state.h(state.d());
            }
            case 0x63 -> { /* LD H, E */
                state.h(state.e());
            }
            case 0x64 -> { /* LD H, H */
                state.h(state.h());
            }
            case 0x65 -> { /* LD H, L */
                state.h(state.l());
            }
            case 0x66 -> { /* LD H, (HL) */
                state.h(bus.readMemory(state.hl()));
            }
            case 0x67 -> { /* LD H, A */
                state.h(state.a());
            }
            case 0x68 -> { /* LD L, B */
                state.l(state.b());
            }
            case 0x69 -> { /* LD L, C */
                state.l(state.c());
            }
            case 0x6A -> { /* LD L, D */
                state.l(state.d());
            }
            case 0x6B -> { /* LD L, E */
                state.l(state.e());
            }
            case 0x6C -> { /* LD L, H */
                state.l(state.h());
            }
            case 0x6D -> { /* LD L, L */
                //state.l(state.l());
            }
            case 0x6E -> { /* LD L, (HL) */
                state.l(bus.readMemory(state.hl()));
            }
            case 0x6F -> { /* LD L, A */
                state.l(state.a());
            }
            case 0x70 -> { /* LD (HL), B */
                bus.writeMemory(state.hl(), state.b());
            }
            case 0x71 -> { /* LD (HL), C */
                bus.writeMemory(state.hl(), state.c());
            }
            case 0x72 -> { /* LD (HL), D */
                bus.writeMemory(state.hl(), state.d());
            }
            case 0x73 -> { /* LD (HL), E */
                bus.writeMemory(state.hl(), state.e());
            }
            case 0x74 -> { /* LD (HL), H */
                bus.writeMemory(state.hl(), state.h());
            }
            case 0x75 -> { /* LD (HL), L */
                bus.writeMemory(state.hl(), state.l());
            }
            case 0x76 -> { /* HALT */
                state.halted(true);
            }
            case 0x77 -> { /* LD (HL), A */
                bus.writeMemory(state.hl(), state.a());
            }
            case 0x78 -> { /* LD A, B */
                state.a(state.b());
            }
            case 0x79 -> { /* LD A, C */
                state.a(state.c());
            }
            case 0x7A -> { /* LD A, D */
                state.a(state.d());
            }
            case 0x7B -> { /* LD A, E */
                state.a(state.e());
            }
            case 0x7C -> { /* LD A, H */
                state.a(state.h());
            }
            case 0x7D -> { /* LD A, L */
                state.a(state.l());
            }
            case 0x7E -> { /* LD A, (HL) */
                state.a(bus.readMemory(state.hl()));
            }
            case 0x7F -> { /* LD A, A */
                //state.a(state.a());
            }
        }
    }
}
