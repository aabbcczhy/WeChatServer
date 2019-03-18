/**
  * 服务器和某个客户端的通信线程
 */
package com.model;

import java.sql.ResultSet;
import java.util.*;
import java.net.*;
import java.io.*;
import com.basic.*;

public class ServerConClientThread extends Thread {
	public Socket s;
	ResultSet rs = null, rs2 = null;
	LinkMySQL link = new LinkMySQL();
	public ServerConClientThread(Socket s) {
		this.s = s;
	}
	public void notifyOther(String iam) {		//通知上线消息
		// 得到所有在线的人的线程
		HashMap<String, ServerConClientThread> hm = ManageClientThread.hm;
		Iterator<String> it = hm.keySet().iterator();
		while (it.hasNext()) {
			Message m = new Message();
			m.setCon(iam);
			m.setMesType(MessageType.message_ret_onLineFriend);
			// 获得所有在线用户的id
			String onLineUserId = it.next().toString();
			try {		//向所有在线好友发送该用户上线消息
				ObjectOutputStream oos = new ObjectOutputStream(ManageClientThread.getClientThread(onLineUserId).s.getOutputStream());
				m.setGetter(onLineUserId);
				oos.writeObject(m);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public void notifyunloadOther(String iam) {		//通知下线消息
		// 得到所有在线的人的线程
		HashMap<String, ServerConClientThread> hm = ManageClientThread.hm;
		Iterator<String> it = hm.keySet().iterator();
		while (it.hasNext()) {
			Message m = new Message();
			m.setCon(iam);
			m.setMesType(MessageType.messgae_ret_unloadFriends);
			// 取出在线人的id
			String onLineUserId = it.next().toString();
			try {		//向所有在线好友发送该用户离线消息
				ObjectOutputStream oos = new ObjectOutputStream(ManageClientThread.getClientThread(onLineUserId).s.getOutputStream());
				m.setGetter(onLineUserId);
				oos.writeObject(m);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public void run() {
		while (true) {
			// 该线程不停地接收客户端的信息
			try {
				ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
				Message m = (Message) ois.readObject();
				// 对从客户端取得的消息进行类型判断，然后做相应的处理
				if (m.getMesType().equals(MessageType.message_comm_mes)) {	//普通信息包
					// 当信息不为空时
					if (m.getCon() != null) {	//先把本条聊天记录保存到数据库
						String sql = " insert into ChatContent(Sender,Content,Receiver,Times,font,size,color) values('"
								+ m.getSender()
								+ "','"
								+ m.getCon()
								+ "','"
								+ m.getGetter()
								+ "','"
								+ m.getSendTime()
								+ "','"
								+ m.getFont()
								+ "','"
								+ m.getSize()
								+ "','" + m.getCol() + "')";
						link.Insert(sql);
						// 取得接收人的通信线程，并发送
						ServerConClientThread sc = ManageClientThread.getClientThread(m.getGetter());
						if(sc!=null) {		//接收方在线就转发给接收方
							ObjectOutputStream oos = new ObjectOutputStream(sc.s.getOutputStream());
							oos.writeObject(m);
						}
					}
				} else if (m.getMesType().equals(MessageType.message_shake)) {
					// 服务器只负责将这条震动消息中转
					ServerConClientThread sc = ManageClientThread.getClientThread(m.getGetter());
					if(sc!=null) {
						ObjectOutputStream oos = new ObjectOutputStream(sc.s.getOutputStream());
						oos.writeObject(m);
					}
				} else if (m.getMesType().equals(MessageType.message_get_onLineFriend)) {
					// 把在服务器的好友给该客户端返回.
					String res = ManageClientThread.getAllOnLineUserid();	//获得所有在线用户的id
					Message m2 = new Message();
					m2.setMesType(MessageType.message_ret_onLineFriend);	//返回在线好友信息包
					m2.setCon(res);
					m2.setGetter(m.getSender());
					ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
					oos.writeObject(m2);
				} else if (m.getMesType().equals(MessageType.message_ret_addFriend)) {
					// 查询好友是否存在
					String sql = "select * from Login where UserId='" + m.getGetter() + "'";
					rs = link.doSelect(sql);	
					if (rs.next()) {		//好友存在
						Message m2 = new Message();
						m2.setMesType(MessageType.message_addpoint);	//返回添加好友报文
						m2.setCon(m.getCon());
						m2.setGetter(m.getGetter());
						m2.setSender(m.getSender());
						sql = "select * from Login where UserId='"+ m.getSender() + "'";
						rs = link.doSelect(sql);
						if (rs.next())
							m2.setFriname(rs.getString("name") + "(" + m.getSender() + ")");
						ServerConClientThread sc = ManageClientThread.getClientThread(m.getGetter());
						if(sc==null) {		//被请求的好友不在线，好友请求暂存数据库
							sql = " insert into AddFriend(UserId,FriendId,PS) values ('" + m.getSender() + "','"
									+ m.getGetter() + "','" + m.getCon() + "')";
							link.Insert(sql);
						}else {
							ObjectOutputStream oos = new ObjectOutputStream(sc.s.getOutputStream());
							oos.writeObject(m2);
						}
					} else {				//好友不存在
						Message m2 = new Message();
						m2.setMesType(MessageType.message_ret_addFriend);	//返回好友不存在报文
						m2.setCon(null);
						ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
						oos.writeObject(m2);
					}
				} else if (m.getMesType().equals(MessageType.message_acceptadd)) {
					// 同意好友请求
					String sql = "select * from Login where UserId='" + m.getGetter() + "'";
					String sql_insert;
					rs = link.doSelect(sql);
					// 往双方好友列表里添加对方
					if (rs.next()) {
						sql_insert = "insert into friendlist values('" + m.getGetter() + "','" + m.getSender()
										+ "','同学','2','" + rs.getString("name") + "')";
						link.Insert(sql_insert);
					}
					sql = "select * from Login where UserId='" + m.getSender() + "'";
					rs = link.doSelect(sql);
					if (rs.next()) {
						sql_insert = "insert into friendlist values('" + m.getSender() + "','" + m.getGetter()
										+ "','同学','2','" + rs.getString("name") + "')";
						link.Insert(sql_insert);
					}
					sql_insert = "update friendlist set fenzucount = '2' where mqq = '" + m.getGetter() + "'";
					link.Insert(sql_insert);
					sql_insert = "update friendlist set fenzucount = '2' where mqq = '" + m.getSender() + "'";
					link.Insert(sql_insert);
					link.Insert("delete from AddFriend where FriendId='" + m.getSender() + "'");	//数据库删掉添加好友请求记录（如果有）
					Message m2 = new Message();
					m2.setMesType(MessageType.message_acceptadd);
					m2.setGetter(m.getGetter());
					m2.setSender(m.getSender());
					m2.setFriname(rs.getString("name") + "(" + m.getSender() + ")");
					ServerConClientThread sc = ManageClientThread.getClientThread(m.getGetter());
					ObjectOutputStream oos = new ObjectOutputStream(sc.s.getOutputStream());
					oos.writeObject(m2);
				}
				else if (m.getMesType().equals(MessageType.message_jujueadd)) {
					// 拒绝好友请求
					link.Insert("delete from AddFriend where FriendId='" + m.getSender() + "'");
					Message m2 = new Message();
					m2.setMesType(MessageType.message_jujueadd);
					m2.setGetter(m.getGetter());
					m2.setSender(m.getSender());
					ServerConClientThread sc = ManageClientThread.getClientThread(m.getGetter());
					ObjectOutputStream oos = new ObjectOutputStream(sc.s.getOutputStream());
					oos.writeObject(m2);
				} else if (m.getMesType().equals(MessageType.message_ret_oldMessage)) {
					// 返回消息记录
					String sql = "select * from ChatContent where Sender='"
							+ m.getSender() + "'and Receiver='" + m.getGetter()
							+ "'or Sender='" + m.getGetter()
							+ "'and Receiver='" + m.getSender() + "'";
					rs = link.doSelect(sql);
					int count = 0;
					while (rs.next()) {
						count++;
					}
					if (count != 0) {
						rs.first();
						m.setFont(rs.getString("font"));
						m.setSendTime(rs.getString("Times"));
						m.setSize(rs.getInt("size"));
						m.setCol(rs.getString("color"));
						m.setCon(rs.getString(4));
						m.setFriname(rs.getString("Sender"));
						ServerConClientThread sc = ManageClientThread.getClientThread(m.getSender());
						ObjectOutputStream oos = new ObjectOutputStream(sc.s.getOutputStream());
						oos.writeObject(m);
						while (rs.next()) {		//双方消息记录逐条返回给客户端
							m.setFont(rs.getString("font"));
							m.setSendTime(rs.getString("Times"));
							m.setSize(rs.getInt("size"));
							m.setCol(rs.getString("color"));
							m.setCon(rs.getString(4));
							m.setFriname(rs.getString("Sender"));
							sc = ManageClientThread.getClientThread(m.getSender());
							oos = new ObjectOutputStream(sc.s.getOutputStream());
							oos.writeObject(m);
						}
					}
				} else if (m.getMesType().equals(MessageType.message_ret_outlineMessage)) {
					// 返回离线记录
					String sql = "select * from ChatContent where Times>(select OutlineTime from Outline where UserId='"
							+ m.getSender()
							+ "') and Receiver='"
							+ m.getSender()
							+ "' and Sender='"
							+ m.getGetter()
							+ "'";
					rs = link.doSelect(sql);
					int count = 0;
					while (rs.next()) {
						count++;
					}
					if (count != 0) {
						rs.first();
						m.setFont(rs.getString("font"));
						m.setSendTime(rs.getString("Times"));
						m.setSize(rs.getInt("size"));
						m.setCol(rs.getString("color"));
						m.setCon(rs.getString(4));
						ServerConClientThread sc = ManageClientThread.getClientThread(m.getSender());
						ObjectOutputStream oos = new ObjectOutputStream(sc.s.getOutputStream());
						oos.writeObject(m);
						while (rs.next()) {		//离线消息逐条返回给客户端
							m.setFont(rs.getString("font"));
							m.setSendTime(rs.getString("Times"));
							m.setSize(rs.getInt("size"));
							m.setCol(rs.getString("color"));
							m.setCon(rs.getString(4));
							sc = ManageClientThread.getClientThread(m.getSender());
							oos = new ObjectOutputStream(sc.s.getOutputStream());
							oos.writeObject(m);
						}
					}
				} else if (m.getMesType().equals(MessageType.message_ret_pointMessage)) {
					// 返回提示有离线消息
					String sql = "select * from ChatContent where Times>(select OutlineTime from Outline where UserId='"
							+ m.getSender() + "') and Receiver='" + m.getSender() + "'";
					rs = link.doSelect(sql);
					int count = 0;
					while (rs.next()) {
						count++;
					}
					if (count != 0) {
						rs.first();
						m.setFont(rs.getString("font"));
						m.setSendTime(rs.getString("Times"));
						m.setSize(rs.getInt("size"));
						m.setCol(rs.getString("color"));
						m.setCon(rs.getString(4));
						m.setGetter(rs.getString(1));
						ServerConClientThread sc = ManageClientThread.getClientThread(m.getSender());
						ObjectOutputStream oos = new ObjectOutputStream(sc.s.getOutputStream());
						oos.writeObject(m);
						while (rs.next()) {		//离线消息逐条返回给客户端
							m.setGetter(rs.getString(1));
							m.setFont(rs.getString("font"));
							m.setSendTime(rs.getString("Times"));
							m.setSize(rs.getInt("size"));
							m.setCol(rs.getString("color"));
							m.setCon(rs.getString(4));
							sc = ManageClientThread.getClientThread(m.getSender());
							oos = new ObjectOutputStream(sc.s.getOutputStream());
							oos.writeObject(m);
						}
					}
				} else if (m.getMesType().equals(MessageType.message_hasfriendask)) {
					// 返回离线期间是否有好友请求
					String sql1;
					String sql = "select * from AddFriend where FriendId='" + m.getSender() + "'";
					//查询数据库中的添加好友请求记录
					rs = link.doSelect(sql);
					int count = 0;
					while (rs.next()) {
						count++;
					}
					if (count != 0) {
						String str[] = new String[count];
						rs.first();
						int i = 0;
						str[i] = "";
						str[i] += rs.getString(1) + "  " + rs.getString(2) + "  " + rs.getString(3) + "  ";
						sql1 = "select * from Login where UserId='" + rs.getString(1) + "'";
						rs2 = link.doSelect(sql1);
						Message m2 = new Message();
						if (rs2.next()) {
							m2.setFriname(rs2.getString("name") + "(" + rs.getString(1) + ")");
						}
						m2.setMesType(MessageType.message_hasfriendask);
						m2.setSender(rs.getString(1));
						m2.setGetter(rs.getString(2));
						m2.setCon(rs.getString(3));
						ServerConClientThread sc = ManageClientThread.getClientThread(m.getSender());
						ObjectOutputStream oos = new ObjectOutputStream(sc.s.getOutputStream());
						oos.writeObject(m2);
						i++;
						while (rs.next()) {		//添加好友请求逐条返回给客户端
							str[i] = "";
							str[i] += rs.getString(1) + "  " + rs.getString(2) + "  " + rs.getString(3) + "  ";
							sql1 = "select * from Login where UserId='" + rs.getString(1) + "'";
							rs2 = link.doSelect(sql1);
							m2.setMesType(MessageType.message_hasfriendask);
							if (rs2.next()) {
								m2.setFriname(rs2.getString("name") + "(" + rs.getString(1) + ")");
							}
							m2.setSender(rs.getString(1));
							m2.setGetter(rs.getString(2));
							m2.setCon(rs.getString(3));
							sc = ManageClientThread.getClientThread(m.getSender());
							oos = new ObjectOutputStream(sc.s.getOutputStream());
							oos.writeObject(m2);
							i++;
						}
					}
					link.Insert("delete from AddFriend where FriendId='" + m.getSender() + "'");	//删除添加好友请求记录（如果有）
				} else if (m.getMesType().equals(MessageType.message_sendfile)) {
					// 发送文件请求，服务器负责中转
					ServerConClientThread sc = ManageClientThread.getClientThread(m.getGetter());
					if(sc!=null) {
						ObjectOutputStream oos = new ObjectOutputStream(sc.s.getOutputStream());
						oos.writeObject(m);
					}
				}else if(m.getMesType().equals(MessageType.message_returnfileinfo)) {
					// 返回文件接收成功与否的报文，服务器负责中转
					ServerConClientThread sc = ManageClientThread.getClientThread(m.getGetter());
					if(sc!=null) {
						ObjectOutputStream oos = new ObjectOutputStream(sc.s.getOutputStream());
						oos.writeObject(m);
					}
				}
				else if (m.getMesType().equals(MessageType.message_data)) {
					// 返回个人资料信息
					String sql = "select * from Login where UserId='" + m.getSender() + "'";
					// 查询数据库
					rs = link.doSelect(sql);
					if (rs.next()) {
						String str = rs.getString("UserId") + ";"
								+ rs.getString("name") + ";"
								+ rs.getString("sex") + ";"
								+ rs.getString("age") + ";"
								+ rs.getString("brithday") + ";"
								+ rs.getString("shengxiao") + ";"
								+ rs.getString("xinzuo") + ";"
								+ rs.getString("xuexing") + ";"
								+ rs.getString("job") + ";"
								+ rs.getString("xueli") + ";"
								+ rs.getString("school") + ";"
								+ rs.getString("telphone") + ";"
								+ rs.getString("youxiang") + ";"
								+ rs.getString("zuye") + ";"
								+ rs.getString("guxiang") + ";"
								+ rs.getString("place") + ";"
								+ rs.getString("youbian") + ";"
								+ rs.getString("personsign") + ";";
						m.setCon(str);
						ServerConClientThread sc = ManageClientThread.getClientThread(m.getSender());
						ObjectOutputStream oos = new ObjectOutputStream(sc.s.getOutputStream());
						oos.writeObject(m);
					}
				}
				else if (m.getMesType().equals(MessageType.message_fridata)) {
					// 返回好友个人资料信息
					String sql = "select * from Login where UserId='" + m.getSender() + "'";
					rs = link.doSelect(sql);
					if (rs.next()) {
						String str = rs.getString("UserId") + ";"
								+ rs.getString("name") + ";"
								+ rs.getString("sex") + ";"
								+ rs.getString("age") + ";"
								+ rs.getString("brithday") + ";"
								+ rs.getString("shengxiao") + ";"
								+ rs.getString("xinzuo") + ";"
								+ rs.getString("xuexing") + ";"
								+ rs.getString("job") + ";"
								+ rs.getString("xueli") + ";"
								+ rs.getString("school") + ";"
								+ rs.getString("telphone") + ";"
								+ rs.getString("youxiang") + ";"
								+ rs.getString("zuye") + ";"
								+ rs.getString("guxiang") + ";"
								+ rs.getString("place") + ";"
								+ rs.getString("youbian") + ";"
								+ rs.getString("personsign") + ";";
						m.setCon(str);
						ServerConClientThread sc = ManageClientThread.getClientThread(m.getGetter());
						ObjectOutputStream oos = new ObjectOutputStream(sc.s.getOutputStream());
						oos.writeObject(m);
					}
				}
				else if (m.getMesType().equals(MessageType.message_savedata)) {
					String[] strs = m.getCon().split(";");
					// 保存个人资料的更新
					String sql = "Update Login set name='" + strs[1] + "', "
							+ "sex='" + strs[2] + "', " + "age=" + strs[3]
							+ ", " + "brithday='" + strs[4] + "', "
							+ "shengxiao='" + strs[5] + "', " + "xinzuo='"
							+ strs[6] + "', " + "xuexing='" + strs[7] + "', "
							+ "job='" + strs[8] + "', " + "xueli='" + strs[9]
							+ "', " + "school='" + strs[10] + "', "
							+ "telphone='" + strs[11] + "', " + "youxiang='"
							+ strs[12] + "', " + "zuye='" + strs[13] + "', "
							+ "guxiang='" + strs[14] + "', " + "place='"
							+ strs[15] + "', " + "youbian='" + strs[16] + "' "
							+ "where UserId='" + m.getSender() + "'";
					link.Insert(sql);
				}
				else if (m.getMesType().equals(MessageType.message_Qun_mes)) {
					// 返回群消息
					String sql = "select * from `" + m.getGetter() + "` where UserId !=" + m.getSender();
					// 查询除本人外其他群成员
					rs = link.doSelect(sql);
					// 将该条群消息保存数据库
					sql = " insert into QunChatContent(QunId,UserId,Content,Times,font,size,color) values('"
							+ m.getGetter()
							+ "','"
							+ m.getSender()
							+ "','"
							+ m.getCon()
							+ "','"
							+ m.getSendTime()
							+ "','"
							+ m.getFont()
							+ "','"
							+ m.getSize()
							+ "','"
							+ m.getCol() + "')";
					link.Insert(sql);
					while (rs.next()) {		//群消息逐条返回给每个在线客户端
						m.setGetter(Integer.toString(rs.getInt("UserId")));
						m.setFriname(rs.getString("name")+"("+Integer.toString(rs.getInt("UserId"))+")");
						ServerConClientThread sc = ManageClientThread.getClientThread(m.getGetter());
						if (sc != null) {
							ObjectOutputStream oos = new ObjectOutputStream(sc.s.getOutputStream());
							oos.writeObject(m);
						}
					}
				} else if (m.getMesType().equals(MessageType.message_Qun_oldmes)) {
					// 返回群聊天记录
					String sql = "select * from QunChatContent where QunId=" + m.getGetter();
					rs = link.doSelect(sql);
					int count = 0;
					while (rs.next()) {
						count++;
					}
					if (count != 0) {
						rs.first();
						m.setFont(rs.getString("font"));
						m.setSendTime(rs.getString("Times"));
						m.setSize(rs.getInt("size"));
						m.setCol(rs.getString("color"));
						m.setCon(rs.getString("Content"));
						ServerConClientThread sc = ManageClientThread.getClientThread(m.getSender());
						ObjectOutputStream oos = new ObjectOutputStream(sc.s.getOutputStream());
						oos.writeObject(m);
						while (rs.next()) {
							m.setFont(rs.getString("font"));
							m.setSendTime(rs.getString("Times"));
							m.setSize(rs.getInt("size"));
							m.setCol(rs.getString("color"));
							m.setCon(rs.getString("Content"));
							sc = ManageClientThread.getClientThread(m.getSender());
							oos = new ObjectOutputStream(sc.s.getOutputStream());
							oos.writeObject(m);
						}
					}
				} else if (m.getMesType().equals(MessageType.message_Qunfile)) {
					// 返回群文件
					String sql = "select * from `100000` where UserId!=" + m.getSender();
					rs = link.doSelect(sql);
					int count = 0;
					while (rs.next()) {
						count++;
					}
					if (count != 0) {
						rs.first();
						m.setGetter(Integer.toString(rs.getInt("UserId")));
						m.setMesType(MessageType.message_sendfile);
						ServerConClientThread sc = ManageClientThread.getClientThread(m.getGetter());
						ObjectOutputStream oos = new ObjectOutputStream(sc.s.getOutputStream());
						oos.writeObject(m);
						while (rs.next()) {
							m.setGetter(Integer.toString(rs.getInt("UserId")));
							m.setMesType(MessageType.message_sendfile);
							sc = ManageClientThread.getClientThread(m.getGetter());
							oos = new ObjectOutputStream(sc.s.getOutputStream());
							oos.writeObject(m);
						}
					}
				} else if (m.getMesType().equals(MessageType.message_getQunPeople)) {
					// 返回所有群成员
					String sql = "select UserId from `" + m.getGetter() + "` where UserId !=" + m.getSender();
					rs = link.doSelect(sql);
					int count = 0;
					while (rs.next()) {
						count++;
					}
					if (count != 0) {
						rs.first();
						m.setCon("" + rs.getInt("UserId"));
						ServerConClientThread sc = ManageClientThread.getClientThread(m.getSender());
						ObjectOutputStream oos = new ObjectOutputStream(sc.s.getOutputStream());
						oos.writeObject(m);
						while (rs.next()) {
							m.setCon("" + rs.getInt("UserId"));
							m.setMesType(MessageType.message_getQunPeople);
							sc = ManageClientThread.getClientThread(m.getSender());
							oos = new ObjectOutputStream(sc.s.getOutputStream());
							oos.writeObject(m);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
