package com.davidconneely.eightbit.zx81;

import java.util.Locale;

public abstract class TerminalSupport {
    static TerminalSupport get() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (osName.startsWith("windows")) {
            return WindowsTerminalSupport.instance();
        } else if (osName.startsWith("mac")) {
            return MacOsTerminalSupport.instance();
        //} else if (osName.startsWith("linux")) {
        //    return LinuxTerminalSupport.INSTANCE;
        } else {
            return null;
        }
    }

    abstract void enableRawMode();

    abstract void reset();

    abstract int read();

    abstract void write(int data);

    abstract void print(String text);
}
