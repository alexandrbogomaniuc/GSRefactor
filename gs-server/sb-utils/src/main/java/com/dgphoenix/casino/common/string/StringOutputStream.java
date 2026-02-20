package com.dgphoenix.casino.common.string;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by user on 24.12.14.
 */
public class StringOutputStream extends OutputStream
{
    private StringBuilder buf = new StringBuilder();

    public void write( byte[] b ) throws IOException
    {
        buf.append( new String( b ) );
    }

    public void write( byte[] b, int off, int len ) throws IOException
    {
        buf.append( new String( b, off, len ) );
    }

    public void write( int b ) throws IOException
    {
        byte[] bytes = new byte[1];
        bytes[0] = (byte)b;
        buf.append( new String( bytes ) );
    }

    public void removeProlog() {
        String prolog = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        int prologSize = prolog.length();
        int i = buf.indexOf(prolog);
        if(i == 0) {
            buf.replace(i, prologSize, "");
        }
    }

    public String toString()
    {
        return buf.toString();
    }
}