/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.capnproto;

import java.io.IOException;
import java.nio.ByteBuffer;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class SerializeSuiteTest {

    /**
     * @param exampleSegmentCount number of segments
     * @param exampleBytes        byte array containing `segmentCount` segments; segment `i` contains `i` words each set to `i`
     *
     * @throws java.io.IOException
     */
    public void expectSerializesTo(int exampleSegmentCount, byte[] exampleBytes) throws IOException {

        MessageReader read = Serialize.read(new ArrayInputStream(ByteBuffer.wrap(exampleBytes)));
        // ----
        // read via ReadableByteChannel

        checkSegmentContents(read.arena, exampleSegmentCount);

        // ------
        // read via ByteBuffer
        {
            MessageReader messageReader = Serialize.read(ByteBuffer.wrap(exampleBytes));
            checkSegmentContents(messageReader.arena, exampleSegmentCount);
        }
    }

    private void checkSegmentContents(AllocatedArena arena, int exampleSegmentCount) {
        assertThat(arena.getSegments().size(), is(exampleSegmentCount));

        for (int i = 0; i < exampleSegmentCount; i++) {

            GenericSegmentReader segment = arena.getSegments().get(i);
            DataView srcView = segment.getBuffer();

            assertThat(srcView.limit(), is(i * 8));
            srcView.rewindReaderPosition();
            while (srcView.hasRemainingReadableBytes()) {
                assertThat(srcView.getByte(), is((byte) i));
            }
        }
    }

    @Test
    public void testOne() throws IOException {
        // When transmitting over a stream, the following should be sent. All integers are unsigned and little-endian.
        // - (4 bytes) The number of segments, minus one (since there is always at least one segment).
        // - (N * 4 bytes) The size of each segment, in words.
        // - (0 or 4 bytes) Padding up to the next word boundary.
        // - The content of each segment, in order.

        expectSerializesTo(1, new byte[]{
            0, 0, 0, 0, // 1 segment
            0, 0, 0, 0 // Segment 0 contains 0 bytes
        // No padding
        // Segment 0 (empty)
        });
    }

    @Test
    public void testTwo() throws IOException {
        expectSerializesTo(2, new byte[]{
            1, 0, 0, 0, // 2 segments
            0, 0, 0, 0, // Segment 0 contains 0 words
            1, 0, 0, 0, // Segment 1 contains 1 words
            // Padding
            0, 0, 0, 0,
            // Segment 0 (empty)
            // Segment 1
            1, 1, 1, 1, 1, 1, 1, 1
        });

    }

    @Test
    public void testThree() throws IOException {
        expectSerializesTo(3, new byte[]{
            2, 0, 0, 0, // 3 segments
            0, 0, 0, 0, // Segment 0 contains 0 words
            1, 0, 0, 0, // Segment 1 contains 1 words
            2, 0, 0, 0, // Segment 2 contains 2 words
            // No padding
            // Segment 0 (empty)
            // Segment 1
            1, 1, 1, 1, 1, 1, 1, 1,
            // Segment 2
            2, 2, 2, 2, 2, 2, 2, 2,
            2, 2, 2, 2, 2, 2, 2, 2
        });

    }

    @Test
    public void testFour() throws IOException {
        expectSerializesTo(4, new byte[]{
            3, 0, 0, 0, // 4 segments
            0, 0, 0, 0, // Segment 0 contains 0 words
            1, 0, 0, 0, // Segment 1 contains 1 words
            2, 0, 0, 0, // Segment 2 contains 2 words
            3, 0, 0, 0, // Segment 3 contains 3 words
            // Padding
            0, 0, 0, 0,
            // Segment 0 (empty)
            // Segment 1
            1, 1, 1, 1, 1, 1, 1, 1,
            // Segment 2
            2, 2, 2, 2, 2, 2, 2, 2,
            2, 2, 2, 2, 2, 2, 2, 2,
            // Segment 3
            3, 3, 3, 3, 3, 3, 3, 3,
            3, 3, 3, 3, 3, 3, 3, 3,
            3, 3, 3, 3, 3, 3, 3, 3
        });
    }
}
