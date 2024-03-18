package com.davidconneely.eightbit.zx81;

import com.davidconneely.eightbit.IBus;

import java.io.Closeable;

public interface IDisplay extends AutoCloseable {
    void init();
    void close();
    void renderDFile(IBus bus, int address);
}
