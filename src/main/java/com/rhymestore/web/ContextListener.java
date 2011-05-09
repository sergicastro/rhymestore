/**
 * Copyright (c) 2010 Enric Ruiz, Ignasi Barrera
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.rhymestore.web;

import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import com.rhymestore.config.Configuration;
import com.rhymestore.store.RhymeLoader;
import com.rhymestore.twitter.TwitterScheduler;
import com.rhymestore.util.SSLUtils;

/**
 * Initializes and shuts down the twitter scheduler.
 * 
 * @author Ignasi Barrera
 */
public class ContextListener implements ServletContextListener
{
	/** The logger. */
	private static final Logger LOGGER = LoggerFactory
	.getLogger(ContextListener.class);

	/** Context attribute name used to store the Twitter user. */
	public static final String TWITTER_USER_NAME = "TWITTER_USER_NAME";

	/** Context parameter name used to enable or disable twitter communication. */
	private static final String TWITTER_ENABLE_PARAM_NAME = "TWITTER_ENABLED";

	/** The Twitter API call scheduler. */
	private TwitterScheduler twitterScheduler;

	/** The Twitter API client. */
	private Twitter twitter;

	@Override
	public void contextDestroyed(final ServletContextEvent sce)
	{
		if (this.twitterEnabled(sce))
		{
			ContextListener.LOGGER
			.info("Shutting down the Twitter API scheduler");

			this.twitterScheduler.shutdown(); // Stop scheduler
		}

		ContextListener.LOGGER.info("Disconnecting from Twitter");

		this.twitter.shutdown(); // Disconnect from Twitter
	}

	@Override
	public void contextInitialized(final ServletContextEvent sce)
	{
		// Starts the Twitter scheduler
		if (this.twitterEnabled(sce))
		{
			// Connects to Twitter
			this.twitter = new TwitterFactory().getInstance();

			try
			{
				ContextListener.LOGGER.info("Connected to Twitter as: {}",
						this.twitter.getScreenName());

				// Store the user name in the servlet context to make it
				// available
				// to Controllers
				sce.getServletContext().setAttribute(
						ContextListener.TWITTER_USER_NAME,
						this.twitter.getScreenName());
			}
			catch (TwitterException ex)
			{
				ContextListener.LOGGER.error(
						"Could not get the Twitter username", ex);
			}

			ContextListener.LOGGER.info("Starting the Twitter API scheduler");

			this.twitterScheduler = new TwitterScheduler(this.twitter);
			this.twitterScheduler.start();
		}
		else
		{
			ContextListener.LOGGER.info("Twitter communication is disabled");
		}

		// Load the default rhymes
		this.loadDefaultRhymes();
	}

	/**
	 * Load the default rhymes, if the URI is defined.
	 */
	private void loadDefaultRhymes()
	{
		// Load the rhymes URI
		String rhymesURI = Configuration
		.getConfigValue(Configuration.DEFAULT_RHYMES_URI_PROPERTY);

		if (rhymesURI != null)
		{
			ContextListener.LOGGER.info("Adding rhymes from: {}", rhymesURI);

			try
			{
				// Ensure there won't be SSL certificate issues
				SSLUtils.installIgnoreCertTrustManager();
				URL url = new URL(rhymesURI);
				URLConnection conn = url.openConnection();

				// Load the rhymes from the configured URI
				new RhymeLoader().load(conn.getInputStream());
			}
			catch (Exception ex)
			{
				ContextListener.LOGGER
				.error("Could not load the default rhymes: "
						+ ex.getMessage(), ex);
			}

		}
	}

	/**
	 * Checks if Twitter communication is enabled.
	 * 
	 * @param sce The <code>ServletContextEvent</code>.
	 * @return A boolean indicating if Twitter communication is enabled.
	 */
	private boolean twitterEnabled(final ServletContextEvent sce)
	{
		String enableTwitter = sce.getServletContext().getInitParameter(
				ContextListener.TWITTER_ENABLE_PARAM_NAME);
		return (enableTwitter == null) || enableTwitter.equals("true");
	}

}
