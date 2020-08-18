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

import java.util.Collections;

/**
 * Abstract base class for audio server providers. Implementations should normally
 * be registered in META-INF/services so that they can be found using ServiceLoader
 * or a similar mechanism.
 * 
 * Instances of this class can maintain a list of arbitrary extension Objects 
 * that can be accessed using the find() or findAll() methods. For example,
 * findAll(Device.class) may return a list of possible sound devices. Objects
 * found in this way may be used in the AudioConfiguration parameter passed in
 * when creating an AudioServer.
 * 
 */
public abstract class AudioServerProvider {

    
    /**
     * The name of the library this AudioServerProvider gives access to (eg.
     * JavaSound, JACK).
     * 
     * @return library name
     */
    public abstract String getLibraryName();

    /**
     * An optional description of the library this AudioServerProvider gives
     * access to.
     * 
     * The default implementation return an empty String. Callers should never
     * receive null.
     * 
     * @return description of the library
     */
    public String getLibraryDescription() {
        return "";
    }
    
    /**
     * Can the provided AudioConfiguration be supported by the underlying library.
     * This is an optimistic test - the default implementation returns true in all
     * cases!
     *
     * @param config
     * @return whether the configuration is supported.
     */
    public boolean isConfigurationSupported(AudioConfiguration config) {
        return true;
    }

    /**
     * Create an AudioServer for the given AudioClient.
     * 
     * It is important to note that the AudioConfiguration passed in to this method
     * is used as a guide. The AudioConfiguration passed into the AudioClient's
     * configure method will reflect the actual configuration used, and may be 
     * rejected there if necessary.
     * 
     * @param config
     * @param client
     * @return An implementation of AudioServer
     * @throws Exception
     */
    public abstract AudioServer createServer(
            AudioConfiguration config, AudioClient client) throws Exception;
    
    /**
     * Find and return the first extension Object of the given type.
     *
     * The default implementation always returns null. Subclasses of this class
     * should override this method and the findAll() method as necessary.
     * 
     * @param <T>
     * @param type
     * @return Object or null
     */
    public <T> T find(Class<T> type) {
        return null;
    }

    /**
     * Find and return all extension Objects of the given type.
     * 
     * The default implementation always returns an empty Iterable.
     * Subclasses of this class should override this method and the find() 
     * method as necessary.
     * 
     * @param <T>
     * @param type
     * @return
     */
    public <T> Iterable<T> findAll(Class<T> type) {
        return Collections.emptyList();
    }

}
