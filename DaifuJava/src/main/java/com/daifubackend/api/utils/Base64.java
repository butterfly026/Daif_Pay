/**********************************************************\
|                                                          |
| Base64.java                                              |
|                                                          |
| Base64 library for Java.                                 |
|                                                          |
| Code Authors: Ma Bingyao <mabingyao@gmail.com>           |
| LastModified: Mar 10, 2015                               |
|                                                          |
\**********************************************************/

package com.daifubackend.api.utils;

import java.io.ByteArrayOutputStream;

final class Base64 {
    private static final char[] base64EncodeChars = new char[] {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
        'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
        'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
        'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
        'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
        'w', 'x', 'y', 'z', '0', '1', '2', '3',
        '4', '5', '6', '7', '8', '9', '+', '/' };

    private static final byte[] base64DecodeChars = new byte[] {
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63,
    52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1,
    -1,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
    15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1,
    -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
    41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1 };

    private static final char[] BASE64_ENCODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    private static final byte[] BASE64_DECODE_CHARS = {
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63,
            52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1,
            -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
            15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1,
            -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
            41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1
    };

    private Base64() {}

    public static final String encode(byte[] data) {
        StringBuilder sb = new StringBuilder();
        int r = data.length % 3;
        int len = data.length - r;
        int i = 0;
        int c;
        while (i < len) {
            c = (0x000000ff & data[i++]) << 16 |
                (0x000000ff & data[i++]) << 8  |
                (0x000000ff & data[i++]);
            sb.append(base64EncodeChars[c >> 18]);
            sb.append(base64EncodeChars[c >> 12 & 0x3f]);
            sb.append(base64EncodeChars[c >> 6  & 0x3f]);
            sb.append(base64EncodeChars[c & 0x3f]);
        }
        if (r == 1) {
            c = 0x000000ff & data[i++];
            sb.append(base64EncodeChars[c >> 2]);
            sb.append(base64EncodeChars[(c & 0x03) << 4]);
            sb.append("==");
        }
        else if (r == 2) {
            c = (0x000000ff & data[i++]) << 8 |
                (0x000000ff & data[i++]);
            sb.append(base64EncodeChars[c >> 10]);
            sb.append(base64EncodeChars[c >> 4 & 0x3f]);
            sb.append(base64EncodeChars[(c & 0x0f) << 2]);
            sb.append("=");
        }
        return sb.toString();
    }

    public static final byte[] decode(String str) {
        byte[] data = str.getBytes();
        int len = data.length;
        ByteArrayOutputStream buf = new ByteArrayOutputStream(len);
        int i = 0;
        int b1, b2, b3, b4;

        while (i < len) {

            /* b1 */
            do {
                b1 = base64DecodeChars[data[i++]];
            } while (i < len && b1 == -1);
            if (b1 == -1) {
                break;
            }

            /* b2 */
            do {
                b2 = base64DecodeChars[data[i++]];
            } while (i < len && b2 == -1);
            if (b2 == -1) {
                break;
            }
            buf.write((int) ((b1 << 2) | ((b2 & 0x30) >>> 4)));

            /* b3 */
            do {
                b3 = data[i++];
                if (b3 == 61) {
                    return buf.toByteArray();
                }
                b3 = base64DecodeChars[b3];
            } while (i < len && b3 == -1);
            if (b3 == -1) {
                break;
            }
            buf.write((int) (((b2 & 0x0f) << 4) | ((b3 & 0x3c) >>> 2)));

            /* b4 */
            do {
                b4 = data[i++];
                if (b4 == 61) {
                    return buf.toByteArray();
                }
                b4 = base64DecodeChars[b4];
            } while (i < len && b4 == -1);
            if (b4 == -1) {
                break;
            }
            buf.write((int) (((b3 & 0x03) << 6) | b4));
        }
        return buf.toByteArray();
    }

    public static String btoa(String str) {
        byte[] bytes = str.getBytes();
        StringBuilder buf = new StringBuilder();
        int i = 0;
        int len = bytes.length;
        int r = len % 3;
        len = len - r;
        int l = (len / 3) << 2;
        if (r > 0) {
            l += 4;
        }
        for (i = 0; i < len; i += 3) {
            int c = (bytes[i] << 16) | (bytes[i + 1] << 8) | bytes[i + 2];
            buf.append(BASE64_ENCODE_CHARS[c >> 18]);
            buf.append(BASE64_ENCODE_CHARS[(c >> 12) & 0x3f]);
            buf.append(BASE64_ENCODE_CHARS[(c >> 6) & 0x3f]);
            buf.append(BASE64_ENCODE_CHARS[c & 0x3f]);
        }
        if (r == 1) {
            int c = bytes[i];
            buf.append(BASE64_ENCODE_CHARS[c >> 2]);
            buf.append(BASE64_ENCODE_CHARS[(c & 0x03) << 4]);
            buf.append("==");
        } else if (r == 2) {
            int c = (bytes[i] << 8) | bytes[i + 1];
            buf.append(BASE64_ENCODE_CHARS[c >> 10]);
            buf.append(BASE64_ENCODE_CHARS[(c >> 4) & 0x3f]);
            buf.append(BASE64_ENCODE_CHARS[(c & 0x0f) << 2]);
            buf.append("=");
        }
        return buf.toString();
    }

    public static byte[] atob(String str) {
        int c1, c2, c3, c4;
        int i, j, len, r, l;
        len = str.length();
        if (len % 4 != 0) {
            return null;
        }
        if (!str.matches("[A-Za-z0-9+/=]+")) {
            return null;
        }
        if (str.charAt(len - 2) == '=') {
            r = 1;
        } else if (str.charAt(len - 1) == '=') {
            r = 2;
        } else {
            r = 0;
        }
        l = len;
        if (r > 0) {
            l -= 4;
        }
        l = (l >> 2) * 3 + r;
        byte[] out = new byte[l];
        i = j = 0;
        while (i < len) {
            c1 = BASE64_DECODE_CHARS[str.charAt(i++)];
            if (c1 == -1) {
                break;
            }
            c2 = BASE64_DECODE_CHARS[str.charAt(i++)];
            if (c2 == -1) {
                break;
            }
            out[j++] = (byte) ((c1 << 2) | ((c2 & 0x30) >> 4));
            c3 = BASE64_DECODE_CHARS[str.charAt(i++)];
            if (c3 == -1) {
                break;
            }
            out[j++] = (byte) (((c2 & 0x0f) << 4) | ((c3 & 0x3c) >> 2));
            c4 = BASE64_DECODE_CHARS[str.charAt(i++)];
            if (c4 == -1) {
                break;
            }
            out[j++] = (byte) (((c3 & 0x03) << 6) | c4);
        }
        return out;
    }
}