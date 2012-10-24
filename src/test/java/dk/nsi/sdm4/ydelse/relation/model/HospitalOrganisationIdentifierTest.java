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

import org.junit.Test;

import static org.junit.Assert.*;

public class HospitalOrganisationIdentifierTest {

	@Test
	public void testCorrectNewInstance() {
		HospitalOrganisationIdentifier.newInstance("7707");
		HospitalOrganisationIdentifier.newInstance("7707,7707");
		HospitalOrganisationIdentifier.newInstance("7707,770761");
		HospitalOrganisationIdentifier.newInstance("7707,770761,7707612");

		HospitalOrganisationIdentifier.newInstance("770761,7707");
		HospitalOrganisationIdentifier.newInstance("770761,7707,7707612");
	}

	@Test
	public void testNewInstanceWithEmptyString() {
		helperForFailingNewInstance(
				"",
				"Invalid hospital organisation identifier: . A hospital organisation identifier (sks) must have a length of one of [4, 6, 7]");
		helperForFailingNewInstance(
				",",
				"Invalid hospital organisation identifier: ,. A hospital organisation identifier (sks) must have a length of one of [4, 6, 7]");
	}

	@Test
	public void testNewInstanceWithConflictingTopCodes() {
		helperForFailingNewInstance("7707,7706",
				"The hospital organisation ids are invalid. The hierachi is broken: 7707,7706");
		helperForFailingNewInstance("7707,770612",
				"The hospital organisation ids are invalid. The hierachi is broken: 7707,770612");
		helperForFailingNewInstance("770761,770762",
				"The hospital organisation ids are invalid. The hierachi is broken: 770761,770762");
	}

	@Test
	public void testNewInstanceWithWrongLengthOnTopCodes() {
		helperForFailingNewInstance("77077", "Invalid hospital organisation identifier: 77077. "
				+ "A hospital organisation identifier (sks) must have a length of one of [4, 6, 7]");
		helperForFailingNewInstance("7707,77077", "Invalid hospital organisation identifier: 7707,77077. "
				+ "A hospital organisation identifier (sks) must have a length of one of [4, 6, 7]");
		helperForFailingNewInstance("770", "Invalid hospital organisation identifier: 770. "
				+ "A hospital organisation identifier (sks) must have a length of one of [4, 6, 7]");
	}

	private void helperForFailingNewInstance(String stringToParse, String expectedIllegalArgumentExceptionMessage) {
		try {
			HospitalOrganisationIdentifier.newInstance(stringToParse);
			fail("Expected IllegalArgumentException with message: " + expectedIllegalArgumentExceptionMessage);
		} catch (IllegalArgumentException e) {
			assertEquals(expectedIllegalArgumentExceptionMessage, e.getMessage());
		}
	}

	@Test
	public void testToString() {
		assertEquals("7707", HospitalOrganisationIdentifier.newInstance("7707").toString());
		assertEquals("770761", HospitalOrganisationIdentifier.newInstance("7707,770761").toString());
		assertEquals("770761", HospitalOrganisationIdentifier.newInstance("770761,7707").toString());
		assertEquals("7707612", HospitalOrganisationIdentifier.newInstance("7707,7707612").toString());
		assertEquals("7707612", HospitalOrganisationIdentifier.newInstance("7707,7707612,770761").toString());
	}

	@Test
	public void testTopCode() {
		assertEquals("7707", HospitalOrganisationIdentifier.newInstance("7707").topCode());
		assertEquals("7707", HospitalOrganisationIdentifier.newInstance("7707,770761").topCode());
		assertEquals("7707", HospitalOrganisationIdentifier.newInstance("770761,7707").topCode());
		assertEquals("7707", HospitalOrganisationIdentifier.newInstance("7707,7707612").topCode());
		assertEquals("7707", HospitalOrganisationIdentifier.newInstance("7707,7707612,770761").topCode());
	}

	@Test
	public void testHashCode() {
		assertEquals(1693532, HospitalOrganisationIdentifier.newInstance("7707").hashCode());
		assertEquals(1626882135, HospitalOrganisationIdentifier.newInstance("770761").hashCode());
		assertEquals(-1106280187, HospitalOrganisationIdentifier.newInstance("7707612").hashCode());
		assertEquals(HospitalOrganisationIdentifier.newInstance("7707,7707612").hashCode(),
				HospitalOrganisationIdentifier.newInstance("7707612").hashCode());
	}

	@Test
	public void testEquals() {
		assertFalse(HospitalOrganisationIdentifier.newInstance("7707").equals(null));
		assertFalse(HospitalOrganisationIdentifier.newInstance("7707").equals(new Object()));

		HospitalOrganisationIdentifier identity = HospitalOrganisationIdentifier.newInstance("7707");
		assertEquals(identity, identity);

		helperForTestEquals("770761", "770761");
		helperForTestEquals("7707,770761", "770761");
		helperForTestEquals("770761", "770761,7707");

		helperForTestNotEquals("7707", "770761");
		helperForTestNotEquals("7707", "770761,7707");
		helperForTestNotEquals("7707", "770761,7707");
	}

	private void helperForTestEquals(String lhs, String rhs) {
		HospitalOrganisationIdentifier lhsHOI = HospitalOrganisationIdentifier.newInstance(lhs);
		HospitalOrganisationIdentifier rhsHOI = HospitalOrganisationIdentifier.newInstance(rhs);
		assertEquals(lhsHOI, rhsHOI);
	}

	private void helperForTestNotEquals(String lhs, String rhs) {
		HospitalOrganisationIdentifier lhsHOI = HospitalOrganisationIdentifier.newInstance(lhs);
		HospitalOrganisationIdentifier rhsHOI = HospitalOrganisationIdentifier.newInstance(rhs);
		assertFalse(lhsHOI + " and " + rhsHOI + " should NOT be equal", lhsHOI.equals(rhsHOI));
	}
}
