package com.battistradadeveloper.firebaseapp;

public class ModelUser {

	//use same name as in firebase database
	String name, email, search, work, image, cover, uid;

	public ModelUser(){

	}

	public ModelUser(String name, String email, String search, String work, String image, String cover, String uid) {
		this.name = name;
		this.email = email;
		this.search = search;
		this.work = work;
		this.image = image;
		this.cover = cover;
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSearch() {
		return search;
	}

	public void setSearch(String search) {
		this.search = search;
	}

	public String getWork() {
		return work;
	}

	public void setWork(String work) {
		this.work = work;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
}
