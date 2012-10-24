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

import dk.nsi.sdm4.ydelse.relation.model.DoctorOrganisationIdentifier;
import dk.nsi.sdm4.ydelse.relation.model.Ean;
import dk.nsi.sdm4.ydelse.relation.model.HospitalOrganisationIdentifier;
import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class CommaConcat {

    protected List<String> strings;
    private String seperator;

    public CommaConcat(String seperator) {
        this.seperator = seperator;
        strings = new ArrayList<String>();
    }

    public CommaConcat() {
        this(",");
    }

    public void addEmpty() {
        strings.add("");
    }

    public void add(int i) {
        strings.add(Integer.toString(i));
    }

    public void add(String s) {
        strings.add(s);
    }

    public void add(DoctorOrganisationIdentifier doctorOrganisationIdentifier) {
        strings.add(doctorOrganisationIdentifier.toString());
    }

    public void add(HospitalOrganisationIdentifier hospitalOrganisationIdentifier) {
        strings.add(hospitalOrganisationIdentifier.toString());
    }

    public void add(Ean ean) {
        strings.add(ean.toString());
    }

    private final static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ");

    public void add(DateTime dateTime) {
        strings.add(format.format(dateTime.toDate()));
    }

    public void addEmptyOnNull(String s) {
        if (s == null) {
            addEmpty();
        } else {
            add(s);
        }
    }

    public void addEmptyOnNull(DoctorOrganisationIdentifier doctorOrganisationIdentifier) {
        if (doctorOrganisationIdentifier == null) {
            addEmpty();
        } else {
            add(doctorOrganisationIdentifier);
        }
    }

    public void addEmptyOnNull(HospitalOrganisationIdentifier hospitalOrganisationIdentifier) {
        if (hospitalOrganisationIdentifier == null) {
            addEmpty();
        } else {
            add(hospitalOrganisationIdentifier);
        }
    }

    public void addEmptyOnNull(Ean ean) {
        if (ean == null) {
            addEmpty();
        } else {
            add(ean);
        }
    }

    public void addEmptyOnNull(DateTime dateTime) {
        if (dateTime == null) {
            addEmpty();
        } else {
            add(dateTime);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (String s : strings) {
            builder.append(s);
            builder.append(seperator);
        }

        return builder.substring(0, builder.length() - 1);
    }
}
