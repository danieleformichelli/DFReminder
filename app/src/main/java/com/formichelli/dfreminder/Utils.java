package com.formichelli.dfreminder;

import java.util.Calendar;

public class Utils {
    public static boolean isInRange(int start, int end)
    {
        Calendar c = Calendar.getInstance();
        int currentTime = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);

        if (end > start) // no midnight crossing
            return currentTime > start || currentTime < end;
        else // no midnight crossing
            return currentTime > start && currentTime < end;
    }
}
