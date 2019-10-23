package org.capnproto;

import java.io.IOException;
import java.nio.ByteBuffer;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ArrayInputStreamSuiteTest {

    @Test
    public void testEmptyArray() throws IOException {

        ArrayInputStream stream = new ArrayInputStream(ByteBuffer.allocate(0));
        ByteBuffer dst = ByteBuffer.allocate(10);

        // read() should return -1 at the end of the stream
        // https://docs.oracle.com/javase/7/docs/api/java/nio/channels/ReadableByteChannel.html
        int read = stream.read(dst);
        assertThat(read, is(-1));
    }

    @Test
    public void testRequestMore() throws IOException {
        byte[] oneByte = "a".getBytes();
        ArrayInputStream stream = new ArrayInputStream(ByteBuffer.wrap(oneByte));
        ByteBuffer dst = ByteBuffer.allocate(10);
        int read = stream.read(dst);
        assertThat(read, is(1));
        read = stream.read(dst);
        assertThat(read, is(-1));
    }

}
