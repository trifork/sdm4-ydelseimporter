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
package dk.nsi.sdm4.ydelse.common.splunk;

import dk.nsi.sdm4.ydelse.common.splunk.SplunkLogEntry.LogLevel;
import org.apache.log4j.Logger;

public class SplunkLogger {

	private Logger auditLog;
	private Logger errorAndDebugLog;

	private static final String AUDIT_LOG_PREFIX = "audit.";

	public SplunkLogger(Class<?> clazz) {
		auditLog = Logger.getLogger(AUDIT_LOG_PREFIX + clazz.getName());
		errorAndDebugLog = Logger.getLogger(clazz.getName());
	}

	public boolean isDebugEnabled() {
		return errorAndDebugLog.isDebugEnabled();
	}

	public SplunkLogEntry getInfoSplunkEntry(String message) {
		return new SplunkLogEntry(auditLog, LogLevel.INFO, message);
	}

	/**
	 * Audit logs info level. Assumes an even number of parameters according to
	 * key value pairs.
	 * 
	 */
	public void info(String message, String... parameters) {
		SplunkLogEntry logEntry = getInfoSplunkEntry(message);
		log(logEntry, parameters);
	}

	/**
	 * Logs debug level to normal (not audit) logs Assumes an even number of
	 * parameters according to key value pairs.
	 * 
	 */
	public void debug(String message, String... parameters) {
		SplunkLogEntry logEntry = new SplunkLogEntry(errorAndDebugLog, LogLevel.DEBUG, message);
		log(logEntry, parameters);
	}

	/**
	 * Logs error level to normal (not audit) logs Assumes an even number of
	 * parameters according to key value pairs.
	 * 
	 */
	public void error(String message, String... parameters) {
		SplunkLogEntry logEntry = new SplunkLogEntry(errorAndDebugLog, LogLevel.ERROR, message);
		log(logEntry, parameters);
	}

	/**
	 * Logs error level to normal (not audit) logs Assumes an even number of
	 * parameters according to key value pairs.
	 * 
	 */
	public void error(Throwable t, String message, String... parameters) {
		SplunkLogEntry logEntry = new SplunkLogEntry(errorAndDebugLog, LogLevel.ERROR, message, t);
		log(logEntry, parameters);
	}

	/**
	 * Logs error level to normal (not audit) logs Assumes an even number of
	 * parameters according to key value pairs.
	 * 
	 */
	public void error(Throwable t) {
		error(t, t.getMessage());
	}

	private void log(SplunkLogEntry logEntry, String... parameters) {
		if (parameters.length % 2 != 0) {
			throw new IllegalArgumentException(
					"Splunk log entry must contain an even number of parameters according to key value pairs");
		} else {
			for (int i = 0; i < parameters.length; i += 2) {
				String name = parameters[i];
				String value = parameters[i + 1];
				logEntry.addParameter(name, value);
			}
			logEntry.commit();
		}
	}
}
