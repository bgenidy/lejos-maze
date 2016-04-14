//CS1010 MazeProgram
import lejos.nxt.*;
import lejos.nxt.LightSensor;
import lejos.robotics.navigation.*;
import java.util.ArrayList;
//Following imports for sound
import java.io.File;
import lejos.nxt.Sound;

public class FinalMaze
{
	//checking our bounds
    /*The following 3 lines below need to be adjusted depending on 
     * the amount of light is being reflected on the surface of the 
     * wodden maze because that will impact the sensors calculations*/
	private static final int black = 350;
	private static final int begSilverRange = 525;
	private static final int endSilverRange = 1000;

	//lejos methods needed
	private static DifferentialPilot pilot = new DifferentialPilot(2.1f, 4.4f, Motor.A, Motor.C);  // parameters in inches;;
	private static LightSensor light = new LightSensor(SensorPort.S3);
	private static UltrasonicSensor sonic = new UltrasonicSensor(SensorPort.S1);

	//keeps track of robot
	//private static ArrayList<Integer> keepTrack = new ArrayList<Integer>();
	private static int counter = 0;
	private static int[] keepTrack = new int[80];
	private static boolean atFinish = false;

	private static boolean isBlack() {
		return light.readNormalizedValue() <= black;
	}
	/******************1 is forward, 2 is rotate right, 3 is rotate left*****************/

	public static void main(String[] args){

		while(!atFinish)
		{
			//System.out.println(light.readNormalizedValue());
			
			celebrate();
			checkForOpening();
			celebrate();
	         //Follow Edge
			go();

	         //To See what color it is reading
			if (light.readNormalizedValue() > black && light.readNormalizedValue() < begSilverRange){
				System.out.println("WOOD " + light.readNormalizedValue());
			}
			if (light.readNormalizedValue() < black){
				System.out.println("BLACK " + light.readNormalizedValue());
			}
			
		}

	}

	public static void go(){
		pilot.setTravelSpeed(5);
		for(int i = 0; i < 8; i++){
			pilot.travel(1);
			follow();
			celebrate();
		}
		pilot.travel(3);
		push(4);

	}

	public static void goBack(){
		pilot.setTravelSpeed(5);
		for(int i = 0; i < 8; i++){
			pilot.travel(1);
			followBack();
		}
		pilot.travel(4);

	}

	public static void push(int i) {
      if (counter > keepTrack.length)
      {
         //error
         return;
      }
      keepTrack[counter] = i;
      counter++;
  	}

	private static void follow(){
		
		double i = 1;
		while (light.readNormalizedValue() > black){
			if((int)i%2 == 0){
				pilot.rotate(i+3);
				i*=-1.5;
				continue;
			}
			pilot.rotate(i);
			i*=-1.5;
		}

	}

	private static void followBack(){
		
		double i = 1;
		while (light.readNormalizedValue() > black){
			if((int)i%2 == 0){
				pilot.rotate(i+3);
				i*=-1.5;
				continue;
			}
			pilot.rotate(i);
			i*=-1.5;
		}

	}

	//Uses UltraSonic to check for opening -> Right, left, Front
	   public static void checkForOpening()
   {
   		celebrate();
   	  int possible = 0;

      pilot.setRotateSpeed(25);
      pilot.rotate(90);   
      //Checks for Opening on the right
      if(sonic.getDistance() > 25)
      {                                            
         possible+= 1;
      }
      
      //Checks for Opening on the left
      pilot.rotate(-180);
      if (sonic.getDistance() > 25)
      {     
      	possible+= 2;                                       
      }
      
      //Brings Back to the beginning
      pilot.rotate(90);
      if (sonic.getDistance() > 25)
      {
      	possible+= 4;
      }

      //************Different Moving Operations****************//

      //Non-decision RIGHT
      if (possible == 1) {
      	push(1);
      	pilot.rotate(90);
      	return;
      }
      //Non-decision  LEFT
      if (possible == 2) {
      	push(2);
      	pilot.rotate(-90);
      	return;
      }
      //DECSION RIGHT - Otherwise Left
      if (possible == 3) {
      	push(3);
      	pilot.rotate(90);
      	return;
      }
      
      //ONLY STRAIGHT
      if (possible == 4) {
      	return;
      }
      
      //DECSION RIGHT - Otherwise Straight
      if (possible == 5) {
      	push(5);
      	pilot.rotate(90);
      }
      //DECSION LEFT - Otherwise Straight
      if (possible == 6) {
      	push(6);
      	pilot.rotate(-90);
      }

      //NO OPTION...REVERSE
      if (possible == 0) {
      	pilot.rotate(180);
      	push(0);
      	//remove mess up...
      }

   }
   public static void fixMistakes() {
   		int startdel = 0;
   		int enddel = 0;

   		for (int i = counter - 1 ; i >= 0 ; i-- ) {
   			if (keepTrack[i] == 0){
   				for (int j = i-1; j >= 0; j--){
   					if(keepTrack[j] == 3 || keepTrack[j] == 5 || keepTrack[j]== 6){
   						startdel = j;
   						break;
   					}
   				}

   				for (int j = i+1; j < keepTrack.length; j++){
   					if(keepTrack[j] == 3 || keepTrack[j] == 5 || keepTrack[j] == 6){
   						enddel = j;
   						break;
   					}
   				}

   				int x = startdel;

   				for (; startdel <= enddel; startdel++){
   					keepTrack[startdel] = -1;
   				}

   				if(x == 3){
   					keepTrack[x] = 2;
   				}
   				else if (x == 5 || x == 6){
   					keepTrack[x] = 4;
   				}
   			}
   		}

   		int [] newKeepTrack = new int [keepTrack.length];
   		int newkeep = 0;

   		for (int i = 0; i< newKeepTrack.length; i++){
   			if(keepTrack[i] != 0 && keepTrack[i] != -1){
   				newKeepTrack[newkeep] = keepTrack[i];
   				newkeep++;
   			}
   		}

   		keepTrack = newKeepTrack;
   		counter = newkeep;
   }
	/****************************************Test Methods*********************************/

	public static void celebrate(){
	      //Check for Silver - Celebrate - HUGE ISSUE!!!! - Too Similar to the Wood
		if (light.readNormalizedValue() > begSilverRange && light.readNormalizedValue() < endSilverRange) {

			System.out.println("VICTORY");
			Sound.twoBeeps();
	         //Sound.playSample("ZeldaSecret.wav");
			atFinish = true;
			pilot.travel(1);
			//push(4);
			pilot.setRotateSpeed(100);
			pilot.rotate(180);
			pilot.setRotateSpeed(25);
			fixMistakes();
			reverse();
		}
	}

	public static void reverse() {
		for(int i = counter - 1; i >= 0; i--) {
			System.out.print("It should ");
			switch (keepTrack[i]){
				case 0:
					//pilot.rotate(180);
					System.out.println("do a u turn");
					break;
				case 1:
					//Go Right
					pilot.rotate(-90);
					System.out.println("Must Right");
					break;
				case 2:
					//Go Left
					pilot.rotate(90);
					System.out.println("Must Left");
					break;
				case 3:
					//Go Right
					pilot.rotate(-90);
					System.out.println("Decision Right");
					break;
				case 4:
					//Go Forward
					goBack();
					System.out.println("go Straight");
				case 5:
					//Go Left
					pilot.rotate(-90);
					System.out.println("Decision Right");
				case 6:
					//Go Right
					pilot.rotate(90);
					System.out.println("Decision Left");
					break;
				default:
					System.out.println("Mistake "+ i);
			atFinish = true;
			}
		}
	}
}
