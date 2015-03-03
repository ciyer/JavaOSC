/*
 * Copyright (C) 2015, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.transport.file;

import com.illposed.osc.OSCPacket;
import com.illposed.osc.OSCParseException;
import com.illposed.osc.OSCParser;
import com.illposed.osc.OSCParserFactory;
import com.illposed.osc.OSCSerializer;
import com.illposed.osc.argument.handler.StringArgumentHandler;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Handles reading from files, using the content format described in the OSC 1.1 specification.
 */
public class OSCFileReader {

	private final ByteBuffer buffer;
	private final OSCParser parser;

	/**
	 * Creates a new file reader.
	 * @param parserFactory to generate a parser that will parse the packet
	 */
	public OSCFileReader(final OSCParserFactory parserFactory) {

		this.buffer = ByteBuffer.allocate(1024);
		this.parser = parserFactory.create();
	}

	private static String toString(final ByteBuffer input, final Charset charset) {
		return new String(OSCSerializer.toByteArray(input), charset);
	}

	private static List<String> parseHeaderLines(
			final ByteBuffer input,
			final Charset charset,
			final int numLines)
	{
		final List<String> headerLines = new ArrayList<String>(numLines);

//		String lastLine = "";
//		int newInputPosition = 0;
//		while (headerLines.size() < numLines) {
//			final ByteBuffer shortInput = input.slice();
//			shortInput.limit(128);
//			final String contentPart = new String(OSCSerializer.toByteArray(shortInput));
//			final String[] currentLines = contentPart.split("\n");
//			currentLines[0] = lastLine + currentLines[0];
//			for (int cli = 0; (cli < (currentLines.length - 1)) && (headerLines.size() < numLines); cli++)
//			{
//				headerLines.add(currentLines[cli]);
//			}
//			lastLine = currentLines[currentLines.length - 1];
//		}
//		input.position(numLines);

		input.mark();
		final List<Integer> newLinePositions = new ArrayList<Integer>(numLines);
		while (newLinePositions.size() < numLines) {
			byte lastByte;
			do {
				lastByte = input.get();
			} while (lastByte != '\n');
			newLinePositions.add(input.position());
		}

		input.reset();
		Integer lastNewLinePos = 0;
		for (final Integer newLinePosition : newLinePositions) {
			final ByteBuffer lineInput = input.slice();
			lineInput.limit(newLinePosition - lastNewLinePos - 1);
			final String line = toString(lineInput, charset);
			headerLines.add(line);
			lastNewLinePos = newLinePosition;
		}

		return headerLines;
	}

	private static class InvalidOSCFileHeader extends IllegalStateException {

		InvalidOSCFileHeader(final String msg) {
			super("Invalid OSC file content header; " + msg);
		}
	}

	public static OSCFile parseInfo(final Path path, final ByteBuffer input, final Charset charset)
			throws IOException
	{
		final List<String> headerLines = parseHeaderLines(input, charset, 6);

		if (!headerLines.get(0).equals(OSCFile.LINE_MIME)) {
			throw new InvalidOSCFileHeader(
					"first line has to be \"" + OSCFile.LINE_MIME + '\"');
		}
		if (!headerLines.get(1).equals(OSCFile.LINE_CONTENT_TYPE)) {
			throw new InvalidOSCFileHeader(
					"second line has to be \"" + OSCFile.LINE_CONTENT_TYPE + '\"');
		}
		if (!headerLines.get(2).trim().equals(OSCFile.LINE_FRAMING)) {
			throw new InvalidOSCFileHeader(
					"third line has to be \"\t" + OSCFile.LINE_FRAMING + '\"');
		}
		if (!headerLines.get(3).trim().startsWith(OSCFile.LINE_VERSION)) {
			throw new InvalidOSCFileHeader(
					"fourth line has to start with \"\t" + OSCFile.LINE_VERSION + '\"');
		}
		if (!headerLines.get(4).trim().startsWith(OSCFile.LINE_SERVICE_URI)) {
			throw new InvalidOSCFileHeader(
					"fifth line has to start with \"\t" + OSCFile.LINE_SERVICE_URI + '\"');
		}
		if (!headerLines.get(5).trim().startsWith(OSCFile.LINE_TYPE_TAGS)) {
			throw new InvalidOSCFileHeader(
					"sixth line has to start with \"\t" + OSCFile.LINE_TYPE_TAGS + '\"');
		}

		final String version = headerLines.get(3).split("=", 2)[1].trim();
		final String serviceUriStr = headerLines.get(4).split("=", 2)[1].trim();
		final URI serviceUri = URI.create(serviceUriStr);
		final String typeTags = headerLines.get(5).split("=", 2)[1].trim();

		return new OSCFile(path, version, serviceUri, typeTags);
	}

	/**
	 * Reads a packet from the file.
	 * @param path
	 * @return the packet parsed from the file TODO
	 * @throws IOException
	 * @throws OSCParseException
 	 */
	public Map<OSCFile, OSCPacket> read(final Path path) throws IOException, OSCParseException {

		final FileChannel fileChan = FileChannel.open(path, StandardOpenOption.READ);

		buffer.rewind();
		fileChan.read(buffer);

		final Charset charset
				= (Charset) parser.getProperties().get(StringArgumentHandler.PROP_NAME_CHARSET);
		final OSCFile file = parseInfo(path, buffer, charset);

		final OSCPacket packet = parser.convert(buffer);

		return Collections.singletonMap(file, packet);
	}
}
