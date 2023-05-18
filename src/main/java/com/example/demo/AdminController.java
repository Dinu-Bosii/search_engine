package com.example.demo;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.socket.WebSocketSession;

import meta1.SearchModule_I;

@Controller
public class AdminController {

    @Autowired
    public SimpMessageSendingOperations messagingTemplate;

    public AdminController() throws RemoteException {
        super();
    }

    @MessageMapping("/admin")
    @SendTo("/admin/info")
    public AdminObject administration(WebSocketSession session, AdminObject adminObj) {

        return adminObj;
    }

    @GetMapping("/admin")
    public String adminPage(Model model) {
        SearchModule_I sm = null;
        Registry registry;
        AdminObject adminObj = null;
        try {
            registry = LocateRegistry.getRegistry(4040);
            sm = (SearchModule_I) registry.lookup("sm");
            adminObj = sm.AdminInfo();
        } catch (Exception e) {
            System.out.println("Could not connect to Search Module.");
        }
        model.addAttribute("info", adminObj);
        return "administration";
    }
}