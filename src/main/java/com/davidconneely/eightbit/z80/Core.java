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
        if (state.halted()) {
            // execute an effective NOP
            state.cyclesInc(4);
            return;
        }
        int pc = state.pc();
        int opCode = bus.readInstruction(pc);
        state.pc((pc + 1) & 0xFFFF);
        int x = (opCode & 0xC0); // values 0x00,0x40,0x80 or 0x80
        int y = (opCode & 0x38); // not shifting here to avoid unnecessary work
        int z = (opCode & 0x07);
        
        switch (opCode) {
            case 0:
                // NOP
                state.cyclesInc(4);
                break;
            case 118:
                // HALT
                state.cyclesInc(4);
                state.halted(true);
                break;
        }
    }
}
