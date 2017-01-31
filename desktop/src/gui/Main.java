package gui;

import jade.MicroBoot;
import jade.core.MicroRuntime;

public class Main extends MicroBoot {
	
	private static String getName(){
		return "desktop" + Integer.toString((int)Math.floor(Math.random() * 999) + 1);
	}
	  
	public static void main(String args[]) {
		MicroBoot.main(args);
		try {
			MicroRuntime.startAgent(Main.getName(), "agentes.AgenteDesktop", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
}	
 