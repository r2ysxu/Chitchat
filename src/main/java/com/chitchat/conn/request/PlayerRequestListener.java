package com.chitchat.conn.request;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.websocket.EncodeException;
import javax.websocket.Session;

import com.chitchat.conn.model.PlayerRequest;

/**
 * This Class Listens to requests
 * 
 * @author arthurxu
 * 
 */
public class PlayerRequestListener {

	private final Map<String, PlayerRequest> clients = new HashMap<String, PlayerRequest>();
	private final Map<String, MoveRequests> moveRequests = new HashMap<String, MoveRequests>();

	private RequestQueue queue;

	public PlayerRequestListener() {
		queue = new RequestQueue(clients);
		queue.start();
	}

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
		stopMovement(senderSession);
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

	public void startMovement(Session senderSession) {
		// System.out.println("Move Request: " + pos);
		PlayerRequest sender = clients.get(senderSession.toString());
		MoveRequests mr = new MoveRequests(queue, sender);
		moveRequests.put(senderSession.toString(), mr);
		mr.start();
	}

	public void sendMovementResponse(Session senderSession, int pos) {
		// System.out.println("Move Request: " + pos);
		MoveRequests mr = moveRequests.get(senderSession.toString());
		switch (pos) {
		case 0:
			mr.jumpUp();
			break;
		case 1:
			mr.moveLeft();
			break;
		case 2:
			mr.moveRight();
			break;
		}
	}

	public void sendStopResponse(Session senderSession) {
		MoveRequests moveRq = moveRequests.get(senderSession.toString());
		moveRq.stopMoving();
	}

	private void stopMovement(Session senderSession) {
		// System.out.println("Stop Request: ");
		MoveRequests moveRq = moveRequests.get(senderSession.toString());
		moveRq.close();
		moveRequests.remove(senderSession.toString());
	}

	public void sendShootResponse(Session senderSession) {
		PlayerRequest sender = clients.get(senderSession.toString());
		queue.enqueue(sender.jsonShootResponse());
	}

	public void sendTextResponse(Session senderSession, String message) {
		PlayerRequest sender = clients.get(senderSession.toString());
		queue.enqueue(sender.jsonChatResponse(message));
	}
}