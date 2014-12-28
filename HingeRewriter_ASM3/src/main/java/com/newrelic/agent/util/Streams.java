package com.newrelic.agent.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Streams {
    public static final int DEFAULT_BUFFER_SIZE = 8192;

    public static int copy(InputStream input, OutputStream output)
            throws IOException {
        return copy(input, output, 8192, false);
    }

    public static int copy(InputStream input, OutputStream output, boolean closeStreams) throws IOException {
        return copy(input, output, 8192, closeStreams);
    }

    public static int copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        return copy(input, output, bufferSize, false);
    }

    public static int copy(InputStream input, OutputStream output, int bufferSize, boolean closeStreams)
            throws IOException {
        try {
            byte[] buffer = new byte[bufferSize];
            int count = 0;
            int n = 0;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
                count += n;
            }
            return count;
        } finally {
            if (closeStreams) {
                input.close();
                output.close();
            }
        }
    }

    public static byte[] slurpBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            copy(in, out);
            out.flush();
            return out.toByteArray();
        } finally {
            out.close();
        }
    }

    public static String slurp(InputStream in, String encoding) throws IOException {
        byte[] bytes = slurpBytes(in);
        return new String(bytes, encoding);
    }

    public static void copyBytesToFile(File file, byte[] newBytes)
            throws IOException {
        OutputStream oStream = new FileOutputStream(file);
        try {
            copy(new ByteArrayInputStream(newBytes), oStream, true);
        } finally {
            oStream.close();
        }
    }
}

/* Location:           /home/cw/class-rewriter/class-rewriter-4.120.0.jar
 * Qualified Name:     com.newrelic.agent.util.Streams
 * JD-Core Version:    0.6.2
 */