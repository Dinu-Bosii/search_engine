package meta1;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import com.example.demo.AdminObject;

public interface SearchModule_I extends Remote {
    public boolean indexURL(String s) throws java.rmi.RemoteException, MalformedURLException, NotBoundException;

    public AdminObject AdminInfo() throws RemoteException, MalformedURLException, NotBoundException;

    public ArrayList<indexObject> GoogolSearch(String s, int page)
            throws java.rmi.RemoteException, MalformedURLException, NotBoundException;

    public ArrayList<String> links(String s) throws java.rmi.RemoteException, MalformedURLException, NotBoundException;

}
