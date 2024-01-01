package pl.rafman.scrollcalendar.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.log28.R;

public final class FixScrollCalendarAdapter extends ScrollCalendarAdapter {

    private final @NonNull ResProvider resProvider; // Duplicate the superclass' value because it's private :(

    public FixScrollCalendarAdapter(@NonNull ResProvider resProvider) {
        super(resProvider);
        this.resProvider = resProvider;
    }

    @Override
    public MonthViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        @NonNull View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.scrollcalendar_month, parent, false);

        return new FixMonthViewHolder(rootView, this, this.resProvider);
    }

}
