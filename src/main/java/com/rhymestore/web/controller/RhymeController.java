/**
 * The Rhymestore project.
 *
 * This application is free software; you can redistribute it and/or
 * modify it under the terms of the GNU LESSER GENERAL PUBLIC
 * LICENSE as published by the Free Software Foundation under
 * version 3 of the License
 *
 * This application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * LESSER GENERAL PUBLIC LICENSE v.3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package com.rhymestore.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhymestore.store.RhymeStore;
import com.rhymestore.twitter.util.TwitterUtils;

/**
 * Controller to manage stored rhymes.
 * 
 * @author Ignasi Barrera
 */
public class RhymeController extends MethodInvokingController
{
    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(RhymeController.class);

    /** The Rhyme store. */
    private RhymeStore store;

    /**
     * Default constructor
     */
    public RhymeController()
    {
        store = RhymeStore.getInstance();
    }

    /**
     * Lists all rhymes in the the store.
     * 
     * @param request The request.
     * @param response The response.
     * @throws ControllerException If the rhyme cannot be added.
     */
    public void list(final HttpServletRequest request, final HttpServletResponse response)
        throws ControllerException
    {
        try
        {
            Set<String> rhymes = store.findAll();

            List<String> sortedRhymes = new ArrayList<String>(rhymes);
            Collections.sort(sortedRhymes, String.CASE_INSENSITIVE_ORDER);

            request.setAttribute("rhymes", sortedRhymes);
        }
        catch (IOException ex)
        {
            String result = "Could get rhymes: " + ex.getMessage();
            LOGGER.error(result);
            request.setAttribute("result", result);
        }
    }

    /**
     * Adds a new rhyme to the store.
     * 
     * @param request The request.
     * @param response The response.
     * @throws ControllerException If the rhyme cannot be added.
     */
    public void add(final HttpServletRequest request, final HttpServletResponse response)
        throws ControllerException
    {
        String result = null;
        String rhyme = request.getParameter("rhyme");

        if (rhyme != null && rhyme.length() > 0)
        {
            try
            {
                String twitterUser = getTwitterUser(request, response);
                if (rhyme.contains(TwitterUtils.user(twitterUser)))
                {
                    result = "Cannot add a rhyme that contains the Twitter user name";
                }
                else
                {
                    // Capitalize rhyme
                    String capitalized = rhyme.substring(0, 1).toUpperCase() + rhyme.substring(1);
                    store.add(capitalized);

                    result = "Added rhyme: " + capitalized;
                }

                LOGGER.info(result);
            }
            catch (IOException ex)
            {
                result = "Could not add rhyme: " + ex.getMessage();
                LOGGER.error(result);
            }

            request.setAttribute("result", result);
        }
    }

    /**
     * Parses the rhyme and prepares it to be stored.
     * 
     * @param rhyme The rhyme to parse.
     * @return The rhyme prepared to be stored.
     */
    private String parseRhyme(final String rhyme)
    {
        String capitalized = rhyme.substring(0, 1).toUpperCase() + rhyme.substring(1);
    }
}
