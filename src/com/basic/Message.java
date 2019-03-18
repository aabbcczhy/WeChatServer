package com.basic;

import java.io.Serializable;
import java.net.InetAddress;

public class Message implements Serializable{
	private static final long serialVersionUID = 1883251098494136011L;
	private String mesType;		//信息类型，详见MessageType接口

	private String sender;		//发送方
	private String getter;		//接收方
	private String con;			//消息1
	private String sendTime;	//发送时间
	private String fenzucount;	//分组数
	private String con1;		//消息2
	private String font;		//字体
	private int size;			//大小
	private String col;			//字体颜色
	private String file;		//文件类型
	private String filename;	//文件名
    private byte[] by;			//文件内容
    
    InetAddress address;		//IP地址
    private int myport;			//发送方端口号
    private int friendport;		//接收方端口号
    
    private String myname;		//发送方主机名
    private String friname;		//接收方主机名
    
	public String getMyname() {
		return myname;
	}

	public void setMyname(String myname) {
		this.myname = myname;
	}

	public String getFriname() {
		return friname;
	}

	public void setFriname(String friname) {
		this.friname = friname;
	}
    
	public int getMyport() {
		return myport;
	}

	public void setMyport(int myport) {
		this.myport = myport;
	}

	public int getFriendport() {
		return friendport;
	}

	public void setFriendport(int friendport) {
		this.friendport = friendport;
	}
  	public InetAddress getAddress() {
  		return address;
  	}

  	public void setAddress(InetAddress address) {
  		this.address = address;
  	}
    
	public byte[] getFilebyte() {
		return by;
	}

	public void setFilebyte(byte[] b,int n) {
		
		by=new byte[n];
		by=b;
	}
	
	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getFont() {
		return font;
	}

	public void setFont(String font) {
		this.font = font;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getCol() {
		return col;
	}

	public void setCol(String col) {
		this.col = col;
	}

	public String getCon1() {
		return con1;
	}

	public void setCon1(String con1) {
		this.con1 = con1;
	}

	public String getFenzucount() {
		return fenzucount;
	}

	public void setFenzucount(String fenzucount) {
		this.fenzucount = fenzucount;
	}
	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getGetter() {
		return getter;
	}

	public void setGetter(String getter) {
		this.getter = getter;
	}

	public String getCon() {
		return con;
	}

	public void setCon(String con) {
		this.con = con;
	}

	public String getSendTime() {
		return sendTime;
	}

	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
	}

	public String getMesType() {
		return mesType;
	}

	public void setMesType(String mesType) {
		this.mesType = mesType;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
}
