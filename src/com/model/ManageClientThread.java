package com.model;
/**
 * 管理服务端与用户端通信线程类
 */
import java.net.InetAddress;
import java.util.*;
public class ManageClientThread {
	
	public static HashMap<String, ServerConClientThread> hm=new HashMap<String, ServerConClientThread>();
	public static HashMap<String, InetAddress> hm2=new HashMap<String, InetAddress>();
	//向哈希表中添加一个客户端通讯线程
	public static void addClientThread(String uid,ServerConClientThread ct)
	{
		hm.put(uid, ct);
	}
	//通过键值获得哈希表中的一个客户端通讯线程
	public static ServerConClientThread getClientThread(String uid)
	{
		return (ServerConClientThread)hm.get(uid);
	}
	//通过键值从哈希表中删除一个客户端线程
	public static void RemoveClientThread(String uid)
	{
		hm.remove(uid);
	}
	//第二个哈希表存储IP地址
	public static void addClientip(String uid,InetAddress ip)
	{
		hm2.put(uid, ip);
	}
	public static InetAddress getClientip(String uid)
	{
		return (InetAddress)hm2.get(uid);
	}
	public static void RemoveClientip(String uid)
	{
		hm2.remove(uid);
	}
	//返回当前在线线程的id
	public static String getAllOnLineUserid()
	{
		//使用迭代器遍历
		Iterator<String> it=hm.keySet().iterator();
		String res="";
		while(it.hasNext())
		{
			res+=it.next().toString()+" ";
		}
		return res;
	}
}
