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

/**
 * A base interface for classes providing a server to run AudioClients.
 *
 * AudioServers provide a run() method as it is intended that the application
 * should provide the Thread in which the server will run, however they do not
 * extend Runnable as the run() method throws an Exception, forcing the
 * application to deal with any problems starting the server.
 */
public interface AudioServer {

    /**
     * Start and run the audio server in the current thread.
     *
     * @throws Exception
     */
    public void run() throws Exception;

    /**
     * Get the current AudioConfiguration. This value should remain constant
     * while the server is processing audio.
     *
     * @return AudioConfiguration
     */
    public AudioConfiguration getAudioContext();

    /**
     * Check whether the server is active. This method can be called from
     * another thread.
     *
     * @return true if active.
     */
    public boolean isActive();

    /**
     * Trigger the server to shut down. This method can be called from another
     * thread, but will be handled asynchronously.
     */
    public void shutdown();

}
