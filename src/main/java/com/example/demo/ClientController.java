package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import meta1.SearchModule;
import meta1.SearchModule_I;

@RestController
public class ClientController {
    private SearchModule_I sm = null;

    public ClientController() {
        Registry registry;
        try{
            registry = LocateRegistry.getRegistry(4040);
            sm = (SearchModule_I) registry.lookup("sm");
        }
        catch ( Exception e)
         {
            System.out.println("Could not connect to Search Module.");
            //TODO retry instead of leaving
            System.exit(0);
        }
        System.out.println("Connected to Search Module succesfully.");
    }

    @GetMapping("/")
    private String HomePage() {
        return "hello";
    }

    @GetMapping("/search")
    private String Search() throws NotBoundException, RemoteException, MalformedURLException {
        sm.GoogolSearch(null, null, 0, 0);
        return "hello2";
    }

}
