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

    boolean step() {
        return decode(bus.cpuReadMemInstr(state.pc()));
    }

    // -----------------------------------------------------------------------
    // ---------- unprefixed op-codes: general-purpose instructions ----------
    // -----------------------------------------------------------------------

    // Decode (00xC0-0xFF) op-codes: general-purpose instructions.
    private boolean decode(int opCode) {
        state.rInc();
        if (opCode == 0x76) { /* `HALT` ZUM(173) HTP(257) */
            nop(); // note that the `PC` register is not changed.
            return false;
        }
        state.pcInc1();
        int qtr = (opCode & 0xC0); // values 0x00|0x40|0x80|0xC0
        switch (qtr) {
            case 0x00/*[0x00-0x3F]*/ -> decodeQ0(opCode);
            case 0x40/*[0x40-0x7F]*/ -> setRegister((opCode & 0x38) >>> 3, getRegister(opCode)); /* `LD r,r'` ZUM(81) HTP(297-298), `LD r,(HL)` ZUM(83) HTP(356-357), `LD (HL),r` ZUM(86) HTP(303-304) */
            case 0x80/*[0x80-0xBF]*/ -> alu_acc_n(opCode, getRegister(opCode)); /* `ADD A,r` ZUM(140-141) HTP(201-202), `ADC A,r` ZUM(146-147) HTP(190-191),`SUB r` ZUM(148-149) HTP(434-435), `SBC A,r` ZUM(150-151) HTP(420-421), `AND r` ZUM(152-153) HTP(209-210), `XOR r` ZUM(156-157) HTP(436-437), `OR r` ZUM(154-155) HTP(360-361), `CP r` ZUM(158-159) HTP(225-226) */
            case 0xC0/*[0xC0-0xFF]*/ -> decodeQ3(opCode);
        }
        return true;
    }

    // Decode (00x00-0x3F) op-codes.
    private void decodeQ0(int opCode) {
        switch (opCode) {
            case 0x00/*NOP*/ -> nop(); // ZUM(172) HTP(359)
            case 0x01/*LD BC,nn*/ -> state.bc(bus.cpuReadMem16(state.pcInc2())); // ZUM(102) HTP(293-294)
            case 0x02/*LD (BC),A*/ -> bus.cpuWriteMem8(state.bc(), state.a()); // ZUM(95) HTP(299)
            case 0x03/*INC BC*/ -> state.bc(inc_nn(state.bc())); // ZUM(184) HTP(265-266)
            case 0x04/*INC B*/ -> state.b(inc_n(state.b())); // ZUM(160) HTP(264)
            case 0x05/*DEC B*/ -> state.b(dec_n(state.b())); // ZUM(164-165) HTP(238-239)
            case 0x06/*LD B,n*/ -> state.b(bus.cpuReadMem8(state.pcInc1())); // ZUM(82) HTP(295-296)
            case 0x07/*RLCA*/ -> rlca(); // ZUM(190) HTP(399)
            case 0x08/*EX AF,AF'*/ -> ex_af_aaf(); // ZUM(123) HTP(248)
            case 0x09/*ADD HL,BC*/ -> add_hl_nn(state.bc()); // ZUM(179) HTP(203-204)
            case 0x0A/*LD A,(BC)*/ -> state.a(bus.cpuReadMem8(state.bc())); // ZUM(92) HTP(329)
            case 0x0B/*DEC BC*/ -> state.bc(dec_nn(state.bc())); // ZUM(187) HTP(240-241)
            case 0x0C/*INC C*/ -> state.c(inc_n(state.c())); // ZUM(160) HTP(264)
            case 0x0D/*DEC C*/ -> state.c(dec_n(state.c())); // ZUM(164-165) HTP(238-239)
            case 0x0E/*LD C,n*/ -> state.c(bus.cpuReadMem8(state.pcInc1())); // ZUM(82) HTP(295-296)
            case 0x0F/*RRCA*/ -> rrca(); // ZUM(192) HTP(415)
            case 0x10/*DJNZ n*/ -> djnz(); // ZUM(253-254) HTP(245-246)
            case 0x11/*LD DE,nn*/ -> state.de(bus.cpuReadMem16(state.pcInc2())); // ZUM(102) HTP(293-294)
            case 0x12/*LD (DE),A*/ -> bus.cpuWriteMem8(state.de(), state.a()); // ZUM(96) HTP(300)
            case 0x13/*INC DE*/ -> state.de(inc_nn(state.de())); // ZUM(184) HTP(265-266)
            case 0x14/*INC D*/ -> state.d(inc_n(state.d())); // ZUM(160) HTP(264)
            case 0x15/*DEC D*/ -> state.d(dec_n(state.d())); // ZUM(164-165) HTP(238-239)
            case 0x16/*LD D,n*/ -> state.d(bus.cpuReadMem8(state.pcInc1())); // ZUM(82) HTP(295-296)
            case 0x17/*RLA*/ -> rla(); // ZUM(191) HTP(398)
            case 0x18/*JR n*/ -> jr(); // ZUM(241) HTP(290)
            case 0x19/*ADD HL,DE*/ -> add_hl_nn(state.de()); // ZUM(179) HTP(203-204)
            case 0x1A/*LD A,(DE)*/ -> state.a(bus.cpuReadMem8(state.de())); // ZUM(93) HTP(330)
            case 0x1B/*DEC DE*/ -> state.de(dec_nn(state.de())); // ZUM(187) HTP(240-241)
            case 0x1C/*INC E*/ -> state.e(inc_n(state.e())); // ZUM(160) HTP(264)
            case 0x1D/*DEC E*/ -> state.e(dec_n(state.e())); // ZUM(164-165) HTP(238-239)
            case 0x1E/*LD E,n*/ -> state.e(bus.cpuReadMem8(state.pcInc1())); // ZUM(82) HTP(295-296)
            case 0x1F/*RRA*/ -> rra(); // ZUM(193) HTP(412)
            case 0x20/*JR NZ,n*/ -> jr_t(!state.zf()); // ZUM(248-249) HTP(288-289)
            case 0x21/*LD HL,nn*/ -> state.hl(bus.cpuReadMem16(state.pcInc2())); // ZUM(102) HTP(293-294)
            case 0x22/*LD (nn),HL*/ -> bus.cpuWriteMem16(bus.cpuReadMem16(state.pcInc2()), state.hl()); // ZUM(109) HTP(323-324)
            case 0x23/*INC HL*/ -> state.hl(inc_nn(state.hl())); // ZUM(184) HTP(265-266)
            case 0x24/*INC H*/ -> state.h(inc_n(state.h())); // ZUM(160) HTP(264)
            case 0x25/*DEC H*/ -> state.h(dec_n(state.h())); // ZUM(164-165) HTP(238-239)
            case 0x26/*LD H,n*/ -> state.h(bus.cpuReadMem8(state.pcInc1())); // ZUM(82) HTP(295-296)
            case 0x27/*DAA*/ -> daa(); // ZUM(166) HTP(236-237)
            case 0x28/*JR Z,n*/ -> jr_t(state.zf()); // ZUM(246-247) HTP(288-289)
            case 0x29/*ADD HL,HL*/ -> add_hl_nn(state.hl()); // ZUM(179) HTP(203-204)
            case 0x2A/*LD HL,(nn)*/ -> state.hl(bus.cpuReadMem16(bus.cpuReadMem16(state.pcInc2()))); // ZUM(105) HTP(334-335)
            case 0x2B/*DEC HL*/ -> state.hl(dec_nn(state.hl())); // ZUM(187) HTP(240-241)
            case 0x2C/*INC L*/ -> state.l(inc_n(state.l())); // ZUM(160) HTP(264)
            case 0x2D/*DEC L*/ -> state.l(dec_n(state.l())); // ZUM(164-165) HTP(238-239)
            case 0x2E/*LD L,n*/ -> state.l(bus.cpuReadMem8(state.pcInc1())); // ZUM(82) HTP(295-296)
            case 0x2F/*CPL*/ -> cpl(); // ZUM(168) HTP(235)
            case 0x30/*JR NC,n*/ -> jr_t(!state.cf()); // ZUM(244-245) HTP(288-289)
            case 0x31/*LD SP,nn*/ -> state.sp(bus.cpuReadMem16(state.pcInc2())); // ZUM(102) HTP(293-294)
            case 0x32/*LD (nn),A*/ -> bus.cpuWriteMem8(bus.cpuReadMem16(state.pcInc2()), state.a()); // ZUM(97) HTP(319-320)
            case 0x33/*INC SP*/ -> state.sp(inc_nn(state.sp())); // ZUM(184) HTP(265-266)
            case 0x34/*INC (HL)*/ -> bus.cpuWriteMem8(state.hl(), inc_n(bus.cpuReadMem8(state.hl()))); // ZUM(161) HTP(267)
            case 0x35/*DEC (HL)*/ -> bus.cpuWriteMem8(state.hl(), dec_n(bus.cpuReadMem8(state.hl()))); // ZUM(164-165) HTP(238-239)
            case 0x36/*LD (HL),n*/ -> bus.cpuWriteMem8(state.hl(), bus.cpuReadMem8(state.pcInc1())); // ZUM(86) HTP(301-302)
            case 0x37/*SCF*/ -> scf(); // ZUM(171) HTP(424)
            case 0x38/*JR C,n*/ -> jr_t(state.cf()); // ZUM(242-243) HTP(288-289)
            case 0x39/*ADD HL,SP*/ -> add_hl_nn(state.sp()); // ZUM(179) HTP(203-204)
            case 0x3A/*LD A,(nn)*/ -> state.a(bus.cpuReadMem8(bus.cpuReadMem16(state.pcInc2()))); // ZUM(94) HTP(317-318)
            case 0x3B/*DEC SP*/ -> state.sp(dec_nn(state.sp())); // ZUM(187) HTP(240-241)
            case 0x3C/*INC A*/ -> state.a(inc_n(state.a())); // ZUM(160) HTP(264)
            case 0x3D/*DEC A*/ -> state.a(dec_n(state.a())); // ZUM(164-165) HTP(238-239)
            case 0x3E/*LD A,n*/ -> state.a(bus.cpuReadMem8(state.pcInc1())); // ZUM(82) HTP(295-296)
            case 0x3F/*CCF*/ -> ccf(); // ZUM(170) HTP(224)
        }
    }

    // Decode (00xC0-0xBF) op-codes.
    private void decodeQ3(int opCode) {
        switch (opCode) {
            case 0xC0/*RET NZ*/ -> ret_t(!state.zf()); // ZUM(261-262) HTP(390-391)
            case 0xC1/*POP BC*/ -> state.bc(bus.cpuReadMem16(state.spInc2())); // ZUM(119) HTP(373-374)
            case 0xC2/*JP NZ,nn*/ -> jp_t(!state.zf()); // ZUM(239-240) HTP(282-283)
            case 0xC3/*JP nn*/ -> jp(); // ZUM(236) HTP(284)
            case 0xC4/*CALL NZ,nn*/ -> call_t(!state.zf()); // ZUM(257-259) HTP(219-221)
            case 0xC5/*PUSH BC*/ -> bus.cpuWriteMem16(state.spDec2(), state.bc()); // ZUM(116) HTP(379-380)
            case 0xC6/*ADD A,n*/ -> add_a_n(bus.cpuReadMem8(state.pcInc1())); // ZUM(142) HTP(200)
            case 0xC7/*RST 00H*/ -> rst_n(0x00); // ZUM(267-268) HTP(418-419)
            case 0xC8/*RET Z*/ -> ret_t(state.zf()); // ZUM(261-262) HTP(390-391)
            case 0xC9/*RET*/ -> ret(); // ZUM(260) HTP(388-389)
            case 0xCA/*JP Z,nn*/ -> jp_t(state.zf()); // ZUM(239-240) HTP(282-283)
            case 0xCB/*[0xCB],...*/ -> decodeCB(bus.cpuReadMem8(state.pcInc1()));
            case 0xCC/*CALL Z,nn*/ -> call_t(state.zf()); // ZUM(257-259) HTP(219-221)
            case 0xCD/*CALL nn*/ -> call(); // ZUM(255-256) HTP(222-223)
            case 0xCE/*ADC A,n*/ -> adc_a_n(bus.cpuReadMem8(state.pcInc1())); // ZUM(146-147) HTP(190-191)
            case 0xCF/*RST 08H*/ -> rst_n(0x08); // ZUM(267-268) HTP(418-419)
            case 0xD0/*RET NC*/ -> ret_t(!state.cf()); // ZUM(261-262) HTP(390-391)
            case 0xD1/*POP DE*/ -> state.de(bus.cpuReadMem16(state.spInc2())); // ZUM(119) HTP(373-374)
            case 0xD2/*JP NC,nn*/ -> jp_t(!state.cf()); // ZUM(239-240) HTP(282-283)
            case 0xD3/*OUT (n),A*/ -> bus.cpuWritePort8((state.a() << 8) | bus.cpuReadMem8(state.pcInc1()), state.a()); // ZUM(279) HTP(368)
            case 0xD4/*CALL NC,nn*/ -> call_t(!state.cf()); // ZUM(257-259) HTP(219-221)
            case 0xD5/*PUSH DE*/ -> bus.cpuWriteMem16(state.spDec2(), state.de()); // ZUM(116) HTP(379-380)
            case 0xD6/*SUB n*/ -> sub_n(bus.cpuReadMem8(state.pcInc1())); // ZUM(148-149) HTP(434-435)
            case 0xD7/*RST 10H*/ -> rst_n(0x10); // ZUM(267-268) HTP(418-419)
            case 0xD8/*RET C*/ -> ret_t(state.cf()); // ZUM(261-262) HTP(390-391)
            case 0xD9/*EXX*/ -> exx(); // ZUM(124) HTP(256)
            case 0xDA/*JP C,nn*/ -> jp_t(state.cf()); // ZUM(239-240) HTP(282-283)
            case 0xDB/*IN A,(n)*/ -> state.a(bus.cpuReadPort8((state.a() << 8) | bus.cpuReadMem8(state.pcInc1()))); // ZUM(269) HTP(263)
            case 0xDC/*CALL C,nn*/ -> call_t(state.cf()); // ZUM(257-259) HTP(219-221)
            case 0xDD/*[0xDD],...*/ -> decodeXY(0xDD, bus.cpuReadMem8(state.pcInc1()), state::ix, state::ix);
            case 0xDE/*SBC A,n*/ -> sbc_a_n(bus.cpuReadMem8(state.pcInc1())); // ZUM(150-151) HTP(420-421)
            case 0xDF/*RST 18H*/ -> rst_n(0x18); // ZUM(267-268) HTP(418-419)
            case 0xE0/*RET PO*/ -> ret_t(!state.pf()); // ZUM(261-262) HTP(390-391)
            case 0xE1/*POP HL*/ -> state.hl(bus.cpuReadMem16(state.spInc2())); // ZUM(119) HTP(373-374)
            case 0xE2/*JP PO,nn*/ -> jp_t(!state.pf()); // ZUM(239-240) HTP(282-283)
            case 0xE3/*EX (SP),HL*/ -> ex_csp_hl(); // ZUM(125) HTP(250-251)
            case 0xE4/*CALL PO,nn*/ -> call_t(!state.pf()); // ZUM(257-259) HTP(219-221)
            case 0xE5/*PUSH HL*/ -> bus.cpuWriteMem16(state.spDec2(), state.hl()); // ZUM(116) HTP(379-380)
            case 0xE6/*AND n*/ -> and_n(bus.cpuReadMem8(state.pcInc1())); // ZUM(152-153) HTP(209-210)
            case 0xE7/*RST 20H*/ -> rst_n(0x20); // ZUM(267-268) HTP(418-419)
            case 0xE8/*RET PE*/ -> ret_t(state.pf()); // ZUM(261-262) HTP(390-391)
            case 0xE9/*JP (HL)*/ -> state.pc(state.hl()); // ZUM(250) HTP(285)
            case 0xEA/*JP PE,nn*/ -> jp_t(state.pf()); // ZUM(239-240) HTP(282-283)
            case 0xEB/*EX DE,HL*/ -> ex_de_hl(); // ZUM(122) HTP(249)
            case 0xEC/*CALL PE,nn*/ -> call_t(state.pf()); // ZUM(257-259) HTP(219-221)
            case 0xED/*[0xED],...*/ -> decodeED(bus.cpuReadMem8(state.pcInc1()));
            case 0xEE/*XOR n*/ -> xor_n(bus.cpuReadMem8(state.pcInc1()));
            case 0xEF/*RST 28H*/ -> rst_n(0x28); // ZUM(267-268) HTP(418-419)
            case 0xF0/*RET P*/ -> ret_t(!state.sf()); // ZUM(261-262) HTP(390-391)
            case 0xF1/*POP AF*/ -> state.af(bus.cpuReadMem16(state.spInc2())); // ZUM(119) HTP(373-374)
            case 0xF2/*JP P,nn*/ -> jp_t(!state.sf()); // ZUM(239-240) HTP(282-283)
            case 0xF3/*DI*/ -> di(); // ZUM(174) HTP(244)
            case 0xF4/*CALL P,nn*/ -> call_t(!state.sf()); // ZUM(257-259) HTP(219-221)
            case 0xF5/*PUSH AF*/ -> bus.cpuWriteMem16(state.spDec2(), state.af()); // ZUM(116) HTP(379-380)
            case 0xF6/*OR n*/ -> or_n(bus.cpuReadMem8(state.pcInc1()));
            case 0xF7/*RST 30H*/ -> rst_n(0x30); // ZUM(267-268) HTP(418-419)
            case 0xF8/*RET M*/ -> ret_t(state.sf()); // ZUM(261-262) HTP(390-391)
            case 0xF9/*LD SP,HL*/ -> state.sp(state.hl()); // ZUM(113) HTP(345)
            case 0xFA/*JP M,nn*/ -> jp_t(state.sf()); // ZUM(239-240) HTP(282-283)
            case 0xFB/*EI*/ -> ei(); // ZUM(175) HTP(247)
            case 0xFC/*CALL M,nn*/ -> call_t(state.sf()); // ZUM(257-259) HTP(219-221)
            case 0xFD/*[0xFD],...*/ -> decodeXY(0xFD, bus.cpuReadMem8(state.pcInc1()), state::iy, state::iy);
            case 0xFE/*CP n*/ -> cp_n(bus.cpuReadMem8(state.pcInc1()));
            case 0xFF/*RST 38H*/ -> rst_n(0x38); // ZUM(267-268) HTP(418-419)
        }
    }

    // -------------------------------------------------------------------------------------------------
    // -------------------- (0xCB)-prefixed op-codes: shift and rotate instructions --------------------
    // -------------------------------------------------------------------------------------------------

    // Decode (0xCB, 0x00-0xFF) op-codes: shift and rotate instructions.
    private void decodeCB(int opCode) {
        int qtr = opCode & 0xC0;
        switch (qtr) {
            case 0x00/*[0xCB],[0x00-0x3F]*/ -> setRegister(opCode, shf_rot_n(opCode, getRegister(opCode))); /* `RLC r` ZUM(194-195) HTP(400-401), `RRC r` ZUM(205-207) HTP(413-414), `RL r` ZUM(202-204) HTP(396-397), `RR r` ZUM(208-210) HTP(410-411), `SLA r` ZUM(211-213) HTP(428-429), `SRA r` ZUM(214-216) HTP(430-431), `SL1 r` undocumented, `SRL r` ZUM(217-219) HTP(432-433) */
            case 0x40/*[0xCB],[0x40-0x7F]*/ -> bit_m_n((opCode & 0x38) >>> 3, getRegister(opCode)); /* `BIT m,r` ZUM(224-225) HTP(217-218), `BIT m,(HL)` ZUM(226-227) HTP(211-212) */
            case 0x80/*[0xCB],[0x80-0xBF]*/ -> setRegister(opCode, res_m_n((opCode & 0x38) >>> 3, getRegister(opCode))); /* `RES m,r` ZUM(236-237) HTP(385-387) */
            case 0xC0/*[0xCB],[0xC0-0xFF]*/ -> setRegister(opCode, set_m_n((opCode & 0x38) >>> 3, getRegister(opCode))); /* `SET m,r` ZUM(232) HTP(425-427), `SET m,(HL)` ZUM(233) HTP(425-427) */
        }
    }

    // -------------------------------------------------------------------------------------------------------
    // -------------------- (0xED)-prefixed op-codes: miscellaneous extended instructions --------------------
    // -------------------------------------------------------------------------------------------------------

    // Decode (0xED, 0x00-0xFF) op-codes: miscellaneous extended instructions.
    private void decodeED(int opCode) {
        int qtr = opCode & 0xC0;
        switch (qtr) {
            case 0x00/*[0xED],[0x00-0x3F]*/ -> nop_(0xED, opCode);
            case 0x40/*[0xED],[0x40-0x7F]*/ -> decodeEDQ1(opCode);
            case 0x80/*[0xED],[0x80-0xBF]*/ -> decodeEDQ2(opCode);
            case 0xC0/*[0xED],[0xC0-0xFF]*/ -> nop_(0xED, opCode);
        }
    }

    // Decode (0xED, 0x40-0x7F) op-codes.
    private void decodeEDQ1(int opCode) {
        switch (opCode) {
            case 0x40/*IN B,(C)*/ -> state.b(bus.cpuReadPort8(state.bc())); // ZUM(270-271) HTP(261-262)
            case 0x41/*OUT (C),B*/ -> bus.cpuWritePort8(state.bc(), state.b()); // ZUM(280-281) HTP(366-367)
            case 0x42/*SBC HL,BC*/ -> sbc_hl_nn(state.bc()); // ZUM(181) HTP(422-423)
            case 0x43/*LD (nn),BC*/ -> bus.cpuWriteMem16(bus.cpuReadMem16(state.pcInc2()), state.bc()); // ZUM(110) HTP(321-322)
            case 0x44/*NEG*/ -> neg(); // ZUM(169) HTP(358)
            case 0x45/*RETN*/ -> retn(); // ZUM(265-266) HTP(394-395)
            case 0x46/*IM 0*/ -> im0(); // ZUM(176) HTP(258)
            case 0x47/*LD I,A*/ -> state.i(state.a()); // ZUM(100) HTP(332)
            case 0x48/*IN C,(C)*/ -> state.c(bus.cpuReadPort8(state.bc())); // ZUM(270-271) HTP(261-262)
            case 0x49/*OUT (C),C*/ -> bus.cpuWritePort8(state.bc(), state.c()); // ZUM(280-281) HTP(366-367)
            case 0x4A/*ADC HL,BC*/ -> adc_hl_nn(state.bc()); // ZUM(180) HTP(192-193)
            case 0x4B/*LD BC,(nn)*/ -> state.bc(bus.cpuReadMem16(bus.cpuReadMem16(state.pcInc2()))); // ZUM(106) HTP(291-292)
            case 0x4C/*NEG'*/ -> neg(); // undocumented
            case 0x4D/*RETI*/ -> reti(); // ZUM(263-264) HTP(392-393)
            case 0x4E/*IM' 0*/ -> im0(); // undocumented
            case 0x4F/*LD R,A*/ -> state.r(state.a()); // ZUM(101) HTP(344)
            case 0x50/*IN D,(C)*/ -> state.d(bus.cpuReadPort8(state.bc())); // ZUM(270-271) HTP(261-262)
            case 0x51/*OUT (C),D*/ -> bus.cpuWritePort8(state.bc(), state.d()); // ZUM(280-281) HTP(366-367)
            case 0x52/*SBC HL,DE*/ -> sbc_hl_nn(state.de()); // ZUM(181) HTP(422-423)
            case 0x53/*LD (nn),DE*/ -> bus.cpuWriteMem16(bus.cpuReadMem16(state.pcInc2()), state.de()); // ZUM(110) HTP(321-322)
            case 0x54/*NEG'*/ -> neg(); // undocumented
            case 0x55/*RETN'*/ -> retn(); // undocumented
            case 0x56/*IM 1*/ -> im1(); // ZUM(177) HTP(259)
            case 0x57/*LD A,I*/ -> ld_a_i(); // ZUM(98) HTP(331)
            case 0x58/*IN E,(C)*/ -> state.e(bus.cpuReadPort8(state.bc())); // ZUM(270-271) HTP(261-262)
            case 0x59/*OUT (C),E*/ -> bus.cpuWritePort8(state.bc(), state.e()); // ZUM(280-281) HTP(366-367)
            case 0x5A/*ADC HL,DE*/ -> adc_hl_nn(state.de()); // ZUM(180) HTP(192-193)
            case 0x5B/*LD DE,(nn)*/ -> state.de(bus.cpuReadMem16(bus.cpuReadMem16(state.pcInc2()))); // ZUM(106) HTP(291-292)
            case 0x5C/*NEG'*/ -> neg(); // undocumented
            case 0x5D/*RETN'*/ -> retn(); // undocumented
            case 0x5E/*IM 2*/ -> im2(); // ZUM(178) HTP(260)
            case 0x5F/*LD A,R*/ -> ld_a_r(); // ZUM(99) HTP(333)
            case 0x60/*IN H,(C)*/ -> state.h(bus.cpuReadPort8(state.bc())); // ZUM(270-271) HTP(261-262)
            case 0x61/*OUT (C),H*/ -> bus.cpuWritePort8(state.bc(), state.h()); // ZUM(280-281) HTP(366-367)
            case 0x62/*SBC HL,HL*/ -> sbc_hl_nn(state.hl()); // ZUM(181) HTP(422-423)
            case 0x63/*LD' (nn),HL*/ -> bus.cpuWriteMem16(bus.cpuReadMem16(state.pcInc2()), state.hl()); // ZUM(110) HTP(321-322)
            case 0x64/*NEG'*/ -> neg(); // undocumented
            case 0x65/*RETN'*/ -> retn(); // undocumented
            case 0x66/*IM' 1*/ -> im1(); // undocumented
            case 0x67/*RRD*/ -> rrd(); // ZUM(222-223) HTP(416-417)
            case 0x68/*IN L,(C)*/ -> state.l(bus.cpuReadPort8(state.bc())); // ZUM(270-271) HTP(261-262)
            case 0x69/*OUT (C),L*/ -> bus.cpuWritePort8(state.bc(), state.l()); // ZUM(280-281) HTP(366-367)
            case 0x6A/*ADC HL,HL*/ -> adc_hl_nn(state.hl()); // ZUM(180) HTP(192-193)
            case 0x6B/*LD' HL,(nn)*/ -> state.hl(bus.cpuReadMem16(state.pcInc2())); // ZUM(106) HTP(291-292)
            case 0x6C/*NEG'*/ -> neg(); // undocumented
            case 0x6D/*RETN'*/ -> retn(); // undocumented
            case 0x6E/*IM' 1*/ -> im1(); // undocumented
            case 0x6F/*RLD*/ -> rld(); // ZUM(220-221) HTP(408-409)
            case 0x70/*IN' (C)*/ -> bus.cpuReadPort8(state.bc()); // undocumented
            case 0x71/*OUT' (C)-*/ -> bus.cpuWritePort8(state.bc(), 0); // undocumented
            case 0x72/*SBC HL,SP*/ -> sbc_hl_nn(state.sp()); // ZUM(181) HTP(422-423)
            case 0x73/*LD (nn),SP*/ -> bus.cpuWriteMem16(bus.cpuReadMem16(state.pcInc2()), state.sp()); // ZUM(110) HTP(321-322)
            case 0x74/*NEG'*/ -> neg(); // undocumented
            case 0x75/*RETN'*/ -> retn(); // undocumented
            case 0x76/*IM' 1*/ -> im1(); // undocumented
            case 0x78/*IN A,(C)*/ -> state.a(bus.cpuReadPort8(state.bc())); // ZUM(270-271) HTP(261-262)
            case 0x79/*OUT (C),A*/ -> bus.cpuWritePort8(state.bc(), state.a()); // ZUM(280-281) HTP(366-367)
            case 0x7A/*ADC HL,SP*/ -> adc_hl_nn(state.sp()); // ZUM(180) HTP(192-193)
            case 0x7B/*LD SP,(nn)*/ -> state.sp(bus.cpuReadMem16(bus.cpuReadMem16(state.pcInc2()))); // ZUM(106) HTP(291-292)
            case 0x7C/*NEG'*/ -> neg(); // undocumented
            case 0x7D/*RETN'*/ -> retn(); // undocumented
            case 0x7E/*IM' 2*/ -> im2(); // undocumented
            default -> nop_(0xED, opCode); // e.g. [0xED],[0x77] and [0xED],[0x7F]
        }
    }

    // Decode (0xED, 0x80-0xBF) op-codes.
    private void decodeEDQ2(int opCode) {
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
            default -> nop_(0xED, opCode); // e.g. 0xED,0x80-0x9F
        }
    }

    // ------------------------------------------------------------------------------------------------------
    // -------------------- (0xDD|0xFD)-prefixed op-codes: IX or IY indexed instructions --------------------
    // ------------------------------------------------------------------------------------------------------

    // Decode (0xDD|0xFD, 0x00-0xFF) op-codes: IX or IY indexed instructions.
    private void decodeXY(int prefix1, int opCode, IntSupplier rrGet, IntConsumer rrSet) {
        int qtr = opCode & 0xC0;
        switch (qtr) {
            case 0x00/*[0xDD|0xFD].[0x00-0x3F]*/ -> decodeXYQ0(prefix1, opCode, rrGet, rrSet);
            case 0x40/*[0xDD|0xFD],[0x40-0x7F]*/ -> decodeXYQ1(prefix1, opCode, rrGet, rrSet);
            case 0x80/*[0xDD|0xFD],[0x80-0xBF]*/ -> decodeXYQ2(opCode, rrGet);
            case 0xC0/*[0xDD|0xFD],[0xC0-0xFF]*/ -> decodeXYQ3(prefix1, opCode, rrGet, rrSet);
        }
    }

    // Decode (0xDD|0xFD, 0x00-0x3F) op-codes.
    private void decodeXYQ0(int prefix1, int opCode, IntSupplier rrGet, IntConsumer rrSet) {
        switch (opCode) {
            case 0x04/*INC B*/ -> state.b(inc_n(state.b())); // undocumented
            case 0x05/*DEC B*/ -> state.b(dec_n(state.b())); // undocumented
            case 0x06/*LD B,n*/ -> state.b(bus.cpuReadMem8(state.pcInc1())); // undocumented
            case 0x09/*ADD IXY,BC*/ -> add_rr_nn(rrGet, rrSet, state.bc()); // ZUM(182-18) HTP(205-208)
            case 0x0C/*INC C*/ -> state.c(inc_n(state.c())); // undocumented
            case 0x0D/*DEC C*/ -> state.c(dec_n(state.c())); // undocumented
            case 0x0E/*LD C,n*/ -> state.c(bus.cpuReadMem8(state.pcInc1())); // undocumented
            case 0x14/*INC D*/ -> state.d(inc_n(state.d())); // undocumented
            case 0x15/*DEC D*/ -> state.d(dec_n(state.d())); // undocumented
            case 0x16/*LD D,n*/ -> state.d(bus.cpuReadMem8(state.pcInc1())); // undocumented
            case 0x19/*ADD IXY,DE*/ -> add_rr_nn(rrGet, rrSet, state.de()); // ZUM(182-18) HTP(205-208)
            case 0x1C/*INC E*/ -> state.e(inc_n(state.e())); // undocumented
            case 0x1D/*DEC E*/ -> state.e(dec_n(state.e())); // undocumented
            case 0x1E/*LD E,n*/ -> state.e(bus.cpuReadMem8(state.pcInc1())); // undocumented
            case 0x21/*LD IXY,nn*/ -> rrSet.accept(bus.cpuReadMem16(state.pcInc2())); // ZUM(103) HTP(336-337)
            case 0x22/*LD (nn),IXY*/ -> bus.cpuWriteMem16(bus.cpuReadMem16(state.pcInc2()), rrGet.getAsInt()); // ZUM(111-112) HTP(325-328)
            case 0x23/*INC IXY*/ -> rrSet.accept(inc_nn(rrGet.getAsInt())); // ZUM(185) HTP(272)
            case 0x24/*INC IXYH*/ -> high(rrGet, rrSet, inc_n(high(rrGet))); // undocumented
            case 0x25/*DEC IXYH*/ -> high(rrGet, rrSet, dec_n(high(rrGet))); // undocumented
            case 0x26/*LD IXYH,n*/ -> high(rrGet, rrSet, bus.cpuReadMem8(state.pcInc1())); // undocumented
            case 0x29/*ADD IXY,IXY*/ -> add_rr_nn(rrGet, rrSet, rrGet.getAsInt()); // ZUM(182-18) HTP(205-208)
            case 0x2A/*LD IXY,(nn)*/ -> rrSet.accept(bus.cpuReadMem16(bus.cpuReadMem16(state.pcInc2()))); // ZUM(107-108) HTP(338-339,342-343)
            case 0x2B/*DEC IXY*/ -> rrSet.accept(dec_nn(rrGet.getAsInt())); // ZUM(186) HTP(242-243)
            case 0x2C/*INC IXYL*/ -> low(rrGet, rrSet, inc_n(low(rrGet))); // undocumented
            case 0x2D/*DEC IXYL*/ -> low(rrGet, rrSet, dec_n(low(rrGet))); // undocumented
            case 0x2E/*LD IXYL,n*/ -> low(rrGet, rrSet, bus.cpuReadMem8(state.pcInc1())); // undocumented
            case 0x34/*INC (IXY+n)*/ -> { // ZUM(162-163) HTP(268-271)
                int address = indexed(rrGet.getAsInt());
                bus.cpuWriteMem8(address, inc_n(bus.cpuReadMem8(address)));
            }
            case 0x35/*DEC (IXY+n)*/ -> { // ZUM(164-165) HTP(238-239)
                int address = indexed(rrGet.getAsInt());
                bus.cpuWriteMem8(address, dec_n(bus.cpuReadMem8(address)));
            }
            case 0x36/*LD (IXY+n),n*/ -> { // ZUM(90-91) HTP(309-312)
                int address = indexed(rrGet.getAsInt());
                bus.cpuWriteMem8(address, bus.cpuReadMem8(state.pcInc1()));
            }
            case 0x39/*ADD IXY,SP*/ -> add_rr_nn(rrGet, rrSet, state.sp()); // ZUM(182-18) HTP(205-208)
            case 0x3C/*INC A*/ -> state.a(inc_n(state.a())); // undocumented
            case 0x3D/*DEC A*/ -> state.a(dec_n(state.a())); // undocumented
            case 0x3E/*LD A,n*/ -> state.a(bus.cpuReadMem8(state.pcInc1())); // undocumented
            default -> nop_(prefix1, opCode);
        }
    }

    // Decode (0xDD|0xFD, 0x40-0x7F) op-codes.
    private void decodeXYQ1(int prefix1, int opCode, IntSupplier rrGet, IntConsumer rrSet) {
        if (opCode == 0x76) { // not a valid op-code
            nop_(prefix1, opCode);
            return;
        }
        int src = (opCode & 0x07);
        int dst = (opCode & 0x38) >>> 3;
        int val = switch (src) {
            case 0x00/*LD r,B*/ -> state.b(); // undocumented
            case 0x01/*LD r,C*/ -> state.c(); // undocumented
            case 0x02/*LD r,D*/ -> state.d(); // undocumented
            case 0x03/*LD r,E*/ -> state.e(); // undocumented
            case 0x04/*LD r,H/IXYH*/ -> dst == 0x06 ? state.h() : high(rrGet); // undocumented if IXYH
            case 0x05/*LD r,L/IXYL*/ -> dst == 0x06 ? state.l() : low(rrGet); // undocumented if IXYL
            case 0x06/*LD r,(IXY+n)*/ -> bus.cpuReadMem8(indexed(rrGet.getAsInt())); // ZUM(84-85) HTP(305-308)
            case 0x07/*LD r,A*/ -> state.a(); // undocumented
            default -> 0; // unreachable
        };
        switch (dst) {
            case 0x00/*LD B,r*/ -> state.b(val); // undocumented
            case 0x01/*LD C,r*/ -> state.c(val); // undocumented
            case 0x02/*LD D,r*/ -> state.d(val); // undocumented
            case 0x03/*LD E,r*/ -> state.e(val); // undocumented
            case 0x04/*LD H/IXYH,r*/ -> {if (src == 0x06) {state.h(val);} else {high(rrGet, rrSet, val);}} // ZUM(84-85) HTP(305-308)
            case 0x05/*LD L/IXYL,r*/ -> {if (src == 0x06) {state.l(val);} else {low(rrGet, rrSet, val);}} // ZUM(84-85) HTP(305-308)
            case 0x06/*LD (IXY+n),r*/ -> bus.cpuWriteMem8(indexed(rrGet.getAsInt()), val); // ZUM(87-88) HTP(313-316)
            case 0x07/*LD A,r*/ -> state.a(val); // undocumented
        }
    }

    // Decode (0xDD|0xFD, 0x80-0xBF) op-codes.
    private void decodeXYQ2(int opCode, IntSupplier rrGet) {
        /* `ADD A,r` ZUM(144-145) HTP(196-199), `ADC A,r` ZUM(146-147) HTP(190-191), `SUB r` ZUM(148-149) HTP(434-435),
           `SBC A,r` ZUM(150-151) HTP(420-421), `AND r` ZUM(152-153) HTP(209-210), `XOR r` ZUM(156-157) HTP(436-437),
           `OR r` ZUM(154-155) HTP(360-361), `CP r` ZUM(158-159) HTP(225-226), `(IXY+n)` ZUM(144-145) HTP(196-199) */
        int val = switch (opCode & 0x07) {
            case 0x00/*aop B*/ -> state.b();
            case 0x01/*aop C*/ -> state.c();
            case 0x02/*aop D*/ -> state.d();
            case 0x03/*aop E*/ -> state.e();
            case 0x04/*aop IXYH*/ -> high(rrGet);
            case 0x05/*aop IXYL*/ -> low(rrGet);
            case 0x06/*aop (IXY)*/ -> bus.cpuReadMem8(indexed(rrGet.getAsInt())); //
            case 0x07/*aop A*/ -> state.a();
            default -> 0; // unreachable
        };
        alu_acc_n(opCode, val);
    }

    // Decode (0xDD|0xFD, 0xC0-0xFF) op-codes.
    private void decodeXYQ3(int prefix1, int opCode, IntSupplier rrGet, IntConsumer rrSet) {
        switch (opCode) {
            case 0xCB/*[0xDD|0xFD],[0xCB],...*/ -> decodeXYCB(indexed(rrGet.getAsInt()));
            case 0xDD/*[0xDD|0xFD],[0xDD],...*/ -> decodeXY(0xDD, bus.cpuReadMem8(state.pcInc1()), state::ix, state::ix);
            case 0xE1/*POP IXY*/ -> rrSet.accept(bus.cpuReadMem16(state.spInc2())); // ZUM(120) HTP(375-376)
            case 0xED/*[0xDD|0xFD],[0xED],...*/ -> decodeED(bus.cpuReadMem8(state.pcInc1()));
            case 0xE3/*EX (SP),IXY*/ -> ex_csp_rr(rrGet, rrSet); // ZUM(126-127) HTP(252-255)
            case 0xE5/*PUSH IXY*/ -> bus.cpuWriteMem16(state.spDec2(), rrGet.getAsInt()); // ZUM(117) HTP(381-382)
            case 0xE9/*JP (IXY)*/ -> state.pc(rrGet.getAsInt()); // ZUM(251) HTP(286)
            case 0xF9/*LD SP,IXY*/ -> state.sp(rrGet.getAsInt()); // ZUM(114-115) HTP(346-347)
            case 0xFD/*[0xDD|0xFD],[0xFD],...*/ -> decodeXY(0xFD, bus.cpuReadMem8(state.pcInc1()), state::iy, state::iy);
            default -> nop_(prefix1, opCode);
        }
    }

    // --------------------------------------------------------------------------------------------------------------
    // -------------------- (0xDD|0xFD, 0xCB, index)-prefixed op-codes: indexed bit instructions --------------------
    // --------------------------------------------------------------------------------------------------------------

    // Decode (0xDD|0xFD, 0xCB, index, 0x00-0xFF) op-codes: indexed bit instructions
    private void decodeXYCB(int address) {
        int opCode = bus.cpuReadMem8(state().pcInc1());
        int qtr = opCode & 0xC0;
        switch (qtr) {
            case 0x00/*[0xDD|0xFD],[0xCB],[0x00-0x3F]*/ -> setAdditionalRegister(address, opCode, shf_rot_n(opCode, bus.cpuReadMem8(address))); /* `RLC (IXY+n),r` ZUM(198-201) HTP(404-407), `RRC (IXY+n),r` ZUM(205-207) HTP(413-414), `RL (IXY+n),r` ZUM(202-204) HTP(396-397), `RR (IXY+n),r` ZUM(208-210) HTP(410-411), `SLA (IXY+n),r` ZUM(211-213) HTP(428-429), `SRA (IXY+n),r` ZUM(214-216) HTP(430-431), `SL1 (IXY+n),r` undocumented, SRL (IXY+n),r` ZUM(217-219) HTP(432-433) */
            case 0x40/*[0xDD|0xFD],[0xCB],[0x40-0x7F]*/ -> bit_m_n((opCode & 0x38) >>> 3, bus.cpuReadMem8(address)); /* `BIT m,(IXY+n)` ZUM(228-231) HTP(213-216) */
            case 0x80/*[0xDD|0xFD],[0xCB],[0x80-0xBF]*/ -> setAdditionalRegister(address, opCode, res_m_n((opCode & 0x38) >>> 3, bus.cpuReadMem8(address))); /* `RES m,(IXY+n),r` ZUM(236-237) HTP(385-387) */
            case 0xC0/*[0xDD|0xFD],[0xCB],[0xC0-0xFF]*/ -> setAdditionalRegister(address, opCode, set_m_n((opCode & 0x38) >>> 3, bus.cpuReadMem8(address))); /* `SET m,(IXY+n),r` ZUM(234-235) HTP(425-427) */
        }
    }

    // ------------------------------------------------------------------------------
    // -------------------- Instruction implementation functions --------------------
    // ------------------------------------------------------------------------------

    private void nop() {
        // do nothing.
    }

    private void nop_(int ignorePrefix, int ignoreOpCode) {
        // do nothing.
    }

    private void ld_a_i() { // ZUM(98) HTP(331)
        int rm = state.i();
        state.nf(false);
        state.pf(false /*TODO: contents of IFF2*/);
        state.hf(false);
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        state.a(rm);
    }

    private void ld_a_r() { // ZUM(99) HTP(333)
        int rm = state.r();
        state.nf(false);
        state.pf(false /*TODO: contents of IFF2*/);
        state.hf(false);
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        state.a(rm);
    }

    private void ex_de_hl() { // ZUM(122) HTP(249)
        int nn = state.de();
        state.de(state.hl());
        state.hl(nn);
    }

    private void ex_af_aaf() { // ZUM(123) HTP(248)
        state.alt_af();
    }

    private void exx() { // ZUM(124) HTP(256)
        state.alt_bcdehl();
    }

    private void ex_csp_hl() { // ZUM(125) HTP(250-251)
        int nn = state.hl();
        state.hl(bus.cpuReadMem16(state.sp()));
        bus.cpuWriteMem16(state.sp(), nn);
    }

    private void ex_csp_rr(IntSupplier rrGet, IntConsumer rrSet) { // ZUM(126-127) HTP(252-255)
        int nn = rrGet.getAsInt();
        rrSet.accept(bus.cpuReadMem16(state.sp()));
        bus.cpuWriteMem16(state.sp(), nn);
    }

    private void ldi() { // ZUM(128) HTP(352-353)
        bus.cpuWriteMem8(state.de(), bus.cpuReadMem8(state.hl()));
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
        bus.cpuWriteMem8(state.de(), bus.cpuReadMem8(state.hl()));
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
        int n = bus.cpuReadMem8(state.hl());
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
        int n = bus.cpuReadMem8(state.hl());
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
        state.pf(((state.a() ^ ~n) & (state.a() ^ rm)) >= 0x80);
        //state.hf((state.a() & 0xF) + (n & 0xF) >= 0x10);
        state.hf(((state.a() ^ n ^ rm) & 0x10) != 0);
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        state.a(rm);
    }

    private void adc_a_n(int n) { // ZUM(146-147) HTP(190-191)
        int ru = state.a() + n + (state.cf() ? 1 : 0);
        int rm = ru & 0xFF;
        state.cf(rm != ru);
        state.nf(false);
        state.pf(((state.a() ^ ~n) & (state.a() ^ rm)) >= 0x80);
        state.hf(((state.a() ^ n ^ rm) & 0x10) != 0);
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        state.a(rm);
    }

    private void sub_n(int n) { // ZUM(148-149) HTP(434-435)
        int ru = state.a() - n;
        int rm = ru & 0xFF;
        state.cf(rm != ru);
        state.nf(true);
        state.pf(((state.a() ^ n) & (state.a() ^ rm)) >= 0x80);
        //state.hf((state.a() & 0xF) < (n & 0xF));
        state.hf(((state.a() ^ n ^ rm) & 0x10) != 0);
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        state.a(rm);
    }

    private void sbc_a_n(int n) { // ZUM(150-151) HTP(420-421)
        int ru = state.a() - n - (state.cf() ? 1 : 0);
        int rm = ru & 0xFF;
        state.cf(rm != ru);
        state.nf(true);
        state.pf(((state.a() ^ n) & (state.a() ^ rm)) >= 0x80);
        state.hf(((state.a() ^ n ^ rm) & 0x10) != 0);
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        state.a(rm);
    }

    private void and_n(int n) { // ZUM(152-153) HTP(209-210)
        int rm = state.a() & n;
        state.cf(false);
        state.nf(false);
        state.pf(parity(rm));
        state.hf(true);
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
        state.pf(((state.a() ^ n) & (state.a() ^ rm)) >= 0x80);
        //state.hf((state.a() & 0xF) < (n & 0xF));
        state.hf(((state.a() ^ n ^ rm) & 0x10) != 0);
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
        int ru = state.a();
        int ff = (state.hf() || (ru & 0x0F) > 0x09) ? 0x06 : 0x00;
        if (state.cf() || ru > 0x9F || (ru > 0x8F && (ru & 0x0F) > 0x09)) {
            ff |= 0x60;
        }
        int rm = (state.nf() ? (ru - ff) : (ru + ff)) & 0xFF;
        if (ru > 0x99) {
            state.cf(true);
        }
        state.pf(parity(rm));
        state.hf(state.nf() ? (ff & 0x0F) > (ru & 0x0F) : (ru & 0x0F) + (ff & 0xF) >= 0x10);
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        state.a(rm);
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
        boolean cf = state.cf();
        state.cf(!cf);
        state.nf(false);
        state.hf(cf);
    }

    private void scf() { // ZUM(171) HTP(424)
        state.cf(true);
        state.nf(false);
        state.hf(false);
    }

    private void di() { // ZUM(174) HTP(244)
        // TODO implementation (IFF1 = IFF2 = false; diflag = true;/*prevent int for one instr*/)
    }

    private void ei() { // ZUM(175) HTP(247)
        // TODO implementation (IFF1 = IFF2 = true; diflag = true;/*prevent int for one instr*/)
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
        state.hf((state.hl() & 0xFFF) + (nn & 0xFFF) >= 0x1000);
        state.hl(rm);
    }

    private void add_rr_nn(IntSupplier rrGet, IntConsumer rrSet, int nn) { // ZUM(182-183) HTP(205-208)
        int ru = rrGet.getAsInt() + nn;
        int rm = ru & 0xFFFF;
        state.cf(rm != ru);
        state.nf(false);
        state.hf((rrGet.getAsInt() & 0xFFF) + (nn & 0xFFF) >= 0x1000);
        rrSet.accept(rm);
    }

    private void adc_hl_nn(int nn) { // ZUM(180) HTP(192-193)
        int ru = state.hl() + nn + (state.cf() ? 1 : 0);
        int rm = ru & 0xFFFF;
        state.cf(rm != ru);
        state.nf(false);
        state.pf(((state.hl() ^ ~nn) & (state.hl() ^ rm)) >= 0x8000);
        state.hf(((state.hl() ^ nn ^ rm) & 0x1000) != 0);
        state.zf(rm == 0);
        state.sf((rm & 0x8000) != 0);
        state.hl(rm);
    }

    private void sbc_hl_nn(int nn) { // ZUM(181) HTP(422-423)
        int ru = state.hl() - nn - (state.cf() ? 1 : 0);
        int rm = ru & 0xFFFF;
        state.cf(rm != ru);
        state.nf(true);
        state.pf(((state.hl() ^ nn) & (state.hl() ^ rm)) >= 0x8000);
        state.hf(((state.hl() ^ nn ^ rm) & 0x1000) != 0);
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
        int n = bus.cpuReadMem8(state.hl());
        bus.cpuWriteMem8(state.hl(), ((n & 0x0F) << 4) | (state.a() & 0x0F));
        int rm = (state.a() & 0xF0) | ((n & 0xF0) >>> 4);
        state.nf(false);
        state.pf(parity(rm));
        state.hf(false);
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        state.a(rm);
    }

    private void rrd() { // ZUM(222-223) HTP(416-417)
        int n = bus.cpuReadMem8(state.hl());
        bus.cpuWriteMem8(state.hl(), ((state.a() & 0x0F) << 4) | ((n & 0xF0) >>> 4));
        int rm = (state.a() & 0xF0) | (n & 0x0F);
        state.nf(false);
        state.pf(parity(rm));
        state.hf(false);
        state.zf(rm == 0);
        state.sf((rm & 0x80) != 0);
        state.a(rm);
    }

    private void bit_m_n(int m, int n) { // ZUM(224-231) HTP(211-218)
        boolean bitm = (n & (1 << (m & 0x07))) != 0;
        state.nf(false);
        state.pf(!bitm);
        state.hf(true);
        state.zf(!bitm);
        state.sf(m == 7 && bitm);
    }

    private int set_m_n(int m, int n) { // ZUM(232-235) HTP(425-427)
        return n | (1 << (m & 0x07));
    }

    private int res_m_n(int m, int n) { // ZUM(236-237) HTP(385-387)
        return n & ~(1 << (m & 0x07));
    }

    private void jp() { // ZUM(238) HTP(284)
        state.pc(bus.cpuReadMem16(state.pc()));
    }

    private void jp_t(boolean t) { // ZUM(239-240) HTP(282-283)
        if (t) { jp(); } else { state.pcInc2(); }
    }

    private void jr() { // ZUM(241) HTP(290)
        state.pc(indexed(state.pc() + 1));
    }

    private void jr_t(boolean t) { // ZUM(242-249) HTP(288-289
        if (t) { jr(); } else { state.pcInc1(); }
    }

    private void djnz() { // ZUM(253-254) HTP(245-246)
        state.b(state.b() - 1);
        if (state.b() != 0) { jr(); } else { state.pcInc1(); }
    }

    private void call() { // ZUM(255-256) HTP(222-223)
        int address = bus.cpuReadMem16(state.pcInc2());
        bus.cpuWriteMem16(state.spDec2(), state.pc());
        state.pc(address);
    }

    private void call_t(boolean t) { // ZUM(257-259) HTP(219-221)
        if (t) { call(); } else { state.pcInc2(); }
    }

    private void ret() { // ZUM(260) HTP(388-389)
        state.pc(bus.cpuReadMem16(state.spInc2()));
    }

    private void ret_t(boolean t) { // ZUM(261-262) HTP(390-391)
        if (t) { ret(); }
    }

    private void reti() { // ZUM(263-264) HTP(392-393)
        // TODO implementation (IFF1 = IFF2;?)
        ret();
    }

    private void retn() { // ZUM(265-266) HTP(394-395)
        // TODO implementation (IFF1 = IFF2;)
        ret();
    }

    private void rst_n(int n) { // ZUM(267-268) HTP(418-419)
        bus.cpuWriteMem16(state.spDec2(), state.pc());
        state.pc(n & 0x38);
    }

    private void ini() { // ZUM(272) HTP(278-279)
        int rm = bus.cpuReadPort8(state.bc());
        bus.cpuWriteMem8(state.hl(), rm);
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
        int rm = bus.cpuReadPort8(state.bc());
        bus.cpuWriteMem8(state.hl(), rm);
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
        bus.cpuWritePort8(state.bc(), bus.cpuReadMem8(state.hl()));
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
        bus.cpuWritePort8(state.bc(), bus.cpuReadMem8(state.hl()));
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
     * Used by non-indexed instructions to retrieve the value of an 8-bit register.
     *
     * @param r The register to get as a bit-pattern (`(HL)` or `M` pseudo-register is 0b1110).
     * @return The value gotten from the register.
     */
    private int getRegister(int r) {
        return switch (r & 0x07) {
            case 0x00 -> state.b();
            case 0x01 -> state.c();
            case 0x02 -> state.d();
            case 0x03 -> state.e();
            case 0x04 -> state.h();
            case 0x05 -> state.l();
            case 0x06 -> bus.cpuReadMem8(state.hl());
            case 0x07 -> state.a();
            default -> 0; // unreachable
        };
    }

    /**
     * Used by non-indexed instructions to set an 8-bit register.
     *
     * @param r The register to set as a bit-pattern (`(HL)` or `M` pseudo-register is 0b1110).
     * @param n THe value to set into the register.
     */
    private void setRegister(int r, int n) {
        switch (r & 0x07) {
            case 0x00 -> state.b(n);
            case 0x01 -> state.c(n);
            case 0x02 -> state.d(n);
            case 0x03 -> state.e(n);
            case 0x04 -> state.h(n);
            case 0x05 -> state.l(n);
            case 0x06 -> bus.cpuWriteMem8(state.hl(), n);
            case 0x07 -> state.a(n);
        }
    }

    /**
     * Used by some of the 0xDD,0xCB or 0xFD,0xCB prefixed instructions (undocumented ones) to set another register,
     * in addition to setting the memory value at `(IXY+n)`.
     *
     * @param r The register to set as a bit-pattern (`(IXY+n)` is ignored as it will be set anyway).
     * @param n THe value to set into the register (ignored for `(IX+n)` 0b1110).
     */
    private void setAdditionalRegister(int address, int r, int n) {
        bus.cpuWriteMem8(address, n);
        switch (r & 0x07) {
            case 0x00 -> state.b(n);
            case 0x01 -> state.c(n);
            case 0x02 -> state.d(n);
            case 0x03 -> state.e(n);
            case 0x04 -> state.h(n);
            case 0x05 -> state.l(n);
            case 0x06 -> {}
            case 0x07 -> state.a(n);
        }
    }

    /**
     * Arithmetic and logical operations that involve the accumulator (or `A` register).
     *
     * @param opCode bits 3-5 determine which operation to select.
     * @param n the parameter to the operation that is not the `A` register.
     */
    private void alu_acc_n(int opCode, int n) {
        switch ((opCode & 0x38) >> 3) {
            case 0x00/*ADD A,n*/ -> add_a_n(n); // ZUM(140-141) HTP(201-202)
            case 0x01/*ADC A,n*/ -> adc_a_n(n); // ZUM(146-147) HTP(190-191)
            case 0x02/*SUB n*/ -> sub_n(n); // ZUM(148-149) HTP(434-435)
            case 0x03/*SBC A,n*/ -> sbc_a_n(n); // ZUM(150-151) HTP(420-421)
            case 0x04/*AND n*/ -> and_n(n); // ZUM(152-153) HTP(209-210)
            case 0x05/*XOR n*/ -> xor_n(n); // ZUM(156-157) HTP(436-437)
            case 0x06/*OR n*/ -> or_n(n); // ZUM(154-155) HTP(360-361)
            case 0x07/*CP n*/ -> cp_n(n); // ZUM(158-159) HTP(225-226)
        }
    }

    /**
     * Shift and rotate operations that involve 8 bit values.
     *
     * @param opCode bits 3-5 determine which operation to select.
     * @param n the parameter to the operation.
     * @return the result of performing the operation on the parameter.
     */
    private int shf_rot_n(int opCode, int n) {
        return switch ((opCode & 0x38) >> 3) {
            case 0x00/*RLC n*/ -> rlc_n(n); // ZUM(194-195) HTP(400-401)
            case 0x01/*RRC n*/ -> rrc_n(n); // ZUM(205-207) HTP(413-414)
            case 0x02/*RL n*/ -> rl_n(n); // ZUM(202-204) HTP(396-397)
            case 0x03/*RR n*/ -> rr_n(n); // ZUM(208-210) HTP(410-411)
            case 0x04/*SLA n*/ -> sla_n(n); // ZUM(211-213) HTP(428-429)
            case 0x05/*SRA n*/ -> sra_n(n); // ZUM(214-216) HTP(430-431)
            case 0x06/*SL1 n*/ -> sl1_n(n); // undocumented
            case 0x07/*SRL n*/ -> srl_n(n); // ZUM(217-219) HTP(432-433)
            default -> throw new IllegalStateException("Unexpected value in shf_rot switch: " + ((opCode & 0x38) >> 3));
        };
    }

    /**
     * Retrieve the MSB (byte) from a 16-bit value.
     *
     * @param rrGet IntSupplier for 16-bit register in question
     * @return high 8-bits from the 16-bit value.
     */
    private int high(IntSupplier rrGet) {
        return (rrGet.getAsInt() & 0xFF00) >>> 8;
    }

    /**
     * Retrieve the LSB (byte) from a 16-bit value.
     *
     * @param rrGet IntSupplier for 16-bit register in question
     * @return low 8-bits from the 16-bit value.
     */
    private int low(IntSupplier rrGet) {
        return rrGet.getAsInt() & 0xFF;
    }

    /**
     * Set the MSB (byte) in the 16-bit value.
     *
     * @param rrGet IntSupplier for 16-bit register in question
     * @param rrSet IntConsumer for 16-bit register in question
     */
    private void high(IntSupplier rrGet, IntConsumer rrSet, int n) {
        rrSet.accept(((n & 0xFF) << 8 )| (rrGet.getAsInt() & 0xFF));
    }
    /**
     * Set the LSB (byte) in the 16-bit value.
     *
     * @param rrGet IntSupplier for 16-bit register in question
     * @param rrSet IntConsumer for 16-bit register in question
     */
    private void low(IntSupplier rrGet, IntConsumer rrSet, int n) {
        rrSet.accept((rrGet.getAsInt() & 0xFF00) | (n & 0xFF));
    }

    /**
     * @return the address added to the next byte in memory as a signed offset (in the range -128 to +127).
     * For example, `JR n` or `DJNZ n` (address would be `PC + 1`) or `LD A,(IX+n)` (address would be `IX`) might use
     * this function during instruction decoding.
     */
    private int indexed(int nn) {
        return (nn + (int)(byte)bus.cpuReadMem8(state.pcInc1())) & 0xFFFF;
    }

    /**
     * @return true if n has even parity, false if n has odd parity (only rightmost 8 bits of n are considered).
     * Note this is the reverse of what you might normally expect because that is how the Z80's P/V condition bit works.
     */
    private boolean parity(int n) {
        return ((0x6996 >>> ((n ^ (n >>> 4)) & 0x0F)) & 1) == 0;
    }
}
