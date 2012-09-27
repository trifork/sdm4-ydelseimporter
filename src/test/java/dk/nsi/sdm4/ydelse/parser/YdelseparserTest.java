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
import dk.nsi.sdm4.ydelse.common.exception.DAOException;
import dk.nsi.sdm4.ydelse.common.exception.RegisterImportException;
import dk.nsi.sdm4.ydelse.config.YdelseimporterApplicationConfig;
import dk.nsi.sdm4.ydelse.dao.SSRTestPurposeDAO;
import dk.nsi.sdm4.ydelse.dao.impl.SSRTestPurposeDAOImpl;
import dk.nsi.sdm4.ydelse.relation.model.DoctorOrganisationIdentifier;
import dk.nsi.sdm4.ydelse.relation.model.HashedCpr;
import dk.nsi.sdm4.ydelse.relation.model.SSR;
import dk.nsi.sdm4.ydelse.simulation.RandomDataUtilForTestPurposes;
import dk.nsi.sdm4.ydelse.simulation.RandomSSR;
import dk.nsi.sdm4.ydelse.testutil.GenerateTestRegisterDumps;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.Timed;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.containsString;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {YdelseparserTest.TestConfig.class, YdelseimporterApplicationConfig.class, TestDbConfiguration.class})
public class YdelseparserTest {
	@Autowired
	GenerateTestRegisterDumps generator;

	@Autowired
	SSRTestPurposeDAO testDao;

	@Autowired
	RandomSSR randomSSR;

	@Configuration
	static class TestConfig {
		@Bean
		public SSRTestPurposeDAO dao() {
			return new SSRTestPurposeDAOImpl();
		}

		@Bean
		public GenerateTestRegisterDumps generator() {
			return new GenerateTestRegisterDumps();
		}

		@Bean
		public RandomSSR randomSSR() {
			RandomSSR randomSSR = new RandomSSR(dataUtil());
			randomSSR.setSeed(1337);
			return randomSSR;
		}

		@Bean
		public RandomDataUtilForTestPurposes dataUtil() {
			return new RandomDataUtilForTestPurposes();
		}
	}

	@Autowired
	YdelseParser parser;


	@Autowired
	YdelseInserter inserter;

	@Rule
	public TemporaryFolder tmpDir = new TemporaryFolder();

	@Before
	public void clearDatabase() {
		// selvom testen starter transaktioner og ruller dem tilbage efter hver testmetode (på grund af @Transactional), kører
		// inserts mod databasen i batches som hver er en selvstændig transaktion, så vi er nødt til at rydde databasen manuelt
		testDao.purge();
	}

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

    @Test
    @Timed(millis=15000L) // kører på 2-3 sek på min Macbook Pro, så 15 sek burde sikre mod at det løber løbsk uden at fejle på langsomme maskiner
    public void canParseLargishFile() throws IOException, DAOException {
	    File datasetDir = tmpDir.newFolder();
	    int numberOfRecords = 200; // skal være nok til at sikre at forskellige tråde ser forskellige dele
	    Set<SSR> expected = new HashSet<SSR>(generator.generateSsrDumps(datasetDir, numberOfRecords));

	    parser.process(datasetDir);

	    Set<SSR> seenAsSet = new HashSet<SSR>(testDao.getAllSSRs());

        assertEquals(expected.size(), seenAsSet.size());
        assertEquals(expected, seenAsSet);
    }

	@Test
	public void canDeleteNonexistingRef() throws IOException {
		// this will happen when we solve NSPSUPPORT-108
		File datasetDir = tmpDir.newFolder();
		generator.generateSingleDeletion(datasetDir, "DoesNotExist            ");

		parser.process(datasetDir);

		List<SSR> seen = testDao.getAllSSRs();

		assertEquals(0, seen.size());
	}

	@Test
	public void insertionFollowedByInsertionResultsInNoRecords() throws IOException {
		// this will happen when we solve NSPSUPPORT-108
		File datasetDir = tmpDir.newFolder();
		generator.generateSingleInsertionFollowedByDeletion(datasetDir, "JustAnOrdinaryReference ");

		parser.process(datasetDir);

		List<SSR> seen = testDao.getAllSSRs();

		assertEquals(0, seen.size());
	}

	@Test
	public void insertionFollowedByInsertionResultsInNoRecordsEvenWhenDatabaseContainsRecordWithSameRefBeforeRun() throws IOException {
		// guards against the fix for NSPSUPPORT-108 forgetting to actually execute the deletions in the file
		final String externalReference = "JustAnOrdinaryReference ";
		testDao.insert(randomSSR.randomSSR().withExternalReference(externalReference));

		File datasetDir = tmpDir.newFolder();
		generator.generateSingleInsertionFollowedByDeletion(datasetDir, externalReference);

		parser.process(datasetDir);

		List<SSR> seen = testDao.getAllSSRs();

		assertEquals(0, seen.size());
	}

	@Test
    public void testParsingOfFileWithUpdate() throws IOException, DAOException {
	    File datasetDir = makeDatadirWithResource("YdelseparserTest-TestFile.csv");

	    parser.process(datasetDir);

	    Set<SSR> seenAsSet = new HashSet<SSR>(testDao.getAllSSRs());

	    assertEquals(2, seenAsSet.size());

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
    public void testParsingOfFileFromCSC() throws RegisterImportException, DAOException, IOException {
	    File datasetDir = makeDatadirWithResource("Ydelsesudtraek.csv");

	    parser.process(datasetDir);

	    List<SSR> seen = testDao.getAllSSRs();

	    assertEquals(2, seen.size());
    }

	private File makeDatadirWithResource(String path) throws IOException {
		File datasetDir = tmpDir.newFolder();
		URL url = this.getClass().getResource(path);
		FileUtils.copyURLToFile(url, new File(datasetDir, "testfile.csv"));

		return datasetDir;
	}
}
