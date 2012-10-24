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
package dk.nsi.sdm4.lpr.relation.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class HashedCprFromHashedStringTester {

	@Test
	public void succesfullTest() {
		String hashedString = "665FD7A5B7EFE0E62CBF7CE1BBD1EC0D400C7B30";
		HashedCpr hashedCpr = HashedCpr.buildFromHashedString(hashedString);
		assertEquals(hashedString, hashedCpr.getHashedCpr());
	}

	@Test
	public void tooShortTest() {
		String shortString = "665FD7A5B7EFE0E62CBF7CE1BBD1EC0D400C7B3";
		try {
			HashedCpr.buildFromHashedString(shortString);
			fail("Should throw exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Illegal length of hashed cpr number, should be exactly 40: " + shortString, e.getMessage());
		}
	}

	@Test
	public void illegalCharsTest() {
		String illegalCharsString = "665FD7A5B7EFE0E62CBF7CE1BBD1EC0D400C7X30";
		try {
			HashedCpr.buildFromHashedString(illegalCharsString);
			fail("Should throw exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Illegal chars in hashed cpr number, only numbers and capital A to Fs: " + illegalCharsString,
					e.getMessage());
		}
	}

	@Test
	public void illegalSmallCharsTest() {
		String illegalCharsString = "665fd7a5b7efe0e62cbf7ce1bbd1ec0d400c7b30";
		try {
			HashedCpr.buildFromHashedString(illegalCharsString);
			fail("Should throw exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Illegal chars in hashed cpr number, only numbers and capital A to Fs: " + illegalCharsString,
					e.getMessage());
		}
	}

	@Test
	public void nullTest() {
		try {
			HashedCpr.buildFromHashedString(null);
			fail("Should throw exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Null-string not legeal", e.getMessage());
		}
	}
}
