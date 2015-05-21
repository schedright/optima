package com.softpoint.optima.util;

import java.util.Comparator;
import java.util.Date;

public class DateComparator implements Comparator<Date>
{
    @Override
    public int compare(Date x, Date y)
    {
        if (x.before(y))
        {
            return -1;
        }
        if (x.after(y))
        {
            return 1;
        }
        return 0;
    }
}
