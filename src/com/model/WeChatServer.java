package com.model;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.util.Date;

import com.basic.Message;
import com.basic.MessageType;
import com.basic.User;
import com.model.ManageClientThread;
import com.model.ServerConClientThread;

public class WeChatServer implements Runnable{
	LinkMySQL link = new LinkMySQL();
	private ResultSet rs=null;
	DateFormat ddtf = DateFormat.getDateTimeInstance();
	public static void main(String[] args) {
		new WeChatServer();
	}
	public WeChatServer() {
		System.out.println(ddtf.format(new Date())+" 服务器启动......");
		Thread server = new Thread(this);
		server.start();
	}
	@SuppressWarnings({ "resource", "deprecation" })
	@Override
	public void run() {
		try {
			ServerSocket ss = new ServerSocket(8888);	//监听8888端口
			while(true) {
				//阻塞状态，等待客户端连接
				Socket client = ss.accept();
				
				/**
				 * 处理信息前的准备
				 * 1.通过输入流获得用户信息
				 * 2.实例化一个Message对象用来处理信息
				 * 3.连接数据库获得处理结果
				 * 4.通过输出流向客户端返回处理信息后的结果
				 */
				
				ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
				User u = (User)ois.readObject();
				Message m = new Message();
				ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
				String sql = "select * from Login where UserId='" + u.getUserId() + "'and PassWord='" + u.getPasswd() + "'";
				rs = link.doSelect(sql);
				
				//服务端开始处理信息
				if(!u.getRegiste().equals("1")) {	//用户注册开始
					String Registemessage[] = u.getRegiste().split(" ");
					String sql1 = " insert into Login(name,PassWord,sex,brithday) values('"
							+ Registemessage[0]
							+ "','"
							+ Registemessage[1]
							+ "','"
							+ Registemessage[2]
							+ "','"
							+ Registemessage[3] + "')";
					link.Insert(sql1);
					sql1 = "select * from Login";
					ResultSet rs1 = link.doSelect(sql1);
					rs1.last();
					String ID = rs1.getString("UserId");
					m.setMesType(MessageType.message_get_onLineFriend);
					m.setCon(ID);
					oos.writeObject(m);
					String sql_insert = "insert into friendlist(fqq,mqq,fenzu,fenzucount,name) values('"
							+ ID + "','" + ID + "','好友','1','" + Registemessage[0] + "')";
					link.Insert(sql_insert);
				} else if (u.getUnload().equals("1234567")) {	//用户下线开始
					ServerConClientThread scct = ManageClientThread.getClientThread(u.getUserId());
					sql = "select * from Outline where UserId='" + u.getUserId() + "'";
					rs = link.doSelect(sql);
					if (rs.next()) {
						sql = "Update Outline set OutlineTime=now() where UserId='" + u.getUserId() + "'";
						link.Insert(sql);
					} else {
						sql = "insert into Outline (UserId) values('" + u.getUserId() + "')";
						link.Insert(sql);
					}														//更新数据库用户离线时间
					ManageClientThread.RemoveClientThread(u.getUserId());	//移除哈希表中该用户线程
					ManageClientThread.RemoveClientip(u.getUserId());	//移除哈希表中存储的用户IP
					scct.notifyunloadOther(u.getUserId());			//通知所有在线线程下线消息
					scct.stop();							//终止该用户线程
				}
				else if (rs.next()) {		//登录成功开始
					if (ManageClientThread.getClientThread(u.getUserId()) != null) {	//用户已经登录
						m.setMesType(MessageType.message_comm_mes);
						oos.writeObject(m);
					} else {
						// 未登录则返回一个成功登陆的信息报文
						m.setMesType(MessageType.message_login_succeed);
						String con = "";
						String search_friend = "select * from friendlist where mqq='" + u.getUserId() + "'";
						rs = link.doSelect(search_friend);
						try {
							while (rs.next()) {
								if (rs.getString("fqq").equals(u.getUserId())) {
									m.setMyname(rs.getString("name") + "(" + u.getUserId() + ")");
								}
								con = con + rs.getString("name") + "(" + rs.getString("fqq") + ")-" + rs.getString("fenzu") + " ";
								m.setFenzucount(rs.getString("fenzucount"));
							}
							m.setCon(con);
						} catch (Exception e) {
							e.printStackTrace();
						}
						oos.writeObject(m);
						// 单开一个线程，让该线程与该客户端保持通讯
						ServerConClientThread scct = new ServerConClientThread(client);
						ManageClientThread.addClientThread(u.getUserId(), scct);

						InetAddress address = u.getAddress();
						ManageClientThread.addClientip(u.getUserId(), address);
						// 启动与该客户端通信的线程
						scct.start();
						// 通知所有在线线程上线消息
						scct.notifyOther(u.getUserId());
					}
				} else {	//登录失败开始
					m.setMesType(MessageType.message_login_fail);
					oos.writeObject(m);
					// 关闭Socket
					client.close();
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
