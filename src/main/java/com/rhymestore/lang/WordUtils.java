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

package com.rhymestore.lang;

import java.util.Arrays;
import java.util.List;

import com.rhymestore.lang.SpanishWordParser.SpanishNumber;

/**
 * Utility methods to manipulate text.
 * 
 * @author Ignasi Barrera
 */
public class WordUtils {
	/**
	 * Capitalizes the given String.
	 * 
	 * @param str
	 *            The String to capitalize.
	 * @return The capitalized String.
	 */
	public static String capitalize(final String str) {
		switch (str.length()) {
		case 0:
			return str;
		case 1:
			return str.toUpperCase();
		default:
			return str.substring(0, 1).toUpperCase() + str.substring(1);
		}
	}

	/**
	 * Gets the last word of the given sentence.
	 * 
	 * @param sentence
	 *            The sentence to parse.
	 * @return The last word of the given sentence.
	 */
	public static String getLastWord(final String sentence) {
		String last = null;

		if (sentence != null) {
			List<String> words = Arrays.asList(sentence.split(" "));

			for (String word : words) {
				if (WordUtils.isWord(word)) {
					last = word;
					break;
				}
				if (WordUtils.isNumber(word)) {
					String sound = SpanishNumber.getBaseSound(word);

					if (sound != null) {

						last = sound;// ()
						break;
					}
				}
			}
		}
		return last;
	}

	public static boolean isNumber(String word) {
		char[] letters = SpanishWordParser.removeTrailingPunctuation(word)
				.toCharArray();
		for (char c : letters) {
			if (!Character.isDigit(c)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isWord(String word) {

		char[] letters = SpanishWordParser.removeTrailingPunctuation(word)
				.toCharArray();
		for (char c : letters) {
			if (!SpanishWordParser.isLetter(c)) {
				return false;
			}
		}
		return true;
	}
}
