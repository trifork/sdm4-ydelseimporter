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
import dk.nsi.sdm4.lpr.relation.model.HashedCpr;
import dk.nsi.sdm4.lpr.relation.model.HospitalOrganisationIdentifier;
import dk.nsi.sdm4.lpr.relation.model.LPR;
import org.joda.time.DateTime;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LprLineParserTest {

    private static final String REFERENCE = "123456789012345678901234567890123456789012345678901234567890";
   
    @Test
    public void testParseLineFailingBasicFormat() {
        testFieldHelper("foo\nbar", "Line contains new-line character");

        testFieldHelper("12345;;3112101234;", "Too few fields on line: 12345;;3112101234;");

        testFieldHelper("foo;;;;;;;bar", "Too many fields on line: foo;;;;;;;bar");

        testFieldHelper(";;;foo;;;;", "Too many fields on line: ;;;foo;;;;");

        testFieldHelper(";;;;;;;", "Too many fields on line: ;;;;;;;");
    }

    @Test
    public void testParseLineFailingMissingMandatoryFields() {
        HashedCpr hashedCpr = HashedCpr.buildFromUnhashedString("0101861234");
        
        testFieldHelper(";" + hashedCpr + ";15-01-2011 14:35;05-02-2011 08:45;S;" + REFERENCE,
                "Either doctor organisation id (ydernummer) or hospital organisation id (sks) must be present");

        testFieldHelper("1234;;15-01-2011 14:35;05-02-2011 08:45;S;" + REFERENCE,
                "Patient cpr must be present");

        testFieldHelper("1234;" + hashedCpr + ";;15-01-2011 14:35;S;" + REFERENCE,
                "Relation start time must be present");

        testFieldHelper("1234;" + hashedCpr + ";15-01-2011 14:35;;U;" + REFERENCE,
                "Relation end time must be present when relation type is not S, Y or P");

        testFieldHelper("1234;" + hashedCpr + ";15-01-2011 14:35;05-02-2011 08:45;;" + REFERENCE,
                "Relation type must be present");

        testFieldHelper("1234;" + hashedCpr + ";15-01-2011 14:35;05-02-2011 08:45;S;",
                "Reference to original lpr record must be present");
    }

    @Test
    public void testParseLineFailingWrongFieldFormat() {
        testFieldHelper("12345;01018612345;2011-1 14:35;15-01-2011 14:35;S;" + REFERENCE,
                "Relation start time is malformed: 2011-1 14:35");

        testFieldHelper("12345;01018612345;15-01-2011 14:35;2011-2;S;" + REFERENCE,
                "Relation end time is malformed: 2011-2");

        testFieldHelper("12345;01018612345;15-02-2011 14:35;05-02-2011 08:45;S;" + REFERENCE,
                "Relation end time must be after relation start time");

        testFieldHelper("12345;01018612345;15-01-2011 14:35;05-02-2011 08:45;foo;" + REFERENCE,
                "Unknown relation type: foo");
    }

    @Test
    public void testParseLineFailingWrongOrganisationSpecified() {
        testFieldHelper("12345;01018612345;15-01-2011 14:35;05-02-2011 08:45;foo;" + REFERENCE,
                "Unknown relation type: foo");
    }

    private void testFieldHelper(String line, String expectedError) {
        try {
            LPRLineParser.parseLine(line);
            fail();
        } catch (Exception e) {
            assertEquals(expectedError, e.getMessage());
        }
    }

    @Test
    public void testParseLineSuccess() throws ParserException {
        HashedCpr hashedCpr = HashedCpr.buildFromUnhashedString("0101861234");
        LprAction action = LPRLineParser.parseLine("1234;" + hashedCpr.getHashedCpr()
                                                              + ";15-01-2011 14:35;05-02-2011 08:45;S;" + REFERENCE);
        assertEquals(LprAction.ActionType.INSERTION, action.actionType);
        LPR lpr = action.lprForInsertion;
        assertEquals(hashedCpr, lpr.getPatientCpr());
        assertEquals(HospitalOrganisationIdentifier.newInstance("1234"), lpr.getHospitalOrganisationIdentifier());
        assertEquals(LPR.LprRelationType.PARENT_UNIT, lpr.getRelationType());
        assertEquals(REFERENCE, lpr.getLprReference());
        assertEquals(new DateTime(2011, 1, 15, 14, 35, 0, 0), lpr.getAdmittedInterval().getStart());
        assertEquals(new DateTime(2011, 2, 5, 8, 45, 0, 0), lpr.getAdmittedInterval().getEnd());
    }
}
