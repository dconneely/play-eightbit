package com.davidconneely.eightbit.zx81;

import org.fusesource.jansi.internal.CLibrary.Termios;
import org.fusesource.jansi.internal.JansiLoader;

import java.io.IOException;
import java.util.function.Consumer;

import static org.fusesource.jansi.internal.CLibrary.TCSANOW;
import static org.fusesource.jansi.internal.CLibrary.tcgetattr;
import static org.fusesource.jansi.internal.CLibrary.tcsetattr;

class PosixTerminalSupport extends TerminalSupport {
    private static final int STDIN_FILENO = 0;

    private final Termios originalTermios;
    private final Consumer<Termios> cfmakeraw;

    static {
        JansiLoader.initialize();
    }

    static PosixTerminalSupport newInstance(final Consumer<Termios> cfmakeraw) {
        return new PosixTerminalSupport(cfmakeraw);
    }

    // private to prevent other classes instantiating this.
    private PosixTerminalSupport(final Consumer<Termios> cfmakeraw) {
        this.originalTermios = new Termios(); // Struct to store original terminal attributes
        this.cfmakeraw = cfmakeraw;
        tcgetattr(STDIN_FILENO, originalTermios);
    }

    @Override
    void enableRawMode() {
        final Termios rawTermios = new Termios(); // Struct for raw mode attributes
        tcgetattr(STDIN_FILENO, rawTermios);
        this.cfmakeraw.accept(rawTermios); // Modify for raw mode
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
        } catch (final IOException e) {
            throw new IllegalStateException(e);
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
