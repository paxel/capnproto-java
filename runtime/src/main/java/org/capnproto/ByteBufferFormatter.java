package org.capnproto;

import java.nio.ByteBuffer;

public class ByteBufferFormatter {

    private int lead = 10;
    private int tail = 6;
    private boolean showSize = true;
    private boolean hex = true;
    private boolean text = true;

    public int getLead() {
        return lead;
    }

    public ByteBufferFormatter setLead(int lead) {
        this.lead = lead;
        return this;
    }

    public int getTail() {
        return tail;
    }

    public ByteBufferFormatter setTail(int tail) {
        this.tail = tail;
        return this;
    }

    public boolean isShowSize() {
        return showSize;
    }

    public ByteBufferFormatter setShowSize(boolean showSize) {
        this.showSize = showSize;
        return this;
    }

    public boolean isHex() {
        return hex;
    }

    public ByteBufferFormatter setHex(boolean hex) {
        this.hex = hex;
        return this;
    }

    public boolean isText() {
        return text;
    }

    public ByteBufferFormatter setText(boolean text) {
        this.text = text;
        return this;
    }

    public String format(ByteBuffer buffer) {
        final ByteBuffer duplicate = buffer.duplicate();
        duplicate.rewind();
        if (hex && text) {
            return formatHex(duplicate) + "|" + formatText(duplicate);
        }
        if (hex) {
            return formatHex(duplicate);
        }
        if (text) {
            return formatText(duplicate);
        }
        if (showSize) {
            return duplicate.remaining() + " bytes";
        }
        return "";
    }

    private String formatHex(ByteBuffer duplicate) {
        StringBuilder builder = new StringBuilder();
        final int size = duplicate.remaining();
        if (showSize) {
            builder.append(size).append(" bytes ");
        }
        if (lead + tail < size) {
            for (int i = 0; i < lead; i++) {
                builder.append(String.format("%02X ", duplicate.get(i)));

            }
            builder.append("...");
            for (int i = 0; i < tail; i++) {
                builder.append(String.format("%02X ", duplicate.get(i + size - tail)));
            }
        } else {
            // show completely
            for (int i = 0; i < size; i++) {
                builder.append(String.format("%02X ", duplicate.get(i)));

            }
        }
        return builder.toString();
    }

    private String formatText(ByteBuffer duplicate) {
        StringBuilder builder = new StringBuilder();
        final int size = duplicate.remaining();
        if (lead + tail < size) {
            for (int i = 0; i < lead; i++) {
                builder.append(printableChar(duplicate.get(i)));

            }
            builder.append("...");
            for (int i = 0; i < tail; i++) {
                builder.append(printableChar(duplicate.get(i + size - tail)));
            }
        } else {
            // show completely
            for (int i = 0; i < size; i++) {
                builder.append(printableChar(duplicate.get(i)));

            }
        }
        return builder.toString();
    }

    private char printableChar(byte get) {
        if (get > 32 && get < 127) {
            return (char) get;
        }
        return '.';
    }

}
