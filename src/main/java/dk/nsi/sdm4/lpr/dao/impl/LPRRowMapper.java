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
package dk.nsi.sdm4.lpr.dao.impl;

import dk.nsi.sdm4.lpr.relation.model.GeneralInterval;
import dk.nsi.sdm4.lpr.relation.model.HashedCpr;
import dk.nsi.sdm4.lpr.relation.model.LPR;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

class LPRRowMapper implements RowMapper<LPR> {
	@Override
	public LPR mapRow(ResultSet resultSet, int rowNum) throws SQLException {
			String patientCpr = resultSet.getString("patientCpr");

			Timestamp admittedStart = resultSet.getTimestamp("admittedStart");
			Timestamp admittedEnd = resultSet.getTimestamp("admittedEnd");
			GeneralInterval admittedInterval;
			if (admittedEnd == null) {
				admittedInterval = GeneralInterval.openInterval(new DateTime(admittedStart));
			} else {
				admittedInterval = GeneralInterval.closedInterval(new Interval(new DateTime(admittedStart), new DateTime(
						admittedEnd)));
			}

			String lprReference = resultSet.getString("lprReference");

			LPR.LprRelationType relationType = LPR.LprRelationType.valueOf(resultSet.getString("relationType"));

			return LPR.newInstance(HashedCpr.buildFromHashedString(patientCpr), admittedInterval, lprReference,
					relationType, resultSet.getString("organisationIdentifier"));
	}
}
