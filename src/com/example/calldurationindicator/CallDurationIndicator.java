package com.example.calldurationindicator;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Vibrator;

public class CallDurationIndicator extends Activity {

	public static final String PREFS_NAME	= "CallDurIndCfgFile";
    public static final String PREFS_DUR	= "duration";
    public static final String PREFS_SNZ	= "snooze";

	
	final static int NO				= 0;
	final static int YES			= 1;
	final static int STATE_IDLE 	= 0;
	final static int STATE_OFFHOOK	= 1;
	final static int STATE_RINGING	= 2;
	final static int DURATION_DEF	= 10; /* 10 sec */
	final static int SNOOZE_DEF		= 3;  /* 3  sec */
	
	static int Previous_State		= STATE_IDLE;
	static int cancel_timer 		= YES;
	
	static Vibrator 	durationIndicator;	
	TelephonyManager	telephonyManager;
	PhoneStateListener	listener;
	
	Button SaveBtn;
	String Durstr;
	String Snzstr;
	EditText DurTxt;
	EditText SnzTxt;
	TextView OutTxt;
	
	String durtext;
	String snztext;
	String Hlptxt;
	
	public int duration;
	public int snooze;
	  
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Hlptxt = new String ("\n" + "Minimum Value for Duration = " + Integer.toString(DURATION_DEF) + " seconds" + "\n" + "Minimum Value for Snooze = " + Integer.toString(SNOOZE_DEF) + " seconds" + "\n");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_duration_indicator);

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        durationIndicator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        boolean firstrun = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean("firstrun", true);
        if (firstrun)
        {				       

        	getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putBoolean("firstrun", false)
            .commit();
        	duration = DURATION_DEF;
        	snooze = SNOOZE_DEF;
        	durtext = Integer.toString(duration);
        	snztext = Integer.toString(snooze);
        } else {        	

        	durtext = "";
            SharedPreferences pref = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);   
            durtext = (pref.getString(PREFS_DUR, null));
            snztext = "";  
            snztext = (pref.getString(PREFS_SNZ, null));                

        }
      
        OutTxt = (TextView) findViewById(R.id.outtext);
        OutTxt.setText(Hlptxt);
        DurTxt = (EditText) findViewById(R.id.durtxt);
        SnzTxt = (EditText) findViewById(R.id.snztxt);    
        
        DurTxt.setText(durtext, TextView.BufferType.EDITABLE);
        SnzTxt.setText(snztext, TextView.BufferType.EDITABLE);
                
        SaveBtn = (Button) findViewById(R.id.savebtn); 
        SaveBtn.setOnClickListener(new OnClickListener() 
        {			
				public void onClick(View v) 
				{					
					durtext = DurTxt.getText().toString();
					snztext = SnzTxt.getText().toString();
					if ((durtext!=null && durtext.length()>0) && (snztext!=null && snztext.length()>0)) 
					{
						if (Integer.parseInt(durtext) < DURATION_DEF) {
							durtext = Integer.toString(DURATION_DEF);
							DurTxt.setText(Integer.toString(DURATION_DEF), TextView.BufferType.EDITABLE);						
						}
				        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
				        .edit()
			            .putString(PREFS_DUR, durtext)
			            .commit();
				        
				        if (Integer.parseInt(snztext) < SNOOZE_DEF) {
				        	snztext = Integer.toString(SNOOZE_DEF);
				        	SnzTxt.setText(Integer.toString(SNOOZE_DEF), TextView.BufferType.EDITABLE);
				        }
				        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
				        .edit()
			            .putString(PREFS_SNZ, snztext)
			            .commit();

			            try {
			            	duration = Integer.parseInt(durtext);
			            	if (duration < DURATION_DEF) {
			            		duration = DURATION_DEF;		            		
			            	}
			            }
			            catch(NumberFormatException exc) {
			            	System.out.println("Invalid format");
			            	duration = DURATION_DEF;
			            }
			            try {
			            	snooze = Integer.parseInt(snztext);
			            	if (snooze < SNOOZE_DEF) {
			            		snooze = SNOOZE_DEF;
			            	}
			            }
			            catch(NumberFormatException exc) {
			            	System.out.println("Invalid format");
			            	snooze = SNOOZE_DEF;
			            }
				        
			            Toast.makeText(getApplicationContext(),"Saved",Toast.LENGTH_LONG).show();			            				                  																																										
					}
				}
		});
        
        
        /* Create a new PhoneStateListener */
        listener = new PhoneStateListener() {
          @Override
          public void onCallStateChanged(int state, String incomingNumber) {
            String stateString = "N/A";
            switch (state) {
              case TelephonyManager.CALL_STATE_IDLE:
                stateString = "Idle";
                System.out.format("Idle%n");
                if(Previous_State == STATE_OFFHOOK) {
            	  /* Cancel Timer, call/dial over */
            	  cancel_timer = YES;
                }
                Previous_State = STATE_IDLE;
                break;
              
              case TelephonyManager.CALL_STATE_OFFHOOK:
                stateString = "Off Hook";
                System.out.format("Off Hook%n");
                if(Previous_State == STATE_IDLE) {
            	  System.out.format("Start Timer%n");
            	  /* Start Timer */
            	  cancel_timer = NO;
            	  new TimerClass(duration, snooze, 1);
                }
                Previous_State = STATE_OFFHOOK;
                break;
              
              case TelephonyManager.CALL_STATE_RINGING:
                stateString = "Ringing";
                System.out.format("Ringing%n");
                Previous_State = STATE_RINGING;
                cancel_timer = YES;
                break;
            }
            System.out.format("\nonCallStateChanged: %s\n", stateString);
          }
        };
        /* Register the listener wit the telephony manager */
        telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_call_duration_indicator, menu);
        return true;
    }

    public static void CallDurIndic () {
    	if (cancel_timer == NO)
    	  durationIndicator.vibrate(500);
    	System.out.format("Vibrt%n");
    }
}