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

import dk.nsi.sdm4.ydelse.common.splunk.SplunkToStringStyle;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * Immutable representation of SSR (LandsPatientRegister) record.
 * 
 */
public class SSR {

    public static final int REFERENCE_LENGTH = 24;
    private HashedCpr patientCpr;
    private DoctorOrganisationIdentifier doctorOrganisationIdentifier;

    private Interval admittedInterval;

    private String externalReference;

    private SSR() {
    }

    private SSR(SSR ssr) {
        this.patientCpr = ssr.patientCpr;
        this.doctorOrganisationIdentifier = ssr.doctorOrganisationIdentifier;
        this.admittedInterval = ssr.admittedInterval;
        this.externalReference = ssr.externalReference;
    }

    public static SSR createInstance(HashedCpr patientCpr, DoctorOrganisationIdentifier doctorOrganisationIdentifier,
            Interval admittedInterval, String externalReference) {
        SSR ssr = new SSR();

        return ssr.withPatientCpr(patientCpr).withDoctorOrganisationIdentifier(doctorOrganisationIdentifier)
                .withTreatmentIntervalIgnoringMillis(admittedInterval).withExternalReference(externalReference);
    }

    // Getters

    public HashedCpr getPatientCpr() {
        return patientCpr;
    }

    public DoctorOrganisationIdentifier getDoctorOrganisationIdentifier() {
        return doctorOrganisationIdentifier;
    }

    public Interval getTreatmentInterval() {
        return admittedInterval;
    }

    public String getExternalReference() {
        return externalReference;
    }

    // New instances

    public SSR withPatientCpr(HashedCpr patientCpr) {
        if (patientCpr == null) {
            throw new IllegalArgumentException("Patient cpr must be non-null.");
        }

        SSR ssr = new SSR(this);
        ssr.patientCpr = patientCpr;
        return ssr;
    }

    public SSR withDoctorOrganisationIdentifier(DoctorOrganisationIdentifier doctorOrganisationIdentifier) {
        if (doctorOrganisationIdentifier == null) {
            throw new IllegalArgumentException("Doctor organisation identifer must be non-null.");
        }

        SSR ssr = new SSR(this);
        ssr.doctorOrganisationIdentifier = doctorOrganisationIdentifier;
        return ssr;
    }

    // SSR records always have an entire day - this class does not enforce this
    public SSR withTreatmentIntervalIgnoringMillis(Interval admittedInterval) {
        if (admittedInterval == null) {
            throw new IllegalArgumentException("Admitted interval must be non-null.");
        }

        DateTime start = admittedInterval.getStart().minusMillis(admittedInterval.getStart().getMillisOfSecond());
        DateTime end = admittedInterval.getEnd().minusMillis(admittedInterval.getEnd().getMillisOfSecond());
        admittedInterval = new Interval(start, end);

        SSR ssr = new SSR(this);
        ssr.admittedInterval = admittedInterval;
        return ssr;
    }
    
    public SSR withExternalReference(String externalReference) {
        if (externalReference == null) {
            throw new IllegalArgumentException("External reference must be non-null.");
        }
        
        if (externalReference.length() != REFERENCE_LENGTH) {
            throw new IllegalArgumentException("Illegal external reference length: " + externalReference.length()
                    + ". Only valid length is " + REFERENCE_LENGTH);
        }

        SSR ssr = new SSR(this);
        ssr.externalReference = externalReference;
        return ssr;
    }

    // Standard methods

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, SplunkToStringStyle.SPLUNK_TO_STRING_STYLE);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SSR other = (SSR) obj;

        return EqualsBuilder.reflectionEquals(this, other);
    }

}
