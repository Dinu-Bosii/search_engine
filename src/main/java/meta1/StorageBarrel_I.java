package meta1;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.util.ArrayList;


public interface StorageBarrel_I extends Remote {
    public ArrayList<String> Search(String s) throws java.rmi.RemoteException, MalformedURLException, NotBoundException;
    public ArrayList<String> getLinks(String s) throws java.rmi.RemoteException, MalformedURLException, NotBoundException;
    
}


