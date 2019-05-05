package org.capnproto;

import java.nio.ByteBuffer;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ByteBufferFormatterTest {

    @Test
    public void testDefaultSmall() {
        final String format = new ByteBufferFormatter().format(ByteBuffer.wrap("Text".getBytes()));
        assertThat(format, is("4 bytes 54 65 78 74 |Text"));
    }

    @Test
    public void testSizeHexSmall() {
        final String format = new ByteBufferFormatter().setText(false).format(ByteBuffer.wrap("Text".getBytes()));
        assertThat(format, is("4 bytes 54 65 78 74 "));
    }

    @Test
    public void testHexSmall() {
        final String format = new ByteBufferFormatter().setShowSize(false).setText(false).format(ByteBuffer.wrap("Text".getBytes()));
        assertThat(format, is("54 65 78 74 "));
    }

    @Test
    public void testTextSmall() {
        final String format = new ByteBufferFormatter().setShowSize(false).setHex(false).format(ByteBuffer.wrap("Text".getBytes()));
        assertThat(format, is("Text"));
    }

    @Test
    public void testDefaultBig() {
        final String format = new ByteBufferFormatter().format(ByteBuffer.wrap("Text                                     sip".getBytes()));
        assertThat(format, is("44 bytes 54 65 78 74 20 20 20 20 20 20 ...20 20 20 73 69 70 |Text............sip"));
    }

    @Test
    public void testSizeHexBig() {
        final String format = new ByteBufferFormatter().setText(false).format(ByteBuffer.wrap("Text                                     sip".getBytes()));
        assertThat(format, is("44 bytes 54 65 78 74 20 20 20 20 20 20 ...20 20 20 73 69 70 "));
    }

    @Test
    public void testHexBig() {
        final String format = new ByteBufferFormatter().setShowSize(false).setText(false).format(ByteBuffer.wrap("Text                                     sip".getBytes()));
        assertThat(format, is("54 65 78 74 20 20 20 20 20 20 ...20 20 20 73 69 70 "));
    }

    @Test
    public void testTextBig() {
        final String format = new ByteBufferFormatter().setShowSize(false).setHex(false).format(ByteBuffer.wrap("Text                                     sip".getBytes()));
        assertThat(format, is("Text............sip"));
    }

}
