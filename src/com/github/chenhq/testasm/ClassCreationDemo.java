package com.github.chenhq.testasm;

public class ClassCreationDemo {
    private int version;
    
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String toString(){
        return "ClassCreationDemo: "+version;
    }
}
