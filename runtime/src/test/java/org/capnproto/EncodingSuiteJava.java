package org.capnproto;

import org.capnproto.test.Test.TestAnyPointer;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Assert;
import org.junit.Test;

public class EncodingSuiteJava {

    @Test
    public void testSerializedSize() {
//  test("SerializedSize") {
//    val builder = new MessageBuilder()
//    val root = builder.initRoot(TestAnyPointer.factory)
//    root.getAnyPointerField().setAs(Text.factory, new Text.Reader("12345"))
//
//    // one word for segment table, one for the root pointer,
//    // one for the body of the TestAnyPointer struct,
//    // and one for the body of the Text.
//    Serialize.computeSerializedSizeInWords(builder) should equal (4)
//  }
        MessageBuilder builder = new MessageBuilder();
        final TestAnyPointer.Builder root = builder.initRoot(TestAnyPointer.factory);
        root.getAnyPointerField().setAs(Text.factory, new Text.Reader("12345"));
        // one word for segment table, one for the root pointer,
        // one for the body of the TestAnyPointer struct,
        // and one for the body of the Text.
        Assert.assertThat(Serialize.computeSerializedSizeInWords(builder), is(4L));
    }
}
