package com.davidconneely.eightbit.z80;

import com.davidconneely.eightbit.IBus;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

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
        decode_0x00vFF(bus.readInstruction(state.pcInc1()));
    }

    private void decode_0x00vFF(int opCode) {
        int qtr = (opCode & 0xC0); // values 0x00,40,80,C0
        switch (qtr) {
            case 0x00/*[0x00-0x3F]*/ -> decode_0x00v3F(opCode);
            case 0x40/*[0x40-0x7F]*/ -> decode_0x40v7F(opCode);
            case 0x80/*[0x80-0xBF]*/ -> decode_0x80vBF(opCode);
            case 0xC0/*[0xC0-0xFF]*/ -> decode_0xC0vFF(opCode);
        }
    }

    private void decode_0x00v3F(int opCode) {
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

    private void decode_0x40v7F(int opCode) {
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

    private void decode_0x80vBF(int opCode) {
        int aop = (opCode & 0x38) >>> 3;
        int reg = (opCode & 0x07);
        int val = switch (reg) {
            case 0x00/*??? B*/ -> state.b();
            case 0x01/*??? C*/ -> state.c();
            case 0x02/*??? D*/ -> state.d();
            case 0x03/*??? E*/ -> state.e();
            case 0x04/*??? H*/ -> state.h();
            case 0x05/*??? L*/ -> state.l();
            case 0x06/*??? (HL)*/ -> bus.readMemory(state.hl());
            case 0x07/*??? A*/ -> state.a();
            default -> 0; // unreachable
        };
        switch (aop) {
            case 0x00/*ADD A,?*/ -> add_a_n(val); // ZUM(140-141) HTP(201-202)
            case 0x01/*ADC A,?*/ -> adc_a_n(val); // ZUM(146-147) HTP(190-191)
            case 0x02/*SUB ?*/ -> sub_n(val); // ZUM(148-149) HTP(434-435)
            case 0x03/*SBC A,?*/ -> sbc_a_n(val); // ZUM(150-151) HTP(420-421)
            case 0x04/*AND ?*/ -> and_n(val); // ZUM(152-153) HTP(209-210)
            case 0x05/*XOR ?*/ -> xor_n(val); // ZUM(156-157) HTP(436-437)
            case 0x06/*OR B*/ -> or_n(val); // ZUM(154-155) HTP(360-361)
            case 0x07/*CP B*/ -> cp_n(val); // ZUM(158-159) HTP(225-226)
        }
    }

    private void decode_0xC0vFF(int opCode) {
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
            case 0xCB/*[0xCB],[0x00-0xFF]*/ -> decode_0xCB_0x00uFF(bus.readMemory(state.pcInc1()));
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
            case 0xDD/*[0xDD],[0x00-0xFF]*/ -> decode_0xXY_0x00vFF(0xDD, bus.readMemory(state.pcInc1()), state::ix, state::ix);
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
            case 0xED/*[0xED],[0x00-0xFF]*/ -> decode_0xED_0x00vFF(bus.readMemory(state.pcInc1()));
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
            case 0xFD/*[0xFD],[0x00-0xFF]*/ -> decode_0xXY_0x00vFF(0xFD, bus.readMemory(state.pcInc1()), state::iy, state::iy);
            case 0xFE/*CP n*/ -> cp_n(bus.readMemory(state.pcInc1()));
            case 0xFF/*RST 38H*/ -> rst_n(0x38); // ZUM(267-268) HTP(418-419)
        }
    }

    // -----------------------------------------------------------------------------
    // -------------------- 0xCB prefix: shift and rotate group --------------------
    // -----------------------------------------------------------------------------

    // 0xCB-prefixed opCodes (bit instructions)
    private void decode_0xCB_0x00uFF(int opCode) {
        int qtr = opCode & 0xC0;
        switch (qtr) {
            case 0x00/*[0xCB],[0x00-0x3F]*/ -> decode_0xCB_0x00v3F(opCode);
            case 0x40/*[0xCB],[0x40-0x7F]*/ -> decode_0xCB_0x40v7F(opCode);
            case 0x80/*[0xCB],[0x80-0xBF]*/ -> decode_0xCB_0x80vBF(opCode);
            case 0xC0/*[0xCB],[0xC0-0xFF]*/ -> decode_0xCB_0xC0vFF(opCode);
        }
    }

    private void decode_0xCB_0x00v3F(int opCode) {
        int shf = (opCode & 0x38) >>> 3;
        int reg = (opCode & 0x07);
        int val = switch (reg) {
            case 0x00/*??? B*/ -> state.b();
            case 0x01/*??? C*/ -> state.c();
            case 0x02/*??? D*/ -> state.d();
            case 0x03/*??? E*/ -> state.e();
            case 0x04/*??? H*/ -> state.h();
            case 0x05/*??? L*/ -> state.l();
            case 0x06/*??? (HL)*/ -> bus.readMemory(state.hl());
            case 0x07/*??? A*/ -> state.a();
            default -> 0; // unreachable
        };
        val = switch (shf) {
            case 0x00/*RLC ?*/ -> rlc_n(val); // ZUM(194-195) HTP(400-401)
            case 0x01/*RRC ?*/ -> rrc_n(val); // ZUM(205-207) HTP(413-414)
            case 0x02/*RL (?*/ -> rl_n(val); // ZUM(202-204) HTP(396-397)
            case 0x03/*RR ?*/ -> rr_n(val); // ZUM(208-210) HTP(410-411)
            case 0x04/*SLA ?*/ -> sla_n(val); // ZUM(211-213) HTP(428-429)
            case 0x05/*SRA ?*/ -> sra_n(val); // ZUM(214-216) HTP(430-431)
            case 0x06/*SL1 ?*/ -> sl1_n(val); // undocumented
            case 0x07/*SRL ?*/ -> srl_n(val); // ZUM(217-219) HTP(432-433)
            default -> 0; // unreachable
        };
        switch (reg) {
            case 0x00/*??? B*/ -> state.b(val);
            case 0x01/*??? C*/ -> state.c(val);
            case 0x02/*??? D*/ -> state.d(val);
            case 0x03/*??? E*/ -> state.e(val);
            case 0x04/*??? H*/ -> state.h(val);
            case 0x05/*??? L*/ -> state.l(val);
            case 0x06/*??? (HL)*/ -> bus.writeMemory(state.hl(), val);
            case 0x07/*??? A*/ -> state.a(val);
        }
    }

    private void decode_0xCB_0x40v7F(int opCode) {
        int bit = (opCode & 0x38) >>> 3;
        int reg = (opCode & 0x07);
        switch (reg) {
            case 0x00/*BIT b,B*/ -> bit_b_n(bit, state.b()); // ZUM(224-225) HTP(217-218)
            case 0x01/*BIT b,C*/ -> bit_b_n(bit, state.c()); // ZUM(224-225) HTP(217-218)
            case 0x02/*BIT b,D*/ -> bit_b_n(bit, state.d()); // ZUM(224-225) HTP(217-218)
            case 0x03/*BIT b,E*/ -> bit_b_n(bit, state.e()); // ZUM(224-225) HTP(217-218)
            case 0x04/*BIT b,H*/ -> bit_b_n(bit, state.h()); // ZUM(224-225) HTP(217-218)
            case 0x05/*BIT b,L*/ -> bit_b_n(bit, state.l()); // ZUM(224-225) HTP(217-218)
            case 0x06/*BIT b,(HL)*/ -> bit_b_n(bit, bus.readMemory(state.hl())); // ZUM(226-227) HTP(211-212)
            case 0x07/*BIT b,A*/ -> bit_b_n(bit, state.a()); // ZUM(224-225) HTP(217-218)
        }
    }

    private void decode_0xCB_0x80vBF(int opCode) {
        int bit = (opCode & 0x38) >>> 3;
        int reg = (opCode & 0x07);
        switch (reg) {
            case 0x00/*RES b,B*/ -> state.b(res_b_n(bit, state.b())); // ZUM(236-237) HTP(385-387)
            case 0x01/*RES b,C*/ -> state.c(res_b_n(bit, state.c())); // ZUM(236-237) HTP(385-387)
            case 0x02/*RES b,D*/ -> state.d(res_b_n(bit, state.d())); // ZUM(236-237) HTP(385-387)
            case 0x03/*RES b,E*/ -> state.e(res_b_n(bit, state.e())); // ZUM(236-237) HTP(385-387)
            case 0x04/*RES b,H*/ -> state.h(res_b_n(bit, state.h())); // ZUM(236-237) HTP(385-387)
            case 0x05/*RES b,L*/ -> state.l(res_b_n(bit, state.l())); // ZUM(236-237) HTP(385-387)
            case 0x06/*RES b,(HL)*/ -> bus.writeMemory(state.hl(), res_b_n(bit, bus.readMemory(state.hl()))); // ZUM(236-237) HTP(385-387)
            case 0x07/*RES b,A*/ -> state.a(res_b_n(bit, state.a())); // ZUM(236-237) HTP(385-387)
        }
    }

    private void decode_0xCB_0xC0vFF(int opCode) {
        int bit = (opCode & 0x38) >>> 3;
        int reg = (opCode & 0x07);
        switch (reg) {
            case 0x00/*SET b,B*/ -> state.b(set_b_n(bit, state.b())); // ZUM(232) HTP(425-427)
            case 0x01/*SET b,C*/ -> state.c(set_b_n(bit, state.c())); // ZUM(232) HTP(425-427)
            case 0x02/*SET b,D*/ -> state.d(set_b_n(bit, state.d())); // ZUM(232) HTP(425-427)
            case 0x03/*SET b,E*/ -> state.e(set_b_n(bit, state.e())); // ZUM(232) HTP(425-427)
            case 0x04/*SET b,H*/ -> state.h(set_b_n(bit, state.h())); // ZUM(232) HTP(425-427)
            case 0x05/*SET b,L*/ -> state.l(set_b_n(bit, state.l())); // ZUM(232) HTP(425-427)
            case 0x06/*SET b,(HL)*/ -> bus.writeMemory(state.hl(), set_b_n(bit, bus.readMemory(state.hl()))); // ZUM(233) HTP(425-427)
            case 0x07/*SET b,A*/ -> state.a(set_b_n(bit, state.a())); // ZUM(232) HTP(425-427)
        }
    }

    // -----------------------------------------------------------------------------------------------
    // -------------------- 0xED prefix: miscellaneous extended instruction group --------------------
    // -----------------------------------------------------------------------------------------------

    // 0xED-prefixed opCodes
    private void decode_0xED_0x00vFF(int opCode) {
        int qtr = opCode & 0xC0;
        switch (qtr) {
            case 0x00/*[0xED],[0x00-0x3F]*/ -> decode_0xED_0x00v3F(opCode);
            case 0x40/*[0xED],[0x40-0x7F]*/ -> decode_0xED_0x40v7F(opCode);
            case 0x80/*[0xED],[0x80-0xBF]*/ -> decode_0xED_0x80vBF(opCode);
            case 0xC0/*[0xED],[0xC0-0xFF]*/ -> decode_0xED_0xC0vFF(opCode);
        }
    }

    private void decode_0xED_0x00v3F(int opCode) {
        unrecognizedOpCode2(0xED, opCode);
    }

    private void decode_0xED_0x40v7F(int opCode) {
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
            default -> unrecognizedOpCode2(0xED, opCode);
        }
    }

    private void decode_0xED_0x80vBF(int opCode) {
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
            default -> unrecognizedOpCode2(0xED, opCode);
        }
    }

    private void decode_0xED_0xC0vFF(int opCode) {
        unrecognizedOpCode2(0xED, opCode);
    }

    // --------------------------------------------------------------------------------------------------
    // -------------------- 0xDD or oxFD prefix: IX- or IY-indexed instruction group --------------------
    // --------------------------------------------------------------------------------------------------

    // 0xDD (IX) or 0xFD (IY) prefixed op-codes
    private void decode_0xXY_0x00vFF(int prefix1, int opCode, IntSupplier rrGet, IntConsumer rrSet) {
        int qtr = opCode & 0xC0;
        switch (qtr) {
            case 0x00/*[0xXY].[0x00-0x3F]*/ -> decode_0xXY_0x00v3F(prefix1, opCode, rrGet, rrSet);
            case 0x40/*[0xXY],[0x40-0x7F]*/ -> decode_0xXY_0x40v7F(prefix1, opCode, rrGet, rrSet);
            case 0x80/*[0xXY],[0x80-0xBF]*/ -> decode_0xXY_0x80vBF(prefix1, opCode, rrGet);
            case 0xC0/*[0xXY],[0xC0-0xFF]*/ -> decode_0xXY_0xC0vFF(prefix1, opCode, rrGet, rrSet);
        }
    }

    private void decode_0xXY_0x00v3F(int prefix1, int opCode, IntSupplier rrGet, IntConsumer rrSet) {
        switch (opCode) {
            case 0x04/*INC B*/ -> state.b(inc_n(state.b())); // undocumented
            case 0x05/*DEC B*/ -> state.b(dec_n(state.b())); // undocumented
            case 0x06/*LD B,n*/ -> state.b(bus.readMemory(state.pcInc1())); // undocumented
            case 0x09/*ADD IXY,BC*/ -> add_rr_nn(rrGet, rrSet, state.bc()); // ZUM(182-18) HTP(205-208)
            case 0x0C/*INC C*/ -> state.c(inc_n(state.c())); // undocumented
            case 0x0D/*DEC C*/ -> state.c(dec_n(state.c())); // undocumented
            case 0x0E/*LD C,n*/ -> state.c(bus.readMemory(state.pcInc1())); // undocumented
            case 0x14/*INC D*/ -> state.d(inc_n(state.d())); // undocumented
            case 0x15/*DEC D*/ -> state.d(dec_n(state.d())); // undocumented
            case 0x16/*LD D,n*/ -> state.d(bus.readMemory(state.pcInc1())); // undocumented
            case 0x19/*ADD IXY,DE*/ -> add_rr_nn(rrGet, rrSet, state.de()); // ZUM(182-18) HTP(205-208)
            case 0x1C/*INC E*/ -> state.e(inc_n(state.e())); // undocumented
            case 0x1D/*DEC E*/ -> state.e(dec_n(state.e())); // undocumented
            case 0x1E/*LD E,n*/ -> state.e(bus.readMemory(state.pcInc1())); // undocumented
            case 0x21/*LD IXY,nn*/ -> rrSet.accept(bus.readWord(state.pcInc2())); // ZUM(103) HTP(336-337)
            case 0x22/*LD (nn),IXY*/ -> bus.writeWord(state.pcInc2(), rrGet.getAsInt()); // ZUM(111-112) HTP(325-328)
            case 0x23/*INC IXY*/ -> rrSet.accept(inc_nn(rrGet.getAsInt())); // ZUM(185) HTP(272)
            case 0x24/*INC IXYH*/ -> high(rrGet, rrSet, inc_n(high(rrGet))); // undocumented
            case 0x25/*DEC IXYH*/ -> high(rrGet, rrSet, dec_n(high(rrGet))); // undocumented
            case 0x26/*LD IXYH,n*/ -> high(rrGet, rrSet, bus.readMemory(state.pcInc1())); // undocumented
            case 0x29/*ADD IXY,IXY*/ -> add_rr_nn(rrGet, rrSet, rrGet.getAsInt()); // ZUM(182-18) HTP(205-208)
            case 0x2A/*LD IXY,(nn)*/ -> rrSet.accept(bus.readWord(bus.readWord(state.pcInc2()))); // ZUM(107-108) HTP(338-339,342-343)
            case 0x2B/*DEC IXY*/ -> rrSet.accept(dec_nn(rrGet.getAsInt())); // ZUM(186) HTP(242-243)
            case 0x2C/*INC IXYL*/ -> low(rrGet, rrSet, inc_n(low(rrGet))); // undocumented
            case 0x2D/*DEC IXYL*/ -> low(rrGet, rrSet, dec_n(low(rrGet))); // undocumented
            case 0x2E/*LD IXYL,n*/ -> low(rrGet, rrSet, bus.readMemory(state.pcInc1())); // undocumented
            case 0x34/*INC (IXY+n)*/ -> { // ZUM(162-163) HTP(268-271)
                int address = indexed(rrGet.getAsInt());
                bus.writeMemory(address, inc_n(bus.readMemory(address)));
            }
            case 0x35/*DEC (IXY+n)*/ -> { // ZUM(164-165) HTP(238-239)
                int address = indexed(rrGet.getAsInt());
                bus.writeMemory(address, dec_n(bus.readMemory(address)));
            }
            case 0x36/*LD (IXY+n),n*/ -> { // ZUM(90-91) HTP(309-312)
                int address = indexed(rrGet.getAsInt());
                bus.writeMemory(address, bus.readMemory(state.pcInc1()));
            }
            case 0x39/*ADD IXY,SP*/ -> add_rr_nn(rrGet, rrSet, state.sp()); // ZUM(182-18) HTP(205-208)
            case 0x3C/*INC A*/ -> state.a(inc_n(state.a())); // undocumented
            case 0x3D/*DEC A*/ -> state.a(dec_n(state.a())); // undocumented
            case 0x3E/*LD A,n*/ -> state.a(bus.readMemory(state.pcInc1())); // undocumented
            default -> unrecognizedOpCode2(prefix1, opCode);
        }
    }

    private void decode_0xXY_0x40v7F(int prefix1, int opCode, IntSupplier rrGet, IntConsumer rrSet) {
        switch (opCode) {
            case 0x40/*LD B,B*/ -> {/*state.b(state.b()):*/} // undocumented
            case 0x41/*LD B,C*/ -> state.b(state.c()); // undocumented
            case 0x42/*LD B,D*/ -> state.b(state.d()); // undocumented
            case 0x43/*LD B,E*/ -> state.b(state.e()); // undocumented
            case 0x44/*LD B,IXYH*/ -> state.b(high(rrGet)); // undocumented
            case 0x45/*LD B,IXYL*/ -> state.b(low(rrGet)); // undocumented
            case 0x46/*LD B,(IXY+n)*/ -> state.b(bus.readMemory(indexed(rrGet.getAsInt()))); // ZUM(84-85) HTP(305-308)
            case 0x47/*LD B,A*/ -> state.b(state.a()); // undocumented
            case 0x48/*LD C,B*/ -> state.c(state.b()); // undocumented
            case 0x49/*LD C,C*/ -> {/*state.c(state.c());*/} // undocumented
            case 0x4A/*LD C,D*/ -> state.c(state.d()); // undocumented
            case 0x4B/*LD C,E*/ -> state.c(state.e()); // undocumented
            case 0x4C/*LD C,IXYH*/ -> state.c(high(rrGet)); // undocumented
            case 0x4D/*LD C,IXYL*/ -> state.c(low(rrGet)); // undocumented
            case 0x4E/*LD C,(IXY+n)*/ -> state.c(bus.readMemory(indexed(rrGet.getAsInt()))); // ZUM(84-85) HTP(305-308)
            case 0x4F/*LD C,A*/ -> state.c(state.a()); // undocumented
            case 0x50/*LD D,B*/ -> state.d(state.b()); // undocumented
            case 0x51/*LD D,C*/ -> state.d(state.c()); // undocumented
            case 0x52/*LD D,D*/ -> {/*state.d(state.d());*/} // undocumented
            case 0x53/*LD D,E*/ -> state.d(state.e()); // undocumented
            case 0x54/*LD D,IXYH*/ -> state.d(high(rrGet)); // undocumented
            case 0x55/*LD D,IXYL*/ -> state.d(low(rrGet)); // undocumented
            case 0x56/*LD D,(IXY+n)*/ -> state.d(bus.readMemory(indexed(rrGet.getAsInt()))); // ZUM(84-85) HTP(305-308)
            case 0x57/*LD D,A*/ -> state.d(state.a()); // undocumented
            case 0x58/*LD E,B*/ -> state.e(state.b()); // undocumented
            case 0x59/*LD E,C*/ -> state.e(state.c()); // undocumented
            case 0x5A/*LD E,D*/ -> state.e(state.d()); // undocumented
            case 0x5B/*LD E,E*/ -> {/*state.e(state.e());*/} // undocumented
            case 0x5C/*LD E,IXYH*/ -> state.e(high(rrGet)); // undocumented
            case 0x5D/*LD E,IXYL*/ -> state.e(low(rrGet)); // undocumented
            case 0x5E/*LD E,(IXY+n)*/ -> state.e(bus.readMemory(indexed(rrGet.getAsInt()))); // ZUM(84-85) HTP(305-308)
            case 0x5F/*LD E,A*/ -> state.e(state.a()); // undocumented
            case 0x60/*LD IXYH,B*/ -> high(rrGet, rrSet, state.b()); // undocumented
            case 0x61/*LD IXYH,C*/ -> high(rrGet, rrSet, state.c()); // undocumented
            case 0x62/*LD IXYH,D*/ -> high(rrGet, rrSet, state.d()); // undocumented
            case 0x63/*LD IXYH,E*/ -> high(rrGet, rrSet, state.e()); // undocumented
            case 0x64/*LD IXYH,IXYH*/ -> {/*high(rrGet, rrSet, high(rrGet));*/} // undocumented
            case 0x65/*LD IXYH,IXYL*/ -> high(rrGet, rrSet, low(rrGet)); // undocumented
            case 0x66/*LD H,(IXY+n)*/ -> state.h(bus.readMemory(indexed(rrGet.getAsInt()))); // ZUM(84-85) HTP(305-308)
            case 0x67/*LD IXYH,A*/ -> high(rrGet, rrSet, state.a()); // undocumented
            case 0x68/*LD IXYL,B*/ -> low(rrGet, rrSet, state.b()); // undocumented
            case 0x69/*LD IXYL,C*/ -> low(rrGet, rrSet, state.c()); // undocumented
            case 0x6A/*LD IXYL,D*/ -> low(rrGet, rrSet, state.d()); // undocumented
            case 0x6B/*LD IXYL,E*/ -> low(rrGet, rrSet, state.e()); // undocumented
            case 0x6C/*LD IXYL,IXYH*/ -> low(rrGet, rrSet, high(rrGet)); // undocumented
            case 0x6D/*LD IXYL,IXYL*/ -> {/*low(rrGet, rrSet, low(rrGet));*/} // undocumented
            case 0x6E/*LD L,(IXY+n)*/ -> state.l(bus.readMemory(indexed(rrGet.getAsInt()))); // ZUM(84-85) HTP(305-308)
            case 0x6F/*LD IXYL,A*/ -> low(rrGet, rrSet, state.a()); // undocumented
            case 0x70/*LD (IXY+n),B*/ -> bus.writeMemory(indexed(rrGet.getAsInt()), state.b()); // ZUM(87-88) HTP(313-316)
            case 0x71/*LD (IXY+n),C*/ -> bus.writeMemory(indexed(rrGet.getAsInt()), state.c()); // ZUM(87-88) HTP(313-316)
            case 0x72/*LD (IXY+n),D*/ -> bus.writeMemory(indexed(rrGet.getAsInt()), state.d()); // ZUM(87-88) HTP(313-316)
            case 0x73/*LD (IXY+n),E*/ -> bus.writeMemory(indexed(rrGet.getAsInt()), state.e()); // ZUM(87-88) HTP(313-316)
            case 0x74/*LD (IXY+n),H*/ -> bus.writeMemory(indexed(rrGet.getAsInt()), state.h()); // ZUM(87-88) HTP(313-316)
            case 0x75/*LD (IXY+n),L*/ -> bus.writeMemory(indexed(rrGet.getAsInt()), state.l()); // ZUM(87-88) HTP(313-316)
            case 0x77/*LD (IXY+n),A*/ -> bus.writeMemory(indexed(rrGet.getAsInt()), state.a()); // ZUM(87-88) HTP(313-316)
            case 0x78/*LD A,B*/ -> state.a(state.b()); // undocumented
            case 0x79/*LD A,C*/ -> state.a(state.c()); // undocumented
            case 0x7A/*LD A,D*/ -> state.a(state.d()); // undocumented
            case 0x7B/*LD A,E*/ -> state.a(state.e()); // undocumented
            case 0x7C/*LD A,IXYH*/ -> state.a(high(rrGet)); // undocumented
            case 0x7D/*LD A,IXYL*/ -> state.a(low(rrGet)); // undocumented
            case 0x7E/*LD A,(IXY+n)*/ -> state.a(bus.readMemory(indexed(rrGet.getAsInt()))); // ZUM(84-85) HTP(305-308)
            case 0x7F/*LD A,A*/ -> {/*state.a(state.a());*/} // undocumented
            default -> unrecognizedOpCode2(prefix1, opCode); // e.g. 0x76 is not decoded
        }
    }

    private void decode_0xXY_0x80vBF(int prefix1, int opCode, IntSupplier rrGet) {
        int aop = (opCode & 0x38) >>> 3;
        int reg = (opCode & 0x07);
        int val = switch (reg) {
            case 0x00/*??? B*/ -> state.b();
            case 0x01/*??? C*/ -> state.c();
            case 0x02/*??? D*/ -> state.d();
            case 0x03/*??? E*/ -> state.e();
            case 0x04/*??? IXYH*/ -> high(rrGet);
            case 0x05/*??? IXYL*/ -> low(rrGet);
            case 0x06/*??? (IXY)*/ -> bus.readMemory(indexed(rrGet.getAsInt())); // ZUM(144-145) HTP(196-199)
            case 0x07/*??? A*/ -> state.a();
            default -> 0; // unreachable
        };
        switch (aop) {
            case 0x00/*ADD A,?*/ -> add_a_n(val); // ZUM(144-145) HTP(196-199)
            case 0x01/*ADC A,?*/ -> adc_a_n(val); // ZUM(146-147) HTP(190-191)
            case 0x02/*SUB ?*/ -> sub_n(val); // ZUM(148-149) HTP(434-435)
            case 0x03/*SBC A,?*/ -> sbc_a_n(val); // ZUM(150-151) HTP(420-421)
            case 0x04/*AND ?*/ -> and_n(val); // ZUM(152-153) HTP(209-210)
            case 0x05/*XOR ?*/ -> xor_n(val); // ZUM(156-157) HTP(436-437)
            case 0x06/*OR B*/ -> or_n(val); // ZUM(154-155) HTP(360-361)
            case 0x07/*CP B*/ -> cp_n(val); // ZUM(158-159) HTP(225-226)
        }
    }

    private void decode_0xXY_0xC0vFF(int prefix1, int opCode, IntSupplier rrGet, IntConsumer rrSet) {
        switch (opCode) {
            case 0xCB/*[0xDD],[0xCB],[0x00-0xFF]*/ -> decode_0xXY_0xCB_0x00vFF(bus.readMemory(state.pcInc1()), rrGet);
            case 0xE1/*POP IXY*/ -> rrSet.accept(bus.readWord(state.spInc2())); // ZUM(120) HTP(375-376)
            case 0xE3/*EX (SP),IXY*/ -> ex_csp_rr(rrGet, rrSet); // ZUM(126-127) HTP(252-255)
            case 0xE5/*PUSH IXY*/ -> bus.writeWord(state.spDec2(), rrGet.getAsInt()); // ZUM(117) HTP(381-382)
            case 0xE9/*JP (IXY)*/ -> state.pc(rrGet.getAsInt()); // ZUM(251) HTP(286)
            case 0xF9/*LD SP,IXY*/ -> state.sp(rrGet.getAsInt()); // ZUM(114-115) HTP(346-347)
            default -> unrecognizedOpCode2(prefix1, opCode);
        }
    }

    // ------------------------------------------------------------------------------------------------------
    // -------------------- 0xDD,0xCB or 0xFD,0xCB prefix: indexed bit instruction group --------------------
    // ------------------------------------------------------------------------------------------------------

    // 0xDD,0xCB or 0xFD,0xCB prefixed op-codes (indexed bit instructions)
    private void decode_0xXY_0xCB_0x00vFF(int opCode, IntSupplier rrGet) {
        int qtr = opCode & 0xC0;
        switch (qtr) {
            case 0x00/*[0xXY],[0xCB],[0x00-0x3F]*/ -> decode_0xXY_0xCB_0x00v3F(opCode, rrGet);
            case 0x40/*[0xXY],[0xCB],[0x40-0x7F]*/ -> decode_0xXY_0xCB_0x40v7F(opCode, rrGet);
            case 0x80/*[0xXY],[0xCB],[0x80-0xBF]*/ -> decode_0xXY_0xCB_0x80vBF(opCode, rrGet);
            case 0xC0/*[0xXY],[0xCB],[0xC0-0xFF]*/ -> decode_0xXY_0xCB_0xC0vFF(opCode, rrGet);
        }
    }

    private void decode_0xXY_0xCB_0x00v3F(int opCode, IntSupplier rrGet) {
        int shf = (opCode & 0x38) >>> 3;
        int reg = (opCode & 0x07);
        int address = indexed(rrGet.getAsInt());
        int val = bus.readMemory(address);
        val = switch (shf) {
            case 0x00/*RLC (IXY+n),?*/ -> rlc_n(val); // ZUM(198-201) HTP(404-407)
            case 0x01/*RRC (IXY+n),?*/ -> rrc_n(val); // ZUM(205-207) HTP(413-414)
            case 0x02/*RL (IXY+n),?*/ -> rl_n(val); // ZUM(202-204) HTP(396-397)
            case 0x03/*RR (IXY+n),?*/ -> rr_n(val); // ZUM(208-210) HTP(410-411)
            case 0x04/*SLA (IXY+n),?*/ -> sla_n(val); // ZUM(211-213) HTP(428-429)
            case 0x05/*SRA (IXY+n),?*/ -> sra_n(val); // ZUM(214-216) HTP(430-431)
            case 0x06/*SL1 (IXY+n),?*/ -> sl1_n(val); // undocumented
            case 0x07/*SRL (IXY+n),?*/ -> srl_n(val); // ZUM(217-219) HTP(432-433)
            default -> 0; // unreachable
        };
        bus.writeMemory(address, val);
        switch (reg) {
            case 0x00/*??? (IXY+n),B*/ -> state.b(val); // undocumented
            case 0x01/*??? (IXY+n),C*/ -> state.c(val); // undocumented
            case 0x02/*??? (IXY+n),D*/ -> state.d(val); // undocumented
            case 0x03/*??? (IXY+n),E*/ -> state.e(val); // undocumented
            case 0x04/*??? (IXY+n),H*/ -> state.h(val); // undocumented
            case 0x05/*??? (IXY+n),L*/ -> state.l(val); // undocumented
            case 0x06/*??? (IXY+n)*/ -> {} // ZUM(198-219) HTP(396-417,428-433)
            case 0x07/*??? (IXY+n),A*/ -> state.a(val); // undocumented
        }
    }

    private void decode_0xXY_0xCB_0x40v7F(int opCode, IntSupplier rrGet) {
        int bit = (opCode & 0x38) >>> 3;
        /*BIT b,(IXY+d)*/ bit_b_n(bit, bus.readMemory(indexed(rrGet.getAsInt()))); // ZUM(228-231) HTP(213-216)
    }

    private void decode_0xXY_0xCB_0x80vBF(int opCode, IntSupplier rrGet) {
        int bit = (opCode & 0x38) >>> 3;
        int reg = (opCode & 0x07);
        int address = indexed(rrGet.getAsInt());
        int val = res_b_n(bit, bus.readMemory(address));
        bus.writeMemory(address, val);
        switch (reg) {
            case 0x00/*RES b,(IXY+n),B*/ -> state.b(val); // undocumented
            case 0x01/*RES b,(IXY+n),C*/ -> state.c(val); // undocumented
            case 0x02/*RES b,(IXY+n),D*/ -> state.d(val); // undocumented
            case 0x03/*RES b,(IXY+n),E*/ -> state.e(val); // undocumented
            case 0x04/*RES b,(IXY+n),H*/ -> state.h(val); // undocumented
            case 0x05/*RES b,(IXY+n),L*/ -> state.l(val); // undocumented
            case 0x06/*RES b,(IXY+n)*/ -> {} // ZUM(236-237) HTP(385-387)
            case 0x07/*RES b,(IXY+n),A*/ -> state.a(val); // undocumented
        }
    }

    private void decode_0xXY_0xCB_0xC0vFF(int opCode, IntSupplier rrGet) {
        int bit = (opCode & 0x38) >>> 3;
        int reg = (opCode & 0x07);
        int address = indexed(rrGet.getAsInt());
        int val = set_b_n(bit, bus.readMemory(address));
        bus.writeMemory(address, val);
        switch (reg) {
            case 0x00/*SET b,(IXY+n),B*/ -> state.b(val); // undocumented
            case 0x01/*SET b,(IXY+n),C*/ -> state.c(val); // undocumented
            case 0x02/*SET b,(IXY+n),D*/ -> state.d(val); // undocumented
            case 0x03/*SET b,(IXY+n),E*/ -> state.e(val); // undocumented
            case 0x04/*SET b,(IXY+n),H*/ -> state.h(val); // undocumented
            case 0x05/*SET b,(IXY+n),L*/ -> state.l(val); // undocumented
            case 0x06/*SET b,(IXY+n)*/ -> {} // ZUM(234-235) HTP(425-427)
            case 0x07/*SET b,(IXY+n),A*/ -> state.a(val); // undocumented
        }
    }

    // ------------------------------------------------------------------------------
    // -------------------- Instruction implementation functions --------------------
    // ------------------------------------------------------------------------------

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

    private void ex_csp_rr(IntSupplier rrGet, IntConsumer rrSet) { // ZUM(126-127) HTP(252-255)
        int rrVal = rrGet.getAsInt();
        rrSet.accept(bus.readWord(state.sp()));
        bus.writeWord(state.sp(), rrVal);
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
        int n = bus.readMemory(state.hl());
        int rm = (state.a() - n) & 0xFF;
        state.hl(state.hl() + 1);
        state.bc(state.bc() - 1);

        state.nf(true);
        state.pf(state.bc() != 0);
        state.hf((state.a() & 0xF) < (n & 0xF));
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
        int n = bus.readMemory(state.hl());
        int rm = (state.a() - n) & 0xFF;
        state.hl(state.hl() - 1);
        state.bc(state.bc() - 1);

        state.nf(true);
        state.pf(state.bc() != 0);
        state.hf((state.a() & 0xF) < (n & 0xF));
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

    private void add_rr_nn(IntSupplier rrGet, IntConsumer rrSet, int nn) { // ZUM(182-183) HTP(205-208)
        int ru = rrGet.getAsInt() + nn;
        int rm = ru & 0xFFFF;
        state.cf(rm != ru);
        state.nf(false);
        rrSet.accept(rm);
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
        int chl = bus.readMemory(state.hl());
        bus.writeMemory(state.hl(), ((chl & 0x0F) << 4) | (state.a() & 0x0F));
        int rm = (state.a() & 0xF0) | ((chl & 0xF0) >>> 4);
        state.nf(false);
        state.pf(parity(rm));
        state.hf(false);
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        state.a(rm);
    }

    private void rrd() { // ZUM(222-223) HTP(416-417)
        int chl = bus.readMemory(state.hl());
        bus.writeMemory(state.hl(), ((state.a() & 0x0F) << 4) | ((chl & 0xF0) >>> 4));
        int rm = (state.a() & 0xF0) | (chl & 0x0F);
        state.nf(false);
        state.pf(parity(rm));
        state.hf(false);
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        state.a(rm);
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

    // ------------------------------------------------------------------
    // -------------------- Helper/utility functions --------------------
    // ------------------------------------------------------------------

    /**
     * @param rrGet IntSupplier for 16-bit register in question
     * @return MSB (byte) in the 16-bit value.
     */
    private int high(IntSupplier rrGet) {
        return (rrGet.getAsInt() & 0xFF00) >>> 8;
    }
    /**
     * @param rrGet IntSupplier for 16-bit register in question
     * @return LSB (byte) in the 16-bit value.
     */
    private int low(IntSupplier rrGet) {
        return rrGet.getAsInt() & 0xFF;
    }
    /**
     * Set the MSB (byte) in the 16-bit value.
     * @param rrGet IntSupplier for 16-bit register in question
     * @param rrSet IntConsumer for 16-bit register in question
     */
    private void high(IntSupplier rrGet, IntConsumer rrSet, int highVal) {
        int fullVal = ((highVal & 0xFF) << 8 )| (rrGet.getAsInt() & 0xFF);
        rrSet.accept(fullVal);
    }
    /**
     * Set the LSB (byte) in the 16-bit value.
     * @param rrGet IntSupplier for 16-bit register in question
     * @param rrSet IntConsumer for 16-bit register in question
     */
    private void low(IntSupplier rrGet, IntConsumer rrSet, int lowVal) {
        int fullVal = (rrGet.getAsInt() & 0xFF00) | (lowVal & 0xFF);
        rrSet.accept(fullVal);
    }

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

    private void unrecognizedOpCode2(int prefix1, int opCode) {
        System.err.printf("?0x%02x,0x%02x?  ", prefix1, opCode);
    }
}
