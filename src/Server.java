import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.Timer;


public class Server{
	
	static ArrayList<Client> clients = new ArrayList<Client>();
	int clientNumber = 0;
	ServerSocket serverSocket;
	String salt = "LGHQGu695sTyW718";
	String allUsernames = "";
	Timer interval = new Timer(3000, new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			allUsernames = "";
			for(int i = 0; i < clients.size(); i++){
				if(clients.get(i).getUsername() != null){
					allUsernames += clients.get(i).getUsername() + ",";
				}
			}
			for(int i = 0; i < clients.size(); i++){
				clients.get(i).sendMessage(clients.get(i).getSocket(), "listall" + allUsernames);
			}
			
		}
	});
	
	public Server(int port){
		boolean hasStarted = true;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println("Unable To Start Server On Port " + port);
			hasStarted = false;
		}
		if(hasStarted){
			listenForClients();
			interval.start();
		}
	}
	
	public void listenForClients(){
		Runnable listen = new Runnable(){
			@Override
			public void run() {
				try {
					while(true){
						System.out.println("Searching");
						Socket clientSocket = serverSocket.accept();
						clients.add(new Client(clientSocket));
						System.out.println("Client " + String.valueOf(clients.size()-1) + " connected");
					}
				} catch (IOException e) {
					System.err.println("Could Not Find Client");
				}
			}
		};
		Thread listenThread = new Thread(listen);
		listenThread.start();
	}
	
	public static void notifyIsParterTo(Client fromClient, String username){
		if(clientNumByUsername(username) > -1)
			clients.get(clientNumByUsername(username)).sendMessage(clients.get(clientNumByUsername(username)).getSocket(), fromClient.getUsername() + " has disconnected");
		
	}
	
	public static void sendClientToClient(Client fromClient, String toUsername, String message){
		fromClient.clientSendMessage(clients.get(clientNumByUsername(toUsername)).getSocket(), fromClient.getUsername(), message);
	}
	
	public static boolean isValidUsername(String username){
		boolean isValid = true;
		for(int i = 0; i < clients.size(); i++){
			if(username.equals(clients.get(i).getUsername()))
				isValid = false;
		}
		
		if(username.contains(","))
			isValid = false;
		return isValid;
	}
	
	public static int clientNumByUsername(String username){
		int clientNum = -1;
		try{
			for(int i = 0; i < clients.size(); i++){
				
				if(username.equals(clients.get(i).getUsername()))
					clientNum = i;
	
			}
		}catch(NullPointerException e){
		}
		
		return clientNum;
	}
	
	public static void connectToPartner(Client fromClient, String toUsername){
		if(clientNumByUsername(toUsername) > -1){
			fromClient.setPartner(toUsername);
			clients.get(clientNumByUsername(toUsername)).setIsPartnerTo(fromClient.getUsername());
			fromClient.sendMessage(fromClient.getSocket(), "Connected to " + toUsername);
		}else{
			fromClient.sendMessage(fromClient.getSocket(), "User doesn't exist");
		}
	}
	
	public static void cleanUpClient(Client client){
		if(client.getUsername() != null){
			System.out.println("Removed client " + client.getUsername());
			clients.remove(client);
		}else{
			System.out.println("Removed client " + clients.indexOf(client));
			clients.remove(client);
		}
	}
}
