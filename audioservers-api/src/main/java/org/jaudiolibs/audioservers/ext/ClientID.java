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
package org.jaudiolibs.audioservers.ext;

/**
 *
 * Provide an ID for how the library refers to the AudioClient, internally or in
 * any user interface.
 *
 * Libraries that support this feature should ensure the correct value is passed
 * in to the AudioConfiguration provided to the AudioClient.
 *
 */
public class ClientID {

    private String identifier;

    public ClientID(String identifier) {
        if (identifier == null) {
            throw new NullPointerException();
        }
        this.identifier = identifier;
    }

    /**
     * Get the client identifier.
     *
     * @return client identifier
     */
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
            return ((ClientID) obj).identifier.equals(identifier);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

}
