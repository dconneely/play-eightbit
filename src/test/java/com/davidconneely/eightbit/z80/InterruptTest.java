package com.davidconneely.eightbit.z80;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.davidconneely.eightbit.IBus;
import org.junit.jupiter.api.Test;

final class InterruptTest {
  private static final int START = 0x0100;
  private static final int STACK = 0x8000;

  private final int[] memory = new int[0x10000];
  private final IBus bus =
      new IBus() {
        @Override
        public int rawReadMemByte(final int address) {
          return memory[address & 0xFFFF];
        }

        @Override
        public void rawWriteMemByte(final int address, final int data) {
          memory[address & 0xFFFF] = data & 0xFF;
        }
      };
  private final Core core = new Core(bus);

  private void run(final int... program) {
    for (int i = 0; i < program.length; ++i) {
      memory[START + i] = program[i];
    }
    core.state().pc(START);
    core.state().sp(STACK);
  }

  private int pushedAddress() {
    return bus.cpuReadMemWord(core.state().sp());
  }

  @Test
  void nmiPushesTheNextInstructionAndJumpsTo0066() {
    run(0x00, 0x00); // NOP, NOP
    core.step();
    core.nmi();
    assertEquals(0x0066, core.state().pc());
    assertEquals(START + 1, pushedAddress());
  }

  @Test
  void interruptContinuesAfterHalt() {
    run(0x76, 0x00); // HALT, NOP
    core.step();
    core.step(); // still halted
    assertTrue(core.state().halted());
    core.nmi();
    assertFalse(core.state().halted());
    assertEquals(START + 1, pushedAddress());
  }

  @Test
  void irqIsAcceptedOnlyWhenEnabled() {
    run(0xFB, 0x00); // EI, NOP
    assertFalse(core.irq(), "disabled");
    core.step();
    assertTrue(core.irq(), "enabled by EI");
    assertEquals(0x0038, core.state().pc());
    assertEquals(START + 1, pushedAddress());
    assertFalse(core.state().iff1(), "disabled again");
  }

  @Test
  void prefixedInstructionIncrementsRTwice() {
    run(0x00, 0xCB, 0x07); // NOP, RLC A
    core.step();
    final int r = core.state().r();
    core.step();
    assertEquals((r + 2) & 0x7F, core.state().r() & 0x7F);
  }
}
