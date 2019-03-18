package com.basic;

import java.io.Serializable;
import java.net.InetAddress;

public class User implements Serializable {
	private static final long serialVersionUID = 2755926491719480265L;
	private String userId;	//用户ID
	private String passwd;	//密码
	private String unload;	//下线
	private String registe;	//注册
	private InetAddress address;	//IP地址
	public InetAddress getAddress() {
		return address;
	}
	public void setAddress(InetAddress address) {
		this.address = address;
	}
	public String getRegiste() {
		return registe;
	}
	public void setRegiste(String registe) {
		this.registe = registe;
	}
	public String getUnload() {
		return unload;
	}
	public void setUnload(String unload) {
		this.unload = unload;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getPasswd() {
		return passwd;
	}
	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
}