/*
 * EPM Chat (Server)
 */
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import javax.swing.*;
import javax.swing.event.*;

public class ChatServer implements ActionListener{
    private JButton servstart = new JButton ("Start Server");;
    private JButton servstop = new JButton ("Stop Server");
    private JLabel jcomp3 = new JLabel ("Port:");
    private JTextField portbox = new JTextField ("9001");
    
    //New jPanel
    JFrame server;
    
    //Listener
    ServerSocket listener;
    
    //GUI
    static ChatServer serverGUI = new ChatServer();
    
    //Server Decelerations
    private static HashSet<String> names = new HashSet<String>();
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
    
    //Declare Password
    static String servpassword;
    static String inpassword;
    
    static Boolean isrun = true;
    
    //Servrun
    public ChatServer() {
    	server = new JFrame ("Server");
        //adjust size and set layout
    	server.getContentPane().setPreferredSize (new Dimension (110, 84));
    	server.getContentPane().setLayout (null);

        //add components
    	server.getContentPane().add (servstart);
    	server.getContentPane().add (servstop);
    	server.getContentPane().add (jcomp3);
    	server.getContentPane().add (portbox);

        //set component bounds (only needed by Absolute Positioning)
        servstart.setBounds (5, 35, 110, 20);
        servstop.setBounds (5, 60, 110, 20);
        jcomp3.setBounds (0, 7, 35, 20);
        portbox.setBounds (30, 5, 85, 25);
        
        //Pack Frame
        server.pack();
        
        //lock frame
        server.setResizable(false);
        
        //Add Action Listener
        servstart.addActionListener(this);
        servstop.addActionListener(this);
       
        
        //Disable GUI items
        servstop.setEnabled(false);
        
        //closing
        server.setDefaultCloseOperation(server.DO_NOTHING_ON_CLOSE);
    }
    public static void main (String[] args) throws IOException {
        serverGUI.server.setVisible(true);
        
    }
    public static class Handler extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                while (true) {
                	if (isrun == false){
                		return;
                	}
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }
                    out.println("ENTERPASS");
                    inpassword = in.readLine();
                    if (inpassword.equals(servpassword)){
                    	out.println("PASSACCEPTED");
                    }else{
                    	out.println("WRONGPASS");
                    	return;
                    }
                    synchronized (names) {
                        if (!names.contains(name)) {
                            names.add(name);
                            break;
                        }
                    }
                }

                out.println("NAMEACCEPTED");
                writers.add(out);
                while (isrun == true) {
                		String input = in.readLine();
                        if (input == null) {
                            return;
                        }
                        for (PrintWriter writer : writers) {
                            writer.println("MESSAGE " + name + ": " + input);
                        }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                
                if (name != null) {
                    names.remove(name);
                }
                if (out != null) {
                    writers.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == servstart){
			servpassword  = JOptionPane.showInputDialog(null, "Enter Server Password", "", JOptionPane.PLAIN_MESSAGE);
			servrun();
			servstop.setEnabled(true);
		}else if(e.getSource() == servstop){
			try {
				stopserver();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	public void stopserver() throws IOException{
		isrun = false;
		servstop.setEnabled(false);
		servstart.setEnabled(true);
		serverGUI.server.dispose();
		try{
			listener.close();
		}catch(Exception e){
			
		}
		serverGUI.listener.close();
		server.setDefaultCloseOperation(server.EXIT_ON_CLOSE);
		server.dispose();
	}
	public void connect() throws IOException{
		int PORT = 9001;
		try{
			PORT = Integer.parseInt(portbox.getText());
		}catch(Exception e){
			JOptionPane.showMessageDialog(null, "Incorrect Port");
			return;
		}
		portbox.setEnabled(false);
		listener = new ServerSocket(PORT);
		servstart.setEnabled(false);
        try {
            while (isrun == true) {
            	try{
            		new Handler(listener.accept()).start();
            	}catch (Exception e){
            		
            	}
            }
        } finally {
            listener.close();
        }
	}
	public void servrun(){
		Runnable serv = new Runnable(){
			public void run() {
				try {
					connect();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		if(isrun == true){
			new Thread(serv).start();
		}else{
			
		}
		
	}
}
