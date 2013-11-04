/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 *
 */
package org.jaudiolibs.audioservers.jack;

import org.jaudiolibs.audioservers.AudioClient;
import org.jaudiolibs.audioservers.AudioConfiguration;
import org.jaudiolibs.audioservers.AudioServer;
import org.jaudiolibs.audioservers.AudioServerProvider;
import org.jaudiolibs.audioservers.ext.ClientID;
import org.jaudiolibs.audioservers.ext.Connections;

/**
 *
 * @author Neil C Smith
 */
public class JackAudioServerProvider extends AudioServerProvider {

    @Override
    public String getLibraryName() {
        return "JACK";
    }

    @Override
    public String getLibraryDescription() {
        return "JACK Audio Connection Kit";
    }
    
    
    @Override
    public AudioServer createServer(AudioConfiguration config, AudioClient client) throws Exception {
        ClientID id = config.find(ClientID.class);
        if (id == null) {
            id = new ClientID("JAudioLibs");
        }
        Connections con = config.find(Connections.class);
        if (con == null) {
            con = Connections.NONE;
        }
        return new JackAudioServer(id, con, config, client);
    }
    
}
