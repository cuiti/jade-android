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

import agentes.AgenteDesktop;
import ontologia.InfoMensaje;

import javax.swing.JLabel;
import java.awt.Font;

public class ControlPanel {
	private AgenteDesktop agenteDesktop;
	private JFrame frame;
	private JTextArea textArea;
	private Browser browser;
	private BrowserView browserView;    

	public ControlPanel(AgenteDesktop agenteDesktop){
    	this.setAgenteDesktop(agenteDesktop);
    	this.setFrame(new JFrame("Panel de Control"));
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
		
		JLabel labelTextArea = new JLabel("Estado de las Conexiones");
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
		
		JLabel labelMap = new JLabel("Mapa de las Ubicaciones");
		labelMap.setFont(new Font("Times New Roman", Font.BOLD, 18));
		labelMap.setHorizontalAlignment(JLabel.CENTER);
		
		panelMap.add(labelMap, BorderLayout.NORTH);
		
		editorPane.setLayout(new BorderLayout());
				
		editorPane.add(this.getBrowserView(), BorderLayout.CENTER);;
		
		panelMap.add(editorPane);
		
		this.getFrame().getContentPane().add(panelMap);
		
		
		
		this.getFrame().setVisible(true);
		
		URL url = this.getClass().getResource("/gui/map.html");
		this.getBrowser().loadURL(url.toString());
		
	}
	
	public void notifySpoken(InfoMensaje infoMensaje) {
		this.getTextArea().append(infoMensaje.toString());		
		this.showCoordinatesOnMap(infoMensaje);
	}
	
	private void showCoordinatesOnMap(InfoMensaje infoMensaje){
		Double latitud = infoMensaje.getLatitud();
		Double longitud = infoMensaje.getLongitud();
		
		if ((latitud != null)&&(longitud!=null)){
			String javascriptMap ="";
			
			javascriptMap += "var myLatlng = new google.maps.LatLng("
					+latitud+","
					+longitud+");\n" +
			        "var marker = new google.maps.Marker({\n" +
			        "    position: myLatlng,\n" +
			        " animation: google.maps.Animation.DROP," +
			        "    map: map,\n" +
			        "    title: " + infoMensaje.getNombreMarcaModelo() + "\n" +
			        "});";
				
			this.getBrowser().executeJavaScript(javascriptMap);
		}			
	}

	public AgenteDesktop getAgenteDesktop() {
		return agenteDesktop;
	}

	private void setAgenteDesktop(AgenteDesktop agenteDesktop) {
		this.agenteDesktop = agenteDesktop;
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



