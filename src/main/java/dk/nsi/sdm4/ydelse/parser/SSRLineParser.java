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
package dk.nsi.sdm4.ydelse.parser;

import dk.nsi.sdm4.core.parser.ParserException;
import dk.nsi.sdm4.ydelse.common.splunk.SplunkLogger;
import dk.nsi.sdm4.ydelse.relation.model.DoctorOrganisationIdentifier;
import dk.nsi.sdm4.ydelse.relation.model.HashedCpr;
import dk.nsi.sdm4.ydelse.relation.model.SSR;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class SSRLineParser {
	private static final SplunkLogger log = new SplunkLogger(SSRLineParser.class);

	private static final int EXPECTED_NUMBER_OF_FIELDS = 5;
	private static final String SEPARATOR = ",";
	private String[] fields;

	public static SsrAction parseLine(String line) throws ParserException {
		SSRLineParser parser = new SSRLineParser(line);
		return parser.parse();
	}

	private SSRLineParser(String line) throws ParserException {
		this.fields = getTokens(line, SEPARATOR, EXPECTED_NUMBER_OF_FIELDS);

		// strip whitespaces - the data from CSC can contain a varying amount of whitespaces
		for (int i = 0; i < EXPECTED_NUMBER_OF_FIELDS; i++) {
			this.fields[i] = this.fields[i].trim();
		}
	}

	private DoctorOrganisationIdentifier doctorOrganisationId;
	private String patientCpr;
	private Interval admittedInterval;
	private String ssrReference;

	private SsrAction parse() throws ParserException {
		if (everythingButExternalReferenceIsBlank()) {
			parseSsrReference();
			return SsrAction.createDeletion(ssrReference);
		} else if (!parseTreatmentInterval()) { // unfortunately CSC does not always add the dates, it has been decided (NSPSUPPORT-96) that we will ignore the fields with no date
			return SsrAction.createNOOP();
		} else {
			parseDoctorOrganisationId();
			parsePatientCpr();
			parseSsrReference();

			SSR ssr = SSR.createInstance(HashedCpr.buildFromHashedString(patientCpr), doctorOrganisationId,
					admittedInterval, ssrReference);
			return SsrAction.createInsertion(ssr);
		}
	}

	private static final int DOCTOR_ORG_ID_FIELD = 0;
	private static final int PATIENT_CPR_FIELD = 1;
	private static final int TREATMENT_START_TIME_FIELD = 2;
	private static final int TREATMENT_END_TIME_FIELD = 3;
	private static final int SSR_REFERENCE_FIELD = 4;

	private boolean fieldIsMissing(int i) {
		return fields[i].trim().equals("");
	}

	private boolean everythingButExternalReferenceIsBlank() {
		return fieldIsMissing(DOCTOR_ORG_ID_FIELD) && fieldIsMissing(PATIENT_CPR_FIELD)
				&& fieldIsMissing(TREATMENT_START_TIME_FIELD) && fieldIsMissing(TREATMENT_END_TIME_FIELD)
				&& !fieldIsMissing(SSR_REFERENCE_FIELD);
	}

	private void parseDoctorOrganisationId() throws ParserException {
		if (fieldIsMissing(DOCTOR_ORG_ID_FIELD)) {
			throw new ParserException("Doctor organisation id (ydernummer) must be present");
		} else {
			try {
				doctorOrganisationId = DoctorOrganisationIdentifier.newInstance(fields[DOCTOR_ORG_ID_FIELD]);
			} catch (IllegalArgumentException e) {
				throw new ParserException(e.getMessage(), e);
			}
		}
	}

	private void parsePatientCpr() throws ParserException {
		if (fieldIsMissing(PATIENT_CPR_FIELD)) {
			throw new ParserException("Patient cpr must be present");
		} else {
			patientCpr = fields[PATIENT_CPR_FIELD];
		}
	}

	private boolean parseTreatmentInterval() throws ParserException {
		try {
			admittedInterval = parseIntervalFromTwoIdenticalDaysAsSpecifiedBySsr(fields, TREATMENT_START_TIME_FIELD,
					TREATMENT_END_TIME_FIELD);
		} catch (ParserException ex) {
			log.error("Failed to parse line with reference " + fields[SSR_REFERENCE_FIELD] + ". Fault: " + ex.getMessage());
			return false;
		}

		return true;
	}

	private void parseSsrReference() throws ParserException {
		if (fieldIsMissing(SSR_REFERENCE_FIELD)) {
			throw new ParserException("Reference to original ssr record must be present");
		} else {
			ssrReference = fields[SSR_REFERENCE_FIELD];

			// NSPSUPPORT-23 Data from SSR was observed to be of length 16, not 24 as previously specified.
			// We have decided to use space padding to overcome this.
			if (ssrReference.length() < SSR.REFERENCE_LENGTH) {
				ssrReference = ssrReference + spacePadding(SSR.REFERENCE_LENGTH - ssrReference.length());
			}
		}
	}

	private String spacePadding(int n) {
		String padding = "";
		for (int i = 0; i < n; i++) {
			padding += " ";
		}
		return padding;
	}

	private String[] getTokens(String line, String separator, int expectedNumberOfFields) throws ParserException {
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

	private boolean fieldIsMissing(String[] fields, int i) {
		return fields[i].equals("");
	}

	public Interval parseIntervalFromTwoIdenticalDaysAsSpecifiedBySsr(String[] fields,
	                                                                  int treatmentStartTimeField, int treatmentEndTimeField) throws ParserException {
		if (fieldIsMissing(fields, treatmentStartTimeField)) {
			throw new ParserException("Treatment start time must be present");
		}

		if (fieldIsMissing(fields, treatmentEndTimeField)) {
			throw new ParserException("Treatment end time must be present");
		}

		DateTime admittedStart;
		try {
			admittedStart = parseDateAsSpecifiedBySsr(fields[treatmentStartTimeField]);
		} catch (ParseException e) {
			throw new ParserException("Treatment start time is malformed: " + fields[treatmentStartTimeField]);
		}

		DateTime admittedEnd;
		try {
			admittedEnd = parseDateAsSpecifiedBySsr(fields[treatmentEndTimeField]);
		} catch (ParseException e) {
			throw new ParserException("Treatment end time is malformed: " + fields[treatmentEndTimeField]);
		}

		if (!admittedStart.equals(admittedEnd)) {
			throw new ParserException("Treatment end time must be the same day as the treatment start time");
		}

		admittedEnd = admittedEnd.plusDays(1);

		return new Interval(admittedStart, admittedEnd);
	}

	private final static SimpleDateFormat ssrFormat = new SimpleDateFormat("yyyyMMdd");

	static {
		ssrFormat.setLenient(false);
	}

	private DateTime parseDateAsSpecifiedBySsr(String s) throws ParseException {
		return new DateTime(ssrFormat.parse(s));
	}
}