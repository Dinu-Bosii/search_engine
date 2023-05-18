package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import meta1.SearchModule_I;

@Service
public class adminPageInfo {
    Registry registry;
    private SearchModule_I sm = null;
    AdminObject adminObj = null;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedRate = 1000)
    public void checkAdminInfo() throws NotBoundException, RemoteException, MalformedURLException {
        try {
            registry = LocateRegistry.getRegistry(4040);
            sm = (SearchModule_I) registry.lookup("sm");
        } catch (Exception e) {
            System.out.println("adminPageInfo: Could not connect to Search Module.");
        }
        try {
            AdminObject adminObj2 = sm.AdminInfo();
            if (!adminObj2.equals(adminObj)) {
                adminObj = adminObj2;

                messagingTemplate.convertAndSend("/admin/info", adminObj);
            }
        } catch (Exception e) {
            System.out.println("adminPageInfo: Could not retrieve the administration info.");
        }

    }
}