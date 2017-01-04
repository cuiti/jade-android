package chat.client;

import java.awt.*;
import java.awt.event.*;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;

import chat.client.agent.ChatClientAgent;

public class AWTChatGui extends Frame implements ChatGui {
	private ChatClientAgent myAgent;
	private TextField writeTf;
	private TextArea allTa;
	private ParticipantsFrame participantsFrame;
	final Browser browser = new Browser();
    BrowserView browserView = new BrowserView(browser);
	
	public AWTChatGui(ChatClientAgent a) {
		myAgent = a;
		
		setTitle("Chat: "+myAgent.getLocalName());
		setSize(getProperSize(256, 320));
		Panel p = new Panel();
		p.setLayout(new BorderLayout());
		writeTf = new TextField();
		p.add(writeTf, BorderLayout.CENTER);
		Button b = new Button("Send");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
		  	String s = writeTf.getText();
		  	if (s != null && !s.equals("")) {
			  	myAgent.handleSpoken(s);
			  	writeTf.setText("");
		  	}
			} 
		} );
		p.add(b, BorderLayout.EAST);
		add(p, BorderLayout.NORTH);
		
		allTa = new TextArea();
		allTa.setEditable(false);
		allTa.setBackground(Color.white);
		add(allTa, BorderLayout.CENTER);
		
		b = new Button("Participants");
		b.setBackground(Color.CYAN);
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!participantsFrame.isVisible()) {
					participantsFrame.setVisible(true);
				}	
			} 
		} );
		add(b, BorderLayout.SOUTH);
		
		participantsFrame = new ParticipantsFrame(this, myAgent.getLocalName());
		
		addWindowListener(new	WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				myAgent.doDelete();
			}
		} );
		
		
	    String dir = System.getProperty("user.dir");
	    JFrame mapFrame = new JFrame(dir);
	    
	    mapFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	    mapFrame.add(browserView, BorderLayout.CENTER);
	    mapFrame.setSize(900, 500);
	    mapFrame.setLocationRelativeTo(null);
	    mapFrame.setVisible(true);
	    
	    browser.loadURL(dir+"\\map.html");
	    
	    
	    
		show();
	}
	
	public void notifyParticipantsChanged(String[] names) {
		if (participantsFrame != null) {
			participantsFrame.refresh(names);
		}
	}
	
	public void notifySpoken(String speaker, String sentence) {
		allTa.append(speaker+": "+sentence+"\n");
		
		showCoordinatesOnMap(sentence);
	}
	
	Dimension getProperSize(int maxX, int maxY) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screenSize.width < maxX ? screenSize.width : maxX);
		int y = (screenSize.height < maxY ? screenSize.height : maxY);
		return new Dimension(x, y);
	}
	
	public void dispose() {
		participantsFrame.dispose();
		super.dispose();
	}
	
	private void showCoordinatesOnMap(String sentence){
		String javascriptMap ="";
		
		if ((sentence.length()<40)&&(sentence.contains("#"))){ 
			//length es la medida maxima de los dos numeros con signo y "#" como separador
			String[] parts = sentence.split("#");
			String latString = parts[0];
			String lonString = parts[1];
			
			Double latitude = Double.parseDouble(latString);
			Double longitude = Double.parseDouble(lonString);
			if ((latitude != null)&&(longitude!=null)){
				javascriptMap += "var myLatlng = new google.maps.LatLng("
						+latitude+","
						+longitude+");\n" +
			               "var marker = new google.maps.Marker({\n" +
			               "    position: myLatlng,\n" +
			               "    map: map,\n" +
			               "    title: 'Hola agente!'\n" +
			               "});";
				
				browser.executeJavaScript(javascriptMap);
			}
			System.out.println(javascriptMap);
		}
	}
}



