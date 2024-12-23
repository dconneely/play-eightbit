package com.davidconneely.eightbit.zx81;

import org.fusesource.jansi.internal.CLibrary.Termios;
import org.fusesource.jansi.internal.JansiLoader;

import java.io.IOException;
import java.util.Arrays;

import static org.fusesource.jansi.internal.CLibrary.TCSANOW;
import static org.fusesource.jansi.internal.CLibrary.tcgetattr;
import static org.fusesource.jansi.internal.CLibrary.tcsetattr;

class MacOsTerminalSupport extends TerminalSupport {
    private static final MacOsTerminalSupport INSTANCE;
    // POSIX termios constants (from termios.h)
    private static final int STDIN_FILENO = 0;
    private static final int ECHO = 0x00000008;
    private static final int ICANON = 0x00000100;
    // macOS-specific values
    private static final int VMIN = 16;
    private static final int VTIME = 17;

    private final Termios originalTermios;

    static {
        JansiLoader.initialize();
        INSTANCE = new MacOsTerminalSupport();
    }

    static MacOsTerminalSupport instance() {
        return INSTANCE;
    }

    // private to prevent other classes instantiating this.
    private MacOsTerminalSupport() {
        originalTermios = new Termios(); // Struct to store original terminal attributes
        tcgetattr(STDIN_FILENO, originalTermios);
    }

    @Override
    void enableRawMode() {
        final Termios rawTermios = new Termios(); // Struct for raw mode attributes
        rawTermios.c_iflag = originalTermios.c_iflag;
        rawTermios.c_oflag = originalTermios.c_oflag;
        rawTermios.c_cflag = originalTermios.c_cflag;
        rawTermios.c_lflag = originalTermios.c_lflag;
        rawTermios.c_cc = Arrays.copyOf(originalTermios.c_cc, originalTermios.c_cc.length);
        // Modify for raw mode.
        rawTermios.c_lflag &= ~(ECHO|ICANON);
        rawTermios.c_cc[VMIN] = 1; // Minimum number of characters to read
        rawTermios.c_cc[VTIME] = 0; // No
        final int errcode = tcsetattr(STDIN_FILENO, TCSANOW, rawTermios);
        if (errcode != 0) {
            throw new IllegalStateException("Failed to set terminal attributes to raw mode");
        }
    }

    @Override
    void reset() {
        final int errcode = tcsetattr(STDIN_FILENO, TCSANOW, originalTermios);
        if (errcode != 0) {
            throw new IllegalStateException("Failed to restore terminal attributes to raw mode");
        }
    }

    @Override
    int read() {
        try {
            return System.in.available() > 0 ? System.in.read() : -1;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    void write(int data) {
        System.out.write(data);
    }

    @Override
    void print(String text) {
        System.out.print(text);
    }
}

