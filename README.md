# `play-eightbit`

An emulator for the Zilog Z80 8-bit CPU, to allow machine emulation of the Sinclair ZX81.

### Limitations

- Interrupt-related instructions (`DI`, `EI`, `IM` and the `IFF1`, `IFF2` flags) and support for interrupts
  are not currently implemented.
- I/O instructions (`IN`, `IND`, `INDR`, `INI`, `INIR` and `OUT`, `OUTD`, `OTDR`, `OUTI`, `OTIR`) are not correctly
  implemented yet (particularly how they affect the condition bits / flags).
- Instruction timing, T-states, machine-cycles are not currently implemented. Each instruction runs as fast as the JVM
  can execute it, which is completely unrelated to the timing on a real Z80.
- Although, ironically, most undocumented instructions had to be implemented to get the `zexdoc` tests to pass,
  the condition bits 3 and 5 (also know as flags X and Y) are not currently implemented.
- The `zexdoc` tests don't test interrupt or I/O instructions, so there could be unexpected issues in these areas.
