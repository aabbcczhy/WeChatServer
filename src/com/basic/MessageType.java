/**
 * 定义包的种类
 */
package com.basic;

public interface MessageType {

	String message_login_succeed="1";//表明是登陆成功
	String message_login_fail="2";//表明登录失败
	String message_comm_mes="3";//普通信息包
	String message_get_onLineFriend="4";//要求在线好友的包
	String message_ret_onLineFriend="5";//返回在线好友的包
	String messgae_ret_unloadFriends = "6"; //返回下线信息
	String message_ret_friend = "7";//返回好友列表
	String message_ret_addFriend = "8";//添加好友
	String message_ret_oldMessage = "9";//返回聊天记录
	String message_ret_outlineMessage = "10";//返回离线记录
	String message_ret_pointMessage = "11";//返回提示有离线消息
	String message_addpoint = "12";//返回提示有好友请求消息
	String message_acceptadd = "13";//返回同意好友请求
	String message_hasfriendask = "14";//返回是否有好友请求
	String message_sendfile = "15";//发送文件
	String message_returnfileinfo = "151";	//返回文件发送成功与否的信息
	String message_shake = "16";//发送震动
	String message_data = "17";//发送个人资料
	String message_savedata = "18";//保存个人资料
	String message_ret_ip = "19";//返回ip信息
	String message_jujueadd = "21";//返回拒绝好友请求
	String message_fridata = "22";//发送好友个人资料
	String message_Qun_mes = "26";//发送群消息
	String message_Qun_oldmes = "27";//发送群消息记录
	String message_Qunfile = "28";//发送群文件
	String message_getQunPeople = "29";//得到群成员
}
