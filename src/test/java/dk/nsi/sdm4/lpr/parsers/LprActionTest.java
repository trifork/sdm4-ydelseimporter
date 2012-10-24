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
package dk.nsi.sdm4.lpr.parsers;

import dk.nsi.sdm4.lpr.common.exception.DAOException;
import dk.nsi.sdm4.lpr.dao.LPRTestPurposeDAO;
import dk.nsi.sdm4.lpr.dao.impl.LPRTestPurposeDAOImpl;
import dk.nsi.sdm4.lpr.relation.model.LPR;
import dk.nsi.sdm4.lpr.simulation.RandomLPR;
import dk.nsi.sdm4.testutils.TestDbConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {LprActionTest.TestConfig.class, TestDbConfiguration.class})
public class LprActionTest {
	@Autowired
	private LPRTestPurposeDAO dao;

	@Autowired
	private RandomLPR randomLPR;

	@Configuration
	static class TestConfig {
		@Bean
		public LPRTestPurposeDAO dao() {
			return new LPRTestPurposeDAOImpl();
		}

		@Bean
		public RandomLPR randomLPR() {
			RandomLPR randomSSR = new RandomLPR();
			randomSSR.setSeed(42);
			return randomSSR;
		}
	}

	@Test
	public void insertionInserts() throws DAOException {
		LPR lprForInsertion = randomLPR.randomLPR();
		LprAction ssrAction = LprAction.createInsertion(lprForInsertion);
		ssrAction.execute(dao);

		List<LPR> allSSRs = dao.getAllLPRs();
		assertEquals(1, allSSRs.size());

		assertEquals(lprForInsertion, allSSRs.get(0));
	}

	@Test
	public void deleteDeletes() throws DAOException {
		LPR lprForDeletion = randomLPR.randomLPR();
		dao.insertOrUpdate(lprForDeletion);

		LprAction ssrAction = LprAction.createDeletion(lprForDeletion.getLprReference());
		ssrAction.execute(dao);

		List<LPR> allSSRs = dao.getAllLPRs();
		assertEquals(0, allSSRs.size());
	}

	@Test
	public void insertionToStringContainsInsertionKeyword() {
		LPR lprForInsertion = randomLPR.randomLPR();

		LprAction lprAction = LprAction.createInsertion(lprForInsertion);

		assertTrue(lprAction.toString().toLowerCase().contains(("insertion")));
	}

	@Test
	public void insertionToStringContainsLprToString() {
		LPR lprForInsertion = randomLPR.randomLPR();

		LprAction lprAction = LprAction.createInsertion(lprForInsertion);

		assertTrue(lprAction.toString().contains(lprForInsertion.toString()));
	}

	@Test
	public void deletionToStringContainsDeletionKeyword() {
		LPR lprForInsertion = randomLPR.randomLPR();

		LprAction lprAction = LprAction.createDeletion(lprForInsertion.getLprReference());

		assertTrue(lprAction.toString().toLowerCase().contains(("deletion")));
	}

	@Test
	public void deletionToStringContainsLprReference() {
		LPR lprForInsertion = randomLPR.randomLPR();

		LprAction lprAction = LprAction.createDeletion(lprForInsertion.getLprReference());

		assertTrue(lprAction.toString().contains(lprForInsertion.getLprReference()));
	}
}
