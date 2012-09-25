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
package dk.nsi.sdm4.ydelse.dao.impl;

import dk.nsi.sdm4.ydelse.common.exception.DAOException;
import dk.nsi.sdm4.ydelse.common.splunk.SplunkLogger;
import dk.nsi.sdm4.ydelse.dao.SSRWriteDAO;
import dk.nsi.sdm4.ydelse.relation.model.DoctorOrganisationIdentifier;
import dk.nsi.sdm4.ydelse.relation.model.HashedCpr;
import dk.nsi.sdm4.ydelse.relation.model.SSR;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

public class SSRDAOImpl implements SSRWriteDAO {
    private static final SplunkLogger log = new SplunkLogger(SSRDAOImpl.class);

	@Autowired
	JdbcTemplate jdbcTemplate;
	private SimpleJdbcInsert basedataInserter;

	@PostConstruct
	protected void setupInserter() {
		// vi bruger primært SimpleJdbcInsert fordi det er den letteste måde at få fat i den genererede primærnøgle fra databasen
		basedataInserter = new SimpleJdbcInsert(jdbcTemplate)
				.withTableName("SSR")
				.usingColumns("patientCpr", "doctorOrganisationIdentifier", "admittedStart", "admittedEnd", "externalReference")
				.usingGeneratedKeyColumns("pk");
	}

    @Override
    public long insert(SSR ssr) throws DAOException {
        long pk = insertBaseData(ssr);
        log.debug("SSR inserted", "SSR", ssr.toString());
        return pk;
    }

	@SuppressWarnings("unchecked")
	private long insertBaseData(final SSR ssr) {
	    return basedataInserter.executeAndReturnKey(new HashMap() {{
	            put("patientCpr", ssr.getPatientCpr().getHashedCpr());
		        put("doctorOrganisationIdentifier", ssr.getDoctorOrganisationIdentifier().toString());
		        put("admittedStart", new Timestamp(ssr.getTreatmentInterval().getStartMillis()));
		        put("admittedEnd", new Timestamp(ssr.getTreatmentInterval().getEndMillis()));
		        put("externalReference", ssr.getExternalReference());
	        }}).longValue();
    }

    @Override
    public SSR getUsingPrimaryKey(long pk) throws DAOException {
	    try {
            return jdbcTemplate.queryForObject("SELECT * FROM SSR WHERE pk=?", new SSRMapper(), pk);
	    } catch (EmptyResultDataAccessException e) {
		    throw new DAOException("No SSR with primary key " + pk);
	    } catch (RuntimeException e) {
		    throw new DAOException("Unable to retrieve SSR with primary key " + pk, e);
	    }
    }

    @Override
    public List<SSR> query(HashedCpr patientCpr, DoctorOrganisationIdentifier doctorOrganisationIdentifier)
            throws DAOException {
        List<SSR> resultSSR;
        long startQueryTimestamp, endQueryTimestamp;

        try {
            startQueryTimestamp = System.currentTimeMillis();
            resultSSR = jdbcTemplate.query("SELECT * FROM SSR WHERE patientCpr=? AND doctorOrganisationIdentifier=?", new SSRMapper(),
		            patientCpr.getHashedCpr(), doctorOrganisationIdentifier.toString());
            endQueryTimestamp = System.currentTimeMillis();
        } catch (RuntimeException e) {
            throw new DAOException("Unable to query database.", e);
        }

	    log.debug("SSR query done", "patientCpr", patientCpr.getHashedCpr(), "doctorOrganisationIdentifier",
                doctorOrganisationIdentifier.toString(), "numberOfFoundSSR", Integer.toString(resultSSR.size()),
                "durationOfQuery", Long.toString(endQueryTimestamp - startQueryTimestamp));

	    return resultSSR;
    }
    @Override
    public void deleteByExternalReference(String externalReference) throws DAOException {
	    try {
	        int numRows = jdbcTemplate.update("DELETE FROM SSR WHERE externalReference = ?", externalReference);
            log.debug("Deleted " + numRows + " for externalReference " + externalReference);
        } catch (RuntimeException e) {
            throw new DAOException("Unable to delete records with external reference " + externalReference
                    + " from database", e);
        }
    }

	class SSRMapper implements RowMapper<SSR> {
		@Override
		public SSR mapRow(ResultSet resultSet, int rowNum) throws SQLException {
			Interval admittedInterval = new Interval(new DateTime(resultSet.getTimestamp("admittedStart")), new DateTime(
					resultSet.getTimestamp("admittedEnd")));
			return SSR.createInstance(HashedCpr.buildFromHashedString(resultSet.getString("patientCpr")),
					DoctorOrganisationIdentifier.newInstance(resultSet.getString("doctorOrganisationIdentifier")),
					admittedInterval, resultSet.getString("externalReference"));
		}
	}
}
