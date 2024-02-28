package com.davidconneely.eightbit.z80;

public final class State {
    private boolean halted;
    private int a, b, c, d, e, h, l, w, z, a_, b_, c_, d_, e_, f_, h_, l_, ix, iy, pc, sp;
    private boolean cf, nf, pf, yf, hf, xf, zf, sf;

    boolean halted() {
        return halted;
    }

    void halted(boolean halted) {
        this.halted = halted;
    }

    // --- 8-Bit Main Registers ---

    int a() {
        return a;
    }

    void a(final int n) {
        a = n & 0xFF;
    }

    int b() {
        return b;
    }

    void b(final int n) {
        b = n & 0xFF;
    }

    int c() {
        return c;
    }

    void c(final int n) {
        c = n & 0xFF;
    }

    int d() {
        return d;
    }

    void d(final int n) {
        d = n & 0xFF;
    }

    int e() {
        return e;
    }

    void e(final int n) {
        e = n & 0xFF;
    }

    int h() {
        return h;
    }

    void h(final int n) {
        h = n & 0xFF;
    }

    int l() {
        return l;
    }

    void l(final int n) {
        l = n & 0xFF;
    }

    int w() {
        return w;
    }

    void w(final int n) {
        w = n & 0xFF;
    }

    int z() {
        return z;
    }

    void z(final int n) {
        z = n & 0xFF;
    }

    // --- 8-Bit Alternate Registers ---

    int a_() {
        return a_;
    }

    void a_(final int n) {
        a_ = n & 0xFF;
    }

    int b_() {
        return b_;
    }

    void b_(final int n) {
        b_ = n & 0xFF;
    }

    int c_() {
        return c_;
    }

    void c_(final int n) {
        c_ = n & 0xFF;
    }

    int d_() {
        return d_;
    }

    void d_(final int n) {
        d_ = n & 0xFF;
    }

    int e_() {
        return e_;
    }

    void e_(final int n) {
        e_ = n & 0xFF;
    }

    int f_() {
        return f_;
    }

    void f_(final int n) {
        f_ = n & 0xFF;
    }

    int h_() {
        return h_;
    }

    void h_(final int n) {
        h_ = n & 0xFF;
    }

    int l_() {
        return l_;
    }

    void l_(final int n) {
        l_ = n & 0xFF;
    }

    // --- Main Flags ---

    int f() {
        return (cf ? 0x01 : 0) | (nf ? 0x02 : 0) | (pf ? 0x04 : 0) | (yf ? 0x08 : 0) |
                (hf ? 0x10 : 0) | (xf ? 0x20 : 0) | (zf ? 0x40 : 0) | (sf ? 0x80 : 0);
    }

    void f(final int n) {
        cf = (n & 0x01) != 0;
        nf = (n & 0x02) != 0;
        pf = (n & 0x04) != 0;
        yf = (n & 0x08) != 0;
        hf = (n & 0x10) != 0;
        xf = (n & 0x20) != 0;
        zf = (n & 0x40) != 0;
        sf = (n & 0x80) != 0;
    }

    boolean cf() {
        return cf;
    }

    void cf(final boolean n) {
        cf = n;
    }

    boolean nf() {
        return nf;
    }

    void nf(final boolean n) {
        nf = n;
    }

    boolean pf() {
        return pf;
    }

    void pf(final boolean n) {
        pf = n;
    }

    boolean yf() {
        return yf;
    }

    void yf(final boolean n) {
        yf = n;
    }

    boolean hf() {
        return hf;
    }

    void hf(final boolean n) {
        hf = n;
    }

    boolean xf() {
        return xf;
    }

    void xf(final boolean n) {
        xf = n;
    }

    boolean zf() {
        return zf;
    }

    void zf(final boolean n) {
        zf = n;
    }

    boolean sf() {
        return sf;
    }

    void sf(final boolean n) {
        sf = n;
    }

    // --- 16-Bit Main Pseudo Registers ---

    int af() {
        return (a << 8) | f();
    }

    void af(final int n) {
        a = (n & 0xFF00) >>> 8;
        f(n & 0x00FF);
    }

    int bc() {
        return (b << 8) | c;
    }

    void bc(final int n) {
        b = (n & 0xFF00) >>> 8;
        c = n & 0x00FF;
    }

    int de() {
        return (d << 8) | e;
    }

    void de(final int n) {
        d = (n & 0xFF00) >>> 8;
        e = n & 0x00FF;
    }

    int hl() {
        return (h << 8) | l;
    }

    void hl(final int n) {
        h = (n & 0xFF00) >>> 8;
        l = n & 0x00FF;
    }

    int wz() {
        return (w << 8) | z;
    }

    void wz(final int n) {
        w = (n & 0xFF00) >>> 8;
        z = n & 0x00FF;
    }

    // --- 16-Bit Alternate Pseudo Registers ---

    int af_() {
        return (a_ << 8) | f_;
    }

    void af_(final int n) {
        a_ = (n & 0xFF00) >>> 8;
        f_ = (n & 0x00FF);
    }

    int bc_() {
        return (b_ << 8) | c_;
    }

    void bc_(final int n) {
        b_ = (n & 0xFF00) >>> 8;
        c_ = n & 0x00FF;
    }

    int de_() {
        return (d_ << 8) | e_;
    }

    void de_(final int n) {
        d_ = (n & 0xFF00) >>> 8;
        e_ = n & 0x00FF;
    }

    int hl_() {
        return (h_ << 8) | l_;
    }

    void hl_(final int n) {
        h_ = (n & 0xFF00) >>> 8;
        l_ = n & 0x00FF;
    }

    // --- 16-Bit Auxiliary Registers ---

    int ix() {
        return ix;
    }

    void ix(final int n) {
        ix = n & 0xFFFF;
    }

    int iy() {
        return iy;
    }

    void iy(final int n) {
        iy = n & 0xFFFF;
    }

    int pc() {
        return pc;
    }

    void pc(final int n) {
        pc = n & 0xFFFF;
    }

    int sp() {
        return sp;
    }

    void sp(final int n) {
        sp = n & 0xFFFF;
    }

    // --- Utility methods ---

    /**
     * Post-increment PC by 1 (used for immediate bytes).
     */
    int pcInc1() {
        int pc_ = pc;
        pc = (pc + 1) & 0xFFFF;
        return pc_;
    }

    /**
     * Post-increment PC by 2 (used for immediate words).
     */
    int pcInc2() {
        int pc_ = pc;
        pc = (pc + 2) & 0xFFFF;
        return pc_;
    }

    /**
     * Post-increment SP by 2 (used for POP).
     */
    int spInc2() {
        int sp_ = sp;
        sp = (sp + 2) & 0xFFFF;
        return sp_;
    }

    /**
     * Pre-decrement SP by 2 (used for PUSH).
     */
    int spDec2() {
        sp = (sp - 2) & 0xFFFF;
        return sp;
    }

    String formatted(final int op) {
        return String.format("pc=0x%04x,sp=0x%04x,op=0x%02x,"
                        + "a=0x%02x,b=0x%02x,c=0x%02x,d=0x%02x,e=0x%02x,h=0x%02x,l=0x%02x,"
                        + "a'=0x%02x,b'=0x%02x,c'=0x%02x,d'=0x%02x,e'=0x%02x,h'=0x%02x,l'=0x%02x,"
                        + "ix=0x%04x,iy=0x%04x,i=0x%02x,r=0x%02x,"
                        + "c=%d,po=%d,hc=%d,n=%d,z=%d,s=%d,y=%d,x=%d",
                pc, sp, op, a, b, c, d, e, h, l, a_, b_, c_, d_, e_, h_, l_, ix, iy, 0, 0,
                cf ? 1 : 0, pf ? 1 : 0, hf ? 1 : 0, nf ? 1 : 0, zf ? 1 : 0, sf ? 1 : 0, yf ? 1 : 0, xf ? 1 : 0);
    }
}
