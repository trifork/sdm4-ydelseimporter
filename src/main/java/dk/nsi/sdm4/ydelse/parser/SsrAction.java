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

import dk.nsi.sdm4.ydelse.common.exception.DAOException;
import dk.nsi.sdm4.ydelse.dao.SSRWriteDAO;
import dk.nsi.sdm4.ydelse.relation.model.SSR;

public class SsrAction {

    enum ActionType {
        INSERTION, DELETION, NOOP
    }

    ActionType actionType;
    SSR ssrForInsertion;
    String externalReferenceForDeletion;

    public static SsrAction createInsertion(SSR ssrForInsertion) {
        SsrAction action = new SsrAction();
        action.actionType = ActionType.INSERTION;
        action.ssrForInsertion = ssrForInsertion;
        return action;
    }
    
    public static SsrAction createNOOP() {
    	SsrAction action = new SsrAction();
    	action.actionType = ActionType.NOOP;
    	return action;
    }

    public static SsrAction createDeletion(String externalReferenceForDeletion) {
        SsrAction action = new SsrAction();
        action.actionType = ActionType.DELETION;
        action.externalReferenceForDeletion = externalReferenceForDeletion;
        return action;
    }

    private SsrAction() {

    }

    public void execute(SSRWriteDAO dao) throws DAOException {
        if (actionType == ActionType.INSERTION) {
            executeInsertion(dao);
        }
        else if (actionType == ActionType.DELETION) {
            executeDeletion(dao);
        }
    }

    private void executeInsertion(SSRWriteDAO dao) throws DAOException {
        dao.insert(ssrForInsertion);
    }

    private void executeDeletion(SSRWriteDAO dao) throws DAOException {
        dao.deleteByExternalReference(externalReferenceForDeletion);
    }

    @Override
    public String toString() {
	    if (actionType == ActionType.INSERTION) {
            return "SsrAction(insertion)[" + ssrForInsertion.toString() + "]";
	    } else if (actionType == ActionType.DELETION) {
		    return "SsrAction(deletion)[" + externalReferenceForDeletion + "]";
	    } else if (actionType == ActionType.NOOP) {
		    return "SsrAction(noop)";
	    } else {
		    return super.toString();
	    }
    }
}
