package com.davidconneely.eightbit.zx81;

import com.davidconneely.eightbit.IBus;

class TerminalDisplay {
    private static final int LEN_STATE = 68;
    private static final int[] codepoints = {
            ' ', '▘', '▝', '▀', '▖', '▌', '▞', '▛',
            '▒', 0x1FB8F, 0x1FB8E, '"', '£', '$', ':', '?',
            '(', ')', '>', '<', '=', '+', '-', '*',
            '/', ';', ',', '.', '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', 'A', 'B',
            'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
            'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };
    private static final int[] codepoints2 = {
            '█', '▟', '▙', '▄', '▜', '▐', '▚', '▗',
            0x1FB90, 0x1FB91, 0x1FB92
    };

    private final TerminalSupport terminal;
    private String state1 = sanitizeState(null), state2 = sanitizeState(null);

    TerminalDisplay(final TerminalSupport terminal) {
        this.terminal = terminal;
    }

    /**
     * Call this before starting the display to prepare it for use.
     */
    void init() {
        terminal.print("\u001B[?25l\u001B[?1049h"); // hide the cursor, use the alternate buffer.
    }

    /**
     * Call this after using the display to release and reset it.
     */
    void reset() {
        terminal.print("\u001B[?1049l\u001B[?25h\u001B[0m"); // switch back to the normal buffer, show the cursor again, reset display attributes.
    }

    void state1(final String state) {
        this.state1 = sanitizeState(state);
    }

    void state2(final String state) {
        this.state2 = sanitizeState(state);
    }

    private static String sanitizeState(final String state) {
        StringBuilder sb = new StringBuilder(LEN_STATE);
        if (state != null) {
            for (int i = 0; i < state.length() && i < LEN_STATE; ++i) {
                int ch = state.charAt(i);
                if (ch >= 0x20 && ch <= 0x7E) {
                    sb.append((char) ch);
                }
            }
        }
        while (sb.length() < LEN_STATE) {
            sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * Convert a screen from the display file into a UTF-8 and ANSI-escapes stream that can be displayed in a terminal.
     * The stream starts with an ANSI-escape to move to the screen home. Always tries to show 24 lines.
     *
     * @param bus interface to read bytes from the display file.
     */
    void renderDFile(final IBus bus) {
        int address = bus.cpuReadMemWord(0x400C); // value of D_FILE
        int ch = bus.cpuReadMemByte(address);
        if ((ch & 0x40) != 0) { // skip any initial byte with bit6 set (usually 0x76 = `HALT`)
            ++address;
        }
        terminal.print("\u001B[H\u001B#6 \u001b[38;5;242msinclair \u001b[38;5;202mZX81\u001B[0m\u001B[K\r\n");
        terminal.print("\u001B#6\u001b[38;5;231m╭──────────────────────────────────╮\u001B[0m\u001B[K\r\n");
        terminal.print("\u001B#6\u001b[38;5;231m│\u001B[38;5;195m\u001B[7m                                  \u001B[27m\u001B[38;5;231m│\u001B[0m\u001B[K\n");
        for (int i = 0; i < 24; ++i) {
            terminal.print("\u001B#6\u001b[38;5;231m│\u001B[38;5;195m\u001B[7m ");
            address = renderLine(bus, address);
            terminal.print(" \u001B[27m\u001b[38;5;231m│\u001B[0m\u001B[K\r\n");
        }
        terminal.print("\u001B#6\u001b[38;5;231m│\u001B[38;5;195m\u001B[7m                                  \u001B[27m\u001B[38;5;231m│\u001B[0m\u001B[K\n");
        terminal.print("\u001B#6\u001b[38;5;231m╰──────────────────────────────────╯\u001B[0m\u001B[K\r\n");
        // 68 x 2 character status area under the main screen area.
        terminal.print("  " + state1 + "  \u001B[0m\u001B[K\r\n");
        terminal.print("  " + state2 + "  \u001B[0m\u001B[K");
    }

    /**
     * Convert a line from the display file into a UTF-8 and ANSI-escapes stream that can be displayed in a terminal.
     * Inverse video mode is always reset at the end of the line. Any bytes with bit 6 set (usually 0x76) end the line.
     *
     * @param bus     interface to read bytes from the display file.
     * @param address address of start of line.
     * @return int address to continue next line from.
     */
    private int renderLine(final IBus bus, int address) {
        boolean inverted = true;
        int column = 0;
        while (column < 33) {
            int ch = bus.cpuReadMemByte(address++);
            if ((ch & 0x40) != 0 || column == 32) break; // end translation if bit6 set (usually 0x76 = `HALT`), or line is too long.
            int index = ch & 0x3F;
            boolean inverse = (ch & 0x80) == 0 || index <= 10; // inverse video unless bit7 set (except for pre-inverted block graphics).
            if (inverted != inverse) {
                terminal.print(inverse ? "\u001B[7m" : "\u001B[27m");
                inverted = inverse;
            }
            writeCodepoint(((ch & 0x80) != 0 && index <= 10) ? codepoints2[index] : codepoints[index]);
            ++column;
        }
        if (!inverted) {
            terminal.print("\u001B[7m");
        }
        while (column < 32) { // only used with collapsed display file.
            terminal.write(' ');
            ++column;
        }
        return address;
    }

    /**
     * Writes to the `stream` in UTF-8 (note that `enableRawMode` should also set the terminal to UTF-8 encoding).
     *
     * @param codepoint the Unicode codepoint to write as a set of UTF-8 bytes to the stream.
     */
    private void writeCodepoint(final int codepoint) {
        if (codepoint <= 0x7F) /* 7-bits */ {
            terminal.write(codepoint);
        } else if (codepoint <= 0x7FF) /* 5-bits,6-bits */ {
            terminal.write(0xC0 | ((codepoint & 0x7C0) >>> 6));
            terminal.write(0x80 | (codepoint & 0x03F));
        } else if (codepoint <= 0xFFFF) /* 4-bits,6-bits,6-bits */ {
            terminal.write(0xE0 | ((codepoint & 0xF000) >>> 12));
            terminal.write(0x80 | ((codepoint & 0x0FC0) >>> 6));
            terminal.write(0x80 | (codepoint & 0x3F));
        } else if (codepoint <= 0x1FFFFF) /* 3-bits,6-bits,6-bits,6-bits */ {
            terminal.write(0xF0 | ((codepoint & 0x1C0000) >>> 18));
            terminal.write(0x80 | ((codepoint & 0x03F000) >>> 12));
            terminal.write(0x80 | ((codepoint & 0x000FC0) >>> 6));
            terminal.write(0x80 | (codepoint & 0x3F));
        }
    }
}
