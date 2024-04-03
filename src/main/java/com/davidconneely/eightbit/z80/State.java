package com.davidconneely.eightbit.z80;

public final class State {
    private int a, b, c, d, e, h, l;
    private boolean cf, nf, pf, hf, zf, sf;
    private int ix, iy, pc, sp;
    private int a_, b_, c_, d_, e_, f_, h_, l_;
    private int i, r06, im;
    private boolean r7, iff1, iff2, halted;


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

    // --- Main Flags ---

    int f() {
        return (cf ? 0x01 : 0) | (nf ? 0x02 : 0) | (pf ? 0x04 : 0) |
                (hf ? 0x10 : 0) | (zf ? 0x40 : 0) | (sf ? 0x80 : 0);
    }

    void f(final int n) {
        cf = (n & 0x01) != 0;
        nf = (n & 0x02) != 0;
        pf = (n & 0x04) != 0;
        hf = (n & 0x10) != 0;
        zf = (n & 0x40) != 0;
        sf = (n & 0x80) != 0;
    }

    public boolean cf() {
        return cf;
    }

    void cf(final boolean t) {
        cf = t;
    }

    boolean nf() {
        return nf;
    }

    void nf(final boolean t) {
        nf = t;
    }

    boolean pf() {
        return pf;
    }

    void pf(final boolean t) {
        pf = t;
    }

    boolean hf() {
        return hf;
    }

    void hf(final boolean t) {
        hf = t;
    }

    boolean zf() {
        return zf;
    }

    void zf(final boolean t) {
        zf = t;
    }

    boolean sf() {
        return sf;
    }

    void sf(final boolean t) {
        sf = t;
    }

    // --- 16-Bit Main Pseudo Registers ---

    int af() {
        return (a << 8) | f();
    }

    void af(final int nn) {
        a = (nn & 0xFF00) >>> 8;
        f(nn & 0x00FF);
    }

    int bc() {
        return (b << 8) | c;
    }

    void bc(final int nn) {
        b = (nn & 0xFF00) >>> 8;
        c = nn & 0x00FF;
    }

    public int de() {
        return (d << 8) | e;
    }

    void de(final int nn) {
        d = (nn & 0xFF00) >>> 8;
        e = nn & 0x00FF;
    }

    int hl() {
        return (h << 8) | l;
    }

    void hl(final int nn) {
        h = (nn & 0xFF00) >>> 8;
        l = nn & 0x00FF;
    }

    // --- 16-Bit Auxiliary Registers ---

    int ix() {
        return ix;
    }

    void ix(final int nn) {
        ix = nn & 0xFFFF;
    }

    int iy() {
        return iy;
    }

    void iy(final int nn) {
        iy = nn & 0xFFFF;
    }

    public int pc() {
        return pc;
    }

    public void pc(final int nn) {
        pc = nn & 0xFFFF;
    }

    int sp() {
        return sp;
    }

    void sp(final int nn) {
        sp = nn & 0xFFFF;
    }

    // --- Interrupt Stuff ---

    int i() {
        return i;
    }

    void i(final int n) {
        i = n & 0xFF;
    }

    int r() {
        return r06 | (r7 ? 0x80 : 0x00);
    }

    void r(final int n) {
        r06 = n & 0x7F;
        r7 = (n & 0x80) != 0;
    }

    int im() {
        return im;
    }

    void im(final int n) {
        im = (n & 0x03) % 3;
    }

    boolean iff1() {
        return iff1;
    }

    void iff1(final boolean t) {
        iff1 = t;
    }

    boolean iff2() {
        return iff2;
    }

    void iff2(final boolean t) {
        iff2 = t;
    }

    public boolean halted() {
        return halted;
    }

    void halted(final boolean t) {
        this.halted = true;
    }

    // --- Alternate Registers ---

    void alt_af() {
        int n = a; a = a_; a_ = n;
        n = f(); f(f_); f_ = n;
    }

    void alt_bcdehl() {
        int n = b; b = b_; b_ = n;
        n = c; c = c_; c_ = n;
        n = d; d = d_; d_ = n;
        n = e; e = e_; e_ = n;
        n = h; h = h_; h_ = n;
        n = l; l = l_; l_ = n;
    }

    // --- Utility methods ---

    /**
     * Increment the `R` refresh register (bits 0 to 6 - bit 7 is left alone).
     */
    void rInc() {
        r06 = (r06 + 1) & 0x7F;
    }

    /**
     * Post-increment `PC` by 1 (used for immediate bytes).
     */
    int pcInc1() {
        int nn = pc;
        pc = (pc + 1) & 0xFFFF;
        return nn;
    }

    /**
     * Post-increment `PC` by 2 (used for immediate words).
     */
    int pcInc2() {
        int nn = pc;
        pc = (pc + 2) & 0xFFFF;
        return nn;
    }

    /**
     * Post-increment `SP` by 2 (used for `POP`).
     */
    public int spInc2() {
        int nn = sp;
        sp = (sp + 2) & 0xFFFF;
        return nn;
    }

    /**
     * Pre-decrement `SP` by 2 (used for `PUSH`).
     */
    int spDec2() {
        return (sp = (sp - 2) & 0xFFFF);
    }

    String formatted(final int opCode) {
        return String.format("pc=0x%04x,sp=0x%04x,op=0x%02x,"
                        + "a=0x%02x,b=0x%02x,c=0x%02x,d=0x%02x,e=0x%02x,h=0x%02x,l=0x%02x,"
                        + "a'=0x%02x,b'=0x%02x,c'=0x%02x,d'=0x%02x,e'=0x%02x,h'=0x%02x,l'=0x%02x,"
                        + "ix=0x%04x,iy=0x%04x,i=0x%02x,r=0x%02x,"
                        + "c=%d,po=%d,hc=%d,n=%d,z=%d,s=%d,y=0,x=0",
                pc, sp, opCode, a, b, c, d, e, h, l, a_, b_, c_, d_, e_, h_, l_, ix, iy, 0, 0,
                cf ? 1 : 0, pf ? 1 : 0, hf ? 1 : 0, nf ? 1 : 0, zf ? 1 : 0, sf ? 1 : 0);
    }
}
