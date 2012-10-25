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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {LprimporterApplicationConfig.class, TestDbConfiguration.class})
public class LPRDAOTest {

    private static final String PATIENT_CPR_UNHASHED = "1806861234";
    private static final HashedCpr PATIENT_CPR_HASHED = HashedCpr.buildFromUnhashedString(PATIENT_CPR_UNHASHED);

    private LPR referredUnitLpr, treatmentUnitLpr, dischargedLpr;

	@Autowired
    private LPRWriteDAO dao;

    private static final String REFERENCE = "123456789012345678901234567890123456789012345678901234567890";

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
