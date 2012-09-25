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
package dk.nsi.sdm4.ydelse.common.exception;

import dk.nsi.sdm4.core.parser.ParserException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExceptionTest {
	@Test
	public void testDAOException() {
		final String thrStr = "Throwable test";
		final String exStr = "Test";
		final String ex2Str = "Test2";
		Exception thr = new Exception(thrStr);
		DAOException ex = new DAOException(exStr);
		DAOException ex2 = new DAOException(ex2Str, thr);

		assertEquals(exStr, ex.getMessage());
		assertEquals(ex2Str, ex2.getMessage());
		assertEquals(thrStr, ex2.getCause().getMessage());
	}

	@Test
	public void testHashException() {
		final String exStr = "Test";
		final String ex2Str = "Test2";
		HashException ex = new HashException(new Exception(exStr));
		HashException ex2 = new HashException(new Exception(ex2Str));

		assertEquals("java.lang.Exception: " + exStr, ex.getMessage());
		assertEquals("java.lang.Exception: " + ex2Str, ex2.getMessage());
	}

	@Test
	public void testAssertionFormatException() {
		final String thrStr = "Throwable test";
		final String exStr = "Test";
		final String ex2Str = "Test2";
		Exception thr = new Exception(thrStr);
		AssertionFormatException ex = new AssertionFormatException(exStr);
		AssertionFormatException ex2 = new AssertionFormatException(ex2Str, thr);

		assertEquals(exStr, ex.getMessage());
		assertEquals(ex2Str, ex2.getMessage());
		assertEquals(thrStr, ex2.getCause().getMessage());
	}

	@Test
	public void testAssertionParseException() {
		final String thrStr = "Throwable test";
		final String exStr = "Test";
		final String ex2Str = "Test2";
		Exception thr = new Exception(thrStr);
		AssertionParseException ex = new AssertionParseException(exStr);
		AssertionParseException ex2 = new AssertionParseException(ex2Str, thr);

		assertEquals(exStr, ex.getMessage());
		assertEquals(ex2Str, ex2.getMessage());
		assertEquals(thrStr, ex2.getCause().getMessage());
	}

	@Test
	public void testRelationTypeMismatchException() {
		final String thrStr = "Throwable test";
		final String exStr = "Test";
		final String ex2Str = "Test2";
		Exception thr = new Exception(thrStr);
		RelationTypeMismatchException ex = new RelationTypeMismatchException(exStr);
		RelationTypeMismatchException ex2 = new RelationTypeMismatchException(ex2Str, thr);

		assertEquals(exStr, ex.getMessage());
		assertEquals(ex2Str, ex2.getMessage());
		assertEquals(thrStr, ex2.getCause().getMessage());
	}

	@Test
	public void testParserException() {
		final String thrStr = "Throwable test";
		final String exStr = "Test";
		final String ex2Str = "Test2";
		Exception thr = new Exception(thrStr);
		ParserException ex = new ParserException(exStr);
		ParserException ex2 = new ParserException(ex2Str, thr);

		assertEquals(exStr, ex.getMessage());
		assertEquals(ex2Str, ex2.getMessage());
		assertEquals(thrStr, ex2.getCause().getMessage());
	}

	@Test
	public void testPropertySupportException() {
		final String thrStr = "Throwable test";
		final String exStr = "Test";
		final String ex2Str = "Test2";
		Exception thr = new Exception(thrStr);
		PropertySupportException ex = new PropertySupportException(exStr);
		PropertySupportException ex2 = new PropertySupportException(ex2Str, thr);

		assertEquals(exStr, ex.getMessage());
		assertEquals(ex2Str, ex2.getMessage());
		assertEquals(thrStr, ex2.getCause().getMessage());
	}

	@Test
	public void testRegisterImportException() {
		final String thrStr = "Throwable test";
		final String exStr = "Test";
		final String ex2Str = "Test2";
		Exception thr = new Exception(thrStr);
		RegisterImportException ex = new RegisterImportException(exStr);
		RegisterImportException ex2 = new RegisterImportException(ex2Str, thr);

		assertEquals(exStr, ex.getMessage());
		assertEquals(ex2Str, ex2.getMessage());
		assertEquals(thrStr, ex2.getCause().getMessage());
	}
}
