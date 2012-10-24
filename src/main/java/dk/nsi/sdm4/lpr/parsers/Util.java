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
package dk.nsi.sdm4.lpr.parsers;

import dk.nsi.sdm4.core.parser.ParserException;
import org.apache.commons.lang.time.DateFormatUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Util {

    public static String[] getTokens(String line, String separator, int expectedNumberOfFields) throws ParserException {
        if (line.contains(System.getProperty("line.separator"))) {
            throw new ParserException("Line contains new-line character");
        }

        int asManyTimesAsPossible = -1;
        String[] tokens = line.split(separator, asManyTimesAsPossible);

        if (tokens.length < expectedNumberOfFields) {
            throw new ParserException("Too few fields on line: " + line);
        }

        if (tokens.length > expectedNumberOfFields) {
            throw new ParserException("Too many fields on line: " + line);
        }
        return tokens;
    }

    public static Interval parseAdmittedIntervalAsSpecifiedByLpr(String[] fields, int relationStartTimeField, int relationEndTimeField)
            throws ParserException {
        if (fieldIsMissing(fields, relationStartTimeField)) {
            throw new ParserException("Relation start time must be present");
        }
        
        if (fieldIsMissing(fields, relationEndTimeField)) {
            throw new ParserException("Relation end time must be present");
        }
        
        DateTime admittedStart;
        try {
            admittedStart = parseDateTimeAsSpecifiedByLpr(fields[relationStartTimeField]);
        } catch (ParseException e) {
            throw new ParserException("Relation start time is malformed: " + fields[relationStartTimeField]);
        }
        
        DateTime admittedEnd;
        try {
            admittedEnd = parseDateTimeAsSpecifiedByLpr(fields[relationEndTimeField]);
        } catch (ParseException e) {
            throw new ParserException("Relation end time is malformed: " + fields[relationEndTimeField]);
        }
        
        if (admittedEnd.isBefore(admittedStart)) {
            throw new ParserException("Relation end time must be after relation start time");
        }
        
        return new Interval(admittedStart, admittedEnd);
    }
    
    public static DateTime parseRelationStartTimeAsSpecifiedByLpr(String[] fields, int relationStartTimeField)
            throws ParserException {
        if (fieldIsMissing(fields, relationStartTimeField)) {
            throw new ParserException("Relation start time must be present");
        }
        
        DateTime admittedStart;
        try {
            admittedStart = parseDateTimeAsSpecifiedByLpr(fields[relationStartTimeField]);
        } catch (ParseException e) {
            throw new ParserException("Relation start time is malformed: " + fields[relationStartTimeField]);
        }
        
        return admittedStart;
    }

    private static boolean fieldIsMissing(String[] fields, int i) {
        return fields[i].equals("");
    }

    private final static SimpleDateFormat format = new SimpleDateFormat(
            DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern());
    static {
        format.setLenient(false);
    }

    private final static SimpleDateFormat ssrFormat = new SimpleDateFormat("yyyyMMdd");
    static {
        ssrFormat.setLenient(false);
    }

    private final static SimpleDateFormat lprFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    static {
        lprFormat.setLenient(false);
    }
    
    private static DateTime parseDateTimeAsSpecifiedByLpr(String s) throws ParseException {
        return new DateTime(lprFormat.parse(s));
    }
}
