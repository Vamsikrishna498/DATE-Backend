package com.farmer.Form.DTO;

 
public class EmailServiceDTO {
 
    private String to; // recipient's email address
    private String subject; // email subject
    private String body; // email body/content
 
    // Default constructor (optional but can be useful)
    public EmailServiceDTO() {
    }
 
    // Constructor with all parameters (optional)
    public EmailServiceDTO(String to, String subject, String body) {
        this.to = to;
        this.subject = subject;
        this.body = body;
    }
 
    // Getters and setters (generated automatically if you're using Lombok)
    public String getTo() {
        return to;
    }
 
    public void setTo(String to) {
        this.to = to;
    }
 
    public String getSubject() {
        return subject;
    }
 
    public void setSubject(String subject) {
        this.subject = subject;
    }
 
    public String getBody() {
        return body;
    }
 
    public void setBody(String body) {
        this.body = body;
    }
}
