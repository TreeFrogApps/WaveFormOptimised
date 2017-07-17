package com.treefrogapps.waveformoptimised.WaveFileReader;


import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

final class WaveFileUtils {

    private WaveFileUtils() {}

    static boolean isWaveFile(File file) {
        if (file != null && file.canRead()) {
            byte[] headerArray = getWaveHeader(file);
            if (headerArray != null) {
                final char[] headerCheck = "WAVEfmt".toCharArray();
                for (int i = 0; i < headerCheck.length; i++) {
                    if ((byte) headerCheck[i] != headerArray[i + 8]) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    static @Nullable byte[] getWaveHeader(File file) {
        try {
            final FileInputStream fis = new FileInputStream(file);
            final int header = getHeaderSize(file);
            final byte[] headerArray = new byte[header];
            final int read = fis.read(headerArray, 0, header);
            fis.close();
            return read == header ? headerArray : null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored") private static int getHeaderSize(File file) {
        try {
            final FileInputStream fis = new FileInputStream(file);
            final int bytes = 4;
            byte[] headerCheck = new byte[bytes];
            fis.skip(16L);
            final int read = fis.read(headerCheck, 0, bytes);
            if (read == bytes) {
                return ((headerCheck[0] & 0xFF) | (headerCheck[1] & 0xFF) << 8 | (headerCheck[2] & 0xFF) << 16 | headerCheck[3] << 24) == 16 ? 44 : 46;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
