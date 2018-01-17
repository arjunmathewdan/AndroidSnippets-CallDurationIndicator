package com.example.calldurationindicator;

import java.util.Timer;
import java.util.TimerTask;

public class TimerClass {
    Timer timer;
    int period;
    int snzperiod;

    public TimerClass(int dur, int snz, int i) {
    	snzperiod = snz;
    	if (i == 1)
    		period = dur;
    	else 
    		period = snz;
    	
    	timer = new Timer();
        if (CallDurationIndicator.cancel_timer != 1) {        
        	timer.schedule(new RemindTask(), period*1000);
        }
	}

    class RemindTask extends TimerTask {
        public void run() {
            System.out.format("Time's up!%n");
            CallDurationIndicator.CallDurIndic();
            timer.cancel(); //Terminate the timer thread
            new TimerClass(snzperiod, snzperiod, 1);
        }
    }
}
