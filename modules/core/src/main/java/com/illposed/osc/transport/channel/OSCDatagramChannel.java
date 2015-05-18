/*
 * Copyright (C) 2015, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.transport.channel;

import com.illposed.osc.OSCPacket;
import com.illposed.osc.OSCParseException;
import com.illposed.osc.OSCParser;
import com.illposed.osc.OSCParserFactory;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.OSCSerializer;
import com.illposed.osc.OSCSerializerFactory;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;

/**
 * TODO.
 * An abstract superclass.
 * To send OSC messages, use {@link OSCPortOut}.
 * To listen for OSC messages, use {@link OSCPortIn}.
 */
public class OSCDatagramChannel extends SelectableChannel {

	private final DatagramChannel underlyingChannel;
//	private final ByteBuffer buffer;
	private final OSCParser parser;
//	private final OSCSerializer serializer;
	private final OSCSerializerFactory serializerFactory;

	public <C extends ByteChannel, InterruptibleChannel> OSCDatagramChannel(
			final DatagramChannel underlyingChannel,
			final OSCParserFactory parserFactory,
			final OSCSerializerFactory serializerFactory/*,
			final ByteBuffer buffer*/)
	{
//		super(underlyingChannel.provider());

		this.underlyingChannel = underlyingChannel;
//		this.buffer = buffer;
		this.parser = (parserFactory == null) ? null : parserFactory.create();
//		this.serializer = serializerFactory.create();
		this.serializerFactory = serializerFactory;
	}

//	public static <C extends ByteChannel, InterruptibleChannel> OSCDatagramChannel open(final C underlyingChannel) throws IOException {
//
//		return new OSCDatagramChannel(underlyingChannel);
//	}

	public OSCPacket read(final ByteBuffer buffer) throws IOException, OSCParseException {

		boolean completed = false;
		try {
			begin();



//			try {
				buffer.clear();
//				try {
					if (underlyingChannel.isConnected()) {
						/*final int readBytes = */underlyingChannel.read(buffer);
					} else {
						underlyingChannel.receive(buffer);
					}
//					final int readBytes = buffer.position();
//				} catch (final ClosedChannelException ex) {
//					if (listening) {
//						throw ex;
//					} else {
//						// if we closed the channel while receiving data,
//						// the exception is expected/normal, so we hide it
//						continue;
//					}
//				}
				buffer.flip();
				if (buffer.limit() == 0) {
//					if (isListening()) {
						throw new OSCParseException("Received a packet without any data");
//					} else {
//						// normal exit: we just get a no-data package becasue we stopped listening
//						break;
//					}
				} else {
					// convert from OSC byte array -> Java object
					// FIXME BAAAAAAAD!! - the overflow would happen on the receiving end, up there, not down here!
					OSCPacket oscPacket = null;
//					do {
//						try {
							oscPacket = parser.convert(buffer);
//						} catch (final BufferOverflowException ex) {
//							buffer = ByteBuffer.allocate(buffer.capacity() + BUFFER_SIZE);
//						}
//					} while (oscPacket == null);
final long timeout = 10000L;
try {
	Thread.currentThread().wait(timeout);
} catch (InterruptedException ex) {
	ex.printStackTrace();
}
long ggg = System.currentTimeMillis();
int bla = (int) ggg % Integer.MAX_VALUE;
for (int i = bla; i < (bla + 100000000); i++) {
bla *= i;
}
long ttt = System.currentTimeMillis() - ggg;
System.err.println("XXX time consuming loop took to execute [ms]: " + ttt);

					// dispatch the Java object
//					dispatcher.dispatchPacket(oscPacket);
					completed = true;
					return oscPacket;
				}
//			} catch (final IOException ex) {
//				stopListening(ex);
//			} catch (final OSCParseException ex) {
//				throw new IOException(ex);
//			}



//			// TODO read from underlyingChannel into buffer
//			// TODO serialize into buffer
//			throw new UnsupportedOperationException("Not supported yet.");
//			completed = vvv;    // Perform blocking I/O operation
//			return xxx;         // Return result
		} finally {
			end(completed);
		}
	}

	public void send(final ByteBuffer buffer, final OSCPacket packet, final SocketAddress remoteAddress) throws IOException, OSCSerializeException {

		boolean completed = false;
		try {
			begin();




			final OSCSerializer serializer = serializerFactory.create(buffer);
			buffer.rewind();
//			try {
				serializer.write(packet);
//			} catch (final OSCSerializeException ex) {
//				throw new IOException(ex);
//			}
			buffer.flip();

			if (underlyingChannel.isConnected()) {
				underlyingChannel.write(buffer);
			} else if (remoteAddress == null) {
				throw new IllegalStateException("Not connected and no remote address is given");
			} else {
				underlyingChannel.send(buffer, remoteAddress);
			}




//			// TODO serialize into buffer
//			// TODO write buffer to underlyingChannel
//			if (1 == 1) throw new UnsupportedOperationException("Not supported yet.");



			completed = true;
		} finally {
			end(completed);
		}
	}

	public void write(final ByteBuffer buffer, final OSCPacket packet) throws IOException, OSCSerializeException {

		boolean completed = false;
		try {
			begin();
			if (!underlyingChannel.isConnected()) {
				throw new IllegalStateException("Either connect the channel or use write()");
			}
			send(buffer, packet, null);
			completed = true;
		} finally {
			end(completed);
		}
	}


//	@Override
//	protected void implCloseChannel() throws IOException {
//		throw new UnsupportedOperationException("Not supported yet.");
//	}

//	@Override
//	protected void implCloseSelectableChannel() throws IOException {
//		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//	}
//
//	@Override
//	protected void implConfigureBlocking(boolean block) throws IOException {
//		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//	}
//
//	@Override
//	public int validOps() {
//		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//	}

//	private final DatagramChannel channel;
//
//	public static final int DEFAULT_SC_OSC_PORT = 57110;
//	public static final int DEFAULT_SC_LANG_OSC_PORT = 57120;
//
//	protected OSCDatagramChannel(final SocketAddress local, final SocketAddress remote) throws IOException {
//
//		this.local = local;
//		this.remote = remote;
//		this.channel = DatagramChannel.open();
//
//		this.channel.setOption(java.net.StandardSocketOptions.SO_REUSEADDR, true); // XXX This is only available since Java 1.7
//		this.channel.socket().bind(local);
//	}
//
//	/**
//	 * The port that the SuperCollider <b>synth</b> engine
//	 * usually listens to.
//	 * @return default SuperCollider <b>synth</b> UDP port
//	 * @see #DEFAULT_SC_OSC_PORT
//	 */
//	public static int defaultSCOSCPort() {
//		return DEFAULT_SC_OSC_PORT;
//	}
//
//	/**
//	 * The port that the SuperCollider <b>language</b> engine
//	 * usually listens to.
//	 * @return default SuperCollider <b>language</b> UDP port
//	 * @see #DEFAULT_SC_LANG_OSC_PORT
//	 */
//	public static int defaultSCLangOSCPort() {
//		return DEFAULT_SC_LANG_OSC_PORT;
//	}
//
//	/**
//	 * Returns the channel associated with this port.
//	 * @return this ports channel
//	 */
//	protected DatagramChannel getChannel() {
//		return channel;
//	}
//
//	public void connect() throws IOException {
//
//		if (getRemoteAddress() == null) {
//			throw new IllegalStateException(
//					"Can not connect a socket without a remote address specified");
//		}
//		getChannel().connect(getRemoteAddress());
//	}
//
//	public void disconnect() throws IOException {
//		getChannel().disconnect();
//	}
//
//	public boolean isConnected() {
//		return getChannel().isConnected();
//	}
//
//	public SocketAddress getLocalAddress() {
//		return local;
//	}
//
//	public SocketAddress getRemoteAddress() {
//		return remote;
//	}
//
//	/**
//	 * Close the socket and free-up resources.
//	 * It is recommended that clients call this when they are done with the
//	 * port.
//	 * @throws IOException If an I/O error occurs on the channel
//	 */
//	public void close() throws IOException {
//		channel.close();
//	}

	@Override
	public SelectorProvider provider() {
		return underlyingChannel.provider();
	}

	@Override
	public boolean isRegistered() {
		return underlyingChannel.isRegistered();
	}

	@Override
	public SelectionKey keyFor(final Selector sel) {
		return underlyingChannel.keyFor(sel);
	}

	@Override
	public SelectionKey register(final Selector sel, final int ops, final Object att) throws ClosedChannelException {
		return underlyingChannel.register(sel, ops, att);
	}

	@Override
	public SelectableChannel configureBlocking(final boolean block) throws IOException {
		return underlyingChannel.configureBlocking(block);
	}

	@Override
	public boolean isBlocking() {
		return underlyingChannel.isBlocking();
	}

	@Override
	public Object blockingLock() {
		return underlyingChannel.blockingLock();
	}

	@Override
	protected void implCloseChannel() throws IOException {
		underlyingChannel.close(); // XXX is this ok?
	}

	@Override
	public int validOps() {
		return underlyingChannel.validOps();
	}
}
