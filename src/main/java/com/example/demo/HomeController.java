package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String showRegistrationPage(Model model){
        model.addAttribute("user", new User());
        return "registration";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String processRegistrationPage(@Valid @ModelAttribute("user") User user, BindingResult result, Model model) {
        model.addAttribute("user", user);
        if (result.hasErrors()) {
            return "registration";
        }

        else {
            userService.saveUser(user);
            model.addAttribute("message", "User Account Successfully Created");
        }
        return "index";
    }

    @RequestMapping("/")
    public String index(){
        return "index";
    }

    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping("/secure")
    public String secure(HttpServletRequest request, Authentication authentication, Principal principal){
        Boolean isAdmin = request.isUserInRole("ADMIN");
        Boolean isUser = request.isUserInRole("USER");
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = principal.getName();
        return "secure";
    }

    private User getUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentusername = authentication.getName();
        User user = userRepository.findByUsername(currentusername);
        return user;
    }

    @RequestMapping("/addmessage")
    public String addMessage(Model model){
        String username = getUser().getUsername();
        Message newmessage = new Message();
        newmessage.setUsername(username);
        model.addAttribute("message", newmessage);
        return "messageform";
    }

    @PostMapping("/process")
    public String processMessage(@Valid Message message, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "messageform";
        }
        messageRepository.save(message);
        model.addAttribute("messages", messageRepository.findByUsername(getUser().getUsername()));
        return "messagelist";
    }

    @RequestMapping("/messagelist")
    public String listMessages(Model model){
        model.addAttribute("messages", messageRepository.findByUsername(getUser().getUsername()));
        return "messagelist";
    }

    @RequestMapping("/timeline")
    public String bullMessages(Model model){
        model.addAttribute("messages", messageRepository.findAll());
        return "timeline";
    }


    @RequestMapping("/update/{id}")
    public String showUpdate(@PathVariable("id") long id, Model model){
        model.addAttribute("message", messageRepository.findById(id).get());
        return "messageform";
    }

    @RequestMapping("/detail/{id}")
    public String showMessage(@PathVariable("id") long id, Model model){
        model.addAttribute("message", messageRepository.findById(id).get());
        return "show";

    }

    @RequestMapping("/delete/{id}")
    public String delMessage(@PathVariable("id") long id){
        messageRepository.deleteById(id);
        return "redirect:/messagelist";
    }
}
