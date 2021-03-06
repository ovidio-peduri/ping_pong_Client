import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.gpio.trigger.GpioPulseStateTrigger;
import java.net.URL;
import java.net.URLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ButtonListener {
    private static final String BASE_URL = "https://pardot-pingpong.herokuapp.com/api/rooms/";
    private static final String TEAM_A = "a";
    private static final String TEAM_B = "b";
    private int roomNumber;

    public ButtonListener(int roomNumber) {
    	this.roomNumber = roomNumber;
	try {	
		run();
	} catch (InterruptedException ie) {
		System.out.println("Inturrupted Exception occured while running the client");
	}
    }

    private void run() throws InterruptedException {
        System.out.println("PingPong Listener Started for room " + this.roomNumber + "\nListening for button presses...\nPress CTRL+C to exit...");
        
        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        // provision gpio pin #02 as an input pin with its internal pull down resistor enabled
        final GpioPinDigitalInput teamA = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, PinPullResistance.PULL_DOWN);

        // create and register gpio pin listener
        teamA.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
	    	handleButtonPressEvent(event, true);
            }
            
        });
       
	// provision gpio pin #03 as an input pin with its internal pull down resistor enabled
        final GpioPinDigitalInput teamB = gpio.provisionDigitalInputPin(RaspiPin.GPIO_03, PinPullResistance.PULL_DOWN);


        // create and register gpio pin listener
        teamB.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
	    	handleButtonPressEvent(event, false);
            }   
        });
        
	//Set up the trigger pin so that the camera is signaled when to start/stop recording
	//GpioPinDigitalOutput triggerPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "Trigger", PinState.LOW);
	//Pulse the triggerPin for 50ms
       	//teamA.addTrigger(new GpioPulseStateTrigger(PinState.HIGH, triggerPin, 50));
	
	// keep program running until user aborts (CTRL-C)
        while(true) {
            Thread.sleep(500);
        }
    }

    /**
    * Handle Button presses and make a call to the heroku app api
    * @param GpioPinDigitalStateChangeEvent event
    * @param Boolean buttonAorB for which button is this event? true = A, false = B
    */
    private void handleButtonPressEvent(GpioPinDigitalStateChangeEvent event, boolean buttonAorB) {
	String buttonName = buttonAorB == true ? "A" : "B";
	int state = event.getState().getValue();
	if (state == 1) {
		//Do nothing on press for now
	} else {
		String urlAddress = getUrl(buttonAorB);
		System.out.println("Button " + buttonName);
		try {
			URL url = new URL(urlAddress);
			System.out.println(url);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
		} catch(Exception e) {
		}
	}
    }
    /**
    * Form the api url based on which button is pressed
    * @param Boolean buttonAorB which team should be incremented? true = A, false = B
    */
    private String getUrl (Boolean teamAorB) {
	String returnUrl =  BASE_URL + this.roomNumber + "/team/";
	if (teamAorB) {
		returnUrl += TEAM_A;
	} else {
		returnUrl += TEAM_B;
	}
	return returnUrl + "/increment";
    }

	public static void main (String args[]) {
		int roomNumber = 1;
		if (args.length > 0) {
			try 
			{
				roomNumber = Integer.parseInt(args[0]);
			} catch (Exception e) {
				System.out.println("Invalid room number... Exiting...");
				System.exit(1);
			}
		}
		ButtonListener buttonListener = new ButtonListener(roomNumber);
	}
}
