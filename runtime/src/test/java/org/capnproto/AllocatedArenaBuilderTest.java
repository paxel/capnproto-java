package org.capnproto;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.List;
import org.hamcrest.CoreMatchers;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class AllocatedArenaBuilderTest {

    @Test
    public void eofChannelShouldReturnNull() throws IOException {

        final AllocatedArena arena = new AllocatedArenaBuilder().build(Channels.newChannel(new ByteArrayInputStream(new byte[0])));
        assertThat(arena, is(CoreMatchers.nullValue()));
    }

    @Test(expected = IOException.class)
    public void oneByteChannelShouldThrowIOE() throws IOException {

        new AllocatedArenaBuilder().build(Channels.newChannel(new ByteArrayInputStream(new byte[1])));
    }

    @Test
    public void eofByteBufferShouldReturnNull() throws IOException {

        final AllocatedArena arena = new AllocatedArenaBuilder().build(ByteBuffer.wrap((new byte[0])));
        assertThat(arena, is(CoreMatchers.nullValue()));
    }

    @Test(expected = java.nio.BufferUnderflowException.class)
    public void oneByteByteBufferShouldThrowException() throws IOException {

        new AllocatedArenaBuilder().build(ByteBuffer.wrap((new byte[1])));
    }

    @Test
    public void readDoubleFarPointer() throws IOException {
//        
//  test("DoubleFarPointers") {
//    val bytes = Array[Byte](2,0,0,0, 1,0,0,0, 2,0,0,0, 1,0,0,0,
//                            6,0,0,0, 1,0,0,0, 2,0,0,0, 2,0,0,0,
//                            0,0,0,0, 1,0,0,0, 1,7, -1, 127, 0,0,0,0)
//
//    val input = new ArrayInputStream (java.nio.ByteBuffer.wrap(bytes))
//    val message = org.capnproto.Serialize.read(input)
//    val root = message.getRoot(TestAllTypes.factory)
//    root.getBoolField() should equal (true)
//    root.getInt8Field() should equal (7)
//    root.getInt16Field() should equal (32767)
//  }

        byte[] in = new byte[]{
            // number of segments -1
            2, 0, 0, 0,
            // size of first segment
            1, 0, 0, 0,
            // size of second segment
            2, 0, 0, 0,
            // size of third segment
            1, 0, 0, 0,
            // first segment
            6, 0, 0, 0,
            1, 0, 0, 0,
            // second segment
            2, 0, 0, 0,
            2, 0, 0, 0,
            0, 0, 0, 0,
            1, 0, 0, 0,
            // third segment
            1, 7, -1, 127,
            0, 0, 0, 0};
        final MessageReader read = new MessageReader(new AllocatedArenaBuilder().build(Channels.newChannel(new ByteArrayInputStream(in))));

        final List<? extends GenericSegmentReader> segments = read.getArena().getSegments();
        assertThat(segments.size(), is(3));
        assertThat(getSegmentArray(segments.get(0)), is(new byte[]{
            6, 0, 0, 0,
            1, 0, 0, 0
        }));
        assertThat(getSegmentArray(segments.get(1)), is(new byte[]{
            2, 0, 0, 0,
            2, 0, 0, 0,
            0, 0, 0, 0,
            1, 0, 0, 0
        }));
        assertThat(getSegmentArray(segments.get(2)), is(new byte[]{
            1, 7, -1, 127,
            0, 0, 0, 0
        }));

        read.getRoot(org.capnproto.test.Test.TestAllTypes.factory);

    }

    private static byte[] getSegmentArray(GenericSegmentReader segment) {
        final byte[] result = new byte[segment.getBuffer().remaining()];
        segment.getBuffer().get(result);
        return result;
    }
}
