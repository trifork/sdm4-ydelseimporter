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
package dk.nsi.sdm4.lpr.common.splunk;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SplunkLoggerTest {

	private SplunkLogger log;

	@Before
	public void before() {
		log = new SplunkLogger(this.getClass());
	}

	@Test
	public void testLogInfo() {
		log.info("Info without parameters");
		log.info("Info with parameters", "param1", "wuptiduudle", "param2", "foo");

		try {
			log.info("Info with uneven number of params", "foo", "bar", "baz");
		} catch (IllegalArgumentException e) {
			assertEquals("Splunk log entry must contain an even number of parameters according to key value pairs",
					e.getMessage());
		}
	}

	@Test
	public void testLogDebug() {
		log.debug("Debug without parameters");
		log.debug("Debug with parameters", "param1", "wuptiduudle", "param2", "foo");

		try {
			log.debug("Debug with uneven number of params", "foo", "bar", "baz");
		} catch (IllegalArgumentException e) {
			assertEquals("Splunk log entry must contain an even number of parameters according to key value pairs",
					e.getMessage());
		}
	}

	@Test
	public void testLogError() {
		log.error("Error without parameters");
		log.error("Error with parameters", "param1", "wuptiduudle", "param2", "foo");

		try {
			log.error("Error with uneven number of params", "foo", "bar", "baz");
		} catch (IllegalArgumentException e) {
			assertEquals("Splunk log entry must contain an even number of parameters according to key value pairs",
					e.getMessage());
		}
	}

	@Test
	public void testLogErrorWithMessageAndThrowable() {
		log.error(new RuntimeException(), "Error with message and throwable");
		log.error(new RuntimeException(), "Error with message, throwable and parameters", "foo", "bar");

		try {
			log.error(new RuntimeException(), "Error with message and throwable but with uneven number of params",
					"foo", "bar", "baz");
		} catch (IllegalArgumentException e) {
			assertEquals("Splunk log entry must contain an even number of parameters according to key value pairs",
					e.getMessage());
		}
	}

	@Test
	public void testLogErrorOnlyThrowable() {
		log.error(new RuntimeException("Error that just has a throwable"));
	}
}
