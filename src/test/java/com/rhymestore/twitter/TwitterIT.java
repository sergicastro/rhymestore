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

package com.rhymestore.twitter;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;

/**
 * Twitter API client integration tests.
 * 
 * @author Ignasi Barrera
 */
public class TwitterIT
{
	/** The twitter user name. */
	private static final String TWITTER_USER_NAME = "telorimo";

	/** The Twitter API client. */
	private Twitter twitter;

	@BeforeMethod
	public void setUp()
	{
		this.twitter = new TwitterFactory().getInstance();
	}

	@AfterMethod
	public void tearDown()
	{
		this.twitter.shutdown();
	}

	@Test
	public void testTwitterConnect() throws Exception
	{
		User user = this.twitter.verifyCredentials();
		Assert.assertEquals(TwitterIT.TWITTER_USER_NAME, user.getScreenName());
	}
}
