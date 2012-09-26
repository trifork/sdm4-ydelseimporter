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
package dk.nsi.sdm4.ydelse.testutil;

import dk.nsi.sdm4.ydelse.relation.model.SSR;
import dk.nsi.sdm4.ydelse.simulation.RandomSSR;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class GenerateTestRegisterDumps {
	private final static SimpleDateFormat ssrFormat = new SimpleDateFormat("yyyyMMdd");

	@Autowired
	RandomSSR randomSSR;

	public GenerateTestRegisterDumps() {
	}

    public List<SSR> generateSsrDumps(File root, int records) {
	    return dumpSsrs(root, randomSSR.randomSSRs(records));
    }

	public void generateSingleDeletion(File root, String externalReference) {
		File file = new File(root, "ssr_foo_bar.csv");
		SsrWriter writer = new SsrWriter(file);
		try {
			writer.write(makeDeletionLine(externalReference));
		} finally {
			writer.closeQuietly();
		}
	}

	private String makeDeletionLine(String externalReference) {
		String all_blank_fields_except_externalReference = ",,,                                                              ,";
		return all_blank_fields_except_externalReference + externalReference;
	}

	public List<SSR> dumpSsrs(File root, List<SSR> ssrs) {
		File file = new File(root, "ssr_foo_bar.csv");
		SsrWriter fileWriter = new SsrWriter(file);
		try {
			for (SSR ssr : ssrs) {
				fileWriter.write(new SsrCommaConcat(ssr).toString());
	        }

		    return ssrs;
	    } finally {
			fileWriter.closeQuietly();
		}
	}

	class SsrWriter {
		FileWriter fileWriter;

		public SsrWriter(File file) {
			try {
				this.fileWriter = new FileWriter(file);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public void write(String line) {
			try {
				fileWriter.write(line);
				fileWriter.write(System.getProperty("line.separator"));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public void closeQuietly() {
			IOUtils.closeQuietly(fileWriter);
		}
	}

	class SsrCommaConcat {
		CommaConcat concat = new CommaConcat();

		public SsrCommaConcat(SSR ssr) {
			concat.add(ssr.getDoctorOrganisationIdentifier());
			concat.add(ssr.getPatientCpr().getHashedCpr());
			DateTime start = ssr.getTreatmentInterval().getStart();
			DateTime end = ssr.getTreatmentInterval().getEnd();
			assert (end.equals(start.plusDays(1)));
			concat.add(ssrFormat.format(start.toDate()));
			concat.add(ssrFormat.format(start.toDate()));
			concat.add(ssr.getExternalReference());
		}

		@Override
		public String toString() {
			return concat.toString();
		}
	}
}
