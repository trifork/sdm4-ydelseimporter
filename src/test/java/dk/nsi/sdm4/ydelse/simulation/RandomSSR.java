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
import dk.nsi.sdm4.ydelse.relation.model.HashedCpr;
import dk.nsi.sdm4.ydelse.relation.model.SSR;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RandomSSR {
    // Needed for some tests
    public static Map<SSR, String> unhashedPatientCprnumbers = new HashMap<SSR, String>();

    private boolean saveUnhashedCprNumbers = true;

    RandomDataUtilForTestPurposes testDataUtil;

	public RandomSSR(RandomDataUtilForTestPurposes testDataUtil) {
		this.testDataUtil = testDataUtil;
	}

	public SSR randomSSR() {
        return build();
    }

    public void setSeed(long seed) {
        testDataUtil.setSeed(seed);
    }

    public List<SSR> randomSSRs(int n) {
        List<SSR> result = new ArrayList<SSR>();

        for (int i = 0; i < n; i++) {
            result.add(randomSSR());
        }

        return result;
    }

    private String patientCpr;
    private DoctorOrganisationIdentifier doctorOrganisationIdentifier;
    private Interval treatmentInterval;
    private String externalReference;

    private SSR build() {
        setRandomPatientCpr();
        setRandomDoctorOrganisationIdentifier();
        setRandomTreatmentTime();
        setRandomExternalReference();

        SSR ssr = SSR.createInstance(HashedCpr.buildFromUnhashedString(patientCpr), doctorOrganisationIdentifier,
                treatmentInterval, externalReference);

        if (saveUnhashedCprNumbers) {
            unhashedPatientCprnumbers.put(ssr, patientCpr);
        }

        return ssr;
    }

    private void setRandomPatientCpr() {
        patientCpr = testDataUtil.randomCpr();
    }

    private void setRandomDoctorOrganisationIdentifier() {
        doctorOrganisationIdentifier = testDataUtil.randomDoctorOrganisationIdentifier();
    }

    private void setRandomTreatmentTime() {
        DateTime earliestTreatmentStart = new DateTime(2010, 12, 24, 0, 0, 0, 0);
        DateTime latestTreatmentStart = new DateTime(2011, 1, 31, 0, 0, 0, 0);
        DateTime treatet = testDataUtil.randomTime(earliestTreatmentStart, latestTreatmentStart);
        treatet = treatet.withTime(0, 0, 0, 0);

        treatmentInterval = new Interval(treatet, treatet.plusDays(1));
    }

    private void setRandomExternalReference() {
        this.externalReference = testDataUtil.randomExternalSsrReference();
    }
}
