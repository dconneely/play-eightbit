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
            case 0x00/*NOP*/ -> {/*do nothing*/} // ZUM(172) HTP(359)
            case 0x01/*LD BC,nn*/ -> state.bc(bus.readWord(state.pcInc2())); // ZUM(102) HTP(293-294)
            case 0x02/*LD (BC),A*/ -> bus.writeMemory(state.bc(), state.a()); // ZUM(95) HTP(299)
            case 0x03/*INC BC*/ -> state.bc(inc_nn(state.bc())); // ZUM(184) HTP(265-266)
            case 0x04/*INC B*/ -> state.b(inc_n(state.b())); // ZUM(160) HTP(264)
            case 0x05/*DEC B*/ -> state.b(dec_n(state.b())); // ZUM(164-165) HTP(238-239)
            case 0x06/*LD B,n*/ -> state.b(bus.readMemory(state.pcInc1())); // ZUM(82) HTP(295-296)
            case 0x07/*RLCA*/ -> rlca(); // ZUM(190) HTP(399)
            case 0x08/*EX AF,AF'*/ -> ex_af_aaf(); // ZUM(123) HTP(248)
            case 0x09/*ADD HL,BC*/ -> add_hl_nn(state.bc()); // ZUM(179) HTP(203-204)
            case 0x0A/*LD A,(BC)*/ -> state.a(bus.readMemory(state.bc())); // ZUM(92) HTP(329)
            case 0x0B/*DEC BC*/ -> state.bc(dec_nn(state.bc())); // ZUM(187) HTP(240-241)
            case 0x0C/*INC C*/ -> state.c(inc_n(state.c())); // ZUM(160) HTP(264)
            case 0x0D/*DEC C*/ -> state.c(dec_n(state.c())); // ZUM(164-165) HTP(238-239)
            case 0x0E/*LD C,n*/ -> state.c(bus.readMemory(state.pcInc1())); // ZUM(82) HTP(295-296)
            case 0x0F/*RRCA*/ -> rrca(); // ZUM(192) HTP(415)
            case 0x10/*DJNZ n*/ -> djnz(); // ZUM(253-254) HTP(245-246)
            case 0x11/*LD DE,nn*/ -> state.de(bus.readWord(state.pcInc2())); // ZUM(102) HTP(293-294)
            case 0x12/*LD (DE),A*/ -> bus.writeMemory(state.de(), state.a()); // ZUM(96) HTP(300)
            case 0x13/*INC DE*/ -> state.de(inc_nn(state.de())); // ZUM(184) HTP(265-266)
            case 0x14/*INC D*/ -> state.d(inc_n(state.d())); // ZUM(160) HTP(264)
            case 0x15/*DEC D*/ -> state.d(dec_n(state.d())); // ZUM(164-165) HTP(238-239)
            case 0x16/*LD D,n*/ -> state.d(bus.readMemory(state.pcInc1())); // ZUM(82) HTP(295-296)
            case 0x17/*RLA*/ -> rla(); // ZUM(191) HTP(398)
            case 0x18/*JR n*/ -> jr(); // ZUM(241) HTP(290)
            case 0x19/*ADD HL,DE*/ -> add_hl_nn(state.de()); // ZUM(179) HTP(203-204)
            case 0x1A/*LD A,(DE)*/ -> state.a(bus.readMemory(state.de())); // ZUM(93) HTP(330)
            case 0x1B/*DEC DE*/ -> state.de(dec_nn(state.de())); // ZUM(187) HTP(240-241)
            case 0x1C/*INC E*/ -> state.e(inc_n(state.e())); // ZUM(160) HTP(264)
            case 0x1D/*DEC E*/ -> state.e(dec_n(state.e())); // ZUM(164-165) HTP(238-239)
            case 0x1E/*LD E,n*/ -> state.e(bus.readMemory(state.pcInc1())); // ZUM(82) HTP(295-296)
            case 0x1F/*RRA*/ -> rra(); // ZUM(193) HTP(412)
            case 0x20/*JR NZ,n*/ -> jr_cc(!state.zf()); // ZUM(248-249) HTP(288-289)
            case 0x21/*LD HL,nn*/ -> state.hl(bus.readWord(state.pcInc2())); // ZUM(102) HTP(293-294)
            case 0x22/*LD (nn),HL*/ -> bus.writeWord(bus.readWord(state.pcInc2()), state.hl()); // ZUM(109) HTP(323-324)
            case 0x23/*INC HL*/ -> state.hl(inc_nn(state.hl())); // ZUM(184) HTP(265-266)
            case 0x24/*INC H*/ -> state.h(inc_n(state.h())); // ZUM(160) HTP(264)
            case 0x25/*DEC H*/ -> state.h(dec_n(state.h())); // ZUM(164-165) HTP(238-239)
            case 0x26/*LD H,n*/ -> state.h(bus.readMemory(state.pcInc1())); // ZUM(82) HTP(295-296)
            case 0x27/*DAA*/ -> daa(); // ZUM(166) HTP(236-237)
            case 0x28/*JR Z,n*/ -> jr_cc(state.zf()); // ZUM(246-247) HTP(288-289)
            case 0x29/*ADD HL,HL*/ -> add_hl_nn(state.hl()); // ZUM(179) HTP(203-204)
            case 0x2A/*LD HL,(nn)*/ -> state.hl(bus.readWord(bus.readWord(state.pcInc2()))); // ZUM(105) HTP(334-335)
            case 0x2B/*DEC HL*/ -> state.hl(dec_nn(state.hl())); // ZUM(187) HTP(240-241)
            case 0x2C/*INC L*/ -> state.l(inc_n(state.l())); // ZUM(160) HTP(264)
            case 0x2D/*DEC L*/ -> state.l(dec_n(state.l())); // ZUM(164-165) HTP(238-239)
            case 0x2E/*LD L,n*/ -> state.l(bus.readMemory(state.pcInc1())); // ZUM(82) HTP(295-296)
            case 0x2F/*CPL*/ -> cpl(); // ZUM(168) HTP(235)
            case 0x30/*JR NC,n*/ -> jr_cc(!state.cf()); // ZUM(244-245) HTP(288-289)
            case 0x31/*LD SP,nn*/ -> state.sp(bus.readWord(state.pcInc2())); // ZUM(102) HTP(293-294)
            case 0x32/*LD (nn),A*/ -> bus.writeMemory(bus.readWord(state.pcInc2()), state.a()); // ZUM(97) HTP(319-320)
            case 0x33/*INC SP*/ -> state.sp(inc_nn(state.sp())); // ZUM(184) HTP(265-266)
            case 0x34/*INC (HL)*/ -> bus.writeMemory(state.hl(), inc_n(bus.readMemory(state.hl()))); // ZUM(161) HTP(267)
            case 0x35/*DEC (HL)*/ -> bus.writeMemory(state.hl(), dec_n(bus.readMemory(state.hl()))); // ZUM(164-165) HTP(238-239)
            case 0x36/*LD (HL),n*/ -> bus.writeMemory(state.hl(), bus.readMemory(state.pcInc1())); // ZUM(86) HTP(301-302)
            case 0x37/*SCF*/ -> scf(); // ZUM(171) HTP(424)
            case 0x38/*JR C,n*/ -> jr_cc(state.cf()); // ZUM(242-243) HTP(288-289)
            case 0x39/*ADD HL,SP*/ -> add_hl_nn(state.sp()); // ZUM(179) HTP(203-204)
            case 0x3A/*LD A,(nn)*/ -> state.a(bus.readMemory(bus.readWord(state.pcInc2()))); // ZUM(94) HTP(317-318)
            case 0x3B/*DEC SP*/ -> state.sp(dec_nn(state.sp())); // ZUM(187) HTP(240-241)
            case 0x3C/*INC A*/ -> state.a(inc_n(state.a())); // ZUM(160) HTP(264)
            case 0x3D/*DEC A*/ -> state.a(dec_n(state.a())); // ZUM(164-165) HTP(238-239)
            case 0x3E/*LD A,n*/ -> state.a(bus.readMemory(state.pcInc1())); // ZUM(82) HTP(295-296)
            case 0x3F/*CCF*/ -> ccf(); // ZUM(170) HTP(224)
        }
    }

    private void decode_0x40u7F(int opCode) {
        switch (opCode) {
            case 0x40/*LD B,B*/ -> {/*state.b(state.b()):*/} // ZUM(81) HTP(297-298)
            case 0x41/*LD B,C*/ -> state.b(state.c()); // ZUM(81) HTP(297-298)
            case 0x42/*LD B,D*/ -> state.b(state.d()); // ZUM(81) HTP(297-298)
            case 0x43/*LD B,E*/ -> state.b(state.e()); // ZUM(81) HTP(297-298)
            case 0x44/*LD B,H*/ -> state.b(state.h()); // ZUM(81) HTP(297-298)
            case 0x45/*LD B,L*/ -> state.b(state.l()); // ZUM(81) HTP(297-298)
            case 0x46/*LD B,(HL)*/ -> state.b(bus.readMemory(state.hl())); // ZUM(83) HTP(356-357)
            case 0x47/*LD B,A*/ -> state.b(state.a()); // ZUM(81) HTP(297-298)
            case 0x48/*LD C,B*/ -> state.c(state.b()); // ZUM(81) HTP(297-298)
            case 0x49/*LD C,C*/ -> {/*state.c(state.c());*/} // ZUM(81) HTP(297-298)
            case 0x4A/*LD C,D*/ -> state.c(state.d()); // ZUM(81) HTP(297-298)
            case 0x4B/*LD C,E*/ -> state.c(state.e()); // ZUM(81) HTP(297-298)
            case 0x4C/*LD C,H*/ -> state.c(state.h()); // ZUM(81) HTP(297-298)
            case 0x4D/*LD C,L*/ -> state.c(state.l()); // ZUM(81) HTP(297-298)
            case 0x4E/*LD C,(HL)*/ -> state.c(bus.readMemory(state.hl())); // ZUM(83) HTP(356-357)
            case 0x4F/*LD C,A*/ -> state.c(state.a()); // ZUM(81) HTP(297-298)
            case 0x50/*LD D,B*/ -> state.d(state.b()); // ZUM(81) HTP(297-298)
            case 0x51/*LD D,C*/ -> state.d(state.c()); // ZUM(81) HTP(297-298)
            case 0x52/*LD D,D*/ -> {/*state.d(state.d());*/} // ZUM(81) HTP(297-298)
            case 0x53/*LD D,E*/ -> state.d(state.e()); // ZUM(81) HTP(297-298)
            case 0x54/*LD D,H*/ -> state.d(state.h()); // ZUM(81) HTP(297-298)
            case 0x55/*LD D,L*/ -> state.d(state.l()); // ZUM(81) HTP(297-298)
            case 0x56/*LD D,(HL)*/ -> state.d(bus.readMemory(state.hl())); // ZUM(83) HTP(356-357)
            case 0x57/*LD D,A*/ -> state.d(state.a()); // ZUM(81) HTP(297-298)
            case 0x58/*LD E,B*/ -> state.e(state.b()); // ZUM(81) HTP(297-298)
            case 0x59/*LD E,C*/ -> state.e(state.c()); // ZUM(81) HTP(297-298)
            case 0x5A/*LD E,D*/ -> state.e(state.d()); // ZUM(81) HTP(297-298)
            case 0x5B/*LD E,E*/ -> {/*state.e(state.e());*/} // ZUM(81) HTP(297-298)
            case 0x5C/*LD E,H*/ -> state.e(state.h()); // ZUM(81) HTP(297-298)
            case 0x5D/*LD E,L*/ -> state.e(state.l()); // ZUM(81) HTP(297-298)
            case 0x5E/*LD E,(HL)*/ -> state.e(bus.readMemory(state.hl())); // ZUM(83) HTP(356-357)
            case 0x5F/*LD E,A*/ -> state.e(state.a()); // ZUM(81) HTP(297-298)
            case 0x60/*LD H,B*/ -> state.h(state.b()); // ZUM(81) HTP(297-298)
            case 0x61/*LD H,C*/ -> state.h(state.c()); // ZUM(81) HTP(297-298)
            case 0x62/*LD H,D*/ -> state.h(state.d()); // ZUM(81) HTP(297-298)
            case 0x63/*LD H,E*/ -> state.h(state.e()); // ZUM(81) HTP(297-298)
            case 0x64/*LD H,H*/ -> {/*state.h(state.h());*/} // ZUM(81) HTP(297-298)
            case 0x65/*LD H,L*/ -> state.h(state.l()); // ZUM(81) HTP(297-298)
            case 0x66/*LD H,(HL)*/ -> state.h(bus.readMemory(state.hl())); // ZUM(83) HTP(356-357)
            case 0x67/*LD H,A*/ -> state.h(state.a()); // ZUM(81) HTP(297-298)
            case 0x68/*LD L,B*/ -> state.l(state.b()); // ZUM(81) HTP(297-298)
            case 0x69/*LD L,C*/ -> state.l(state.c()); // ZUM(81) HTP(297-298)
            case 0x6A/*LD L,D*/ -> state.l(state.d()); // ZUM(81) HTP(297-298)
            case 0x6B/*LD L,E*/ -> state.l(state.e()); // ZUM(81) HTP(297-298)
            case 0x6C/*LD L,H*/ -> state.l(state.h()); // ZUM(81) HTP(297-298)
            case 0x6D/*LD L,L*/ -> {/*state.l(state.l());*/} // ZUM(81) HTP(297-298)
            case 0x6E/*LD L,(HL)*/ -> state.l(bus.readMemory(state.hl())); // ZUM(83) HTP(356-357)
            case 0x6F/*LD L,A*/ -> state.l(state.a()); // ZUM(81) HTP(297-298)
            case 0x70/*LD (HL),B*/ -> bus.writeMemory(state.hl(), state.b()); // ZUM(86) HTP(303-304)
            case 0x71/*LD (HL),C*/ -> bus.writeMemory(state.hl(), state.c()); // ZUM(86) HTP(303-304)
            case 0x72/*LD (HL),D*/ -> bus.writeMemory(state.hl(), state.d()); // ZUM(86) HTP(303-304)
            case 0x73/*LD (HL),E*/ -> bus.writeMemory(state.hl(), state.e()); // ZUM(86) HTP(303-304)
            case 0x74/*LD (HL),H*/ -> bus.writeMemory(state.hl(), state.h()); // ZUM(86) HTP(303-304)
            case 0x75/*LD (HL),L*/ -> bus.writeMemory(state.hl(), state.l()); // ZUM(86) HTP(303-304)
            case 0x76/*HALT*/ -> state.halted(true); // ZUM(173) HTP(257)
            case 0x77/*LD (HL),A*/ -> bus.writeMemory(state.hl(), state.a()); // ZUM(86) HTP(303-304)
            case 0x78/*LD A,B*/ -> state.a(state.b()); // ZUM(81) HTP(297-298)
            case 0x79/*LD A,C*/ -> state.a(state.c()); // ZUM(81) HTP(297-298)
            case 0x7A/*LD A,D*/ -> state.a(state.d()); // ZUM(81) HTP(297-298)
            case 0x7B/*LD A,E*/ -> state.a(state.e()); // ZUM(81) HTP(297-298)
            case 0x7C/*LD A,H*/ -> state.a(state.h()); // ZUM(81) HTP(297-298)
            case 0x7D/*LD A,L*/ -> state.a(state.l()); // ZUM(81) HTP(297-298)
            case 0x7E/*LD A,(HL)*/ -> state.a(bus.readMemory(state.hl())); // ZUM(83) HTP(356-357)
            case 0x7F/*LD A,A*/ -> {/*state.a(state.a());*/} // ZUM(81) HTP(297-298)
        }
    }

    private void decode_0x80uBF(int opCode) {
        switch (opCode) {
            case 0x80/*ADD A,B*/ -> add_a_n(state.b()); // ZUM(140-141) HTP(201-202)
            case 0x81/*ADD A,C*/ -> add_a_n(state.c()); // ZUM(140-141) HTP(201-202)
            case 0x82/*ADD A,D*/ -> add_a_n(state.d()); // ZUM(140-141) HTP(201-202)
            case 0x83/*ADD A,E*/ -> add_a_n(state.e()); // ZUM(140-141) HTP(201-202)
            case 0x84/*ADD A,H*/ -> add_a_n(state.h()); // ZUM(140-141) HTP(201-202)
            case 0x85/*ADD A,L*/ -> add_a_n(state.l()); // ZUM(140-141) HTP(201-202)
            case 0x86/*ADD A,(HL)*/ -> add_a_n(bus.readMemory(state.hl())); // ZUM(143) HTP(194-195)
            case 0x87/*ADD A,A*/ -> add_a_n(state.a()); // ZUM(140-141) HTP(201-202)
            case 0x88/*ADC A,B*/ -> adc_a_n(state.b()); // ZUM(146-147) HTP(190-191)
            case 0x89/*ADC A,C*/ -> adc_a_n(state.c()); // ZUM(146-147) HTP(190-191)
            case 0x8A/*ADC A,D*/ -> adc_a_n(state.d()); // ZUM(146-147) HTP(190-191)
            case 0x8B/*ADC A,E*/ -> adc_a_n(state.e()); // ZUM(146-147) HTP(190-191)
            case 0x8C/*ADC A,H*/ -> adc_a_n(state.h()); // ZUM(146-147) HTP(190-191)
            case 0x8D/*ADC A,L*/ -> adc_a_n(state.l()); // ZUM(146-147) HTP(190-191)
            case 0x8E/*ADC A,(HL)*/ -> adc_a_n(bus.readMemory(state.hl())); // ZUM(146-147) HTP(190-191)
            case 0x8F/*ADC A,A*/ -> adc_a_n(state.a()); // ZUM(146-147) HTP(190-191)
            case 0x90/*SUB B*/ -> sub_n(state.b()); // ZUM(148-149) HTP(434-435)
            case 0x91/*SUB C*/ -> sub_n(state.c()); // ZUM(148-149) HTP(434-435)
            case 0x92/*SUB D*/ -> sub_n(state.d()); // ZUM(148-149) HTP(434-435)
            case 0x93/*SUB E*/ -> sub_n(state.e()); // ZUM(148-149) HTP(434-435)
            case 0x94/*SUB H*/ -> sub_n(state.h()); // ZUM(148-149) HTP(434-435)
            case 0x95/*SUB L*/ -> sub_n(state.l()); // ZUM(148-149) HTP(434-435)
            case 0x96/*SUB (HL)*/ -> sub_n(bus.readMemory(state.hl())); // ZUM(148-149) HTP(434-435)
            case 0x97/*SUB A*/ -> sub_n(state.a()); // ZUM(148-149) HTP(434-435)
            case 0x98/*SBC A,B*/ -> sbc_a_n(state.b()); // ZUM(150-151) HTP(420-421)
            case 0x99/*SBC A,C*/ -> sbc_a_n(state.c()); // ZUM(150-151) HTP(420-421)
            case 0x9A/*SBC A,D*/ -> sbc_a_n(state.d()); // ZUM(150-151) HTP(420-421)
            case 0x9B/*SBC A,E*/ -> sbc_a_n(state.e()); // ZUM(150-151) HTP(420-421)
            case 0x9C/*SBC A,H*/ -> sbc_a_n(state.h()); // ZUM(150-151) HTP(420-421)
            case 0x9D/*SBC A,L*/ -> sbc_a_n(state.l()); // ZUM(150-151) HTP(420-421)
            case 0x9E/*SBC A,(HL)*/ -> sbc_a_n(bus.readMemory(state.hl())); // ZUM(150-151) HTP(420-421)
            case 0x9F/*SBC A,A*/ -> sbc_a_n(state.a()); // ZUM(150-151) HTP(420-421)
            case 0xA0/*AND B*/ -> and_n(state.b()); // ZUM(152-153) HTP(209-210)
            case 0xA1/*AND C*/ -> and_n(state.c()); // ZUM(152-153) HTP(209-210)
            case 0xA2/*AND D*/ -> and_n(state.d()); // ZUM(152-153) HTP(209-210)
            case 0xA3/*AND E*/ -> and_n(state.e()); // ZUM(152-153) HTP(209-210)
            case 0xA4/*AND H*/ -> and_n(state.h()); // ZUM(152-153) HTP(209-210)
            case 0xA5/*AND L*/ -> and_n(state.l()); // ZUM(152-153) HTP(209-210)
            case 0xA6/*AND (HL)*/ -> and_n(bus.readMemory(state.hl())); // ZUM(152-153) HTP(209-210)
            case 0xA7/*AND A*/ -> and_n(state.a()); // ZUM(152-153) HTP(209-210)
            case 0xA8/*XOR B*/ -> xor_n(state.b()); // ZUM(156-157) HTP(436-437)
            case 0xA9/*XOR C*/ -> xor_n(state.c()); // ZUM(156-157) HTP(436-437)
            case 0xAA/*XOR D*/ -> xor_n(state.d()); // ZUM(156-157) HTP(436-437)
            case 0xAB/*XOR E*/ -> xor_n(state.e()); // ZUM(156-157) HTP(436-437)
            case 0xAC/*XOR H*/ -> xor_n(state.h()); // ZUM(156-157) HTP(436-437)
            case 0xAD/*XOR L*/ -> xor_n(state.l()); // ZUM(156-157) HTP(436-437)
            case 0xAE/*XOR (HL)*/ -> xor_n(bus.readMemory(state.hl())); // ZUM(156-157) HTP(436-437)
            case 0xAF/*XOR A*/ -> xor_n(state.a()); // ZUM(156-157) HTP(436-437)
            case 0xB0/*OR B*/ -> or_n(state.b()); // ZUM(154-155) HTP(360-361)
            case 0xB1/*OR C*/ -> or_n(state.c()); // ZUM(154-155) HTP(360-361)
            case 0xB2/*OR D*/ -> or_n(state.d()); // ZUM(154-155) HTP(360-361)
            case 0xB3/*OR E*/ -> or_n(state.e()); // ZUM(154-155) HTP(360-361)
            case 0xB4/*OR H*/ -> or_n(state.h()); // ZUM(154-155) HTP(360-361)
            case 0xB5/*OR L*/ -> or_n(state.l()); // ZUM(154-155) HTP(360-361)
            case 0xB6/*OR (HL)*/ -> or_n(bus.readMemory(state.hl())); // ZUM(154-155) HTP(360-361)
            case 0xB7/*OR A*/ -> or_n(state.a()); // ZUM(154-155) HTP(360-361)
            case 0xB8/*CP B*/ -> cp_n(state.b()); // ZUM(158-159) HTP(225-226)
            case 0xB9/*CP C*/ -> cp_n(state.c()); // ZUM(158-159) HTP(225-226)
            case 0xBA/*CP D*/ -> cp_n(state.d()); // ZUM(158-159) HTP(225-226)
            case 0xBB/*CP E*/ -> cp_n(state.e()); // ZUM(158-159) HTP(225-226)
            case 0xBC/*CP H*/ -> cp_n(state.h()); // ZUM(158-159) HTP(225-226)
            case 0xBD/*CP L*/ -> cp_n(state.l()); // ZUM(158-159) HTP(225-226)
            case 0xBE/*CP (HL)*/ -> cp_n(bus.readMemory(state.hl())); // ZUM(158-159) HTP(225-226)
            case 0xBF/*CP A*/ -> cp_n(state.a()); // ZUM(158-159) HTP(225-226)
        }
    }

    private void decode_0xC0uFF(int opCode) {
        switch (opCode) {
            case 0xC0/*RET NZ*/ -> ret_cc(!state.zf()); // ZUM(261-262) HTP(390-391)
            case 0xC1/*POP BC*/ -> state.bc(bus.readWord(state.spInc2())); // ZUM(119) HTP(373-374)
            case 0xC2/*JP NZ,nn*/ -> jp_cc(!state.zf()); // ZUM(239-240) HTP(282-283)
            case 0xC3/*JP nn*/ -> jp(); // ZUM(236) HTP(284)
            case 0xC4/*CALL NZ,nn*/ -> call_cc(!state.zf()); // ZUM(257-259) HTP(219-221)
            case 0xC5/*PUSH BC*/ -> bus.writeWord(state.spDec2(), state.bc()); // ZUM(116) HTP(379-380)
            case 0xC6/*ADD A,n*/ -> add_a_n(bus.readMemory(state.pcInc1())); // ZUM(142) HTP(200)
            case 0xC7/*RST 00H*/ -> rst_n(0x00); // ZUM(267-268) HTP(418-419)
            case 0xC8/*RET Z*/ -> ret_cc(state.zf()); // ZUM(261-262) HTP(390-391)
            case 0xC9/*RET*/ -> ret(); // ZUM(260) HTP(388-389)
            case 0xCA/*JP Z,nn*/ -> jp_cc(state.zf()); // ZUM(239-240) HTP(282-283)
            case 0xCB/*[0xCB] ...*/ -> decode_0xCB_00uFF(bus.readMemory(state.pcInc1()));
            case 0xCC/*CALL Z,nn*/ -> call_cc(state.zf()); // ZUM(257-259) HTP(219-221)
            case 0xCD/*CALL nn*/ -> call(); // ZUM(255-256) HTP(222-223)
            case 0xCE/*ADC A,n*/ -> adc_a_n(bus.readMemory(state.pcInc1())); // ZUM(146-147) HTP(190-191)
            case 0xCF/*RST 08H*/ -> rst_n(0x08); // ZUM(267-268) HTP(418-419)
            case 0xD0/*RET NC*/ -> ret_cc(!state.cf()); // ZUM(261-262) HTP(390-391)
            case 0xD1/*POP DE*/ -> state.de(bus.readWord(state.spInc2())); // ZUM(119) HTP(373-374)
            case 0xD2/*JP NC,nn*/ -> jp_cc(!state.cf()); // ZUM(239-240) HTP(282-283)
            case 0xD3/*OUT (n),A*/ -> bus.writeIoPort((state.a() << 8) | bus.readMemory(state.pcInc1()), state.a()); // ZUM(279) HTP(368)
            case 0xD4/*CALL NC,nn*/ -> call_cc(!state.cf()); // ZUM(257-259) HTP(219-221)
            case 0xD5/*PUSH DE*/ -> bus.writeWord(state.spDec2(), state.de()); // ZUM(116) HTP(379-380)
            case 0xD6/*SUB n*/ -> sub_n(bus.readMemory(state.pcInc1())); // ZUM(148-149) HTP(434-435)
            case 0xD7/*RST 10H*/ -> rst_n(0x10); // ZUM(267-268) HTP(418-419)
            case 0xD8/*RET C*/ -> ret_cc(state.cf()); // ZUM(261-262) HTP(390-391)
            case 0xD9/*EXX*/ -> exx(); // ZUM(124) HTP(256)
            case 0xDA/*JP C,nn*/ -> jp_cc(state.cf()); // ZUM(239-240) HTP(282-283)
            case 0xDB/*IN A,(n)*/ -> state.a(bus.readIoPort((state.a() << 8) | bus.readMemory(state.pcInc1()))); // ZUM(269) HTP(263)
            case 0xDC/*CALL C,nn*/ -> call_cc(state.cf()); // ZUM(257-259) HTP(219-221)
            case 0xDD/*[0xDD] ...*/ -> decode_0xDD_00uFF(bus.readMemory(state.pcInc1()));
            case 0xDE/*SBC A,n*/ -> sbc_a_n(bus.readMemory(state.pcInc1())); // ZUM(150-151) HTP(420-421)
            case 0xDF/*RST 18H*/ -> rst_n(0x18); // ZUM(267-268) HTP(418-419)
            case 0xE0/*RET PO*/ -> ret_cc(!state.pf()); // ZUM(261-262) HTP(390-391)
            case 0xE1/*POP HL*/ -> state.hl(bus.readWord(state.spInc2())); // ZUM(119) HTP(373-374)
            case 0xE2/*JP PO,nn*/ -> jp_cc(!state.pf()); // ZUM(239-240) HTP(282-283)
            case 0xE3/*EX (SP),HL*/ -> ex_csp_hl(); // ZUM(125) HTP(250-251)
            case 0xE4/*CALL PO,nn*/ -> call_cc(!state.pf()); // ZUM(257-259) HTP(219-221)
            case 0xE5/*PUSH HL*/ -> bus.writeWord(state.spDec2(), state.hl()); // ZUM(116) HTP(379-380)
            case 0xE6/*AND n*/ -> and_n(bus.readMemory(state.pcInc1())); // ZUM(152-153) HTP(209-210)
            case 0xE7/*RST 20H*/ -> rst_n(0x20); // ZUM(267-268) HTP(418-419)
            case 0xE8/*RET PE*/ -> ret_cc(state.pf()); // ZUM(261-262) HTP(390-391)
            case 0xE9/*JP (HL)*/ -> state.pc(state.hl()); // ZUM(250) HTP(285)
            case 0xEA/*JP PE,nn*/ -> jp_cc(state.pf()); // ZUM(239-240) HTP(282-283)
            case 0xEB/*EX DE,HL*/ -> ex_de_hl(); // ZUM(122) HTP(249)
            case 0xEC/*CALL PE,nn*/ -> call_cc(state.pf()); // ZUM(257-259) HTP(219-221)
            case 0xED/*[0xED] ...*/ -> decode_0xED_00uFF(bus.readMemory(state.pcInc1()));
            case 0xEE/*XOR n*/ -> xor_n(bus.readMemory(state.pcInc1()));
            case 0xEF/*RST 28H*/ -> rst_n(0x28); // ZUM(267-268) HTP(418-419)
            case 0xF0/*RET P*/ -> ret_cc(!state.sf()); // ZUM(261-262) HTP(390-391)
            case 0xF1/*POP AF*/ -> state.af(bus.readWord(state.spInc2())); // ZUM(119) HTP(373-374)
            case 0xF2/*JP P,nn*/ -> jp_cc(!state.sf()); // ZUM(239-240) HTP(282-283)
            case 0xF3/*DI*/ -> di(); // ZUM(174) HTP(244)
            case 0xF4/*CALL P,nn*/ -> call_cc(!state.sf()); // ZUM(257-259) HTP(219-221)
            case 0xF5/*PUSH AF*/ -> bus.writeWord(state.spDec2(), state.af()); // ZUM(116) HTP(379-380)
            case 0xF6/*OR n*/ -> or_n(bus.readMemory(state.pcInc1()));
            case 0xF7/*RST 30H*/ -> rst_n(0x30); // ZUM(267-268) HTP(418-419)
            case 0xF8/*RET M*/ -> ret_cc(state.sf()); // ZUM(261-262) HTP(390-391)
            case 0xF9/*LD SP,HL*/ -> state.sp(state.hl()); // ZUM(113) HTP(345)
            case 0xFA/*JP M,nn*/ -> jp_cc(state.sf()); // ZUM(239-240) HTP(282-283)
            case 0xFB/*EI*/ -> ei(); // ZUM(175) HTP(247)
            case 0xFC/*CALL M,nn*/ -> call_cc(state.sf()); // ZUM(257-259) HTP(219-221)
            case 0xFD/*[0xFD] ...*/ -> decode_0xFD_00uFF(bus.readMemory(state.pcInc1()));
            case 0xFE/*CP n*/ -> cp_n(bus.readMemory(state.pcInc1()));
            case 0xFF/*RST 38H*/ -> rst_n(0x38); // ZUM(267-268) HTP(418-419)
        }
    }

    // ---------- 0xCB prefix: shift and rotate group ----------

    private void decode_0xCB_00u3F(int opCode) {
        switch (opCode) {
            case 0x00/*RLC B*/ -> state.b(rlc_n(state.b())); // ZUM(194-195) HTP(400-401)
            case 0x01/*RLC C*/ -> state.c(rlc_n(state.c())); // ZUM(194-195) HTP(400-401)
            case 0x02/*RLC D*/ -> state.d(rlc_n(state.d())); // ZUM(194-195) HTP(400-401)
            case 0x03/*RLC E*/ -> state.e(rlc_n(state.e())); // ZUM(194-195) HTP(400-401)
            case 0x04/*RLC H*/ -> state.h(rlc_n(state.h())); // ZUM(194-195) HTP(400-401)
            case 0x05/*RLC L*/ -> state.l(rlc_n(state.l())); // ZUM(194-195) HTP(400-401)
            case 0x06/*RLC (HL)*/ -> bus.writeMemory(state.hl(), rlc_n(bus.readMemory(state.hl()))); // ZUM(196-197) HTP(402-403)
            case 0x07/*RLC A*/ -> state.a(rlc_n(state.a())); // ZUM(194-195) HTP(400-401)
            case 0x08/*RRC B*/ -> state.b(rrc_n(state.b())); // ZUM(205-207) HTP(413-414)
            case 0x09/*RRC C*/ -> state.c(rrc_n(state.c())); // ZUM(205-207) HTP(413-414)
            case 0x0A/*RRC D*/ -> state.d(rrc_n(state.d())); // ZUM(205-207) HTP(413-414)
            case 0x0B/*RRC E*/ -> state.e(rrc_n(state.e())); // ZUM(205-207) HTP(413-414)
            case 0x0C/*RRC H*/ -> state.h(rrc_n(state.h())); // ZUM(205-207) HTP(413-414)
            case 0x0D/*RRC L*/ -> state.l(rrc_n(state.l())); // ZUM(205-207) HTP(413-414)
            case 0x0E/*RRC (HL)*/ -> bus.writeMemory(state.hl(), rrc_n(bus.readMemory(state.hl()))); // ZUM(205-207) HTP(413-414)
            case 0x0F/*RRC A*/ -> state.a(rrc_n(state.a())); // ZUM(205-207) HTP(413-414)
            case 0x10/*RL B*/ -> state.b(rl_n(state.b())); // ZUM(202-204) HTP(396-397)
            case 0x11/*RL C*/ -> state.c(rl_n(state.c())); // ZUM(202-204) HTP(396-397)
            case 0x12/*RL D*/ -> state.d(rl_n(state.d())); // ZUM(202-204) HTP(396-397)
            case 0x13/*RL E*/ -> state.e(rl_n(state.e())); // ZUM(202-204) HTP(396-397)
            case 0x14/*RL H*/ -> state.h(rl_n(state.h())); // ZUM(202-204) HTP(396-397)
            case 0x15/*RL L*/ -> state.l(rl_n(state.l())); // ZUM(202-204) HTP(396-397)
            case 0x16/*RL (HL)*/ -> bus.writeMemory(state.hl(), rl_n(bus.readMemory(state.hl()))); // ZUM(202-204) HTP(396-397)
            case 0x17/*RL A*/ -> state.a(rl_n(state.a())); // ZUM(202-204) HTP(396-397)
            case 0x18/*RR B*/ -> state.b(rr_n(state.b())); // ZUM(208-210) HTP(410-411)
            case 0x19/*RR C*/ -> state.c(rr_n(state.c())); // ZUM(208-210) HTP(410-411)
            case 0x1A/*RR D*/ -> state.d(rr_n(state.d())); // ZUM(208-210) HTP(410-411)
            case 0x1B/*RR E*/ -> state.e(rr_n(state.e())); // ZUM(208-210) HTP(410-411)
            case 0x1C/*RR H*/ -> state.h(rr_n(state.h())); // ZUM(208-210) HTP(410-411)
            case 0x1D/*RR L*/ -> state.l(rr_n(state.l())); // ZUM(208-210) HTP(410-411)
            case 0x1E/*RR (HL)*/ -> bus.writeMemory(state.hl(), rr_n(bus.readMemory(state.hl()))); // ZUM(208-210) HTP(410-411)
            case 0x1F/*RR A*/ -> state.a(rr_n(state.a())); // ZUM(208-210) HTP(410-411)
            case 0x20/*SLA B*/ -> state.b(sla_n(state.b())); // ZUM(211-213) HTP(428-429)
            case 0x21/*SLA C*/ -> state.c(sla_n(state.c())); // ZUM(211-213) HTP(428-429)
            case 0x22/*SLA D*/ -> state.d(sla_n(state.d())); // ZUM(211-213) HTP(428-429)
            case 0x23/*SLA E*/ -> state.e(sla_n(state.e())); // ZUM(211-213) HTP(428-429)
            case 0x24/*SLA H*/ -> state.h(sla_n(state.h())); // ZUM(211-213) HTP(428-429)
            case 0x25/*SLA L*/ -> state.l(sla_n(state.l())); // ZUM(211-213) HTP(428-429)
            case 0x26/*SLA (HL)*/ -> bus.writeMemory(state.hl(), sla_n(bus.readMemory(state.hl()))); // ZUM(211-213) HTP(428-429)
            case 0x27/*SLA A*/ -> state.a(sla_n(state.a())); // ZUM(211-213) HTP(428-429)
            case 0x28/*SRA B*/ -> state.b(sra_n(state.b())); // ZUM(214-216) HTP(430-431)
            case 0x29/*SRA C*/ -> state.c(sra_n(state.c())); // ZUM(214-216) HTP(430-431)
            case 0x2A/*SRA D*/ -> state.d(sra_n(state.d())); // ZUM(214-216) HTP(430-431)
            case 0x2B/*SRA E*/ -> state.e(sra_n(state.e())); // ZUM(214-216) HTP(430-431)
            case 0x2C/*SRA H*/ -> state.h(sra_n(state.h())); // ZUM(214-216) HTP(430-431)
            case 0x2D/*SRA L*/ -> state.l(sra_n(state.l())); // ZUM(214-216) HTP(430-431)
            case 0x2E/*SRA (HL)*/ -> bus.writeMemory(state.hl(), sra_n(bus.readMemory(state.hl()))); // ZUM(214-216) HTP(430-431)
            case 0x2F/*SRA A*/ -> state.a(sra_n(state.a())); // ZUM(214-216) HTP(430-431)
            case 0x30/*SL1 B*/ -> state.b(sl1_n(state.b())); // undocumented
            case 0x31/*SL1 C*/ -> state.c(sl1_n(state.c())); // undocumented
            case 0x32/*SL1 D*/ -> state.d(sl1_n(state.d())); // undocumented
            case 0x33/*SL1 E*/ -> state.e(sl1_n(state.e())); // undocumented
            case 0x34/*SL1 H*/ -> state.h(sl1_n(state.h())); // undocumented
            case 0x35/*SL1 L*/ -> state.l(sl1_n(state.l())); // undocumented
            case 0x36/*SL1 (HL)*/ -> bus.writeMemory(state.hl(), sl1_n(bus.readMemory(state.hl()))); // undocumented
            case 0x37/*SL1 A*/ -> state.a(sl1_n(state.a())); // undocumented
            case 0x38/*SRL B*/ -> state.b(srl_n(state.b())); // ZUM(217-219) HTP(432-433)
            case 0x39/*SRL C*/ -> state.c(srl_n(state.c())); // ZUM(217-219) HTP(432-433)
            case 0x3A/*SRL D*/ -> state.d(srl_n(state.d())); // ZUM(217-219) HTP(432-433)
            case 0x3B/*SRL E*/ -> state.e(srl_n(state.e())); // ZUM(217-219) HTP(432-433)
            case 0x3C/*SRL H*/ -> state.h(srl_n(state.h())); // ZUM(217-219) HTP(432-433)
            case 0x3D/*SRL L*/ -> state.l(srl_n(state.l())); // ZUM(217-219) HTP(432-433)
            case 0x3E/*SRL (HL)*/ -> bus.writeMemory(state.hl(), srl_n(bus.readMemory(state.hl()))); // ZUM(217-219) HTP(432-433)
            case 0x3F/*SRL A*/ -> state.a(srl_n(state.a())); // ZUM(217-219) HTP(432-433)
            default -> unrecognizedOpCode(0xCB, opCode);
        }
    }

    private void decode_0xCB_40u7F(int opCode) {
        int y = (opCode & 0x38) >>> 3;
        int z = (opCode & 0x07);
        switch (z) {
            case 0x00/*BIT b,B*/ -> bit_b_n(y, state.b()); // ZUM(224-225) HTP(217-218)
            case 0x01/*BIT b,C*/ -> bit_b_n(y, state.c()); // ZUM(224-225) HTP(217-218)
            case 0x02/*BIT b,D*/ -> bit_b_n(y, state.d()); // ZUM(224-225) HTP(217-218)
            case 0x03/*BIT b,E*/ -> bit_b_n(y, state.e()); // ZUM(224-225) HTP(217-218)
            case 0x04/*BIT b,H*/ -> bit_b_n(y, state.h()); // ZUM(224-225) HTP(217-218)
            case 0x05/*BIT b,L*/ -> bit_b_n(y, state.l()); // ZUM(224-225) HTP(217-218)
            case 0x06/*BIT b,(HL)*/ -> bit_b_n(y, bus.readMemory(state.hl())); // ZUM(226-227) HTP(211-212)
            case 0x07/*BIT b,A*/ -> bit_b_n(y, state.a()); // ZUM(224-225) HTP(217-218)
        }
    }

    private void decode_0xCB_80uBF(int opCode) {
        int y = (opCode & 0x38) >>> 3;
        int z = (opCode & 0x07);
        switch (z) {
            case 0x00/*RES b,B*/ -> state.b(res_b_n(y, state.b())); // ZUM(236-237) HTP(385-387)
            case 0x01/*RES b,C*/ -> state.c(res_b_n(y, state.c())); // ZUM(236-237) HTP(385-387)
            case 0x02/*RES b,D*/ -> state.d(res_b_n(y, state.d())); // ZUM(236-237) HTP(385-387)
            case 0x03/*RES b,E*/ -> state.e(res_b_n(y, state.e())); // ZUM(236-237) HTP(385-387)
            case 0x04/*RES b,H*/ -> state.h(res_b_n(y, state.h())); // ZUM(236-237) HTP(385-387)
            case 0x05/*RES b,L*/ -> state.l(res_b_n(y, state.l())); // ZUM(236-237) HTP(385-387)
            case 0x06/*RES b,(HL)*/ -> bus.writeMemory(state.hl(), res_b_n(y, bus.readMemory(state.hl()))); // ZUM(236-237) HTP(385-387)
            case 0x07/*RES b,A*/ -> state.a(res_b_n(y, state.a())); // ZUM(236-237) HTP(385-387)
        }
    }

    private void decode_0xCB_C0uFF(int opCode) {
        int y = (opCode & 0x38) >>> 3;
        int z = (opCode & 0x07);
        switch (z) {
            case 0x00/*SET b,B*/ -> state.b(set_b_n(y, state.b())); // ZUM(232) HTP(425-427)
            case 0x01/*SET b,C*/ -> state.c(set_b_n(y, state.c())); // ZUM(232) HTP(425-427)
            case 0x02/*SET b,D*/ -> state.d(set_b_n(y, state.d())); // ZUM(232) HTP(425-427)
            case 0x03/*SET b,E*/ -> state.e(set_b_n(y, state.e())); // ZUM(232) HTP(425-427)
            case 0x04/*SET b,H*/ -> state.h(set_b_n(y, state.h())); // ZUM(232) HTP(425-427)
            case 0x05/*SET b,L*/ -> state.l(set_b_n(y, state.l())); // ZUM(232) HTP(425-427)
            case 0x06/*SET b,(HL)*/ -> bus.writeMemory(state.hl(), set_b_n(y, bus.readMemory(state.hl()))); // ZUM(233) HTP(425-427)
            case 0x07/*SET b,A*/ -> state.a(set_b_n(y, state.a())); // ZUM(232) HTP(425-427)
        }
    }

    // ---------- 0xED prefix: miscellaneous extended instruction group ----------

    private void decode_0xED_00u3F(int opCode) {
        unrecognizedOpCode(0xED, opCode);
    }

    private void decode_0xED_40u7F(int opCode) {
        switch (opCode) {
            case 0x40/*IN B,(C)*/ -> state.b(bus.readIoPort(state.bc())); // ZUM(270-271) HTP(261-262)
            case 0x41/*OUT (C),B*/ -> bus.writeIoPort(state.bc(), state.b()); // ZUM(280-281) HTP(366-367)
            case 0x42/*SBC HL,BC*/ -> sbc_hl_nn(state.bc()); // ZUM(181) HTP(422-423)
            case 0x43/*LD (nn),BC*/ -> bus.writeWord(bus.readWord(state.pcInc2()), state.bc()); // ZUM(110) HTP(321-322)
            case 0x44/*NEG*/ -> neg(); // ZUM(169) HTP(358)
            case 0x45/*RETN*/ -> retn(); // ZUM(265-266) HTP(394-395)
            case 0x46/*IM 0*/ -> im0(); // ZUM(176) HTP(258)
            case 0x47/*LD I,A*/ -> {/*TODO ld_i_a();*/} // ZUM(100) HTP(332)
            case 0x48/*IN C,(C)*/ -> state.c(bus.readIoPort(state.bc())); // ZUM(270-271) HTP(261-262)
            case 0x49/*OUT (C),C*/ -> bus.writeIoPort(state.bc(), state.c()); // ZUM(280-281) HTP(366-367)
            case 0x4A/*ADC HL,BC*/ -> adc_hl_nn(state.bc()); // ZUM(180) HTP(192-193)
            case 0x4B/*LD BC,(nn)*/ -> state.bc(bus.readWord(state.pcInc2())); // ZUM(106) HTP(291-292)
            case 0x4D/*RETI*/ -> reti(); // ZUM(263-264) HTP(392-393)
            case 0x4F/*LD R,A*/ -> {/*TODO ld_r_a();*/} // ZUM(101) HTP(344)
            case 0x50/*IN D,(C)*/ -> state.d(bus.readIoPort(state.bc())); // ZUM(270-271) HTP(261-262)
            case 0x51/*OUT (C),D*/ -> bus.writeIoPort(state.bc(), state.d()); // ZUM(280-281) HTP(366-367)
            case 0x52/*SBC HL,DE*/ -> sbc_hl_nn(state.de()); // ZUM(181) HTP(422-423)
            case 0x53/*LD (nn),DE*/ -> bus.writeWord(bus.readWord(state.pcInc2()), state.de()); // ZUM(110) HTP(321-322)
            case 0x56/*IM 1*/ -> im1(); // ZUM(177) HTP(259)
            case 0x57/*LD A,I*/ -> {/*TODO ld_a_i();*/} // ZUM(98) HTP(331)
            case 0x58/*IN E,(C)*/ -> state.e(bus.readIoPort(state.bc())); // ZUM(270-271) HTP(261-262)
            case 0x59/*OUT (C),E*/ -> bus.writeIoPort(state.bc(), state.e()); // ZUM(280-281) HTP(366-367)
            case 0x5A/*ADC HL,DE*/ -> adc_hl_nn(state.de()); // ZUM(180) HTP(192-193)
            case 0x5B/*LD DE,(nn)*/ -> state.de(bus.readWord(state.pcInc2())); // ZUM(106) HTP(291-292)
            case 0x5E/*IM 2*/ -> im2(); // ZUM(178) HTP(260)
            case 0x5F/*LD A,R*/ -> {/*TODO ld_a_r();*/} // ZUM(99) HTP(333)
            case 0x60/*IN H,(C)*/ -> state.h(bus.readIoPort(state.bc())); // ZUM(270-271) HTP(261-262)
            case 0x61/*OUT (C),H*/ -> bus.writeIoPort(state.bc(), state.h()); // ZUM(280-281) HTP(366-367)
            case 0x62/*SBC HL,HL*/ -> sbc_hl_nn(state.hl()); // ZUM(181) HTP(422-423)
            case 0x63/*LD (nn),HL*/ -> bus.writeWord(bus.readWord(state.pcInc2()), state.hl()); // ZUM(110) HTP(321-322)
            case 0x67/*RRD*/ -> rrd(); // ZUM(222-223) HTP(416-417)
            case 0x68/*IN L,(C)*/ -> state.l(bus.readIoPort(state.bc())); // ZUM(270-271) HTP(261-262)
            case 0x69/*OUT (C),L*/ -> bus.writeIoPort(state.bc(), state.l()); // ZUM(280-281) HTP(366-367)
            case 0x6A/*ADC HL,HL*/ -> adc_hl_nn(state.hl()); // ZUM(180) HTP(192-193)
            case 0x6B/*LD HL,(nn)*/ -> state.hl(bus.readWord(state.pcInc2())); // ZUM(106) HTP(291-292)
            case 0x6F/*RLD*/ -> rld(); // ZUM(220-221) HTP(408-409)
            case 0x72/*SBC HL,SP*/ -> sbc_hl_nn(state.sp()); // ZUM(181) HTP(422-423)
            case 0x73/*LD (nn),SP*/ -> bus.writeWord(bus.readWord(state.pcInc2()), state.sp()); // ZUM(110) HTP(321-322)
            case 0x78/*IN A,(C)*/ -> state.a(bus.readIoPort(state.bc())); // ZUM(270-271) HTP(261-262)
            case 0x79/*OUT (C),A*/ -> bus.writeIoPort(state.bc(), state.a()); // ZUM(280-281) HTP(366-367)
            case 0x7A/*ADC HL,SP*/ -> adc_hl_nn(state.sp()); // ZUM(180) HTP(192-193)
            case 0x7B/*LD SP,(nn)*/ -> state.sp(bus.readWord(bus.readWord(state.pcInc2()))); // ZUM(106) HTP(291-292)
            default -> unrecognizedOpCode(0xED, opCode);
        }
    }

    private void decode_0xED_80uBF(int opCode) {
        switch (opCode) {
            case 0xA0/*LDI*/ -> ldi(); // ZUM(128) HTP(352-353)
            case 0xA1/*CPI*/ -> cpi(); // ZUM(134) HTP(231-232)
            case 0xA2/*INI*/ -> ini(); // ZUM(272) HTP(278-279)
            case 0xA3/*OUTI*/ -> outi(); // ZUM(282) HTP(371-372)
            case 0xA8/*LDD*/ -> ldd(); // ZUM(131) HTP(348-349)
            case 0xA9/*CPD*/ -> cpd(); // ZUM(137) HTP(227-228)
            case 0xAA/*IND*/ -> ind(); // ZUM(275-276) HTP(274-275)
            case 0xAB/*OUTD*/ -> outd(); // ZUM(285) HTP(369-370)
            case 0xB0/*LDIR*/ -> ldir(); // ZUM(129-130) HTP(354-355)
            case 0xB1/*CPIR*/ -> cpir(); // ZUM(135-136) HTP(233-234)
            case 0xB2/*INIR*/ -> inir(); // ZUM(273-274) HTP(280-281)
            case 0xB3/*OTIR*/ -> otir(); // ZUM(283-284) HTP(364-365)
            case 0xB8/*LDDR*/ -> lddr(); // ZUM(132-133) HTP(350-351)
            case 0xB9/*CPDR*/ -> cpdr(); // ZUM(138-139) HTP(229-230)
            case 0xBA/*INDR*/ -> indr(); // ZUM(277-278) HTP(276-277)
            case 0xBB/*OTDR*/ -> otdr(); // ZUM(286-287) HTP(362-363)
            default -> unrecognizedOpCode(0xED, opCode);
        }
    }

    private void decode_0xED_C0uFF(int opCode) {
        unrecognizedOpCode(0xED, opCode);
    }

    // ---------- 0xDD prefix: IX-indexed instruction group ----------

    private void decode_0xDD_00u3F(int opCode) {
        // TODO implementation
        switch (opCode) {
            case 0x21/*LD IX,nn*/ -> state.ix(bus.readWord(state.pcInc2())); // ZUM(103) HTP(336-337)
            case 0x23/*INC IX*/ -> state.ix(inc_nn(state.ix())); // ZUM(185) HTP(272)
            default -> unrecognizedOpCode(0xDD, opCode);
        }
    }

    private void decode_0xDD_40u7F(int opCode) {
        // TODO implementation
        switch (opCode) {
            case 0x7E/*LD A,(IX+n)*/ -> state.a(bus.readMemory(indexed(state.ix()))); // ZUM(84) HTP(305-306)
            default -> unrecognizedOpCode(0xDD, opCode);
        }
    }

    private void decode_0xDD_80uBF(int opCode) {
        // TODO implementation
        switch (opCode) {
            default -> unrecognizedOpCode(0xDD, opCode);
        }
    }

    private void decode_0xDD_C0uFF(int opCode) {
        // TODO implementation
        switch (opCode) {
            case 0xE1/*POP IX*/ -> state.ix(bus.readWord(state.spInc2())); // ZUM(120) HTP(375-376)
            case 0xE5/*PUSH IX*/ -> bus.writeWord(state.spDec2(), state.ix()); // ZUM(117) HTP(381-382)
            case 0xE9/*JP (IX)*/ -> state.pc(state.ix()); // ZUM(251) HTP(286)
            default -> unrecognizedOpCode(0xDD, opCode);
        }
    }

    // ---------- 0xFD prefix: IY-indexed instruction group ----------

    private void decode_0xFD_00u3F(int opCode) {
        // TODO implementation
        switch (opCode) {
            case 0x21/*LD IY,nn*/ -> state.iy(bus.readWord(state.pcInc2())); // ZUM(104) HTP(340-341)
            case 0x23/*INC IY*/ -> state.iy(inc_nn(state.iy())); // ZUM(186) HTP(273)
            default -> unrecognizedOpCode(0xFD, opCode);
        }
    }

    private void decode_0xFD_40u7F(int opCode) {
        // TODO implementation
        switch (opCode) {
            case 0x7E/*LD A,(IY+n)*/ -> state.a(bus.readMemory(indexed(state.iy()))); // ZUM(85) HTP(307-308)
            default -> unrecognizedOpCode(0xFD, opCode);
        }
    }

    private void decode_0xFD_80uBF(int opCode) {
        // TODO implementation
        switch (opCode) {
            default -> unrecognizedOpCode(0xFD, opCode);
        }
    }

    private void decode_0xFD_C0uFF(int opCode) {
        // TODO implementation
        switch (opCode) {
            case 0xE1/*POP IY*/ -> state.iy(bus.readWord(state.spInc2())); // ZUM(121) HTP(377-378)
            case 0xE5/*PUSH IY*/ -> bus.writeWord(state.spDec2(), state.iy()); // ZUM(118) HTP(383-384)
            case 0xE9/*JP (IY)*/ -> state.pc(state.iy()); // ZUM(252) HTP(287)
            default -> unrecognizedOpCode(0xFD, opCode);
        }
    }

    // ---------- Instruction implementation functions ----------

    private void ex_de_hl() { // ZUM(122) HTP(249)
        int de = state.de();
        state.de(state.hl());
        state.hl(de);
    }

    private void ex_af_aaf() { // ZUM(123) HTP(248)
        state.alt_af();
    }

    private void exx() { // ZUM(124) HTP(256)
        state.alt_bcdehl();
    }

    private void ex_csp_hl() { // ZUM(125) HTP(250-251)
        int hl = state.hl();
        state.hl(bus.readWord(state.sp()));
        bus.writeWord(state.sp(), hl);
    }

    private void ldi() { // ZUM(128) HTP(352-353)
        bus.writeMemory(state.de(), bus.readMemory(state.hl()));
        state.de(state.de() + 1);
        state.hl(state.hl() + 1);
        state.bc(state.bc() - 1);
        state.nf(false);
        state.pf(state.bc() != 0);
        state.hf(false);
    }
    private void ldir() { // ZUM(129-130) HTP(354-355)
        ldi();
        if (state.pf()) {
            state.pc(state.pc()-2); // repeat the instruction.
        }
    }
    private void ldd() { // ZUM(131) HTP(348-349)
        bus.writeMemory(state.de(), bus.readMemory(state.hl()));
        state.de(state.de() - 1);
        state.hl(state.hl() - 1);
        state.bc(state.bc() - 1);
        state.nf(false);
        state.pf(state.bc() != 0);
        state.hf(false);
    }
    private void lddr() { // ZUM(132-133) HTP(350-351)
        ldd();
        if (state.pf()) {
            state.pc(state.pc()-2); // repeat the instruction.
        }
    }
    private void cpi() { // ZUM(134) HTP(231-232)
        int rm = (state.a() - bus.readMemory(state.hl())) & 0xFF;
        state.hl(state.hl() + 1);
        state.bc(state.bc() - 1);
        state.pf(state.bc() != 0);
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
    }
    private void cpir() { // ZUM(135-136) HTP(233-234)
        cpi();
        if (!state.zf() && state.pf()) {
            state.pc(state.pc()-2); // repeat the instruction.
        }
    }
    private void cpd() { // ZUM(137) HTP(227-228)
        int ru = state.a() - bus.readMemory(state.hl());
        int rm = ru & 0xFF;
        state.hl(state.hl() - 1);
        state.bc(state.bc() - 1);

        state.pf(state.bc() != 0);
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
    }
    private void cpdr() { // ZUM(138-139) HTP(229-230)
        cpd();
        if (!state.zf() && state.pf()) {
            state.pc(state.pc()-2); // repeat the instruction.
        }
    }

    private void add_a_n(int n) { // ZUM(140-145) HTP(194-202)
        int ru = state.a() + n;
        int rm = ru & 0xFF;
        state.cf(rm != ru);
        state.nf(false);
        state.pf(rm != ru);
        state.hf((state.a() & 0xF) + (n & 0xF) >= 0x10);
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        state.a(rm);
    }

    private void adc_a_n(int n) { // ZUM(146-147) HTP(190-191)
        int ru = state.a() + n + (state.cf() ? 1 : 0);
        int rm = ru & 0xFF;
        state.cf(rm != ru);
        state.nf(false);
        state.pf(rm != ru);
        state.hf((state.a() & 0xF) + (n & 0xF) + (state.cf() ? 1: 0) >= 0x10);
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        state.a(rm);
    }

    private void sub_n(int n) { // ZUM(148-149) HTP(434-435)
        int ru = state.a() - n;
        int rm = ru & 0xFF;
        state.cf(rm != ru);
        state.nf(true);
        state.pf(rm != ru);
        state.hf((state.a() & 0xF) < (n & 0xF));
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        state.a(rm);
    }

    private void sbc_a_n(int n) { // ZUM(150-151) HTP(420-421)
        int ru = state.a() - n - (state.cf() ? 1 : 0);
        int rm = ru & 0xFF;
        state.cf(rm != ru);
        state.nf(true);
        state.pf(rm != ru);
        state.hf((state.a() & 0xF) < (n & 0xF) + (state.cf() ? 1 : 0));
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        state.a(rm);
    }

    private void and_n(int n) { // ZUM(152-153) HTP(209-210)
        int rm = state.a() & n;
        state.cf(false);
        state.nf(false);
        state.pf(parity(rm));
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        state.a(rm);
    }

    private void or_n(int n) { // ZUM(154-155) HTP(360-361)
        int rm = state.a() | n;
        state.cf(false);
        state.nf(false);
        state.pf(parity(rm));
        state.hf(false);
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        state.a(rm);
    }

    private void xor_n(int n) { // ZUM(156-157) HTP(436-437)
        int rm = state.a() ^ n;
        state.cf(false);
        state.nf(false);
        state.pf(parity(rm));
        state.hf(false);
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        state.a(rm);
    }

    private void cp_n(int n) { // ZUM(158-159) HTP(225-226)
        int ru = state.a() - n;
        int rm = ru & 0xFF;
        state.cf(rm != ru);
        state.nf(true);
        state.pf(rm != ru);
        state.hf((state.a() & 0xF) < (n & 0xF));
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
    }

    private int inc_n(int n) { // ZUM(160-163) HTP(264,267-271)
        int rm = (n + 1) & 0xFF;
        state.nf(false);
        state.pf(n == 0x7F); // 0x7F -> 0x80 is overflow
        state.hf((n & 0x0F) == 0x0F);
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        return rm;
    }

    private int dec_n(int n) { // ZUM(164-165) HTP(238-239)
        int rm = (n - 1) & 0xFF;
        state.nf(true);
        state.pf(n == 0x80); // 0x80 -> 0x7F is overflow
        state.hf((n & 0x0F) == 0x00);
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        return rm;
    }

    private void daa() { // ZUM(166-167) HTP(236-237)
        // TODO implementation
    }

    private void cpl() { // ZUM(168) HTP(235)
        state.nf(true);
        state.hf(true);
        state.a(state.a() ^ 0xFF);
    }

    private void neg() { // ZUM(169) HTP(358)
        int ru = 0x100 - state.a();
        int rm = ru & 0xFF;
        state.cf(rm != 0x00);
        state.nf(true);
        state.pf(rm == 0x80); // i.e. state.a() == 0x80 for overflow
        state.hf((state.a() & 0x0F) != 0x00);
        state.zf(rm == 0x00);
        state.sf((rm & 0x80) != 0);
        state.a(rm);
    }

    private void ccf() { // ZUM(170) HTP(224)
        state.cf(!state.cf());
        state.nf(false);
    }

    private void scf() { // ZUM(171) HTP(424)
        state.cf(true);
        state.nf(false);
        state.hf(false);
    }

    private void di() { // ZUM(174) HTP(244)
        // TODO implementation
    }

    private void ei() { // ZUM(175) HTP(247)
        // TODO implementation
    }

    private void im0() { // ZUM(176) HTP(258)
        // TODO implementation
    }

    private void im1() { // ZUM(177) HTP(259)
        // TODO implementation
    }

    private void im2() { // ZUM(178) HTP(260)
        // TODO implementation
    }

    private void add_hl_nn(int nn) { // ZUM(179) HTP(203-204)
        int ru = state.hl() + nn;
        int rm = ru & 0xFFFF;
        state.cf(rm != ru);
        state.nf(false);
        state.hl(rm);
    }

    private void adc_hl_nn(int nn) { // ZUM(180) HTP(192-193)
        int ru = state.hl() + nn + (state.cf() ? 1 : 0);
        int rm = ru & 0xFFFF;
        state.cf(rm != ru);
        state.nf(false);
        state.pf(rm != ru);
        state.hf((state.hl() & 0x0FFF) + (nn & 0x0FFF) + (state.cf() ? 1 : 0) >= 0x1000);
        state.zf(rm == 0);
        state.sf((rm & 0x8000) != 0);
        state.hl(rm);
    }

    private void sbc_hl_nn(int nn) { // ZUM(181) HTP(422-423)
        int ru = state.hl() - nn - (state.cf() ? 1 : 0);
        int rm = ru & 0xFFFF;
        state.cf(rm != ru);
        state.nf(true);
        state.pf(rm != ru);
        state.hf((state.hl() & 0x0FFF) < (nn & 0x0FFF) + (state.cf() ? 1 : 0));
        state.zf(rm == 0);
        state.sf((rm & 0x8000) != 0);
        state.hl(rm);
    }

    private int inc_nn(int nn) { // ZUM(184-186) HTP(265-266,272-273)
        return (nn + 1) & 0xFFFF;
    }

    private int dec_nn(int nn) { // ZUM(187-189) HTP(240-243)
        return (nn - 1) & 0xFFFF;
    }

    private void rlca() { // ZUM(190) HTTP(399)
        boolean bit7 = (state.a() & 0x80) != 0;
        int rm = ((state.a() << 1) | (bit7 ? 1 : 0)) & 0xFF;
        state.cf(bit7);
        state.nf(false);
        state.hf(false);
        state.a(rm);
    }

    private void rla() { // ZUM(191) HTP(398)
        boolean bit7 = (state.a() & 0x80) != 0;
        int rm = ((state.a() << 1) | (state.cf() ? 1 : 0)) & 0xFF;
        state.cf(bit7);
        state.nf(false);
        state.hf(false);
        state.a(rm);
    }

    private void rrca() { // ZUM(192) HTP(415)
        boolean bit0 = (state.a() & 0x1) != 0;
        int rm = (bit0 ? 0x80 : 0) | (state.a() >>> 1);
        state.cf(bit0);
        state.nf(false);
        state.hf(false);
        state.a(rm);
    }

    private void rra() { // ZUM(193) HTP(412)
        boolean bit0 = (state.a() & 0x1) != 0;
        int rm = (state.cf() ? 0x80 : 0) | (state.a() >>> 1);
        state.cf(bit0);
        state.nf(false);
        state.hf(false);
        state.a(rm);
    }

    private int rlc_n(int n) { // ZUM(194-201) HTP(400-407)
        boolean bit7 = (n & 0x80) != 0;
        int rm = ((n << 1) | (bit7 ? 1 : 0)) & 0xFF;
        state.cf(bit7);
        state.nf(false);
        state.pf(parity(rm));
        state.hf(false);
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        return rm;
    }

    private int rl_n(int n) { // ZUM(202-204) HTP(396-397)
        boolean bit7 = (n & 0x80) != 0;
        int rm = ((n << 1) | (state.cf() ? 1 : 0)) & 0xFF;
        state.cf(bit7);
        state.nf(false);
        state.pf(parity(rm));
        state.hf(false);
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        return rm;
    }

    private int rrc_n(int n) { // ZUM(205-207) HTP(413-414)
        boolean bit0 = (n & 0x1) != 0;
        int rm = (bit0 ? 0x80 : 0) | (n >>> 1);
        state.cf(bit0);
        state.nf(false);
        state.pf(parity(rm));
        state.hf(false);
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        return rm;
    }

    private int rr_n(int n) { // ZUM(208-210) HTTP(410-411)
        boolean bit0 = (n & 0x1) != 0;
        int rm = (state.cf() ? 0x80 : 0) | (n >>> 1);
        state.cf(bit0);
        state.nf(false);
        state.pf(parity(rm));
        state.hf(false);
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        return rm;
    }

    private int sla_n(int n) { // ZUM(211-213) HTP(428-429)
        boolean bit7 = (n & 0x80) != 0;
        int rm = (n << 1) & 0xFF;
        state.cf(bit7);
        state.nf(false);
        state.pf(parity(rm));
        state.hf(false);
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        return rm;
    }

    private int sra_n(int n) { // ZUM(214-216) HTP(430-431)
        boolean bit7 = (n & 0x80) != 0;
        boolean bit0 = (n & 0x01) != 0;
        int rm = (bit7 ? 0x80 : 0) | (n >>> 1) & 0xFF;
        state.cf(bit0);
        state.nf(false);
        state.pf(parity(rm));
        state.hf(false);
        state.zf(rm == 0);
        state.sf(bit7);
        return rm;
    }

    private int sl1_n(int n) { // undocumented
        boolean bit7 = (n & 0x80) != 0;
        int rm = ((n << 1) & 0xFF) | 0x01; // the `| 0x01` is why it's undocumented !!
        state.cf(bit7);
        state.nf(false);
        state.pf(parity(rm));
        state.hf(false);
        state.zf(false);
        state.sf((rm & 0x80) != 0);
        return rm;
    }

    private int srl_n(int n) { // ZUM(217-219) HTP(432-433)
        boolean bit0 = (n & 0x01) != 0;
        int rm = (n >>> 1) & 0xFF;
        state.cf(bit0);
        state.nf(false);
        state.pf(parity(rm));
        state.hf(false);
        state.zf(rm == 0);
        state.sf(false);
        return rm;
    }

    private void rld() { // ZUM(220-221) HTP(408-409)
        // TODO implementation
        state.nf(false);
        //state.pf(...) // TODO update pf with parity
        state.hf(false);
    }

    private void rrd() { // ZUM(222-223) HTP(416-417)
        // TODO implementation
        state.nf(false);
        //state.pf(...) // TODO update pf with parity
        state.hf(false);
    }

    private void bit_b_n(int b, int n) { // ZUM(224-231) HTP(211-218)
        boolean test = (n & (0x01 << (b & 0x07))) != 0;
        state.nf(false);
        state.pf(!test);
        state.hf(true);
        state.zf(!test);
        state.sf(b == 7 && test);
    }

    private int set_b_n(int b, int n) { // ZUM(232-235) HTP(425-427)
        return n | (1 << b);
    }

    private int res_b_n(int b, int n) { // ZUM(236-237) HTP(385-387)
        return n & ~(1 << b);
    }

    private void jp() { // ZUM(238) HTP(284)
        state.pc(bus.readWord(state.pc()));
    }

    private void jp_cc(boolean cond) { // ZUM(239-240) HTP(282-283)
        if (cond) { jp(); } else { state.pcInc2(); }
    }

    private void jr() { // ZUM(241) HTP(290)
        state.pc(indexed(state.pc() + 1));
    }

    private void jr_cc(boolean cond) { // ZUM(242-249) HTP(288-289
        if (cond) { jr(); } else { state.pcInc1(); }
    }

    private void djnz() { // ZUM(253-254) HTP(245-246)
        state.b(state.b() - 1);
        if (state.b() != 0) { jr(); } else { state.pcInc1(); }
    }

    private void call() { // ZUM(255-256) HTP(222-223)
        int address = bus.readWord(state.pcInc2());
        bus.writeWord(state.spDec2(), state.pc());
        state.pc(address);
    }

    private void call_cc(boolean cond) { // ZUM(257-259) HTP(219-221)
        if (cond) { call(); } else { state.pcInc2(); }
    }

    private void ret() { // ZUM(260) HTP(388-389)
        state.pc(bus.readWord(state.spInc2()));
    }

    private void ret_cc(boolean cond) { // ZUM(261-262) HTP(390-391)
        if (cond) { ret(); }
    }

    private void reti() { // ZUM(263-264) HTP(392-393)
        // TODO implementation
        ret();
    }

    private void retn() { // ZUM(265-266) HTP(394-395)
        // TODO implementation
        ret();
    }

    private void rst_n(int n) { // ZUM(267-268) HTP(418-419)
        bus.writeWord(state.spDec2(), state.pc());
        state.pc(n & 0x38);
    }

    private void ini() { // ZUM(272) HTP(278-279)
        int rm = bus.readIoPort(state.bc());
        bus.writeMemory(state.hl(), rm);
        state.b(state.b() - 1);
        state.hl(state.hl() + 1);
        state.zf(state.b() == 0);
        state.nf(false);
    }
    private void inir() { // ZUM(273-274) HTP(280-281)
        ini();
        if (!state.zf()) {
            state.pc(state.pc()-2); // repeat the instruction.
        }
    }
    private void ind() { // ZUM(275-276) HTP(274-275)
        int rm = bus.readIoPort(state.bc());
        bus.writeMemory(state.hl(), rm);
        state.b(state.b() - 1);
        state.hl(state.hl() - 1);
        state.zf(state.b() == 0);
        state.nf(true);
    }
    private void indr() { // ZUM(277-278) HTP(276-277)
        ind();
        if (!state.zf()) {
            state.pc(state.pc()-2); // repeat the instruction.
        }
    }
    private void outi() { // ZUM(282) HTP(371-372)
        bus.writeIoPort(state.bc(), bus.readMemory(state.hl()));
        state.b(state.b() - 1);
        state.hl(state.hl() + 1);
        state.zf(state.b() == 0);
        state.nf(false);
    }
    private void otir() { // ZUM(283-284) HTP(364-365)
        outi();
        if (!state.zf()) {
            state.pc(state.pc()-2); // repeat the instruction.
        }
    }
    private void outd() { // ZUM(285) HTP(369-370)
        bus.writeIoPort(state.bc(), bus.readMemory(state.hl()));
        state.b(state.b() - 1);
        state.hl(state.hl() - 1);
        state.zf(state.b() == 0);
        state.nf(true);
    }
    private void otdr() { // ZUM(286-287) HTP(362-363)
        outd();
        if (!state.zf()) {
            state.pc(state.pc()-2); // repeat the instruction.
        }
    }

    // ---------- Helper functions ----------

    /**
     * @return the address added to the next byte in memory as a signed offset (in the range -128 to +127).
     * For example, `JR n` or `DJNZ n` (address would be `PC + 1`) or `LD A,(IX+n)` (address would be `IX`) might use
     * this function during instruction decoding.
     */
    private int indexed(int nn) {
        int offset = bus.readMemory(state.pcInc1());
        return (nn + ((offset & 0x80) == 0 ? offset : offset - 0x100)) & 0xFFFF;
    }

    /**
     * @return true if n has even parity, false if n has odd parity (only rightmost 8 bits of n are considered).
     * Note this is the reverse of what you might normally expect because that is how the Z80's P/V condition bit works.
     */
    private boolean parity(int n) {
        return ((0x6996 >>> ((n ^ (n >>> 4)) & 0x0F)) & 1) == 0;
    }

    private void unrecognizedOpCode(int prefix, int opCode) {
        // TODO when have implemented the IX and IY instructions, remove 0xDD and 0xFD exclusion here.
        if (prefix != 0xDD && prefix != 0xFD) {
            System.err.printf("?0x%02x,0x%02x?  ", prefix, opCode);
        }
    }
}
