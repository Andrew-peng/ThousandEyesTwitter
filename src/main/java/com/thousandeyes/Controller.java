package com.thousandeyes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/")
public class Controller {
    
    @Autowired
    UserDAO userDAO;
    
    @RequestMapping(value = "followers", method = RequestMethod.GET)
    public @ResponseBody Map followers() {
        User logged_in = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        BaseUser user = userDAO.getUserInfo(logged_in.getUsername());
        Map follow = new HashMap<String, List>();
        follow.put("followers", user.getFollowers());
        follow.put("following", user.getFollowing());
        return follow;
    }
    @RequestMapping(value = "followers/follow", method = RequestMethod.POST, consumes = {"application/json"})
    public @ResponseBody Map follow(@RequestBody Person person) {
        System.out.println(person.getName());
        //POST DATA is json that includes id and name or just name
        User logged_in = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Map follow = new HashMap<String, List>();
        BaseUser user = userDAO.getUserInfo(logged_in.getUsername());
        if (user.getFollowing().contains(person.getName())) {
            follow.put("followers", user.getFollowers());
            follow.put("following", user.getFollowing());
            return follow;
        } else {
            //ASSUME NO ID 0 PERSON
            if (person.getId() > 0) {
                userDAO.follow(person.getId(), userDAO.getId(logged_in.getUsername()));
            } else {
                userDAO.follow(userDAO.getId(person.getName()), userDAO.getId(logged_in.getUsername()));
            }
            user = userDAO.getUserInfo(logged_in.getUsername());
            follow.put("followers", user.getFollowers());
            follow.put("following", user.getFollowing());
            return follow;  
        }
        
    }
    @RequestMapping(value = "followers/unfollow", method = RequestMethod.POST, consumes = {"application/json"})
    public @ResponseBody Map unfollow(@RequestBody Person person) {
        //POST DATA is json includes id and name or just name
        User logged_in = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //ASSUME NO ID 0 PERSON
        if (person.getId() > 0) {
            userDAO.unfollow(person.getId(), userDAO.getId(logged_in.getUsername()));
        } else {
            userDAO.unfollow(userDAO.getId(person.getName()), userDAO.getId(logged_in.getUsername()));
        }
        BaseUser user = userDAO.getUserInfo(logged_in.getUsername());
        Map follow = new HashMap<String, List>();
        follow.put("followers", user.getFollowers());
        follow.put("following", user.getFollowing());
        return follow;
    }
    @RequestMapping(value = "messages", method = RequestMethod.GET)
    public @ResponseBody Map messages(@RequestParam(value="search", defaultValue="") String search) {
        User logged_in = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        BaseUser user = userDAO.getUserInfo(logged_in.getUsername());
        Map messages = new HashMap<String, List>();
        messages.put("messages", new ArrayList<String>());
        List<String> list_of_messages = user.getMessages();
        for(String s : list_of_messages) {
            if(s.toLowerCase().contains(search.toLowerCase())) {
                ((List) messages.get("messages")).add(s);
            }
        }
        return messages;
    }
    @RequestMapping(value = "path", method = RequestMethod.GET)
    public @ResponseBody Map shortestPath(@RequestParam(value="target", defaultValue="") String target) {
        User logged_in = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        HashMap<String, Object> map = new HashMap<String, Object>();
        int pathLen = -1;
        map.put("length", pathLen);
        //No target case
        if(target.equals("")) {
            return map;
        }
        //Use BFS to find shortest path
        HashSet<String> set = new HashSet<String>();
        LinkedList<String> queue = new LinkedList<String>();
        LinkedList<String> path = new LinkedList<String>();
        HashMap<String, String> backtrack = new HashMap<String, String>();
        queue.addLast(logged_in.getUsername());
        backtrack.put(logged_in.getUsername(), "end");
        while(!queue.isEmpty()) {
            String curr = queue.removeFirst();
            if(curr.equals(target)) {
                pathLen = 0;
                while(!backtrack.get(curr).equals("end")) {
                    pathLen++;
                    path.addFirst(curr);
                    curr = backtrack.get(curr);
                }
                path.addFirst(logged_in.getUsername());
                map.put("length", pathLen);
                map.put("path", path);
                return map;
            }
            if(!set.contains(curr)) {
                set.add(curr);
                for(String following : userDAO.getUserInfo(curr).getFollowing()) {
                    backtrack.putIfAbsent(following, curr);
                    queue.add(following);
                }
            }
        }
        return map;
    }
    private static class Person {
        private int id;
        private String name;
        public int getId() {
            return id;
        }
        public String getName() {
            return name;
        }
        public void setId(int _id) {
            this.id = _id;
        }
        public void setName(String _name) {
            this.name = _name;
        }
    }
}
