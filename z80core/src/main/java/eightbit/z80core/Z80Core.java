package eightbit.z80core;

import eightbit.IBus;
import eightbit.ICore;

public class Z80Core implements ICore {
    private IBus bus;
    private int cycles;
    private int a, b, c, d, e, h, l, w, z;
    private int a_, b_, c_, d_, e_, f_, h_, l_;
    private int ix, iy, pc, sp;
    private boolean halted, cf, nf, pf, f3, hf, f5, zf, sf;

    public Z80Core(final IBus bus) {
        this.bus = bus;
    }

    @Override
    public final void resetCycleCount() {
        cycles = 0;
    }

    @Override
    public final int getCycleCount() {
        return cycles;
    }

    @Override
    public final void runOneInstruction() {
        if (halted) {
            // execute an effective NOP
            cycles += 4;
            return;
        }
        int opCode = bus.readOpCode(pc++);
        int x = (opCode & 0xC0); // values 0x00,0x40,0x80 or 0x80
        int y = (opCode & 0x38); // not shifting here to avoid unnecessary work
        int z = (opCode & 0x07);
        
        switch (opCode) {
            case 0:
                // NOP
                cycles += 4;
                break;
            case 118:
                // HALT
                cycles += 4;
                halted = true;
                break;
        }
    }

    // 8-Bit Main Registers
    public final int getA() {
        return a;
    }
    public final void setA(final int n) {
        a = n&0xFF;
    }
    public final int getB() {
        return b;
    }
    public final void setB(final int n) {
        b = n&0xFF;
    }
    public final int getC() {
        return c;
    }
    public final void setC(final int n) {
        c = n&0xFF;
    }
    public final int getD() {
        return d;
    }
    public final void setD(final int n) {
        d = n&0xFF;
    }
    public final int getE() {
        return e;
    }
    public final void setE(final int n) {
        e = n&0xFF;
    }
    public final int getH() {
        return h;
    }
    public final void setH(final int n) {
        h = n&0xFF;
    }
    public final int getL() {
        return l;
    }
    public final void setL(final int n) {
        l = n&0xFF;
    }
    public final int getW() {
        return w;
    }
    public final void setW(final int n) {
        w = n&0xFF;
    }
    public final int getZ() {
        return z;
    }
    public final void setZ(final int n) {
        z = n&0xFF;
    }
    
    // 8-Bit Alternate Registers
    public final int getA_() {
        return a_;
    }
    public final void setA_(final int n) {
        a_ = n&0xFF;
    }
    public final int getB_() {
        return b_;
    }
    public final void setB_(final int n) {
        b_ = n&0xFF;
    }
    public final int getC_() {
        return c_;
    }
    public final void setC_(final int n) {
        c_ = n&0xFF;
    }
    public final int getD_() {
        return d_;
    }
    public final void setD_(final int n) {
        d_ = n&0xFF;
    }
    public final int getE_() {
        return e_;
    }
    public final void setE_(final int n) {
        e_ = n&0xFF;
    }
    public final int getF_() {
        return f_;
    }
    public final void setF_(final int n) {
        f_ = n&0xFF;
    }
    public final int getH_() {
        return h_;
    }
    public final void setH_(final int n) {
        h_ = n&0xFF;
    }
    public final int getL_() {
        return l_;
    }
    public final void setL_(final int n) {
        l_ = n&0xFF;
    }
    
    // Main Flags
    public final int getF() {
        return (cf?0x01:0) | (nf?0x02:0) | (pf?0x04:0) | (f3?0x08:0) |
                (hf?0x10:0) | (f5?0x20:0) | (zf?0x40:0) | (sf?0x80:0);
    }
    public final void setF(final int n) {
        cf = (n&0x01)!=0; nf = (n&0x02)!=0; pf = (n&0x04)!=0; f3 = (n&0x08)!=0;
        hf = (n&0x10)!=0; f5 = (n&0x20)!=0; zf = (n&0x40)!=0; sf = (n&0x80)!=0;
    }
    public final boolean isCF() {
        return cf;
    }
    public final void setCF(final boolean n) {
        cf = n;
    }
    public final boolean isNF() {
        return nf;
    }
    public final void setNF(final boolean n) {
        nf = n;
    }
    public final boolean isPF() {
        return pf;
    }
    public final void setPF(final boolean n) {
        pf = n;
    }
    public final boolean isF3() {
        return f3;
    }
    public final void setF3(final boolean n) {
        f3 = n;
    }
    public final boolean isHF() {
        return hf;
    }
    public final void setHF(final boolean n) {
        hf = n;
    }
    public final boolean isF5() {
        return f5;
    }
    public final void setF5(final boolean n) {
        f5 = n;
    }
    public final boolean isZF() {
        return zf;
    }
    public final void setZF(final boolean n) {
        zf = n;
    }
    public final boolean isSF() {
        return sf;
    }
    public final void setSF(final boolean n) {
        sf = n;
    }
    
    // 16-Bit Main Pseudo Registers
    public final int getAF() {
        return (a<<8) | getF();
    }
    public final void setAF(final int n) {
        a = (n&0xFF00)>>>8; setF(n&0x00FF);
    }
    public final int getBC() {
        return (b<<8) | c;
    }
    public final void setBC(final int n) {
        b = (n&0xFF00)>>>8; c = n&0x00FF;
    }
    public final int getDE() {
        return (d<<8) | e;
    }
    public final void setDE(final int n) {
        d = (n&0xFF00)>>>8; e = n&0x00FF;
    }
    public final int getHL() {
        return (h<<8) | l;
    }
    public final void setHL(final int n) {
        h = (n&0xFF00)>>>8; l = n&0x00FF;
    }
    public final int getWZ() {
        return (w<<8) | z;
    }
    public final void setWZ(final int n) {
        w = (n&0xFF00)>>>8; z = n&0x00FF;
    }
    
    // 16-Bit Alternate Pseudo Registers
    public final int getAF_() {
        return (a_<<8) | f_;
    }
    public final void setAF_(int n) {
        a_ = (n&0xFF00)>>>8; f_ = (n&0x00FF);
    }
    public final int getBC_() {
        return (b_<<8) | c_;
    }
    public final void setBC_(final int n) {
        b_ = (n&0xFF00)>>>8; c_ = n&0x00FF;
    }
    public final int getDE_() {
        return (d_<<8) | e_;
    }
    public final void setDE_(final int n) {
        d_ = (n&0xFF00)>>>8; e_ = n&0x00FF;
    }
    public final int getHL_() {
        return (h_<<8) | l_;
    }
    public final void setHL_(final int n) {
        h_ = (n&0xFF00)>>>8; l_ = n&0x00FF;
    }
    
    // 16-Bit Auxiliary Registers
    public final int getIX() {
        return ix;
    }
    public final void setIX(int n) {
        ix = n&0xFFFF;
    }
    public final int getIY() {
        return iy;
    }
    public final void setIY(int n) {
        iy = n&0xFFFF;
    }
    @Override
    public final int getPC() {
        return pc;
    }
    public final void setPC(final int n) {
        pc = n&0xFFFF;
    }
    public final int getSP() {
        return sp;
    }
    public final void setSP(final int n) {
        sp = n&0xFFFF;
    }

}
