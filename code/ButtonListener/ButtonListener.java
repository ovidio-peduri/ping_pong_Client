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

public class ButtonListen {
    public static final String BASE_URL = "https://safe-reaches-52945.herokuapp.com/api/rooms/";
    public static final int ROOM_NUMBER = 1;
    public static final String TEAM_A = "a";
    public static final String TEAM_B = "b";

    public static void main(String args[]) throws InterruptedException {
        System.out.println("PingPong Listener Started\nListening for button presses...");
        
        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        // provision gpio pin #02 as an input pin with its internal pull down resistor enabled
        final GpioPinDigitalInput teamA = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, PinPullResistance.PULL_DOWN);

        // create and register gpio pin listener
        teamA.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
		int state = event.getState().getValue();
		if (state == 1) {
			//Do nothing on press for now
		} else {
			String urlAddress = getUrl(true);
			System.out.println("Button A released.\nHitting: " + urlAddress);
			try {
				URL url = new URL(urlAddress);
				BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
				for (String line; (line = reader.readLine()) != null;) {
					System.out.println(line);
				}
			} catch(Exception e) {

			}

				
		}
            }
            
        });
       
	// provision gpio pin #03 as an input pin with its internal pull down resistor enabled
        final GpioPinDigitalInput teamB = gpio.provisionDigitalInputPin(RaspiPin.GPIO_03, PinPullResistance.PULL_DOWN);

        // create and register gpio pin listener
        teamB.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
		int state = event.getState().getValue();
		if (state == 1) {
			//Do nothing on press for now
		} else {

			System.out.println("Button B released.\n"+getUrl(false));
		}
            }
            
        });

 
        System.out.println(" ... complete the GPIO #02 circuit and see the listener feedback here in the console.");
        
        // keep program running until user aborts (CTRL-C)
        while(true) {
            Thread.sleep(500);
        }
        
        // stop all GPIO activity/threads by shutting down the GPIO controller
        // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        // gpio.shutdown();   <--- implement this method call if you wish to terminate the Pi4J GPIO controller        
    }

    public static String getUrl (Boolean teamAorB) {
	String returnUrl =  BASE_URL + ROOM_NUMBER + "/team/";
	if (teamAorB) {
		returnUrl += TEAM_A;
	} else {
		returnUrl += TEAM_B;
	}
	return returnUrl + "/increment";
    }
}
