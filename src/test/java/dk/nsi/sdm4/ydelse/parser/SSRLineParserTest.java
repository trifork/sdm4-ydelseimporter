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
import dk.nsi.sdm4.ydelse.relation.model.DoctorOrganisationIdentifier;
import dk.nsi.sdm4.ydelse.relation.model.HashedCpr;
import dk.nsi.sdm4.ydelse.relation.model.SSR;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SSRLineParserTest {

    @Test
    public void testParseLineFailingBasicFormat() {
        testFieldHelper("foo\nbar", "Line contains new-line character");
        testFieldHelper("12345,,3112101234,", "Too few fields on line: 12345,,3112101234,");
        testFieldHelper("foo,,,,,bar", "Too many fields on line: foo,,,,,bar");
        testFieldHelper(",,,foo,,", "Too many fields on line: ,,,foo,,");
        testFieldHelper(",,,,,,,", "Too many fields on line: ,,,,,,,");
    }

    @Test
    public void testParseLineFailingMissingMandatoryFields() {
        testFieldHelper(",01018612345,20110115,20110115,ssrRef:42",
                "Doctor organisation id (ydernummer) must be present");
        testFieldHelper("12345,,20110115,20110115,ssrRef:42", "Patient cpr must be present");
        testFieldHelper("12345,01018612345,20110115,20110115,", "Reference to original ssr record must be present");
    }

    private void testFieldHelper(String line, String expectedError) {
        try {
            SSRLineParser.parseLine(line);
            fail("Should not be able to parse line " + line + ", expected error: " + expectedError);
        } catch (ParserException e) {
            assertEquals(expectedError, e.getMessage());
        }
    }

    @Test
    public void testParseLineInsertionSuccess() throws ParserException {
        HashedCpr hashedCpr = HashedCpr.buildFromUnhashedString("0101861234");

        SsrAction ssrAction = SSRLineParser.parseLine("12345," + hashedCpr
                + ",20110115,20110115,AnExternalReferenceToSSR");
        assertEquals(SsrAction.ActionType.INSERTION, ssrAction.actionType);
        SSR ssr = ssrAction.ssrForInsertion;
        assertEquals(DoctorOrganisationIdentifier.newInstance("12345"), ssr.getDoctorOrganisationIdentifier());
        assertEquals(hashedCpr, ssr.getPatientCpr());
        assertEquals(new DateTime(2011, 1, 15, 0, 0, 0, 0), ssr.getTreatmentInterval().getStart());
        assertEquals(new DateTime(2011, 1, 16, 0, 0, 0, 0), ssr.getTreatmentInterval().getEnd());
        assertEquals("AnExternalReferenceToSSR", ssr.getExternalReference());
    }

    @Test
    public void testParseLineDeletionSuccess() throws ParserException {
        SsrAction ssrAction = SSRLineParser.parseLine(",,,,AnExternalReferenceToSSR");
        assertEquals(SsrAction.ActionType.DELETION, ssrAction.actionType);
        assertEquals("AnExternalReferenceToSSR", ssrAction.externalReferenceForDeletion);
    }

	public static class SSRFileHandlerTest {

	    @Test
	    public void testParsing() throws IOException, DAOException {
	        purgeDB();

	        String dirname = "stubbedftp";
	        String filename = dirname + "/ssr_foo_bar.csv";

	        HandlerTestUtils.removeFile(filename);
	        assertFalse(HandlerTestUtils.fileExists(filename));

	        int n = 10;
	        GenerateTestRegisterDumps.generateDumps(n);
	        DirectoryWatcher directoryWatcher = new DirectoryWatcher(new File(dirname), new File("unexistingErrorLocation"));
	        RegisterImportJob job = new RegisterImportJob(directoryWatcher);
	        assertTrue(HandlerTestUtils.fileExists(filename));

	        job.run();
	        assertTrue(HandlerTestUtils.fileExists(filename));

	        job.run();
	        assertFalse(HandlerTestUtils.fileExists(filename));

	        List<SSR> seen = null;
	        seen = DAOFactoryForTestPurposes.getSSRForTestPurposes(RegisterProvider.DI).getAllSSRs();

	        Set<SSR> seenAsSet = new HashSet<SSR>(seen);

	        RandomSSR randomSSR = new RandomSSR();
	        randomSSR.setSeed(1337);
	        Set<SSR> expected = new HashSet<SSR>();
	        for (SSR ssr : randomSSR.randomSSRs(n)) {
	            expected.add(ssr);
	        }

	        assertEquals(expected.size(), seenAsSet.size());
	        assertEquals(expected, seenAsSet);
	    }

	    @Test
	    public void testParsingOfFileWithUpdate() throws IOException, DAOException {
	        purgeDB();

	        String dirname = "stubbedftp";
	        String filename = dirname + "/ssr_date.csv";

	        URL url = this.getClass().getResource("SSRFileHandlerTest-TestFile.csv");
	        FileUtils.copyURLToFile(url, new File(filename));

	        DirectoryWatcher directoryWatcher = new DirectoryWatcher(new File(dirname), new File("unexistingErrorLocation"));
	        RegisterImportJob job = new RegisterImportJob(directoryWatcher);
	        assertTrue(HandlerTestUtils.fileExists(filename));

	        job.run();
	        assertTrue(HandlerTestUtils.fileExists(filename));

	        job.run();
	        assertFalse(HandlerTestUtils.fileExists(filename));

	        List<SSR> seen = null;
	        seen = DAOFactoryForTestPurposes.getSSRForTestPurposes(RegisterProvider.DI).getAllSSRs();

	        assertEquals(2, seen.size());

	        Set<SSR> seenAsSet = new HashSet<SSR>(seen);

	        Set<SSR> expected = new HashSet<SSR>();
	        HashedCpr hashedPatientCpr = HashedCpr.buildFromHashedString("1234567890123456789012345678901234567890");
	        DoctorOrganisationIdentifier doctorOrganisationIdentifier = DoctorOrganisationIdentifier.newInstance("034002");
	        String externalReference = "AnExternalReferenceToSSR";

	        DateTime firstDate = new DateTime(2011, 2, 17, 0, 0, 0, 0);
	        DateTime secondDate = new DateTime(2011, 2, 20, 0, 0, 0, 0);

	        Interval firstInterval = new Interval(firstDate, firstDate.plusDays(1));
	        Interval secondInterval = new Interval(secondDate, secondDate.plusDays(1));

	        expected.add(SSR.createInstance(hashedPatientCpr, doctorOrganisationIdentifier, firstInterval,
	                externalReference));
	        expected.add(SSR.createInstance(hashedPatientCpr, doctorOrganisationIdentifier, secondInterval,
	                externalReference));

	        assertEquals(expected.size(), seenAsSet.size());
	        assertEquals(expected, seenAsSet);
	    }

	    @Test
	    public void testParsingOfFileFromCSC() throws RegisterImportException, DAOException
	    {
	        String exampleFileFromRefhostDir = "src/test/resources//";
	        String exampleCsvFile = exampleFileFromRefhostDir + "Ydelsesudtraek.csv";

	        purgeDB();
	        SSRFileHandler ssrFileHandler = new SSRFileHandler();
	        List<SSR> seen = null;
	        ssrFileHandler.handleFile(new File(exampleCsvFile));
	        seen = getAllSsr();

	        assertEquals(2, seen.size());
	    }

	    private void purgeDB() {
	        try {
	            DAOFactoryForTestPurposes.getSSRForTestPurposes(RegisterProvider.DI).purge();
	        } catch (DAOException e) {
	            e.printStackTrace();
	            fail();
	        }
	    }

	    private List<SSR> getAllSsr() throws DAOException {
	        return DAOFactoryForTestPurposes.getSSRForTestPurposes(RegisterProvider.DI).getAllSSRs();
	    }
	}
}
