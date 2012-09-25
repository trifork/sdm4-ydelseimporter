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
import dk.nsi.sdm4.testutils.TestDbConfiguration;
import dk.nsi.sdm4.ydelse.config.YdelseimporterApplicationConfig;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {YdelseparserTest.TestConfig.class, YdelseimporterApplicationConfig.class, TestDbConfiguration.class})
public class YdelseparserTest {
	@Configuration
	static class TestConfig {
/*
		@Bean
		public SSRTestPurposeDAO dao() {
			return new SSRTestPurposeDAOImpl();
		}
*/
	}

	@Autowired
	YdelseParser parser;

	@Rule
	public TemporaryFolder tmpDir = new TemporaryFolder();

	@Test(expected = ParserException.class)
	public void complainsIfDatasetIsNull() throws IOException {
		parser.process(null);
	}

	@Test
	public void complainsAboutEmptyDataset() throws IOException {
		try {
			parser.process(tmpDir.getRoot());
			fail("Expected ParserException, but none came");
		} catch (ParserException e) {
			assertThat(e.getMessage(), containsString("empty"));
			assertThat(e.getMessage(), containsString(tmpDir.getRoot().getAbsolutePath()));
		}
	}

	@Test
	public void complainsAboutDatasetWithTwoFiles() throws IOException {
		tmpDir.newFile();
		tmpDir.newFile();
		try {
			parser.process(tmpDir.getRoot());
			fail("Expected ParserException, but none came");
		} catch (ParserException e) {
			assertThat(e.getMessage(), containsString("expected 1"));
			assertThat(e.getMessage(), containsString("2")); // antal filer
			assertThat(e.getMessage(), containsString(tmpDir.getRoot().getAbsolutePath()));
		}
	}

/*
    @Test
    public void testParsing() throws IOException, DAOException {
        purgeDB();

        String dirname = "stubbedftp";
        String filename = dirname + "/ssr_foo_bar.csv";

	    File file = new File(filename);
	    FileUtils.deleteQuietly(file);
		assertFalse(file.exists());

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

        URL url = this.getClass().getResource("YdelseparserTest-TestFile.csv");
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
        YdelseParser ydelseParser = new YdelseParser();
        List<SSR> seen = null;
        ydelseParser.handleFile(new File(exampleCsvFile));
        seen = getAllSsr();

        assertEquals(2, seen.size());
    }

    private List<SSR> getAllSsr() throws DAOException {
        return DAOFactoryForTestPurposes.getSSRForTestPurposes(RegisterProvider.DI).getAllSSRs();
    }
    */

}
