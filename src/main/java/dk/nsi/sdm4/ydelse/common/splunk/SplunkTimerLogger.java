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

import org.joda.time.DateTime;

public class SplunkTimerLogger {

    public static SplunkTimerLogger createAndStart(SplunkLogger splunkLogger, String message) {
        SplunkTimerLogger logger = new SplunkTimerLogger(splunkLogger, message);
        logger.start();
        return logger;
    }

    SplunkLogger splunkLogger;
    String message;
    DateTime start, stop, previousStep;

    public SplunkTimerLogger(SplunkLogger splunkLogger, String message) {
        this.splunkLogger = splunkLogger;
        this.message = message;
    }

    public void start() {
        if (start == null) {
            if (splunkLogger.isDebugEnabled()) {
                start = new DateTime();
                previousStep = start;
                splunkLogger.debug(message, "stepDescription", "--- start ---");
            }
        } else {
            throw new IllegalArgumentException("You can only invocate start once for each timer");
        }
    }

    public void step(String stepDescription) {
        if (splunkLogger.isDebugEnabled()) {
            DateTime stepTime = new DateTime();
            splunkLogger.debug(message, "stepDescription", stepDescription, "stepDuration",
                    Long.toString(stepTime.getMillis() - previousStep.getMillis()));
            previousStep = stepTime;
        }
    }

    public void stop() {
        if (stop == null) {
            if (splunkLogger.isDebugEnabled()) {
                stop = new DateTime();
                long duration = stop.getMillis() - start.getMillis();
                splunkLogger.debug(message, "stepDescription", "--- end ---", "durationInMillis", Long.toString(duration));
            }
        } else {
            throw new IllegalArgumentException("You can only invocate stop once for each timer");
        }
    }
}
