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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Model object for doctor organisation identifier ("ydernummer" in Danish).
 * Objects of this class are ensured to have the correct format according to
 * Danish standards. Objects are immutable.
 * 
 * @author tgk
 * 
 */
public class DoctorOrganisationIdentifier implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final Set<Integer> VALID_LENGTHS_OF_DOCTOR_ORGANISATION_IDENTIFIERS;
	static {
		Set<Integer> validLengthsOfDoctorOrganisationIdentifiers = new HashSet<Integer>();
		validLengthsOfDoctorOrganisationIdentifiers.add(5);
		validLengthsOfDoctorOrganisationIdentifiers.add(6);
		VALID_LENGTHS_OF_DOCTOR_ORGANISATION_IDENTIFIERS = Collections
				.unmodifiableSet(validLengthsOfDoctorOrganisationIdentifiers);
	}

	public static boolean isStringValidDoctorOrganisationIdentifier(String s) {
		return VALID_LENGTHS_OF_DOCTOR_ORGANISATION_IDENTIFIERS.contains(s.length());
	}

	public static DoctorOrganisationIdentifier newInstance(String doctorOrganisationIdentiferStringRepresentation) {
		if (isStringValidDoctorOrganisationIdentifier(doctorOrganisationIdentiferStringRepresentation)) {
			return new DoctorOrganisationIdentifier(doctorOrganisationIdentiferStringRepresentation);
		} else {
			throw new IllegalArgumentException("Invalid doctor organisation identifier: "
					+ doctorOrganisationIdentiferStringRepresentation
					+ ". A doctor organisation identifier (ydernummer) must have a length of one of "
					+ VALID_LENGTHS_OF_DOCTOR_ORGANISATION_IDENTIFIERS);
		}
	}

	private String stringRepresentation;

	private DoctorOrganisationIdentifier(String stringRepresentation) {
		this.stringRepresentation = stringRepresentation;
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
}
