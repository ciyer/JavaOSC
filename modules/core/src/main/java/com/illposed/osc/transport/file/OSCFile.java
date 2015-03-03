/*
 * Copyright (C) 2015, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.transport.file;

import java.net.URI;
import java.nio.file.Path;

/**
 * Contains all the meta-info about an OSC file, as described in the OSC specification 1.1.
 */
public class OSCFile {

	public static final String LINE_MIME = "MIME-Version: 1.0";
	public static final String LINE_CONTENT_TYPE = "Content-type: application/osc;";
	public static final String LINE_FRAMING = "framing=slip";
	public static final String LINE_VERSION = "version=";
	public static final String LINE_SERVICE_URI = "uri=";
	public static final String LINE_TYPE_TAGS = "types=";

//	private final File file;
	private final Path path;
	private final String version;
	private final URI serviceUri;
	private final CharSequence typeTags;

	/**
	 * Creates a new OSC file.
	 * @param path location of the file
	 * @param version OSC version support; 1.0 or 1.1
	 * @param serviceUri to identify the service running behind the endpoint
	 *   or the source that generated the data stream
	 * @param typeTags contains all the type-tag symbols
	 *   supported by the endpoint/present in the stream
	 */
	public OSCFile(
			final Path path,
			final String version,
			final URI serviceUri,
			final CharSequence typeTags)
	{
//		this.file = file;
		this.path = path;
		this.version = version;
		this.serviceUri = serviceUri;
		this.typeTags = typeTags;
	}

	public Path getPath() {
		return path;
	}

	public String getVersion() {
		return version;
	}

	public URI getServiceUri() {
		return serviceUri;
	}

	public CharSequence getTypeTags() {
		return typeTags;
	}
}
