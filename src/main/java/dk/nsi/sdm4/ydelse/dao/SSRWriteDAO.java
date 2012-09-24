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
package dk.nsi.sdm4.ydelse.dao;

import dk.nsi.sdm4.ydelse.relation.model.SSR;

public interface SSRWriteDAO extends SSRReadDAO {

    /**
     * Inserts the {@link SSR} in the database The patient cpr is assumed to be
     * unhashed. A hashed version will be inserted in the database.
     * 
     * @param ssr
     *            The {@link SSR} to insert
     * @return long The primary key of the newly inserted {@link SSR}
     * @throws dk.nsi.sdm4.ydelse.common.exception.DAOException
     *             if something goes wrong in the process
     */
    public long insert(SSR ssr);

    /**
     * Deletes all {@link SSR}s in the database with the given external
     * reference.
     * 
     * @param externalReference
     *            The reference in the original SSR database
     * @throws dk.nsi.sdm4.ydelse.common.exception.DAOException
     *             if something goes wrong in the process
     */
    public void deleteByExternalReference(String externalReference);
}
