package eightbit;

public interface ICore {
    void resetCycleCount();
    int getCycleCount();
    void runOneInstruction();
    short getPC();
    
}
