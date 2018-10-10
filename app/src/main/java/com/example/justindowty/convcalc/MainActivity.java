package com.example.justindowty.convcalc;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    /* Used for settings return */
    public static final int SETTINGS_RETURN = 1;

    private EditText fromInput;
    private EditText toInput;

    private TextView fromLabel;
    private TextView toLabel;

    private Button calcButton;
    private Button clearButton;
    private Button modeButton;

    private String currMode = "length";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fromInput = (EditText) findViewById(R.id.fromEditText);
        toInput = (EditText) findViewById(R.id.toEditText);
        fromLabel = (TextView) findViewById(R.id.fromUnits);
        toLabel = (TextView) findViewById(R.id.toUnits);
        calcButton = (Button) findViewById(R.id.calculateButton);
        clearButton = (Button) findViewById(R.id.clearButton);
        modeButton = (Button) findViewById(R.id.modeButton);

        /* Erasing Other Inputs On Click */
        fromInput.setOnFocusChangeListener((View v, boolean b) -> toInput.setText(""));
        toInput.setOnFocusChangeListener((View v, boolean b) -> fromInput.setText(""));

        /* Mode Button */
        modeButton.setOnClickListener((View v) -> switchMode());

        /* Calculate Button */
        calcButton.setOnClickListener((View v) -> calculate());

        /* Clear Button */
        clearButton.setOnClickListener((View v) -> clearInputs());
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
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        clearInputs();
        if(resultCode == Activity.RESULT_OK) {
            fromLabel.setText(data.getStringExtra("FromUnit"));
            toLabel.setText(data.getStringExtra("ToUnit"));
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
        if(fromInput.getText().toString().equals("")){
            double val = Double.parseDouble(toInput.getText().toString());
            fromInput.setText(Double.toString(convert(toLabel.getText().toString(), fromLabel.getText().toString(), val)));
        } else {
            double val = Double.parseDouble(fromInput.getText().toString());
            toInput.setText(Double.toString(convert(fromLabel.getText().toString(), toLabel.getText().toString(), val)));
        }
    }
}
