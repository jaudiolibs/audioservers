/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2019 Neil C Smith.
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
package org.jaudiolibs.audioservers.ext;

/**
 * This extension can be used to control whether the AudioServer is connected to
 * a physical device (soundcard). It only makes sense where the underlying
 * library supports running without being directly connected to a physical
 * device (eg. JACK).
 *
 * Libraries that support this extension should ensure the correct instance of
 * this class is included in the AudioConfiguration passed to the AudioClient.
 *
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

    /**
     * Check whether connected to physical device inputs.
     * 
     * @return connected to physical inputs
     */
    public boolean isConnectInputs() {
        return connectInputs;
    }

    /**
     * Check whether connected to physical device outputs.
     * 
     * @return connected to physical outputs
     */
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
