import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import java.net.URL;
import java.net.URLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ButtonListener {
    public static final String BASE_URL = "https://safe-reaches-52945.herokuapp.com/api/rooms/";
    public static final String TEAM_A = "a";
    public static final String TEAM_B = "b";
    public int roomNumber;

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
        
        // keep program running until user aborts (CTRL-C)
        while(true) {
            Thread.sleep(500);
        }
        
        // stop all GPIO activity/threads by shutting down the GPIO controller
        // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        // gpio.shutdown();   <--- implement this method call if you wish to terminate the Pi4J GPIO controller        
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
