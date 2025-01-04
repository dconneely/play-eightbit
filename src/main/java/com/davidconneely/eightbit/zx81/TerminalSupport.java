package com.davidconneely.eightbit.zx81;

import java.util.Locale;

abstract class TerminalSupport {
    private static TerminalSupport instance;

    static synchronized TerminalSupport get() {
        if (instance == null) {
            final String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
            if (osName.startsWith("windows")) {
                instance = WindowsTerminalSupport.newInstance();
            } else if (osName.startsWith("mac") || osName.startsWith("darwin")) {
                instance = PosixTerminalSupport.newInstance(MacOsTermiosBits::cfmakeraw);
            }  else if (osName.startsWith("linux")) {
                instance = PosixTerminalSupport.newInstance(LinuxTermiosBits::cfmakeraw);
            } else {
                throw new IllegalStateException("Unsupported operating system: " + osName);
            }
        }
        return instance;
    }

    abstract void enableRawMode();

    abstract void reset();

    abstract int read();

    abstract void write(int data);

    abstract void print(String text);
}
