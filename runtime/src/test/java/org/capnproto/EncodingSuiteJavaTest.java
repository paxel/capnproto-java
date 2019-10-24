package org.capnproto;

import java.io.IOException;
import java.util.logging.Logger;
import static org.capnproto.test.Test.GLOBAL_INT;
import org.capnproto.test.Test.GenericMap;
import org.capnproto.test.Test.TestAllTypes;
import org.capnproto.test.Test.TestAnyPointer;
import org.capnproto.test.Test.TestConstants;
import org.capnproto.test.Test.TestDefaults;
import org.capnproto.test.Test.TestEmptyStruct;
import org.capnproto.test.Test.TestEnum;
import org.capnproto.test.Test.TestGenerics;
import org.capnproto.test.Test.TestGroups;
import org.capnproto.test.Test.TestLists;
import org.capnproto.test.Test.TestNewVersion;
import org.capnproto.test.Test.TestOldVersion;
import org.capnproto.test.Test.TestUnion;
import org.capnproto.test.Test.TestUseGenerics;
import org.capnproto.test.TestImport;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Assert;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Ignore;
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
        final String[] result = root.toString().split("\\) ");
        final String[] expectedSplit = "TestAllTypes { VoidField=(VOID) BoolField=(true) Int8Field=(-123) Int16Field=(-12345) Int32Field=(-12345678) Int64Field=(-123456789012345) UInt8Field=(-22) UInt16Field=(17767) UInt32Field=(878082192) UInt64Field=(1311768467284833366) Float32Field=(1234.5) Float64Field=(-1.23E47) TextField=(foo) DataField=(Data{3 bytes 62 61 72 |bar}) StructField={TestAllTypes { VoidField=(VOID) BoolField=(true) Int8Field=(-12) Int16Field=(3456) Int32Field=(-78901234) Int64Field=(56789012345678) UInt8Field=(90) UInt16Field=(1234) UInt32Field=(56789012) UInt64Field=(345678901234567890) Float32Field=(-1.25E-10) Float64Field=(345.0) TextField=(baz) DataField=(Data{3 bytes 71 75 78 |qux}) StructField={TestAllTypes { VoidField=(VOID) BoolField=(false) Int8Field=(0) Int16Field=(0) Int32Field=(0) Int64Field=(0) UInt8Field=(0) UInt16Field=(0) UInt32Field=(0) UInt64Field=(0) Float32Field=(0.0) Float64Field=(0.0) TextField=(nested) StructField={TestAllTypes { VoidField=(VOID) BoolField=(false) Int8Field=(0) Int16Field=(0) Int32Field=(0) Int64Field=(0) UInt8Field=(0) UInt16Field=(0) UInt32Field=(0) UInt64Field=(0) Float32Field=(0.0) Float64Field=(0.0) TextField=(really nested) EnumField=(FOO) InterfaceField=(VOID)}} EnumField=(FOO) InterfaceField=(VOID)}} EnumField=(BAZ) InterfaceField=(VOID) BoolList={false, true, false, true, true}}} EnumField=(CORGE) InterfaceField=(VOID) VoidList={VOID, VOID, VOID, VOID, VOID, VOID} BoolList={true, false, false, true} Float64List={7777.75, Infinity, -Infinity, NaN} TextList={plugh, xyzzy, thud} StructList={TestAllTypes { VoidField=(VOID) BoolField=(false) Int8Field=(0) Int16Field=(0) Int32Field=(0) Int64Field=(0) UInt8Field=(0) UInt16Field=(0) UInt32Field=(0) UInt64Field=(0) Float32Field=(0.0) Float64Field=(0.0) TextField=(structlist 1) EnumField=(FOO) InterfaceField=(VOID)},TestAllTypes { VoidField=(VOID) BoolField=(false) Int8Field=(0) Int16Field=(0) Int32Field=(0) Int64Field=(0) UInt8Field=(0) UInt16Field=(0) UInt32Field=(0) UInt64Field=(0) Float32Field=(0.0) Float64Field=(0.0) TextField=(structlist 2) EnumField=(FOO) InterfaceField=(VOID)},TestAllTypes { VoidField=(VOID) BoolField=(false) Int8Field=(0) Int16Field=(0) Int32Field=(0) Int64Field=(0) UInt8Field=(0) UInt16Field=(0) UInt32Field=(0) UInt64Field=(0) Float32Field=(0.0) Float64Field=(0.0) TextField=(structlist 3) EnumField=(FOO) InterfaceField=(VOID)}} EnumList={FOO,GARPLY}}".split("\\) ");

        for (int i = 0; i < expectedSplit.length; i++) {
            String expectedPart = expectedSplit[i];
            if (result.length <= i) {
                Assert.fail("Not enough data in result for " + expectedPart);
            }
            assertThat(result[i], is(expectedPart));
        }
        assertThat(result, is(expectedSplit));
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

    @Test
    public void testAllTypes() {
        MessageBuilder message = new MessageBuilder();
        TestAllTypes.Builder allTypes = message.initRoot(TestAllTypes.factory);
        TestUtilJava.initTestMessage(allTypes);
        TestUtilJava.checkTestMessage(allTypes);
        TestUtilJava.checkTestMessage(allTypes.asReader());
    }

    @Test
    public void testSetters() {
        MessageBuilder message = new MessageBuilder();
        TestAllTypes.Builder allTypes = message.initRoot(TestAllTypes.factory);
        TestUtilJava.initTestMessage(allTypes);

        MessageBuilder message2 = new MessageBuilder();
        TestAllTypes.Builder allTypes2 = message2.initRoot(TestAllTypes.factory);

        allTypes2.setStructField(allTypes.asReader());
        TestUtilJava.checkTestMessage(allTypes2.getStructField());
        TestAllTypes.Reader reader = allTypes2.asReader().getStructField();
        TestUtilJava.checkTestMessage(reader);
    }

    @Test
    public void testZeroing() {
        MessageBuilder message = new MessageBuilder();
        TestAllTypes.Builder allTypes = message.initRoot(TestAllTypes.factory);

        StructList.Builder<TestAllTypes.Builder> structList = allTypes.initStructList(3);
        TestUtilJava.initTestMessage(structList.get(0));

        TestAllTypes.Builder structField = allTypes.initStructField();
        TestUtilJava.initTestMessage(structField);

        TestUtilJava.initTestMessage(structList.get(1));
        TestUtilJava.initTestMessage(structList.get(2));
        TestUtilJava.checkTestMessage(structList.get(0));
        allTypes.initStructList(0);

        TestUtilJava.checkTestMessage(allTypes.getStructField());
        TestAllTypes.Reader allTypesReader = allTypes.asReader();
        TestUtilJava.checkTestMessage(allTypesReader.getStructField());

        AnyPointer.Builder any = message.initRoot(AnyPointer.factory);
        DataView[] segments = message.getArena().getSegmentsForOutput();
        for (DataView segment : segments) {
            for (int jj = 0; jj < segment.limit() - 1; jj++) {
                assertThat(segment.get(jj), is((byte) 0));
            }
        }
    }

    @Test
    public void testDoubleFarPointers() throws IOException {
        byte[] bytes = new byte[]{
            2, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0,
            6, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0,
            0, 0, 0, 0, 1, 0, 0, 0, 1, 7, -1, 127, 0, 0, 0, 0};

        ArrayInputStream input = new ArrayInputStream(java.nio.ByteBuffer.wrap(bytes));
        MessageReader message = org.capnproto.Serialize.read(input);
        TestAllTypes.Reader root = message.getRoot(TestAllTypes.factory);
        assertThat(root.getBoolField(), is(true));
        assertThat(root.getInt8Field(), is((byte) 7));
        assertThat(root.getInt16Field(), is((short) 32767));
    }

    @Test
    public void testUpgradeStruct() {
        MessageBuilder builder = new MessageBuilder();
        TestAnyPointer.Builder root = builder.initRoot(TestAnyPointer.factory);

        {
            TestOldVersion.Builder oldVersion = root.getAnyPointerField().initAs(org.capnproto.test.Test.TestOldVersion.factory);
            oldVersion.setOld1(123);
            oldVersion.setOld2("foo");
            TestOldVersion.Builder sub = oldVersion.initOld3();
            sub.setOld1(456);
            sub.setOld2("bar");
        }

        {
            TestNewVersion.Reader newVersion = root.getAnyPointerField().asReader().getAs(TestNewVersion.factory);
            assertThat(newVersion.getOld1(), is(123L));
            assertThat(newVersion.getOld2().toString(), is("foo"));
            assertThat(newVersion.getNew2().toString(), is("baz"));
            assertThat(newVersion.hasNew2(), is(false));
            assertThat(newVersion.hasNew3(), is(false));
        }
    }

    @Test
    public void testUpgradeStructInBuilder() {
        MessageBuilder builder = new MessageBuilder();
        TestAnyPointer.Builder root = builder.initRoot(TestAnyPointer.factory);

        {
            TestOldVersion.Builder oldVersion = root.getAnyPointerField().initAs(TestOldVersion.factory);
            oldVersion.setOld1(123);
            oldVersion.setOld2("foo");
            TestOldVersion.Builder sub = oldVersion.initOld3();
            sub.setOld1(456);
            sub.setOld2("bar");
        }

        {
            TestNewVersion.Builder newVersion = root.getAnyPointerField().getAs(TestNewVersion.factory);
            assertThat(newVersion.getOld1(), is(123L));
            assertThat(newVersion.getOld2().toString(), is("foo"));
            assertThat(newVersion.getNew1(), is(987L));
            assertThat(newVersion.getNew2().toString(), is("baz"));
            TestNewVersion.Builder sub = newVersion.getOld3();
            assertThat(sub.getOld1(), is(456L));
            assertThat(sub.getOld2().toString(), is("bar"));

            newVersion.setOld1(234);
            newVersion.setOld2("qux");
            newVersion.setNew1(654);
            newVersion.setNew2("quux");

        }

        {
            TestOldVersion.Builder oldVersion = root.getAnyPointerField().getAs(TestOldVersion.factory);
            assertThat(oldVersion.getOld1(), is(234L));
            assertThat(oldVersion.getOld2().toString(), is("qux"));
        }
    }

    @Test
    public void testStructListUpgrade() {
        MessageBuilder message = new MessageBuilder();
        TestAnyPointer.Builder root = message.initRoot(TestAnyPointer.factory);
        AnyPointer.Builder any = root.getAnyPointerField();

        {
            // set primitive list builder
            PrimitiveList.Long.Builder longs = any.initAs(PrimitiveList.Long.factory, 3);
            longs.set(0, 123);
            longs.set(1, 456);
            longs.set(2, 789);
        }

        {
            // get builder as reader and verify same content
            StructList.Reader<TestOldVersion.Reader> olds = any.asReader().getAs(TestOldVersion.listFactory);
            assertThat(olds.get(0).getOld1(), is(123L));
            assertThat(olds.get(1).getOld1(), is(456L));
            assertThat(olds.get(2).getOld1(), is(789L));
        }

        {
            // get builder of new version and modify again
            StructList.Builder<TestOldVersion.Builder> olds = any.getAs(TestOldVersion.listFactory);
            assertThat(olds.size(), is(3));
            assertThat(olds.get(0).getOld1(), is(123L));
            assertThat(olds.get(1).getOld1(), is(456L));
            assertThat(olds.get(2).getOld1(), is(789L));

            olds.get(0).setOld2("zero");
            olds.get(1).setOld2("one");
            olds.get(2).setOld2("two");
        }

        {
            StructList.Builder<TestNewVersion.Builder> news = any.getAs(TestNewVersion.listFactory);
            assertThat(news.size(), is(3));
            assertThat(news.get(0).getOld1(), is(123L));
            assertThat(news.get(0).getOld2().toString(), is("zero"));

            assertThat(news.get(1).getOld1(), is(456L));
            assertThat(news.get(1).getOld2().toString(), is("one"));

            assertThat(news.get(2).getOld1(), is(789L));
            assertThat(news.get(2).getOld2().toString(), is("two"));
        }
    }

    @Test
    public void testStructListUpgradeDoubleFar() {
        byte[] bytes = new byte[]{
            1, 0, 0, 0, 0x1f, 0, 0, 0, // list, inline composite, 3 words
            4, 0, 0, 0, 1, 0, 2, 0, // struct tag. 1 element, 1 word data, 2 pointers.
            91, 0, 0, 0, 0, 0, 0, 0, // data: 91
            0x05, 0, 0, 0, 0x42, 0, 0, 0, // list pointer, offset 1, type = BYTE, length 8.
            0, 0, 0, 0, 0, 0, 0, 0, // null pointer
            (byte) 0x68, (byte) 0x65, (byte) 0x6c, (byte) 0x6c, (byte) 0x6f, (byte) 0x21, (byte) 0x21, 0}; // "hello!!"
        ByteBufferDataView segment = ByteBufferDataView.wrap(bytes);
        segment.order(java.nio.ByteOrder.LITTLE_ENDIAN);
        MessageReader messageReader = new MessageReader(new DataView[]{segment}, ReaderOptions.DEFAULT_READER_OPTIONS);

        StructList.Factory<TestOldVersion.Builder, TestOldVersion.Reader> oldFactory = new StructList.Factory<>(TestOldVersion.factory);
        StructList.Reader<TestOldVersion.Reader> oldVersion = messageReader.getRoot(oldFactory);

        assertThat(oldVersion.size(), is(1));
        assertThat(oldVersion.get(0).getOld1(), is(91L));
        assertThat(oldVersion.get(0).getOld2().toString(), is("hello!!")); // Make the first segment exactly large enough to fit the original message.
        // This leaves no room for a far pointer landing pad in the first segment.
        MessageBuilder message = new MessageBuilder(6);
        message.setRoot(oldFactory, oldVersion);

        DataView[] segments = message.getArena().getSegmentsForOutput();
        assertThat(segments.length, is(1));
        assertThat(segments[0].limit(), is(6 * 8));

        StructList.Builder<TestNewVersion.Builder> newVersion = message.getRoot(new StructList.Factory<>(TestNewVersion.factory));
        assertThat(newVersion.size(), is(1));
        assertThat(newVersion.get(0).getOld1(), is(91L));
        assertThat(newVersion.get(0).getOld2().toString(), is("hello!!"));

        DataView[] segments1 = message.getArena().getSegmentsForOutput();
        assertThat(segments[0].limit(), is(6 * 8));
        for (int ii = 8; ii < (5 * 8) - 1; ii++) {
            // Check the the old list, including the tag, was zeroed.
            assertThat(segments[0].get(ii), is((byte) 0));
        }
    }

    @Test
    public void testListBuilderAsReader() {
        MessageBuilder message = new MessageBuilder();
        TestAllTypes.Builder allTypes = message.initRoot(TestAllTypes.factory);

        allTypes.initVoidList(10);
        assertThat(allTypes.getVoidList().asReader().size(), is(10));

        PrimitiveList.Boolean.Builder boolList = allTypes.initBoolList(7);
        boolList.set(3, true);
        PrimitiveList.Boolean.Reader boolListReader = boolList.asReader();
        assertThat(boolListReader.size(), is(7));
        assertThat(boolListReader.get(0), is(false));
        assertThat(boolListReader.get(1), is(false));
        assertThat(boolListReader.get(2), is(false));
        assertThat(boolListReader.get(3), is(true));
        assertThat(boolListReader.get(4), is(false));

        PrimitiveList.Byte.Builder int8List = allTypes.initInt8List(9);
        int8List.set(4, (byte) 100);
        int8List.set(8, (byte) 11);
        PrimitiveList.Byte.Reader int8ListReader = int8List.asReader();
        assertThat(int8ListReader.size(), is(9));
        assertThat(int8ListReader.get(0), is((byte) 0));
        assertThat(int8ListReader.get(4), is((byte) 100));
        assertThat(int8ListReader.get(8), is((byte) 11));

        PrimitiveList.Short.Builder int16List = allTypes.initInt16List(2);
        int16List.set(0, (short) 1);
        PrimitiveList.Short.Reader int16ListReader = int16List.asReader();
        assertThat(int16ListReader.size(), is(2));
        assertThat(int16ListReader.get(0), is((short) 1));
        assertThat(int16ListReader.get(1), is((short) 0)); // TODO other primitive lists

        TextList.Builder textList = allTypes.initTextList(1);
        textList.set(0, new Text.Reader("abcdefg"));
        TextList.Reader textListReader = textList.asReader();
        assertThat(textListReader.size(), is(1));
        assertThat(textListReader.get(0).toString(), is("abcdefg"));

        DataList.Builder dataList = allTypes.initDataList(1);
        dataList.set(0, new Data.Reader(new byte[]{1, 2, 3, 4}));
        DataList.Reader dataListReader = dataList.asReader();
        assertThat(dataListReader.size(), is(1));
        assertThat(dataListReader.get(0).toArray(), is(new byte[]{1, 2, 3, 4}));

        StructList.Builder<TestAllTypes.Builder> structList = allTypes.initStructList(2);
        structList.get(0).setInt8Field((byte) 5);
        structList.get(1).setInt8Field((byte) 9);
        StructList.Reader<TestAllTypes.Reader> structListReader = structList.asReader(TestAllTypes.factory);
        assertThat(structListReader.size(), is(2));
        assertThat(structListReader.get(0).getInt8Field(), is((byte) 5));
        assertThat(structListReader.get(1).getInt8Field(), is((byte) 9));

        EnumList.Builder<TestEnum> enumList = allTypes.initEnumList(3);
        enumList.set(0, TestEnum.FOO);
        enumList.set(1, TestEnum.BAR);
        enumList.set(2, TestEnum.BAZ);
        EnumList.Reader<TestEnum> enumListReader = enumList.asReader();
        assertThat(enumListReader.size(), is(3));
        assertThat(enumListReader.get(0), is(TestEnum.FOO));
        assertThat(enumListReader.get(1), is(TestEnum.BAR));
        assertThat(enumListReader.get(2), is(TestEnum.BAZ));
    }

    @Test
    public void testNestedListBuilderAsReader() {
        MessageBuilder builder = new MessageBuilder();
        TestLists.Builder root = builder.initRoot(TestLists.factory);

        ListList.Builder<StructList.Builder<TestAllTypes.Builder>> structListList = root.initStructListList(3);
        StructList.Builder<TestAllTypes.Builder> structList0 = structListList.init(0, 1);
        structList0.get(0).setInt16Field((short) 1); // leave structList1 as default
        StructList.Builder<TestAllTypes.Builder> structList2 = structListList.init(2, 3);
        structList2.get(0).setInt16Field((short) 22);
        structList2.get(1).setInt16Field((short) 333);
        structList2.get(2).setInt16Field((short) 4444);

        ListList.Reader<StructList.Reader<TestAllTypes.Reader>> structListListReader = structListList.asReader(new StructList.Factory<>(TestAllTypes.factory));
        assertThat(structListListReader.size(), is(3));
        StructList.Reader<TestAllTypes.Reader> structList0Reader = structListListReader.get(0);
        assertThat(structList0Reader.size(), is(1));
        assertThat(structList0Reader.get(0).getInt16Field(), is((short) 1));
        assertThat(structListListReader.get(1).size(), is(0));
        StructList.Reader<TestAllTypes.Reader> structList2Reader = structListListReader.get(2);
        assertThat(structList2Reader.size(), is(3));
        assertThat(structList2Reader.get(0).getInt16Field(), is((short) 22));
        assertThat(structList2Reader.get(1).getInt16Field(), is((short) 333));
        assertThat(structList2Reader.get(2).getInt16Field(), is((short) 4444));
    }

    @Test
    public void testGenerics() {
        MessageBuilder message = new MessageBuilder();
        TestGenerics.Factory<TestAllTypes.Builder, TestAllTypes.Reader, Text.Builder, Text.Reader> factory = TestGenerics.newFactory(TestAllTypes.factory, Text.factory);
        TestGenerics.Builder<TestAllTypes.Builder, Text.Builder> root = message.initRoot(factory);
        TestUtilJava.initTestMessage(root.getFoo());
        root.getDub().setFoo(Text.factory, new Text.Reader("Hello"));
        PrimitiveList.Byte.Builder bar = root.getDub().initBar(1);
        bar.set(0, (byte) 11);
        TestAllTypes.Builder revBar = root.getRev().getBar();
        revBar.setInt8Field((byte) 111);
        PrimitiveList.Boolean.Builder boolList = revBar.initBoolList(2);
        boolList.set(0, false);
        boolList.set(1, true);

        TestUtilJava.checkTestMessage(root.getFoo());
        TestGenerics.Reader<TestAllTypes.Reader, Text.Reader> rootReader = root.asReader(factory);
        TestUtilJava.checkTestMessage(rootReader.getFoo());
        TestGenerics.Builder<Text.Builder, PrimitiveList.Byte.Builder> dubReader = root.getDub();
        assertThat(dubReader.getFoo().toString(), is("Hello"));
        PrimitiveList.Byte.Builder barReader = dubReader.getBar();
        assertThat(barReader.size(), is(1));
        assertThat(barReader.get(0), is((byte) 11));
    }

    @Test
    public void testUseGenerics() {
        MessageBuilder message = new MessageBuilder();
        TestUseGenerics.Builder root = message.initRoot(TestUseGenerics.factory);

        {
            MessageBuilder message2 = new MessageBuilder();
            TestGenerics.Factory<AnyPointer.Builder, AnyPointer.Reader, AnyPointer.Builder, AnyPointer.Reader> factory2 = TestGenerics.newFactory(AnyPointer.factory, AnyPointer.factory);
            TestGenerics.Builder<AnyPointer.Builder, AnyPointer.Builder> root2 = message2.initRoot(factory2);
            TestGenerics.Builder dub2 = root2.initDub().setFoo(Text.factory, new Text.Reader("foobar"));
            final TestGenerics.Reader<AnyPointer.Reader, AnyPointer.Reader> asReader = root2.asReader(factory2);

            root.setUnspecified(factory2, asReader);
        }

        TestUseGenerics.Reader rootReader = root.asReader();
        assertThat(root.getUnspecified().getDub().getFoo().toString(), is("foobar"));
    }

    @Test
    public void testDefaults() {
        MessageBuilder message = new MessageBuilder();
        TestDefaults.Builder defaults = message.initRoot(TestDefaults.factory);
        TestUtilJava.checkDefaultMessage(defaults);
        TestUtilJava.checkDefaultMessage(defaults.asReader());
        TestUtilJava.setDefaultMessage(defaults);
        TestUtilJava.checkSettedDefaultMessage(defaults.asReader());
    }

    @Test
    public void testUnions() {
        MessageBuilder builder = new MessageBuilder();
        TestUnion.Builder root = builder.initRoot(TestUnion.factory);
        TestUnion.Union0.Builder u0 = root.initUnion0();
        u0.initU0f1sp(10);
        assertThat((u0.which()), is(TestUnion.Union0.Which.U0F1SP));;

        u0.initPrimitiveList(10);
        assertThat((u0.which()), is(TestUnion.Union0.Which.PRIMITIVE_LIST));;
    }

    @Test
    public void testGroups() {
        MessageBuilder builder = new MessageBuilder();
        TestGroups.Builder root = builder.initRoot(TestGroups.factory);

        {
            TestGroups.Groups.Foo.Builder foo = root.getGroups().initFoo();
            foo.setCorge(12345678);
            foo.setGrault(123456789012345L);
            foo.setGarply(new Text.Reader("foobar"));

            assertThat((12345678), is(foo.getCorge()));
            assertThat((123456789012345L), is(foo.getGrault()));
            assertThat(("foobar"), is(foo.getGarply().toString()));
        }

        {
            TestGroups.Groups.Bar.Builder bar = root.getGroups().initBar();
            bar.setCorge(23456789);
            bar.setGrault(new Text.Reader("barbaz"));
            bar.setGarply(234567890123456L);

            assertThat((23456789), is(bar.getCorge()));
            assertThat(("barbaz"), is(bar.getGrault().toString()));
            assertThat((234567890123456L), is(bar.getGarply()));
        }

        {
            TestGroups.Groups.Baz.Builder baz = root.getGroups().initBaz();
            baz.setCorge(34567890);
            baz.setGrault(new Text.Reader("bazqux"));
            baz.setGarply(new Text.Reader("quxquux"));

            assertThat((34567890), is(baz.getCorge()));
            assertThat(("bazqux"), is(baz.getGrault().toString()));
            assertThat(("quxquux"), is(baz.getGarply().toString()));
        }
    }

    @Test
    public void testNestedLists() {
        MessageBuilder builder = new MessageBuilder();
        TestLists.Builder root = builder.initRoot(TestLists.factory);

        {
            ListList.Builder<PrimitiveList.Int.Builder> intListList = root.initInt32ListList(2);
            PrimitiveList.Int.Builder intList0 = intListList.init(0, 4);
            intList0.set(0, 1);
            intList0.set(1, 2);
            intList0.set(2, 3);
            intList0.set(3, 4);
            PrimitiveList.Int.Builder intList1 = intListList.init(1, 1);
            intList1.set(0, 100);
        }

        {
            TestLists.Reader reader = root.asReader(); // why?
            ListList.Builder<PrimitiveList.Int.Builder> intListList = root.getInt32ListList();
            assertThat(intListList.size(), is(2));
            PrimitiveList.Int.Builder intList0 = intListList.get(0);
            assertThat(intList0.size(), is(4));
            assertThat(intList0.get(0), is(1));
            assertThat(intList0.get(1), is(2));
            assertThat(intList0.get(2), is(3));
            assertThat(intList0.get(3), is(4));
            PrimitiveList.Int.Builder intList1 = intListList.get(1);
            assertThat(intList1.size(), is(1));
            assertThat(intList1.get(0), is(100));
        }
    }

    @Test
    public void testConstants() {
        assertThat((Void.VOID), is(TestConstants.VOID_CONST));
        assertThat((true), is(TestConstants.BOOL_CONST));
        assertThat(((byte) -123), is(TestConstants.INT8_CONST));
        assertThat(((short) -12345), is(TestConstants.INT16_CONST));
        assertThat((-12345678), is(TestConstants.INT32_CONST));
        assertThat((-123456789012345L), is(TestConstants.INT64_CONST));

        assertThat(((byte) -22), is(TestConstants.UINT8_CONST));
        assertThat(((short) -19858), is(TestConstants.UINT16_CONST));
        assertThat((-838178284), is(TestConstants.UINT32_CONST));
        assertThat((-6101065172474983726L), is(TestConstants.UINT64_CONST));

        assertThat((1234.5f), is(TestConstants.FLOAT32_CONST));
        assertThat((-123e45), is(TestConstants.FLOAT64_CONST));

        assertThat((TestConstants.TEXT_CONST.toString()), is("foo"));
        assertThat((TestConstants.DATA_CONST.toArray()), is(TestUtilJava.data("bar")));

        assertThat((TestConstants.ENUM_CONST), is(TestEnum.CORGE
        ));

        {
            TestAllTypes.Reader subReader = TestConstants.STRUCT_CONST;
            assertThat(subReader.getBoolField(), is(true));
            assertThat(subReader.getInt8Field(), is((byte) -12));
            assertThat(subReader.getInt16Field(), is((short) 3456));
            assertThat(subReader.getInt32Field(), is(-78901234));
            assertThat(subReader.getInt64Field(), is(56789012345678L));
            assertThat(subReader.getUInt8Field(), is((byte) 90));
            assertThat(subReader.getUInt16Field(), is((short) 1234));
            assertThat(subReader.getUInt32Field(), is(56789012));
            assertThat(subReader.getUInt64Field(), is(345678901234567890L));
            assertThat(subReader.getFloat32Field(), is(-1.25e-10f));
            assertThat(subReader.getFloat64Field(), is(345.0));
            assertThat(subReader.getTextField().toString(), is("baz")); // ...
        }

        assertThat(TestConstants.VOID_LIST_CONST.size(), is(6));

        {
            PrimitiveList.Boolean.Reader listReader = TestConstants.BOOL_LIST_CONST;
            assertThat(listReader.size(), is(4));
            assertThat(listReader.get(0), is(true));
            assertThat(listReader.get(1), is(false));
            assertThat(listReader.get(2), is(false));
            assertThat(listReader.get(3), is(true));
        }

        // ...
        {
            TextList.Reader listReader = TestConstants.TEXT_LIST_CONST;
            assertThat(listReader.size(), is(3));
            assertThat(listReader.get(0).toString(), is("plugh"));
            assertThat(listReader.get(1).toString(), is("xyzzy"));
            assertThat(listReader.get(2).toString(), is("thud"));
        }

        {
            StructList.Reader<TestAllTypes.Reader> listReader = TestConstants.STRUCT_LIST_CONST;
            assertThat(listReader.size(), is(3));
            assertThat(listReader.get(0).getTextField().toString(), is("structlist 1"));
            assertThat(listReader.get(1).getTextField().toString(), is("structlist 2"));
            assertThat(listReader.get(2).getTextField().toString(), is("structlist 3"));
        }

    }

    @Test
    public void testGlobalConstants() {
        assertThat((12345), is(GLOBAL_INT));
    }

    public void testEmptyStruct() {
        MessageBuilder builder = new MessageBuilder();

        TestAnyPointer.Builder root = builder.initRoot(TestAnyPointer.factory);
        assertThat(root.hasAnyPointerField(), is(false));
        AnyPointer.Builder any = root.getAnyPointerField();
        assertThat(any.isNull(), is(true));
        any.initAs(TestEmptyStruct.factory);
        assertThat(any.isNull(), is(false));
        assertThat(root.hasAnyPointerField(), is(true));

        {
            TestAnyPointer.Reader rootReader = root.asReader();
            assertThat(rootReader.hasAnyPointerField(), is(true));
            assertThat(rootReader.getAnyPointerField().isNull(), is(false));
        }
    }

    @Test
    public void testTextBuilderIntUnderflow() {
        MessageBuilder message = new MessageBuilder();
        TestAnyPointer.Builder root = message.initRoot(TestAnyPointer.factory);
        root.getAnyPointerField().initAs(org.capnproto.Data.factory, 0);
        try {
            root.getAnyPointerField().getAs(org.capnproto.Text.factory);
            fail("Decode Exception expected");
        } catch (DecodeException e) {
        }
    }

    @Test
    public void testInlineCompositeListIntOverflow() {
        byte[] bytes = new byte[]{0, 0, 0, 0, 0, 0, 1, 0,
            1, 0, 0, 0, 0x17, 0, 0, 0, 0, 0, 0, -128, 16, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        ByteBufferDataView segment = ByteBufferDataView.wrap(bytes);
        segment.order(java.nio.ByteOrder.LITTLE_ENDIAN);
        MessageReader message = new MessageReader(new DataView[]{segment}, ReaderOptions.DEFAULT_READER_OPTIONS);

        TestAnyPointer.Reader root = message.getRoot(TestAnyPointer.factory);
// TODO add this after we impelement totalSize():
        //root.totalSize()

        try {
            root.getAnyPointerField().getAs(new StructList.Factory(TestAllTypes.factory));
            fail("Expected a Decode Exception");
        } catch (DecodeException e) {

        }
        MessageBuilder messageBuilder = new MessageBuilder();
        TestAnyPointer.Builder builderRoot = messageBuilder.initRoot(TestAnyPointer.factory);
        try {
            builderRoot.getAnyPointerField().setAs(TestAnyPointer.factory, root);
            fail("A Decode Exception expected");
        } catch (DecodeException e) {

        }
    }

    @Test
    public void testVoidListAmplification() {
        MessageBuilder builder = new MessageBuilder();
        builder.initRoot(TestAnyPointer.factory).getAnyPointerField().initAs(PrimitiveList.Void.factory, 1 << 28);
        DataView[] segments = builder.getArena().getSegmentsForOutput();
        assertThat(segments.length, is(1));

        MessageReader reader = new MessageReader(segments, ReaderOptions.DEFAULT_READER_OPTIONS);
        TestAnyPointer.Reader root = reader.getRoot(TestAnyPointer.factory);
        try {
            root.getAnyPointerField().getAs(new StructList.Factory(TestAllTypes.factory));
            fail("Decode Exception expected");
        } catch (DecodeException e) {
        }
    }

    @Test
    public void testEmptyStructListAmplification() {
        MessageBuilder builder = new MessageBuilder();
        builder.initRoot(TestAnyPointer.factory).getAnyPointerField()
                .initAs(new StructList.Factory(TestEmptyStruct.factory), (1 << 29) - 1);
        DataView[] segments = builder.getArena().getSegmentsForOutput();
        assertThat(segments.length, is(1));
        MessageReader reader = new MessageReader(segments, ReaderOptions.DEFAULT_READER_OPTIONS);
        TestAnyPointer.Reader root = reader.getRoot(TestAnyPointer.factory);
        try {
            root.getAnyPointerField().getAs(new StructList.Factory(TestAllTypes.factory));
            fail("DecodeException expected");
        } catch (DecodeException e) {
        }
    }

    @Test
    public void testLongUint8List() {
        {
            MessageBuilder message = new MessageBuilder();
            TestAllTypes.Builder allTypes = message.initRoot(TestAllTypes.factory);
            int length = ((1 << 28) + 1);
            PrimitiveList.Byte.Builder list = allTypes.initUInt8List(length);
            assertThat(list.size(), is(length));
            list.set(length - 1, (byte) 3);
            assertThat(list.get(length - 1), is((byte) 3));
            assertThat(allTypes.asReader().getUInt8List().get(length - 1), is((byte) 3));
        }
    }

    @Test
    public void testLongUint16List() {
        {
            MessageBuilder message = new MessageBuilder();
            TestAllTypes.Builder allTypes = message.initRoot(TestAllTypes.factory);
            int length = (1 << 27) + 1;
            PrimitiveList.Short.Builder list = allTypes.initUInt16List(length);
            assertThat(list.size(), is(length));
            list.set(length - 1, (short) 3);
            assertThat(list.get(length - 1), is((short) 3));
            assertThat(allTypes.asReader().getUInt16List().get(length - 1), is((short) 3));
        }
    }

    @Test
    public void testLongUint32List() {
        {
            MessageBuilder message = new MessageBuilder();
            TestAllTypes.Builder allTypes = message.initRoot(TestAllTypes.factory);
            int length = (1 << 26) + 1;
            PrimitiveList.Int.Builder list = allTypes.initUInt32List(length);
            assertThat(list.size(), is(length));
            list.set(length - 1, 3);
            assertThat(list.get(length - 1), is(3));
            assertThat(allTypes.asReader().getUInt32List().get(length - 1), is(3));
        }
    }

    @Test
    public void testLongUint64List() {
        {
            MessageBuilder message = new MessageBuilder();
            TestAllTypes.Builder allTypes = message.initRoot(TestAllTypes.factory);
            int length = (1 << 25) + 1;
            PrimitiveList.Long.Builder list = allTypes.initUInt64List(length);
            assertThat(list.size(), is(length));
            list.set(length - 1, 3L);
            assertThat(list.get(length - 1), is(3L));
            assertThat(allTypes.asReader().getUInt64List().get(length - 1), is(3L));
        }
    }

    @Test
    public void testLongFloat32List() {
        {
            MessageBuilder message = new MessageBuilder();
            TestAllTypes.Builder allTypes = message.initRoot(TestAllTypes.factory);
            int length = (1 << 26) + 1;
            PrimitiveList.Float.Builder list = allTypes.initFloat32List(length);
            assertThat(list.size(), is(length));
            list.set(length - 1, 3.14f);
            assertThat(list.get(length - 1), is(3.14f));
            assertThat(allTypes.asReader().getFloat32List().get(length - 1), is(3.14f));
        }
    }

    @Test
    public void testLongFloat64List() {
        {
            MessageBuilder message = new MessageBuilder();
            TestAllTypes.Builder allTypes = message.initRoot(TestAllTypes.factory);
            int length = (1 << 25) + 1;
            PrimitiveList.Double.Builder list = allTypes.initFloat64List(length);
            assertThat(list.size(), is(length));
            list.set(length - 1, 3.14);
            assertThat(list.get(length - 1), is(3.14));
            assertThat(allTypes.asReader().getFloat64List().get(length - 1), is(3.14));
        }
    }

    @Test
    public void testLongStructList() {
        {
            MessageBuilder message = new MessageBuilder();
            TestAllTypes.Builder allTypes = message.initRoot(TestAllTypes.factory);
            int length = (1 << 21) + 1;
            StructList.Builder<TestAllTypes.Builder> list = allTypes.initStructList(length);
            assertThat(list.size(), is(length));
            list.get(length - 1).setUInt8Field((byte) 3);
            assertThat(allTypes.asReader().getStructList().get(length - 1).getUInt8Field(), is((byte) 3));
        }
    }

    @Test
    public void testLongTextList() {
        {
            MessageBuilder message = new MessageBuilder();
            TestAllTypes.Builder allTypes = message.initRoot(TestAllTypes.factory);
            int length = (1 << 25) + 1;
            TextList.Builder list = allTypes.initTextList(length);
            assertThat(list.size(), is(length));
            list.set(length - 1, new Text.Reader("foo"));
            assertThat(allTypes.asReader().getTextList().get(length - 1).toString(), is("foo"));
        }
    }

    @Test
    public void testLongListList() {
        {
            MessageBuilder message = new MessageBuilder();
            TestLists.Builder root = message.initRoot(TestLists.factory);
            int length = (1 << 25) + 1;
            ListList.Builder<StructList.Builder<TestAllTypes.Builder>> list = root.initStructListList(length);
            assertThat(list.size(), is(length));
            list.init(length - 1, 3);
            assertThat(list.get(length - 1).size(), is(3));
            assertThat(root.asReader().getStructListList().get(length - 1).size(), is(3));
        }
    }

    @Test
    public void testStructSetters() {
        MessageBuilder builder = new MessageBuilder();
        TestAllTypes.Builder root = builder.initRoot(TestAllTypes.factory);
        TestUtilJava.initTestMessage(root);

        {
            MessageBuilder builder2 = new MessageBuilder();
            builder2.setRoot(TestAllTypes.factory, root.asReader());
            TestUtilJava.checkTestMessage(builder2.getRoot(TestAllTypes.factory));
        }

        {
            MessageBuilder builder2 = new MessageBuilder();
            TestAllTypes.Builder root2 = builder2.getRoot(TestAllTypes.factory);
            root2.setStructField(root.asReader());
            TestUtilJava.checkTestMessage(root2.getStructField());
        }

        {
            MessageBuilder builder2 = new MessageBuilder();
            TestAnyPointer.Builder root2 = builder2.getRoot(TestAnyPointer.factory);
            root2.getAnyPointerField().setAs(TestAllTypes.factory, root.asReader());
            TestUtilJava.checkTestMessage(root2.getAnyPointerField().getAs(TestAllTypes.factory));
        }
    }

    @Test
    public void testImport() {
        MessageBuilder builder = new MessageBuilder();
        TestImport.Foo.Builder root = builder.initRoot(org.capnproto.test.TestImport.Foo.factory);
        TestAllTypes.Builder field = root.initImportedStruct();
        TestUtilJava.initTestMessage(field);
        TestUtilJava.checkTestMessage(field);
        TestUtilJava.checkTestMessage(field.asReader());
    }

    @Test
    public void testGenericMap() {
        MessageBuilder builder = new MessageBuilder();
        GenericMap.Factory<Text.Builder, Text.Reader, TestAllTypes.Builder, TestAllTypes.Reader> mapFactory = new GenericMap.Factory<>(Text.factory, TestAllTypes.factory);
        StructList.Factory<GenericMap.Entry.Builder<Text.Builder, TestAllTypes.Builder>, GenericMap.Entry.Reader<Text.Reader, TestAllTypes.Reader>> entryFactory = new StructList.Factory<>(new GenericMap.Entry.Factory<>(Text.factory, TestAllTypes.factory));
        GenericMap.Builder<Text.Builder, TestAllTypes.Builder> root = builder.initRoot(mapFactory);

        {
            StructList.Builder<GenericMap.Entry.Builder<Text.Builder, TestAllTypes.Builder>> entries = root.initEntries(entryFactory, 3);

            GenericMap.Entry.Builder<Text.Builder, TestAllTypes.Builder> entry0 = entries.get(0);
            entry0.setKey(Text.factory, new Text.Reader("foo"));
            TestAllTypes.Builder ue0 = entry0.initValue();
            ue0.setInt64Field(101);

            GenericMap.Entry.Builder<Text.Builder, TestAllTypes.Builder> entry1 = entries.get(1);
            entry1.setKey(Text.factory, new Text.Reader("bar"));
            TestAllTypes.Builder ue1 = entry1.initValue();
            ue1.setInt64Field(202);

            GenericMap.Entry.Builder<Text.Builder, TestAllTypes.Builder> entry2 = entries.get(2);
            entry2.setKey(Text.factory, new Text.Reader("baz"));
            TestAllTypes.Builder ue2 = entry2.initValue();
            ue2.setInt64Field(303);
        }

        {
            StructList.Reader<GenericMap.Entry.Reader<Text.Reader, TestAllTypes.Reader>> entries = root.asReader(mapFactory).getEntries(entryFactory);
            GenericMap.Entry.Reader<Text.Reader, TestAllTypes.Reader> entry0 = entries.get(0);
            assertThat((entry0.getKey().toString()), is("foo"));
            assertThat((entry0.getValue().getInt64Field()), is(101L));

            GenericMap.Entry.Reader<Text.Reader, TestAllTypes.Reader> entry1 = entries.get(1);
            assertThat((entry1.getKey().toString()), is("bar"));
            assertThat((entry1.getValue().getInt64Field()), is(202L));

            GenericMap.Entry.Reader<Text.Reader, TestAllTypes.Reader> entry2 = entries.get(2);
            assertThat((entry2.getKey().toString()), is("baz"));
            assertThat((entry2.getValue().getInt64Field()), is(303L));
        }
    }

    @Test
    public void testsetWithCaveats() {
        MessageBuilder builder = new MessageBuilder();
        TestAllTypes.Builder root = builder.initRoot(TestAllTypes.factory);
        StructList.Builder<TestAllTypes.Builder> list = root.initStructList(2);

        {
            MessageBuilder message1 = new MessageBuilder();
            TestAllTypes.Builder root1 = message1.initRoot(TestAllTypes.factory);
            root1.setInt8Field((byte) 11);
            list.setWithCaveats(TestAllTypes.factory, 0, root1.asReader());
        }

        {
            MessageBuilder message2 = new MessageBuilder();
            TestAllTypes.Builder root2 = message2.initRoot(TestAllTypes.factory);
            TestUtilJava.initTestMessage(root2);
            list.setWithCaveats(TestAllTypes.factory, 1, root2.asReader());
        }

        StructList.Reader<TestAllTypes.Reader> listReader = list.asReader(TestAllTypes.factory);
        assertThat(listReader.get(0).getInt8Field(), is((byte) 11));
        TestUtilJava.checkTestMessage(listReader.get(1));
    }
}
