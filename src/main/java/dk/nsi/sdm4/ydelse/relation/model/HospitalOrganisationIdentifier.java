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
import java.util.*;

public class HospitalOrganisationIdentifier implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final Set<Integer> VALID_LENGTHS_OF_HOSPITAL_ORGANISATION_IDENTIFIERS;
	static {
		Set<Integer> validLengthsOfHospitalOrganisationIdentifiers = new HashSet<Integer>();
		validLengthsOfHospitalOrganisationIdentifiers.add(4);
		validLengthsOfHospitalOrganisationIdentifiers.add(6);
		validLengthsOfHospitalOrganisationIdentifiers.add(7);
		VALID_LENGTHS_OF_HOSPITAL_ORGANISATION_IDENTIFIERS = Collections
				.unmodifiableSet(validLengthsOfHospitalOrganisationIdentifiers);
	}

	/**
	 * Attempts to parse a hospital organisation identifier (sks) consisting of
	 * a list of codes. The codes must all share common prefixes, e.g.
	 * \"7474,7474061,747406\".
	 * 
	 * @param s
	 *            hospital organisation identifier string representation, e.g.
	 *            \"7474,7474061,747406\" or just \"7474061\"
	 */
	public static HospitalOrganisationIdentifier newInstance(String s) {
		int asManyTimesAsPossible = -1;
		String[] tokens = s.split(",", asManyTimesAsPossible);

		Comparator<String> lengthComparator = new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.length() - o2.length();
			}
		};

		List<String> sortedHospitalOrganisationIdentifiers = new ArrayList<String>();
        Collections.addAll(sortedHospitalOrganisationIdentifiers, tokens);
		Collections.sort(sortedHospitalOrganisationIdentifiers, lengthComparator);

		String previousPrefix = "";
		for (String hospitalOrganisationIdString : sortedHospitalOrganisationIdentifiers) {
			if (!hospitalOrganisationIdString.startsWith(previousPrefix)) {
				throw new IllegalArgumentException(
						"The hospital organisation ids are invalid. The hierachi is broken: " + s);
			}
			previousPrefix = hospitalOrganisationIdString;
		}

		return HospitalOrganisationIdentifier.instanceFromSingleIdentifier(
				sortedHospitalOrganisationIdentifiers.get(sortedHospitalOrganisationIdentifiers.size() - 1), s);
	}

	private static HospitalOrganisationIdentifier instanceFromSingleIdentifier(
			String hospitalOrganisationIdentifierStringRepresentation, String originalInput) {
		if (isStringLengthValidHospitalOrganisationIdentifierAsSingleSymbol(hospitalOrganisationIdentifierStringRepresentation)) {
			return new HospitalOrganisationIdentifier(hospitalOrganisationIdentifierStringRepresentation);
		} else {
			throw new IllegalArgumentException("Invalid hospital organisation identifier: " + originalInput
					+ ". A hospital organisation identifier (sks) must have a length of one of "
					+ VALID_LENGTHS_OF_HOSPITAL_ORGANISATION_IDENTIFIERS);
		}
	}

	private static boolean isStringLengthValidHospitalOrganisationIdentifierAsSingleSymbol(String s) {
		return VALID_LENGTHS_OF_HOSPITAL_ORGANISATION_IDENTIFIERS.contains(s.length());
	}

	private String stringRepresentation;

	private HospitalOrganisationIdentifier(String stringRepresentation) {
		this.stringRepresentation = stringRepresentation;
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}

	/**
	 * Extracts the topCode of a SKS - the four first digits.
	 */
	public String topCode() {
		return stringRepresentation.substring(0, 4);
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
