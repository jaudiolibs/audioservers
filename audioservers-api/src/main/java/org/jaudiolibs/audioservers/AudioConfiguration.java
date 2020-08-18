/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2020 Neil C Smith.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package org.jaudiolibs.audioservers;

import java.util.Iterator;
import org.jaudiolibs.audioservers.util.ObjectLookup;

/**
 * Provides details of the configuration of the server from which an AudioClient
 * will be called.
 *
 * Instances of this class can maintain a list of arbitrary extension Objects
 * that can be accessed using the find() or findAll() methods. These provide a
 * mechanism to pass additional configuration parameters when constructing
 * servers or between server and client.
 *
 */
public final class AudioConfiguration {

    private final float sampleRate;
    private final int inputChannelCount;
    private final int outputChannelCount;
    private final int maxBufferSize;
    private final boolean fixedBufferSize;
    private final ObjectLookup lookup;

    /**
     * Create an AudioConfiguration with fixed buffer size. Extension Objects
     * passed into the constructor can be accessed using the find() or findAll()
     * methods.
     *
     * @param sampleRate requested sample rate in Hz
     * @param inputChannelCount requested number of input channels
     * @param outputChannelCount requested number of output channels
     * @param bufferSize the size (in samples) of each buffer to process
     * @param exts system extensions
     */
    public AudioConfiguration(float sampleRate,
            int inputChannelCount,
            int outputChannelCount,
            int bufferSize,
            Object... exts) {
        this(sampleRate, inputChannelCount, outputChannelCount, bufferSize, true, exts);
    }

    /**
     * Create an AudioConfiguration. Extension Objects passed into the
     * constructor can be accessed using the find() or findAll() methods.
     *
     * @param sampleRate requested sample rate in Hz
     * @param inputChannelCount requested number of input channels
     * @param outputChannelCount requested number of output channels
     * @param maxBufferSize the maximum size (in samples) of each buffer to
     * process
     * @param fixedBufferSize whether buffer size is always equal to
     * maxBufferSize
     * @param exts system extensions
     */
    public AudioConfiguration(float sampleRate,
            int inputChannelCount,
            int outputChannelCount,
            int maxBufferSize,
            boolean fixedBufferSize,
            Object... exts) {
        this.sampleRate = validate(sampleRate, 1);
        this.inputChannelCount = validate(inputChannelCount, 0);
        this.outputChannelCount = validate(outputChannelCount, 0);
        this.maxBufferSize = validate(maxBufferSize, 1);
        this.fixedBufferSize = fixedBufferSize;
        if (exts == null || exts.length == 0) {
            lookup = ObjectLookup.EMPTY;
        } else {
            lookup = new ObjectLookup(exts);
        }
    }

    /**
     * Create an AudioConfiguration without extensions.
     *
     * @param sampleRate requested sample rate in Hz
     * @param inputChannelCount requested number of input channels
     * @param outputChannelCount requested number of output channels
     * @param maxBufferSize the maximum size (in samples) of each buffer to
     * process
     * @param fixedBufferSize whether buffer size is always equal to
     * maxBufferSize
     */
    public AudioConfiguration(float sampleRate,
            int inputChannelCount,
            int outputChannelCount,
            int maxBufferSize,
            boolean fixedBufferSize) {
        this(sampleRate, inputChannelCount, outputChannelCount, maxBufferSize, fixedBufferSize, (Object[]) null);
    }

    private static float validate(float value, float minimum) {
        if (value < minimum) {
            throw new IllegalArgumentException();
        }
        return value;
    }

    private static int validate(int value, int minimum) {
        if (value < minimum) {
            throw new IllegalArgumentException();
        }
        return value;
    }

    /**
     * Is the buffer size fixed. If variable, the buffer size will always be
     * between 1 and getMaxBufferSize(). If fixed will always be equal to
     * getMaxBufferSize().
     *
     * @return true if fixed, otherwise variable.
     */
    public boolean isFixedBufferSize() {
        return fixedBufferSize;
    }

    /**
     * Get the number of input channels.
     *
     * @return int ( >=0 )
     */
    public int getInputChannelCount() {
        return inputChannelCount;
    }

    /**
     * Get the maximum buffer size. This is the buffer size in samples per
     * channel.
     *
     * @return int ( >=1 )
     */
    public int getMaxBufferSize() {
        return maxBufferSize;
    }

    /**
     * Get the number of output channels.
     *
     * @return int ( >=0 )
     */
    public int getOutputChannelCount() {
        return outputChannelCount;
    }

    /**
     * Get the sample rate.
     *
     * @return float ( >=1 )
     */
    public float getSampleRate() {
        return sampleRate;
    }

    /**
     * Find and return the first extension Object of the given type.
     *
     * @param <T>
     * @param type
     * @return Object or null
     */
    public <T> T find(Class<T> type) {
        return lookup.find(type);
    }

    /**
     * Find and return all extension Objects of the given type.
     *
     * @param <T>
     * @param type
     * @return
     */
    public <T> Iterable<T> findAll(Class<T> type) {
        return lookup.findAll(type);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Audio Configuration --- \n");
        sb.append("Sample Rate : ").append(sampleRate).append('\n');
        sb.append("Input Channels : ").append(inputChannelCount).append('\n');
        sb.append("Output Channels : ").append(outputChannelCount).append('\n');
        sb.append("Max Buffer Size : ").append(maxBufferSize).append('\n');
        sb.append("Fixed Buffer Size : ").append(fixedBufferSize).append('\n');
        Iterator<Object> exts = findAll(Object.class).iterator();
        if (exts.hasNext()) {
            sb.append("Extensions -\n");
            do {
                Object o = exts.next();
                sb.append(" -- ").append(o).append(" (").append(o.getClass()).append(")\n");
            } while (exts.hasNext());
        }
        return sb.toString();
    }
}
