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
package dk.nsi.sdm4.lpr.dao.impl;

import dk.nsi.sdm4.lpr.common.exception.DAOException;
import dk.nsi.sdm4.lpr.config.LprimporterApplicationConfig;
import dk.nsi.sdm4.lpr.dao.LPRWriteDAO;
import dk.nsi.sdm4.lpr.relation.model.*;
import dk.nsi.sdm4.lpr.simulation.RandomDataUtilForTestPurposes;
import dk.nsi.sdm4.testutils.TestDbConfiguration;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {LPRDAOTest.TestConfig.class, LprimporterApplicationConfig.class, TestDbConfiguration.class})
public class LPRDAOTest {

    private static final String PATIENT_CPR_UNHASHED = "1806861234";
    private static final HashedCpr PATIENT_CPR_HASHED = HashedCpr.buildFromUnhashedString(PATIENT_CPR_UNHASHED);

    private LPR referredUnitLpr, treatmentUnitLpr, dischargedLpr;

	@Autowired
    private LPRWriteDAO dao;

    private static final String REFERENCE = "123456789012345678901234567890123456789012345678901234567890";

	@Configuration
	static class TestConfig {

	}

	@Before
    public void setup() {
        DateTime admittedStart = new DateTime(2011, 1, 15, 12, 34, 0, 0);
        DateTime admittedEnd = new DateTime(2011, 2, 13, 18, 59, 0, 0);
        GeneralInterval admittedInterval = GeneralInterval.closedInterval(new Interval(admittedStart, admittedEnd));

        RandomDataUtilForTestPurposes dataUtilForTestPurposes = new RandomDataUtilForTestPurposes();
        HospitalOrganisationIdentifier randomHospitalIdentifier = dataUtilForTestPurposes.randomHospitalOrganisationIdentifier();
        DoctorOrganisationIdentifier randomDoctorIdentifier = dataUtilForTestPurposes.randomDoctorOrganisationIdentifier();

        referredUnitLpr = LPR.newInstance(PATIENT_CPR_HASHED, admittedInterval, REFERENCE, LPR.LprRelationType.PARENT_UNIT, randomHospitalIdentifier.toString());
        treatmentUnitLpr = LPR.newInstance(PATIENT_CPR_HASHED, admittedInterval, REFERENCE, LPR.LprRelationType.PROCEDURE_UNIT, randomHospitalIdentifier.toString());
        dischargedLpr = LPR.newInstance(PATIENT_CPR_HASHED, admittedInterval, REFERENCE, LPR.LprRelationType.DUSAS, randomDoctorIdentifier.toString());
    }
    
    @Test
    public void doesNotReusePrimaryKeys() throws DAOException {
        long pk1 = dao.insertOrUpdate(referredUnitLpr.withLprReference("xy"+REFERENCE.substring(2)));
        long pk2 = dao.insertOrUpdate(referredUnitLpr.withLprReference("xz"+REFERENCE.substring(2)));
        assertFalse(pk1 == pk2);
    }

    @Test
    public void testWriteAndRead() throws DAOException {
        long pk;
        LPR databaseLPR;

        pk = dao.insertOrUpdate(referredUnitLpr);
        databaseLPR = dao.getUsingPrimaryKey(pk);
        assertEquals(referredUnitLpr, databaseLPR);

        pk = dao.insertOrUpdate(treatmentUnitLpr);
        databaseLPR = dao.getUsingPrimaryKey(pk);
        assertEquals(treatmentUnitLpr, databaseLPR);

        pk = dao.insertOrUpdate(dischargedLpr);
        databaseLPR = dao.getUsingPrimaryKey(pk);
        assertEquals(dischargedLpr, databaseLPR);
    }

    @Test
    public void testQueryLPR() {
        try {
            dao.insertOrUpdate(referredUnitLpr);

            List<LPR> result = dao.queryHospitalOrganisationIdentifier(referredUnitLpr.getPatientCpr(),
                    referredUnitLpr.getHospitalOrganisationIdentifier());
            assertEquals(1, result.size());
            assertEquals(referredUnitLpr, result.get(0));

            // Test that discriminate against cprs
            dao.insertOrUpdate(referredUnitLpr.withPatientCpr(HashedCpr.buildFromUnhashedString("0000000000")).withLprReference("x"+REFERENCE.substring(1)));
            result = dao.queryHospitalOrganisationIdentifier(referredUnitLpr.getPatientCpr(),
                    referredUnitLpr.getHospitalOrganisationIdentifier());
            assertEquals(1, result.size());

            // Test that discriminate against hospital organisation ids
            dao.insertOrUpdate(referredUnitLpr.inParentUnit(HospitalOrganisationIdentifier.newInstance("foo1234")).withLprReference("y"+REFERENCE.substring(1)));
            result = dao.queryHospitalOrganisationIdentifier(referredUnitLpr.getPatientCpr(),
                    referredUnitLpr.getHospitalOrganisationIdentifier());
            assertEquals(1, result.size());
        } catch (Exception e) {
            fail("Unable to run database test:\n" + e.getMessage());
        }
    }

    @Test
    public void testQueryLPRWithPartialSksCodes() {
        List<LPR> result;

        try {
            RandomDataUtilForTestPurposes testDataUtil = new RandomDataUtilForTestPurposes();
            String patientCpr = testDataUtil.randomCpr();
            HashedCpr patientCprHashed = HashedCpr.buildFromUnhashedString(patientCpr);
            LPR lprWithRandomPatientCpr = referredUnitLpr.withPatientCpr(patientCprHashed);

            HospitalOrganisationIdentifier aarhusSygehus = HospitalOrganisationIdentifier.newInstance("7003");
            HospitalOrganisationIdentifier psykiatrien = HospitalOrganisationIdentifier.newInstance("6600");
            HospitalOrganisationIdentifier overAfdeling1 = HospitalOrganisationIdentifier.newInstance("700328");
            HospitalOrganisationIdentifier afdeling1 = HospitalOrganisationIdentifier.newInstance("7003287");
            HospitalOrganisationIdentifier overAfdeling2 = HospitalOrganisationIdentifier.newInstance("700330");
            HospitalOrganisationIdentifier afdeling2 = HospitalOrganisationIdentifier.newInstance("7003301");

            LPR lprAarhusSygehus = lprWithRandomPatientCpr.inParentUnit(aarhusSygehus).withLprReference("a"+REFERENCE.substring(1));
            LPR lprPsykiatrien = lprWithRandomPatientCpr.inParentUnit(psykiatrien).withLprReference("b"+REFERENCE.substring(1));

            LPR lprOverAfdeling1 = lprWithRandomPatientCpr.inParentUnit(overAfdeling1).withLprReference("c"+REFERENCE.substring(1));
            LPR lprAfdeling1 = lprWithRandomPatientCpr.inParentUnit(afdeling1).withLprReference("d"+REFERENCE.substring(1));

            LPR lprOverAfdeling2 = lprWithRandomPatientCpr.inParentUnit(overAfdeling2).withLprReference("e"+REFERENCE.substring(1));
            LPR lprAfdeling2 = lprWithRandomPatientCpr.inParentUnit(afdeling2).withLprReference("f"+REFERENCE.substring(1));

            dao.insertOrUpdate(lprAarhusSygehus);
            dao.insertOrUpdate(lprPsykiatrien);
            dao.insertOrUpdate(lprOverAfdeling1);
            dao.insertOrUpdate(lprAfdeling1);
            dao.insertOrUpdate(lprOverAfdeling2);
            dao.insertOrUpdate(lprAfdeling2);

            result = dao.queryHospitalOrganisationIdentifier(patientCprHashed, aarhusSygehus);

            assertEquals(5, result.size());
            assertTrue(result.contains(lprAarhusSygehus));
            assertTrue(result.contains(lprOverAfdeling1));
            assertTrue(result.contains(lprAfdeling1));
            assertTrue(result.contains(lprOverAfdeling2));
            assertTrue(result.contains(lprAfdeling2));
            assertFalse(result.contains(lprPsykiatrien));

            result = dao.queryHospitalOrganisationIdentifier(patientCprHashed, overAfdeling1);
            assertEquals(5, result.size());
            assertTrue(result.contains(lprAarhusSygehus));
            assertTrue(result.contains(lprOverAfdeling1));
            assertTrue(result.contains(lprAfdeling1));
            assertTrue(result.contains(lprOverAfdeling2));
            assertTrue(result.contains(lprAfdeling2));
            assertFalse(result.contains(lprPsykiatrien));

            result = dao.queryHospitalOrganisationIdentifier(patientCprHashed, afdeling1);
            assertEquals(5, result.size());
            assertTrue(result.contains(lprAarhusSygehus));
            assertTrue(result.contains(lprOverAfdeling1));
            assertTrue(result.contains(lprAfdeling1));
            assertTrue(result.contains(lprOverAfdeling2));
            assertTrue(result.contains(lprAfdeling2));
            assertFalse(result.contains(lprPsykiatrien));

            result = dao.queryHospitalOrganisationIdentifier(patientCprHashed,
                    HospitalOrganisationIdentifier.newInstance("660003"));
            assertEquals(1, result.size());
            assertTrue(result.contains(lprPsykiatrien));

            result = dao.queryHospitalOrganisationIdentifier(patientCprHashed, psykiatrien);
            assertEquals(1, result.size());
            assertTrue(result.contains(lprPsykiatrien));

            result = dao.queryHospitalOrganisationIdentifier(patientCprHashed,
                    HospitalOrganisationIdentifier.newInstance("9001"));
            assertEquals(0, result.size());
        } catch (Exception e) {
            fail("Unable to run database test:\n" + e.getMessage());
        }
    }

    @Test
    public void testDifferentiateBetweenHospitalAndDoctorOrganisationIdentifier() {
        RandomDataUtilForTestPurposes testDataUtil = new RandomDataUtilForTestPurposes();
        String patientCpr = testDataUtil.randomCpr();
        HashedCpr patientCprHashed = HashedCpr.buildFromUnhashedString(patientCpr);
        GeneralInterval admittedInterval = GeneralInterval.closedInterval(new Interval(new DateTime(2011, 1, 14, 12, 0,
                0, 0), new DateTime(2011, 1, 20, 12, 0, 0, 0)));
        HospitalOrganisationIdentifier hospitalOrganisationIdentifier = HospitalOrganisationIdentifier
                .newInstance("730011");
        DoctorOrganisationIdentifier doctorOrganisationIdentifier = DoctorOrganisationIdentifier.newInstance("123456");

        LPR hospitalLprRecord = LPR.newInstance(patientCprHashed, admittedInterval, "d" + REFERENCE.substring(1),
                LPR.LprRelationType.PARENT_UNIT, hospitalOrganisationIdentifier.toString());

        LPR doctorLprRecord = LPR.newInstance(patientCprHashed, admittedInterval, "h" + REFERENCE.substring(1),
                LPR.LprRelationType.DUSAS, doctorOrganisationIdentifier.toString());

        LPR doctorLprRecordWithHospitalCode = LPR.newInstance(patientCprHashed, admittedInterval,
                "b" + REFERENCE.substring(1), LPR.LprRelationType.DUSAS, hospitalOrganisationIdentifier.toString());

        List<LPR> result;

        try {
            dao.insertOrUpdate(hospitalLprRecord);
            dao.insertOrUpdate(doctorLprRecord);
            dao.insertOrUpdate(doctorLprRecordWithHospitalCode);

            result = dao.queryHospitalOrganisationIdentifier(patientCprHashed, hospitalOrganisationIdentifier);
            assertEquals(1, result.size());
        } catch (DAOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void canDeleteByExternalReferenceId() throws DAOException {
        long primaryKey = dao.insertOrUpdate(treatmentUnitLpr);
        assertNotNull(dao.getUsingPrimaryKey(primaryKey));

        dao.deleteByLprReference(treatmentUnitLpr.getLprReference());
        assertRecordDoesNotExist(primaryKey);
    }

    @Test
    public void canDeleteTwoRecordsWithTheSameExternalReferenceId() throws DAOException {
        String lprReference = treatmentUnitLpr.getLprReference();
        dischargedLpr.withLprReference(lprReference);
        long primaryKey1 = dao.insertOrUpdate(dischargedLpr);
        long primaryKey2 = dao.insertOrUpdate(treatmentUnitLpr);

        assertNotNull(dao.getUsingPrimaryKey(primaryKey1));
        assertNotNull(dao.getUsingPrimaryKey(primaryKey2));

        dao.deleteByLprReference(lprReference);

        assertRecordDoesNotExist(primaryKey1);
        assertRecordDoesNotExist(primaryKey2);
    }

    @Test
    public void testDAOExceptions() {
	    assertRecordDoesNotExist(-1);
    }

    private void assertRecordDoesNotExist(long primaryKey) {
        try {
            dao.getUsingPrimaryKey(primaryKey);
            fail("The lpr record with primary key " + primaryKey + " should not be available");
        } catch (DAOException e) {
            assertEquals("No LPR with primary key " + primaryKey, e.getMessage());
        }
    }
}
