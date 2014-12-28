package com.newrelic.agent.compile;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Map;

final class FileLogImpl
        implements Log {
    private final PrintWriter writer;
    private final Map<String, String> agentOptions;

    public FileLogImpl(Map<String, String> agentOptions, String logFileName) {
        this.agentOptions = agentOptions;
        try {
            this.writer = new PrintWriter(new FileOutputStream(logFileName));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeln(String level, String message) {
        this.writer.write("[" + level + "] " + message + "\n");
        this.writer.flush();
    }

    public void info(String message) {
        writeln("info", message);
    }

    public void debug(String message) {
        if (this.agentOptions.get("debug") != null)
            writeln("debug", message);
    }

    public void warning(String message) {
        writeln("warn", message);
    }

    public void warning(String message, Throwable cause) {
        writeln("warn", message);
        cause.printStackTrace(this.writer);
        this.writer.flush();
    }

    public void error(String message) {
        writeln("error", message);
    }

    public void error(String message, Throwable cause) {
        writeln("error", message);
        cause.printStackTrace(this.writer);
        this.writer.flush();
    }
}

/* Location:           /home/cw/class-rewriter/class-rewriter-4.120.0.jar
 * Qualified Name:     com.newrelic.agent.compile.FileLogImpl
 * JD-Core Version:    0.6.2
 */