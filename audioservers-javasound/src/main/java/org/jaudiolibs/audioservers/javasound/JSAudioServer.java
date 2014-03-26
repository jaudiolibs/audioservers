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

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import org.jaudiolibs.audioservers.AudioClient;
import org.jaudiolibs.audioservers.AudioConfiguration;
import org.jaudiolibs.audioservers.AudioServer;

/**
 * Implementation of an AudioServer using Javasound.
 *
 * @author Neil C Smith
 */
public class JSAudioServer implements AudioServer {

    private final static Logger LOG = Logger.getLogger(JSAudioServer.class.getName());

    private enum State {

        New, Initialising, Active, Closing, Terminated
    };
    private final static int NON_BLOCKING_MIN_BUFFER = 16384;
    // JS line defaults - need way to make these settable.
    private final static int nonBlockingOutputRatio = 16;
    private final static int lineBitSize = 16;
    private final static boolean signed = true;
    private final static boolean bigEndian = false;
    //
    private final AtomicReference<State> state;
    private final AudioConfiguration context;
    private final Mixer inputMixer;
    private final Mixer outputMixer;
    private final AudioClient client;
    private final JSTimingMode mode;
    
    private TargetDataLine inputLine;
    private SourceDataLine outputLine;
    private byte[] inputByteBuffer;
    private float[] inputFloatBuffer;
    private byte[] outputByteBuffer;
    private float[] outputFloatBuffer;
    private List<FloatBuffer> inputBuffers;
    private List<FloatBuffer> outputBuffers;
    private AudioFloatConverter converter;
    
    JSAudioServer(Mixer inputMixer,
            Mixer outputMixer,
            JSTimingMode mode,
            AudioConfiguration context,
            AudioClient client) {
        this.inputMixer = inputMixer;
        this.outputMixer = outputMixer;
        this.context = context;
        this.mode = mode;
        this.client = client;
        state = new AtomicReference<State>(State.New);
    }

    public void run() throws Exception {
        if (!state.compareAndSet(State.New, State.Initialising)) {
            throw new IllegalStateException();
        }
        try {
            initialise();
            client.configure(context);
        } catch (Exception ex) {
            state.set(State.Terminated);
            closeAll();
            client.shutdown();
            throw ex;
        }
        if (state.compareAndSet(State.Initialising, State.Active)) {
            runImpl();
        }
        closeAll();
        client.shutdown();
        state.set(State.Terminated);
    }

    public AudioConfiguration getAudioContext() {
        return context;
    }

    public boolean isActive() {
        State st = state.get();
        return (st == State.Active || st == State.Closing);
    }

    public void shutdown() {
        State st;
        do {
            st = state.get();
            if (st == State.Terminated || st == State.Closing) {
                break;
            }
        } while (!state.compareAndSet(st, State.Closing));
    }

    private void initialise() throws Exception {
        float srate = (float) context.getSampleRate();
        int buffersize = context.getMaxBufferSize();
        int inputChannels = context.getInputChannelCount();
        int outputChannels = context.getOutputChannelCount();
        // open input line and create internal buffers
        if (inputChannels > 0) {
            AudioFormat inputFormat = new AudioFormat(srate, lineBitSize,
                    inputChannels, signed, bigEndian);
            DataLine.Info inputInfo = new DataLine.Info(TargetDataLine.class, inputFormat);
            if (inputMixer == null) {
                inputLine = (TargetDataLine) AudioSystem.getLine(inputInfo);
            } else {
                inputLine = (TargetDataLine) inputMixer.getLine(inputInfo);
            }          
            inputFloatBuffer = new float[buffersize * inputChannels];
            int byteBufferSize = buffersize * inputFormat.getFrameSize();
            inputByteBuffer = new byte[byteBufferSize];
            byteBufferSize *= nonBlockingOutputRatio;
            inputLine.open(inputFormat, byteBufferSize);

        }
        // open output line and create internal buffers
        AudioFormat outputFormat = new AudioFormat(srate, lineBitSize,
                outputChannels, signed, bigEndian);
        DataLine.Info outputInfo = new DataLine.Info(SourceDataLine.class, outputFormat);
        if (outputMixer == null) {
            outputLine = (SourceDataLine) AudioSystem.getLine(outputInfo);
        } else {
            outputLine = (SourceDataLine) outputMixer.getLine(outputInfo);
        }
        outputFloatBuffer = new float[buffersize * outputChannels];
        int byteBufferSize = buffersize * outputFormat.getFrameSize();
        outputByteBuffer = new byte[byteBufferSize];
        if (mode != JSTimingMode.Blocking) {
            byteBufferSize *= nonBlockingOutputRatio;
            byteBufferSize = Math.min(byteBufferSize,
                    NON_BLOCKING_MIN_BUFFER * outputFormat.getFrameSize());
        }
        outputLine.open(outputFormat, byteBufferSize);

        // create audio converter
        converter = AudioFloatConverter.getConverter(outputFormat);

        // create client buffers
        List<FloatBuffer> ins = new ArrayList<FloatBuffer>(inputChannels);
        for (int i = 0; i < inputChannels; i++) {
            ins.add(FloatBuffer.allocate(buffersize));
        }
        inputBuffers = Collections.unmodifiableList(ins);
        List<FloatBuffer> outs = new ArrayList<FloatBuffer>(outputChannels);
        for (int i = 0; i < outputChannels; i++) {
            outs.add(FloatBuffer.allocate(buffersize));
        }
        outputBuffers = Collections.unmodifiableList(outs);
    }

    private void runImpl() {
        if (inputLine != null) {
            inputLine.start();
        }
        outputLine.start();

        long startTime = System.nanoTime();
        long now = startTime;
        double bufferTime = ((double) context.getMaxBufferSize()
                / context.getSampleRate());
        TimeFilter dll = new TimeFilter(bufferTime, 1.5);
//        bufferTime *= 1e9;
        long bufferCount = 0;
        int bufferSize = context.getMaxBufferSize();
        final boolean debug = LOG.isLoggable(Level.FINEST);
        long bufferTimeNS = (long) (bufferTime * 1e9);
        long msFrames = (long) (context.getSampleRate() / 1000);
        long target, difference;
        try {
            while (state.get() == State.Active) {
                now = System.nanoTime();
                readInput();
                if (client.process((long) (dll.update(now / 1e9) * 1e9), inputBuffers, outputBuffers, bufferSize)) {
                    writeOutput();
                    switch (mode) {
                        case Estimated:
                            target = startTime + (long) (bufferTimeNS * (bufferCount + 1));
                            difference = System.nanoTime() - target;
                            while (difference < -(bufferTimeNS / 16)) {
                                if (difference < -1000000) {
                                    try {
                                        LockSupport.parkNanos(500000);
                                    } catch (Exception ex) {
                                    }
                                } else {
                                    Thread.yield();
                                }
                                difference = System.nanoTime() - target;
                            }
                            break;
                        case FramePosition:
                            target = bufferCount * bufferSize;
                            difference = outputLine.getLongFramePosition() - target;
                            while (difference < -(bufferSize / 16)) {
                                if (difference < -msFrames) {
                                    try {
                                        LockSupport.parkNanos(500000);
                                    } catch (Exception ex) {
                                    }
                                } else {
                                    Thread.yield();
                                }
                                difference = outputLine.getLongFramePosition() - target;
                            }
                            break;
                        default:
                            // do nothing - blocking on write
                    }
                    bufferCount++;
                } else {
                    shutdown();
                }
                if (debug) {
                    processDebug(dll);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(JSAudioServer.class.getName()).log(Level.SEVERE, "", ex);
        }
    }

    private void processDebug(TimeFilter dll) {
        long x = dll.ncycles - 1;
        if (x == 0) {
            LOG.finest("| audiotime drift | filter drift  | systime jitter | filter jitter  |");
        }
        if (x % 1000 == 0) {
            double device_drift = (dll.device_time - dll.system_time) * 1000.0;
            double filter_drift = (dll.filter_time - dll.system_time) * 1000.0;
            double device_rate_error = device_drift / dll.ncycles;
            double filter_jitter = dll.filter_period_error - device_rate_error;
            double system_jitter = dll.system_period_error - device_rate_error;

            LOG.finest(String.format("| %15.6f | %13.6f | %14.6f | %14.6f |",
                    device_drift,
                    filter_drift,
                    system_jitter,
                    filter_jitter));
        }
    }

    private void readInput() {
        TargetDataLine tdl = inputLine;
        if (tdl != null) {
            int bsize = inputByteBuffer.length;
            if (tdl.available() < bsize) {
                int fsize = inputFloatBuffer.length;
                for (int i = 0; i < fsize; i++) {
                    inputFloatBuffer[i] = 0;
                }
            } else {
                tdl.read(inputByteBuffer, 0, bsize);
                converter.toFloatArray(inputByteBuffer, inputFloatBuffer);
            }
            int channels = inputBuffers.size();
            // deinterleave into buffers
            for (int channel = 0; channel < channels; channel++) {
                FloatBuffer inBuf = inputBuffers.get(channel);
                float[] input = inBuf.array();
                for (int i = 0, x = channel; i < input.length; i++) {
                    input[i] = inputFloatBuffer[x];
                    x += channels;
                }
                inBuf.rewind();
            }
        }
    }

    private void writeOutput() {
        // interleave outputs
        int channels = outputBuffers.size();
        for (int channel = 0; channel < channels; channel++) {
            FloatBuffer outBuf = outputBuffers.get(channel);
            float[] output = outBuf.array();
            for (int i = 0, x = channel; i < output.length; i++) {
                outputFloatBuffer[x] = output[i];
                x += channels;
            }
            outBuf.rewind();
        }
        // convert audio
        converter.toByteArray(outputFloatBuffer, outputByteBuffer);
        // write to output
        outputLine.write(outputByteBuffer, 0, outputByteBuffer.length);

    }

    private void closeAll() {
        SourceDataLine sdl = outputLine;
        if (sdl != null) {
            sdl.close();
        }
        TargetDataLine tdl = inputLine;
        if (tdl != null) {
            tdl.close();
        }
    }

}
