package wubbalubbadubdub.eecs448project1;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import wubbalubbadubdub.eecs448project1.data.DatabaseHelper;
import wubbalubbadubdub.eecs448project1.data.Event;
import wubbalubbadubdub.eecs448project1.data.HelperMethods;

import org.w3c.dom.Text;

/**
 * This page list all the activities created by other users.
 * Also this page allows the user to add an event which brings them to the AddEvent page
 * @author Dustin
 * @version 1.0
 */
public class ListActivity extends Activity {

    private DatabaseHelper dbHelper;
    private String currentUser;
    private ListView eventList;

    int BLUE_MAT = Color.rgb(2,136,209); //For dolling up the event titles

    private boolean allEvents = true; //Determines whether to show all events or own events

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        dbHelper = new DatabaseHelper(getApplicationContext());
        eventList = (ListView) findViewById(R.id.lvEvents);

        Intent intent = getIntent();
        currentUser = intent.getStringExtra("currentUser");
        TextView welcomeText = (TextView) findViewById(R.id.tvWelcome);
        welcomeText.setText("Hello " + currentUser + "!");

        populateEventTable();

    }


    /*   ListView version of event display (inferior to table)
    private void populateEvents() {

        List<Event> events = (allEvents) ? dbHelper.getAllEvents() : dbHelper.getUserEvents(currentUser);
        List<String> eventTitles = new ArrayList<>(); //Titles of events will be shown //TODO subItems to show dates?

        for (Event e : events) eventTitles.add(e.getName());

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                eventTitles);

        eventList.setAdapter(arrayAdapter);

        //populateEventTable();

    }
    */

    /**
     * Handles All Events toggle button
     * @param v - (Given view)
     */
    public void toggleEventList(View v) {
        allEvents = !allEvents;
        populateEventTable();
    }

    /*TODO found bug: if attempting to show own events right after creating a new
     *event and multiple users have events, app crashes.*/
    private void populateEventTable() {
        TableLayout layout = (TableLayout) findViewById(R.id.eventTableLayout);
        TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);

        //Clears the table before (re)populating
        for (int i = 0; i < layout.getChildCount(); i++) {
            View row = layout.getChildAt(i);
            if (row instanceof TableRow) ((ViewGroup) row).removeAllViews();
        }

        tableRowParams.setMargins(10, 2, 10, 2);
        ArrayList<Event> events = (allEvents) ? dbHelper.getAllEvents() : dbHelper.getUserEvents(currentUser);

        int numberOfEvents = events.size();

        for (int i = 0; i < numberOfEvents; i++) {
            TableRow row = new TableRow(this);

            Event workingEvent = events.get(i);

            String creator = "  -- Created by " + workingEvent.getCreator() + ": ";


            TableRow.LayoutParams tvLayout = new TableRow.LayoutParams();

            tvLayout.setMargins(0, 8, 10, 5);

            TextView eventName = new TextView(this);
            TextView eventCreator = new TextView(this);
            TextView eventDay = new TextView(this);
            TextView eventTimeslots = new TextView(this);

            eventName.setLayoutParams(tvLayout);
            eventCreator.setLayoutParams(tvLayout);
            eventDay.setLayoutParams(tvLayout);
            eventTimeslots.setLayoutParams(tvLayout);

            eventName.setTextSize(22);
            eventName.setTypeface(null, Typeface.BOLD);
            eventName.setTextColor(BLUE_MAT);
            eventCreator.setTextSize(20);
            eventDay.setTextSize(20);
            eventTimeslots.setTextSize(20);

            //Prettier formatting, convert timeslots into timestring through series of parsing methods
            eventCreator.setText(creator);
            eventDay.setText("  " + workingEvent.getDate());
            eventName.setText("  " + workingEvent.getName());
            eventTimeslots.setText("  " + HelperMethods.getTimeString(HelperMethods.listifyTimeslotInts(workingEvent.getTimeslots()), false));

            row.addView(eventName);
            row.addView(eventCreator);
            row.addView(eventDay);
            row.addView(eventTimeslots);

            row.setLayoutParams(tableRowParams);

            layout.addView(row, tableRowParams);

        }
    }

    /**
     * Handles Add new Event button
     * @param v - (given view)
     */
    public void newEvent(View v) {
        Intent intent = new Intent(this, AddEventActivity.class);
        intent.putExtra("currentUser", currentUser);
        finish();
        startActivity(intent);
    }


}
