package com.example.demo;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import meta1.SearchModule_I;
import meta1.indexObject;

@Controller
public class ClientController {
    private SearchModule_I sm = null;
    public ClientController() throws RemoteException {
        super();
    }

    @GetMapping("/")
    public String HomePage() {
        return "home";
    }


    @GetMapping("/indexUrlPage")
    public String IndexUrlPage()  {
        return "indexUrlPage";
    }


    @GetMapping("/search/{page}")
    public String Search(@RequestParam(defaultValue = "hello, World") String text, 
                         @PathVariable("page") String page, Model model) 
                         throws NotBoundException, RemoteException, MalformedURLException {
        Registry registry;
        
        try{
            registry = LocateRegistry.getRegistry(4040);
            sm = (SearchModule_I) registry.lookup("sm");
        }
        catch ( Exception e)
         {
            System.out.println("Could not connect to Search Module.");
            return "redirect:/";
        }

        try{
            List<indexObject> results = sm.GoogolSearch(text, -1, Integer.parseInt(page));
            model.addAttribute("urls", results);
            model.addAttribute("currentPage", page);
            model.addAttribute("query", text);
        }
        catch (Exception e)
        {
           //e.printStackTrace();
           System.out.println(e.getLocalizedMessage());
           return "redirect:/";
       }
        return "search";
    }
    

    @GetMapping("/indexUrl")
    public String IndexUrl(@RequestParam(required=true) String url, Model model) throws NotBoundException, RemoteException, MalformedURLException {
        Registry registry;
        
        try{ //fazer dentro do search()
            registry = LocateRegistry.getRegistry(4040);
            sm = (SearchModule_I) registry.lookup("sm");
        }
        catch ( Exception e)
         {
            System.out.println("Could not connect to Search Module.");
        }
        System.out.println("Connected to Search Module succesfully.");
        boolean success = false;
        try{
            success = sm.indexURL(url);
        }
        catch ( Exception e)
         {
            System.out.println("Error indexing the url.");
        }
        model.addAttribute("success", success);
        return "indexUrl";
    }

    @GetMapping("/urlLinks")
    public String UrlLinks(@RequestParam(required=true) String url, Model model) throws NotBoundException, RemoteException, MalformedURLException {
        Registry registry;
        
        try{ //fazer dentro do search()
            registry = LocateRegistry.getRegistry(4040);
            sm = (SearchModule_I) registry.lookup("sm");
        }
        catch ( Exception e)
         {
            System.out.println("Could not connect to Search Module.");

        }
        System.out.println("Connected to Search Module succesfully.");
        ArrayList<String> links = new ArrayList<>();
        try {
            links = sm.links(url);
        }
        catch ( Exception e)
         {
            System.out.println("Error retrieving links that point to: " + url);
        }
        model.addAttribute("url",url);
        model.addAttribute("linkedUrls", links);
        return "urlLinks";
    }


    @MessageMapping("/admin")
	@SendTo("/topic/AdminObject")
    public void administration(WebSocketSession session) throws NotBoundException, RemoteException, MalformedURLException {
        
    }
}
