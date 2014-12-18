import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;


public class Client{
	
	AESEncryption aes = new AESEncryption();
	Socket client;
	String username = null;
	String partner = null;
	String isPartnerTo = null;
	boolean disconnected = false;
	String salt = "LGHQGu695sTyW718";
	
    OutputStream output;
    OutputStreamWriter outputWriter;
    BufferedWriter bw;
    InputStream input;
    InputStreamReader inputReader;
    BufferedReader br;
    
	public Client(Socket clientSocket){
		this.client = clientSocket;
		
		Thread clientThread = new Thread(){
			public void run(){
				do{
					username = assignUsername();
				}while(username == null && !disconnected);
				sendMessage(client, "Username Set");
				if(!disconnected){
					sendMessage(client, "You are now registered, either double click someone's name or type /connect <username> to connect to someone");
					while(!disconnected){
						dealWithMessages();
					}
				}
			}
		};
		clientThread.start();
	}
	
	public void dealWithMessages(){
		String userInput = readMessage(client);
		boolean userMessage = false;
		try{
			if(userInput.substring(0, 9).equals("messagex ")){
				userMessage = true;
			}
			
			if(userMessage){
				userInput = userInput.substring(9);
				
				if(userInput.substring(0, 1).equals("/")){
					dealWithCommands(userInput);
				}else if(partner != null){
					try{
						Server.sendClientToClient(this, partner, "messagex " + userInput);
					}catch(ArrayIndexOutOfBoundsException e){
						System.err.println("Client Disconnected");
					}
				}
			}
		}catch(NullPointerException e){
			disconnect();
		}
		
	}
	
	public String assignUsername(){
		String username = null;
		try{
			do{
				username = readMessage(client);
			}while(!username.substring(0, 9).equals("username "));
			
			username = username.substring(9);
			
			if(Server.isValidUsername(username)){
				return username;
			}else{
				sendMessage(client, "Invalid Username");
				return null;
			}
		}catch(NullPointerException e){
			System.err.println("Failed assigning username, client probably disconnected");
			disconnect();
		}
		return username;

	}
	
	public void dealWithCommands(String userInput){
		try{
			if(userInput.substring(0, 9).equals("/connect ")){
				Server.connectToPartner(this, userInput.substring(9));
			}else{
				sendMessage(client, "Unknown Command");
			}
		}catch(StringIndexOutOfBoundsException e){
			sendMessage(client, "Unknown Command");
		}
	}
	
	public void disconnect(){
		if(!disconnected){
			disconnected = true;
			Server.notifyIsParterTo(this, isPartnerTo);
			Server.cleanUpClient(this);
		}
	}
	
	public void clientSendMessage(Socket client, String username, String message){
		try {
			output = client.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		outputWriter = new OutputStreamWriter(output);
		bw = new BufferedWriter(outputWriter);
		
		try {
			message = aes.encrypt(username + "," + message, salt);
		} catch (Exception e1) {
			System.err.println("Encryption Failed");
		}
		
		try {
			bw.write(message);
			bw.newLine();
			bw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void sendMessage(Socket client, String message){
		try {
			output = client.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		outputWriter = new OutputStreamWriter(output);
		bw = new BufferedWriter(outputWriter);
		
		try {
			message = aes.encrypt("serverxx " + message, salt);
		} catch (Exception e) {
			System.err.println("Encryption Failed");
		}
		
		
		try {
			bw.write(message);
			bw.newLine();
			bw.flush();
		} catch (IOException e) {
			System.out.println("Client Disconnected");
			disconnect();
		}
			
		

	}
	
	public String readMessage(Socket client){
		try {
			input = client.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		inputReader = new InputStreamReader(input);
		br = new BufferedReader(inputReader);
		
		String message = null;
		try {
			message = br.readLine();
		} catch (IOException e) {
			System.err.println("Client Disconnected");
			disconnect();
		}
		try {
			message = aes.decrypt(message, salt);
		} catch (Exception e) {
			System.err.println("Decryption Failed, client probably disconnected");
			disconnect();
		}
		return message;
	}	
	
	public boolean isConnected(){
		return this.disconnected;
	}
	
	public String getIsPartnerTo() {
		return isPartnerTo;
	}

	public void setIsPartnerTo(String isPartnerTo) {
		this.isPartnerTo = isPartnerTo;
	}

	public void setPartner(String partner){
		this.partner = partner;
	}
	
	public String getUsername(){
		return this.username;
	}
	
	public Socket getSocket(){
		return this.client;
	}
	
}
