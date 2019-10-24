package org.capnproto;

import java.io.IOException;
import java.nio.ByteBuffer;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Test;

public class SerializedPackedSuiteJavaTest {

    public void expectPacksTo(byte[] unpacked, byte[] packed) throws IOException {
        // ----
        // write
        {
            byte[] bytes = new byte[packed.length];
            ArrayOutputStream writer = new ArrayOutputStream(ByteBuffer.wrap(bytes));
            PackedOutputStream packedOutputStream = new PackedOutputStream(writer);
            packedOutputStream.write(ByteBuffer.wrap(unpacked));

            assertThat(bytes, is(packed));
        }

        // ------
        // read
        {
            ArrayInputStream reader = new ArrayInputStream(ByteBuffer.wrap(packed));
            PackedInputStream packedInputStream = new PackedInputStream(reader);
            byte[] bytes = new byte[unpacked.length];
            int n = packedInputStream.read(ByteBuffer.wrap(bytes));

            assertThat(n, is(unpacked.length));

            assertThat(bytes, is(unpacked));
        }
    }

    @Test
    public void testSimplePacking() throws IOException {
        expectPacksTo(new byte[]{}, new byte[]{});
        expectPacksTo(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}, new byte[]{0, 0});
        expectPacksTo(new byte[]{0, 0, 12, 0, 0, 34, 0, 0}, new byte[]{0x24, 12, 34});
        expectPacksTo(new byte[]{1, 3, 2, 4, 5, 7, 6, 8}, new byte[]{(byte) 0xff, 1, 3, 2, 4, 5, 7, 6, 8, 0});
        expectPacksTo(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 1, 3, 2, 4, 5, 7, 6, 8},
                new byte[]{0, 0, (byte) 0xff, 1, 3, 2, 4, 5, 7, 6, 8, 0});
        expectPacksTo(new byte[]{0, 0, 12, 0, 0, 34, 0, 0, 1, 3, 2, 4, 5, 7, 6, 8},
                new byte[]{(byte) 0x24, 12, 34, (byte) 0xff, 1, 3, 2, 4, 5, 7, 6, 8, 0});
        expectPacksTo(new byte[]{1, 3, 2, 4, 5, 7, 6, 8, 8, 6, 7, 4, 5, 2, 3, 1},
                new byte[]{(byte) 0xff, 1, 3, 2, 4, 5, 7, 6, 8, 1, 8, 6, 7, 4, 5, 2, 3, 1});

        expectPacksTo(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5, 6, 7, 8, 0, 2, 4, 0, 9, 0, 5, 1},
                new byte[]{(byte) 0xff, 1, 2, 3, 4, 5, 6, 7, 8, 3, 1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5, 6, 7, 8, (byte) 0xd6, 2, 4, 9, 5, 1});

        expectPacksTo(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5, 6, 7, 8, 6, 2, 4, 3, 9, 0, 5, 1, 1, 2, 3, 4, 5, 6, 7, 8, 0, 2, 4, 0, 9, 0, 5, 1},
                new byte[]{(byte) 0xff, 1, 2, 3, 4, 5, 6, 7, 8, 3, 1, 2, 3, 4, 5, 6, 7, 8, 6, 2, 4, 3, 9, 0, 5, 1, 1, 2, 3, 4, 5, 6, 7, 8, (byte) 0xd6, 2, 4, 9, 5, 1});

        expectPacksTo(new byte[]{8, 0, 100, 6, 0, 1, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 2, 0, 3, 1},
                new byte[]{(byte) 0xed, 8, 100, 6, 1, 1, 2, 0, 2, (byte) 0xd4, 1, 2, 3, 1});

        expectPacksTo(new byte[]{0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new byte[]{0x10, 2, 0x40, 1, 0, 0});

        expectPacksTo(new byte[8 * 200],
                new byte[]{0, (byte) 199});

        byte[] allOnes = new byte[8 * 200];
        for (int i = 0; i < allOnes.length; i++) {
            allOnes[i] = 1;
        }

        byte[] expected = new byte[8 * 200 + 2];
        for (int i = 0; i < expected.length; i++) {
            expected[i] = 1;
        }
        expected[0] = (byte) 0xff;
        expected[9] = (byte) 199;

        expectPacksTo(allOnes, expected);
    }
}
