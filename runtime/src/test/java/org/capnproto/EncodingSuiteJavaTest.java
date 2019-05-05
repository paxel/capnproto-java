package org.capnproto;

import org.capnproto.test.Test.TestAllTypes;
import org.capnproto.test.Test.TestAnyPointer;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Assert;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class EncodingSuiteJavaTest {

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
    public void testToString() {
        MessageBuilder builder = new MessageBuilder();
        final TestAllTypes.Builder root = builder.initRoot(TestAllTypes.factory);
        TestUtilJava.initTestMessage(root);
        assertThat(root.toString(), is("TestAllTypes { VoidField=(VOID) BoolField=(true) Int8Field=(-123) Int16Field=(-12345) Int32Field=(-12345678) Int64Field=(-123456789012345) UInt8Field=(-22) UInt16Field=(17767) UInt32Field=(878082192) UInt64Field=(1311768467284833366) Float32Field=(1234.5) Float64Field=(-1.23E47) TextField=(foo) DataField=(Data{8192 bytes 00 00 00 00 06 00 13 00 01 85 ...00 00 00 00 00 00 |...................}) StructField={TestAllTypes { VoidField=(VOID) BoolField=(true) Int8Field=(-12) Int16Field=(3456) Int32Field=(-78901234) Int64Field=(56789012345678) UInt8Field=(90) UInt16Field=(1234) UInt32Field=(56789012) UInt64Field=(345678901234567890) Float32Field=(-1.25E-10) Float64Field=(345.0) TextField=(baz) DataField=(Data{8192 bytes 00 00 00 00 06 00 13 00 01 85 ...00 00 00 00 00 00 |...................}) StructField={TestAllTypes { VoidField=(VOID) BoolField=(false) Int8Field=(0) Int16Field=(0) Int32Field=(0) Int64Field=(0) UInt8Field=(0) UInt16Field=(0) UInt32Field=(0) UInt64Field=(0) Float32Field=(0.0) Float64Field=(0.0) TextField=(nested) StructField={TestAllTypes { VoidField=(VOID) BoolField=(false) Int8Field=(0) Int16Field=(0) Int32Field=(0) Int64Field=(0) UInt8Field=(0) UInt16Field=(0) UInt32Field=(0) UInt64Field=(0) Float32Field=(0.0) Float64Field=(0.0) TextField=(really nested) EnumField=(FOO) InterfaceField=(VOID)}} EnumField=(FOO) InterfaceField=(VOID)}} EnumField=(BAZ) InterfaceField=(VOID) BoolList={false, true, false, true, true}}} EnumField=(CORGE) InterfaceField=(VOID) VoidList={VOID, VOID, VOID, VOID, VOID, VOID} BoolList={true, false, false, true} Float64List={7777.75, Infinity, -Infinity, NaN} TextList={plugh, xyzzy, thud} StructList={TestAllTypes { VoidField=(VOID) BoolField=(false) Int8Field=(0) Int16Field=(0) Int32Field=(0) Int64Field=(0) UInt8Field=(0) UInt16Field=(0) UInt32Field=(0) UInt64Field=(0) Float32Field=(0.0) Float64Field=(0.0) TextField=(structlist 1) EnumField=(FOO) InterfaceField=(VOID)},TestAllTypes { VoidField=(VOID) BoolField=(false) Int8Field=(0) Int16Field=(0) Int32Field=(0) Int64Field=(0) UInt8Field=(0) UInt16Field=(0) UInt32Field=(0) UInt64Field=(0) Float32Field=(0.0) Float64Field=(0.0) TextField=(structlist 2) EnumField=(FOO) InterfaceField=(VOID)},TestAllTypes { VoidField=(VOID) BoolField=(false) Int8Field=(0) Int16Field=(0) Int32Field=(0) Int64Field=(0) UInt8Field=(0) UInt16Field=(0) UInt32Field=(0) UInt64Field=(0) Float32Field=(0.0) Float64Field=(0.0) TextField=(structlist 3) EnumField=(FOO) InterfaceField=(VOID)}} EnumList={FOO,GARPLY}}"));
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
