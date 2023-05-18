package meta1;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfaceRMI extends Remote {
    public String getUrl() throws RemoteException;

    public void addUrl(String url) throws RemoteException;

    public int getQueueSize() throws RemoteException;

    public boolean checkQueueForUrl(String url) throws RemoteException;

}
