package com.davidconneely.eightbit.zx81;

import java.io.IOException;

public interface TerminalSupport {
    void enableRawMode();
    void reset();
    int read();
    void write(int data);
    void print(String text);
}
