package eightbit.z80core;

import eightbit.ICore;

public class Z80Core implements ICore {
    private int cycles;
    private short pc;

    @Override
    public void resetCycleCount() {
        cycles = 0;
    }

    @Override
    public int getCycleCount() {
        return cycles;
    }

    @Override
    public void runOneInstruction() {
        ilog()
    }

    short getPC() {
        return pc;
    }

}
