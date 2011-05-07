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

import java.io.File;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(ContextListener.class);

    /** Context attribute name used to store the Twitter user. */
    public static final String TWITTER_USER_NAME = "TWITTER_USER_NAME";

    /** Context parameter name used to enable or disable twitter communication. */
    private static final String TWITTER_ENABLE_PARAM_NAME = "TWITTER_ENABLED";

    /** The Twitter API call scheduler. */
    private TwitterScheduler twitterScheduler;

    /** The Twitter API client. */
    private Twitter twitter;

    @Override
    public void contextInitialized(final ServletContextEvent sce)
    {
        // Starts the Twitter scheduler
        if (twitterEnabled(sce))
        {
            // Connects to Twitter
            twitter = new TwitterFactory().getInstance();

            try
            {
                LOGGER.info("Connected to Twitter as: {}", twitter.getScreenName());

                // Store the user name in the servlet context to make it available
                // to Controllers
                sce.getServletContext().setAttribute(TWITTER_USER_NAME, twitter.getScreenName());
            }
            catch (TwitterException ex)
            {
                LOGGER.error("Could not get the Twitter username", ex);
            }

            LOGGER.info("Starting the Twitter API scheduler");

            twitterScheduler = new TwitterScheduler(twitter);
            twitterScheduler.start();
        }
        else
        {
            LOGGER.info("Twitter communication is disabled");
        }

        // Load the default rhymes
        loadDefaultRhymes();
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce)
    {
        if (twitterEnabled(sce))
        {
            LOGGER.info("Shutting down the Twitter API scheduler");

            twitterScheduler.shutdown(); // Stop scheduler
        }

        LOGGER.info("Disconnecting from Twitter");

        twitter.shutdown(); // Disconnect from Twitter
    }

    /**
     * Checks if Twitter communication is enabled.
     * 
     * @param sce The <code>ServletContextEvent</code>.
     * @return A boolean indicating if Twitter communication is enabled.
     */
    private boolean twitterEnabled(final ServletContextEvent sce)
    {
        String enableTwitter = sce.getServletContext().getInitParameter(TWITTER_ENABLE_PARAM_NAME);
        return enableTwitter == null || enableTwitter.equals("true");
    }

    /**
     * Load the default rhymes, if the URI is defined.
     */
    private void loadDefaultRhymes()
    {
        // Load the rhymes URI
        String rhymesURI = Configuration.getConfigValue(Configuration.DEFAULT_RHYMES_URI_PROPERTY);

        if (rhymesURI != null)
        {
            LOGGER.info("Adding rhymes from: {}", rhymesURI);

            try
            {
                // Ensure there won't be SSL certificate issues
				// SSLUtils.installIgnoreCertTrustManager();
				// URL url = new URL(rhymesURI);
				// URLConnection conn = url.openConnection();

				File rhymes = new File(rhymesURI);
				// Load the rhymes from the configured URI
				// new RhymeLoader().load(conn.getInputStream());
				new RhymeLoader().load(rhymes);
            }
            catch (Exception ex)
            {
                LOGGER.error("Could not load the default rhymes: " + ex.getMessage(), ex);
            }

        }
    }

}
