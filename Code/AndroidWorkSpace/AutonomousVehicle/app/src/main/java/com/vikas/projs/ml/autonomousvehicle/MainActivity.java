package com.vikas.projs.ml.autonomousvehicle;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Created by Vikas K Vijayakumar(kvvikas@yahoo.co.in) on 12/21/2014.
 */
public class MainActivity extends ActionBarActivity {

    public static final String STREAMING_PORT_NUMBER = "com.vikas.projs.ml.autonomousvehicle.MainActivity.STREAMING_PORT_NUMBER";
    public static final String TOP_PIXEL_ROWS_STRIP_NUMBER = "com.vikas.projs.ml.autonomousvehicle.MainActivity.TOP_PIXEL_ROWS_STRIP_NUMBER";
    public static final String BOTTOM_PIXEL_ROWS_STRIP_NUMBER = "com.vikas.projs.ml.autonomousvehicle.MainActivity.BOTTOM_PIXEL_ROWS_STRIP_NUMBER";
    public static final String PERFORM_TRAINING = "com.vikas.projs.ml.autonomousvehicle.MainActivity.PERFORM_TRAINING";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("MainActivity","Started the onCreate Method");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //For the switch provided for choosing between LiveRun and Training,choose Training by default
        Switch performTrainingSwitch = (Switch) findViewById(R.id.switchLiveRun);
        performTrainingSwitch.setChecked(false);
        //The inputs for Top and Bottom pixel rows stripping need to be enabled only for LiveRun and not
        //when Training
        if(performTrainingSwitch != null){
            performTrainingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        toggleViewVisibilityForTraining(View.VISIBLE);
                    }else{
                        toggleViewVisibilityForTraining(View.INVISIBLE);
                    }
                }
            });
        }
        if (performTrainingSwitch.isChecked()){
            toggleViewVisibilityForTraining(View.VISIBLE);
        }else{
            toggleViewVisibilityForTraining(View.INVISIBLE);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i("MainActivity","Started the onCreateOptionsMenu Method");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i("MainActivity","Started the onOptionsItemSelected Method");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * This method will be called when the main button to get started will be clicked by the user
     * @param view
     */
    public void onMainButtonClick(View view){
        Log.i("MainActivity","Started the onMainButtonClick Method");
        //Create Intent to open the ProcessCameraPreview Activity
        Intent intent = new Intent(this, ProcessSensorData.class);

        //Pass the values supplied in the main screen for Training, streaming port number and the number of
        // pixels rows to be stripped from the top and bottom respectively
        Switch performTraining = (Switch) findViewById(R.id.switchLiveRun);
        if(performTraining.isChecked()){
            intent.putExtra(PERFORM_TRAINING,"false");
        }else{
            intent.putExtra(PERFORM_TRAINING,"true");
        }
        EditText streamingPortNumber = (EditText) findViewById(R.id.editTextStreamingPortNumber);
        intent.putExtra(STREAMING_PORT_NUMBER,Integer.valueOf(streamingPortNumber.getText().toString()));
        EditText topPixelRowsStripNumber = (EditText) findViewById(R.id.editTextTopPixelStripNumber);
        intent.putExtra(TOP_PIXEL_ROWS_STRIP_NUMBER, Integer.valueOf(topPixelRowsStripNumber.getText().toString()));
        EditText bottomPixelRowsStripNumber = (EditText) findViewById(R.id.editTextBottomPixelStripNumber);
        intent.putExtra(BOTTOM_PIXEL_ROWS_STRIP_NUMBER, Integer.valueOf(bottomPixelRowsStripNumber.getText().toString()));

        //Start the activity
        Log.i("MainActivity","About to start the intent for ProcessSensorData from the onMainButtonclick Method");
        startActivity(intent);
    }

    /**
     * This function is used to make the views for Top and Bottom pixel row count capture
     * invisible during a Training and visible during a LiveRun
     * @param visibility
     */
    private void toggleViewVisibilityForTraining(int visibility){
        Log.i("MainActivity","About to change the Visibility to: "+String.valueOf(visibility));
        TextView textViewTopPixelRows = (TextView) findViewById(R.id.textViewTopPixelStripNumber);
        textViewTopPixelRows.setVisibility(visibility);
        EditText editTextTopPixelRows = (EditText) findViewById(R.id.editTextTopPixelStripNumber);
        editTextTopPixelRows.setVisibility(visibility);
        TextView textViewBottomPixelRows = (TextView) findViewById(R.id.textViewBottomPixelStripNumber);
        textViewBottomPixelRows.setVisibility(visibility);
        EditText editTextBottomPixelRows = (EditText) findViewById(R.id.editTextBottomPixelStripNumber);
        editTextBottomPixelRows.setVisibility(visibility);
    }
}
