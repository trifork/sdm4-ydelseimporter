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

import java.util.Arrays;

/**
 * Immutable representation of LPR (LandsPatientRegister) record. A record
 * represents a type of relation defined by the types in {@link LprRelationType}
 * . Depending on this type, the object will have different place properties. If
 * the record represents a relation to a referring unit or a treatment unit, the
 * object will have a hospital organisation id (sks). If the record represents a
 * patient discharged to own doctor, the object will have a doctor organisation
 * id (ydernummer).
 */
public class LPR {
    private static final int REFERENCE_LENGTH = 60;

    private HashedCpr patientCpr;
    private GeneralInterval admittedInterval;
    private String lprReference;

    private LprRelationType relationType;
    private HospitalOrganisationIdentifier hospitalOrganisationIdentifier; // sks
    private DoctorOrganisationIdentifier doctorOrganisationIdentifier; // ydernummer

    private LPR() {
    }

    LPR(LPR lpr) {
        this.patientCpr = lpr.patientCpr;
        this.doctorOrganisationIdentifier = lpr.doctorOrganisationIdentifier;
        this.hospitalOrganisationIdentifier = lpr.hospitalOrganisationIdentifier;
        this.admittedInterval = lpr.admittedInterval;
        this.relationType = lpr.relationType;
        this.lprReference = lpr.lprReference;
    }

    // New instance

    /**
     * Creates a new LPR record instance. The organisation identifier is stored
     * as a hospital organisation identifier if the {@link LprRelationType}
     * represents a hospital, and as a doctor organisation identifier if it
     * represents a doctor.
     */
    public static LPR newInstance(HashedCpr patientCpr, GeneralInterval admittedInterval, String lprReference,
                                  LprRelationType relationType, String organisationIdentifierString) {
        LPR lpr = (new LPR()).withPatientCpr(patientCpr).withAdmittedIntervalIgnoringMillis(admittedInterval)
                             .withLprReference(lprReference);
        lpr.relationType = relationType;

        if (relationType.definesHospital()) {
            lpr.hospitalOrganisationIdentifier = HospitalOrganisationIdentifier
                    .newInstance(organisationIdentifierString);
        } else if (relationType.definesDoctor()) {
            lpr.doctorOrganisationIdentifier = DoctorOrganisationIdentifier.newInstance(organisationIdentifierString);
        } else {
            throw new AssertionError("Relation type should define a hospital or a doctor.");
        }

        return lpr;
    }

    // Getters

    public HashedCpr getPatientCpr() {
        return patientCpr;
    }

    public GeneralInterval getAdmittedInterval() {
        return admittedInterval;
    }

    public String getLprReference() {
        return lprReference;
    }

    public LprRelationType getRelationType() {
        return this.relationType;
    }

    public HospitalOrganisationIdentifier getHospitalOrganisationIdentifier() {
        if (relationType.definesHospital()) {
            return hospitalOrganisationIdentifier;
        } else {
            throw new IllegalArgumentException(
                    "Hospital organisation identifier is not defined for this relation type (" + relationType + ").");
        }
    }

    public DoctorOrganisationIdentifier getDoctorOrganisationIdentifier() {
        if (relationType.definesDoctor()) {
            return doctorOrganisationIdentifier;
        } else {
            throw new IllegalArgumentException("Doctor organisation identifier is not defined for this relation type ("
                                                       + relationType + ").");
        }
    }

    // New objects from old

    public LPR withPatientCpr(HashedCpr patientCpr) {
        checkNotNull(patientCpr, "Patient cpr");

        LPR lpr = new LPR(this);
        lpr.patientCpr = patientCpr;
        return lpr;
    }

    public LPR withAdmittedIntervalIgnoringMillis(GeneralInterval admittedInterval) {
        checkNotNull(admittedInterval, "Admitted interval");

        LPR lpr = new LPR(this);
        lpr.admittedInterval = admittedInterval.withMillisRemoved().withSecondsRemoved();
        return lpr;
    }

    public LPR withLprReference(String lprReference) {
        checkNotNull(lprReference, "LPR reference");

        if (lprReference.length() != REFERENCE_LENGTH) {
            throw new IllegalArgumentException("The reference length is " + lprReference.length()
                                                       + " but should be exactly " + REFERENCE_LENGTH);
        }

        LPR lpr = new LPR(this);
        lpr.lprReference = lprReference;
        return lpr;
    }

    public LPR inParentUnit(HospitalOrganisationIdentifier hospitalOrganisationIdentifier) {
        checkNotNull(hospitalOrganisationIdentifier, "Hospital organisation identifier");

        LPR lpr = new LPR(this);
        lpr.relationType = LprRelationType.PARENT_UNIT;
        lpr.hospitalOrganisationIdentifier = hospitalOrganisationIdentifier;
        lpr.doctorOrganisationIdentifier = null;
        return lpr;
    }

    public LPR inProcedureUnit(HospitalOrganisationIdentifier hospitalOrganisationIdentifier) {
        checkNotNull(hospitalOrganisationIdentifier, "Hospital organisation identifier");

        LPR lpr = new LPR(this);
        lpr.relationType = LprRelationType.PROCEDURE_UNIT;
        lpr.hospitalOrganisationIdentifier = hospitalOrganisationIdentifier;
        lpr.doctorOrganisationIdentifier = null;
        return lpr;
    }

    public LPR dischargedToUnit(HospitalOrganisationIdentifier hospitalOrganisationIdentifier) {
        checkNotNull(hospitalOrganisationIdentifier, "Hospital organisation identifier");

        LPR lpr = new LPR(this);
        lpr.relationType = LprRelationType.DISCHARGED_TO_UNIT;
        lpr.hospitalOrganisationIdentifier = hospitalOrganisationIdentifier;
        lpr.doctorOrganisationIdentifier = null;
        return lpr;
    }

    public LPR withDusasContact(DoctorOrganisationIdentifier doctorOrganisationIdentifier) {
        checkNotNull(doctorOrganisationIdentifier, "Doctor organisation identifier");

        LPR lpr = new LPR(this);
        lpr.relationType = LprRelationType.DUSAS;
        lpr.hospitalOrganisationIdentifier = null;
        lpr.doctorOrganisationIdentifier = doctorOrganisationIdentifier;
        return lpr;
    }

    private static void checkNotNull(Object o, String variableName) {
        if (o == null) {
            throw new IllegalArgumentException(variableName + " must be non-null.");
        }
    }

    // Standard methods

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, Arrays.asList("relationType"));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        LPR other = (LPR)obj;

        EqualsBuilder equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(admittedInterval, other.admittedInterval);
        equalsBuilder.append(relationType, other.relationType);
        equalsBuilder.append(lprReference, other.lprReference);
        equalsBuilder.append(lprReference, other.lprReference);
        equalsBuilder.append(patientCpr, other.patientCpr);
        if (relationType == LprRelationType.PARENT_UNIT || relationType == LprRelationType.PROCEDURE_UNIT) {
            equalsBuilder.append(hospitalOrganisationIdentifier, other.hospitalOrganisationIdentifier);
        }
        if (relationType == LprRelationType.DUSAS) {
            equalsBuilder.append(doctorOrganisationIdentifier, other.doctorOrganisationIdentifier);
        }

        return equalsBuilder.isEquals();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, SplunkToStringStyle.SPLUNK_TO_STRING_STYLE);
    }

    public enum LprRelationType {
        // S - Stamafdeling - parent unit
        // P - Producerende afdeling - procedure unit
        // U - Udskrevet til sgh/afd - discharged to unit
        // Y - DUSAS - DUSAS

        PARENT_UNIT("S"), PROCEDURE_UNIT("P"), DISCHARGED_TO_UNIT("U"), DUSAS("Y");
        private String stringRepresentation;

        private LprRelationType(String stringRepresentation) {
            this.stringRepresentation = stringRepresentation;
        }

        public boolean definesHospital() {
            return this == PARENT_UNIT || this == PROCEDURE_UNIT || this == DISCHARGED_TO_UNIT;
        }

        public boolean definesDoctor() {
            return this == DUSAS;
        }

        public String getStringRepresentation() {
            return stringRepresentation;
        }

        /**
         * @param stringRepresentation S, P, U, Y or X
         */
        public static LprRelationType fromStringRepresentation(String stringRepresentation) {
            for (LprRelationType o : values()) {
                if (o.stringRepresentation.equals(stringRepresentation)) {
                    return o;
                }
            }

            throw new IllegalArgumentException("No LprRelationType with representation " + stringRepresentation
                                                       + " exist");
        }
    }
}
