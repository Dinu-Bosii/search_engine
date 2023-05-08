package meta1;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.rmi.NotBoundException;
import java.net.MalformedURLException;


public class RMIClient extends UnicastRemoteObject implements RMIClient_I {
	// estrutura de dados
	RMIClient() throws RemoteException {
		super();
	}

	public static void main(String[] args) throws NotBoundException, RemoteException, MalformedURLException {
		
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(input);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> { 
			System.out.println("Exiting...");
			try {
				reader.close();
				input.close();
			} catch (IOException e) {
				//e.printStackTrace();
				System.out.println("error");
			}
			

		}));
		Registry registry;
		SearchModule_I sm = null;
		try {
			try{
				registry = LocateRegistry.getRegistry(4040);
				sm = (SearchModule_I) registry.lookup("sm");
			}
			catch ( Exception e)
			{
				System.out.println("Could not connect to Search Module.");
				System.out.println("Retry? (y/n)");
				String answer = reader.readLine();
				boolean connected = false;
				while (answer.equals("y"))
				{
					try
					{
						registry = LocateRegistry.getRegistry(4040);
						sm = (SearchModule_I) registry.lookup("sm");	
						connected = true;
						break;
					}	
					catch(Exception e2){
						System.out.println("Retry? (y/n)");
						answer = reader.readLine();
					}
				}
				if(!connected)
				{
					System.out.println("leaving...");
					return;
				}
			}


			RMIClient_I c = new RMIClient();

			String option = null;
			String url = "";
			String username = "";
			String password = "";
			boolean logged = false;
			boolean active = true;
			boolean connected = true;
			while(active) {
				if(!connected){
					try{
						registry = LocateRegistry.getRegistry(4040);
						sm = (SearchModule_I) registry.lookup("sm");
						System.out.println("Connected to Search Module.");
						connected = true;
					}
					catch ( Exception e)
					{
						System.out.println("Could not connect to Search Module.");
						System.out.println("Retry? (y/n)");
						String answer = reader.readLine();
						while (answer.equals("y"))
						{
							try
							{
								registry = LocateRegistry.getRegistry(4040);
								sm = (SearchModule_I) registry.lookup("sm");	
								connected = true;
								System.out.println("Connected to Search Module.");
								break;
							}	
							catch(Exception e2){
								System.out.println("Retry? (y/n)");
								answer = reader.readLine();
								continue;
							}
						}
						if(!connected)
						{
							System.out.println("leaving...");
							return;
						}
						
					}
					
				}
				// print do menu
				showMenu(logged);
				option = reader.readLine();
				if(option == null || option.equals("0"))
				{
					input.close();
					reader.close();
					break;
				}

				System.out.println("option = " + option);

				switch (option) {
					case "1": //indexar URL
						System.out.println("URL: ");
						url = reader.readLine();
						try {
							
							boolean t = sm.indexURL(url);
							if(t){
								System.out.println("URL indexed succesfully");
							}
							else{
								System.out.println("Could not index URL");
							}
						} catch(RemoteException re){
							connected = false;
							System.out.println("Lost connection to the server.");
						} 
						catch (Exception e) {
							System.out.println("Could not index URL.");
							//e.printStackTrace();
						}
						break;
					case "2": //Stats
						try{
							sm.info(c);
						} catch(RemoteException re){
							connected = false;
							System.out.println("Lost connection to the server.");
						} 
						catch ( Exception e)
						{
							//e.printStackTrace();
							System.out.println("Error retrieving stats.");
						}
						break;
					case "3": //register
						System.out.println("Username: ");
						username = reader.readLine();
						System.out.println("Password: ");
						password = reader.readLine();
						try{
							if(sm.register(username, password)){
								System.out.println("Registered succesfully.");
								logged = true;
							}
							else{
							System.out.println("Could not register with those credentials.");
							}
						} catch(RemoteException re){
							connected = false;
							System.out.println("Lost connection to the server.");
						} catch ( Exception e)
						{
							System.out.println("Could not register.");
						}

						break;
					case "4"://login
						System.out.println("Username: ");
						username = reader.readLine();
						System.out.println("Password: ");
						password = reader.readLine();
						//login func
						try{
							if(sm.login(username, password)){
							System.out.println("Logged in succesfully.");
							logged = true;
							}
							else{
							System.out.println("Wrong credentials.");
							}
						} catch(RemoteException re){
							connected = false;
							System.out.println("Lost connection to the server.");
						} catch ( Exception e)
						{
							System.out.println("Could not login.");
						}
						break;
					case "5"://search for words
						System.out.println("Search for:");
						String search =(String) reader.readLine();
						while (search == null || search.length() < 2)
						{
							System.out.println("Search for:");
							search = (String) reader.readLine();

						}
						try{
							sm.GoogolSearch(c, search);
						} catch(RemoteException re){
							connected = false;
							System.out.println("Lost connection to the server.");
						} catch( Exception e)
						{
							//e.printStackTrace();
							System.out.println("Error while trying to search.");
						}
						break;
					case "6": //search for links
						if(logged){
							System.out.println("Search links for:");
							String urlForLinks = (String) reader.readLine();
							
							try{
								sm.links(c, urlForLinks);
							} catch(RemoteException re){
								connected = false;
								System.out.println("Lost connection to the server.");
							} 
							catch(Exception e)
							{	
								e.printStackTrace();
								System.out.println("Error retrieving links");
							}
						}
						break;
					case "0":
						reader.close();
						input.close();
						active = false; //doesn't work
						break;
					default:
						break;
				}
			} // quitting the program

		} catch (Exception e) {
			//e.printStackTrace();
			System.out.println("Exception in main: " + e);
		}
	}


	public static void showMenu(boolean logged) {

		System.out.println("-------------------");
		System.out.println("1.Index an URL");
		System.out.println("2.Show statistics");
		System.out.println("3.Register");
		System.out.println("4.Login");
		System.out.println("5.Search");
		if(logged){
			System.out.println("6.Pages linked to given URL");
		}
		System.out.println("0.Quit");
		System.out.println("-------------------");
		System.out.println("Option: ");

	
	}

	public void printOnClient(String information) throws java.rmi.RemoteException {
		System.out.println(information);
	}

	public void printResults(ArrayList<String> results) throws java.rmi.RemoteException
	{
		for(int i = 0; i < results.size(); i+= 3)
		{
				String url = results.get(i);
				System.out.println("URL:" + url);
				String title = results.get(i+1);
				System.out.println("Title: " + title);
			
				String citation = results.get(i+2);
				System.out.println("\"" + citation + "...\"");
			
		}
	}

}
