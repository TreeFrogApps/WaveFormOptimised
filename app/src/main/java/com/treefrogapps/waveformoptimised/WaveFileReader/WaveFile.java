package com.treefrogapps.waveformoptimised.WaveFileReader;


import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

import static com.treefrogapps.waveformoptimised.WaveFileReader.WaveFileUtils.getWaveHeader;
import static com.treefrogapps.waveformoptimised.WaveFileReader.WaveFileUtils.isWaveFile;

public class WaveFile {

    private static final String TAG = WaveFile.class.getSimpleName();

    public enum Channels {
        MONO, STEREO, UNKNOWN
    }

    private File waveFile;
    private byte[] header;

    public WaveFile(File waveFile) {
        preconditions(waveFile);
        this.waveFile = waveFile;
        this.header = getWaveHeader(this.waveFile);

        Log.i(TAG, "Sample Rate : \t\t\t" + getSampleRate() +
                "\nBytes Per second : \t" + getBytesPerSecond() +
                "\nBits Per Sample : \t\t" + getBitRate() +
                "\nChannels : \t\t\t" + getChannels().toString() +
                "\nType Format : \t\t" + getFormat() +
                "\nData Size : \t\t\t" + getDataSize() +
                "\nRecording Length : \t" + getRecordingLength() +
                "\nHeader Size : \t\t" + getHeaderSize(header));
    }

    public String getFormat() {
        return ((header[20] & 0xff) | header[21] << 8) == 1 ? "PCM" : "UNKNOWN";
    }

    // header 24-27 - 4 bytes : little endian
    public int getSampleRate() {
        return (header[24] & 0xff) | (header[25] & 0xff) << 8 | (header[26] & 0xff) << 16 | header[27] << 24;
    }

    // header 28-31 - 4 bytes : little endian
    public int getBytesPerSecond() {
        return (header[28] & 0xFF) | (header[29] & 0xff) << 8 | (header[30] & 0xff) << 16 | header[31] << 24;
    }

    // header 34-35 - 2 bytes : little endian
    public int getBitRate() {
        return header[34] | header[35] << 8;
    }

    // header 22-23 - 2 bytes : little endian
    public Channels getChannels() {
        switch ((header[22] & 0xff) | header[23] << 8) {
            case 1:
                return Channels.MONO;
            case 2:
                return Channels.STEREO;
            default:
                return Channels.UNKNOWN;
        }
    }

    // header 40-43 data size : little endian
    public int getDataSize() {
        return (header[40] & 0xff) | (header[41] & 0xff) << 8 | (header[42] & 0xff) << 16 | header[43] << 24;
    }

    public int getRecordingLength() {
        return getDataSize() / getBytesPerSecond();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored") Observable<Float> getAmplitudes(int width) {
        return Observable.defer(() -> Observable.create((ObservableOnSubscribe<Float>) e -> {
            final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(waveFile));
            final long skipCount = getDataSize() / width;
            final int buffer = 2;
            final byte[] bytes = new byte[buffer];
            int read = 0;
            bis.skip(44L);
            while ((read = bis.read(bytes, 0, 2)) != -1) {
                if (read == 2) {
                    final int current = bytes[0] & 0xFF | bytes[1] << 8;
                    if (current != 0) {
                        final float normalised = current / 32768f;
                        e.onNext(normalised);
                    } else {
                        e.onNext(0.0f);
                    }
                }
                bis.skip(skipCount);
            }
            bis.close();
            e.onComplete();
        }).subscribeOn(Schedulers.io()));
    }

    private void preconditions(File waveFile) {
        if (!isWaveFile(waveFile)) {
            throw new IllegalArgumentException("File is either not a wave file, or cannot be read");
        }
    }

    private int getHeaderSize(byte[] header) {
        return header.length;
    }
}
