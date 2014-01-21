package org.jahia.services.usermanager;

/**
 * This bean represents the basic userinfo retrieved by the /me Google service.
 * @author gkuhmel
 */
public class DefaultGooglePlusUser {

	private String id = "";
	private String email = "";
	private boolean verifiedEmail = false;
	private String name = "";
	private String given_name = "";
	private String family_name = "";
	private String link = "";
	private String picture = "";
	private String gender = "";
	private String locale = "";
	
		
	public DefaultGooglePlusUser(String id, String email, boolean verifiedEmail,
			String name, String given_name, String family_name, String link,
			String picture, String gender, String locale) {
		super();
		this.id = id;
		this.email = email;
		this.verifiedEmail = verifiedEmail;
		this.name = name;
		this.given_name = given_name;
		this.family_name = family_name;
		this.link = link;
		this.picture = picture;
		this.gender = gender;
		this.locale = locale;
	}
	
	public DefaultGooglePlusUser() {
		
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public boolean isVerifiedEmail() {
		return verifiedEmail;
	}
	public void setVerifiedEmail(boolean verifiedEmail) {
		this.verifiedEmail = verifiedEmail;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getGiven_name() {
		return given_name;
	}
	public void setGiven_name(String given_name) {
		this.given_name = given_name;
	}
	public String getFamily_name() {
		return family_name;
	}
	public void setFamily_name(String family_name) {
		this.family_name = family_name;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getPicture() {
		return picture;
	}
	public void setPicture(String picture) {
		this.picture = picture;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getLocale() {
		return locale;
	}
	public void setLocale(String locale) {
		this.locale = locale;
	}
	
	
	

}
