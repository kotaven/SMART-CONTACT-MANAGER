package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;
    
    @ModelAttribute
    public void addCommonData(Model model, Principal principal) {
        String userName = principal.getName();
        User user = userRepository.getUserByUserName(userName);
        model.addAttribute("user", user);
    }
    
    @RequestMapping("/index")
    public String dashboard(Model model) {
        model.addAttribute("title", "User Dashboard");
        return "normal/user_dashboard";
    }

    @GetMapping("/add-contact")
    public String openAddContactForm(Model model) {
        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());
        return "normal/add_contact_form";
    }

    @PostMapping("/process-contact")
    public String processContact(
            @ModelAttribute Contact contact,
            @RequestParam("profileImage") MultipartFile file,
            Principal principal,
            Model model,
            HttpSession session) {
        
        try {
            String name = principal.getName();
            User user = this.userRepository.getUserByUserName(name);
            
            if (file.isEmpty()) {
                contact.setImage("contact.jpg");
                session.setAttribute("message", new Message("Please select a valid image to upload.", "danger"));
            } else {
                // Handle uploaded file
                contact.setImage(file.getOriginalFilename());
                File saveFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Image is uploaded successfully.");
            }

            contact.setUser(user);
            user.getContacts().add(contact);
            this.userRepository.save(user);
            System.out.println("Contact added to the database: " + contact);
            
            session.setAttribute("message", new Message("Your contact is added! Add more..", "success"));
            
        } catch (Exception e) {
            System.out.println("Error while adding contact: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("message", new Message("Some went wrong! Try again..", "danger"));
        }

        return "normal/add_contact_form";
    }
    @GetMapping("/show-contacts/{page}")
    public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {
        m.addAttribute("title", "Show User Contacts");
        
        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);
        
        int pageSize = 8;
        List<Contact> allContacts = this.contactRepository.findContactsByUser(user.getId());
        long totalContacts = this.contactRepository.countByUser(user.getId());
        int totalPages = (int) Math.ceil((double) totalContacts / pageSize);

       
        int start = page * pageSize;
        int end = Math.min(start + pageSize, allContacts.size());

       
        List<Contact> contacts = allContacts.subList(start, end);

        m.addAttribute("contacts", contacts);
        m.addAttribute("currentPage", page);
        m.addAttribute("totalPages", totalPages);
        
        return "normal/show_contacts";
    }
    
    //showing particular contact details.
    
    @RequestMapping("/{cId}/contact")
    public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal principal) {
    	
    	System.out.println("CID "+cId);
    	
    	Optional<Contact> contactOption= this.contactRepository.findById(cId);
    	Contact contact=contactOption.get();
    	
    	//
    	String userName=principal.getName();
    	User user=this.userRepository.getUserByUserName(userName);
    	
    	if(user.getId()==contact.getUser().getId()) {
    		model.addAttribute("contact",contact);
    		model.addAttribute("title",contact.getName());
    	}
    	
		return "normal/contact_detail";	
    	
    }
    
    // delete contact handler
    
    @GetMapping("/delete/{cid}")
    @Transactional
    public String deleteContact(@PathVariable("cid") Integer cId,Model model,HttpSession session,Principal principal) {
    	
    	System.out.println("CID "+cId);
    	
    	Contact contact=this.contactRepository.findById(cId).get();
    	
    	// check... Assignment..Image delete
    	
    	//delete old photo
    	    	
    	User user=this.userRepository.getUserByUserName(principal.getName());
    	user.getContacts().remove(contact);
    	this.userRepository.save(user);
    	
    	System.out.println("DELETE");
    	session.setAttribute("message", new Message("contact delete succesfully", "succes"));
    	
    	
		return "redirect:/user/show-contacts/0";
    	
    }
    
    
    // open update form handler
    
    @PostMapping("/update-contact/{cid}")
    
    public String updateForm(@PathVariable("cid") Integer cid ,Model m) {
    	
    	m.addAttribute("title","Update Contact");
    	
    	Contact contact=this.contactRepository.findById(cid).get();
    	
    	m.addAttribute("contact",contact);
    	
		return "normal/update_form";
	}
    
    // update contact handler
    
    @RequestMapping(value = "/process-update",method=RequestMethod.POST)
    public String updateHandler(@ModelAttribute Contact contact,
    		@RequestParam("profileImage") MultipartFile file,Model m,
    		HttpSession session,Principal principal) {
    	
    	try {
    		
    		// old contact detalis
    		
    		Contact oldcontactDetail=this.contactRepository.findById(contact.getcId()).get();
    		
    		// image..
    		if(!file.isEmpty()) {
    			
    			// file work
    			// rewrite
    			
    			// delete old photo
    			
    			File deleteFile=new ClassPathResource("static/img").getFile();
    			File file1=new File(deleteFile,oldcontactDetail.getImage());
    			file1.delete();
    			
    			// update new photo
    			
                File saveFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                contact.setImage(file.getOriginalFilename());
    			
    		}else {
				contact.setImage(oldcontactDetail.getImage());
			}
    			
    			User user=this.userRepository.getUserByUserName(principal.getName());
    			contact.setUser(user);
    			this.contactRepository.save(contact);
    			
    			session.setAttribute("message", new Message("your contact is update...","success"));
    		
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	System.out.println("CONTACT NAME "+ contact.getName());
    	System.out.println("CONTACT ID "+ contact.getcId());
    	return "redirect:/user/"+contact.getcId()+"/contact";
    }
    
    // your profile handler
    
    @GetMapping("/profile")
    public String yourProfile(Model model) {
    	model.addAttribute("title","profile page");
		return "normal/profile";
    	
    }
    
    // open setting handler
    
    @GetMapping("/settings")
    public String openSettings() {
    	return "normal/settings";
    }
    
    // change password..handler
    
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,Principal principal,HttpSession session) {
		System.out.println("OLD PASSWORD" +oldPassword);
		System.out.println("NEW PASSWORD" +newPassword);
		
		String userName= principal.getName();
		User currentUser= this.userRepository.getUserByUserName(userName);
		System.out.println(currentUser.getPassword());
		
		if(this.bCryptPasswordEncoder.matches(oldPassword,currentUser.getPassword())) {
			
			// change the password
			
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			
			this.userRepository.save(currentUser);
			session.setAttribute("message",new Message("your password is successFully changed..","success"));
			
		}else {
			
			// error
			
			session.setAttribute("message",new Message("Please Enter correct old password !!","danger"));
			
	    	return "redirect:/user/settings";

			
		}
    	
    	return "redirect:/user/index";
    	
    }
    
}
