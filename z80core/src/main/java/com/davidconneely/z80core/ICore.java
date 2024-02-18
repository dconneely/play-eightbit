package com.davidconneely.z80core;

public interface ICore {
    void resetCycleCount();
    int getCycleCount();
    void runOneInstruction();
    int getPC();
}
