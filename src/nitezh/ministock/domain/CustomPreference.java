package nitezh.ministock.domain;

import android.app.SearchManager;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import nitezh.ministock.DialogTools;
import nitezh.ministock.R;
import nitezh.ministock.activities.PreferencesActivity;

/**
 * Created by Tim Greiner on 30.05.2016.
 *
 * This class handles the behaviour of every individual stock preference
 * It is different to how preferences are handled elsewhere.
 * Instead of controlling the behaviour of a preference in PreferenceActivity,
 * everything is controlled in this custom preference.
 */
public class CustomPreference extends EditTextPreference {
    public CustomPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onClick() {
        return;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        return;
    }

    View VIEW;
    PreferencesActivity PREFERENCE_ACTIVITY;
    String mSymbolSearchKey = "";


    //Overrides the default onCreateView to set listeners for the preferences
    @Override
    protected View onCreateView(final ViewGroup parent )
    {
        LayoutInflater li = (LayoutInflater)getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View view =  li.inflate( R.layout.preferences_layout, parent, false);

        view.setTag(getKey());

        mSymbolSearchKey = getKey();
        PREFERENCE_ACTIVITY = (PreferencesActivity) getContext();
        VIEW = view;
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PREFERENCE_ACTIVITY.mSymbolSearchKey = getKey();

                // Start search with current value as query
                String query = getSharedPreferences().getString(mSymbolSearchKey, "");
                PREFERENCE_ACTIVITY.startSearch(query, false, null, false);
            }
        });
        view.setOnLongClickListener(longListen);
        view.setOnDragListener(dragListen);

        return view;
    }


    public View getView(){
        return VIEW;
    }

    //Implements the "drag" of drag and drop
    View.OnLongClickListener longListen = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            DragShadow dragShadow = new DragShadow(v);
            ClipData data = ClipData.newPlainText("", "");

            v.startDrag(data, dragShadow, v, 0);

            return true;
        }
    };

    //Implements the "drop" of drag and drop
    View.OnDragListener dragListen = new View.OnDragListener(){
        @Override
        public boolean onDrag(View v, DragEvent event){
            int dragEvent = event.getAction();

            switch (dragEvent){
                case DragEvent.ACTION_DRAG_ENTERED:
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    break;
                //This part switches the content of the dropper and the drop target
                case DragEvent.ACTION_DROP:
                    SharedPreferences preferences = getSharedPreferences();
                    SharedPreferences.Editor editor = preferences.edit();

                    //Gets the tag AKA preference key of the currently used preferences
                    String target = (String) v.getTag();
                    String dropped = (String) ((View) event.getLocalState()).getTag();

                    //Gets the values of the stocks currently in the preferences
                    String targetValue = preferences.getString(target,"");
                    String droppedValue = preferences.getString(dropped,"");
                    String bufferValue = preferences.getString(target,"");

                    //Swaps preference values
                    editor.putString(target, droppedValue);
                    editor.putString(dropped,bufferValue);
                    editor.apply();
                    break;
            }

            return true;
        }
    };


    // Draws the Object that is created when "dragging" a Preference
    private class DragShadow extends View.DragShadowBuilder{
        //Grey Boy: The object which will be drawn
        ColorDrawable greyBox;

        public DragShadow(View view) {
            super(view);
            //Sets the color of the grey box
            greyBox = new ColorDrawable(Color.LTGRAY);
        }

        //Sets up the metrics of the box
        @Override
        public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
            View v = getView();

            int height = v.getHeight()/2;
            int width = v.getWidth()/2;

            //Sets the size of the grey box
            greyBox.setBounds(0,0,width,height);
            shadowSize.set(width, height);

            //This is the TouchPoint
            //At this point in the box will the cursor be when dragging
            shadowTouchPoint.set(width/2,height/2);


        }

        //Draws the object on the canvas
        @Override
        public void onDrawShadow(Canvas canvas) {
            greyBox.draw(canvas);
        }
    }
}
