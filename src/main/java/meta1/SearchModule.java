package meta1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.time.LocalTime;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import com.example.demo.AdminObject;

public class SearchModule extends UnicastRemoteObject implements SearchModule_I {
    static ArrayList<RMIClient_I> clients = new ArrayList<>(); // para vários clientes
    static ArrayList<profile> clients_registered = new ArrayList<>();
    private static final String MULTICAST_DOWN = "224.3.2.2";
    private static int PORT_d = 4322;
    private static final String MULTICAST_BARREL = "224.3.2.3";
    private static int PORT_b = 4323;
    private static ArrayList<AliveObject> downloaders = new ArrayList<>();
    private static ArrayList<AliveObject> barrels = new ArrayList<>();
    private static final Random rand = new Random();
    private static final long SLEEP_TIME_ACTIVE = 2000;
    private static int PAGE_SIZE = 10;
    private static int ID = 0;
    private static final long SLEEP = 5000;
    private static AdminObject currentStats;
    @Autowired
    private static SimpMessagingTemplate messagingTemplate;
    static Map<String, Integer> Searches = new HashMap<>();


    protected SearchModule() throws RemoteException {
        super();
    }

    public static void main(String[] args) throws RemoteException {
        SearchModule_I sm = new SearchModule();
        LocateRegistry.createRegistry(4040).rebind("sm", sm);
        System.out.println("Search Module Server ready.");
        SearchModule s1 = new SearchModule();

        SearchModule.CheckIfActive th1 = s1.new CheckIfActive(downloaders, MULTICAST_DOWN, PORT_d);
        SearchModule.CheckIfActive th2 = s1.new CheckIfActive(barrels, MULTICAST_BARREL, PORT_b);
        new Thread(th1).start();
        new Thread(th2).start();
        try {
            currentStats = info();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Runnable run = () -> {
            try{    
                while (true) {
                    AdminObject checkStats = info();
                    if (currentStats != checkStats){
                        currentStats = checkStats;
                        //send to Websocket
                        messagingTemplate.convertAndSend("/topic/adminStats", currentStats);
                    }
                    Thread.sleep(SLEEP);
                }
            }
            catch (InterruptedException e) {
                System.out.println(e.getMessage());
                //e.printStackTrace();
            } finally {
                Thread.currentThread().interrupt();
            }

        };
        new Thread(run).start();
    }

    public boolean indexURL(String s) throws java.rmi.RemoteException, MalformedURLException, NotBoundException {
        try{
             InterfaceRMI q = (InterfaceRMI) Naming.lookup("queue");
            q.addUrl(s);
        } catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // vai ficar à escuta num canal multicast pelas mensagens dos
    // barrels/downloaders para saber se estão ativos
    public class CheckIfActive implements Runnable {
        String MULTICAST_ADDRESS_ACTIVE;
        int PORT;

        ArrayList<AliveObject> aliveArray = new ArrayList<>();

        public CheckIfActive(ArrayList<AliveObject> array, String multIp, int p) {
            this.aliveArray = array;
            this.MULTICAST_ADDRESS_ACTIVE = multIp;
            this.PORT = p;
        }

        @Override
        public void run() {
            System.out.println(
                    "thread created with multicast address: " + MULTICAST_ADDRESS_ACTIVE + " : " + PORT);
            try (MulticastSocket socket = new MulticastSocket(PORT)) {

                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS_ACTIVE);
                socket.joinGroup(group);

                while (true) {
                    byte[] buffer = new byte[1024];

                    //System.out.println("waiting for packets...");
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String data = new String(packet.getData(), 0, packet.getLength());
                    String[] parts = data.split(":");
                    //System.out.println(data);
                    int porto = Integer.parseInt(parts[0]);
                   
                    //System.out.println("barrel:" + packet.getAddress() + ":" + packet.getPort());
                    boolean exists = false;

                    for (AliveObject ao : this.aliveArray) {
                        if (ao.getPort() == porto) {
                            ao.setTime(LocalTime.now());
                            exists = true;
                        }
                    }

                    if (!exists) {
                        AliveObject obj = new AliveObject(packet.getAddress(), porto,
                                LocalTime.now());
                        aliveArray.add(obj);
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class AliveObject {
        InetAddress ip;
        int port;
        LocalTime time;

        public AliveObject(InetAddress newIp, int newPort, LocalTime newTime) {
            this.ip = newIp;
            this.port = newPort;
            this.time = newTime;
        }

        public LocalTime getTime() {
            return this.time;
        }

        public InetAddress getIp() {
            return ip;
        }

        public void setIp(InetAddress ip) {
            this.ip = ip;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public void setTime(LocalTime t) {
            this.time = t;
        }

    }


    public static AdminObject info() {
        //subscribe the client

        //----------------------------------Obter Top 10 pesquisas
        List<Map.Entry<String, Integer>> topSearches = new ArrayList<>(Searches.entrySet());
        Collections.sort(topSearches, (e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        List<Map.Entry<String, Integer>> top10Searches = topSearches.subList(0, Math.min(10, topSearches.size()));
        List<String> top10Admin = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : top10Searches) {
            top10Admin.add(entry.getKey());
        }

        checkBarrels();
        checkDownloaders();
        
        ArrayList<String> adminDownloaders = new ArrayList<>();
        ArrayList<String> adminBarrels = new ArrayList<>();
        for(AliveObject d : downloaders)
        {
            adminDownloaders.add("IP:" + d.getIp() + "|PORT:" + d.getPort());
        }
        for(AliveObject b : barrels)
        {
            adminBarrels.add("IP:" + b.getIp() + "|PORT:" + b.getPort());
        }

        return new AdminObject(top10Admin, adminBarrels, adminDownloaders);
        
    }


    public ArrayList<indexObject> GoogolSearch(String s, int id, int page) throws java.rmi.RemoteException, MalformedURLException, NotBoundException {
        // check if there are Barrels active
        System.out.println("Searching for: " + s);
        
        /*if (id != 0 && searchResults.containsKey(id)){
            if (searchResults.get(id).size() >= 10) {
                return new ArrayList<indexObject>(searchResults.get(id).subList(page * PAGE_SIZE, (page + 1) * PAGE_SIZE));
            } else {
               return new ArrayList<indexObject>(searchResults.get(id));
            }
            //return new ArrayList<indexObject>(searchResults.get(id).subList(page * PAGE_SIZE, (page + 1) * PAGE_SIZE));
            
        }*/
        if (!checkBarrels()) {
            System.out.println("No Storage Barrel active. Try again later.");
            return null;
        }
        int max = barrels.size();
        
        int chosenBarrel;
        if(max == 1)
        {
            chosenBarrel = 1;
        }
        else{
            chosenBarrel = rand.nextInt(max);
        }
        System.out.println("barrel chosen = " + chosenBarrel + ", and there are " + max);
        int barrelPort = 0;
        try{
            AliveObject b = barrels.get(chosenBarrel - 1);
            barrelPort = b.getPort();
            System.out.println("barrel port " + barrelPort);
        } catch ( Exception e)
        {
            e.printStackTrace();
        }

        Registry registry = LocateRegistry.getRegistry(barrelPort);
        StorageBarrel_I brInter = (StorageBarrel_I) registry.lookup("StorageBarrel" + barrelPort);
        ArrayList<indexObject> results = null;
        try
        {
            results =  brInter.Search(s);
        }catch (RemoteException re){
            //retry with another barrel if there is one
            /*if(checkBarrels()){
                for (AliveObject barrel : barrels)
                {
                    if(barrel.getPort() != barrelPort)
                    {
                        try{
                            barrelPort = barrel.getPort();
                            System.out.println("barrel port " + barrelPort);
                        } catch ( Exception e)
                        {
                            e.printStackTrace();
                        }
                
                        registry = LocateRegistry.getRegistry(barrelPort);
                        brInter = (StorageBarrel_I) registry.lookup("StorageBarrel" + barrelPort);
                        try
                        {
                            results =  brInter.Search(s);
                        } catch (Exception e){
                            continue;
                        }
                    }
                }
            } */
        }
        catch (Exception e2)
        {
            e2.printStackTrace();
            System.out.println("Error while searching.");
        }
        ArrayList<indexObject> results_10 = new ArrayList<>();
        if(results == null)
        {
            System.out.println("not indexed");
        }
        else if(results.isEmpty()){
            System.out.println("No results found.");
        }
        else{     

            results_10 = new ArrayList<indexObject>(results.subList(0, Math.min(10, results.size())));

            System.out.println("Found results: " + results.size());
        }
        // print do lado do cliente
        return results_10;
    }

    
    public ArrayList<String> links(String s) throws java.rmi.RemoteException, MalformedURLException, NotBoundException {
        // check if there are Barrels active
        System.out.println("Searching links for...");
        if (!checkBarrels()) {
            System.out.println("No Storage Barrel active. Try again later.");
            return new ArrayList<>();
        }
        int max = barrels.size();
        
        int chosenBarrel;
        if(max == 1)
        {
            chosenBarrel = 1;
        }
        else{
            chosenBarrel = rand.nextInt(max);
        }
        System.out.println("barrel chosen for searching links: " + chosenBarrel);
        int barrelPort = 0;
        try{
            AliveObject b = barrels.get(chosenBarrel - 1);
            barrelPort = b.getPort();
        } catch ( Exception e)
        {
            e.printStackTrace();
        }

        Registry registry = LocateRegistry.getRegistry(barrelPort);
        StorageBarrel_I brInter = (StorageBarrel_I) registry.lookup("StorageBarrel" + barrelPort);
        ArrayList<String> results = new ArrayList<String>();
        try
        {
            results =  brInter.getLinks(s);
        }catch (RemoteException re){
            //retry with another barrel if there is one
            
        }
        catch (Exception e2)
        {
            e2.printStackTrace();
            System.out.println("Error while searching.");
        }
        /*/
        if(results.isEmpty())
        {
            return new ArrayList<>();;
        }
        else{
            for(String st : results){
                c.printOnClient("url:" + st);
            }
        }*/

        return results;
    }


    public static boolean checkBarrels() {
        boolean flag = false;
        ArrayList<AliveObject> toRemove = new ArrayList<>();
        for (AliveObject obj : barrels) {
            LocalTime currentTime = LocalTime.now();
            LocalTime barrelTime = obj.getTime();

            if (barrelTime.plusSeconds(SLEEP_TIME_ACTIVE/1000).isAfter(currentTime)) {
                //System.out.println( "barrel:" + barrelTime + ", current:" + currentTime);
                flag = true;
            } else {
                toRemove.add(obj);

            }
            
        }
    
        for (AliveObject dead : toRemove)
        {
            System.out.println("removing :" + dead.getPort());
            barrels.remove(dead);
        }
        return flag;
    }
    

    public static boolean checkDownloaders() {
        boolean flag = false;
        ArrayList<AliveObject> toRemove = new ArrayList<>();
        for (AliveObject obj : downloaders) {
            LocalTime currentTime = LocalTime.now();
            LocalTime DownloaderTime = obj.getTime();

            if (DownloaderTime.plusSeconds(SLEEP_TIME_ACTIVE/1000).isAfter(currentTime)) {
                System.out.println( "barrel:" + DownloaderTime + ", current:" + currentTime);
                flag = true;
            } 
            else {
                toRemove.add(obj);
            }
        }
        for (AliveObject dead : toRemove)
        {
            downloaders.remove(dead);
        }
        return flag;
    }
    
    public class profile {
        String username;
        String password;
        public profile(String username, String password) {
            this.username = username;
            this.password = password;
        }
        public String getUsername() {
            return username;
        }
        public void setUsername(String username) {
            this.username = username;
        }
        public String getPassword() {
            return password;
        }
        public void setPassword(String password) {
            this.password = password;
        }
    }
    

    public boolean login(String username, String password)
    {
        for(profile us : clients_registered)
        {
            if (us.getUsername().equals(username) && us.getPassword().equals(password))
            {
                return true;
            }
        }
        return false;
    }
    
    
    public boolean register(String username, String password)
    {
        for(profile us : clients_registered)
        {
            if (us.getUsername().equals(username))
            {
                return false;
            }
        }
        profile newProfile = new profile(username, password);
        clients_registered.add(newProfile);
        return true;
    }
}
