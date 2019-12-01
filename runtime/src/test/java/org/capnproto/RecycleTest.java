package org.capnproto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import org.capnproto.test.MessageJava;
import static org.capnproto.test.MessageJava.Flag.END;
import static org.capnproto.test.MessageJava.Flag.START;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 * Tests the optional recycle of all the Reader and Builder instances that support it.
 */
public class RecycleTest {

    private MessageBuilder messageBuilder;

    @Test
    public void testBuilding() {
        messageBuilder = new MessageBuilder();
        MessageJava.Message.Builder ownedRoot = messageBuilder.initRoot(MessageJava.Message.factory);
        ownedRoot.setTime(23L);
        StructList.Builder<MessageJava.Message.Builder> ownedMessages = ownedRoot.initMessages(2);
        MessageJava.Message.Builder first = ownedMessages.get(0);
        first.setTime(103L);
        first.setData("Don".getBytes());
        first.recycle();
        MessageJava.Message.Builder second = ownedMessages.get(1);
        second.setData("key".getBytes());
        second.recycle();
        assertThat(first, is(sameInstance(second)));
        ownedMessages.recycle();
        ownedRoot.recycle();
    }

    @Test
    public void testReading() throws IOException {
        testBuilding();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        messageBuilder.write(Channels.newChannel(out));

        AllocatedArena arena = new AllocatedArenaBuilder().build(Channels.newChannel(new ByteArrayInputStream(out.toByteArray())));
        MessageJava.Message.Reader ownedMessage = new MessageReader(arena).getRoot(MessageJava.Message.factory);
        ownedMessage.doMessages(l -> {
            assertThat(l.size(), is(2));
            MessageJava.Message.Reader first = l.get(0);
            assertThat(first.getTime(), is(103L));
            final Data.Reader firstData = first.getData();
            assertThat(firstData.toString(), is("Data{3 bytes 44 6F 6E |Don}"));
            firstData.recycle();
            first.recycle();
            MessageJava.Message.Reader second = l.get(1);
            assertThat(second.getTime(), is(0L));
            final Data.Reader secondData = second.getData();
            assertThat(secondData.toString(), is("Data{3 bytes 6B 65 79 |key}"));
            secondData.recycle();

            // all data was in the same instances, because it was recycled before getting the next instance
            assertThat(first, is(sameInstance(second)));
            assertThat(firstData, is(sameInstance(secondData)));

            l.recycle();
        });
        ownedMessage.recycle();

    }

}
