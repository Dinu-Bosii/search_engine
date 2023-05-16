package meta1;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.util.ArrayList;

import com.example.demo.AdminObject;


public interface SearchModule_I extends Remote {
    public boolean indexURL(String s) throws java.rmi.RemoteException, MalformedURLException, NotBoundException;
    //public static AdminObject info() throws java.rmi.RemoteException, MalformedURLException, NotBoundException;
    public ArrayList<indexObject> GoogolSearch(String s, int id, int page) throws java.rmi.RemoteException, MalformedURLException, NotBoundException;
    public boolean login(String username, String password) throws java.rmi.RemoteException, MalformedURLException, NotBoundException;
    public boolean register(String username, String password) throws java.rmi.RemoteException, MalformedURLException, NotBoundException;
    public ArrayList<String> links(String s) throws java.rmi.RemoteException, MalformedURLException, NotBoundException;

}


