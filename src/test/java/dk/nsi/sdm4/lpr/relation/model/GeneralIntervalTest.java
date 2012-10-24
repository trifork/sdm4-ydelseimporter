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
package dk.nsi.sdm4.lpr.relation.model;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GeneralIntervalTest {

    private DateTime beforeStartFirst, beforeStartSecond, start, insideFirst, insideSecond, end, afterEndFirst, afterEndSecond;
    private Interval referenceInterval;
    private GeneralInterval closedWithMillis;
    private GeneralInterval openWithMillis;

    @Before
    public void before() {
        beforeStartFirst = new DateTime(2010, 10, 10, 10, 10, 10, 10);
        beforeStartSecond = beforeStartFirst.plusDays(1);
        start = beforeStartFirst.plusDays(2);
        insideFirst = beforeStartFirst.plusDays(3);
        insideSecond = beforeStartFirst.plusDays(4);
        end = beforeStartFirst.plusDays(5);
        afterEndFirst = beforeStartFirst.plusDays(6);
        afterEndSecond = beforeStartFirst.plusDays(7);
        
        referenceInterval = new Interval(start, end);
    }
    
    @Before
    public void setupIntervalsWithMillis() {
        DateTime millisStart = new DateTime(2010, 10, 10, 10, 10, 10, 10);
        DateTime millisEnd = millisStart.plusDays(1);
        Interval intervalWithMillis = new Interval(millisStart, millisEnd);
        closedWithMillis = GeneralInterval.closedInterval(intervalWithMillis);
        openWithMillis = GeneralInterval.openInterval(millisStart);
}

    // Closed interval, starting before

    @Test
    public void testClosedIntervalStartingBeforeEndingBefore() {
        GeneralInterval interval = GeneralInterval.closedInterval(new Interval(beforeStartFirst, beforeStartSecond));
        assertFalse(interval.overlaps(referenceInterval));
    }

    @Test
    public void testClosedIntervalStartingBeforeEndingAtStart() {
        GeneralInterval interval = GeneralInterval.closedInterval(new Interval(beforeStartFirst, start));
        assertFalse(interval.overlaps(referenceInterval));
    }

    @Test
    public void testClosedIntervalStartingBeforeEndingInside() {
        GeneralInterval interval = GeneralInterval.closedInterval(new Interval(beforeStartFirst, insideFirst));
        assertTrue(interval.overlaps(referenceInterval));
    }

    @Test
    public void testClosedIntervalStartingBeforeEndingAfterEnd() {
        GeneralInterval interval = GeneralInterval.closedInterval(new Interval(beforeStartFirst, afterEndFirst));
        assertTrue(interval.overlaps(referenceInterval));
    }

    // Closed interval, starting at start

    @Test
    public void testClosedIntervalStartingAtStartEndingAtEnd() {
        GeneralInterval interval = GeneralInterval.closedInterval(new Interval(start, end));
        assertTrue(interval.overlaps(referenceInterval));
    }

    // Closed interval, starting inside

    @Test
    public void testClosedIntervalStartingInsideEndingInside() {
        GeneralInterval interval = GeneralInterval.closedInterval(new Interval(insideFirst, insideSecond));
        assertTrue(interval.overlaps(referenceInterval));
    }

    @Test
    public void testClosedIntervalStartingInsideEndingAtEnd() {
        GeneralInterval interval = GeneralInterval.closedInterval(new Interval(insideFirst, end));
        assertTrue(interval.overlaps(referenceInterval));
    }

    @Test
    public void testClosedIntervalStartingInsideEndingAfterEnd() {
        GeneralInterval interval = GeneralInterval.closedInterval(new Interval(insideFirst, afterEndFirst));
        assertTrue(interval.overlaps(referenceInterval));
    }

    // Closed interval, starting at end

    @Test
    public void testClosedIntervalStartingAtEndEndingAfterEnd() {
        GeneralInterval interval = GeneralInterval.closedInterval(new Interval(end, afterEndFirst));
        assertFalse(interval.overlaps(referenceInterval));
    }

    // Closed interval, starting after

    @Test
    public void testClosedIntervalStartingAfterEndEndingAfterEnd() {
        GeneralInterval interval = GeneralInterval.closedInterval(new Interval(afterEndFirst, afterEndSecond));
        assertFalse(interval.overlaps(referenceInterval));
    }
    
    // Open intervals
    @Test
    public void testOpenIntervalStartingBefore() {
        GeneralInterval interval = GeneralInterval.openInterval(beforeStartFirst);
        assertTrue(interval.overlaps(referenceInterval));
    }

    @Test
    public void testOpenIntervalStartingAtStart() {
        GeneralInterval interval = GeneralInterval.openInterval(start);
        assertTrue(interval.overlaps(referenceInterval));
    }

    @Test
    public void testOpenIntervalStartingInside() {
        GeneralInterval interval = GeneralInterval.openInterval(insideFirst);
        assertTrue(interval.overlaps(referenceInterval));
    }

    @Test
    public void testOpenIntervalStartingAtEnd() {
        GeneralInterval interval = GeneralInterval.openInterval(end);
        assertFalse(interval.overlaps(referenceInterval));
    }

    @Test
    public void testOpenIntervalStartingAfterEnd() {
        GeneralInterval interval = GeneralInterval.openInterval(afterEndFirst);
        assertFalse(interval.overlaps(referenceInterval));
    }

    @Test
    public void closedSaysItsClosed() {
        assertFalse(GeneralInterval.closedInterval(referenceInterval).isOpenEnded());
    }
    
    @Test
    public void openSaysItsOpen() {
        assertTrue(GeneralInterval.openInterval(start).isOpenEnded());
    }

    @Test
    public void closedKnowsItsStart() {
        assertEquals(referenceInterval.getStart(), GeneralInterval.closedInterval(referenceInterval).getStart());
    }

    @Test
    public void closedKnowsItsEnd() {
        assertEquals(referenceInterval.getEnd(), GeneralInterval.closedInterval(referenceInterval).getEnd());
    }

    @Test
    public void closedHasWorkingToString() {
        GeneralInterval.closedInterval(referenceInterval).toString(); // no exception expected
    }

    @Test
    public void closedEquals() {
        assertTrue(GeneralInterval.closedInterval(referenceInterval).equals(GeneralInterval.closedInterval(referenceInterval)));
    }
    
    @Test
    public void closedCanRemoveMillisFromStart() {
        assertEquals(0, closedWithMillis.withMillisRemoved().getStart().getMillisOfSecond());
    }

    @Test
    public void closedCanRemoveMillisFromEnd() {
        assertEquals(0, closedWithMillis.withMillisRemoved().getEnd().getMillisOfSecond());
    }

    @Test
    public void openKnowsItsStart() {
        assertEquals(end, GeneralInterval.openInterval(end).getStart());
    }

    @Test(expected = IllegalStateException.class)
    public void openHasNoEnd() {
        assertEquals(end, GeneralInterval.openInterval(end).getEnd());
    }
    
    @Test
    public void openHasWorkingToString() {
        GeneralInterval.openInterval(end).toString(); // no exception expected
    }

    @Test
    public void openCanRemoveMillisFromStart() {
        assertEquals(0, openWithMillis.withMillisRemoved().getStart().getMillisOfSecond());
    }    
    
    @Test
    public void openEquals() {
        assertTrue(GeneralInterval.openInterval(end).equals(GeneralInterval.openInterval(end)));
    }
}
