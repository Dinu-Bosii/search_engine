package meta1;


import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;


public class URLQueue extends UnicastRemoteObject implements InterfaceRMI {
    public static ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(200000);

    public URLQueue() throws RemoteException {
        super();
    }
    
    public static void main(String[] args) throws RemoteException{
        URLQueue q = new URLQueue();
        LocateRegistry.createRegistry(1099).rebind("queue", q);
        System.out.println("URLQueue ready.");
        queue.add("https://jsoup.org/cookbook/introduction/");
        ArrayList<Thread> ths = new ArrayList<Thread>();
        for (int i = 0; i < 3; i++)
        {
            //criar 3 downloaders
            Downloader d = new Downloader();
            d.setDaemon(true);
            new Thread(d).start();
            ths.add(d);
        }
    }

    public void addUrl(String url) {
        try {
            queue.put(url);
            //System.out.println("adding:" + url);
        } catch (InterruptedException e)  {
            e.printStackTrace();
        }
    }

    public String getUrl() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
    public int getQueueSize() {
        return queue.size();
    }
    public boolean checkQueueForUrl(String url) {
        return queue.contains(url);
    }
}
