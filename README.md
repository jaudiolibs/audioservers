Audio Servers
=============

The JAudioLibs AudioServer API provides a Java callback-based API for audio and 
DSP programming, loosely inspired by PortAudio. Implementations based on JavaSound
and JACK (using JAudioLibs' JNAJack) can also be found here. Additional
implementation may be found elsewhere.

The AudioServer API was initially developed for use in
[PraxisLIVE](https://www.praxislive.org), but has found its way into a variety
of other applications. Using the AudioServer API provides an application with
the ability to easily switch between audio backends at runtime, and can also
make working with JavaSound a little easier.

## Usage

The AudioServer API is designed to be as simple as possible to use. All an 
application needs to do is provide an implementation of the `AudioClient` interface,
and its three methods – `configure()`, `process()` and `shutdown()`. This `AudioClient`
implementation can then be passed to any `AudioServer` to run.

The `configure()` method is called prior to any call to `process()`. It provides an
`AudioConfiguration` object with details of sample rate, buffer size, channels,
etc. When requesting an `AudioServer` implementation, an `AudioConfiguration`
object is also used to request configuration. Depending on the server
implementation, the passed back configuration may not match!

All audio processing is done through the `process()` callback. The client will be
provided with lists of `FloatBuffer`. These may be direct pointers to natively
allocated audio buffers. The code inside `process()` should never block, and never
do anything other than necessary audio processing - it's going to be called
hundreds of times a second.

Applications should not use audio server implementations directly, but use the
Java ServiceLoader mechanism to lookup available server implementations at
runtime.

Check out the [examples repository](https://github.com/jaudiolibs/examples) for
some simple examples of using the API directly. In particular the
[SineAudioClient](https://github.com/jaudiolibs/examples/blob/master/src/main/java/org/jaudiolibs/examples/SineAudioClient.java)

The [JAudioLibs Pipes](https://github.com/jaudiolibs/pipes) library provides an
audio routing and unit generator library built on top of the AudioServer API that
may be useful for many applications.

## Arbitrary extensions

Both the `AudioConfiguration` and `AudioServerProvider` classes provide a way of
passing across or searching for arbitrary extensions.

```java
public <T> T find(Class<T> type)
public Iterable<T> findAll(Class<T> type)
```

eg. use this feature to lookup devices if the audio server provides more than one.

```java
for (AudioServerProvider provider : ServiceLoader.load(AudioServerProvider.class)) {
  System.out.println("Found library : " + provider.getLibraryName());
    System.out.println("==============================================");
    System.out.println("Devices");
    System.out.println("----------------------------------------------");
    for (Device dev : provider.findAll(Device.class)) {
        System.out.println(dev.getName() + 
        " (inputs: " + dev.getMaxInputChannels() +
        ", outputs: " + dev.getMaxOutputChannels() + ")");

    }
}
```

A `Device` can be chosen and passed into the constructor for the `AudioConfiguration`
used to create an audio server in order to choose that device for playback.
Note that on some OS, two devices (one for input, one for output) may need to
be used.

Additional extensions include `ClientID` and `Connections` that are primarily of
use when use to control the JACK server implementation.