package meta1;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;


public interface RMIClient_I extends Remote {
    void printOnClient(String information) throws RemoteException;
    void printResults(ArrayList<indexObject> results) throws RemoteException;
}


