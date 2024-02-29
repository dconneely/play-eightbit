package com.davidconneely.eightbit.z80;

import com.davidconneely.eightbit.IBus;

final class Core {
    private final IBus bus;
    private final State state;

    public Core(final IBus bus) {
        this.bus = bus;
        this.state = new State();
    }

    void step() {
        if (state.halted()) { // execute an effective NOP
            return;
        }
        decode_0x00uFF(bus.readInstruction(state.pcInc1()));
    }

    private void decode_0x00uFF(int opCode) {
        int x = (opCode & 0xC0); // values 0x00,40,80,C0
        switch (x) {
            case 0x00/*0x00-3F*/ -> decode_0x00u3F(opCode);
            case 0x40/*0x40-7F*/ -> decode_0x40u7F(opCode);
            case 0x80/*0x80-BF*/ -> decode_0x80uBF(opCode);
            case 0xC0/*0xC0-FF*/ -> decode_0xC0uFF(opCode);
        }
    }

    // 0xCB-prefixed opCodes (bit instructions)
    private void decode_0xCB_00uFF(int opCode) {
        int x = opCode & 0xC0;
        switch (x) {
            case 0x00/*0x00-3F*/ -> decode_0xCB_00u3F(opCode);
            case 0x40/*0x40-7F*/ -> decode_0xCB_40u7F(opCode);
            case 0x80/*0x80-BF*/ -> decode_0xCB_80uBF(opCode);
            case 0xC0/*0xC0-FF*/ -> decode_0xCB_C0uFF(opCode);
        }
    }

    // 0xDD-prefixed opCodes (IX instructions)
    private void decode_0xDD_00uFF(int opCode) {
        int x = opCode & 0xC0;
        switch (x) {
            case 0x00/*0x00-3F*/ -> decode_0xDD_00u3F(opCode);
            case 0x40/*0x40-7F*/ -> decode_0xDD_40u7F(opCode);
            case 0x80/*0x80-BF*/ -> decode_0xDD_80uBF(opCode);
            case 0xC0/*0xC0-FF*/ -> decode_0xDD_C0uFF(opCode);
        }
    }

    // 0xED-prefixed opCodes
    private void decode_0xED_00uFF(int opCode) {
        int x = opCode & 0xC0;
        switch (x) {
            case 0x00/*0x00-3F*/ -> decode_0xED_00u3F(opCode);
            case 0x40/*0x40-7F*/ -> decode_0xED_40u7F(opCode);
            case 0x80/*0x80-BF*/ -> decode_0xED_80uBF(opCode);
            case 0xC0/*0xC0-FF*/ -> decode_0xED_C0uFF(opCode);
        }
    }

    // 0xFD-prefixed opCodes (IY instructions)
    private void decode_0xFD_00uFF(int opCode) {
        int x = opCode & 0xC0;
        switch (x) {
            case 0x00/*0x00-3F*/ -> decode_0xFD_00u3F(opCode);
            case 0x40/*0x40-7F*/ -> decode_0xFD_40u7F(opCode);
            case 0x80/*0x80-BF*/ -> decode_0xFD_80uBF(opCode);
            case 0xC0/*0xC0-FF*/ -> decode_0xFD_C0uFF(opCode);
        }
    }

    private void decode_0x00u3F(int opCode) {
        switch (opCode) {
            case 0x00/*NOP*/ -> {/*do nothing*/} // ZUM(p.172)
            case 0x01/*LD BC,nn*/ -> state.bc(bus.readWord(state.pcInc2())); // ZUM(p.102)
            case 0x02/*LD (BC),A*/ -> bus.writeMemory(state.bc(), state.a()); // ZUM(p.95)
            case 0x03/*INC BC*/ -> state.bc(inc16(state.bc())); // ZUM(p.184)
            case 0x04/*INC B*/ -> state.b(inc8(state.b())); // ZUM(p.160)
            case 0x05/*DEC B*/ -> state.b(dec8(state.b())); // ZUM(pp.164-165)
            case 0x06/*LD B,n*/ -> state.b(bus.readMemory(state.pcInc1())); // ZUM(p.82)
            case 0x07/*RLCA*/ -> rlca(); // ZUM(p.190)
            case 0x08/*EX AF,AF'*/ -> exAfAf_(); // ZUM(p.123)
            case 0x09/*ADD HL,BC*/ -> state.hl(add16(state.bc())); // ZUM(p.179)
            case 0x0A/*LD A,(BC)*/ -> state.a(bus.readMemory(state.bc())); // ZUM(p.92)
            case 0x0B/*DEC BC*/ -> state.bc(dec16(state.bc())); // ZUM(p.187)
            case 0x0C/*INC C*/ -> state.c(inc8(state.c())); // ZUM(p.160)
            case 0x0D/*DEC C*/ -> state.c(dec8(state.c())); // ZUM(pp.164-165)
            case 0x0E/*LD C,n*/ -> state.c(bus.readMemory(state.pcInc1())); // ZUM(p.82)
            case 0x0F/*RRCA*/ -> rrca(); // ZUM(p.192)
            case 0x10/*DJNZ n*/ -> djnz(); // ZUM(pp.253-254)
            case 0x11/*LD DE,nn*/ -> state.de(bus.readWord(state.pcInc2())); // ZUM(p.102)
            case 0x12/*LD (DE),A*/ -> bus.writeMemory(state.de(), state.a()); // ZUM(p.96)
            case 0x13/*INC DE*/ -> state.de(inc16(state.de())); // ZUM(p.184)
            case 0x14/*INC D*/ -> state.d(inc8(state.d())); // ZUM(p.160)
            case 0x15/*DEC D*/ -> state.d(dec8(state.d())); // ZUM(pp.164-165)
            case 0x16/*LD D,n*/ -> state.d(bus.readMemory(state.pcInc1())); // ZUM(p.82)
            case 0x17/*RLA*/ -> rla(); // ZUM(p.191)
            case 0x18/*JR n*/ -> jr0(); // ZUM(p.241)
            case 0x19/*ADD HL,DE*/ -> state.hl(add16(state.de())); // ZUM(p.179)
            case 0x1A/*LD A,(DE)*/ -> state.a(bus.readMemory(state.de())); // ZUM(p.93)
            case 0x1B/*DEC DE*/ -> state.de(dec16(state.de())); // ZUM(p.187)
            case 0x1C/*INC E*/ -> state.e(inc8(state.e())); // ZUM(p.160)
            case 0x1D/*DEC E*/ -> state.e(dec8(state.e())); // ZUM(pp.164-165)
            case 0x1E/*LD E,n*/ -> state.e(bus.readMemory(state.pcInc1())); // ZUM(p.82)
            case 0x1F/*RRA*/ -> rra(); // ZUM(p.193)
            case 0x20/*JR NZ,n*/ -> jr1(!state.zf()); // ZUM(pp.248-249)
            case 0x21/*LD HL,nn*/ -> state.hl(bus.readWord(state.pcInc2())); // ZUM(p.102)
            case 0x22/*LD (nn),HL*/ -> bus.writeWord(bus.readWord(state.pcInc2()), state.hl()); // ZUM(p.109)
            case 0x23/*INC HL*/ -> state.hl(inc16(state.hl())); // ZUM(p.184)
            case 0x24/*INC H*/ -> state.h(inc8(state.h())); // ZUM(p.160)
            case 0x25/*DEC H*/ -> state.h(dec8(state.h())); // ZUM(pp.164-165)
            case 0x26/*LD H,n*/ -> state.h(bus.readMemory(state.pcInc1())); // ZUM(p.82)
            case 0x27/*DAA*/ -> daa(); // ZUM(p.166)
            case 0x28/*JR Z,n*/ -> jr1(state.zf()); // ZUM(pp.246-247)
            case 0x29/*ADD HL,HL*/ -> state.hl(add16(state.hl())); // ZUM(p.179)
            case 0x2A/*LD HL,(nn)*/ -> state.hl(bus.readWord(bus.readWord(state.pcInc2()))); // ZUM(p.105)
            case 0x2B/*DEC HL*/ -> state.hl(dec16(state.hl())); // ZUM(p.187)
            case 0x2C/*INC L*/ -> state.l(inc8(state.l())); // ZUM(p.160)
            case 0x2D/*DEC L*/ -> state.l(dec8(state.l())); // ZUM(pp.164-165)
            case 0x2E/*LD L,n*/ -> state.l(bus.readMemory(state.pcInc1())); // ZUM(p.82)
            case 0x2F/*CPL*/ -> cpl(); // ZUM(p.168)
            case 0x30/*JR NC,n*/ -> jr1(!state.cf()); // ZUM(pp.244-245)
            case 0x31/*LD SP,nn*/ -> state.sp(bus.readWord(state.pcInc2())); // ZUM(p.102)
            case 0x32/*LD (nn),A*/ -> bus.writeMemory(bus.readWord(state.pcInc2()), state.a()); // ZUM(p.97)
            case 0x33/*INC SP*/ -> state.sp(inc16(state.sp())); // ZUM(p.184)
            case 0x34/*INC (HL)*/ -> bus.writeMemory(state.hl(), inc8(bus.readMemory(state.hl()))); // ZUM(p.161)
            case 0x35/*DEC (HL)*/ -> bus.writeMemory(state.hl(), dec8(bus.readMemory(state.hl()))); // ZUM(pp.164-165)
            case 0x36/*LD (HL),n*/ -> bus.writeMemory(state.hl(), bus.readMemory(state.pcInc1())); // ZUM(p.86)
            case 0x37/*SCF*/ -> scf(); // ZUM(p.171)
            case 0x38/*JR C,n*/ -> jr1(state.cf()); // ZUM(pp.242-243)
            case 0x39/*ADD HL,SP*/ -> state.hl(add16(state.sp())); // ZUM(p.179)
            case 0x3A/*LD A,(nn)*/ -> state.a(bus.readMemory(bus.readWord(state.pcInc2()))); // ZUM(p.94)
            case 0x3B/*DEC SP*/ -> state.sp(dec16(state.sp())); // ZUM(p.187)
            case 0x3C/*INC A*/ -> state.a(inc8(state.a())); // ZUM(p.160)
            case 0x3D/*DEC A*/ -> state.a(dec8(state.a())); // ZUM(pp.164-165)
            case 0x3E/*LD A,n*/ -> state.a(bus.readMemory(state.pcInc1())); // ZUM(p.82)
            case 0x3F/*CCF*/ -> ccf(); // ZUM(p.170)
        }
    }

    private void decode_0x40u7F(int opCode) {
        switch (opCode) {
            case 0x40/*LD B,B*/ -> {/*state.b(state.b()):*/} // ZUM(p.81)
            case 0x41/*LD B,C*/ -> state.b(state.c()); // ZUM(p.81)
            case 0x42/*LD B,D*/ -> state.b(state.d()); // ZUM(p.81)
            case 0x43/*LD B,E*/ -> state.b(state.e()); // ZUM(p.81)
            case 0x44/*LD B,H*/ -> state.b(state.h()); // ZUM(p.81)
            case 0x45/*LD B,L*/ -> state.b(state.l()); // ZUM(p.81)
            case 0x46/*LD B,(HL)*/ -> state.b(bus.readMemory(state.hl())); // ZUM(p.83)
            case 0x47/*LD B,A*/ -> state.b(state.a()); // ZUM(p.81)
            case 0x48/*LD C,B*/ -> state.c(state.b()); // ZUM(p.81)
            case 0x49/*LD C,C*/ -> {/*state.c(state.c());*/} // ZUM(p.81)
            case 0x4A/*LD C,D*/ -> state.c(state.d()); // ZUM(p.81)
            case 0x4B/*LD C,E*/ -> state.c(state.e()); // ZUM(p.81)
            case 0x4C/*LD C,H*/ -> state.c(state.h()); // ZUM(p.81)
            case 0x4D/*LD C,L*/ -> state.c(state.l()); // ZUM(p.81)
            case 0x4E/*LD C,(HL)*/ -> state.c(bus.readMemory(state.hl())); // ZUM(p.83)
            case 0x4F/*LD C,A*/ -> state.c(state.a()); // ZUM(p.81)
            case 0x50/*LD D,B*/ -> state.d(state.b()); // ZUM(p.81)
            case 0x51/*LD D,C*/ -> state.d(state.c()); // ZUM(p.81)
            case 0x52/*LD D,D*/ -> {/*state.d(state.d());*/} // ZUM(p.81)
            case 0x53/*LD D,E*/ -> state.d(state.e()); // ZUM(p.81)
            case 0x54/*LD D,H*/ -> state.d(state.h()); // ZUM(p.81)
            case 0x55/*LD D,L*/ -> state.d(state.l()); // ZUM(p.81)
            case 0x56/*LD D,(HL)*/ -> state.d(bus.readMemory(state.hl())); // ZUM(p.83)
            case 0x57/*LD D,A*/ -> state.d(state.a()); // ZUM(p.81)
            case 0x58/*LD E,B*/ -> state.e(state.b()); // ZUM(p.81)
            case 0x59/*LD E,C*/ -> state.e(state.c()); // ZUM(p.81)
            case 0x5A/*LD E,D*/ -> state.e(state.d()); // ZUM(p.81)
            case 0x5B/*LD E,E*/ -> {/*state.e(state.e());*/} // ZUM(p.81)
            case 0x5C/*LD E,H*/ -> state.e(state.h()); // ZUM(p.81)
            case 0x5D/*LD E,L*/ -> state.e(state.l()); // ZUM(p.81)
            case 0x5E/*LD E,(HL)*/ -> state.e(bus.readMemory(state.hl())); // ZUM(p.83)
            case 0x5F/*LD E,A*/ -> state.e(state.a()); // ZUM(p.81)
            case 0x60/*LD H,B*/ -> state.h(state.b()); // ZUM(p.81)
            case 0x61/*LD H,C*/ -> state.h(state.c()); // ZUM(p.81)
            case 0x62/*LD H,D*/ -> state.h(state.d()); // ZUM(p.81)
            case 0x63/*LD H,E*/ -> state.h(state.e()); // ZUM(p.81)
            case 0x64/*LD H,H*/ -> {/*state.h(state.h());*/} // ZUM(p.81)
            case 0x65/*LD H,L*/ -> state.h(state.l()); // ZUM(p.81)
            case 0x66/*LD H,(HL)*/ -> state.h(bus.readMemory(state.hl())); // ZUM(p.83)
            case 0x67/*LD H,A*/ -> state.h(state.a()); // ZUM(p.81)
            case 0x68/*LD L,B*/ -> state.l(state.b()); // ZUM(p.81)
            case 0x69/*LD L,C*/ -> state.l(state.c()); // ZUM(p.81)
            case 0x6A/*LD L,D*/ -> state.l(state.d()); // ZUM(p.81)
            case 0x6B/*LD L,E*/ -> state.l(state.e()); // ZUM(p.81)
            case 0x6C/*LD L,H*/ -> state.l(state.h()); // ZUM(p.81)
            case 0x6D/*LD L,L*/ -> {/*state.l(state.l());*/} // ZUM(p.81)
            case 0x6E/*LD L,(HL)*/ -> state.l(bus.readMemory(state.hl())); // ZUM(p.83)
            case 0x6F/*LD L,A*/ -> state.l(state.a()); // ZUM(p.81)
            case 0x70/*LD (HL),B*/ -> bus.writeMemory(state.hl(), state.b()); // ZUM(p.86)
            case 0x71/*LD (HL),C*/ -> bus.writeMemory(state.hl(), state.c()); // ZUM(p.86)
            case 0x72/*LD (HL),D*/ -> bus.writeMemory(state.hl(), state.d()); // ZUM(p.86)
            case 0x73/*LD (HL),E*/ -> bus.writeMemory(state.hl(), state.e()); // ZUM(p.86)
            case 0x74/*LD (HL),H*/ -> bus.writeMemory(state.hl(), state.h()); // ZUM(p.86)
            case 0x75/*LD (HL),L*/ -> bus.writeMemory(state.hl(), state.l()); // ZUM(p.86)
            case 0x76/*HALT*/ -> state.halted(true); // ZUM(p.173)
            case 0x77/*LD (HL),A*/ -> bus.writeMemory(state.hl(), state.a()); // ZUM(p.86)
            case 0x78/*LD A,B*/ -> state.a(state.b()); // ZUM(p.81)
            case 0x79/*LD A,C*/ -> state.a(state.c()); // ZUM(p.81)
            case 0x7A/*LD A,D*/ -> state.a(state.d()); // ZUM(p.81)
            case 0x7B/*LD A,E*/ -> state.a(state.e()); // ZUM(p.81)
            case 0x7C/*LD A,H*/ -> state.a(state.h()); // ZUM(p.81)
            case 0x7D/*LD A,L*/ -> state.a(state.l()); // ZUM(p.81)
            case 0x7E/*LD A,(HL)*/ -> state.a(bus.readMemory(state.hl())); // ZUM(p.83)
            case 0x7F/*LD A,A*/ -> {/*state.a(state.a());*/} // ZUM(p.81)
        }
    }

    private void decode_0x80uBF(int opCode) {
        switch (opCode) {
            case 0x80/*ADD A,B*/ -> add8(state.b()); // ZUM(pp.140-141)
            case 0x81/*ADD A,C*/ -> add8(state.c()); // ZUM(pp.140-141)
            case 0x82/*ADD A,D*/ -> add8(state.d()); // ZUM(pp.140-141)
            case 0x83/*ADD A,E*/ -> add8(state.e()); // ZUM(pp.140-141)
            case 0x84/*ADD A,H*/ -> add8(state.h()); // ZUM(pp.140-141)
            case 0x85/*ADD A,L*/ -> add8(state.l()); // ZUM(pp.140-141)
            case 0x86/*ADD A,(HL)*/ -> add8(bus.readMemory(state.hl())); // ZUM(p.143)
            case 0x87/*ADD A,A*/ -> add8(state.a()); // ZUM(pp.140-141)
            case 0x88/*ADC A,B*/ -> adc8(state.b()); // ZUM(pp.146-147)
            case 0x89/*ADC A,C*/ -> adc8(state.c()); // ZUM(pp.146-147)
            case 0x8A/*ADC A,D*/ -> adc8(state.d()); // ZUM(pp.146-147)
            case 0x8B/*ADC A,E*/ -> adc8(state.e()); // ZUM(pp.146-147)
            case 0x8C/*ADC A,H*/ -> adc8(state.h()); // ZUM(pp.146-147)
            case 0x8D/*ADC A,L*/ -> adc8(state.l()); // ZUM(pp.146-147)
            case 0x8E/*ADC A,(HL)*/ -> adc8(bus.readMemory(state.hl())); // ZUM(pp.146-147)
            case 0x8F/*ADC A,A*/ -> adc8(state.a()); // ZUM(pp.146-147)
            case 0x90/*SUB B*/ -> sub8(state.b()); // ZUM(pp.148-149)
            case 0x91/*SUB C*/ -> sub8(state.c()); // ZUM(pp.148-149)
            case 0x92/*SUB D*/ -> sub8(state.d()); // ZUM(pp.148-149)
            case 0x93/*SUB E*/ -> sub8(state.e()); // ZUM(pp.148-149)
            case 0x94/*SUB H*/ -> sub8(state.h()); // ZUM(pp.148-149)
            case 0x95/*SUB L*/ -> sub8(state.l()); // ZUM(pp.148-149)
            case 0x96/*SUB (HL)*/ -> sub8(bus.readMemory(state.hl())); // ZUM(pp.148-149)
            case 0x97/*SUB A*/ -> sub8(state.a()); // ZUM(pp.148-149)
            case 0x98/*SBC A,B*/ -> sbc8(state.b()); // ZUM(pp.150-151)
            case 0x99/*SBC A,C*/ -> sbc8(state.c()); // ZUM(pp.150-151)
            case 0x9A/*SBC A,D*/ -> sbc8(state.d()); // ZUM(pp.150-151)
            case 0x9B/*SBC A,E*/ -> sbc8(state.e()); // ZUM(pp.150-151)
            case 0x9C/*SBC A,H*/ -> sbc8(state.h()); // ZUM(pp.150-151)
            case 0x9D/*SBC A,L*/ -> sbc8(state.l()); // ZUM(pp.150-151)
            case 0x9E/*SBC A,(HL)*/ -> sbc8(bus.readMemory(state.hl())); // ZUM(pp.150-151)
            case 0x9F/*SBC A,A*/ -> sbc8(state.a()); // ZUM(pp.150-151)
            case 0xA0/*AND B*/ -> and8(state.b()); // ZUM(pp.152-153)
            case 0xA1/*AND C*/ -> and8(state.c()); // ZUM(pp.152-153)
            case 0xA2/*AND D*/ -> and8(state.d()); // ZUM(pp.152-153)
            case 0xA3/*AND E*/ -> and8(state.e()); // ZUM(pp.152-153)
            case 0xA4/*AND H*/ -> and8(state.h()); // ZUM(pp.152-153)
            case 0xA5/*AND L*/ -> and8(state.l()); // ZUM(pp.152-153)
            case 0xA6/*AND (HL)*/ -> and8(bus.readMemory(state.hl())); // ZUM(pp.152-153)
            case 0xA7/*AND A*/ -> and8(state.a()); // ZUM(pp.152-153)
            case 0xA8/*XOR B*/ -> xor8(state.b()); // ZUM(pp.156-157)
            case 0xA9/*XOR C*/ -> xor8(state.c()); // ZUM(pp.156-157)
            case 0xAA/*XOR D*/ -> xor8(state.d()); // ZUM(pp.156-157)
            case 0xAB/*XOR E*/ -> xor8(state.e()); // ZUM(pp.156-157)
            case 0xAC/*XOR H*/ -> xor8(state.h()); // ZUM(pp.156-157)
            case 0xAD/*XOR L*/ -> xor8(state.l()); // ZUM(pp.156-157)
            case 0xAE/*XOR (HL)*/ -> xor8(bus.readMemory(state.hl())); // ZUM(pp.156-157)
            case 0xAF/*XOR A*/ -> xor8(state.a()); // ZUM(pp.156-157)
            case 0xB0/*OR B*/ -> or8(state.b()); // ZUM(pp.154-155)
            case 0xB1/*OR C*/ -> or8(state.c()); // ZUM(pp.154-155)
            case 0xB2/*OR D*/ -> or8(state.d()); // ZUM(pp.154-155)
            case 0xB3/*OR E*/ -> or8(state.e()); // ZUM(pp.154-155)
            case 0xB4/*OR H*/ -> or8(state.h()); // ZUM(pp.154-155)
            case 0xB5/*OR L*/ -> or8(state.l()); // ZUM(pp.154-155)
            case 0xB6/*OR (HL)*/ -> or8(bus.readMemory(state.hl())); // ZUM(pp.154-155)
            case 0xB7/*OR A*/ -> or8(state.a()); // ZUM(pp.154-155)
            case 0xB8/*CP B*/ -> cp8(state.b()); // ZUM(pp.158-159)
            case 0xB9/*CP C*/ -> cp8(state.c()); // ZUM(pp.158-159)
            case 0xBA/*CP D*/ -> cp8(state.d()); // ZUM(pp.158-159)
            case 0xBB/*CP E*/ -> cp8(state.e()); // ZUM(pp.158-159)
            case 0xBC/*CP H*/ -> cp8(state.h()); // ZUM(pp.158-159)
            case 0xBD/*CP L*/ -> cp8(state.l()); // ZUM(pp.158-159)
            case 0xBE/*CP (HL)*/ -> cp8(bus.readMemory(state.hl())); // ZUM(pp.158-159)
            case 0xBF/*CP A*/ -> cp8(state.a()); // ZUM(pp.158-159)
        }
    }

    private void decode_0xC0uFF(int opCode) {
        switch (opCode) {
            case 0xC0/*RET NZ*/ -> ret1(!state.zf()); // ZUM(pp.261-262)
            case 0xC1/*POP BC*/ -> state.bc(bus.readWord(state.spInc2())); // ZUM(p.119)
            case 0xC2/*JP NZ,nn*/ -> jp1(!state.zf()); // ZUM(pp.239-240)
            case 0xC3/*JP nn*/ -> jp0(); // ZUM(p.236)
            case 0xC4/*CALL NZ,nn*/ -> call1(!state.zf()); // ZUM(pp.257-259)
            case 0xC5/*PUSH BC*/ -> bus.writeWord(state.spDec2(), state.bc()); // ZUM(p.116)
            case 0xC6/*ADD A,n*/ -> add8(bus.readMemory(state.pcInc1())); // ZUM(p.142)
            case 0xC7/*RST 00H*/ -> rst(0x00); // ZUM(pp.267-268)
            case 0xC8/*RET Z*/ -> ret1(state.zf()); // ZUM(pp.261-262)
            case 0xC9/*RET*/ -> ret0(); // ZUM(p.260)
            case 0xCA/*JP Z,nn*/ -> jp1(state.zf()); // ZUM(pp.239-240)
            case 0xCB/*[0xCB] ...*/ -> decode_0xCB_00uFF(bus.readMemory(state.pcInc1()));
            case 0xCC/*CALL Z,nn*/ -> call1(!state.zf()); // ZUM(pp.257-259)
            case 0xCD/*CALL nn*/ -> call0(); // ZUM(pp.255-256)
            case 0xCE/*ADC A,n*/ -> adc8(bus.readMemory(state.pcInc1())); // ZUM(pp.146-147)
            case 0xCF/*RST 08H*/ -> rst(0x08); // ZUM(pp.267-268)
            case 0xD0/*RET NC*/ -> ret1(!state.cf()); // ZUM(pp.261-262)
            case 0xD1/*POP DE*/ -> state.de(bus.readWord(state.spInc2())); // ZUM(p.119)
            case 0xD2/*JP NC,nn*/ -> jp1(!state.cf()); // ZUM(pp.239-240)
            case 0xD3/*OUT (n),A*/ ->
                    bus.writeIoPort((state.a() << 8) | bus.readMemory(state.pcInc1()), state.a()); // ZUM(p.279)
            case 0xD4/*CALL NC,nn*/ -> call1(!state.cf()); // ZUM(pp.257-259)
            case 0xD5/*PUSH DE*/ -> bus.writeWord(state.spDec2(), state.de()); // ZUM(p.116)
            case 0xD6/*SUB n*/ -> sub8(bus.readMemory(state.pcInc1())); // ZUM(pp.148-149)
            case 0xD7/*RST 10H*/ -> rst(0x10); // ZUM(pp.267-268)
            case 0xD8/*RET C*/ -> ret1(state.cf()); // ZUM(pp.261-262)
            case 0xD9/*EXX*/ -> exx(); // ZUM(p.124)
            case 0xDA/*JP C,nn*/ -> jp1(state.cf()); // ZUM(pp.239-240)
            case 0xDB/*IN A,(n)*/ ->
                    state.a(bus.readIoPort((state.a() << 8) | bus.readMemory(state.pcInc1()))); // ZUM(p.269)
            case 0xDC/*CALL C,nn*/ -> call1(state.cf()); // ZUM(pp.257-259)
            case 0xDD/*[0xDD] ...*/ -> decode_0xDD_00uFF(bus.readMemory(state.pcInc1()));
            case 0xDE/*SBC A,n*/ -> sbc8(bus.readMemory(state.pcInc1())); // ZUM(pp.150-151)
            case 0xDF/*RST 18H*/ -> rst(0x18); // ZUM(pp.267-268)
            case 0xE0/*RET PO*/ -> ret1(!state.pf()); // ZUM(pp.261-262)
            case 0xE1/*POP HL*/ -> state.hl(bus.readWord(state.spInc2())); // ZUM(p.119)
            case 0xE2/*JP PO,nn*/ -> jp1(!state.pf()); // ZUM(pp.239-240)
            case 0xE3/*EX (SP),HL*/ -> exStkHl(); // ZUM(p.125)
            case 0xE4/*CALL PO,nn*/ -> call1(!state.pf()); // ZUM(pp.257-259)
            case 0xE5/*PUSH HL*/ -> bus.writeWord(state.spDec2(), state.hl()); // ZUM(p.116)
            case 0xE6/*AND n*/ -> and8(bus.readMemory(state.pcInc1())); // ZUM(pp.152-153)
            case 0xE7/*RST 20H*/ -> rst(0x20); // ZUM(pp.267-268)
            case 0xE8/*RET PE*/ -> ret1(state.pf()); // ZUM(pp.261-262)
            case 0xE9/*JP (HL)*/ -> state.pc(bus.readWord(state.hl())); // ZUM(p.250)
            case 0xEA/*JP PE,nn*/ -> jp1(state.pf()); // ZUM(pp.239-240)
            case 0xEB/*EX DE,HL*/ -> exDeHl(); // ZUM(p.122)
            case 0xEC/*CALL PE,nn*/ -> call1(state.pf()); // ZUM(pp.257-259)
            case 0xED/*[0xED] ...*/ -> decode_0xED_00uFF(bus.readMemory(state.pcInc1()));
            case 0xEE/*XOR n*/ -> xor8(bus.readMemory(state.pcInc1()));
            case 0xEF/*RST 28H*/ -> rst(0x28); // ZUM(pp.267-268)
            case 0xF0/*RET P*/ -> ret1(!state.sf()); // ZUM(pp.261-262)
            case 0xF1/*POP AF*/ -> state.af(bus.readWord(state.spInc2())); // ZUM(p.119)
            case 0xF2/*JP P,nn*/ -> jp1(!state.sf()); // ZUM(pp.239-240)
            case 0xF3/*DI*/ -> di(); // ZUM(p.174)
            case 0xF4/*CALL P,nn*/ -> call1(!state.sf()); // ZUM(pp.257-259)
            case 0xF5/*PUSH AF*/ -> bus.writeWord(state.spDec2(), state.af()); // ZUM(p.116)
            case 0xF6/*OR n*/ -> or8(bus.readMemory(state.pcInc1()));
            case 0xF7/*RST 30H*/ -> rst(0x30); // ZUM(pp.267-268)
            case 0xF8/*RET M*/ -> ret1(state.sf()); // ZUM(pp.261-262)
            case 0xF9/*LD SP,HL*/ -> state.sp(state.hl()); // ZUM(p.113)
            case 0xFA/*JP M,nn*/ -> jp1(state.sf()); // ZUM(pp.239-240)
            case 0xFB/*EI*/ -> ei(); // ZUM(p.175)
            case 0xFC/*CALL M,nn*/ -> call1(state.sf()); // ZUM(pp.257-259)
            case 0xFD/*[0xFD] ...*/ -> decode_0xFD_00uFF(bus.readMemory(state.pcInc1()));
            case 0xFE/*CP n*/ -> cp8(bus.readMemory(state.pcInc1()));
            case 0xFF/*RST 38H*/ -> rst(0x38); // ZUM(pp.267-268)
        }
    }

    // shift and rotate group
    private void decode_0xCB_00u3F(int opCode) {
        switch (opCode) {
            case 0x00/*RLC B*/ -> state.b(rlc(state.b())); // ZUM(pp.194-195)
            case 0x01/*RLC C*/ -> state.c(rlc(state.c())); // ZUM(pp.194-195)
            case 0x02/*RLC D*/ -> state.d(rlc(state.d())); // ZUM(pp.194-195)
            case 0x03/*RLC E*/ -> state.e(rlc(state.e())); // ZUM(pp.194-195)
            case 0x04/*RLC H*/ -> state.h(rlc(state.h())); // ZUM(pp.194-195)
            case 0x05/*RLC L*/ -> state.l(rlc(state.l())); // ZUM(pp.194-195)
            case 0x06/*RLC (HL)*/ -> bus.writeMemory(state.hl(), rlc(bus.readMemory(state.hl()))); // ZUM(pp.196-197)
            case 0x07/*RLC A*/ -> state.a(rlc(state.a())); // ZUM(pp.194-195)
            case 0x08/*RRC B*/ -> state.b(rrc(state.b())); // ZUM(pp.205-207)
            case 0x09/*RRC C*/ -> state.c(rrc(state.c())); // ZUM(pp.205-207)
            case 0x0A/*RRC D*/ -> state.d(rrc(state.d())); // ZUM(pp.205-207)
            case 0x0B/*RRC E*/ -> state.e(rrc(state.e())); // ZUM(pp.205-207)
            case 0x0C/*RRC H*/ -> state.h(rrc(state.h())); // ZUM(pp.205-207)
            case 0x0D/*RRC L*/ -> state.l(rrc(state.l())); // ZUM(pp.205-207)
            case 0x0E/*RRC (HL)*/ -> bus.writeMemory(state.hl(), rrc(bus.readMemory(state.hl()))); // ZUM(pp.205-207)
            case 0x0F/*RRC A*/ -> state.a(rrc(state.a())); // ZUM(pp.205-207)
            case 0x10/*RL B*/ -> state.b(rl(state.b())); // ZUM(pp.202-204)
            case 0x11/*RL C*/ -> state.c(rl(state.c())); // ZUM(pp.202-204)
            case 0x12/*RL D*/ -> state.d(rl(state.d())); // ZUM(pp.202-204)
            case 0x13/*RL E*/ -> state.e(rl(state.e())); // ZUM(pp.202-204)
            case 0x14/*RL H*/ -> state.h(rl(state.h())); // ZUM(pp.202-204)
            case 0x15/*RL L*/ -> state.l(rl(state.l())); // ZUM(pp.202-204)
            case 0x16/*RL (HL)*/ -> bus.writeMemory(state.hl(), rl(bus.readMemory(state.hl()))); // ZUM(pp.202-204)
            case 0x17/*RL A*/ -> state.a(rl(state.a())); // ZUM(pp.202-204)
            case 0x18/*RR B*/ -> state.b(rr(state.b())); // ZUM(pp.208-210)
            case 0x19/*RR C*/ -> state.c(rr(state.c())); // ZUM(pp.208-210)
            case 0x1A/*RR D*/ -> state.d(rr(state.d())); // ZUM(pp.208-210)
            case 0x1B/*RR E*/ -> state.e(rr(state.e())); // ZUM(pp.208-210)
            case 0x1C/*RR H*/ -> state.h(rr(state.h())); // ZUM(pp.208-210)
            case 0x1D/*RR L*/ -> state.l(rr(state.l())); // ZUM(pp.208-210)
            case 0x1E/*RR (HL)*/ -> bus.writeMemory(state.hl(), rr(bus.readMemory(state.hl()))); // ZUM(pp.208-210)
            case 0x1F/*RR A*/ -> state.a(rr(state.a())); // ZUM(pp.208-210)
            case 0x20/*SLA B*/ -> state.b(sla(state.b())); // ZUM(pp.211-213)
            case 0x21/*SLA C*/ -> state.c(sla(state.c())); // ZUM(pp.211-213)
            case 0x22/*SLA D*/ -> state.d(sla(state.d())); // ZUM(pp.211-213)
            case 0x23/*SLA E*/ -> state.e(sla(state.e())); // ZUM(pp.211-213)
            case 0x24/*SLA H*/ -> state.h(sla(state.h())); // ZUM(pp.211-213)
            case 0x25/*SLA L*/ -> state.l(sla(state.l())); // ZUM(pp.211-213)
            case 0x26/*SLA (HL)*/ -> bus.writeMemory(state.hl(), sla(bus.readMemory(state.hl()))); // ZUM(pp.211-213)
            case 0x27/*SLA A*/ -> state.a(sla(state.a())); // ZUM(pp.211-213)
            case 0x28/*SRA B*/ -> state.b(sra(state.b())); // ZUM(pp.214-216)
            case 0x29/*SRA C*/ -> state.c(sra(state.c())); // ZUM(pp.214-216)
            case 0x2A/*SRA D*/ -> state.d(sra(state.d())); // ZUM(pp.214-216)
            case 0x2B/*SRA E*/ -> state.e(sra(state.e())); // ZUM(pp.214-216)
            case 0x2C/*SRA H*/ -> state.h(sra(state.h())); // ZUM(pp.214-216)
            case 0x2D/*SRA L*/ -> state.l(sra(state.l())); // ZUM(pp.214-216)
            case 0x2E/*SRA (HL)*/ -> bus.writeMemory(state.hl(), sra(bus.readMemory(state.hl()))); // ZUM(pp.214-216)
            case 0x2F/*SRA A*/ -> state.a(sra(state.a())); // ZUM(pp.214-216)
            case 0x30/*~SLL B*/ -> state.b(sll(state.b())); // undocumented
            case 0x31/*~SLL C*/ -> state.c(sll(state.c())); // undocumented
            case 0x32/*~SLL D*/ -> state.d(sll(state.d())); // undocumented
            case 0x33/*~SLL E*/ -> state.e(sll(state.e())); // undocumented
            case 0x34/*~SLL H*/ -> state.h(sll(state.h())); // undocumented
            case 0x35/*~SLL L*/ -> state.l(sll(state.l())); // undocumented
            case 0x36/*~SLL (HL)*/ -> bus.writeMemory(state.hl(), sll(bus.readMemory(state.hl()))); // undocumented
            case 0x37/*~SLL A*/ -> state.a(sll(state.a())); // undocumented
            case 0x38/*SRL B*/ -> state.b(srl(state.b())); // ZUM(pp.217-219)
            case 0x39/*SRL C*/ -> state.c(srl(state.c())); // ZUM(pp.217-219)
            case 0x3A/*SRL D*/ -> state.d(srl(state.d())); // ZUM(pp.217-219)
            case 0x3B/*SRL E*/ -> state.e(srl(state.e())); // ZUM(pp.217-219)
            case 0x3C/*SRL H*/ -> state.h(srl(state.h())); // ZUM(pp.217-219)
            case 0x3D/*SRL L*/ -> state.l(srl(state.l())); // ZUM(pp.217-219)
            case 0x3E/*SRL (HL)*/ -> bus.writeMemory(state.hl(), srl(bus.readMemory(state.hl()))); // ZUM(pp.217-219)
            case 0x3F/*SRL A*/ -> state.a(srl(state.a())); // ZUM(pp.217-219)
        }
    }

    private void decode_0xCB_40u7F(int opCode) {
        int y = (opCode & 0x38) >>> 3;
        int z = (opCode & 0x07);
        switch (z) {
            case 0x00/*BIT b,B*/ -> bit(y, state.b()); // ZUM(pp.224-225)
            case 0x01/*BIT b,C*/ -> bit(y, state.c()); // ZUM(pp.224-225)
            case 0x02/*BIT b,D*/ -> bit(y, state.d()); // ZUM(pp.224-225)
            case 0x03/*BIT b,E*/ -> bit(y, state.e()); // ZUM(pp.224-225)
            case 0x04/*BIT b,H*/ -> bit(y, state.h()); // ZUM(pp.224-225)
            case 0x05/*BIT b,L*/ -> bit(y, state.l()); // ZUM(pp.224-225)
            case 0x06/*BIT b,(HL)*/ -> bit(y, bus.readMemory(state.hl())); // ZUM(pp.226-227)
            case 0x07/*BIT b,A*/ -> bit(y, state.c()); // ZUM(pp.224-225)
        }
    }

    private void decode_0xCB_80uBF(int opCode) {
        int y = (opCode & 0x38) >>> 3;
        int z = (opCode & 0x07);
        switch (z) {
            case 0x00/*RES b,B*/ -> state.b(res(y, state.b())); // ZUM(pp.236-237)
            case 0x01/*RES b,C*/ -> state.c(res(y, state.c())); // ZUM(pp.236-237)
            case 0x02/*RES b,D*/ -> state.d(res(y, state.d())); // ZUM(pp.236-237)
            case 0x03/*RES b,E*/ -> state.e(res(y, state.e())); // ZUM(pp.236-237)
            case 0x04/*RES b,H*/ -> state.h(res(y, state.h())); // ZUM(pp.236-237)
            case 0x05/*RES b,L*/ -> state.l(res(y, state.l())); // ZUM(pp.236-237)
            case 0x06/*RES b,(HL)*/ -> bus.writeMemory(state.hl(), res(y, bus.readMemory(state.hl()))); // ZUM(pp.236-237)
            case 0x07/*RES b,A*/ -> state.a(res(y, state.c())); // ZUM(pp.236-237)
        }
    }

    private void decode_0xCB_C0uFF(int opCode) {
        int y = (opCode & 0x38) >>> 3;
        int z = (opCode & 0x07);
        switch (z) {
            case 0x00/*SET b,B*/ -> state.b(set(y, state.b())); // ZUM(p.232)
            case 0x01/*SET b,C*/ -> state.c(set(y, state.c())); // ZUM(p.232)
            case 0x02/*SET b,D*/ -> state.d(set(y, state.d())); // ZUM(p.232)
            case 0x03/*SET b,E*/ -> state.e(set(y, state.e())); // ZUM(p.232)
            case 0x04/*SET b,H*/ -> state.h(set(y, state.h())); // ZUM(p.232)
            case 0x05/*SET b,L*/ -> state.l(set(y, state.l())); // ZUM(p.232)
            case 0x06/*SET b,(HL)*/ -> bus.writeMemory(state.hl(), set(y, bus.readMemory(state.hl()))); // ZUM(p.233)
            case 0x07/*SET b,A*/ -> state.a(set(y, state.c())); // ZUM(p.232)
        }
    }

    private void decode_0xDD_00u3F(int opCode) {
        switch (opCode) {
            // TODO implementation
        }
    }

    private void decode_0xDD_40u7F(int opCode) {
        switch (opCode) {
            // TODO implementation
        }
    }

    private void decode_0xDD_80uBF(int opCode) {
        switch (opCode) {
            // TODO implementation
        }
    }

    private void decode_0xDD_C0uFF(int opCode) {
        switch (opCode) {
            // TODO implementation
        }
    }

    private void decode_0xED_00u3F(int opCode) {
        switch (opCode) {
            // TODO implementation
        }
    }

    private void decode_0xED_40u7F(int opCode) {
        switch (opCode) {
            // TODO implementation
        }
    }

    private void decode_0xED_80uBF(int opCode) {
        switch (opCode) {
            // TODO implementation
        }
    }

    private void decode_0xED_C0uFF(int opCode) {
        switch (opCode) {
            // TODO implementation
        }
    }

    private void decode_0xFD_00u3F(int opCode) {
        switch (opCode) {
            // TODO implementation
        }
    }

    private void decode_0xFD_40u7F(int opCode) {
        switch (opCode) {
            // TODO implementation
        }
    }

    private void decode_0xFD_80uBF(int opCode) {
        switch (opCode) {
            // TODO implementation
        }
    }

    private void decode_0xFD_C0uFF(int opCode) {
        switch (opCode) {
            // TODO implementation
        }
    }

    // --- Utility functions ---

    // Let's concentrate on only the cf, pf, zf, sf flags for now
    // (because hf, nf are needed for DAA only; and xf, yf are undocumented).
    //
    // NOTES:
    // * `LD` (p81-97, p100-115) `PUSH` (p116-118), `POP` (p119-121), `EX` (p122-127) opcodes don't normally affect the
    //   conditions bit (flags register).
    // * `LD A,I` (p98) and `LD A,R` (p99) are the exception and affect all the condition bits, based on the value of
    //   the copied register and the IFF2 flag (but `LD R,A` and `LD R,I` don't affect the condition bits).
    // * `LDI`, `LDIR`, `LDD`, `LDDR` (p128-133) affect pf (and hf/nf) but not cf/zf/sf.
    // * `CPI`, `CPIR`, `CPD`, `CPDR` (p134-139) affect pf/zf (and hf/nf) but not cf/sf.
    //

    private void exDeHl() { // ZUM(p.122)
        int de = state.de();
        state.de(state.hl());
        state.hl(de);
    }

    private void exAfAf_() { // ZUM(p.123)
        int af = state.af();
        state.af(state.af_());
        state.af_(af);
    }

    private void exx() { // ZUM(p.124)
        int bc = state.bc();
        int de = state.de();
        int hl = state.hl();
        state.bc(state.bc_());
        state.de(state.de_());
        state.hl(state.hl_());
        state.bc_(bc);
        state.de_(de);
        state.hl_(hl);
    }

    private void exStkHl() { // ZUM(p.125)
        int hl = state.hl();
        state.hl(bus.readWord(state.sp()));
        bus.writeWord(state.sp(), hl);
    }

    private void add8(int n) { // ZUM(pp.140-145)
        int ru = state.a() + n;
        int rm = ru & 0xFF;
        state.cf(rm != ru);
        //state.pf(...); // TODO update pf
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        state.a(rm);
    }

    private void adc8(int n) { // ZUM(pp.146-147)
        int ru = state.a() + n + (state.cf() ? 1 : 0);
        int rm = ru & 0xFF;
        state.cf(rm != ru);
        //state.pf(...); // TODO update pf
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        state.a(rm);
    }

    private void sub8(int n) { // ZUM(pp.148-149)
        int ru = state.a() - n;
        int rm = ru & 0xFF;
        state.cf(rm != ru);
        //state.pf(...); // TODO update pf
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        state.a(rm);
    }

    private void sbc8(int n) { // ZUM(pp.150-151)
        int ru = state.a() - n - (state.cf() ? 1 : 0);
        int rm = ru & 0xFF;
        state.cf(rm != ru);
        //state.pf(...); // TODO update pf
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        state.a(rm);
    }

    private void and8(int n) { // ZUM(pp.152-153)
        int rm = state.a() & n;
        state.cf(false);
        //state.pf(...); // TODO update pf
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        state.a(rm);
    }

    private void or8(int n) { // ZUM(pp.154-155)
        int rm = state.a() | n;
        state.cf(false);
        //state.pf(...); // TODO update pf
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        state.a(rm);
    }

    private void xor8(int n) { // ZUM(pp.156-157)
        int rm = state.a() ^ n;
        state.cf(false);
        //state.pf(...); // TODO update pf
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        state.a(rm);
    }

    private void cp8(int n) { // ZUM(pp.158-159)
        int rm = (state.a() - n) & 0xFF;
        //state.pf(...); // TODO update pf
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
    }

    private int inc8(int n) { // ZUM(pp.160-163)
        int rm = (n + 1) & 0xFF;
        state.pf(n == 0x7F); // 0x7F -> 0x80 is overflow
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        return n;
    }

    private int dec8(int n) { // ZUM(pp.164-165)
        int rm = (n - 1) & 0xFF;
        state.pf(n == 0x80); // 0x80 -> 0x7F is overflow
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        return rm;
    }

    private void daa() { // ZUM(pp.166-167)
        // TODO implementation
    }

    private void cpl() { // ZUM(p.168)
        state.a(state.a() ^ 0xFF);
    }

    private void neg() { // ZUM(p.169)
        int ru = 0x100 - state.a();
        int rm = ru & 0xFF;
        state.cf(rm == ru); // i.e. state.a() != 0
        state.pf(rm == 0x80); // i.e. state.a() == 0x80
        state.zf(rm == 0x00);
        state.sf((rm & 0x80) != 0);
        state.a(rm);
    }

    private void ccf() { // ZUM(p.170)
        state.cf(!state.cf());
    }

    private void scf() { // ZUM(p.171)
        state.cf(true);
    }

    private void di() { // ZUM(p.174)
        // TODO implementation
    }

    private void ei() { // ZUM(p.175)
        // TODO implementation
    }

    private int add16(int n) { // ZUM(p.179)
        int ru = state.hl() + n;
        int rm = ru & 0xFFFF;
        state.cf(rm != ru);
        // state.pf(...) // TODO update pf
        return rm;
    }

    private int adc16(int n) { // ZUM(p.180)
        int ru = state.hl() + n + (state.cf() ? 1 : 0);
        int rm = ru & 0xFFFF;
        state.cf(rm != ru);
        // state.pf(...) // TODO update pf
        state.zf(rm == 0);
        state.sf((rm & 0x8000) != 0);
        return rm;
    }

    private int sbc16(int n) { // ZUM(p.181)
        int ru = state.hl() - n - (state.cf() ? 1 : 0);
        int rm = ru & 0xFFFF;
        state.cf(rm != ru);
        // state.pf(...) // TODO update pf
        state.zf(rm == 0);
        state.sf((rm & 0x8000) != 0);
        return rm;
    }

    private int inc16(int n) { // ZUM(pp.184-186)
        return (n + 1) & 0xFFFF;
    }

    private int dec16(int n) { // ZUM(pp.187-189)
        return (n - 1) & 0xFFFF;
    }

    private void rlca() { // ZUM(p.190)
        boolean bit7 = (state.a() & 0x80) != 0;
        int rm = ((state.a() << 1) | (bit7 ? 1 : 0)) & 0xFF;
        state.cf(bit7);
        state.a(rm);
    }

    private void rla() { // ZUM(p.191)
        boolean bit7 = (state.a() & 0x80) != 0;
        int rm = ((state.a() << 1) | (state.cf() ? 1 : 0)) & 0xFF;
        state.cf(bit7);
        state.a(rm);
    }

    private void rrca() { // ZUM(p.192)
        boolean bit0 = (state.a() & 0x1) != 0;
        int rm = (bit0 ? 0x80 : 0) | (state.a() >>> 1);
        state.cf(bit0);
        state.a(rm);
    }

    private void rra() { // ZUM(p.193)
        boolean bit0 = (state.a() & 0x1) != 0;
        int rm = (state.cf() ? 0x80 : 0) | (state.a() >>> 1);
        state.cf(bit0);
        state.a(rm);
    }

    private int rlc(int n) { // ZUM(pp.194-201)
        boolean bit7 = (n & 0x80) != 0;
        int rm = ((n << 1) | (bit7 ? 1 : 0)) & 0xFF;
        state.cf(bit7);
        //state.pf(...) // TODO update pf
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        return rm;
    }

    private int rl(int n) { // ZUM(pp.202-204)
        boolean bit7 = (n & 0x80) != 0;
        int rm = ((n << 1) | (state.cf() ? 1 : 0)) & 0xFF;
        state.cf(bit7);
        //state.pf(...) // TODO update pf
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        return rm;
    }

    private int rrc(int n) { // ZUM(pp.205-207)
        boolean bit0 = (n & 0x1) != 0;
        int rm = (bit0 ? 0x80 : 0) | (n >>> 1);
        state.cf(bit0);
        //state.pf(...) // TODO update pf
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        return rm;
    }

    private int rr(int n) { // ZUM(pp.208-210)
        boolean bit0 = (n & 0x1) != 0;
        int rm = (state.cf() ? 0x80 : 0) | (n >>> 1);
        state.cf(bit0);
        //state.pf(...) // TODO update pf
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        return rm;
    }

    private int sla(int n) { // ZUM(pp.211-213)
        boolean bit7 = (n & 0x80) != 0;
        int rm = (n << 1) & 0xFF;
        state.cf(bit7);
        //state.pf(...) // TODO update pf
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        return rm;
    }

    private int sra(int n) { // ZUM(pp.214-216)
        boolean bit7 = (n & 0x80) != 0;
        boolean bit0 = (n & 0x01) != 0;
        int rm = (bit7 ? 0x80 : 0) | (n >>> 1) & 0xFF;
        state.cf(bit0);
        //state.pf(...) // TODO update pf
        state.zf(rm == 0);
        state.sf(bit7);
        return rm;
    }

    private int sll(int n) { // undocumented
        boolean bit7 = (n & 0x80) != 0;
        int rm = ((n << 1) | 0x01) & 0xFF;
        state.cf(bit7);
        //state.pf(...) // TODO update pf
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        return rm;
    }

    private int srl(int n) { // ZUM(pp.217-219)
        boolean bit0 = (n & 0x01) != 0;
        int rm = (n >>> 1) & 0xFF;
        state.cf(bit0);
        //state.pf(...) // TODO update pf
        state.zf(rm == 0);
        state.sf(false);
        return rm;
    }

    private void bit(int b, int n) { // ZUM(pp.224-231)
        state.zf((n & (1 << b)) == 0);
    }

    private int set(int b, int n) { // ZUM(pp.232-235)
        return n | (1 << b);
    }

    private int res(int b, int n) { // ZUM(pp.236-237)
        return n & ~(1 << b);
    }

    private void jp0() { // ZUM(p.238)
        state.pc(bus.readWord(state.pc()));
    }

    private void jp1(boolean cond) { // ZUM(pp.239-240)
        if (cond) jp0();
        else state.pcInc2();
    }

    private void jr0() { // ZUM(p.241)
        int offset = bus.readMemory(state.pc());
        state.pc(state.pc() + (offset < 0x80 ? offset : offset - 0x100) + 1);
    }

    private void jr1(boolean cond) { // ZUM(pp.242-249)
        if (cond) jr0();
        else state.pcInc1();
    }

    private void djnz() { // ZUM(pp.253-254)
        state.b(state.b() - 1);
        if (state.b() != 0) jr0();
        else state.pcInc1();
    }

    private void call0() { // ZUM(pp.255-256)
        int address = bus.readWord(state.pcInc2());
        bus.writeWord(state.spDec2(), state.pc());
        state.pc(address);
    }

    private void call1(boolean cond) { // ZUM(pp.257-259)
        if (cond) call0();
        else state.pcInc2();
    }

    private void ret0() { // ZUM(p.260)
        state.pc(bus.readWord(state.spInc2()));
    }

    private void ret1(boolean cond) { // ZUM(pp.261-262)
        if (cond) ret0();
    }

    private void rst(int address) { // ZUM(pp.267-268)
        bus.writeWord(state.spDec2(), state.pc());
        state.pc(address & 0x0038);
    }
}
