package com.davidconneely.eightbit.zx81;

import java.util.Locale;

public interface TerminalSupport {
    static TerminalSupport get() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (osName.startsWith("windows")) {
            return WindowsTerminalSupport.INSTANCE;
        //} else if (osName.startsWith("mac")) {
        //    return MacOsTerminalSupport.INSTANCE;
        //} else if (osName.startsWith("linux")) {
        //    return LinuxTerminalSupport.INSTANCE;
        } else {
            return null;
        }
    }

    void enableRawMode();

    void reset();

    int read();

    void write(int data);

    void print(String text);
}
