package org.capnproto;

/**
 * Changes the defaults of the ByteFormatter that are used in toString()
 */
class ByteBufferFormatterDefaults {

    private static boolean printText = true;
    private static boolean printHex = true;
    private static boolean isShowSize = true;
    private static int lead = 10;
    private static int tail = 6;

    public static void setPrintText(boolean printText) {
        ByteBufferFormatterDefaults.printText = printText;
    }

    public static void setPrintHex(boolean printHex) {
        ByteBufferFormatterDefaults.printHex = printHex;
    }

    public static void setIsShowSize(boolean isShowSize) {
        ByteBufferFormatterDefaults.isShowSize = isShowSize;
    }

    public static void setLead(int lead) {
        ByteBufferFormatterDefaults.lead = lead;
    }

    public static void setTail(int tail) {
        ByteBufferFormatterDefaults.tail = tail;
    }

    static boolean isPrintText() {
        return printText;
    }

    static boolean isPrintHex() {
        return printHex;
    }

    static boolean isShowSize() {
        return isShowSize;
    }

    static int getLead() {
        return lead;
    }

    static int getTail() {
        return tail;
    }

}
