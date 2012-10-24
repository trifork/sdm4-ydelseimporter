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
package dk.nsi.sdm4.ydelse.relation.model;

import org.joda.time.DateTime;
import org.joda.time.Interval;

public abstract class GeneralInterval {
    public static GeneralInterval openInterval(DateTime startTime)
    {
        return new OpenInterval(startTime);
    }
    
    public static GeneralInterval closedInterval(Interval interval)
    {
        return new ClosedInterval(interval);
    }
    
    public abstract boolean overlaps(Interval interval);
    public abstract boolean isOpenEnded();
    public abstract DateTime getStart();
    public abstract DateTime getEnd();
    public abstract GeneralInterval withMillisRemoved();
    public abstract GeneralInterval withSecondsRemoved();
}

class ClosedInterval extends GeneralInterval {
    private Interval interval;

    protected ClosedInterval(Interval interval) {
        this.interval = new Interval(interval);
    }

    public boolean overlaps(Interval interval)
    {
        return this.interval.overlaps(interval);
    }
    
    public boolean isOpenEnded() {
        return false;
    }
    
    public DateTime getStart() {
        return interval.getStart();
    }

    public DateTime getEnd() {
        return interval.getEnd();
    }
    
    @Override
    public String toString() {
        return interval.toString();
    }
    
    @Override
    public int hashCode() {
        return interval.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ClosedInterval other = (ClosedInterval) obj;
        if (interval == null) {
            if (other.interval != null)
                return false;
        } else if (!interval.equals(other.interval))
            return false;
        return true;
    }

    @Override
    public GeneralInterval withMillisRemoved() {
        return GeneralInterval.closedInterval(new Interval(this.interval.getStart().withMillisOfSecond(0), this.interval.getEnd().withMillisOfSecond(0)));
    }

    @Override
    public GeneralInterval withSecondsRemoved() {
        return GeneralInterval.closedInterval(new Interval(this.interval.getStart().withSecondOfMinute(0), this.interval.getEnd().withSecondOfMinute(0)));
    }    
}

class OpenInterval extends GeneralInterval {
    private DateTime start;

    protected OpenInterval(DateTime start) {
        this.start = start;
    }
    public boolean overlaps(Interval interval)
    {
        return interval.getEnd().isAfter(start);
    }
    
    public boolean isOpenEnded() {
        return true;
    }
    
    public DateTime getStart() {
        return start;
    }

    public DateTime getEnd() {
        throw new IllegalStateException("Open interval has no end");
    }
    
    @Override
    public String toString() {
        return start.toString() + "/(openended)";
    }
    
    @Override
    public int hashCode() {
        return start.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OpenInterval other = (OpenInterval) obj;
        if (start == null) {
            if (other.start != null)
                return false;
        } else if (!start.equals(other.start))
            return false;
        return true;
    }
    @Override
    public GeneralInterval withMillisRemoved() {
        return GeneralInterval.openInterval(this.start.withMillisOfSecond(0));
    }
    @Override
    public GeneralInterval withSecondsRemoved() {
        return GeneralInterval.openInterval(this.start.withSecondOfMinute(0));
    }
}
