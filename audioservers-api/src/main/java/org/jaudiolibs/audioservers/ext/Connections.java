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
 * This extension can be used to control whether the AudioServer is connected to
 * a physical device (soundcard). It only makes sense where the underlying library
 * supports running without being directly connected to a physical device
 * (currently only JACK).
 * 
 * Libraries that support this extension should ensure the correct instance of this class
 * is included in the AudioConfiguration passed to the AudioClient. 
 * 
 * @author Neil C Smith
 */
public class Connections {
    
    public final static Connections NONE = new Connections(false, false);
    public final static Connections INPUT = new Connections(true, false);
    public final static Connections OUTPUT = new Connections(false, true);
    public final static Connections ALL = new Connections(true, true);
    
    private final boolean connectInputs;
    private final boolean connectOutputs;
    
    private Connections(boolean connectInputs, boolean connectOutputs) {
        this.connectInputs = connectInputs;
        this.connectOutputs = connectOutputs;
    }

    public boolean isConnectInputs() {
        return connectInputs;
    }

    public boolean isConnectOutputs() {
        return connectOutputs;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Connections other = (Connections) obj;
        if (this.connectInputs != other.connectInputs) {
            return false;
        }
        if (this.connectOutputs != other.connectOutputs) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.connectInputs ? 1 : 0);
        hash = 29 * hash + (this.connectOutputs ? 1 : 0);
        return hash;
    }

    @Override
    public String toString() {
        if (connectInputs) {
            if (connectOutputs) {
                return "Connections.ALL";
            } else {
                return "Connections.INPUT";
            }
        } else {
            if (connectOutputs) {
                return "Connections.OUTPUT";
            } else {
                return "Connections.NONE";
            }
        }
    }
    
    
    
}
