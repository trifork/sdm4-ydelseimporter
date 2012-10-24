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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class SplunkLogEntry {

	public enum LogLevel {
		INFO, WARN, DEBUG, ERROR
	}

	private Logger logger;
	private LogLevel logLevel;
	private List<String> parameters;
	private Throwable throwable;

	SplunkLogEntry(Logger logger, LogLevel logLevel, String message) {
		this.logger = logger;
		this.logLevel = logLevel;
		parameters = new ArrayList<String>();
		addParameter("msg", message);
		throwable = null;
	}

	SplunkLogEntry(Logger logger, LogLevel logLevel, String message, Throwable throwable) {
		this(logger, logLevel, message);
		this.throwable = throwable;
		addParameter("error", throwable.getMessage());
	}

	public void addParameter(String name, String value) {
		parameters.add(name + "=" + value);
	}

	private String getLogEntry() {
		return StringUtils.join(parameters, ", ");
	}

	public void commit() {
		if (throwable == null) {
			if (logLevel == LogLevel.INFO) {
				logger.info(getLogEntry());
			} else if (logLevel == LogLevel.WARN) {
				logger.warn(getLogEntry());
			} else if (logLevel == LogLevel.DEBUG) {
				logger.debug(getLogEntry());
			} else if (logLevel == LogLevel.ERROR) {
				logger.error(getLogEntry());
			}
		} else {
			if (logLevel == LogLevel.INFO) {
				logger.info(getLogEntry(), throwable);
			} else if (logLevel == LogLevel.WARN) {
				logger.warn(getLogEntry(), throwable);
			} else if (logLevel == LogLevel.DEBUG) {
				logger.debug(getLogEntry(), throwable);
			} else if (logLevel == LogLevel.ERROR) {
				logger.error(getLogEntry(), throwable);
			}
		}
	}
}
