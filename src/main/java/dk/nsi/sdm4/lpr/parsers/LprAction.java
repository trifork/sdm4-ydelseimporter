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
import dk.nsi.sdm4.lpr.dao.LPRWriteDAO;
import dk.nsi.sdm4.lpr.relation.model.LPR;

public class LprAction {
    enum ActionType {
        INSERTION, DELETION
    }

    ActionType actionType;
    LPR lprForInsertion;
    String lprReferenceForDeletion;

    public static LprAction createInsertion(LPR lprForInsertion) {
        LprAction action = new LprAction();
        action.actionType = ActionType.INSERTION;
        action.lprForInsertion = lprForInsertion;
        return action;
    }

    public static LprAction createDeletion(String lprReferenceForDeletion) {
        LprAction action = new LprAction();
        action.actionType = ActionType.DELETION;
        action.lprReferenceForDeletion = lprReferenceForDeletion;
        return action;
    }

    private LprAction() {
    }

    public void execute(LPRWriteDAO dao) throws DAOException {
        if (actionType == ActionType.INSERTION) {
            dao.insertOrUpdate(lprForInsertion);
        } else if (actionType == ActionType.DELETION) {
            dao.deleteByLprReference(lprReferenceForDeletion);
        }
    }

    @Override
    public String toString() {
        if (actionType == ActionType.INSERTION) {
            return "LprAction(insertion)[" + lprForInsertion.toString() + "]";
        } else {
            return "LprAction(deletion)[lprReference=" + lprReferenceForDeletion;
        }
    }
}
