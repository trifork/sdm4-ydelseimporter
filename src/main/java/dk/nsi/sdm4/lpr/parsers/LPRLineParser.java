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
import dk.nsi.sdm4.lpr.relation.model.GeneralInterval;
import dk.nsi.sdm4.lpr.relation.model.HashedCpr;
import dk.nsi.sdm4.lpr.relation.model.LPR;
import org.joda.time.DateTime;
import org.joda.time.Interval;

public class LPRLineParser {

    private static final int EXPECTED_NUMBER_OF_FIELDS = 6;
    private static final String SEPERATOR = ";";
    private String[] fields;

    public static LprAction parseLine(String line)  {
        LPRLineParser parser = new LPRLineParser(line);
        return parser.parse();
    }

    private LPRLineParser(String line) {
        this.fields = Util.getTokens(line, SEPERATOR, EXPECTED_NUMBER_OF_FIELDS);
    }

    private String organisationId;
    private String patientCpr;
    private GeneralInterval admittedInterval;
    private LPR.LprRelationType relationType;
    private String lprReference;

    private LprAction parse()  {
        if (relationTypeIsDeletion()) {
            parseLprReference();
            return LprAction.createDeletion(lprReference);
        } else {
            parseRelationType();
            parseOrganisationId();
            parsePatientCpr();
            parseAdmittedInterval();
            parseLprReference();

            LPR lprForInsertion = LPR.newInstance(HashedCpr.buildFromHashedString(patientCpr), admittedInterval, lprReference,
                                                  relationType, organisationId);
            return LprAction.createInsertion(lprForInsertion);
        }
    }

    private static final int ORGANISATION_ID_FIELD = 0;
    private static final int PATIENT_CPR_FIELD = 1;
    private static final int RELATION_START_TIME_FIELD = 2;
    private static final int RELATION_END_TIME_FIELD = 3;
    private static final int RELATION_TYPE_FIELD = 4;
    private static final int LPR_REFERENCE_FIELD = 5;

    private boolean fieldIsMissing(int i) {
        return fields[i].equals("");
    }

    private void parseOrganisationId()  {
        if (fieldIsMissing(ORGANISATION_ID_FIELD)) {
            throw new ParserException(
                    "Either doctor organisation id (ydernummer) or hospital organisation id (sks) must be present");
        }

        organisationId = fields[ORGANISATION_ID_FIELD];
    }

    private void parseRelationType()  {
        if (fieldIsMissing(RELATION_TYPE_FIELD)) {
            throw new ParserException("Relation type must be present");
        }

        String relationTypeString = fields[RELATION_TYPE_FIELD];
        try {
            relationType = LPR.LprRelationType.fromStringRepresentation(relationTypeString);
        } catch (IllegalArgumentException ignore) {
            throw new ParserException("Unknown relation type: " + relationTypeString);
        }
    }

    private boolean relationTypeIsDeletion()  {
        if (fieldIsMissing(RELATION_TYPE_FIELD)) {
            throw new ParserException("Relation type must be present");
        }

        String relationTypeString = fields[RELATION_TYPE_FIELD];
        return "X".equals(relationTypeString);
    }

    private void parsePatientCpr()  {
        if (fieldIsMissing(PATIENT_CPR_FIELD)) {
            throw new ParserException("Patient cpr must be present");
        } else {
            patientCpr = fields[PATIENT_CPR_FIELD];
        }
    }

    private void parseAdmittedInterval()  {
        // The relationType should be set
        assert (relationType != null);

        if (fieldIsMissing(RELATION_END_TIME_FIELD)) {
            // Open intervals are only legal for S and Y relations
            if (relationType == LPR.LprRelationType.PARENT_UNIT || relationType == LPR.LprRelationType.DUSAS || relationType == LPR.LprRelationType.PROCEDURE_UNIT) {
                DateTime relationStartTime = Util.parseRelationStartTimeAsSpecifiedByLpr(fields,
                        RELATION_START_TIME_FIELD);
                admittedInterval = GeneralInterval.openInterval(relationStartTime);
            } else {
                throw new ParserException("Relation end time must be present when relation type is not S, Y or P");
            }
        } else {
            Interval interval = Util.parseAdmittedIntervalAsSpecifiedByLpr(fields, RELATION_START_TIME_FIELD,
                    RELATION_END_TIME_FIELD);
            if (relationType == LPR.LprRelationType.DISCHARGED_TO_UNIT) {
                if (interval.toDurationMillis() != 0) {
                    throw new ParserException(
                            "Interval must have start and end at the same time for discharge records (U)");
                }
                interval = new Interval(interval.getStart(), interval.getEnd().plusDays(1));
            }
            admittedInterval = GeneralInterval.closedInterval(interval);
        }
    }

    private void parseLprReference()  {
        if (fieldIsMissing(LPR_REFERENCE_FIELD)) {
            throw new ParserException("Reference to original lpr record must be present");
        } else {
            lprReference = fields[LPR_REFERENCE_FIELD];
        }
    }
}
