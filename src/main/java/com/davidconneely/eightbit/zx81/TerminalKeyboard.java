package com.davidconneely.eightbit.zx81;

import java.io.EOFException;
import java.io.IOException;
import java.util.Map;

/**
 * To be able to emulate the keyboard in a terminal, I think I need to be able to enable terminal input raw mode, which
 * can't be done from Java without the use of native code or executing an external process.
 */
public class TerminalKeyboard {
    record Key(int highPort, int data, boolean isShifted) {
    }

    private static final Map<Character, Key> KEY_MAP = Map.<Character, Key>ofEntries(
            Map.entry('1', new Key(0xF7, 0xFE, false)),
            Map.entry('!', new Key(0xF7, 0xFE, true)),
            Map.entry('2', new Key(0xF7, 0xFD, false)),
            Map.entry('@', new Key(0xF7, 0xFD, true)),
            Map.entry('3', new Key(0xF7, 0xFB, false)),
            Map.entry('#', new Key(0xF7, 0xFB, true)),
            Map.entry('4', new Key(0xF7, 0xF7, false)),
            Map.entry('$', new Key(0xF7, 0xF7, true)),
            Map.entry('5', new Key(0xF7, 0xEF, false)),
            Map.entry('%', new Key(0xF7, 0xEF, true)),
            Map.entry('←', new Key(0xF7, 0xEF, true)), // additional, left arrow
            Map.entry('6', new Key(0xEF, 0xEF, false)),
            Map.entry('^', new Key(0xEF, 0xEF, true)),
            Map.entry('↓', new Key(0xEF, 0xEF, true)), // additional, down arrow
            Map.entry('7', new Key(0xEF, 0xF7, false)),
            Map.entry('&', new Key(0xEF, 0xF7, true)),
            Map.entry('↑', new Key(0xEF, 0xF7, true)), // additional, up arrow
            Map.entry('8', new Key(0xEF, 0xFB, false)),
            Map.entry('*', new Key(0xEF, 0xFB, true)),
            Map.entry('→', new Key(0xEF, 0xFB, true)), // additional, right arrow
            Map.entry('9', new Key(0xEF, 0xFD, false)),
            Map.entry('(', new Key(0xEF, 0xFD, true)),
            Map.entry('\u0007', new Key(0xEF, 0xFD, true)), // additional, Ctrl+G for GRAPHICS mode
            Map.entry('0', new Key(0xEF, 0xFE, false)),
            Map.entry(')', new Key(0xEF, 0xFE, true)),
            Map.entry('\u007F', new Key(0xEF, 0xFE, true)), // additional, Backspace for RUBOUT
            Map.entry('q', new Key(0xFB, 0xFE, false)),
            Map.entry('Q', new Key(0xFB, 0xFE, true)),
            Map.entry('w', new Key(0xFB, 0xFD, false)),
            Map.entry('W', new Key(0xFB, 0xFD, true)),
            Map.entry('e', new Key(0xFB, 0xFB, false)),
            Map.entry('E', new Key(0xFB, 0xFB, true)),
            Map.entry('r', new Key(0xFB, 0xF7, false)),
            Map.entry('R', new Key(0xFB, 0xF7, true)),
            Map.entry('t', new Key(0xFB, 0xEF, false)),
            Map.entry('T', new Key(0xFB, 0xEF, true)),
            Map.entry('y', new Key(0xDF, 0xEF, false)),
            Map.entry('Y', new Key(0xDF, 0xEF, true)),
            Map.entry('u', new Key(0xDF, 0xF7, false)),
            Map.entry('U', new Key(0xDF, 0xF7, true)),
            Map.entry('i', new Key(0xDF, 0xFB, false)),
            Map.entry('I', new Key(0xDF, 0xFB, true)),
            Map.entry('[', new Key(0xDF, 0xFB, true)), // additional, '[' for '('
            Map.entry('{', new Key(0xDF, 0xFB, true)), // additional, '{' for '('
            Map.entry('o', new Key(0xDF, 0xFD, false)),
            Map.entry('O', new Key(0xDF, 0xFD, true)),
            Map.entry(']', new Key(0xDF, 0xFD, true)), // additional, ']' for ')'
            Map.entry('}', new Key(0xDF, 0xFD, true)), // additional, '}' for ')'
            Map.entry('p', new Key(0xDF, 0xFE, false)),
            Map.entry('P', new Key(0xDF, 0xFE, true)),
            Map.entry('"', new Key(0xDF, 0xFE, true)), // additional, '"'
            Map.entry('a', new Key(0xFD, 0xFE, false)),
            Map.entry('A', new Key(0xFD, 0xFE, true)),
            Map.entry('s', new Key(0xFD, 0xFD, false)),
            Map.entry('S', new Key(0xFD, 0xFD, true)),
            Map.entry('d', new Key(0xFD, 0xFB, false)),
            Map.entry('D', new Key(0xFD, 0xFB, true)),
            Map.entry('f', new Key(0xFD, 0xF7, false)),
            Map.entry('F', new Key(0xFD, 0xF7, true)),
            Map.entry('g', new Key(0xFD, 0xEF, false)),
            Map.entry('G', new Key(0xFD, 0xEF, true)),
            Map.entry('h', new Key(0xBF, 0xEF, false)),
            Map.entry('H', new Key(0xBF, 0xEF, true)),
            Map.entry('j', new Key(0xBF, 0xF7, false)),
            Map.entry('J', new Key(0xBF, 0xF7, true)),
            Map.entry('-', new Key(0xBF, 0xF7, true)), // additional, '-'
            Map.entry('k', new Key(0xBF, 0xFB, false)),
            Map.entry('K', new Key(0xBF, 0xFB, true)),
            Map.entry('+', new Key(0xBF, 0xFB, true)), // additional, '+'
            Map.entry('l', new Key(0xBF, 0xFD, false)),
            Map.entry('L', new Key(0xBF, 0xFD, true)),
            Map.entry('=', new Key(0xBF, 0xFD, true)), // additional, '='
            Map.entry('\r', new Key(0xBF, 0xFE, false)), // Enter key for NEWLINE
            Map.entry('\u0006', new Key(0xBF, 0xFE, true)), // Ctrl+F for FUNCTION mode
            Map.entry('\t', new Key(0xFE, 0xFE, false)), // Tab key for SHIFT (by itself)
            Map.entry('z', new Key(0xFE, 0xFD, false)),
            Map.entry('Z', new Key(0xFE, 0xFD, true)),
            Map.entry(':', new Key(0xFE, 0xFD, true)), // additional, ':'
            Map.entry('x', new Key(0xFE, 0xFB, false)),
            Map.entry('X', new Key(0xFE, 0xFB, true)),
            Map.entry(';', new Key(0xFE, 0xFB, true)), // additional, ';'
            Map.entry('c', new Key(0xFE, 0xF7, false)),
            Map.entry('C', new Key(0xFE, 0xF7, true)),
            Map.entry('?', new Key(0xFE, 0xF7, true)), // additional, '?'
            Map.entry('v', new Key(0xFE, 0xEF, false)),
            Map.entry('V', new Key(0xFE, 0xEF, true)),
            Map.entry('/', new Key(0xFE, 0xEF, true)), // additional, '/'
            Map.entry('b', new Key(0x7F, 0xEF, false)),
            Map.entry('B', new Key(0x7F, 0xEF, true)),
            Map.entry('n', new Key(0x7F, 0xF7, false)),
            Map.entry('N', new Key(0x7F, 0xF7, true)),
            Map.entry('<', new Key(0x7F, 0xF7, true)), // additional, '<'
            Map.entry('m', new Key(0x7F, 0xFB, false)),
            Map.entry('M', new Key(0x7F, 0xFB, true)),
            Map.entry('>', new Key(0x7F, 0xFB, true)), // additional, '>'
            Map.entry('.', new Key(0x7F, 0xFD, false)),
            Map.entry(',', new Key(0x7F, 0xFD, true)), // note unshifted
            Map.entry(' ', new Key(0x7F, 0xFE, false)),
            Map.entry('\\', new Key(0x7F, 0xFE, true)) // use '\\' (backslash) for '£' key
    );

    private final TerminalSupport terminal;

    private long lastKeyTime;
    private Key lastKey;

    public TerminalKeyboard(TerminalSupport terminal) {
        this.terminal = terminal;
    }

    private void consumeVTInput() {
        // the only Virtual Terminal Control Sequences we really care about are the arrow keys:
        int ch = terminal.read();
        if (ch == 27) {
            ch = terminal.read();
            if (ch == '[') {
                ch = terminal.read();
                switch (ch) {
                    case 'A' -> ch = '↑'; // up arrow
                    case 'B' -> ch = '↓'; // down arrow
                    case 'C' -> ch = '→'; // right arrow
                    case 'D' -> ch = '←'; // left arrow
                }
            }
        }
        long now = System.currentTimeMillis();

        if (ch == -1) {
            if (now - lastKeyTime >= 20L) {
                lastKeyTime = now;
                lastKey = null;
            }
            return;
        }
        if (ch == 3) { // exit on Ctrl+C
            throw new ShutdownException("Closing emulator because Ctrl+C pressed");
        }
        Key tmp = KEY_MAP.get((char) ch);
        if (tmp != null) {
            lastKeyTime = now;
            lastKey = tmp;
        } else {
            lastKeyTime = now;
            lastKey = null;
        }
    }

    public int readKeyPortByte(int portNum) {
        consumeVTInput();
        if (lastKey == null) {
            return 0xFF;
        }
        int readFrom = portNum >>> 8;
        if (lastKey.isShifted() && readFrom == 0xFE) { // handle shift key
            if (lastKey.highPort == 0xFE) {
                return lastKey.data & 0xFE;
            } else {
                return 0xFE;
            }
        } else if (lastKey.highPort == readFrom) { // handle unshifted keys or keys on other rows
            return lastKey.data;
        } else {
            return 0xFF;
        }
    }
}
