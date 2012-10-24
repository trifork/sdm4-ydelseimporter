/**
 * The MIT License
 *
 * Original work sponsored and donated by National Board of e-Health (NSI), Denmark
 * (http://www.nsi.dk)
 *
 * Copyright (C) 2011 National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dk.nsi.sdm4.ydelse.relation.model;

import dk.nsi.sdm4.ydelse.common.exception.HashException;
import dk.nsi.sdm4.ydelse.common.util.CprUtil;
import dk.nsi.sdm4.ydelse.common.util.Hasher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HashedCpr {

	private static final int UNHASHED_STRING_LENGTH = 10;
	private static final int HASHED_STRING_LENGTH = 40;
	private static final Pattern HASHED_CPR_PATTERN = Pattern.compile("[0-9A-F]*");

	public static HashedCpr buildFromHashedString(String hashedString) {
		if (hashedString == null) {
			throw new IllegalArgumentException("Null-string not legeal");
		}

		if (hashedString.length() != HASHED_STRING_LENGTH) {
			throw new IllegalArgumentException("Illegal length of hashed cpr number, should be exactly "
					+ HASHED_STRING_LENGTH + ": " + hashedString);
		}

		Matcher matcher = HASHED_CPR_PATTERN.matcher(hashedString);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("Illegal chars in hashed cpr number, only numbers and capital A to Fs: "
					+ hashedString);
		}

		return new HashedCpr(hashedString);
	}

	public static HashedCpr buildFromUnhashedString(String unhashedString) {
		if (unhashedString == null) {
			throw new IllegalArgumentException("Null-string not legeal");
		}

		if (unhashedString.length() != UNHASHED_STRING_LENGTH) {
			throw new IllegalArgumentException("Illegal length of unhashed cpr number, should be exactly "
					+ UNHASHED_STRING_LENGTH + ": " + unhashedString);
		}

		if (!CprUtil.validateCpr(unhashedString)) {
			throw new IllegalArgumentException("Illegal chars in unhashed cpr number, only numbers: " + unhashedString);
		}

		try {
			return new HashedCpr(Hasher.hash(unhashedString));
		} catch (HashException e) {
			throw new RuntimeException(e);
		}
	}

	private String hashedCpr;

	private HashedCpr(String hashedCpr) {
		this.hashedCpr = hashedCpr;
	}

	public String getHashedCpr() {
		return hashedCpr;
	}

	@Override
	public String toString() {
		return hashedCpr;
	}

	@Override
	public int hashCode() {
		return hashedCpr.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HashedCpr other = (HashedCpr) obj;
		if (hashedCpr == null) {
			if (other.hashedCpr != null)
				return false;
		} else if (!hashedCpr.equals(other.hashedCpr))
			return false;
		return true;
	}
}
