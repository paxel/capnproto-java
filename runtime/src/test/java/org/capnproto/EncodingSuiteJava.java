package org.capnproto;

import org.capnproto.test.Test.TestAllTypes;
import org.capnproto.test.Test.TestAnyPointer;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Assert;
import org.junit.Test;

public class EncodingSuiteJava {

    @Test
    public void testSerializedSize() {
        MessageBuilder builder = new MessageBuilder();
        final TestAnyPointer.Builder root = builder.initRoot(TestAnyPointer.factory);
        root.getAnyPointerField().setAs(Text.factory, new Text.Reader("12345"));
        // one word for segment table, one for the root pointer,
        // one for the body of the TestAnyPointer struct,
        // and one for the body of the Text.
        Assert.assertThat(builder.getSerializedSize(), is(32L));
    }

    @Test
    public void testAllData() {

        MessageBuilder builder = new MessageBuilder();
        final TestAllTypes.Builder root = builder.initRoot(TestAllTypes.factory);
        TestUtilJava.initTestMessage(root);
        TestUtilJava.checkTestMessage(root);
        TestUtilJava.checkTestMessage(root.asReader());
    }

    @Test
    public void testAllTypesMultiSegment() {
        MessageBuilder message = new MessageBuilder(5, BuilderArena.AllocationStrategy.FIXED_SIZE);
        TestAllTypes.Builder allTypes = message.initRoot(TestAllTypes.factory);
        TestUtilJava.initTestMessage(allTypes);

        TestUtilJava.checkTestMessage(allTypes);
        TestUtilJava.checkTestMessage(allTypes.asReader());

    }
}
