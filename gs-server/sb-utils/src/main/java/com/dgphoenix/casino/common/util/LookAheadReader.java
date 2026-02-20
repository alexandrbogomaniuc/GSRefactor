package com.dgphoenix.casino.common.util;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndexes;

/**
 * User: flsh
 * Date: 21.09.16.
 * see com.google.common.io.CharSequenceReader, this implementation required for solve package visibility
 * also, this implementation is not threadsafe
 */
public class LookAheadReader extends Reader {

    private CharSequence seq;
    private int pos;
    private int mark;

    /**
     * Creates a new reader wrapping the given character sequence.
     */
    public LookAheadReader(CharSequence seq) {
        this.seq = checkNotNull(seq);
    }

    private void checkOpen() throws IOException {
        if (seq == null) {
            throw new IOException("reader closed");
        }
    }

    private boolean hasRemaining() {
        return remaining() > 0;
    }

    private int remaining() {
        return seq.length() - pos;
    }

    @Override
    public int read(CharBuffer target) throws IOException {
        checkNotNull(target);
        checkOpen();
        if (!hasRemaining()) {
            return -1;
        }
        int charsToRead = Math.min(target.remaining(), remaining());
        for (int i = 0; i < charsToRead; i++) {
            target.put(seq.charAt(pos++));
        }
        return charsToRead;
    }

    @Override
    public int read() throws IOException {
        checkOpen();
        return hasRemaining() ? seq.charAt(pos++) : -1;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        checkPositionIndexes(off, off + len, cbuf.length);
        checkOpen();
        if (!hasRemaining()) {
            return -1;
        }
        int charsToRead = Math.min(len, remaining());
        for (int i = 0; i < charsToRead; i++) {
            cbuf[off + i] = seq.charAt(pos++);
        }
        return charsToRead;
    }

    @Override
    public long skip(long n) throws IOException {
        checkArgument(n >= 0, "n (%s) may not be negative", n);
        checkOpen();
        int charsToSkip = (int) Math.min(remaining(), n); // safe because remaining is an int
        pos += charsToSkip;
        return charsToSkip;
    }

    @Override
    public boolean ready() throws IOException {
        checkOpen();
        return true;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        checkArgument(readAheadLimit >= 0, "readAheadLimit (%s) may not be negative", readAheadLimit);
        checkOpen();
        mark = pos;
    }

    @Override
    public void reset() throws IOException {
        checkOpen();
        pos = mark;
    }

    @Override
    public void close() throws IOException {
        seq = null;
    }

    public void rewind(int rewindPos) throws IOException {
        checkOpen();
        pos = rewindPos;
        mark = rewindPos;
    }

    public int lookAhead() throws IOException {
        mark(1);
        final int c = super.read();
        reset();
        return c;
    }

    public int skipWhiteSpacesAndLookAhead() throws IOException {
        while(true) {
            int current = lookAhead();
            if(current == -1) {
                return current;
            }
            if (Character.isSpaceChar((char) current)) {
                read();
            } else {
                return current;
            }
        }

    }

    public int getCurrentPosition() {
        return pos;
    }

}
