package com.davidconneely.eightbit.zx81;

import org.fusesource.jansi.internal.JansiLoader;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.fusesource.jansi.internal.Kernel32.GetConsoleMode;
import static org.fusesource.jansi.internal.Kernel32.GetConsoleOutputCP;
import static org.fusesource.jansi.internal.Kernel32.GetNumberOfConsoleInputEvents;
import static org.fusesource.jansi.internal.Kernel32.GetStdHandle;
import static org.fusesource.jansi.internal.Kernel32.INPUT_RECORD;
import static org.fusesource.jansi.internal.Kernel32.INPUT_RECORD.KEY_EVENT;
import static org.fusesource.jansi.internal.Kernel32.KEY_EVENT_RECORD;
import static org.fusesource.jansi.internal.Kernel32.STD_INPUT_HANDLE;
import static org.fusesource.jansi.internal.Kernel32.STD_OUTPUT_HANDLE;
import static org.fusesource.jansi.internal.Kernel32.SetConsoleMode;
import static org.fusesource.jansi.internal.Kernel32.SetConsoleOutputCP;
import static org.fusesource.jansi.internal.Kernel32.readConsoleInputHelper;

class WindowsTerminalSupport extends TerminalSupport {
    private final long handleConsoleInput;
    private final long handleConsoleOutput;
    private final int originalConsoleModeInput;
    private final int originalConsoleModeOutput;
    private final int originalConsoleOutputCP;
    private final PrintStream originalSystemOut;

    static {
        JansiLoader.initialize();
    }

    static WindowsTerminalSupport newInstance() {
        return new WindowsTerminalSupport();
    }

    // private to prevent other classes instantiating this.
    private WindowsTerminalSupport() {
        this.handleConsoleInput = GetStdHandle(STD_INPUT_HANDLE);
        this.handleConsoleOutput = GetStdHandle(STD_OUTPUT_HANDLE);
        int[] mode = new int[1];
        GetConsoleMode(this.handleConsoleInput, mode);
        this.originalConsoleModeInput = mode[0];
        GetConsoleMode(this.handleConsoleOutput, mode);
        this.originalConsoleModeOutput = mode[0];
        this.originalConsoleOutputCP = GetConsoleOutputCP();
        this.originalSystemOut = System.out;
    }

    /* hConsole input flags */
    private static final int ENABLE_PROCESSED_INPUT             = 0x0001; /* Ctrl+C processed by system; also BS, CR, LF in line input mode */
    private static final int ENABLE_LINE_INPUT                  = 0x0002; /* Return only when CR is read */
    private static final int ENABLE_ECHO_INPUT                  = 0x0004; /* Chars written as they are read */
    private static final int ENABLE_WINDOW_INPUT                = 0x0008; /* Window size changes in input buffer */
    private static final int ENABLE_MOUSE_INPUT                 = 0x0010; /* Mouse events in input buffer */
    private static final int ENABLE_INSERT_MODE                 = 0x0020; /* Text inserts not overwrites */
    private static final int ENABLE_QUICK_EDIT_MODE             = 0x0040; /* Mouse to select and edit text */
    private static final int ENABLE_VIRTUAL_TERMINAL_INPUT      = 0x0200; /* Virtual terminal processing on user input */

    /* hConsole output flags */
    private static final int ENABLE_PROCESSED_OUTPUT            = 0x0001; /* Process BS, TAB, BEL, CR, etc. */
    private static final int ENABLE_WRAP_AT_EOL_OUTPUT          = 0x0002;
    private static final int ENABLE_VIRTUAL_TERMINAL_PROCESSING = 0x0004; /* VT100 escape sequences */
    private static final int DISABLE_NEWLINE_AUTO_RETURN        = 0x0008;
    private static final int ENABLE_LVB_GRID_WORLDWIDE          = 0x0010;

    private static final int CODEPAGE_UTF_8                     = 65001;
    /**
     * It's likely this will need a recent Windows 10 or 11 build. I'm not aiming to support every old Windows OS here.
     */
    @Override
    void enableRawMode() {
        final int[] mode = new int[1];
        final boolean isConsoleIn = GetConsoleMode(handleConsoleInput, mode) != 0;
        if (isConsoleIn) {
            SetConsoleMode(handleConsoleInput,
                    (mode[0] | ENABLE_VIRTUAL_TERMINAL_INPUT) &
                            ~(ENABLE_PROCESSED_INPUT|ENABLE_LINE_INPUT|ENABLE_ECHO_INPUT|ENABLE_WINDOW_INPUT|ENABLE_MOUSE_INPUT));
        }
        final boolean isConsoleOut = GetConsoleMode(handleConsoleOutput, mode) != 0;
        if (isConsoleOut) {
            SetConsoleMode(handleConsoleOutput,
                    (mode[0] | ENABLE_PROCESSED_OUTPUT|ENABLE_VIRTUAL_TERMINAL_PROCESSING) &
                            ~(ENABLE_WRAP_AT_EOL_OUTPUT));
        }
        SetConsoleOutputCP(CODEPAGE_UTF_8); // UTF-8
        if (!StandardCharsets.UTF_8.equals(System.out.charset())) {
            final var utf8 = new PrintStream(System.out, true, StandardCharsets.UTF_8);
            System.setOut(utf8);
        }
    }

    @Override
    void reset() {
        SetConsoleMode(handleConsoleInput, originalConsoleModeInput);
        SetConsoleMode(handleConsoleOutput, originalConsoleModeOutput);
        SetConsoleOutputCP(originalConsoleOutputCP);
        System.setOut(originalSystemOut);
    }


    @Override
    int read() {
        final int[] count = new int[1];
        while (true) {
            GetNumberOfConsoleInputEvents(handleConsoleInput, count);
            if (count[0] < 1) {
                return -1;
            }
            final INPUT_RECORD[] inputRecords;
            try {
                inputRecords = readConsoleInputHelper(handleConsoleInput, 1, false);
            } catch (final IOException e) {
                return -1;
            }
            if (inputRecords.length < 1) {
                continue;
            }
            final INPUT_RECORD inputRecord = inputRecords[0];
            if (inputRecord.eventType != KEY_EVENT) {
                continue;
            }
            final KEY_EVENT_RECORD keyEventRecord = inputRecord.keyEvent;
            if (!keyEventRecord.keyDown) {
                continue;
            }
            return keyEventRecord.uchar;
        }
    }

    @Override
    void write(final int data) {
        System.out.write(data);
    }

    @Override
    void print(final String text) {
        System.out.print(text);
    }
}
