/*
 * Copyright (C) 2015, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.transport.file;

import com.illposed.osc.OSCPacket;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.OSCSerializer;
import com.illposed.osc.OSCSerializerFactory;
import com.illposed.osc.argument.handler.StringArgumentHandler;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.StandardOpenOption;

/**
 * Handles writing to files, using the content format described in the OSC 1.1 specification.
 */
public class OSCFileWriter {

	private final ByteBuffer buffer;
	private final OSCSerializer serializer;

	/**
	 * Creates a new file writer.
	 * @param serializerFactory to generate a serialzier that will serialize the packet
	 */
	public OSCFileWriter(final OSCSerializerFactory serializerFactory) {

		this.buffer = ByteBuffer.allocate(1024);
		this.serializer = serializerFactory.create(this.buffer);
	}

	public static void generateHeader(
			final ByteBuffer output,
			final Charset charset,
			final OSCFile file)
	{
		output.put("MIME-Version: 1.0\n".getBytes(charset));
		output.put("Content-type: application/osc;\n".getBytes(charset));
		output.put("\tframing=slip\n".getBytes(charset));
		output.put(("\tversion=" + file.getVersion() + '\n').getBytes(charset));
		output.put(("\turi=" + file.getServiceUri().toString() + '\n').getBytes(charset));
		output.put(("\ttypes=" + file.getTypeTags()).getBytes(charset));
	}

	/**
	 * Writes a packet to the file, discarding the current file content.
	 * @param file
	 * @param packet to be written to the file
	 * @throws IOException
	 * @throws OSCSerializeException
 	 */
	public void write(final OSCFile file, final OSCPacket packet)
			throws IOException, OSCSerializeException
	{
		final FileChannel fileChan = FileChannel.open(
				file.getPath(),
				StandardOpenOption.WRITE,
				StandardOpenOption.TRUNCATE_EXISTING,
				StandardOpenOption.CREATE);

		buffer.rewind();
		final Charset charset
				= (Charset) serializer.getProperties().get(StringArgumentHandler.PROP_NAME_CHARSET);
		generateHeader(buffer, charset, file);
		buffer.put((byte) '\n');
		buffer.flip();
		fileChan.write(buffer);

		buffer.rewind();
		serializer.write(packet);
		buffer.flip();
		fileChan.write(buffer);
	}
}
