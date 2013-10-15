package com.example.AsyncTaskExample;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private TextView txtTitle;

    private class SomeTask extends AsyncTask<Integer, Integer, Integer>{

        @Override
        protected Integer doInBackground(Integer... params) {
            for(int i=1; i<=10; i++){
                Log.d(TAG, "i:" + Integer.toString(i));

                try {
                    publishProgress(i);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return 1;
        }

        @Override
        protected void onProgressUpdate(Integer... values){
            Log.i(TAG, "onProgressUpdate");
            txtTitle.setText(Integer.toString(values[0]));
        }

        protected void onPreExecute(){
            Log.i(TAG, "onPreExecute");
        }

        protected void onPostExecute(Integer result){
            Log.i(TAG, "onPostExecute");
            txtTitle.setText("Finished");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        txtTitle = (TextView)findViewById(R.id.txt_title);
        txtTitle.setText("Started");
    }

    public void buttonPressed(View view){
        Log.i(TAG, "buttonPressed");
        txtTitle.setText("Pressed");

        new SomeTask().execute();
    }

}
