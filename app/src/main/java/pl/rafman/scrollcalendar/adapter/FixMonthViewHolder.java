package pl.rafman.scrollcalendar.adapter;

import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.log28.R;

import java.util.Calendar;

import pl.rafman.scrollcalendar.contract.ClickCallback;
import pl.rafman.scrollcalendar.data.CalendarDay;
import pl.rafman.scrollcalendar.data.CalendarMonth;

public class FixMonthViewHolder extends MonthViewHolder {

    @Nullable
    private final TextView title; // Duplicate the superclass' value because it's private :(

    private final WeekHolder[] weeks = new WeekHolder[6]; // Duplicate the superclass' value because it's private :(

    public FixMonthViewHolder(@NonNull View rootView, @NonNull ClickCallback calendarCallback, @NonNull ResProvider resProvider) {
        super(rootView);

        LinearLayout monthContainer = rootView.findViewById(R.id.monthContainer);

        title = rootView.findViewById(R.id.title);
        Typeface typeface = resProvider.getCustomFont();
        if (typeface != null) {
            title.setTypeface(typeface);
        }
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, resProvider.fontSize());

        for (int i = 0; i < weeks.length; i++) {
            weeks[i] = new WeekHolder(calendarCallback, resProvider);
            monthContainer.addView(weeks[i].layout(monthContainer));
        }
    }

    void bind(CalendarMonth month) {
        if (title != null) {
            title.setText(month.getReadableMonthName());
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.SUNDAY);
        calendar.set(Calendar.YEAR, month.getYear());
        calendar.set(Calendar.MONTH, month.getMonth());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Set calendar to the first day of the month, then go back to the start of the week...
        calendar.set(Calendar.DAY_OF_MONTH, month.getDays()[0].getDay());
        while(calendar.get(Calendar.DAY_OF_WEEK)!=calendar.getFirstDayOfWeek()) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }

        CalendarDay monthDays[] = month.getDays();

        int weekIndex = 0;

        // Create and display weeks, so long as we have days in month.
        for(int dayIndex = 0; dayIndex <monthDays.length; weekIndex++) {
            CalendarDay weekDays[] = new CalendarDay[7];

            for(int i=0;i<7;i++) {
                if (calendar.get(Calendar.MONTH)==month.getMonth() && dayIndex <monthDays.length && calendar.get(Calendar.DAY_OF_MONTH)==monthDays[dayIndex].getDay()) {
                    weekDays[i] = monthDays[dayIndex];
                    dayIndex++;
                }
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            weeks[weekIndex].display(weekIndex+1, month, weekDays);
        }

        // Add empty weeks if necessary.
        for(;weekIndex<weeks.length;weekIndex++) {
            weeks[weekIndex].display(weekIndex+1, month, new CalendarDay[0]);
        }
    }

}
