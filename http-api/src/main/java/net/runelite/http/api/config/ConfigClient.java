/*
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.http.api.config;

import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;
import net.runelite.http.api.RuneliteAPI;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigClient
{
	private static final Logger logger = LoggerFactory.getLogger(ConfigClient.class);

	private static final MediaType TEXT_PLAIN = MediaType.parse("text/plain");

	private final UUID uuid;

	public ConfigClient(UUID uuid)
	{
		this.uuid = uuid;
	}

	public Configuration get() throws IOException
	{
		HttpUrl url = RuneliteAPI.getApiBase().newBuilder()
			.addPathSegment("config")
			.build();

		logger.debug("Built URI: {}", url);

		Request request = new Request.Builder()
			.header(RuneliteAPI.RUNELITE_AUTH, uuid.toString())
			.url(url)
			.build();

		Response response = RuneliteAPI.CLIENT.newCall(request).execute();

		try (ResponseBody body = response.body())
		{
			InputStream in = body.byteStream();
			return RuneliteAPI.GSON.fromJson(new InputStreamReader(in), Configuration.class);
		}
		catch (JsonParseException ex)
		{
			throw new IOException(ex);
		}
	}

	public void set(String key, String value) throws IOException
	{
		HttpUrl url = RuneliteAPI.getApiBase().newBuilder()
			.addPathSegment("config")
			.addPathSegment(key)
			.build();

		logger.debug("Built URI: {}", url);

		Request request = new Request.Builder()
			.put(RequestBody.create(TEXT_PLAIN, value))
			.header(RuneliteAPI.RUNELITE_AUTH, uuid.toString())
			.url(url)
			.build();

		try (Response response = RuneliteAPI.CLIENT.newCall(request).execute())
		{
			logger.debug("Set configuration value '{}' to '{}'", key, value);
		}
	}

	public void unset(String key) throws IOException
	{
		HttpUrl url = RuneliteAPI.getApiBase().newBuilder()
			.addPathSegment("config")
			.addPathSegment(key)
			.build();

		logger.debug("Built URI: {}", url);

		Request request = new Request.Builder()
			.delete()
			.header(RuneliteAPI.RUNELITE_AUTH, uuid.toString())
			.url(url)
			.build();

		try (Response response = RuneliteAPI.CLIENT.newCall(request).execute())
		{
			logger.debug("Unset configuration value '{}'", key);
		}
	}
}
