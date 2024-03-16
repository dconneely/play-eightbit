# `play-eightbit`

An emulator for the Zilog Z80 8-bit CPU, to allow machine emulation of the Sinclair ZX81.

### Limitations

- Currently no interrupt support. Most interrupt-related instructions (including `LD A,R`, which is sometimes used for
  pseudo-random number generation in games) are not currently implemented.
- No tracking of instruction timing, T-states, machine-cycles, etc. Instructions run as fast as the JVM can execute
  them, and this is completely unrelated to how long a real Z80 would take (with no way to regulate it).
- The `zexdoc` tests don't test interrupt or I/O functionality, so it's likely the `IN` and `OUT` (and also
  `IND`, `INDR`, `INI`, `INIR`, `OTDR`, `OTIR`, `OUTD`, `OUTI`) instructions are implemented incorrectly. Getting the
  keyboard, etc. to work in an emulated machine should sort out a lot of this.
- Although, ironically, most undocumented op-codes had to be implemented to get the `zexdoc` tests to pass,
  the condition bits 3 and 5 (also know as flags X and Y) are only tested by the `zexall` tests. Only the
  documented condition bits / flags are implemented (most operations will leave X and Y unchanged).
