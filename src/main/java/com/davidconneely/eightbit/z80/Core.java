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
        if (state.halted()) { // execute an effective NOP
            return;
        }
        decode(bus.readInstruction(state.pcInc1()));
    }

    private void decode(int opCode) {
        //int x = (opCode & 0xC0); // values 0x00,0x40,0x80 or 0x80
        //int y = (opCode & 0x38); // not shifting here to avoid unnecessary work
        //int z = (opCode & 0x07);
        switch (opCode) {
            case 0x00/*NOP*/ ->          {}
            case 0x01/*LD BC,imm16*/ ->  state.bc(bus.readWord(state.pcInc2()));
            case 0x02/*LD (BC),A*/ ->    bus.writeMemory(state.bc(), state.a());
            case 0x03/*INC BC*/ ->       state.bc(inc16(state.bc()));
            case 0x04/*INC B*/ ->        state.b(inc8(state.b()));
            case 0x05/*DEC B*/ ->        state.b(dec8(state.b()));
            case 0x06/*LD B,imm8>*/ ->   state.b(bus.readMemory(state.pcInc1()));
            case 0x07/*RLCA*/ ->         rlca();
            case 0x08/*EX AF,AF'*/ ->    exAfAf_();
            case 0x09/*ADD HL,BC*/ ->    state.hl(add16(state.bc()));
            case 0x0A/*LD A,(BC)*/ ->    state.a(bus.readMemory(state.bc()));
            case 0x0B/*DEC BC*/ ->       state.bc(dec16(state.bc()));
            case 0x0C/*INC C*/ ->        state.c(inc8(state.c()));
            case 0x0D/*DEC C*/ ->        state.c(dec8(state.c()));
            case 0x0E/*LD C,imm8*/ ->    state.c(bus.readMemory(state.pcInc1()));
            case 0x0F/*RRCA*/ ->         rrca();
            case 0x10/*DJNZ imm8*/ -> {
                state.b(state.b() - 1);
                if (state.b() != 0) {
                    int offset = bus.readMemory(state.pc());
                    state.pc(state.pc() + (offset < 0x80 ? offset : offset - 0x100) + 1);
                } else {
                    state.pcInc1();
                }
            }
            case 0x11/*LD DE,imm16*/ ->  state.de(bus.readWord(state.pcInc2()));
            case 0x12/*LD (DE),A*/ ->    bus.writeMemory(state.de(), state.a());
            case 0x13/*INC DE*/ ->       state.de(inc16(state.de()));
            case 0x14/*INC D*/ ->        state.d(inc8(state.d()));
            case 0x15/*DEC D*/ ->        state.d(dec8(state.d()));
            case 0x16/*LD D,imm8*/ ->    state.d(bus.readMemory(state.pcInc1()));
            case 0x17/*RLA*/ ->          rla();
            case 0x18/*JR imm8*/ -> {
                int offset = bus.readMemory(state.pc());
                state.pc(state.pc() + (offset < 0x80 ? offset : offset - 0x100) + 1);
            }
            case 0x19/*ADD HL,DE*/ ->    state.hl(add16(state.de()));
            case 0x1A/*LD A,(DE)*/ ->    state.a(bus.readMemory(state.de()));
            case 0x1B/*DEC DE*/ ->       state.de(dec16(state.de()));
            case 0x1C/*INC E*/ ->        state.e(inc8(state.e()));
            case 0x1D/*DEC E*/ ->        state.e(dec8(state.e()));
            case 0x1E/*LD E,imm8*/ ->    state.e(bus.readMemory(state.pcInc1()));
            case 0x1F/*RRA*/ ->          rra();
            case 0x20/*JR NZ,imm8*/ -> {
                if (!state.zf()) {
                    int offset = bus.readMemory(state.pc());
                    state.pc(state.pc() + (offset < 0x80 ? offset : offset - 0x100) + 1);
                } else {
                    state.pcInc1();
                }
            }
            case 0x21/*LD HL,imm16*/ ->  state.hl(bus.readWord(state.pcInc2()));
            case 0x22/*LD (imm16),HL*/ -> bus.writeWord(bus.readWord(state.pcInc2()), state.hl());
            case 0x23/*INC HL*/ ->       state.hl(inc16(state.hl()));
            case 0x24/*INC H*/ ->        state.h(inc8(state.h()));
            case 0x25/*DEC H*/ ->        state.h(dec8(state.h()));
            case 0x26/*LD H,imm8*/ ->    state.h(bus.readMemory(state.pcInc1()));
            case 0x27/*DAA*/ ->          daa();
            case 0x28/*JR Z,imm8*/-> {
                if (state.zf()) {
                    int offset = bus.readMemory(state.pc());
                    state.pc(state.pc() + (offset < 0x80 ? offset : offset - 0x100) + 1);
                } else {
                    state.pcInc1();
                }
            }
            case 0x29/*ADD HL,HL*/ ->    state.hl(add16(state.hl()));
            case 0x2A/*LD HL,(imm16)*/ -> state.hl(bus.readWord(bus.readWord(state.pcInc2())));
            case 0x2B/*DEC HL*/ ->       state.hl(dec16(state.hl()));
            case 0x2C/*INC L*/ ->        state.l(inc8(state.l()));
            case 0x2D/*DEC L*/ ->        state.l(dec8(state.l()));
            case 0x2E/*LD L,imm8*/ ->    state.l(bus.readMemory(state.pcInc1()));
            case 0x2F/*CPL*/ ->          cpl();
            case 0x30/*JR NC,imm8*/ -> {
                if (!state.cf()) {
                    int offset = bus.readMemory(state.pc());
                    state.pc(state.pc() + (offset < 0x80 ? offset : offset - 0x100) + 1);
                } else {
                    state.pcInc1();
                }
            }
            case 0x31/*LD SP,imm16*/ ->  state.sp(bus.readWord(state.pcInc2()));
            case 0x32/*LD (imm16),A*/ -> bus.writeMemory(bus.readWord(state.pcInc2()), state.a());
            case 0x33/*INC SP*/ ->       state.sp(inc16(state.sp()));
            case 0x34/*INC (HL)*/ ->     bus.writeMemory(state.hl(), inc8(bus.readMemory(state.hl())));
            case 0x35/*DEC (HL)*/ ->     bus.writeMemory(state.hl(), dec8(bus.readMemory(state.hl())));
            case 0x36/*LD (HL),imm8*/ -> bus.writeMemory(state.hl(), bus.readMemory(state.pcInc1()));
            case 0x37/*SCF*/ ->          scf();
            case 0x38/*JR C,imm8*/ -> {
                if (state.cf()) {
                    int offset = bus.readMemory(state.pc());
                    state.pc(state.pc() + (offset < 0x80 ? offset : offset - 0x100) + 1);
                } else {
                    state.pcInc1();
                }
            }
            case 0x39/*ADD HL,SP*/ ->    state.hl(add16(state.sp()));
            case 0x3A/*LD A,(imm16)*/ -> state.a(bus.readMemory(bus.readWord(state.pcInc2())));
            case 0x3B/*DEC SP*/ ->       state.sp(dec16(state.sp()));
            case 0x3C/*INC A*/ ->        state.a(inc8(state.a()));
            case 0x3D/*DEC A*/ ->        state.a(dec8(state.a()));
            case 0x3E/*LD A,imm8*/ ->    state.a(bus.readMemory(state.pcInc1()));
            case 0x3F/*CCF*/ ->          ccf();
            case 0x40/*LD B,B*/ ->       {/*state.b(state.b()):*/}
            case 0x41/*LD B,C*/ ->       state.b(state.c());
            case 0x42/*LD B,D*/ ->       state.b(state.d());
            case 0x43/*LD B,E*/ ->       state.b(state.e());
            case 0x44/*LD B,H*/ ->       state.b(state.h());
            case 0x45/*LD B,L*/ ->       state.b(state.l());
            case 0x46/*LD B,(HL)*/ ->    state.b(bus.readMemory(state.hl()));
            case 0x47/*LD B,A*/ ->       state.b(state.a());
            case 0x48/*LD C,B*/ ->       state.c(state.b());
            case 0x49/*LD C,C*/ ->       {/*state.c(state.c());*/}
            case 0x4A/*LD C,D*/ ->       state.c(state.d());
            case 0x4B/*LD C,E*/ ->       state.c(state.e());
            case 0x4C/*LD C,H*/ ->       state.c(state.h());
            case 0x4D/*LD C,L*/ ->       state.c(state.l());
            case 0x4E/*LD C,(HL)*/ ->    state.c(bus.readMemory(state.hl()));
            case 0x4F/*LD C,A*/ ->       state.c(state.a());
            case 0x50/*LD D,B*/ ->       state.d(state.b());
            case 0x51/*LD D,C*/ ->       state.d(state.c());
            case 0x52/*LD D,D*/ ->       {/*state.d(state.d());*/}
            case 0x53/*LD D,E*/ ->       state.d(state.e());
            case 0x54/*LD D,H*/ ->       state.d(state.h());
            case 0x55/*LD D,L*/ ->       state.d(state.l());
            case 0x56/*LD D,(HL)*/ ->    state.d(bus.readMemory(state.hl()));
            case 0x57/*LD D,A*/ ->       state.d(state.a());
            case 0x58/*LD E,B*/ ->       state.e(state.b());
            case 0x59/*LD E,C*/ ->       state.e(state.c());
            case 0x5A/*LD E,D*/ ->       state.e(state.d());
            case 0x5B/*LD E,E*/ ->       {/*state.e(state.e());*/}
            case 0x5C/*LD E,H*/ ->       state.e(state.h());
            case 0x5D/*LD E,L*/ ->       state.e(state.l());
            case 0x5E/*LD E,(HL)*/ ->    state.e(bus.readMemory(state.hl()));
            case 0x5F/*LD E,A*/ ->       state.e(state.a());
            case 0x60/*LD H,B*/ ->       state.h(state.b());
            case 0x61/*LD H,C*/ ->       state.h(state.c());
            case 0x62/*LD H,D*/ ->       state.h(state.d());
            case 0x63/*LD H,E*/ ->       state.h(state.e());
            case 0x64/*LD H,H*/ ->       {/*state.h(state.h());*/}
            case 0x65/*LD H,L*/ ->       state.h(state.l());
            case 0x66/*LD H,(HL)*/ ->    state.h(bus.readMemory(state.hl()));
            case 0x67/*LD H,A*/ ->       state.h(state.a());
            case 0x68/*LD L,B*/ ->       state.l(state.b());
            case 0x69/*LD L,C*/ ->       state.l(state.c());
            case 0x6A/*LD L,D*/ ->       state.l(state.d());
            case 0x6B/*LD L,E*/ ->       state.l(state.e());
            case 0x6C/*LD L,H*/ ->       state.l(state.h());
            case 0x6D/*LD L,L*/ ->       {/*state.l(state.l());*/}
            case 0x6E/*LD L,(HL)*/ ->    state.l(bus.readMemory(state.hl()));
            case 0x6F/*LD L,A*/ ->       state.l(state.a());
            case 0x70/*LD (HL),B*/ ->    bus.writeMemory(state.hl(), state.b());
            case 0x71/*LD (HL),C*/ ->    bus.writeMemory(state.hl(), state.c());
            case 0x72/*LD (HL),D*/ ->    bus.writeMemory(state.hl(), state.d());
            case 0x73/*LD (HL),E*/ ->    bus.writeMemory(state.hl(), state.e());
            case 0x74/*LD (HL),H*/ ->    bus.writeMemory(state.hl(), state.h());
            case 0x75/*LD (HL),L*/ ->    bus.writeMemory(state.hl(), state.l());
            case 0x76/*HALT*/ ->         state.halted(true);
            case 0x77/*LD (HL),A*/ ->    bus.writeMemory(state.hl(), state.a());
            case 0x78/*LD A,B*/ ->       state.a(state.b());
            case 0x79/*LD A,C*/ ->       state.a(state.c());
            case 0x7A/*LD A,D*/ ->       state.a(state.d());
            case 0x7B/*LD A,E*/ ->       state.a(state.e());
            case 0x7C/*LD A,H*/ ->       state.a(state.h());
            case 0x7D/*LD A,L*/ ->       state.a(state.l());
            case 0x7E/*LD A,(HL)*/ ->    state.a(bus.readMemory(state.hl()));
            case 0x7F/*LD A,A*/ ->       {/*state.a(state.a());*/}
            case 0x80/*ADD A,B*/ ->      add8(state.b());
            case 0x81/*ADD A,C*/ ->      add8(state.c());
            case 0x82/*ADD A,D*/ ->      add8(state.d());
            case 0x83/*ADD A,E*/ ->      add8(state.e());
            case 0x84/*ADD A,H*/ ->      add8(state.h());
            case 0x85/*ADD A,L*/ ->      add8(state.l());
            case 0x86/*ADD A,(HL)*/ ->   add8(bus.readMemory(state.hl()));
            case 0x87/*ADD A,A*/ ->      add8(state.a());
            case 0x88/*ADC A,B*/ ->      adc8(state.b());
            case 0x89/*ADC A,C*/ ->      adc8(state.c());
            case 0x8A/*ADC A,D*/ ->      adc8(state.d());
            case 0x8B/*ADC A,E*/ ->      adc8(state.e());
            case 0x8C/*ADC A,H*/ ->      adc8(state.h());
            case 0x8D/*ADC A,L*/ ->      adc8(state.l());
            case 0x8E/*ADC A,(HL)*/ ->   adc8(bus.readMemory(state.hl()));
            case 0x8F/*ADC A,A*/ ->      adc8(state.a());
            case 0x90/*SUB B*/ ->        sub8(state.b());
            case 0x91/*SUB C*/ ->        sub8(state.c());
            case 0x92/*SUB D*/ ->        sub8(state.d());
            case 0x93/*SUB E*/ ->        sub8(state.e());
            case 0x94/*SUB H*/ ->        sub8(state.h());
            case 0x95/*SUB L*/ ->        sub8(state.l());
            case 0x96/*SUB (HL)*/ ->     sub8(bus.readMemory(state.hl()));
            case 0x97/*SUB A*/ ->        sub8(state.a());
            case 0x98/*SBC A,B*/ ->      sbc8(state.b());
            case 0x99/*SBC A,C*/ ->      sbc8(state.c());
            case 0x9A/*SBC A,D*/ ->      sbc8(state.d());
            case 0x9B/*SBC A,E*/ ->      sbc8(state.e());
            case 0x9C/*SBC A,H*/ ->      sbc8(state.h());
            case 0x9D/*SBC A,L*/ ->      sbc8(state.l());
            case 0x9E/*SBC A,(HL)*/ ->   sbc8(bus.readMemory(state.hl()));
            case 0x9F/*SBC A,A*/ ->      sbc8(state.a());
            case 0xA0/*AND B*/ ->        and8(state.b());
            case 0xA1/*AND C*/ ->        and8(state.c());
            case 0xA2/*AND D*/ ->        and8(state.d());
            case 0xA3/*AND E*/ ->        and8(state.e());
            case 0xA4/*AND H*/ ->        and8(state.h());
            case 0xA5/*AND L*/ ->        and8(state.l());
            case 0xA6/*AND (HL)*/ ->     and8(bus.readMemory(state.hl()));
            case 0xA7/*AND A*/ ->        and8(state.a());
            case 0xA8/*XOR B*/ ->        xor8(state.b());
            case 0xA9/*XOR C*/ ->        xor8(state.c());
            case 0xAA/*XOR D*/ ->        xor8(state.d());
            case 0xAB/*XOR E*/ ->        xor8(state.e());
            case 0xAC/*XOR H*/ ->        xor8(state.h());
            case 0xAD/*XOR L*/ ->        xor8(state.l());
            case 0xAE/*XOR (HL)*/ ->     xor8(bus.readMemory(state.hl()));
            case 0xAF/*XOR A*/ ->        xor8(state.a());
            case 0xB0/*OR B*/ ->         or8(state.b());
            case 0xB1/*OR C*/ ->         or8(state.c());
            case 0xB2/*OR D*/ ->         or8(state.d());
            case 0xB3/*OR E*/ ->         or8(state.e());
            case 0xB4/*OR H*/ ->         or8(state.h());
            case 0xB5/*OR L*/ ->         or8(state.l());
            case 0xB6/*OR (HL)*/ ->      or8(bus.readMemory(state.hl()));
            case 0xB7/*OR A*/ ->         or8(state.a());
            case 0xB8/*CP B*/ ->         cp8(state.b());
            case 0xB9/*CP C*/ ->         cp8(state.c());
            case 0xBA/*CP D*/ ->         cp8(state.d());
            case 0xBB/*CP E*/ ->         cp8(state.e());
            case 0xBC/*CP H*/ ->         cp8(state.h());
            case 0xBD/*CP L*/ ->         cp8(state.l());
            case 0xBE/*CP (HL)*/ ->      cp8(bus.readMemory(state.hl()));
            case 0xBF/*CP A*/ ->         cp8(state.a());
            case 0xC0/*RET NZ*/ ->       ret1(!state.zf());
            case 0xC1/*POP BC*/ ->       {}
            case 0xC2/*JP NZ,imm16*/ ->  {}
            case 0xC3/*JP imm16*/ ->     {}
            case 0xC4/*CALL NZ,imm16*/ -> {}
            case 0xC5/*PUSH BC*/ ->      {}
            case 0xC6/*ADD A,imm8*/ ->   {}
            case 0xC7/*RST 00H*/ ->      {}
            case 0xC8/*RET Z*/ ->        ret1(state.zf());
            case 0xC9/*RET*/ ->          {}
            case 0xCA/*JP Z,imm16*/ ->   {}
            case 0xCB/*prefix_cb*/ ->    {}
            case 0xCC/*CALL Z,imm16*/ -> {}
            case 0xCD/*CALL imm16*/ ->   {}
            case 0xCE/*ADC A,imm8*/ ->   {}
            case 0xCF/*RST 08H*/ ->      {}
            case 0xD0/*RET NC*/ ->       ret1(!state.cf());
            case 0xD1/*POP DE*/ ->       {}
            case 0xD2/*JP NC,imm16*/ ->  {}
            case 0xD3/*OUT (imm8),A*/ -> {}
            case 0xD4/*CALL NC,imm16*/ -> {}
            case 0xD5/*PUSH DE*/ ->      {}
            case 0xD6/*SUB imm8*/ ->     {}
            case 0xD7/*RST 10H*/ ->      {}
            case 0xD8/*RET C*/ ->        ret1(state.cf());
            case 0xD9/*EXX*/ ->          {}
            case 0xDA/*JP C,imm16*/ ->   {}
            case 0xDB/*IN A,(imm8)*/ ->  {}
            case 0xDC/*CALL C,imm16*/ -> {}
            case 0xDD/*prefix_dd*/ ->    {}
            case 0xDE/*SBC A,imm8*/ ->   {}
            case 0xDF/*RST 18H*/ ->      {}
            case 0xE0/*RET PO*/ ->       {}
            case 0xE1/*POP HL*/ ->       {}
            case 0xE2/*JP PO,imm16*/ ->  {}
            case 0xE3/*EX (SP),HL*/ ->   {}
            case 0xE4/*CALL PO,imm16*/ -> {}
            case 0xE5/*PUSH HL*/ ->      {}
            case 0xE6/*AND imm8*/ ->     {}
            case 0xE7/*RST 20H*/ ->      {}
            case 0xE8/*RET PE*/ ->       {}
            case 0xE9/*JP (HL)*/ ->      {}
            case 0xEA/*JP PE,imm16*/ ->  {}
            case 0xEB/*EX DE,HL*/ ->     {}
            case 0xEC/*CALL PE,imm16*/ -> {}
            case 0xED/*prefix_ed*/ ->    {}
            case 0xEE/*XOR imm8*/ ->     {}
            case 0xEF/*RST 28H*/ ->      {}
            case 0xF0/*RET P*/ ->        {}
            case 0xF1/*POP AF*/ ->       {}
            case 0xF2/*JP P,imm16*/ ->   {}
            case 0xF3/*DI*/ ->           {}
            case 0xF4/*CALL P,imm16*/ -> {}
            case 0xF5/*PUSH AF*/ ->      {}
            case 0xF6/*OR imm8*/ ->      {}
            case 0xF7/*RST 30H*/ ->      {}
            case 0xF8/*RET M*/ ->        {}
            case 0xF9/*LD SP,HL*/ ->     {}
            case 0xFA/*JP M,imm16*/ ->   {}
            case 0xFB/*EI*/ ->           {}
            case 0xFC/*CALL M,imm16*/ -> {}
            case 0xFD/*prefix_fd*/ ->    {}
            case 0xFE/*CP imm8*/ ->      {}
            case 0xFF/*RST 38H*/ ->      {}
        }
    }

    private int inc16(int in) {
        // TODO set flags
        return in + 1;
    }
    private int inc8(int in) {
        // TODO set flags
        return in + 1;
    }
    private int dec8(int in) {
        // TODO set flags
        return in - 1;
    }
    private int add16(int in) {
        // TODO set flags
        return state.hl() + in;
    }
    private int dec16(int in) {
        // TODO set flags
        return in - 1;
    }
    private void rlca() {
        // TODO
    }
    private void exAfAf_() {
        // TODO
    }
    private void rrca() {
        // TODO
    }
    private void rla() {
        // TODO
    }
    private void rra() {
        // TODO
    }
    private void daa() {
        // TODO
    }
    private void cpl() {
        // TODO
    }
    private void scf() {
        // TODO
    }
    private void ccf() {
        // TODO
    }
    private void add8(int in) {
        // TODO set flags
        state.a(state.a() + in);
    }
    private void adc8(int in) {
        // TODO set flags
        state.a(state.a() + in + (state.cf() ? 1 : 0));
    }
    private void sub8(int in) {
        // TODO set flags
        state.a(state.a() - in);
    }
    private void sbc8(int in) {
        // TODO set flags
        state.a(state.a() - in - (state.cf() ? 1 : 0));
    }
    private void and8(int in) {
        // TODO set flags
        state.a(state.a() & in);
    }
    private void xor8(int in) {
        // TODO set flags
        state.a(state.a() ^ in);
    }
    private void or8(int in) {
        // TODO set flags
        state.a(state.a() | in);
    }
    private void cp8(int in) {
        // TODO set flags
        int diff = state.a() - in;
        state.zf(diff == 0);
    }
    private void ret1(boolean cond) {
        if (cond) {
            state.pc(bus.readWord(state.spInc2()));
        }
    }
}
