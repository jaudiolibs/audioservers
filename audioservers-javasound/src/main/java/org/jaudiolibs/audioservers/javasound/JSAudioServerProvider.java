/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this work; if not, see http://www.gnu.org/licenses/
 *
 *
 * Linking this work statically or dynamically with other modules is making a
 * combined work based on this work. Thus, the terms and conditions of the GNU
 * General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this work give you permission
 * to link this work with independent modules to produce an executable,
 * regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that
 * you also meet, for each linked independent module, the terms and conditions of
 * the license of that module. An independent module is a module which is not
 * derived from or based on this work. If you modify this work, you may extend
 * this exception to your version of the work, but you are not obligated to do so.
 * If you do not wish to do so, delete this exception statement from your version.
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package org.jaudiolibs.audioservers.javasound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import org.jaudiolibs.audioservers.AudioClient;
import org.jaudiolibs.audioservers.AudioConfiguration;
import org.jaudiolibs.audioservers.AudioServer;
import org.jaudiolibs.audioservers.AudioServerProvider;
import org.jaudiolibs.audioservers.ext.Device;

/**
 *
 * @author Neil C Smith
 */
public class JSAudioServerProvider extends AudioServerProvider {
    
    private final static Logger LOG = Logger.getLogger(JSAudioServerProvider.class.getName());

    @Override
    public <T> T find(Class<T> type) {
        Iterator<T> itr = findAll(type).iterator();
        if (itr.hasNext()) {
            return itr.next();
        } else {
            return null;
        }
    }

    @Override
    public <T> Iterable<T> findAll(Class<T> type) {
        if (type.isAssignableFrom(Device.class)) {
            return (Iterable<T>) findDevices();
        } else {
            return Collections.emptyList();
        }

    }

    @Override
    public String getLibraryName() {
        return "JavaSound";
    }

    @Override
    public AudioServer createServer(AudioConfiguration config, AudioClient client) throws Exception {
        Device inputDevice = findInputDevice(config);
        Mixer inputMixer = inputDevice == null ? null : inputDevice.find(Mixer.class);
        Device outputDevice = findOutputDevice(config);
        Mixer outputMixer = outputDevice == null ? null : outputDevice.find(Mixer.class);
        JSTimingMode timingMode = findTimingMode(config);
        
        ArrayList<Object> exts = new ArrayList<Object>();
        if (inputDevice != null) {
            exts.add(inputDevice);
        }
        if (outputDevice != null && outputDevice != inputDevice) {
            exts.add(outputDevice);
        }
        exts.add(timingMode);
        
        config = new AudioConfiguration(
                config.getSampleRate(),
                config.getInputChannelCount(),
                config.getOutputChannelCount(),
                config.getMaxBufferSize(),
                exts.toArray());
        
        if (LOG.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Building JSAudioServer\n");
            sb.append(config);
            sb.append("Input Mixer : ").append(inputMixer).append('\n');
            sb.append("Output Mixer : ").append(outputMixer).append('\n');
            LOG.fine(sb.toString());
        }
        
        return new JSAudioServer(inputMixer, outputMixer, timingMode, config, client);
    }
    
    private static Device findInputDevice(AudioConfiguration config) {
        for (Device dev : config.findAll(Device.class)) {
            if (dev.getMaxInputChannels() > 0) {
                Mixer m = dev.find(Mixer.class);
                if (m != null) {
                    return dev;
                }
            }
        }
        return null;
    }

    private static Device findOutputDevice(AudioConfiguration config) {
        for (Device dev : config.findAll(Device.class)) {
            if (dev.getMaxOutputChannels() > 0) {
                Mixer m = dev.find(Mixer.class);
                if (m != null) {
                    return dev;
                }
            }
        }
        return null;
    }
    
    private static Mixer findInputMixer(AudioConfiguration config) {
        Mixer ret = null;
        for (Device dev : config.findAll(Device.class)) {
            if (dev.getMaxInputChannels() > 0) {
                ret = dev.find(Mixer.class);
                if (ret != null) {
                    break;
                }
            }
        }
        return ret;
    }
    
    private static Mixer findOutputMixer(AudioConfiguration config) {
        Mixer ret = null;
        for (Device dev : config.findAll(Device.class)) {
            if (dev.getMaxOutputChannels() > 0) {
                ret = dev.find(Mixer.class);
                if (ret != null) {
                    break;
                }
            }
        }
        return ret;
    }

    

    private static JSTimingMode findTimingMode(AudioConfiguration config) {
        JSTimingMode mode = config.find(JSTimingMode.class);
        if (mode == null) {
            return JSTimingMode.Estimated;
        } else {
            return mode;
        }
    }

    private static List<Device> findDevices() {
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        if (mixerInfos.length == 0) {
            return Collections.emptyList();
        }
        List<Device> devices = new ArrayList<Device>(mixerInfos.length);
        for (Mixer.Info info : mixerInfos) {
            Mixer mixer = AudioSystem.getMixer(info);
            int ins = getMaximumChannels(mixer, true);
            int outs = getMaximumChannels(mixer, false);
            // @TODO what about port mixers?
//            if (ins == 0 && outs == 0) {
//                continue;
//            }
            devices.add(new JSDevice(mixer, ins, outs));
        }
        return devices;
    }

    private static int getMaximumChannels(Mixer mixer, boolean input) {
        int max = 0;
        Line.Info[] lines = input ? mixer.getTargetLineInfo() : mixer.getSourceLineInfo();
        for (Line.Info line : lines) {
            if (line instanceof DataLine.Info) {
                AudioFormat[] formats = ((DataLine.Info) line).getFormats();
                for (AudioFormat format : formats) {
                    int channels = format.getChannels();
                    if (channels == AudioSystem.NOT_SPECIFIED) {
                        max = 32;
                    } else if (channels > max) {
                        max = channels;
                    }
                }
            }
        }
        return max;
    }
}
