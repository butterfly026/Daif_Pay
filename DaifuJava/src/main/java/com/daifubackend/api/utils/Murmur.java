package com.daifubackend.api.utils;

import java.nio.ByteBuffer;

public class Murmur {

    public static int hash3_int(String key, int seed) {
        byte[] bytes = key.getBytes();
        int len = bytes.length;
        int h1 = seed < 0 ? -seed : seed;
        int remainder, i = 0;

        for (int bytesToRead = len - (remainder = len & 3); i < bytesToRead;) {
            int k1 = (bytes[i++] & 0xFF)
                    | ((bytes[i++] & 0xFF) << 8)
                    | ((bytes[i++] & 0xFF) << 16)
                    | ((bytes[i++] & 0xFF) << 24);

            k1 = mixK1(k1);
            h1 = mixH1(h1, k1);
        }

        int k1 = 0;

        switch (remainder) {
            case 3:
                k1 ^= (bytes[i + 2] & 0xFF) << 16;
            case 2:
                k1 ^= (bytes[i + 1] & 0xFF) << 8;
            case 1:
                k1 ^= bytes[i] & 0xFF;
                k1 = mixK1(k1);
                h1 ^= k1;
        }

        h1 ^= len;
        h1 ^= h1 >>> 16;
        h1 = mixFinal(h1);

        return h1;
    }

    public static String hash3(String key, int seed) {
        return Long.toString((long) hash3_int(key, seed) & 0xFFFFFFFFL, 32);
    }

    private static int mixK1(int k1) {
        k1 *= 0xcc9e2d51;
        k1 = Integer.rotateLeft(k1, 15);
        k1 *= 0x1b873593;
        return k1;
    }

    private static int mixH1(int h1, int k1) {
        h1 ^= k1;
        h1 = Integer.rotateLeft(h1, 13);
        h1 = h1 * 5 + 0xe6546b64;
        return h1;
    }

    private static int mixFinal(int h1) {
        h1 ^= h1 >>> 16;
        h1 *= 0x85ebca6b;
        h1 ^= h1 >>> 13;
        h1 *= 0xc2b2ae35;
        h1 ^= h1 >>> 16;
        return h1;
    }
}
