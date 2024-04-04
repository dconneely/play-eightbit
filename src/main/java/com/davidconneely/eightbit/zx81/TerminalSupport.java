package com.davidconneely.eightbit.zx81;

public interface TerminalSupport {
    void enableRawMode();

    void reset();

    int read();

    void write(int data);

    void print(String text);
}
