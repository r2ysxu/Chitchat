package com.chitchat.conn.request;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.websocket.EncodeException;
import javax.websocket.Session;

import com.chitchat.conn.model.MoveRequest;
import com.chitchat.conn.model.PlayerRequest;

/**
 * This Class Listens to requests
 * 
 * @author arthurxu
 * 
 */
public class PlayerRequestListener {

	private final Map<String, PlayerRequest> clients = new HashMap<String, PlayerRequest>();

	private boolean unusedplayerSlots[];

	private RequestQueue queue;

	public PlayerRequestListener() {
		unusedplayerSlots = new boolean[4];
		Arrays.fill(unusedplayerSlots, false);
		queue = new RequestQueue(clients);
		queue.start();
	}

	private int occupyPlayerSlot() {
		int i;
		for (i = 0; i < unusedplayerSlots.length; i++) {
			if (!unusedplayerSlots[i]) {
				unusedplayerSlots[i] = true;
				break;
			}
		}
		return i;
	}

	private void freePlayerSlot(int slotIndex) {
		unusedplayerSlots[slotIndex] = false;
	}

	public void addPlayerRequest(Session session, String name) {
		int nextIndex = occupyPlayerSlot();
		clients.put(session.toString(), new PlayerRequest(session, queue, nextIndex));
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
		freePlayerSlot(sender.getIndex());
		System.out.println("Removed:" + senderSession.toString() + " index: "
				+ sender.getIndex());
		removePlayerRequest(senderSession);
		queue.enqueue(sender.jsonQuitResponse(clients.size()));
	}

	public void startMovement(Session senderSession) {
		PlayerRequest sender = clients.get(senderSession.toString());
		sender.startMoving(queue);
	}

	public void sendMovementResponse(Session senderSession, int pos) {
		PlayerRequest sender = clients.get(senderSession.toString());
		MoveRequest mr = sender.getMoveRequest();
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
		PlayerRequest sender = clients.get(senderSession.toString());
		sender.getMoveRequest().stopMoving();
	}

	private void stopMovement(Session senderSession) {
		PlayerRequest sender = clients.get(senderSession.toString());
		sender.getMoveRequest().close();
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