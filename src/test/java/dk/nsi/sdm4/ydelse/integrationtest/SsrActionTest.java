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
package dk.nsi.sdm4.ydelse.integrationtest;

import dk.nsi.sdm4.ydelse.common.exception.DAOException;
import dk.nsi.sdm4.ydelse.dao.SSRTestPurposeDAO;
import dk.nsi.sdm4.ydelse.dao.impl.SSRTestPurposeDAOImpl;
import dk.nsi.sdm4.ydelse.parser.SsrAction;
import dk.nsi.sdm4.ydelse.relation.model.SSR;
import dk.nsi.sdm4.ydelse.simulation.RandomSSR;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(loader=AnnotationConfigContextLoader.class)
public class SsrActionTest {
	@Autowired
    private SSRTestPurposeDAO dao;

	@Autowired
	private RandomSSR randomSSR;

	@Configuration
	static class TestConfig {
		@Bean
		public SSRTestPurposeDAO dao() {
			return new SSRTestPurposeDAOImpl();
		}

		@Bean
		public RandomSSR randomSSR() {
			RandomSSR randomSSR = new RandomSSR();
			randomSSR.setSeed(42);
			return randomSSR;
		}
	}

    @Before
    public void before() throws DAOException {
        dao.purge();
    }

    @Test
    public void insertionInserts() throws DAOException {
        SSR ssrForInsertion = randomSSR.randomSSR();
        SsrAction ssrAction = SsrAction.createInsertion(ssrForInsertion);
        ssrAction.execute(dao);

        List<SSR> allSSRs = dao.getAllSSRs();
        assertEquals(1, allSSRs.size());

        assertEquals(ssrForInsertion, allSSRs.get(0));
    }

    @Test
    public void deleteDeletes() throws DAOException {
        SSR ssrForDeletion = randomSSR.randomSSR();
        dao.insert(ssrForDeletion);

        SsrAction ssrAction = SsrAction.createDeletion(ssrForDeletion.getExternalReference());
        ssrAction.execute(dao);

        List<SSR> allSSRs = dao.getAllSSRs();
        assertEquals(0, allSSRs.size());
    }

    @Test
    public void insertionToStringContainsInsertionKeyword() {
        RandomSSR randomSSR = new RandomSSR();
        randomSSR.setSeed(42);
        SSR ssrForInsertion = randomSSR.randomSSR();

        SsrAction ssrAction = SsrAction.createInsertion(ssrForInsertion);

        assertTrue(ssrAction.toString().toLowerCase().contains(("insertion")));
    }

    @Test
    public void insertionToStringContainsSsrToString() {
        RandomSSR randomSSR = new RandomSSR();
        randomSSR.setSeed(42);
        SSR ssrForInsertion = randomSSR.randomSSR();

        SsrAction ssrAction = SsrAction.createInsertion(ssrForInsertion);

        assertTrue(ssrAction.toString().contains(ssrForInsertion.toString()));
    }
}
