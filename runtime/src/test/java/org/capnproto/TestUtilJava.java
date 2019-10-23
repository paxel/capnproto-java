package org.capnproto;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.capnproto.test.Test;
import org.capnproto.test.Test.TestDefaults;
import org.capnproto.test.Test.TestEnum;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestUtilJava {

    static void initTestMessage(Test.TestAllTypes.Builder builder) {
        builder.setVoidField(org.capnproto.Void.VOID);
        builder.setBoolField(true);
        builder.setInt8Field((byte) -123);
        builder.setInt16Field((short) -12345);
        builder.setInt32Field(-12345678);
        builder.setInt64Field(-123456789012345L);
        builder.setUInt8Field((byte) 0xea);
        builder.setUInt16Field((short) 0x4567);
        builder.setUInt32Field(0x34567890);
        builder.setUInt64Field(0x1234567890123456L);
        builder.setFloat32Field(1234.5f);
        builder.setFloat64Field(-123e45);
        builder.setTextField("foo");
        builder.setDataField(new Data.Reader("bar".getBytes()));

        {
            Test.TestAllTypes.Builder subBuilder = builder.initStructField();
            subBuilder.setVoidField(org.capnproto.Void.VOID);
            subBuilder.setBoolField(true);
            subBuilder.setInt8Field((byte) -12);
            subBuilder.setInt16Field((short) 3456);
            subBuilder.setInt32Field(-78901234);
            subBuilder.setInt64Field(56789012345678L);
            subBuilder.setUInt8Field((byte) 90);
            subBuilder.setUInt16Field((short) 1234);
            subBuilder.setUInt32Field(56789012);
            subBuilder.setUInt64Field(345678901234567890L);
            subBuilder.setFloat32Field(-1.25e-10f);
            subBuilder.setFloat64Field(345);
            subBuilder.setTextField(new Text.Reader("baz"));
            subBuilder.setDataField(new Data.Reader("qux".getBytes()));

            {
                Test.TestAllTypes.Builder subSubBuilder = subBuilder.initStructField();
                subSubBuilder.setTextField(new Text.Reader("nested"));
                subSubBuilder.initStructField().setTextField(new Text.Reader("really nested"));
            }

            subBuilder.setEnumField(Test.TestEnum.BAZ);

            PrimitiveList.Boolean.Builder boolList = subBuilder.initBoolList(5);
            boolList.set(0, false);
            boolList.set(1, true);
            boolList.set(2, false);
            boolList.set(3, true);
            boolList.set(4, true);
        }

        builder.setEnumField(TestEnum.CORGE);
        builder.initVoidList(6);

        PrimitiveList.Boolean.Builder boolList = builder.initBoolList(4);
        boolList.set(0, true);
        boolList.set(1, false);
        boolList.set(2, false);
        boolList.set(3, true);

        PrimitiveList.Double.Builder float64List = builder.initFloat64List(4);
        float64List.set(0, 7777.75);
        float64List.set(1, Double.POSITIVE_INFINITY);
        float64List.set(2, Double.NEGATIVE_INFINITY);
        float64List.set(3, Double.NaN);

        TextList.Builder textList = builder.initTextList(3);
        textList.set(0, new Text.Reader("plugh"));
        textList.set(1, new Text.Reader("xyzzy"));
        textList.set(2, new Text.Reader("thud"));

        StructList.Builder<Test.TestAllTypes.Builder> structList = builder.initStructList(3);
        structList.get(0).setTextField(new Text.Reader("structlist 1"));
        structList.get(1).setTextField(new Text.Reader("structlist 2"));
        structList.get(2).setTextField(new Text.Reader("structlist 3"));

        EnumList.Builder<TestEnum> enumList = builder.initEnumList(2);
        enumList.set(0, TestEnum.FOO);
        enumList.set(1, TestEnum.GARPLY);
    }

    static void checkTestMessage(Test.TestAllTypes.Builder builder) {
        builder.getVoidField();
        assertThat(builder.getBoolField(), is(true));
        assertThat(builder.getInt8Field(), is((byte) -123));
        assertThat(builder.getInt16Field(), is((short) -12345));
        assertThat(builder.getInt32Field(), is(-12345678));
        assertThat(builder.getInt64Field(), is(-123456789012345L));
        assertThat(builder.getUInt8Field(), is((byte) 0xea));
        assertThat(builder.getUInt16Field(), is((short) 0x4567));
        assertThat(builder.getUInt32Field(), is(0x34567890));
        assertThat(builder.getUInt64Field(), is(0x1234567890123456L));
        assertThat(builder.getFloat32Field(), is(1234.5f));
        assertThat(builder.getFloat64Field(), is(-123e45));
        assertThat(builder.getTextField().toString(), is("foo"));

        {
            Test.TestAllTypes.Builder subBuilder = builder.getStructField();
            subBuilder.getVoidField();
            assertThat(subBuilder.getBoolField(), is(true));
            assertThat(subBuilder.getInt8Field(), is((byte) -12));
            assertThat(subBuilder.getInt16Field(), is((short) 3456));
            assertThat(subBuilder.getInt32Field(), is(-78901234));
            assertThat(subBuilder.getInt64Field(), is(56789012345678L));
            assertThat(subBuilder.getUInt8Field(), is((byte) 90));
            assertThat(subBuilder.getUInt16Field(), is((short) 1234));
            assertThat(subBuilder.getUInt32Field(), is(56789012));
            assertThat(subBuilder.getUInt64Field(), is(345678901234567890L));
            assertThat(subBuilder.getFloat32Field(), is(-1.25e-10f));
            assertThat(subBuilder.getFloat64Field(), is(345.0));

            {
                Test.TestAllTypes.Builder subSubBuilder = subBuilder.getStructField();
                assertThat(subSubBuilder.getTextField().toString(), is("nested"));
            }

            assertThat(subBuilder.getEnumField(), is(TestEnum.BAZ));

            PrimitiveList.Boolean.Builder boolList = subBuilder.getBoolList();
            assertThat(boolList.get(0), is(false));
            assertThat(boolList.get(1), is(true));
            assertThat(boolList.get(2), is(false));
            assertThat(boolList.get(3), is(true));
            assertThat(boolList.get(4), is(true));

        }
        assertThat(builder.getEnumField(), is(TestEnum.CORGE));

        assertThat(builder.getVoidList().size(), is(6));

        PrimitiveList.Boolean.Builder boolList = builder.getBoolList();
        assertThat(boolList.size(), is(4));
        assertThat(boolList.get(0), is(true));
        assertThat(boolList.get(1), is(false));
        assertThat(boolList.get(2), is(false));
        assertThat(boolList.get(3), is(true));

        PrimitiveList.Double.Builder float64List = builder.getFloat64List();
        assertThat(float64List.get(0), is(7777.75));
        assertThat(float64List.get(1), is(Double.POSITIVE_INFINITY));
        assertThat(float64List.get(2), is(Double.NEGATIVE_INFINITY));
        assertThat(float64List.get(3), is(Double.NaN)); // NaN

        TextList.Builder textList = builder.getTextList();
        assertThat(textList.size(), is(3));
        assertThat(textList.get(0).toString(), is("plugh"));
        assertThat(textList.get(1).toString(), is("xyzzy"));
        assertThat(textList.get(2).toString(), is("thud"));

        StructList.Builder<Test.TestAllTypes.Builder> structList = builder.getStructList();
        assertThat(3, is(structList.size()));
        assertThat(structList.get(0).getTextField().toString(), is("structlist 1"));
        assertThat(structList.get(1).getTextField().toString(), is("structlist 2"));
        assertThat(structList.get(2).getTextField().toString(), is("structlist 3"));

        EnumList.Builder<TestEnum> enumList = builder.getEnumList();
        assertThat(enumList.get(0), is((TestEnum.FOO)));
        assertThat(enumList.get(1), is((TestEnum.GARPLY)));
    }

    static void checkTestMessage(Test.TestAllTypes.Reader reader) {
        reader.getVoidField();
        assertThat(reader.getBoolField(), is(true));
        assertThat(reader.getInt8Field(), is((byte) -123));
        assertThat(reader.getInt16Field(), is((short) -12345));
        assertThat(reader.getInt32Field(), is(-12345678));
        assertThat(reader.getInt64Field(), is(-123456789012345L));
        assertThat(reader.getUInt8Field(), is((byte) 0xea));
        assertThat(reader.getUInt16Field(), is((short) 0x4567));
        assertThat(reader.getUInt32Field(), is(0x34567890));
        assertThat(reader.getUInt64Field(), is(0x1234567890123456L));
        assertThat(reader.getFloat32Field(), is(1234.5f));
        assertThat(reader.getFloat64Field(), is(-123e45));
        assertThat(reader.getTextField().toString(), is("foo"));

        {
            Test.TestAllTypes.Reader subReader = reader.getStructField();
            subReader.getVoidField();
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

            {
                Test.TestAllTypes.Reader subSubReader = subReader.getStructField();
                assertThat(subSubReader.getTextField().toString(), is("nested"));
            }

            assertThat(subReader.getEnumField(), is(TestEnum.BAZ));

            PrimitiveList.Boolean.Reader boolList = subReader.getBoolList();
            assertThat(boolList.get(0), is(false));
            assertThat(boolList.get(1), is(true));
            assertThat(boolList.get(2), is(false));
            assertThat(boolList.get(3), is(true));
            assertThat(boolList.get(4), is(true));

        }
        assertThat(reader.getEnumField(), is(TestEnum.CORGE));

        assertThat(reader.getVoidList().size(), is(6));

        PrimitiveList.Boolean.Reader boolList = reader.getBoolList();
        assertThat(boolList.size(), is(4));
        assertThat(boolList.get(0), is(true));
        assertThat(boolList.get(1), is(false));
        assertThat(boolList.get(2), is(false));
        assertThat(boolList.get(3), is(true));

        PrimitiveList.Double.Reader float64List = reader.getFloat64List();
        assertThat(float64List.get(0), is(7777.75));
        assertThat(float64List.get(1), is(Double.POSITIVE_INFINITY));
        assertThat(float64List.get(2), is(Double.NEGATIVE_INFINITY));
        assertThat(float64List.get(3), is(Double.NaN)); // NaN

        TextList.Reader textList = reader.getTextList();
        assertThat(textList.size(), is(3));
        assertThat(textList.get(0).toString(), is("plugh"));
        assertThat(textList.get(1).toString(), is("xyzzy"));
        assertThat(textList.get(2).toString(), is("thud"));

        StructList.Reader<Test.TestAllTypes.Reader> structList = reader.getStructList();
        assertThat(3, is(structList.size()));
        assertThat(structList.get(0).getTextField().toString(), is("structlist 1"));
        assertThat(structList.get(1).getTextField().toString(), is("structlist 2"));
        assertThat(structList.get(2).getTextField().toString(), is("structlist 3"));

        EnumList.Reader<TestEnum> enumList = reader.getEnumList();
        assertThat(enumList.get(0), is((TestEnum.FOO)));
        assertThat(enumList.get(1), is((TestEnum.GARPLY)));
    }

    public static void checkDefaultMessage(TestDefaults.Builder builder) {
        builder.getVoidField();
        assertThat(builder.getBoolField(), is(true));
        assertThat(builder.getInt8Field(), is((byte)-123));
        assertThat(builder.getInt16Field(), is((short)-12345));
        assertThat(builder.getInt32Field(), is(-12345678));;
        assertThat(builder.getInt64Field(), is(-123456789012345L));;
        assertThat(builder.getUInt8Field(), is((byte) 0xea));;
        assertThat(builder.getUInt16Field(), is((short) 45678));;
        assertThat(builder.getUInt32Field(), is(0xce0a6a14));;
        assertThat(builder.getUInt64Field(), is(0xab54a98ceb1f0ad2L));;
        assertThat(builder.getFloat32Field(), is(1234.5f));;
        assertThat(builder.getFloat64Field(), is(-123e45));;
        assertThat(builder.getEnumField(), is(TestEnum.CORGE));;

        assertThat((builder.getTextField().toString()), is("foo"));
        assertThat((builder.getDataField().toArray()), is(new byte[]{0x62, 0x61, 0x72}));
    }

    public static void checkDefaultMessage(TestDefaults.Reader reader) {
        reader.getVoidField();
        assertThat(reader.getBoolField(), is(true));
        assertThat(reader.getInt8Field(), is((byte)-123));
        assertThat(reader.getInt16Field(), is((short)-12345));
        assertThat(reader.getInt32Field(), is(-12345678));
        assertThat(reader.getInt64Field(), is(-123456789012345L));
        assertThat(reader.getUInt8Field(), is((byte) 0xea));
        assertThat(reader.getUInt16Field(), is((short) 45678));
        assertThat(reader.getUInt32Field(), is(0xce0a6a14));
        assertThat(reader.getUInt64Field(), is(0xab54a98ceb1f0ad2L));
        assertThat(reader.getFloat32Field(), is(1234.5f));
        assertThat(reader.getFloat64Field(), is(-123e45));
        assertThat((reader.getTextField().toString()), is("foo"));
        assertThat((reader.getDataField().toArray()), is(new byte[]{0x62, 0x61, 0x72}));

        {
            Test.TestAllTypes.Reader subReader = reader.getStructField();
            subReader.getVoidField();
            assertThat((subReader.getBoolField()), is(true));
            assertThat((subReader.getInt8Field()), is((byte)-12));
            assertThat((subReader.getInt16Field()), is((short)3456));
            assertThat((subReader.getInt32Field()), is(-78901234));
            // ...
            assertThat((subReader.getTextField().toString()), is("baz"));

            {
                Test.TestAllTypes.Reader subSubReader = subReader.getStructField();
                assertThat((subSubReader.getTextField().toString()), is("nested"));
            }

        }

        assertThat((reader.getEnumField()), is(TestEnum.CORGE));

        assertThat((reader.getVoidList().size()), is(6));

        {
            PrimitiveList.Boolean.Reader listReader = reader.getBoolList();
            assertThat((listReader.size()), is(4));
            assertThat((listReader.get(0)), is(true));
            assertThat((listReader.get(1)), is(false));
            assertThat((listReader.get(2)), is(false));
            assertThat((listReader.get(3)), is(true));
        }

        {
            PrimitiveList.Byte.Reader listReader = reader.getInt8List();
            assertThat((listReader.size()), is(2));
            assertThat((listReader.get(0)), is((byte)111));
            assertThat((listReader.get(1)), is((byte)-111));
        }

    }

    public static void setDefaultMessage(TestDefaults.Builder builder) {
        builder.setBoolField(false);
        builder.setInt8Field((byte) -122);
        builder.setInt16Field((short) -12344);
        builder.setInt32Field(-12345677);
        builder.setInt64Field(-123456789012344L);
        builder.setUInt8Field((byte) 0xe9);
        builder.setUInt16Field((short) 45677);
        builder.setUInt32Field(0xce0a6a13);
        builder.setUInt64Field(0xab54a98ceb1f0ad1L);
        builder.setFloat32Field(1234.4f);
        builder.setFloat64Field(-123e44);
        builder.setTextField(new Text.Reader("bar"));
        builder.setEnumField(TestEnum.QUX);
    }

    public static void checkSettedDefaultMessage(TestDefaults.Reader reader) {
        assertThat(reader.getBoolField(), is(false));
        assertThat(reader.getInt8Field(), is((byte)-122));
        assertThat(reader.getInt16Field(), is((short)-12344));
        assertThat(reader.getInt32Field(), is(-12345677));
        assertThat(reader.getInt64Field(), is(-123456789012344L));
        assertThat(reader.getUInt8Field(), is((byte) 0xe9));
        assertThat(reader.getUInt16Field(), is((short) 45677));
        assertThat(reader.getUInt32Field(), is(0xce0a6a13));
        assertThat(reader.getUInt64Field(), is(0xab54a98ceb1f0ad1L));
        assertThat(reader.getFloat32Field(), is(1234.4f));
        assertThat(reader.getFloat64Field(), is(-123e44));
        assertThat(reader.getEnumField(), is(TestEnum.QUX));
    }

    public static byte[] data(String str) {
        try {
            return str.getBytes(ENCODING);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Could not get bytes for encoding: " + ENCODING);
        }
    }
    private static final String ENCODING = "ISO_8859-1";

}
