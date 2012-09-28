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

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SSRTest {

    private static final DoctorOrganisationIdentifier DOCTOR_ORG_ID = DoctorOrganisationIdentifier.newInstance("45753");
    private static final HashedCpr PATIENT_CPR = HashedCpr.buildFromUnhashedString("1806861234");
    private static final String EXTERNAL_REF = "AnExternalReferenceToSSR";
    private SSR exampleSSR, sameSSR;

    @Before
    public void setup() {
        exampleSSR = generateSSR();
        sameSSR = generateSSR();
    }

    public static SSR generateSSR() {
	    DateTimeZone zone = DateTimeZone.UTC;
	    Interval admittedInterval = new Interval(new DateTime(2011, 1, 15, 12, 34, 0, 0, zone),
		                                         new DateTime(2011, 2, 13, 18, 59, 0, 0, zone));
        return SSR.createInstance(PATIENT_CPR, DOCTOR_ORG_ID, admittedInterval, EXTERNAL_REF);
    }

    @Test
    public void testGetters() {
        assertEquals(DOCTOR_ORG_ID, exampleSSR.getDoctorOrganisationIdentifier());
        assertEquals(PATIENT_CPR, exampleSSR.getPatientCpr());
        assertEquals(EXTERNAL_REF, exampleSSR.getExternalReference());
    }

    @Test
    public void testNoNewObjectsWithNull() {
        try {
            SSR.createInstance(null, DoctorOrganisationIdentifier.newInstance("12345"), new Interval(0, 1),
                    EXTERNAL_REF);
            fail("Should be unable to set null values");
        } catch (IllegalArgumentException e) {

        }

        try {
            SSR.createInstance(null, null, new Interval(0, 1), EXTERNAL_REF);
            fail("Should be unable to set null values");
        } catch (IllegalArgumentException e) {

        }

        try {
            SSR.createInstance(null, DoctorOrganisationIdentifier.newInstance("12345"), null, EXTERNAL_REF);
            fail("Should be unable to set null values");
        } catch (IllegalArgumentException e) {

        }

        try {
            SSR.createInstance(null, DoctorOrganisationIdentifier.newInstance("12345"), new Interval(0, 1), null);
            fail("Should be unable to set null values");
        } catch (IllegalArgumentException e) {

        }

        try {
            exampleSSR.withPatientCpr(null);
            fail("Should be unable to set null values");
        } catch (IllegalArgumentException e) {

        }

        try {
            exampleSSR.withDoctorOrganisationIdentifier(null);
            fail("Should be unable to set null values");
        } catch (IllegalArgumentException e) {

        }

        try {
            exampleSSR.withTreatmentIntervalIgnoringMillis(null);
            fail("Should be unable to set null values");
        } catch (IllegalArgumentException e) {

        }

        try {
            exampleSSR.withExternalReference(null);
            fail("Should be unable to set null values");
        } catch (IllegalArgumentException e) {

        }
    }

    @Test
    public void testNoMilliseconds() {
        DateTime start = new DateTime(2010, 1, 1, 12, 30, 45, 0);
        DateTime end = new DateTime(2011, 1, 1, 12, 30, 45, 0);

        Interval intervalWhereStartHasMillis = new Interval(start.plusMillis(42), end);
        exampleSSR = exampleSSR.withTreatmentIntervalIgnoringMillis(intervalWhereStartHasMillis);
        assertEquals(start, exampleSSR.getTreatmentInterval().getStart());
        assertEquals(end, exampleSSR.getTreatmentInterval().getEnd());

        Interval intervalWhereEndHasMillis = new Interval(start, end.plusMillis(42));
        exampleSSR = exampleSSR.withTreatmentIntervalIgnoringMillis(intervalWhereEndHasMillis);
        assertEquals(start, exampleSSR.getTreatmentInterval().getStart());
        assertEquals(end, exampleSSR.getTreatmentInterval().getEnd());
    }

    @Test
    public void testExternalReferenceTooShort() {
        try {
            exampleSSR.withExternalReference("123");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Illegal external reference length: 3. Only valid length is 24", e.getMessage());
        }
    }

    @Test
    public void testExternalReferenceTooLong() {
        try {
            exampleSSR.withExternalReference("12345678901234567890123456789");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Illegal external reference length: 29. Only valid length is 24", e.getMessage());
        }
    }

    @Test
    public void testHashCode() {
        assertEquals(exampleSSR.hashCode(), sameSSR.hashCode());
    }

    @Test
    public void testEqualsObject() {
        assertFalse(exampleSSR.equals(null));

        assertFalse(exampleSSR.equals(new Object()));

        assertTrue(exampleSSR.equals(exampleSSR));

        assertTrue(exampleSSR.equals(sameSSR));

        assertFalse(exampleSSR.equals(exampleSSR.withPatientCpr(HashedCpr.buildFromUnhashedString("0000000000"))));

        assertFalse(exampleSSR.equals(exampleSSR.withDoctorOrganisationIdentifier(DoctorOrganisationIdentifier
                .newInstance("12345"))));

        DateTime now = new DateTime();
        Interval interval = new Interval(now.minusDays(1), now);
        assertFalse(exampleSSR.equals(exampleSSR.withTreatmentIntervalIgnoringMillis(interval)));

        assertFalse(exampleSSR.equals(exampleSSR.withExternalReference("AnotherExternalReference")));
    }

    @Test
    public void testToString() {
        assertEquals("SSR" + "[patientCpr:EBCE374754BE6ABA40CD9F62DB0CDECFA3CC1F2C,doctorOrganisationIdentifier:45753,"
                + "admittedInterval:2011-01-15T12:34:00.000Z/2011-02-13T18:59:00.000Z,"
                + "externalReference:AnExternalReferenceToSSR]", exampleSSR.toString());
    }
}
