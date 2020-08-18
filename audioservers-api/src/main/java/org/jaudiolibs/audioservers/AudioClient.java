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

import java.nio.FloatBuffer;
import java.util.List;

/**
 * The basic interface that will be used by server implementations. This interface
 * is deliberately decoupled from any particular audio server implementation. All
 * necessary information will be passed from the server in the configure method
 * before any call to process().
 *
 */
public interface AudioClient {

    /**
     * This method will be called by the server implementation prior to any call
     * to process(). The supplied AudioConfiguration object provides information on
     * sample rate, buffer size, etc. required by the server. An Exception may be
     * thrown if the client is unable to be configured to match the requirements of
     * the server. The type of the Exception is deliberately unspecified and left to
     * the implementation, but will commonly be an IllegalArgumentException or
     * IllegalStateException.
     * 
     * @param context encapsulates information required for configuring the client
     * @throws Exception if the server cannot be configured
     */
    public void configure(AudioConfiguration context) throws Exception;

    /**
     * The method that actually processes the audio. The client is provided with
     * the time for the current buffer, measured in nanoseconds and relative to
     * System.nanotime(). The client should always use the time provided by the
     * server.
     *
     * The server will provide the client with unmodifiable lists of input and
     * output audio buffers as FloatBuffers. In the case of there being no input
     * channels, a zero length list rather than null will be passed in. Input buffers
     * should be treated as read-only.
     *
     * The server will provide the number of frames of audio in each buffer. The count
     * will always be the same across each input and output buffer. If the
     * context passed to configure returns true for isFixedBufferSize() then the
     * number of frames will always be equal to getMaxBufferSize(). Otherwise, the
     * number of frames may be between 1 and getMaxBufferSize().
     *
     * The client should return a boolean value - true if the audio has been processed
     * OK, false to disconnect the client from the server.
     *
     * @param time buffer time relative to {@link System#nanoTime()}
     * @param inputs input buffers (may be empty)
     * @param outputs output buffers
     * @param nframes number of samples / frames per buffer
     * @return boolean (OK / disconnect)
     */
    public boolean process(long time, List<FloatBuffer> inputs,
           List<FloatBuffer> outputs, int nframes);

    /**
     * Signal that the client is being shut down. It is up to implementors of this
     * interface whether they allow themselves to be reconfigured ready to process
     * audio again.
     */
    public void shutdown();

}
