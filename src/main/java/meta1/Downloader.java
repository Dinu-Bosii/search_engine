package meta1;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.net.InetAddress;
import java.net.DatagramPacket;

public class Downloader extends Thread {
    private HashSet<String> processed = new HashSet<>();

    @Override
    public void run() {

        Runnable run = () -> {
            String aliveDownloaders = "224.3.2.2";
            int PORT_ALIVE = 4322;
            long SLEEP = 2000;

            // envia para o Search module a dizer que está vivo
            try (MulticastSocket socketSM = new MulticastSocket()) {
                InetAddress group = InetAddress.getByName(aliveDownloaders);
                socketSM.joinGroup(group);
                // Enviar a mensagem a dizer que está vivo
                int PORT = socketSM.getLocalPort();

                String alive = PORT + ":" + "Downloader";
                while (true) {
                    //System.out.println("Sending packets...");
                    DatagramPacket alivePacket = new DatagramPacket(alive.getBytes(), alive.length(), group, PORT_ALIVE);
                    socketSM.send(alivePacket);
                    Thread.sleep(SLEEP); // esperar 1 segundo
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                Thread.currentThread().interrupt();
            }

        };
        new Thread(run).start();

        try (MulticastSocket socketBarrel = new MulticastSocket()) {
            // envia para os barrels
            String multicast_Downloader = "224.3.2.1";
            InetAddress toBarrels = InetAddress.getByName(multicast_Downloader);
            socketBarrel.joinGroup(toBarrels);

            InterfaceRMI rmi = (InterfaceRMI) Naming.lookup("queue");
            int id = 0;

            while (true) { // keep reading from the url queue
                if (id == Integer.MAX_VALUE - 1) { id = 0; }
                id++;
                try {

                    String url = rmi.getUrl();
                    //System.out.println("url:" + url);
                    //System.out.println("queue size is " + rmi.getQueueSize());
                    Document doc;

                    try{
                     doc = Jsoup.connect(url).get();
                    }
                    catch ( Exception e)
                    {
                        //add to badUrls? TODO
                        //System.out.println("bad url: " + url);
                        continue;
                    }

                    StringTokenizer tokens = new StringTokenizer(doc.text());
                    Elements links = doc.select("a[href]");
                    String[] urls = new String[links.size()];
                    String abs = "abs:href";
                    int i = 0;
                    processed.add(url);
                    for (Element link : links) // meter urls na fila
                    {
                        // System.out.println(link.text() + "\n" + link.attr(abs) + "\n");
                        urls[i++] = link.attr(abs);
                    }
                    
                    
  
                    String[] words = new String[tokens.countTokens()];
                    int k = 0;
                    while (tokens.hasMoreTokens()) {
                        String curr = tokens.nextToken();
                        //if token not in stop words
                            words[k++] = curr;
                    }
                    
                    String title = doc.title();
                    String citacao;
                    String text = doc.text();
                    if(text.length() > 150){
                        citacao = doc.text().substring(0, 150);   
                    }
                    else{
                        citacao = doc.text().substring(0, text.length());
                    }

                    //type 1 - first packet
                    //type 2 - urls
                    //type 3 - words
                    
                    int partsUrl = (int) Math.ceil(urls.length / 10.0); //por implementar
                    int partUrlAtual = 1;
                    int partsWords = (int) Math.ceil(words.length / 10.0); 
                    int partWordsAtual = 1;
                    objectMulticast obj = new objectMulticast(id, partsWords, partsUrl, "1", null, null, url, title, citacao);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    oos.writeObject(obj);
                    byte[] data = baos.toByteArray();

                    //System.out.println("data size is " + data.length);
                    int PORT_TO_BARREL = 4321;
                    socketBarrel.send(new DatagramPacket(data, data.length, toBarrels, PORT_TO_BARREL));
                    oos.close();
                    baos.close();

                    //---------------------------------------------------------------------------enviar urls
                    //enviar um número de urls de cada vez por pacote

                    int sourcePos = 0;
                    int size2 = 10;
                    int size = size2;
                    while(sourcePos < urls.length){
                        if (sourcePos + size2 > urls.length)
                        {
                            size = urls.length - sourcePos;
                            
                        }
                        
                        String[] urls_sliced;
                        urls_sliced = Arrays.copyOfRange(urls, sourcePos, sourcePos + size);

                        obj = new objectMulticast(id, partUrlAtual, 0,"2", null, urls_sliced, url, null, null);
                        baos = new ByteArrayOutputStream();
                        oos = new ObjectOutputStream(baos);
                        oos.writeObject(obj);
                        data = baos.toByteArray();   
                        DatagramPacket packetSend = new DatagramPacket(data, data.length, toBarrels, PORT_TO_BARREL);        
  
                        socketBarrel.send(packetSend);
                        oos.close();
                        baos.close();

                        sourcePos += size2;
                        partUrlAtual++;
            
                        //System.out.println("data 1size is " + data.length);
                    }

                    //-------------------------------------------------------------------------------enviar words
                    sourcePos = 0;
                    
                    size = size2;
                    while(sourcePos < words.length){
                        if (sourcePos + size2 > words.length)
                        {
                            size = words.length - sourcePos;

                        }
                        String[] words_sliced;
                        words_sliced = Arrays.copyOfRange(words, sourcePos, sourcePos + size);
                        obj = new objectMulticast(id, partWordsAtual, 0, "3", words_sliced, null, url, null, null);
                        baos = new ByteArrayOutputStream();
                        oos = new ObjectOutputStream(baos);
                        oos.writeObject(obj);
                        data = baos.toByteArray();                            
                        socketBarrel.send(new DatagramPacket(data, data.length, toBarrels, PORT_TO_BARREL));
                        oos.close();
                        baos.close();
                        sourcePos += size2;

                        //System.out.println("data 2size is " + data.length);
                    }
                    /*
                    Runnable  run2 = () -> {
                        for (String u: urls) 
                        {
                            try {
                                if (!processed.contains(u) && !rmi.checkQueueForUrl(u)) {
                                    rmi.addUrl(u);
                                }

                            } catch (RemoteException e) {
                                //e.printStackTrace();
                                System.out.println("Could not add to the queue the url:" + u);
                            }
                        }

                    };
                    new Thread(run2).start();*/
                    for (String u: urls) 
                    {
                        try {
                            if (!processed.contains(u) && !rmi.checkQueueForUrl(u)) {
                                rmi.addUrl(u);
                            }

                        } catch (RemoteException e) {
                            //e.printStackTrace();
                            System.out.println("Could not add to the queue the url:" + u);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("ERROR HERE");
                }

            }
        } catch (IOException | NotBoundException e) {
            e.printStackTrace();
        }
    }

 }

// urls de sites que não existem devem ser guardados na mesma
// numa lista de urls
