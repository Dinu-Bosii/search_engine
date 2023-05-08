package meta1;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;


public interface SearchModule_I extends Remote {
    public boolean indexURL(String s) throws java.rmi.RemoteException, MalformedURLException, NotBoundException;
    public void info(RMIClient_I c) throws java.rmi.RemoteException, MalformedURLException, NotBoundException;
    public int GoogolSearch(RMIClient_I c, String s, int id, int page) throws java.rmi.RemoteException, MalformedURLException, NotBoundException;
    public boolean login(String username, String password) throws java.rmi.RemoteException, MalformedURLException, NotBoundException;
    public boolean register(String username, String password) throws java.rmi.RemoteException, MalformedURLException, NotBoundException;
    public int links(RMIClient_I c, String s) throws java.rmi.RemoteException, MalformedURLException, NotBoundException;

}


