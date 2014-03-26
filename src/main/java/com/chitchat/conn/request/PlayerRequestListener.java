package com.chitchat.conn.request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.websocket.EncodeException;
import javax.websocket.Session;

import com.chitchat.conn.model.PlayerRequest;

public class PlayerRequestListener {

	private final Map<String, PlayerRequest> clients = new HashMap<String, PlayerRequest>();
	private final Map<String, MoveRequests> moveRequests = new HashMap<String, MoveRequests>();

	public void addPlayerRequest(Session session, String name) {
		clients.put(session.toString(),
				new PlayerRequest(session, clients.size()));
	}

	public void removePlayerRequest(Session session) {
		clients.remove(session.toString());
	}

	public void sendNewJoinerResponse(Session senderSession, String name) {
		PlayerRequest sender = clients.get(senderSession.toString());
		sender.setName(name);
		for (Entry<String, PlayerRequest> client : clients.entrySet()) {
			PlayerRequest clientValue = client.getValue();
			Session clientSession = clientValue.getSession();
			try {

				clientSession.getBasicRemote().sendObject(
						sender.jsonJoinResponse(clients.size()));
				sender.getSession()
						.getBasicRemote()
						.sendObject(
								clientValue.jsonJoinResponse(clients.size()));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (EncodeException e) {
				e.printStackTrace();
			}
		}
	}

	public void sendConnectionResponse(Session senderSession) {
		PlayerRequest sender = clients.get(senderSession.toString());
		try {
			sender.getSession().getBasicRemote()
					.sendObject(sender.jsonConnResponse(clients.size()));
		} catch (IOException | EncodeException e) {
			e.printStackTrace();
		}
	}

	public void sendQuitterResponse(Session senderSession) {
		PlayerRequest sender = clients.get(senderSession.toString());
		for (Entry<String, PlayerRequest> client : clients.entrySet()) {
			PlayerRequest clientValue = client.getValue();
			Session clientSession = clientValue.getSession();
			try {
				clientSession.getBasicRemote().sendObject(
						sender.jsonQuitResponse(clients.size()));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (EncodeException e) {
				e.printStackTrace();
			}
		}
		moveRequests.remove(senderSession.toString());
	}

	public void sendMovementResponse(Session senderSession, int pos) {
		System.out.println("Move Request: " + pos);
		PlayerRequest sender = clients.get(senderSession.toString());
		if (pos > 0) {
			MoveRequests mr = new MoveRequests(clients, sender, pos);
			moveRequests.put(senderSession.toString(), mr);
			mr.start();
		} else {
			sendJumpResponse(sender);
		}
	}

	private void sendJumpResponse(PlayerRequest sender) {
		for (Entry<String, PlayerRequest> client : clients.entrySet()) {
			PlayerRequest clientValue = client.getValue();
			Session clientSession = clientValue.getSession();
			try {
				clientSession.getBasicRemote().sendObject(
						sender.jsonJumpResponse());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (EncodeException e) {
				e.printStackTrace();
			}
		}
	}

	public void sendStopResponse(Session senderSession) {
		System.out.println("Stop Request: ");
		PlayerRequest sender = clients.get(senderSession.toString());
		MoveRequests moveRq = moveRequests.get(senderSession.toString());
		moveRq.stopMoving();
		moveRq.close();
		moveRequests.remove(senderSession.toString());
	}

	public void sendTextResponse(Session senderSession, String message) {
		PlayerRequest sender = clients.get(senderSession.toString());
		for (Entry<String, PlayerRequest> client : clients.entrySet()) {
			PlayerRequest clientValue = client.getValue();
			Session clientSession = clientValue.getSession();
			try {
				clientSession.getBasicRemote().sendObject(
						sender.jsonChatResponse(message));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (EncodeException e) {
				e.printStackTrace();
			}
		}
	}
}