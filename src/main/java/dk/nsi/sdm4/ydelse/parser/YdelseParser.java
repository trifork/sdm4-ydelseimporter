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
package dk.nsi.sdm4.ydelse.parser;

import dk.nsi.sdm4.core.parser.Parser;
import dk.nsi.sdm4.core.parser.ParserException;
import dk.nsi.sdm4.ydelse.common.splunk.SplunkLogger;
import dk.nsi.sdm4.ydelse.dao.SSRWriteDAO;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Foretager gennemløb af en Ydelse-fil og koordinerer parsning og indsættelse/sletning.
 */
public class YdelseParser implements Parser {
    private static final SplunkLogger log = new SplunkLogger(YdelseParser.class);

	@Autowired
	SSRWriteDAO dao;

	@Autowired
	YdelseInserter inserter;

	/**
	 * @see Parser#process(java.io.File)
	 */
	@Override
    public void process(File dataset) throws ParserException {
		File file = findSingleFileOrComplain(dataset);

		countNumberOfLines(file);

		Future<Void> insertionFuture = inserter.readFileAndPerformDatabaseOperations(file);
		try {
			insertionFuture.get();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new ParserException("Unable to perform insertions for " + file.getAbsolutePath(), e);
		}
	}

	private long countNumberOfLines(File file) {
		BufferedReader bf = null;
		try {
			bf = new BufferedReader(new FileReader(file));

			long numLines = 0;
			while (bf.readLine() != null) {
				numLines++;
			}

			return numLines;
		} catch (Exception e) {
			throw new ParserException("Could not count number of lines in" + file.getAbsolutePath(), e);
		} finally {
			closeQuietly(bf);
		}
	}

	private void closeQuietly(BufferedReader bf) {
		if (bf != null) {
		    try {
		        bf.close();
		    } catch (IOException e) {
		        log.error(e);
		    }
		}
	}

	private File findSingleFileOrComplain(File dataset) {
		if (dataset == null) {
			throw new ParserException("Dataset cannot be null");
		}

		File[] files = dataset.listFiles();
		assert files != null;
		if (files.length == 0) {
			throw new ParserException("Dataset " + dataset.getAbsolutePath() + " is empty. Will not continue.");
		}

		if (files.length > 1) {
			throw new ParserException("Dataset " + dataset.getAbsolutePath() + " contains " + files.length + " files, I only expected 1. Will not continue.");
		}

		return files[0];
	}

	/**
	 * @see Parser#getHome()
	 */
	@Override
	public String getHome() {
		return "ydelseimporter";
	}
}
