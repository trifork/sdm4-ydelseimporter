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
package dk.nsi.sdm4.ydelse.relation.model;

import dk.nsi.sdm4.ydelse.relation.model.LPR.LprRelationType;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class LPRTest {
    private static final HashedCpr PATIENT_CPR = HashedCpr.buildFromUnhashedString("1806861234");
    private GeneralInterval admittedInterval;
    private static final String HOSPITAL_ORG_ID = "1234";
    private static final String DOCTOR_ORG_ID = "654321";

    private LPR parentUnitLpr, procedureUnitLpr, dischargedLpr, dusasLpr;

    private static final String REFERENCE = "123456789012345678901234567890123456789012345678901234567890";

    @Before
    public void setup() {
	    DateTimeZone zone = DateTimeZone.UTC;
	    admittedInterval = GeneralInterval.closedInterval(
			    new Interval(
					    new DateTime(2011, 02, 15, 12, 0, 0, 0, zone),
					    new DateTime(2011, 02, 27, 15, 0, 0, 0, zone)));


	    parentUnitLpr = LPR.newInstance(PATIENT_CPR, admittedInterval, REFERENCE, LPR.LprRelationType.PARENT_UNIT,
                HOSPITAL_ORG_ID);
        procedureUnitLpr = LPR.newInstance(PATIENT_CPR, admittedInterval, REFERENCE, LprRelationType.PROCEDURE_UNIT,
                HOSPITAL_ORG_ID);
        dischargedLpr = LPR.newInstance(PATIENT_CPR, admittedInterval, REFERENCE, LprRelationType.DISCHARGED_TO_UNIT,
                HOSPITAL_ORG_ID);
        dusasLpr = LPR.newInstance(PATIENT_CPR, admittedInterval, REFERENCE, LprRelationType.DUSAS, DOCTOR_ORG_ID);
    }

    @Test
    public void testSettersWithNull() {

        try {
            parentUnitLpr.withPatientCpr(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Patient cpr must be non-null.", e.getMessage());
        }

        try {
            parentUnitLpr.withAdmittedIntervalIgnoringMillis(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Admitted interval must be non-null.", e.getMessage());
        }

        try {
            parentUnitLpr.withLprReference(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("LPR reference must be non-null.", e.getMessage());
        }

        try {
            parentUnitLpr.inParentUnit(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Hospital organisation identifier must be non-null.", e.getMessage());
        }

        try {
            parentUnitLpr.inProcedureUnit(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Hospital organisation identifier must be non-null.", e.getMessage());
        }

        try {
            parentUnitLpr.dischargedToUnit(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Hospital organisation identifier must be non-null.", e.getMessage());
        }

        try {
            parentUnitLpr.withDusasContact(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Doctor organisation identifier must be non-null.", e.getMessage());
        }
    }

    @Test
    public void testSettingWrongLengthReference() {
        try {
            dischargedLpr.withLprReference("foo");
            fail("Should not be able to set wrong length reference");
        } catch (IllegalArgumentException e) {
            assertEquals("The reference length is 3 but should be exactly 60", e.getMessage());
        }
    }

    @Test
    public void testNoMilliseconds() {
        DateTime start = new DateTime(2010, 1, 1, 12, 30, 0, 0);
        DateTime end = new DateTime(2011, 1, 1, 12, 40, 0, 0);

        GeneralInterval intervalWhereStartHasMillis = GeneralInterval.closedInterval(new Interval(start.plusMillis(42),
                end));
        parentUnitLpr = parentUnitLpr.withAdmittedIntervalIgnoringMillis(intervalWhereStartHasMillis);
        assertEquals(start, parentUnitLpr.getAdmittedInterval().getStart());
        assertEquals(end, parentUnitLpr.getAdmittedInterval().getEnd());

        GeneralInterval intervalWhereEndHasMillis = GeneralInterval.closedInterval(new Interval(start, end
                .plusMillis(42)));
        parentUnitLpr = parentUnitLpr.withAdmittedIntervalIgnoringMillis(intervalWhereEndHasMillis);
        assertEquals(start, parentUnitLpr.getAdmittedInterval().getStart());
        assertEquals(end, parentUnitLpr.getAdmittedInterval().getEnd());
    }

    @Test
    public void testIntervalStartBeforeEnd() {
        DateTime now = new DateTime();
        DateTime yesterday = now.minusDays(1);
        try {
            new Interval(now, yesterday);
            fail("Expected joda to throw exception on wrong interval");
        } catch (IllegalArgumentException e) {
            assertEquals("The end instant must be greater or equal to the start", e.getMessage());
        }
    }

    @Test
    public void testGetters() {
        assertEquals(PATIENT_CPR, parentUnitLpr.getPatientCpr());

        assertEquals(admittedInterval, parentUnitLpr.getAdmittedInterval());

        assertEquals(REFERENCE, parentUnitLpr.getLprReference());
        assertEquals(REFERENCE, procedureUnitLpr.getLprReference());
        assertEquals(REFERENCE, dischargedLpr.getLprReference());
        assertEquals(REFERENCE, dusasLpr.getLprReference());

        assertEquals(LprRelationType.PARENT_UNIT, parentUnitLpr.getRelationType());
        assertEquals(LprRelationType.PROCEDURE_UNIT, procedureUnitLpr.getRelationType());
        assertEquals(LprRelationType.DISCHARGED_TO_UNIT, dischargedLpr.getRelationType());
        assertEquals(LprRelationType.DUSAS, dusasLpr.getRelationType());

        assertEquals(HospitalOrganisationIdentifier.newInstance(HOSPITAL_ORG_ID),
                parentUnitLpr.getHospitalOrganisationIdentifier());
        assertEquals(HospitalOrganisationIdentifier.newInstance(HOSPITAL_ORG_ID),
                procedureUnitLpr.getHospitalOrganisationIdentifier());
        assertEquals(HospitalOrganisationIdentifier.newInstance(HOSPITAL_ORG_ID),
                dischargedLpr.getHospitalOrganisationIdentifier());
        assertEquals(DoctorOrganisationIdentifier.newInstance(DOCTOR_ORG_ID),
                dusasLpr.getDoctorOrganisationIdentifier());

        try {
            parentUnitLpr.getDoctorOrganisationIdentifier();
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Doctor organisation identifier is not defined for this relation type (PARENT_UNIT).",
                    e.getMessage());
        }

        try {
            procedureUnitLpr.getDoctorOrganisationIdentifier();
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Doctor organisation identifier is not defined for this relation type (PROCEDURE_UNIT).",
                    e.getMessage());
        }

        try {
            dischargedLpr.getDoctorOrganisationIdentifier();
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Doctor organisation identifier is not defined for this relation type (DISCHARGED_TO_UNIT).",
                    e.getMessage());
        }

        try {
            dusasLpr.getHospitalOrganisationIdentifier();
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Hospital organisation identifier is not defined for this relation type (DUSAS).",
                    e.getMessage());
        }
    }

    @Test
    public void testHashCode() {
        assertEquals(-195737331, parentUnitLpr.hashCode());
        assertEquals(-195737331, procedureUnitLpr.hashCode());
        assertEquals(-195737331, dischargedLpr.hashCode());
        assertEquals(1344911180, dusasLpr.hashCode());
    }

    @Test
    public void testEqualsObject() {
        assertTrue(parentUnitLpr.equals(parentUnitLpr));
        assertFalse(parentUnitLpr.equals(null));
        assertFalse(parentUnitLpr.equals(new Object()));
        assertTrue(parentUnitLpr.equals(new LPR(parentUnitLpr)));

        assertFalse(parentUnitLpr.equals(parentUnitLpr.withPatientCpr(HashedCpr.buildFromUnhashedString("0000000000"))));
        assertFalse(parentUnitLpr.equals(parentUnitLpr.withAdmittedIntervalIgnoringMillis(GeneralInterval
                .closedInterval(new Interval(0, 1000)))));
        assertFalse(parentUnitLpr.equals(parentUnitLpr.withLprReference("f" + REFERENCE.substring(1))));

        assertFalse(parentUnitLpr.equals(parentUnitLpr.inParentUnit(HospitalOrganisationIdentifier
                .newInstance("foo1234"))));
        assertFalse(procedureUnitLpr.equals(procedureUnitLpr.inProcedureUnit(HospitalOrganisationIdentifier
                .newInstance("foo1234"))));
        assertFalse(dischargedLpr.equals(dischargedLpr.inProcedureUnit(HospitalOrganisationIdentifier
                .newInstance("foo1234"))));
        assertFalse(dusasLpr.equals(dusasLpr.withDusasContact(DoctorOrganisationIdentifier.newInstance("foo123"))));

        assertFalse(parentUnitLpr.equals(procedureUnitLpr));
        assertFalse(parentUnitLpr.equals(dusasLpr));
    }

    @Test
    public void testToString() {
        assertEquals(
                "LPR[patientCpr:EBCE374754BE6ABA40CD9F62DB0CDECFA3CC1F2C,admittedInterval:2011-02-15T12:00:00.000Z/2011-02-27T15:00:00.000Z,"
                        + "lprReference:" + REFERENCE + ",relationType:PARENT_UNIT,"
                        + "hospitalOrganisationIdentifier:1234,doctorOrganisationIdentifier:<null>]",
                parentUnitLpr.toString());
        assertEquals(
                "LPR[patientCpr:EBCE374754BE6ABA40CD9F62DB0CDECFA3CC1F2C,admittedInterval:2011-02-15T12:00:00.000Z/2011-02-27T15:00:00.000Z,"
                        + "lprReference:" + REFERENCE + ",relationType:PROCEDURE_UNIT,"
                        + "hospitalOrganisationIdentifier:1234,doctorOrganisationIdentifier:<null>]",
                procedureUnitLpr.toString());
        assertEquals(
                "LPR[patientCpr:EBCE374754BE6ABA40CD9F62DB0CDECFA3CC1F2C,admittedInterval:2011-02-15T12:00:00.000Z/2011-02-27T15:00:00.000Z,"
                        + "lprReference:" + REFERENCE + ",relationType:DISCHARGED_TO_UNIT,"
                        + "hospitalOrganisationIdentifier:1234,doctorOrganisationIdentifier:<null>]",
                dischargedLpr.toString());
        assertEquals(
                "LPR[patientCpr:EBCE374754BE6ABA40CD9F62DB0CDECFA3CC1F2C,admittedInterval:2011-02-15T12:00:00.000Z/2011-02-27T15:00:00.000Z,"
                        + "lprReference:" + REFERENCE + ",relationType:DUSAS,"
                        + "hospitalOrganisationIdentifier:<null>,doctorOrganisationIdentifier:654321]",
                dusasLpr.toString());
    }

    @Test
    public void relationTypeKnowsHowToParseItselfFromTypeS() {
        assertEquals(LprRelationType.PARENT_UNIT, LprRelationType.fromStringRepresentation("S"));
    }

    @Test
    public void relationTypeKnowsHowToParseItselfFromTypeP() {
        assertEquals(LprRelationType.PROCEDURE_UNIT, LprRelationType.fromStringRepresentation("P"));
    }

    @Test
    public void relationTypeKnowsHowToParseItselfFromTypeU() {
        assertEquals(LprRelationType.DISCHARGED_TO_UNIT, LprRelationType.fromStringRepresentation("U"));
    }

    @Test
    public void relationTypeKnowsHowToParseItselfFromTypeY() {
        assertEquals(LprRelationType.DUSAS, LprRelationType.fromStringRepresentation("Y"));
    }
}
