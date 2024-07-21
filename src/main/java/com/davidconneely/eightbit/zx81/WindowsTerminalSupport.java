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

class WindowsTerminalSupport implements TerminalSupport {
    static final WindowsTerminalSupport INSTANCE;
    private final long handleConsoleInput;
    private final long handleConsoleOutput;
    private final int originalConsoleModeInput;
    private final int originalConsoleModeOutput;
    private final int originalConsoleOutputCP;
    private final PrintStream originalSystemOut;

    static {
        JansiLoader.initialize();
        INSTANCE = new WindowsTerminalSupport();
    }

    // private to prevent other classes instantiating this.
    private WindowsTerminalSupport() {
        handleConsoleInput = GetStdHandle(STD_INPUT_HANDLE);
        handleConsoleOutput = GetStdHandle(STD_OUTPUT_HANDLE);
        int[] mode = new int[1];
        GetConsoleMode(handleConsoleInput, mode);
        originalConsoleModeInput = mode[0];
        GetConsoleMode(handleConsoleOutput, mode);
        originalConsoleModeOutput = mode[0];
        originalConsoleOutputCP = GetConsoleOutputCP();
        originalSystemOut = System.out;
    }

    /**
     * It's likely this will need a recent Windows 10 or 11 build. I'm not aiming to support every old Windows OS here.
     */
    @Override
    public void enableRawMode() {
        int[] mode = new int[1];
        boolean isConsoleIn = GetConsoleMode(handleConsoleInput, mode) != 0;
        if (isConsoleIn) {
            // (ENABLE_VIRTUAL_TERMINAL_INPUT) ON; (ENABLE_ECHO_INPUT | ENABLE_LINE_INPUT | ENABLE_PROCESSED_INPUT) OFF.
            SetConsoleMode(handleConsoleInput, (mode[0] | 0x200) & ~0x7);
        }
        boolean isConsoleOut = GetConsoleMode(handleConsoleOutput, mode) != 0;
        if (isConsoleOut) {
            // (ENABLE_PROCESSED_OUTPUT | ENABLE_VIRTUAL_TERMINAL_PROCESSING) ON; (ENABLE_WRAP_AT_EOL_OUTPUT) OFF.
            SetConsoleMode(handleConsoleOutput, (mode[0] | 0x5) & ~0x2);
        }
        SetConsoleOutputCP(65001); // UTF-8
        if (!StandardCharsets.UTF_8.equals(System.out.charset())) {
            var utf8 = new PrintStream(System.out, true, StandardCharsets.UTF_8);
            System.setOut(utf8);
        }
    }

    @Override
    public void reset() {
        SetConsoleMode(handleConsoleInput, originalConsoleModeInput);
        SetConsoleMode(handleConsoleOutput, originalConsoleModeOutput);
        SetConsoleOutputCP(originalConsoleOutputCP);
        System.setOut(originalSystemOut);
    }


    @Override
    public int read() {
        int[] count = new int[1];
        while (true) {
            GetNumberOfConsoleInputEvents(handleConsoleInput, count);
            if (count[0] < 1) {
                return -1;
            }
            INPUT_RECORD[] inputRecords;
            try {
                inputRecords = readConsoleInputHelper(handleConsoleInput, 1, false);
            } catch (IOException e) {
                return -1;
            }
            if (inputRecords.length < 1) {
                continue;
            }
            INPUT_RECORD inputRecord = inputRecords[0];
            if (inputRecord.eventType != KEY_EVENT) {
                continue;
            }
            KEY_EVENT_RECORD keyEventRecord = inputRecord.keyEvent;
            if (!keyEventRecord.keyDown) {
                continue;
            }
            return keyEventRecord.uchar;
        }
    }

    @Override
    public void write(int data) {
        System.out.write(data);
    }

    @Override
    public void print(String text) {
        System.out.print(text);
    }
}
