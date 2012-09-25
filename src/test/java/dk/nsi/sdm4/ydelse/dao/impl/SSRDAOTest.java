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
package dk.nsi.sdm4.ydelse.dao.impl;

import dk.nsi.sdm4.testutils.TestDbConfiguration;
import dk.nsi.sdm4.ydelse.common.exception.DAOException;
import dk.nsi.sdm4.ydelse.config.YdelseimporterApplicationConfig;
import dk.nsi.sdm4.ydelse.dao.SSRWriteDAO;
import dk.nsi.sdm4.ydelse.relation.model.DoctorOrganisationIdentifier;
import dk.nsi.sdm4.ydelse.relation.model.HashedCpr;
import dk.nsi.sdm4.ydelse.relation.model.SSR;
import dk.nsi.sdm4.ydelse.simulation.RandomDataUtilForTestPurposes;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {SSRDAOTest.TestConfig.class, YdelseimporterApplicationConfig.class, TestDbConfiguration.class})
public class SSRDAOTest {
	private SSR exampleSSR, sameSSR;

	@Autowired
	RandomDataUtilForTestPurposes dataUtilForTestPurposes;

	@Autowired
	private SSRWriteDAO dao;

	@Configuration
	static class TestConfig {
		@Bean
		public RandomDataUtilForTestPurposes dataUtil() {
			return new RandomDataUtilForTestPurposes();
		}
	}

	@Before
	public void setup() {
		exampleSSR = generateSSR();
		sameSSR = generateSSR();

		DoctorOrganisationIdentifier randomDoctorIdentifier =
				dataUtilForTestPurposes.randomDoctorOrganisationIdentifier();

		exampleSSR = exampleSSR.withDoctorOrganisationIdentifier(randomDoctorIdentifier);
		sameSSR = sameSSR.withDoctorOrganisationIdentifier(randomDoctorIdentifier);
	}

	@Test
	public void testWriteAndRead() {
		long pk = dao.insert(exampleSSR);
		SSR databaseSSR = dao.getUsingPrimaryKey(pk);

		HashedCpr hashedPatientCpr = exampleSSR.getPatientCpr();
		SSR expectedSSR = SSR.createInstance(hashedPatientCpr, exampleSSR.getDoctorOrganisationIdentifier(),
				exampleSSR.getTreatmentInterval(), exampleSSR.getExternalReference());

		assertEquals(expectedSSR, databaseSSR);
	}

	@Test
	public void testQuerySSR() {
		dao.insert(exampleSSR);

		List<SSR> result = dao.query(exampleSSR.getPatientCpr(), exampleSSR.getDoctorOrganisationIdentifier());
		assertEquals(1, result.size());
		SSR expectedSSR = SSR.createInstance(exampleSSR.getPatientCpr(),
				exampleSSR.getDoctorOrganisationIdentifier(), exampleSSR.getTreatmentInterval(),
				exampleSSR.getExternalReference());
		assertEquals(expectedSSR, result.get(0));

		// Test that discriminate against cprs
		sameSSR = sameSSR.withPatientCpr(HashedCpr.buildFromUnhashedString("0000000000"));
		dao.insert(sameSSR);
		result = dao.query(exampleSSR.getPatientCpr(), exampleSSR.getDoctorOrganisationIdentifier());
		assertEquals(1, result.size());

		// Test that discriminate against hospital organisation ids
		sameSSR = sameSSR.withPatientCpr(exampleSSR.getPatientCpr());
		sameSSR = sameSSR.withDoctorOrganisationIdentifier(DoctorOrganisationIdentifier.newInstance("foo123"));
		dao.insert(sameSSR);
		result = dao.query(exampleSSR.getPatientCpr(), exampleSSR.getDoctorOrganisationIdentifier());
		assertEquals(1, result.size());
	}

	@Test
	public void testGetSSRWithNonExistingKey() {
		try {
			dao.getUsingPrimaryKey(-10);
			fail("Should not be able to get SSR with negative key.");
		} catch (DAOException e) {
			assertEquals("No SSR with primary key -10", e.getMessage());
		}
	}

	@Test
	public void testDeletionWithExternalReferenceId() throws DAOException {
		long primaryKey = dao.insert(exampleSSR);
		assertNotNull(dao.getUsingPrimaryKey(primaryKey));

		dao.deleteByExternalReference(exampleSSR.getExternalReference());
		try {
			dao.getUsingPrimaryKey(primaryKey);
			fail("The ssr record should not be available");
		} catch (DAOException e) {
			assertEquals("No SSR with primary key " + primaryKey, e.getMessage());
		}
	}

	@Test
	public void testDeletionWithTwoRecordsWithTheSameExternalReferenceId() throws DAOException {
		long primaryKey1 = dao.insert(exampleSSR);
		long primaryKey2 = dao.insert(exampleSSR);

		assertNotNull(dao.getUsingPrimaryKey(primaryKey1));
		assertNotNull(dao.getUsingPrimaryKey(primaryKey2));

		dao.deleteByExternalReference(exampleSSR.getExternalReference());

		try {
			dao.getUsingPrimaryKey(primaryKey1);
			fail("The ssr record should not be available");
		} catch (DAOException e) {
			assertEquals("No SSR with primary key " + primaryKey1, e.getMessage());
		}

		try {
			dao.getUsingPrimaryKey(primaryKey2);
			fail("The ssr record should not be available");
		} catch (DAOException e) {
			assertEquals("No SSR with primary key " + primaryKey2, e.getMessage());
		}
	}

	public static SSR generateSSR() {
		Interval admittedInterval = new Interval(new DateTime(2011, 1, 15, 12, 34, 0, 0), new DateTime(2011, 2, 13, 18,
				59, 0, 0));
		return SSR.createInstance(HashedCpr.buildFromUnhashedString("1806861234"),
				DoctorOrganisationIdentifier.newInstance("457153"), admittedInterval, "AnExternalReferenceToSSR");
	}
}
