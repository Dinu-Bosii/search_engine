package meta1;

//https://gist.github.com/alopes/5358189 //stop words pt
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class StorageBarrel extends UnicastRemoteObject implements StorageBarrel_I {

    public static HashMap<String, HashSet<indexObject>> index = new HashMap<>();
    public static HashMap<String, HashSet<String>> urlsIndex = new HashMap<>();
    public static HashSet<String> StopWords = new HashSet<>();

    public StorageBarrel() throws RemoteException {
        super();
    }

    public static void main(String[] args) throws IOException {
        int portArg;
        if (args.length < 1) {
            System.out.println(" Missing argument {PORT}");
            return;
        } else {
            portArg = Integer.parseInt(args[0]);
        }
        File file = new File(System.getProperty("user.dir") + "\\search_engine\\src\\main\\java\\meta1\\stopwords.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String st;
            while ((st = br.readLine()) != null) {
                StopWords.add(st);
            }
        } catch (Exception e) {
            System.out.println("Error reading stop words");
            return;
        }

        Thread t;
        try {
            StorageBarrel barrelInter = new StorageBarrel();
            LocateRegistry.createRegistry(portArg).rebind("StorageBarrel" + portArg, barrelInter);
            StorageBarrel.SendLifeProof th1 = barrelInter.new SendLifeProof(portArg, "StorageBarrel:" + portArg);
            t = new Thread(th1);
            t.start();
            System.out.println("Storage Barrel ready.");
        } catch (Exception e) {
            System.out.println("RMI interface problems");
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Exiting...");
            t.interrupt();
        }));

        // thread to write the content of the index on a file
        /*
         * Runnable run = () -> {
         * while(true)
         * {
         * try {
         * Thread.sleep(20000);
         * Properties properties = new Properties();
         * properties.putAll(index);
         * 
         * properties.store(new FileOutputStream("Storage.txt"), null);
         * } catch (InterruptedException e) {
         * Thread.currentThread().interrupt();
         * }
         * catch (Exception e) {
         * e.printStackTrace();
         * System.out.println("Error writing file");
         * }
         * }
         * };
         * new Thread(run).start();
         */
        //
        receiveMulticast();

    }

    // Receives the information from the Downloader
    public static void receiveMulticast() throws IOException {
        int port = 4321;
        String MULTICAST_ADDRESS = "224.3.2.1";
        class objectAux {
            public objectAux(int id, InetAddress add, int pORT, int wordNum, int urlsNum, String url, String title,
                    String citacao) {
                this.id = id;
                this.add = add;
                PORT = pORT;
                this.wordNum = wordNum;
                this.urlsNum = urlsNum;
                this.url = url;
                this.citacao = citacao;
                this.title = title;

            }

            int id;
            InetAddress add;
            int PORT;
            int wordNum;
            int urlsNum;
            String url;
            String citacao;
            String title;

            public InetAddress getAdd() {
                return add;
            }

            public int getPORT() {
                return PORT;
            }

            public String getUrl() {
                return url;
            }

            public int getId() {
                return id;
            }

            public String getCitacao() {
                return citacao;
            }

            public String getTitle() {
                return title;
            }

        }

        ArrayList<objectAux> received = new ArrayList<>();
        InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);

        try (MulticastSocket socket = new MulticastSocket(port)) {
            socket.joinGroup(group);
            while (true) {
                byte[] buffer = new byte[8192];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // Deserialze object
                ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
                ObjectInputStream ois = new ObjectInputStream(bais);
                objectMulticast obj = null;

                try {
                    Object readObject = ois.readObject();
                    ois.close();
                    bais.close();
                    if (readObject instanceof objectMulticast) {
                        obj = (objectMulticast) readObject;
                    } else {
                        System.out.println("The received object is not of right type!");
                    }
                } catch (EOFException eof) {
                    System.out.println("ObjectInputStream is throwing EOFException");
                    continue;

                } catch (Exception e) {
                    // e.printStackTrace();
                    System.out.println("No object could be read from the received UDP datagram.");
                }

                if (obj != null) {
                    if (obj.getType().equals("1")) {

                        objectAux newAux = new objectAux(obj.getId(), packet.getAddress(), packet.getPort(),
                                obj.getWord_parts(),
                                obj.getUrl_parts(), obj.getUrl(), obj.getTitulo(), obj.getCitacao());
                        received.add(newAux);
                        for (objectAux aux : received) {
                            if (packet.getAddress().equals(aux.getAdd()) && packet.getPort() == aux.getPORT()
                                    && aux.getId() < obj.getId() - 1) {
                                received.remove(aux);
                                break;
                            }
                        }
                        // System.out.println("TYPE 1");

                    }
                    if (obj.getType().equals("2")) {
                        // System.out.println("TYPE 2");
                        for (objectAux aux : received) {
                            if (packet.getAddress().equals(aux.getAdd()) &&
                                    packet.getPort() == aux.getPORT() && aux.getId() == obj.getId()) {
                                for (String u : obj.getUrls()) {
                                    if (urlsIndex.containsKey(u)) {
                                        if (!urlsIndex.get(u).contains(aux.getUrl())) {
                                            urlsIndex.get(u).add(aux.getUrl());
                                        }
                                    } else {
                                        HashSet<String> set1 = new HashSet<>();
                                        set1.add(aux.getUrl());
                                        urlsIndex.put(u, set1);
                                    }

                                }
                            }
                        }
                    }
                    if (obj.getType().equals("3")) {
                        // System.out.println("TYPE 3");
                        for (objectAux aux : received) {
                            if (packet.getAddress().equals(aux.getAdd()) &&
                                    packet.getPort() == aux.getPORT() && aux.getId() == obj.getId()) {
                                for (String w : obj.getWords()) {
                                    String cur = w.toLowerCase();
                                    if (StopWords.contains(cur)) {
                                        continue;
                                    }
                                    if (index.containsKey(cur)) {
                                        boolean urlExists = false;
                                        for (indexObject indexAux : index.get(cur)) {
                                            if (indexAux.getUrl().equals(aux.getUrl())) {
                                                urlExists = true;
                                                break;
                                            }
                                        }
                                        if (!urlExists) {
                                            indexObject objNew = new indexObject(obj.getUrl(), aux.getTitle(),
                                                    aux.getCitacao(), 0);
                                            index.get(cur).add(objNew);
                                        }

                                    } else {
                                        HashSet<indexObject> set1 = new HashSet<>();
                                        set1.add(new indexObject(obj.getUrl(), obj.getTitulo(), obj.getCitacao(), 0));
                                        index.put(cur, set1);
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Mandar sinal de vida demonstrar que está ativo
    public class SendLifeProof implements Runnable {
        int port = 4323;
        String ADDRESS = "224.3.2.3";
        long SLEEP = 2000;
        int interfacePORT;
        String interfaceNAME;

        public SendLifeProof(int interfacePORT, String interfaceNAME) {
            this.interfacePORT = interfacePORT;
            this.interfaceNAME = interfaceNAME;
        }

        @Override
        public void run() {

            try (MulticastSocket socket = new MulticastSocket()) {

                InetAddress group = InetAddress.getByName(ADDRESS);
                socket.joinGroup(group);

                // Enviar a mensagem a dizer que está vivo
                String alive = interfacePORT + ":StorageBarrel";

                while (true) {

                    DatagramPacket alivePacket = new DatagramPacket(alive.getBytes(), alive.length(), group, port);
                    // System.out.println("Sending packets...");
                    socket.send(alivePacket);

                    Thread.sleep(SLEEP); // esperar 15 segundos
                }

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("IOException...");
                // Thread.currentThread().interrupt();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    public ArrayList<indexObject> Search(String s) {
        // remove stop words

        String[] tokens = s.split("[\\s,]+");
        ArrayList<indexObject> results = new ArrayList<>();
        for (var word : tokens) {

            if (!(index.containsKey(word.toLowerCase()))) { // ver se as palavras foram indexadas

                System.out.println(word + ": not indexed");
                // System.out.println(index.keySet());

                return results;
            }
        }
        ;
        HashSet<indexObject> set1 = index.get(tokens[0].toLowerCase());

        for (indexObject obj1 : set1) {
            String currUrl = obj1.getUrl();

            int k = 1;
            for (int i = 1; i < tokens.length; i++) {
                for (indexObject obj2 : index.get(tokens[i].toLowerCase())) {
                    if ((obj2.getUrl().equals(currUrl))) {
                        k++;
                        break;
                    }
                }
            }
            if (k == tokens.length) {
                indexObject uObject = new indexObject(currUrl, obj1.getTitulo(), obj1.getCitacao(),
                        urlsIndex.get(currUrl).size());
                results.add(uObject);

            }
        }
        Comparator<indexObject> scoreComparator = Comparator.comparingInt(indexObject::getrelevance).reversed();
        Collections.sort(results, scoreComparator);
        System.out.println("Found results: " + results.size());

        return results;
    }

    public ArrayList<String> getLinks(String s) {
        ArrayList<String> urls = new ArrayList<>();
        HashSet<String> set1 = new HashSet<>();
        try {
            set1 = urlsIndex.get(s);
        } catch (Exception e) {
            System.out.println("Could not retrieve links.");
        }
        if (set1.isEmpty()) {
            return urls;
        } else {
            for (String u : set1) {
                urls.add(u);
            }
        }
        return urls;
    }

}
