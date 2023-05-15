package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.client.RestTemplate;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import meta1.RMIClient;
import meta1.RMIClient_I;
import meta1.SearchModule_I;
import meta1.indexObject;

@Controller
public class ClientController {
    private SearchModule_I sm = null;
    RMIClient_I c = null;
    public ClientController() throws RemoteException {
        super();
    }

    @GetMapping("/")
    public String HomePage() {
        
        return "home";
    }

    @GetMapping("/hello")
    public String Hello(Model model) {
        //String message = "Welcome to my application!";
        //model.addAttribute("greeting", message);
        return "hello";
    }

    @GetMapping("/search/{page}")
    public String Search(@RequestParam(required=true) String text, @RequestParam(defaultValue = "-1") int id,@PathVariable("page") int page, Model model) throws NotBoundException, RemoteException, MalformedURLException {
        Registry registry;
        
        try{
            c = new RMIClient();
            registry = LocateRegistry.getRegistry(4040);
            sm = (SearchModule_I) registry.lookup("sm");
            System.out.println("Connected to Search Module succesfully.");
            ArrayList<indexObject> results = sm.GoogolSearch(c, text, id, page);
            model.addAttribute("urls", results);
            model.addAttribute("currentPage", page);
        }
        catch ( Exception e)
         {
            System.out.println("Could not connect to Search Module.");
            return "redirect:home";
        }
        return "search";
    }

    @GetMapping("/indexUrl")
    private String IndexUrl(@RequestParam(required=true) String url, Model model) throws NotBoundException, RemoteException, MalformedURLException {
        Registry registry;
        
        try{ //fazer dentro do search()
            c = new RMIClient();
            registry = LocateRegistry.getRegistry(4040);
            sm = (SearchModule_I) registry.lookup("sm");
        }
        catch ( Exception e)
         {
            System.out.println("Could not connect to Search Module.");
        }
        System.out.println("Connected to Search Module succesfully.");
        boolean success = sm.indexURL(url);
        model.addAttribute("success", success);
        return "search";
    }
}
