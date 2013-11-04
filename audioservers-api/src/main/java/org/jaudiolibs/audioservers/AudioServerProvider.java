/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Neil C Smith.
 *
 * Copying and distribution of this file, with or without modification,
 * are permitted in any medium without royalty provided the copyright
 * notice and this notice are preserved.  This file is offered as-is,
 * without any warranty.
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
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
 * @author Neil C Smith (http://neilcsmith.net)
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
