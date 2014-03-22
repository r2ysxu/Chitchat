package com.chitchat.conn.model;

import javax.websocket.Session;

public class PlayerRequest {
	private Session session;
	private String name;
	private double xPos;
	private double yPos;
	private int index;

	public PlayerRequest(Session session, int index) {
		this.session = session;
		this.name = name;
		this.xPos = 0.0;
		this.yPos = 0.0;
		this.index = index;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public double getxPos() {
		return xPos;
	}

	public void setxPos(double xPos) {
		this.xPos = xPos;
	}

	public double getyPos() {
		return yPos;
	}

	public void setyPos(double yPos) {
		this.yPos = yPos;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean equals(Object comp) {
		Session s = (Session) comp;
		return session.equals(s);
	}

	public String jsonConnResponse(int pc) {
		String str = "{" + "\"type\" : \"conn\"" + ", \"index\" : \"" + index
				+ "\"" + ", \"name\" : " + "\"" + name + "\"" + ", \"pc\" :"
				+ "\"" + pc + "\"" + "}";
		return str;
	}

	public String jsonJoinResponse(int pc) {
		String str = "{" + "\"type\" : \"join\"" + ", \"index\" : \"" + index
				+ "\"" + ", \"name\" : " + "\"" + name + "\"" + ", \"pc\" :"
				+ "\"" + pc + "\"" + "}";
		// System.out.println("Join Rsp:" + str);
		return str;
	}

	public String jsonQuitResponse(int pc) {
		String str = "{" + "\"type\" : \"quit\"" + ", \"index\" : \"" + index
				+ "\"" + ", \"pc\" :" + "\"" + pc + "\"" + "}";
		// System.out.println("Quit Rsp:" + str);
		return str;
	}

	public String jsonMoveResponse(String pos) {
		String str = "{" + "\"type\" : \"move\"" + ", \"index\" : \"" + index
				+ "\", \"pos\" : \"" + pos + "\"" + "}";
		// System.out.println("Move Rsp:" + str);
		return str;
	}

	public String jsonStopResponse(long eta) {
		String str = "{" + "\"type\" : \"stop\"" + ", \"index\" : \"" + index
				+ "\", \"eta\" : \"" + eta + "\"" + "}";
		System.out.println("Stop Rsp:" + str);
		return str;
	}

	public String jsonChatResponse(String message) {
		String str = "{" + "\"type\" : \"chat\"" + ", \"index\" : \"" + index
				+ "\"" + ", \"name\" : " + "\"" + name + "\""
				+ ", \"message\": \"" + message + "\"" + "}";
		System.out.println("Chat Rsp:" + str);
		return str;
	}
}