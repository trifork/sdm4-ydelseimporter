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
package dk.nsi.sdm4.lpr.dao;

import dk.nsi.sdm4.lpr.common.exception.DAOException;
import dk.nsi.sdm4.lpr.relation.model.DoctorOrganisationIdentifier;
import dk.nsi.sdm4.lpr.relation.model.HashedCpr;
import dk.nsi.sdm4.lpr.relation.model.HospitalOrganisationIdentifier;
import dk.nsi.sdm4.lpr.relation.model.LPR;

import java.util.List;

public interface LPRReadDAO {

	/**
	 * Retrieves the {@link LPR} given the provided primary key
	 * 
	 * @param primaryKey
	 *            The primary key of the {@link LPR} in question
	 * @return {@link LPR} The {@link LPR} with the provided primary key
	 * @throws DAOException
	 *             if something goes wrong in the process
	 */
	public LPR getUsingPrimaryKey(long primaryKey) throws DAOException;

	/**
	 * Retrieves a list of {@link LPR}s given various properties. Does not
	 * discriminate on the basis of time intervals. Hashes the patient cpr
	 * number before performing the query.
	 * 
	 * @param patientCpr
	 *            The cpr number of the patient
	 * @param hospitalOrganisationIdentifier
	 *            The doctor organisation identifier as "sks"
	 * @return List of {@link LPR}s matching the query
	 * @throws DAOException
	 *             if something goes wrong in the process
	 */
	public List<LPR> queryHospitalOrganisationIdentifier(HashedCpr patientCpr,
	                                                     HospitalOrganisationIdentifier hospitalOrganisationIdentifier) throws DAOException;

	/**
	 * Retrieves a list of {@link LPR}s given various properties. Does not
	 * discriminate on the basis of time intervals. Hashes the patient cpr
	 * number before performing the query.
	 * 
	 * @param patientCpr
	 *            The cpr number of the patient
	 * @param doctorOrganisationIdentifier
	 *            The doctor organisation identifier as "ydernummer"
	 * @return List of {@link LPR}s matching the query
	 * @throws DAOException
	 *             if something goes wrong in the process
	 */
	public List<LPR> queryDoctorOrganisationIdentifier(HashedCpr patientCpr,
	                                                   DoctorOrganisationIdentifier doctorOrganisationIdentifier) throws DAOException;

    long insertOrUpdate(LPR lpr) throws DAOException;

}