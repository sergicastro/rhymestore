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

package com.rhymestore.twitter.commands;

import java.io.IOException;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.rhymestore.store.RhymeStore;
import com.rhymestore.twitter.TwitterScheduler;
import com.rhymestore.twitter.util.TwitterUtils;

/**
 * Executes a reply to a user's tweet.
 * 
 * @author Ignasi Barrera
 * @see Twitter
 * @see TwitterScheduler
 */
public class ReplyCommand implements TwitterCommand
{
	/** The logger. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ReplyCommand.class);

	/** The queue with the pending commands. */
	private final Queue<TwitterCommand> commandQueue;

	/** The status to reply. */
	private final Status status;

	/** The Rhyme Store. */
	private final RhymeStore rhymeStore;

	/**
	 * Creates a new {@link ReplyCommand} for the given status.
	 * 
	 * @param status The status to reply.
	 * @param commandQueue The queue with the pending commands.
	 */
	public ReplyCommand(final Status status,
			final Queue<TwitterCommand> commandQueue)
	{
		super();
		this.status = status;
		this.commandQueue = commandQueue;
		this.rhymeStore = RhymeStore.getInstance();
	}

	/**
	 * We should discuss whether the rhyme with the name should be performed in
	 * the #com.rhymestore.store.RhymeStore.getRhyme(String) by adding an
	 * additional parameters (varargs?) or use Exceptions. (non-Javadoc)
	 * 
	 * @see com.rhymestore.twitter.commands.TwitterCommand#execute(twitter4j.Twitter)
	 */
	@Override
	public void execute(final Twitter twitter) throws TwitterException
	{
		String rhyme = null;

		try
		{
			rhyme = this.rhymeStore.getRhyme(this.status.getText());
			// No rhyme Exception flux
			if ("Patada en los cojones".equalsIgnoreCase(rhyme))
			{
				// rhyme the name!
				ReplyCommand.LOGGER.debug("with the name");
				rhyme = this.rhymeStore.getRhyme(this.status.getUser()
						.getScreenName());
			}
		}
		catch (IOException ex)
		{
			ReplyCommand.LOGGER
					.error("An error occured while connecting to the rhyme store. Could not reply to {}",
							this.status.getUser().getScreenName(), ex);
		}

		try
		{
			String tweet = TwitterUtils.reply(this.status.getUser()
					.getScreenName(), rhyme);

			ReplyCommand.LOGGER.info("Replying to {} with: {}", this.status
					.getUser().getScreenName(), tweet);

			// Reply in the timeline and DM to the user
			StatusUpdate newStatus = new StatusUpdate(tweet);
			newStatus.setInReplyToStatusId(this.status.getId());
			twitter.updateStatus(newStatus);
		}
		catch (TwitterException ex)
		{
			ReplyCommand.LOGGER.error("Could not send reply to tweet "
					+ this.status.getId(), ex);

			// If it is not a duplicate tweet, enqueue the API call again, to
			// retry it later
			if (!TwitterUtils.isDuplicateTweetError(ex))
			{
				ReplyCommand.LOGGER
						.debug("Enqueuing the reply to try again later...");

				this.commandQueue.add(this);
			}
		}
	}
}
