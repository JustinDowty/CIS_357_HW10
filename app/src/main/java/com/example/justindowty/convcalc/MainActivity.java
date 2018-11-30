package com.example.justindowty.convcalc;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.justindowty.convcalc.dummy.HistoryContent;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.List;

import static WeatherService.WeatherService.BROADCAST_WEATHER;
import static java.lang.String.valueOf;

public class MainActivity extends AppCompatActivity {
    public static final int SETTINGS_RETURN = 1;
    public static final int HISTORY_RESULT = 2;

    public DatabaseReference topRef;
    public static List<HistoryContent.HistoryItem> allHistory;

    private EditText fromInput;
    private EditText toInput;

    private TextView fromLabel;
    private TextView toLabel;

    /* Weather */
    private TextView current;
    private TextView temperature;
    private ImageView weatherIcon;

    private Button calcButton;
    private Button clearButton;
    private Button modeButton;

    private String currMode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        allHistory = new ArrayList<HistoryContent.HistoryItem>();

        fromInput = (EditText) findViewById(R.id.fromEditText);
        toInput = (EditText) findViewById(R.id.toEditText);
        fromLabel = (TextView) findViewById(R.id.fromUnits);
        toLabel = (TextView) findViewById(R.id.toUnits);
        calcButton = (Button) findViewById(R.id.calculateButton);
        clearButton = (Button) findViewById(R.id.clearButton);
        modeButton = (Button) findViewById(R.id.modeButton);
        currMode = "length";

        /* Weather */
        current = (TextView) findViewById(R.id.current);
        temperature = (TextView) findViewById(R.id.temperature);
        weatherIcon = (ImageView) findViewById(R.id.weatherIcon);

        /* Erasing Other Inputs On Click */
        fromInput.setOnFocusChangeListener((View v, boolean b) -> toInput.setText(""));
        toInput.setOnFocusChangeListener((View v, boolean b) -> fromInput.setText(""));

        /* Mode Button */
        modeButton.setOnClickListener((View v) -> switchMode());

        /* Calculate Button */
        calcButton.setOnClickListener((View v) -> calculate());

        /* Clear Button */
        clearButton.setOnClickListener((View v) -> clearInputs());
        setWeatherViews(View.INVISIBLE);
    }

    private BroadcastReceiver weatherReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("TAG", "onReceive: " + intent);
            Bundle bundle = intent.getExtras();
            double temp = bundle.getDouble("TEMPERATURE");
            String summary = bundle.getString("SUMMARY");
            String icon = bundle.getString("ICON").replaceAll("-", "_");
            String key = bundle.getString("KEY");
            int resID = getResources().getIdentifier(icon , "drawable", getPackageName());
            setWeatherViews(View.VISIBLE);
            if (key.equals("p1"))  {
                current.setText(summary);
                temperature.setText(Double.toString(temp));
                weatherIcon.setImageResource(resID);
            } else {
                current.setText(summary);
                temperature.setText(Double.toString(temp));
                weatherIcon.setImageResource(resID);
            }
        }
    };

    public void setWeatherViews(int vis){
        current.setVisibility(vis);
        temperature.setVisibility(vis);
        weatherIcon.setVisibility(vis);
    }

    @Override
    public void onResume(){
        super.onResume();
        allHistory.clear();
        topRef = FirebaseDatabase.getInstance().getReference("history");
        topRef.addChildEventListener (chEvListener);
        IntentFilter weatherFilter = new IntentFilter(BROADCAST_WEATHER);
        LocalBroadcastManager.getInstance(this).registerReceiver(weatherReceiver, weatherFilter);
        setWeatherViews(View.INVISIBLE);
    }

    @Override
    public void onPause(){
        super.onPause();
        topRef.removeEventListener(chEvListener);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(weatherReceiver);
    }


    /* Clears Inputs */
    public void clearInputs(){
        fromInput.setText("");
        toInput.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.settings){
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            i.putExtra("Mode", currMode);
            startActivityForResult(i, SETTINGS_RETURN);
        } else if(item.getItemId() == R.id.action_history) {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivityForResult(intent, HISTORY_RESULT );
            return true;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        clearInputs();
        if(resultCode == Activity.RESULT_OK) {
            fromLabel.setText(data.getStringExtra("FromUnit"));
            toLabel.setText(data.getStringExtra("ToUnit"));
        } else if (resultCode == HISTORY_RESULT) {
            String[] vals = data.getStringArrayExtra("item");
            this.fromInput.setText(vals[0]);
            this.toInput.setText(vals[1]);
            this.currMode = valueOf(vals[2]);
            this.fromLabel.setText(vals[3]);
            this.toLabel.setText(vals[4]);
        }

    }

    /* Conversion Function */
    public double convert(String fromUnit, String toUnit, Double value){
        if (fromUnit.equals("Yards")){
            if (toUnit.equals("Yards")){
                return value;
            } else if (toUnit.equals("Meters")){
                return value * 0.9144;
            } else if (toUnit.equals("Miles")) {
                return value * 0.000568;
            }
        } else if (fromUnit.equals("Meters")){
            if (toUnit.equals("Yards")){
                return value * 1.09361;
            } else if (toUnit.equals("Meters")){
                return value;
            } else if (toUnit.equals("Miles")) {
                return value * 0.000621371;
            }
        } else if (fromUnit.equals("Miles")){
            if (toUnit.equals("Yards")){
                return value * 1760.0;
            } else if (toUnit.equals("Meters")){
                return value * 1609.34;
            } else if (toUnit.equals("Miles")) {
                return value;
            }
        } else if (fromUnit.equals("Gallons")){
            if (toUnit.equals("Gallons")){
                return value;
            } else if (toUnit.equals("Liters")){
                return value * 3.78541;
            } else if (toUnit.equals("Quarts")){
                return value * 4.0;
            }
        } else if (fromUnit.equals("Liters")){
            if (toUnit.equals("Gallons")){
                return value * 0.264172;
            } else if (toUnit.equals("Liters")){
                return value;
            } else if (toUnit.equals("Quarts")){
                return value * 1.05669;
            }
        } else if (fromUnit.equals("Quarts")){
            if (toUnit.equals("Gallons")){
                return value * 0.25;
            } else if (toUnit.equals("Liters")){
                return value * 0.946353;
            } else if (toUnit.equals("Quarts")){
                return value;
            }
        }
        return -1.0;
    }

    /* Switches Modes */
    public void switchMode(){
        if(currMode.equals("length")){
            currMode = "volume";
            fromLabel.setText("Gallons");
            toLabel.setText("Liters");
        } else {
            currMode = "length";
            fromLabel.setText("Meters");
            toLabel.setText("Yards");
        }
    }

    /* Uses convert to calculate and set inputs */
    public void calculate(){
        WeatherService.WeatherService.startGetWeather(this, "42.963686", "-85.888595", "p1");
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        if(fromInput.getText().toString().equals("")){
            double val = Double.parseDouble(toInput.getText().toString());
            fromInput.setText(Double.toString(convert(toLabel.getText().toString(), fromLabel.getText().toString(), val)));
            HistoryContent.HistoryItem item = new HistoryContent.HistoryItem(Double.parseDouble(fromInput.getText().toString()),
                    Double.parseDouble(toInput.getText().toString()), currMode,
                    fromLabel.getText().toString(), toLabel.getText().toString(), fmt.print(DateTime.now()));
            HistoryContent.addItem(item);
            topRef.push().setValue(item);
        } else {
            double val = Double.parseDouble(fromInput.getText().toString());
            toInput.setText(Double.toString(convert(fromLabel.getText().toString(), toLabel.getText().toString(), val)));
            HistoryContent.HistoryItem item = new HistoryContent.HistoryItem(Double.parseDouble(fromInput.getText().toString()),
                    Double.parseDouble(toInput.getText().toString()), currMode,
                    fromLabel.getText().toString(), toLabel.getText().toString(), fmt.print(DateTime.now()));
            HistoryContent.addItem(item);
            topRef.push().setValue(item);
        }
        setWeatherViews(View.VISIBLE);
    }

    private ChildEventListener chEvListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HistoryContent.HistoryItem entry =
                    (HistoryContent.HistoryItem) dataSnapshot.getValue(HistoryContent.HistoryItem.class);
            entry._key = dataSnapshot.getKey();
            allHistory.add(entry);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            HistoryContent.HistoryItem entry =
                    (HistoryContent.HistoryItem) dataSnapshot.getValue(HistoryContent.HistoryItem.class);
            List<HistoryContent.HistoryItem> newHistory = new ArrayList<HistoryContent.HistoryItem>();
            for (HistoryContent.HistoryItem t : allHistory) {
                if (!t._key.equals(dataSnapshot.getKey())) {
                    newHistory.add(t);
                }
            }
            allHistory = newHistory;
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
}
