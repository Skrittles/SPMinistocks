package nitezh.ministock.domain;

import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import nitezh.ministock.PreferenceStorage;
import nitezh.ministock.R;
import nitezh.ministock.Storage;
import nitezh.ministock.activities.PreferencesActivity;
import nitezh.ministock.utils.ReflectionTools;

/**
 * Created by Tim Greiner on 30.05.2016.
 *
 * This class handles the behaviour of every individual stock preference
 * It is different to how preferences are handled elsewhere.
 * Instead of controlling the behaviour of a preference in PreferenceActivity,
 * everything is controlled in this custom preference.
 */
public class StockPreference extends EditTextPreference {
    int widgetSize = 0;
    //Instead of just widget size, we use a letter code to avoid duplicate names for png files
    String size;
    boolean isVisual;
    public StockPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        Storage storage = PreferenceStorage.getInstance(getContext());
        isVisual = storage.getBoolean("visual_stockboard",false);
        widgetSize = storage.getInt("widgetSize",0);
        switch (widgetSize){
            case 0:
                size="a";
                break;
            case 1:
                size="b";
                break;
            case 2:
                size="c";
                break;
            case 3:
                size="d";
                break;
            default:
                size="a";
                break;
        }
    }

    @Override
    protected void onClick() {
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

    }

    View VIEW;
    PreferencesActivity PREFERENCE_ACTIVITY;
    String mSymbolSearchKey = "";


    /**
     * Overrides the default onCreateView to set listeners to preferences
     *
     * @param parent parent ViewGroup of the preference
     * @return the view of the preference
     */
    @Override
    protected View onCreateView(final ViewGroup parent )
    {
        super.onCreateView(parent);
        LayoutInflater li = (LayoutInflater)getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View view =  li.inflate( R.layout.preferences_layout, parent, false);

        view.setTag(getKey());

        mSymbolSearchKey = getKey();
        PREFERENCE_ACTIVITY = (PreferencesActivity) getContext();
        VIEW = view;
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferencesActivity.mSymbolSearchKey = getKey();

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
            ImageView screen = (ImageView) getView().getRootView().findViewById(R.id.screen);
            int dragEvent = event.getAction();

            switch (dragEvent){
                // Shows preview of the widget
                case DragEvent.ACTION_DRAG_ENTERED:
                    if(isVisual) {
                        screen.setVisibility(View.VISIBLE);
                        screen.setImageResource(ReflectionTools.getFieldDrawable((getKey()).toLowerCase() + size));
                    }
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    break;
                // Hides preview if nothing is dragged
                case DragEvent.ACTION_DRAG_ENDED:
                    screen.setVisibility(View.GONE);
                    break;
                //This part switches the content of the dropper and the drop target
                case DragEvent.ACTION_DROP:
                    screen.setVisibility(View.GONE);
                    SharedPreferences preferences = getSharedPreferences();
                    SharedPreferences.Editor editor = preferences.edit();

                    //Gets the tag AKA preference key of the currently used preferences
                    String target = (String) v.getTag();
                    String dropped = (String) ((View) event.getLocalState()).getTag();
                    String targetSummaryKey = target + "_summary";
                    String droppedSummaryKey = dropped + "_summary";

                    //Gets the summary of the stocks currently in the preferences
                    String targetSummary = preferences.getString(targetSummaryKey,"");
                    String droppedSummary = preferences.getString(droppedSummaryKey,"");

                    //Gets the values of the stocks currently in the preferences
                    String targetValue = preferences.getString(target,"");
                    String droppedValue = preferences.getString(dropped,"");


                    //Swaps preference values
                    editor.putString(target, droppedValue);
                    editor.putString(dropped, targetValue);
                    editor.putString(targetSummaryKey, droppedSummary);
                    editor.putString(droppedSummaryKey, targetSummary);
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

        /**
         * Sets up the metrics of the box that is dragged
         *
         * From android documentation:
         * @param shadowSize A {@link android.graphics.Point} containing the width and height
         * of the shadow image. Your application must set {@link android.graphics.Point#x} to the
         * desired width and must set {@link android.graphics.Point#y} to the desired height of the
         * image.
         *
         * @param shadowTouchPoint A {@link android.graphics.Point} for the position within the
         * shadow image that should be underneath the touch point during the drag and drop
         * operation. Your application must set {@link android.graphics.Point#x} to the
         * X coordinate and {@link android.graphics.Point#y} to the Y coordinate of this position.
         */
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

        /**
         * Draws the object on a canvas
         *
         * @param canvas the canvas where the object should be drawn
         */
        @Override
        public void onDrawShadow(Canvas canvas) {
            greyBox.draw(canvas);
        }
    }
}
