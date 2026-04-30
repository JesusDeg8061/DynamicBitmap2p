package com.dynamicbitmap.core;

import java.util.BitSet;

public class DynamicBitmap {

    private BitSet bits;
    private int size;

    public DynamicBitmap(int size) {
        this.size = size;
        this.bits = new BitSet(size);
    }

    public void set(int index) {
        bits.set(index);
    }

    public boolean get(int index) {
        return bits.get(index);
    }

    public int count() {
        return bits.cardinality();
    }
    
    public int getSize() {
    return size;
}

    public DynamicBitmap and(DynamicBitmap other) {
        BitSet result = (BitSet) bits.clone();
        result.and(other.bits);
        return new DynamicBitmap(result, size);
    }

    private DynamicBitmap(BitSet bits, int size) {
        this.bits = bits;
        this.size = size;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(bits.get(i) ? "1" : "0");
        }
        return sb.toString();
    }
    public void clear(int index) {
    bits.clear(index);
}
}