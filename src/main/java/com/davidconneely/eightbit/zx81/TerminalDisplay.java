package com.davidconneely.eightbit.zx81;

import com.davidconneely.eightbit.IBus;

import java.io.PrintStream;

public class TerminalDisplay implements IDisplay {
    private final int[] codepoints = {
            ' ', '▘', '▝', '▀', '▖', '▌', '▞', '▛',
            '▒', 0x1FB8F, 0x1FB8E, '"', '£', '$', ':', '?',
            '(', ')', '>', '<', '=', '+', '-', '*',
            '/', ';', ',', '.', '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', 'A', 'B',
            'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
            'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };

    private final PrintStream stream;
    private final boolean useAltBuffer;
    private final boolean hideCursor;
    private final boolean doubleWidth;
    private final boolean clearLines;

    TerminalDisplay(PrintStream stream, boolean useAltBuffer, boolean hideCursor, boolean doubleWidth, boolean clearLines) {
        this.stream = stream;
        this.useAltBuffer = useAltBuffer;
        this.hideCursor = hideCursor;
        this.doubleWidth = doubleWidth;
        this.clearLines = clearLines;
    }

    /**
     * Call this before starting the display to prepare it for use.
     */
    @Override
    public void init() {
        if (hideCursor) {
            stream.print("\u001b[?25l"); // hide the cursor.
        }
        if (useAltBuffer) {
            stream.print("\u001b[?1049h"); // use the alternate buffer.
        }
    }

    /**
     * Call this after using the display to release and reset it.
     */
    @Override
    public void close() {
        if (useAltBuffer) {
            stream.print("\u001b[?1049l"); // switch back to the normal buffer.
        }
        if (hideCursor) {
            stream.print("\u001b[?25h"); // show the cursor again.
        }
        stream.print("\u001b[0m"); // reset display attributes.
    }

    /**
     * Convert a screen from the display file into a UTF-8 and ANSI-escapes stream that can be displayed in a terminal.
     * The stream starts with an ANSI-escape to move to the screen home. Always tries to show 24 lines.
     *
     * @param bus interface to read bytes from the display file.
     * @param address address of start of screen.
     */
    @Override
    public void renderDFile(final IBus bus, final int address) {
        int read = address;
        if (bus.readMemory(read) == 0x76) {
            ++read; // skip the initial `HALT`.
        }
        stream.print("\u001b[H"); // move to home.
        for (int i = 0; i < 24; ++i) {
            read = renderLine(bus, read);
            stream.write('\r');
            stream.write('\n');
        }
    }

    /**
     * Convert a line from the display file into a UTF-8 and ANSI-escapes stream that can be displayed in a terminal.
     * Inverse video mode is always reset at the end of the line. Any bytes with bit 6 set (usually 0x76) end the line.
     *
     * @param bus interface to read bytes from the display file.
     * @param address address of start of line.
     * @return int address to continue next line from.
     */
    private int renderLine(final IBus bus, final int address) {
        if (doubleWidth) {
            stream.print("\u001b#6");
        }
        int read = address;
        boolean inverted = false;
        int column = 0;
        while (column < 33) {
            int ch = bus.readMemory(read++);
            if ((ch & 0x40) != 0 || column == 32) break; // end translation if bit6 set, or line is too long.
            boolean inverse = (ch & 0x80) != 0; // inverse video if bit7 set.
            if (inverted != inverse) {
                stream.print(inverse ? "\u001b[7m" : "\u001b[27m");
                inverted = inverse;
            }
            writeCodepoint(codepoints[ch & 0x3F]);
            ++column;
        }
        if (inverted) { // reset inverse video
            stream.print("\u001b[27m");
        }
        if (clearLines) {
            stream.print("\u001b[K"); // erase to end of terminal line
        } else {
            while (column < 32) { // blank the line, up to column 32
                stream.write(' ');
                ++column;
            }
        }
        return read;
    }

    /**
     * Writes to the `stream` in UTF-8 (actually regardless of the underlying charset of `stream`).
     *
     * @param codepoint the Unicode codepoint to write as a set of UTF-8 bytes to the stream.
     */
    private void writeCodepoint(int codepoint) {
        if (codepoint <= 0x7F) /* 7-bits */ {
            stream.write(codepoint);
        } else if (codepoint <= 0x7FF) /* 5-bits,6-bits */ {
            stream.write(0xC0 | ((codepoint & 0x7C0) >>> 6));
            stream.write(0x80 | (codepoint & 0x03F));
        } else if (codepoint <= 0xFFFF) /* 4-bits,6-bits,6-bits */ {
            stream.write(0xE0 | ((codepoint & 0xF000) >>> 12));
            stream.write(0x80 | ((codepoint & 0x0FC0) >>> 6));
            stream.write(0x80 | (codepoint & 0x3F));
        } else if (codepoint <= 0x1FFFFF) /* 3-bits,6-bits,6-bits,6-bits */ {
            stream.write(0xF0 | ((codepoint & 0x1C0000) >>> 18));
            stream.write(0x80 | ((codepoint & 0x03F000) >>> 12));
            stream.write(0x80 | ((codepoint & 0x000FC0) >>> 6));
            stream.write(0x80 | (codepoint & 0x3F));
        }
    }
}
