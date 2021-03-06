package wubbalubbadubdub.eecs448project1;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.graphics.Color;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


import wubbalubbadubdub.eecs448project1.data.DatabaseHelper;
import wubbalubbadubdub.eecs448project1.data.DateSlot;
import wubbalubbadubdub.eecs448project1.data.Event;
import wubbalubbadubdub.eecs448project1.data.HelperMethods; //For toTime() method
import wubbalubbadubdub.eecs448project1.data.Task;

/**
 * AddEventActivity.java
 * @author Dustin, Damian, Lane
 * @version 1.1
 * This Class allows the user to create an event and select timeslots for the event created
 */
public class AddEventActivity extends Activity {
    private List<dayitem> daylist;//listview item information, used to hold all date item
    private ListView lvday; //listview, used to show all item information

    private DatabaseHelper dbHelper;
    private Toast statusMessage;

    private String currentUser;
    private List<Integer> selectedTimeslots;
    private boolean format = false; //Time format: false=12h | true=24h

    //Color Variables - Material Design
    int BLUE_MAT = Color.rgb(2,136,209);
    int GREEN_MAT = Color.rgb(139,195,74);

    /**
     * Method called when the activity is first created. Lots of formatting done.
     * @param savedInstanceState Unused Bundle object. Usually used if the app is killed then we can resume
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        lvday = (ListView) findViewById(R.id.date_list);
        daylist = new ArrayList<>();
        final DatePicker datePicker = (DatePicker) findViewById(R.id.datePicker);
        final TextView emptybar = (TextView) findViewById(R.id.lvemptybar);

        dbHelper = new DatabaseHelper(getApplicationContext());
        statusMessage = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        Intent intent = getIntent();
        currentUser = intent.getStringExtra("currentUser");

        TextView welcome = (TextView) findViewById(R.id.tvWelcome);
        welcome.setText(currentUser + ", create your event");
        selectedTimeslots = new ArrayList<>();

        createTimeslotTable();
        setupDatePicker(datePicker);
        LayoutInflater inflater = getLayoutInflater();

        final day_list_item adapter = new day_list_item(inflater,daylist);
        lvday.setAdapter(adapter);

        //it's a Imagebutton, used to add multi-day into a tiny list_view
        ImageButton addDay = (ImageButton) findViewById(R.id.addDayToList);
        addDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkSameDate(datePicker.getDayOfMonth(),datePicker.getYear(),datePicker.getMonth())){
                dayitem newdate = new dayitem(datePicker.getDayOfMonth(),datePicker.getYear(),datePicker.getMonth());
                daylist.add(newdate);
                day_list_item adapter = new day_list_item(getLayoutInflater(),daylist);
                lvday.setAdapter(adapter);
                if(checkDayListEmpty()){
                    emptybar.setText("");
                }
            }}
        });
        ImageButton cleardate = (ImageButton) findViewById(R.id.removeall);
        cleardate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                while (!daylist.isEmpty()) {
                    daylist.remove(0);
                }
                day_list_item adapter = new day_list_item(getLayoutInflater(),daylist);
                lvday.setAdapter(adapter);
                emptybar.setText("Multi-Day List Empty Now");
            }
        });
    }

    /**
     * This function will create the table of buttons to select an event's timeframe
     */
    private void createTimeslotTable() {
        TableLayout layout = (TableLayout) findViewById(R.id.tbLayout);

        int count = 0;
        for (int i = 0; i < 4; i++) {
            TableRow tr = new TableRow(this);
            for (int j = 0; j < 12; j++) {
                final int current = count;
                Button b = new Button(this);
                b.setText(HelperMethods.toTime(count,format));
                b.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                b.setTransformationMethod(null);
                TableRow.LayoutParams cellParams = new TableRow.LayoutParams();
                cellParams.rightMargin = 5;
                b.setLayoutParams(cellParams);
                b.setBackgroundColor(GREEN_MAT);
                b.setOnClickListener(new Button.OnClickListener() {
                    int id = current;
                    boolean selected = false;

                    @Override
                    public void onClick(View v) {
                        Button obj = (Button) v;
                        if (selected) {
                            obj.setBackgroundColor(GREEN_MAT);
                            selectedTimeslots.remove(Integer.valueOf(id));
                        } else {
                            obj.setBackgroundColor(BLUE_MAT);
                            selectedTimeslots.add(id);
                        }
                        selected = !selected;
                        updateTimeDisplay();
                    }
                });
                tr.addView(b);
                count++;
            }
            TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);
            tableRowParams.setMargins(10, 2, 10, 2);
            tr.setLayoutParams(tableRowParams);
            layout.addView(tr, tableRowParams);
        }
    }

    /**
     * clear timeslot table - originally wrote for onDateChangedListener, realized it's probably
     * unnecessary but keeping it in case we want it for anything else, e.g. a 'clear times' button
     */
    private void clearTimeslotTable() {

        selectedTimeslots.clear();

        TableLayout tableLayout = (TableLayout) findViewById(R.id.tbLayout);
        int count = 0;
        for (int i = 0; i < 4; i++) {
            TableRow row = (TableRow)tableLayout.getChildAt(i);
            for (int j = 0; j < 12; j++) {
                Button b = (Button) row.getChildAt(j);
                b.setBackgroundColor(GREEN_MAT);
                count++;
            }
        }
        updateTimeDisplay();

    }

    /**
     * This function is called when the 12h/24h toggle button is pressed
     * @param v View of the pressed button
     */
    public void toggleFormat(View v) {
        TableLayout tableLayout = (TableLayout) findViewById(R.id.tbLayout);

        format = !format;

        int count = 0;
        for (int i = 0; i < 4; i++) {
            TableRow row = (TableRow)tableLayout.getChildAt(i);
            for (int j = 0; j < 12; j++) {
                Button b = (Button) row.getChildAt(j);
                b.setText(HelperMethods.toTime(count, format));
                count++;
            }
        }
        updateTimeDisplay();

    }

    /**
     * Updates the Selected timeframe for the event
     */
    private void updateTimeDisplay() {
        TextView timeDisplay = (TextView) findViewById(R.id.tvSelectedTimes);

        String disp = "Event Timeframe: " + HelperMethods.getTimeString(selectedTimeslots, format);

        if (selectedTimeslots.isEmpty()) disp = "PLEASE SELECT TIMES";

        timeDisplay.setText(disp);
    }

    /**
     * Verifies that the Event is valid before attempting to insert to the database
     * @param e Event object of the event to be created.
     * @return True if valid, false otherwise
     */
    boolean verify(Event e) {

        //Dates should have no way of being invalid

        //Name verification
        if (e.getName().isEmpty()) {
            statusMessage.setText("ERROR: Please name your event!");
            return false;
        }

        //Timeslot verification
        if (e.getDateSlots().isEmpty()) {
            statusMessage.setText("ERROR: Please choose times for your event!");
            return false;
        }

        //Check if user is already signed up for any conflicting events
        /*pseudo: if intersection of (currentuser.signups.timeslots) list with (e.timeslots) list
         * is nonempty, -> conflict found, return false*/
        return true;
    }

    /**
     * onSaveButtonClick() - Handles Save button - creates event object, verifies, and adds event
     * @param v View of the button that was pressed
     */
    public void onSaveButtonClick(View v) {

        List<DateSlot> dateSlotList = new ArrayList<>();
        List<String> dates = new ArrayList<>();

        // Get dates from date picker or date list
        if (daylist.isEmpty()) {
            //Build date string for event
            DatePicker datePicker = (DatePicker) findViewById(R.id.datePicker);
            int month = datePicker.getMonth() + 1;
            int day = datePicker.getDayOfMonth();
            int year = datePicker.getYear();
            dates.add(HelperMethods.dateToString(month, day, year));
        } else {
            for (int i = 0; i < daylist.size(); i++) {
                int month = daylist.get(i).getMonth() + 1;
                int day = daylist.get(i).getDay();
                int year = daylist.get(i).getYear();
                dates.add(HelperMethods.dateToString(month, day, year));
            }
        }

        //Get name of event
        EditText nameText = (EditText) findViewById(R.id.textName);
        String name = nameText.getText().toString();

        //Stringify timeslot list in int format for storage in db
        String timeslotIntList = HelperMethods.stringifyTimeslotInts(selectedTimeslots);

        for (int i = 0; i < dates.size(); i++) {
            DateSlot dateSlot = new DateSlot(timeslotIntList, dates.get(i));
            dateSlotList.add(dateSlot);
        }

        List<Task> taskList = new ArrayList<>();

        //Create an event, attempt to verify it, and send to db if all is well
        /*Event ID is set to -1 because it's useless until a real ID is assigned
         *by the primary key upon insertion to the database after successful verification.*/
        Event e = new Event(-1,  name, currentUser, dateSlotList, taskList);
        System.out.println("Number of dates: " + dates.size());
        if (verify(e)){

            //Add event and automatically sign creator up for duration of event
            int eventID = dbHelper.addEvent(e);
            for (int i = 0; i < dates.size(); i ++) {
                dbHelper.addSignup(eventID, currentUser, selectedTimeslots, dates.get(i));
            }

            statusMessage.setText("Your event has been created.");
            statusMessage.show();
            Intent intent = new Intent(getApplicationContext(), ListActivity.class);
            intent.putExtra("currentUser", currentUser);
            finish();
            startActivity(intent);

        }else statusMessage.show();
    }
    public void setupDatePicker(DatePicker datePicker){
        //Set Date Picker to current date, datePicker constraints etc.
        int[] date = HelperMethods.getCurrentDate();
        int month = date[0];
        int day = date[1];
        int year = date[2];

        //Now set the default date to today thru this init method I found
        datePicker.init(year, month, day, new OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker dp, int y, int m, int d) {
                //clearTimeslotTable(); ENABLE TO RESET TIMESLOTS UPON DATE SWITCH
            }
        });

        datePicker.setMinDate(System.currentTimeMillis() - 1000);
        Calendar max = Calendar.getInstance();
        max.set(Calendar.YEAR, 2100);

        datePicker.setMaxDate((max.getTime()).getTime());

    }
    public boolean checkDayListEmpty(){
        if(daylist.isEmpty()) {
            return false;
        }else{
            return true;
        }
    }

    public int getSizeOfListView(){
        if(daylist.isEmpty())
        {return 0;}
        return daylist.size();
    }
    public boolean checkSameDate(int day, int year ,int month){
        for(int i = 0; i < daylist.size(); i++) {
            if((daylist.get(i).getDay() == day)&&(daylist.get(i).getMonth() == month)&&(daylist.get(i).getYear() == year)){
                statusMessage.setText("This day is already in your List");
                statusMessage.show();
                return false;
            }
        }
        return true;
    }
}