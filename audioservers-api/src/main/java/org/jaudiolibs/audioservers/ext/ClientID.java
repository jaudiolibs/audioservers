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
package org.jaudiolibs.audioservers.ext;

/**
 * 
 * Provide an ID for how the library refers to the AudioClient, internally or in
 * any user interface.
 * 
 * Libraries that support this feature should ensure the correct value is passed in
 * to the AudioConfiguration provided to the AudioClient.
 * 
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class ClientID {
      
    private String identifier;
    
    public ClientID(String identifier) {
        if (identifier == null) {
            throw new NullPointerException();
        }
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return identifier;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClientID) {
            return ((ClientID)obj).identifier.equals(identifier);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }
    
    
    
}
