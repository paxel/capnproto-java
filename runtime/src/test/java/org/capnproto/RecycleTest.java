package org.capnproto;

import org.capnproto.test.MessageJava;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 * Tests the optional recycle of all the Reader and Builder instances that support it.
 */
public class RecycleTest {

    @Test
    public void testBuilding() {
        MessageBuilder messageBuilder = new MessageBuilder();
        MessageJava.Message.Builder ownedRoot = messageBuilder.initRoot(MessageJava.Message.factory);
        ownedRoot.setTime(23L);
        StructList.Builder<MessageJava.Message.Builder> ownedMessages = ownedRoot.initMessages(2);
        MessageJava.Message.Builder first = ownedMessages.get(0);
        first.setFlag(MessageJava.Flag.START);
        first.setTime(103L);
        first.setData("Don".getBytes());
        //first.recycle();
        MessageJava.Message.Builder second = ownedMessages.get(1);
        second.setFlag(MessageJava.Flag.END);
        second.setData("key".getBytes());
        //second.recycle();
        assertThat(first, is(sameInstance(second)));
        ownedMessages.recycle();
        //ownedRoot.recylce();

    }
}
