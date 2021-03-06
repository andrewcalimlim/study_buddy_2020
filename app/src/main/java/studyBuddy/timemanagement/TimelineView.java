package studyBuddy.timemanagement;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.widget.LinearLayoutCompat;

import com.example.studdybuddy.R;

public class TimelineView extends LinearLayout {

    private int timelineRange;
    private static final double RANGE_DIVIDER = 365.0;       // as magic as it gets

    private View marker;                                     // time marker

    public TimelineView(Context ctx) {
        super(ctx);
        inflateLayouts(ctx);
        marker = findViewById(R.id.time_marker);
        timelineRange = -1;
    }

    public TimelineView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        inflateLayouts(ctx);
        marker = findViewById(R.id.time_marker);
        timelineRange = -1;
    }

    // todo: create some sort of resource class which is used to color each element (BG, etc)

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        timelineRange = (int)((getMeasuredWidth() / RANGE_DIVIDER) * 150);
    }

    /**
     * Updates the time marker on the timeline view to represent some fraction of completion.
     * @param completion - A floating-point value from 0 to 1.
     */
    public void setPercentageCompletion(double completion) {
        // bind to 0 and 1
        completion = Math.max(Math.min(completion, 1.0), 0.0);
        if (timelineRange > 0) {
            LinearLayoutCompat.LayoutParams param = (LinearLayoutCompat.LayoutParams)marker.getLayoutParams();
            param.setMarginStart((int)(((completion * 2.0) - 1.0) * timelineRange));
            marker.setLayoutParams(param);
            postInvalidate();
        }
    }

    // https://stackoverflow.com/questions/3820401/how-to-load-an-xml-inside-a-view-in-android

    /**
     * Used internally to inflate our timeline view layout.
     * @param ctx - The currently active context.
     */
    private void inflateLayouts(Context ctx) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View v = inflater.inflate(R.layout.session_timeline_view, this, false);
        addView(v);
    }
}
