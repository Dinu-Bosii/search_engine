package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public String IndexUrlPage() {
        return "indexUrlPage";
    }

    @GetMapping("/search/{page}")
    public String Search(@RequestParam(defaultValue = "hello, World") String text,
            @PathVariable("page") String page, Model model)
            throws NotBoundException, RemoteException, MalformedURLException {
        System.out.println("Searching for page " + page + " of: " + text);
        Registry registry;

        try {
            registry = LocateRegistry.getRegistry(4040);
            sm = (SearchModule_I) registry.lookup("sm");
        } catch (Exception e) {
            System.out.println("Search: Could not connect to Search Module.");
            return "redirect:/";
        }

        try {
            List<indexObject> results = sm.GoogolSearch(text, Integer.parseInt(page));
            model.addAttribute("urls", results);
            model.addAttribute("currentPage", page);
            model.addAttribute("query", text);
            model.addAttribute("NextPage", Integer.parseInt(page) + 1);
            model.addAttribute("PrevPage", Integer.parseInt(page) - 1);
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            return "redirect:/";
        }
        return "search";
    }

    @GetMapping("/indexUrl")
    @ResponseBody
    public Map<String, Object> IndexUrl(@RequestParam(required = true) String url, Model model)
            throws NotBoundException, RemoteException, MalformedURLException {
        Map<String, Object> response = new HashMap<>();
        Registry registry;

        try {
            registry = LocateRegistry.getRegistry(4040);
            sm = (SearchModule_I) registry.lookup("sm");
        } catch (Exception e) {
            System.out.println("Could not connect to Search Module.");
        }
        System.out.println("Connected to Search Module succesfully.");
        boolean success = false;
        try {
            success = sm.indexURL(url);
            response.put("success", success);
            System.out.println(success);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error indexing the url.");
            response.put("success", false);
        }
        model.addAttribute("success", success);
        return response;
    }

    @GetMapping("/urlLinks")
    public String UrlLinks(@RequestParam(required = true) String url, Model model)
            throws NotBoundException, RemoteException, MalformedURLException {
        Registry registry;

        try {
            registry = LocateRegistry.getRegistry(4040);
            sm = (SearchModule_I) registry.lookup("sm");
        } catch (Exception e) {
            System.out.println("Could not connect to Search Module.");
            model.addAttribute("url", url);
            model.addAttribute("linkedUrls", new ArrayList<>());
            return "urlLinks";
        }
        System.out.println("Connected to Search Module succesfully.");
        ArrayList<String> links = new ArrayList<>();
        try {
            links = sm.links(url);
        } catch (Exception e) {
            System.out.println("Error retrieving links that point to: " + url);
        }
        model.addAttribute("url", url);
        model.addAttribute("linkedUrls", links);
        return "urlLinks";
    }

    @GetMapping("/hackerNewsUser")
    public String hackerNewsUser() {
        return "hackerNewsUser";
    }

    @GetMapping("/hackerNewsUserSearch")
    public String userStories(@RequestParam(required = true) String user, Model model) {
        ArrayList<String> storyUrls = new ArrayList<>();
        Registry registry;
        System.out.println("searching for user: " + user);
        try {
            registry = LocateRegistry.getRegistry(4040);
            sm = (SearchModule_I) registry.lookup("sm");
        } catch (Exception e) {
            System.out.println("Could not connect to Search Module.");
            model.addAttribute("user", user);
            model.addAttribute("storyUrls", 0);
            return "hackerNewsUserSearch";
        }
        System.out.println("Connected to Search Module succesfully.");
        try {
            RestTemplate restTemplate = new RestTemplate();
            String userEndpoint = "https://hacker-news.firebaseio.com/v0/user/" + user + ".json?print=pretty";
            HackerNewsUserRecord hackerNewsUserRecord = restTemplate.getForObject(userEndpoint,
                    HackerNewsUserRecord.class);
            if (hackerNewsUserRecord == null) {
                return "hackerNewsUserSearch";
            }
            boolean success = false;
            List<Integer> storyIds = hackerNewsUserRecord.getSubmitted();
            for (Integer id : storyIds) {
                String storyEndpoint = "https://hacker-news.firebaseio.com/v0/item/" + id + ".json";
                HackerNewsItemRecord hackerNewsItemRecord = restTemplate.getForObject(storyEndpoint,
                        HackerNewsItemRecord.class);
                if (hackerNewsItemRecord == null) {
                    continue;
                }
                if (hackerNewsItemRecord.url() != null) {
                    try {
                        success = sm.indexURL(hackerNewsItemRecord.url());
                    } catch (Exception e) {
                        System.out.println("Error indexing the url.");
                    }
                    if (success) {
                        storyUrls.add(hackerNewsItemRecord.url());
                        System.out.println("URL indexed successfully: " + hackerNewsItemRecord.url());
                    } else {
                        System.out.println("Failed to index the URL: " + hackerNewsItemRecord.url());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error retrieving stories by: " + user);
        }
        // model.addAttribute("storyUrls", storyUrls);
        model.addAttribute("user", user);
        model.addAttribute("storyUrls", storyUrls.size());
        return "hackerNewsUserSearch";
    }

    @GetMapping("/indexTopStories")
    public String indexTopStories(@RequestParam(required = true) String query, Model model) {
        System.out.println("indexTopStories for: " + query);
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(4040);
            sm = (SearchModule_I) registry.lookup("sm");
        } catch (Exception e) {
            System.out.println("indexTopStories: Could not connect to Search Module.");
        }
        System.out.println("indexTopStories: Connected to Search Module succesfully.");

        int count = 0;
        ArrayList<String> urlsToIndex = new ArrayList<>();
        try {
            RestTemplate restTemplate = new RestTemplate();
            String topStoriesEndpoint = "https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty";
            List hackerNewsNewTopStories = restTemplate.getForObject(topStoriesEndpoint, List.class);

            assert hackerNewsNewTopStories != null;
            System.out.println("indexTopStories: Stories retrieved.");
            for (int i = 0; i <= 400; i++) {
                Integer storyId = (Integer) hackerNewsNewTopStories.get(i);

                String storyItemDetailsEndpoint = String
                        .format("https://hacker-news.firebaseio.com/v0/item/%s.json?print=pretty", storyId);
                HackerNewsItemRecord hackerNewsItemRecord = restTemplate.getForObject(storyItemDetailsEndpoint,
                        HackerNewsItemRecord.class);

                if (hackerNewsItemRecord == null ||
                        hackerNewsItemRecord.text() == null ||
                        hackerNewsItemRecord.url() == null) {
                    continue;
                }
                if (query != null) {
                    List<String> searchTermsList = List.of(query.toLowerCase().split(" ")); // termos de pesquisa
                    // verifica se contem os termos no texto da story
                    if (searchTermsList.stream().anyMatch(hackerNewsItemRecord.text().toLowerCase()::contains)) {
                        urlsToIndex.add(hackerNewsItemRecord.url());
                    }
                }
            }
            System.out.println("indexTopStories: potentially indexing for " + query + ": " +
                    urlsToIndex.size() + " urls");
            for (String url : urlsToIndex) {
                System.out.println("indexing: " + url);
                boolean success = false;
                try {
                    success = sm.indexURL(url);
                } catch (Exception e) {
                    System.out.println("Error trying to index the URL");
                }
                if (success) {
                    System.out.println("URL indexed successfully: " + url);
                    count++;
                } else {
                    System.out.println("Failed to index the URL: " + url);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error retrieving top stories with: " + query);
        }
        model.addAttribute("query", query);
        model.addAttribute("storyUrls", count);
        return "hackerNewsUserSearch2";
    }

}
