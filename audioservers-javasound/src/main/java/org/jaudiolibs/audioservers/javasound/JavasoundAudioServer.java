/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Neil C Smith.
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

import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import org.jaudiolibs.audioservers.AudioClient;
import org.jaudiolibs.audioservers.AudioConfiguration;

/**
 * Implementation of an AudioServer using Javasound.
 *
 * @author Neil C Smith
 */
@Deprecated
public class JavasoundAudioServer extends JSAudioServer {

    private final static Logger LOG = Logger.getLogger(JavasoundAudioServer.class.getName());

    /**
     * Timing mode used by the server.
     *
     * For lowest latency try {@link #FramePosition} or {@link #Estimated}.
     */
    @Deprecated
    public static enum TimingMode {

        /**
         * Blocking timing mode. Block on write to output line. Javasound output
         * buffer is same size as internal buffers.
         */
        Blocking,
        /**
         * FramePosition timing mode. Use large Javasound output buffer.
         * Determine when to write to output line via getLongFramePosition().
         */
        FramePosition,
        /**
         * Estimated timing mode. Use large Javasound output buffer. Determine
         * when to write to output line by estimating position using
         * System.nanotime().
         */
        // @TODO Investigate whether xruns in underlying library are causing latency to increase.
        Estimated
    };

    private JavasoundAudioServer(Mixer inputMixer,
            Mixer outputMixer,
            JSTimingMode mode,
            AudioConfiguration context,
            AudioClient client) {
        super(inputMixer, outputMixer, mode, context, client);
    }

   
    /**
     * Create a JavasoundAudioServer.
     *
     *
     * @param inputMixer Javasound mixer to use for audio input. Can be null as
     * long as the context passed in requests no audio input channels.
     * @param outputMixer Javasound mixer to use for audio output.
     * @param context Requested audio configuration. Variable buffer size
     * property is ignored.
     * @param mode Timing mode to use. For lowest latency use
     * TimingMode.Estimated or TimingMode.FramePosition.
     * @param client Audio client to process every callback.
     * @return server
     * @throws IllegalArgumentException if requested context has no output
     * channels.
     */
    @Deprecated
    public static JavasoundAudioServer create(Mixer inputMixer, Mixer outputMixer,
            AudioConfiguration context, TimingMode mode, AudioClient client) {
        if (outputMixer == null || mode == null
                || context == null || client == null) {
            throw new NullPointerException();
        }
        if (inputMixer == null && context.getInputChannelCount() > 0) {
            throw new NullPointerException();
        }
        // must have real time output
        if (context.getOutputChannelCount() == 0) {
            throw new IllegalArgumentException();
        }
        // always fixed buffer size
        if (!context.isFixedBufferSize()) {
            context = new AudioConfiguration(context.getSampleRate(),
                    context.getInputChannelCount(),
                    context.getOutputChannelCount(),
                    context.getMaxBufferSize(),
                    true);
        }
        JSTimingMode jsMode = convertTimingMode(mode);
        return new JavasoundAudioServer(inputMixer, outputMixer, jsMode, context, client);
    }

    /**
     * Create a JavasoundAudioServer.
     *
     *
     * @param device Name of the device to use for audio input and output. If
     * null or an empty String, the first (default) device that provides the
     * required number of lines will be chosen. Otherwise a device will be
     * searched for that contains the provided String in its name.
     * @param context Requested audio configuration. Variable buffer size
     * property is ignored.
     * @param mode Timing mode to use. For lowest latency use
     * TimingMode.Estimated or TimingMode.FramePosition.
     * @param client Audio client to process every callback.
     * @return server
     * @throws Exception if requested context has no output channels, or if
     * suitable mixers cannot be found to satisfy the device name.
     */
    @Deprecated
    public static JavasoundAudioServer create(String device, AudioConfiguration context,
            TimingMode mode, AudioClient client) throws Exception {
        if (mode == null || client == null) {
            throw new NullPointerException();
        }
        if (device == null) {
            device = "";
        }
        // must have real time output
        if (context.getOutputChannelCount() == 0) {
            throw new IllegalArgumentException();
        }
        Mixer in = null;
        if (context.getInputChannelCount() > 0) {
            in = findInputMixer(device, context);
        }
        Mixer out = findOutputMixer(device, context);
        // always fixed buffer size
        if (!context.isFixedBufferSize()) {
            context = new AudioConfiguration(context.getSampleRate(),
                    context.getInputChannelCount(),
                    context.getOutputChannelCount(),
                    context.getMaxBufferSize(),
                    true);
        }
        JSTimingMode jsMode = convertTimingMode(mode);
        return new JavasoundAudioServer(in, out, jsMode, context, client);
    }

    private static Mixer findInputMixer(String device, AudioConfiguration conf) throws Exception {
        Mixer.Info[] infos = AudioSystem.getMixerInfo();
        Mixer mixer;
        DataLine.Info lineInfo = new DataLine.Info(TargetDataLine.class, getInputFormat(conf));
        for (int i = 0; i < infos.length; i++) {
            mixer = AudioSystem.getMixer(infos[i]);
            if (mixer.isLineSupported(lineInfo)) {
                if (infos[i].getName().indexOf(device) >= 0) {
                    LOG.finest("Found input mixer :\n" + infos[i]);
                    return mixer;
                }
            }
        }
        throw new Exception();
    }

    private static Mixer findOutputMixer(String device, AudioConfiguration conf) throws LineUnavailableException {
        Mixer.Info[] infos = AudioSystem.getMixerInfo();
        Mixer mixer;
        DataLine.Info lineInfo = new DataLine.Info(SourceDataLine.class, getOutputFormat(conf));
        for (int i = 0; i < infos.length; i++) {
            mixer = AudioSystem.getMixer(infos[i]);
            if (mixer.isLineSupported(lineInfo)) {
                if (infos[i].getName().indexOf(device) >= 0) {
                    LOG.finest("Found output mixer :\n" + infos[i]);
                    return mixer;
                }
            }
        }
        throw new LineUnavailableException();
    }

    private static AudioFormat getInputFormat(AudioConfiguration conf) {
        return new AudioFormat(conf.getSampleRate(),
                16,
                conf.getInputChannelCount(),
                true,
                false);
    }

    private static AudioFormat getOutputFormat(AudioConfiguration conf) {
        return new AudioFormat(conf.getSampleRate(),
                16,
                conf.getOutputChannelCount(),
                true,
                false);
    }
    
    private static JSTimingMode convertTimingMode(TimingMode mode) {
        switch (mode) {
            case Estimated:
                return JSTimingMode.Estimated;
            case FramePosition:
                return JSTimingMode.FramePosition;
            default:
                return JSTimingMode.Blocking;
        }
    }
}
