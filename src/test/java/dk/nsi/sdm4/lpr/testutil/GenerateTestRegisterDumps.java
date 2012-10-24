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
package dk.nsi.sdm4.lpr.testutil;

import dk.nsi.sdm4.lpr.relation.model.LPR;
import dk.nsi.sdm4.lpr.simulation.RandomLPR;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

import static dk.nsi.sdm4.lpr.relation.model.LPR.LprRelationType;

public class GenerateTestRegisterDumps {
	public static final String LPR_OUTPUT_FILE_NAME = "lpr_foo_bar.csv";
	public static final int LPR_SEED = 1337;
	private final static SimpleDateFormat lprFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");

	@Autowired
	RandomLPR randomLPR;

	public void generateTestRegisterDumps(File path, int numberOfRecords) {
		RandomLPR randomLPR1 = new RandomLPR();
		randomLPR1.setSeed(LPR_SEED);

		File file = new File(path, LPR_OUTPUT_FILE_NAME);
		try {
			FileWriter fileWriter = new FileWriter(file);

			for (LPR lpr : randomLPR1.randomLPRs(numberOfRecords)) {
				CommaConcat concat = new CommaConcat(";");

				if (lpr.getRelationType().definesDoctor()) {
					concat.add(lpr.getDoctorOrganisationIdentifier());
				} else {
					concat.add(lpr.getHospitalOrganisationIdentifier());
				}
				concat.add(lpr.getPatientCpr().getHashedCpr());

				if (lpr.getRelationType() == LprRelationType.DISCHARGED_TO_UNIT) {
					concat.add(lprFormat.format(lpr.getAdmittedInterval().getStart().toDate()));
					concat.add(lprFormat.format(lpr.getAdmittedInterval().getStart().toDate()));
				} else {
					concat.add(lprFormat.format(lpr.getAdmittedInterval().getStart().toDate()));
					concat.add(lprFormat.format(lpr.getAdmittedInterval().getEnd().toDate()));
				}

				concat.add(lpr.getRelationType().getStringRepresentation());
				concat.add(lpr.getLprReference());

				String line = concat.toString();
				fileWriter.write(line);
				fileWriter.write(System.getProperty("line.separator"));
			}

			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
