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
package dk.nsi.sdm4.ydelse.simulation;

import dk.nsi.sdm4.ydelse.relation.model.DoctorOrganisationIdentifier;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.management.relation.RelationType;
import java.util.*;

public class RandomDataUtilForTestPurposes {

	private Random random = new Random();

	public void setSeed(long seed) {
		random = new Random(seed);
	}

	public void setRandom(Random random) {
		this.random = random;
	}

	public String randomIntegerSequence(int length) {
		String result = "";
		for (int i = 0; i < length; i++) {
			result += random.nextInt(10);
		}
		return result;
	}

	private DateTime oldestBirthDate = new DateTime(1930, 1, 1, 0, 0, 0, 0);
	private DateTime youngestBirthDate = new DateTime(1990, 1, 1, 0, 0, 0, 0);

	public String randomCpr() {
		DateTime dayOfBirth = randomTime(oldestBirthDate, youngestBirthDate);

		DateTimeFormatter formatter = DateTimeFormat.forPattern("ddMMyy");

		String datePart = formatter.print(dayOfBirth);

		String numberPart = randomIntegerSequence(4);

		return datePart + numberPart;
	}

	public String randomCvr() {
		return randomIntegerSequence(8);
	}

	public DateTime randomTime(DateTime begin, DateTime end) {
		long low = begin.getMillis();
		long high = end.getMillis();
		double span = high - low;
		double factor = random.nextDouble();
		long dateInMillis = low + ((long) (factor * span));

		return new DateTime(dateInMillis);
	}

	public String randomAuthorisationIdentifier() {
		int randomId = random.nextInt(9000000) + 1000000;
		return "auth:" + Integer.toString(randomId);
	}

	public String randomExternalReference() {
		return "external:" + random.nextInt();
	}

	public String randomExternalSsrReference() {
	    return "external:" + randomIntegerSequence(24 - 9);
	}
	
	public String randomExternalLprReference() {
	    return "external:" + randomIntegerSequence(60 - 9);
	}
	
	public double randomDouble() {
		return random.nextDouble();
	}

	public int nextInt(int n) {
		return random.nextInt(n);
	}

	public <T> T choice(List<T> records) {
		int index = random.nextInt(records.size());
		return records.get(index);
	}
}
