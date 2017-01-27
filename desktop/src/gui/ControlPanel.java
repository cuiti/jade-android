package gui;


import java.awt.BorderLayout;
import java.net.URL;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;

import agentes.ChatClientAgent;

import javax.swing.JLabel;
import java.awt.Font;
import java.io.Serializable;

public class ControlPanel {
	private ChatClientAgent chatClientAgent;
	private JFrame frame;
	private JTextArea textArea;
	private Browser browser;
	private BrowserView browserView;    

	public ControlPanel(ChatClientAgent chatClientAgent){
    	this.setChatClientAgent(chatClientAgent);
    	this.setFrame(new JFrame("Panel de Conexiones"));
    	this.setTextArea(new JTextArea(40,40));
    	this.setBrowser(new Browser());
    	this.setBrowserView(new BrowserView(this.getBrowser()));
    	this.initialize();
    }
	
	private void initialize() {
		this.getFrame().setSize(1024, 768);
		this.getFrame().setLocationRelativeTo(null);
		this.getFrame().setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				
		this.getFrame().getContentPane().setLayout(new BorderLayout());
		
		JPanel panelText = new JPanel();
		
		JScrollPane scrollPane = new JScrollPane(this.getTextArea(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); 
		
		JLabel labelTextArea = new JLabel("Estado de Conexiones");
		labelTextArea.setFont(new Font("Times New Roman", Font.BOLD, 18));
		labelTextArea.setHorizontalAlignment(JLabel.CENTER);
		scrollPane.setColumnHeaderView(labelTextArea);
		
		this.getTextArea().setEditable(false);	
				
		((DefaultCaret)this.getTextArea().getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
				
		panelText.add(scrollPane);
		
		this.getFrame().getContentPane().add(panelText, BorderLayout.WEST);
		
		JPanel panelMap = new JPanel();
		
		panelMap.setLayout(new BorderLayout());
		
		JEditorPane editorPane = new JEditorPane();
		
		JLabel labelMap = new JLabel("Mapa de Conexiones");
		labelMap.setFont(new Font("Times New Roman", Font.BOLD, 18));
		labelMap.setHorizontalAlignment(JLabel.CENTER);
		
		panelMap.add(labelMap, BorderLayout.NORTH);
		
		editorPane.setLayout(new BorderLayout());
				
		editorPane.add(this.getBrowserView(), BorderLayout.CENTER);;
		
		panelMap.add(editorPane);
		
		this.getFrame().getContentPane().add(panelMap);
		
		
		
		this.getFrame().setVisible(true);
		
		URL url = this.getClass().getResource("/chat/client/map.html");
		this.getBrowser().loadURL(url.toString());
		
	}
	
	public void notifyParticipantsChanged(String[] names) {
		//Ver que pasa con este metodo
		/*if (participantsFrame != null) 
			participantsFrame.refresh(names);*/
	}
	
	public void notifySpoken(String speaker, Serializable sentence) {
		//MensajeConInformacion m = (MensajeConInformacion)sentence;
		
		this.getTextArea().append(speaker+": "+sentence+"\n");		
		//this.showCoordinatesOnMap(sentence);
	}
	
	public void dispose() {
		//Ver que pasa con este metodo
		//participantsFrame.dispose();
		//super.dispose();
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
			               " animation: google.maps.Animation.BOUNCE," +
			               "    map: map,\n" +
			               "    title: 'Hola agente!'\n" +
			               "});";
				
				this.getBrowser().executeJavaScript(javascriptMap);
			}
			System.out.println(javascriptMap);
		}
	}

	public ChatClientAgent getChatClientAgent() {
		return chatClientAgent;
	}

	private void setChatClientAgent(ChatClientAgent chatClientAgent) {
		this.chatClientAgent = chatClientAgent;
	}
	
	public JFrame getFrame() {
		return frame;
	}

	private void setFrame(JFrame frame) {
		this.frame = frame;
	}
	
	public JTextArea getTextArea() {
		return textArea;
	}

	private void setTextArea(JTextArea textArea) {
		this.textArea = textArea;
	}

	public Browser getBrowser() {
		return browser;
	}

	private void setBrowser(Browser browser) {
		this.browser = browser;
	}
	
	public BrowserView getBrowserView() {
		return browserView;
	}

	private void setBrowserView(BrowserView browserView) {
		this.browserView = browserView;
	}
}



