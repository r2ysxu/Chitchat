package com.chitchat.conn.request;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.websocket.EncodeException;
import javax.websocket.Session;

import com.chitchat.conn.model.PlayerRequest;

public class MoveRequests extends Thread {
	private static double xSpeed = 0.01;
	private static Map<String, PlayerRequest> clients;
	private PlayerRequest sender;
	private boolean moving;
	private boolean jumping;
	private boolean leftright;
	private boolean closed;

	public MoveRequests(Map<String, PlayerRequest> clients,
			PlayerRequest sender, int pos) {
		MoveRequests.clients = clients;
		this.sender = sender;
		this.closed = false;

		switch (pos) {
		// Jump
		case 0:
			jumping = true;
			break;
		// Left
		case 1:
			moving = true;
			leftright = true;
			break;
		// Right
		case 2:
			moving = true;
			leftright = false;
			break;
		default:
			moving = false;
			jumping = false;
		}
	}

	public void close() {
		this.closed = true;
	}

	public void startMoving() {
		this.moving = true;
	}

	public void stopMoving() {
		this.moving = false;
	}

	private void sendMovementResponse() {
		for (Entry<String, PlayerRequest> client : clients.entrySet()) {
			PlayerRequest clientValue = client.getValue();
			Session clientSession = clientValue.getSession();
			try {
				double offset = 0.01;
				if (leftright)
					offset *= -1;
				sender.addxPos(offset);
				clientSession.getBasicRemote().sendObject(
						sender.jsonMoveResponse());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (EncodeException e) {
				e.printStackTrace();
			}
		}
	}

	private void sendJumpResponse() {
		for (Entry<String, PlayerRequest> client : clients.entrySet()) {
			PlayerRequest clientValue = client.getValue();
			Session clientSession = clientValue.getSession();
			try {
				clientSession.getBasicRemote().sendObject(
						sender.jsonMoveResponse());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (EncodeException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		while (moving) {
			if (closed)
				return;
			try {
				Thread.sleep(15);
				if (moving)
					sendMovementResponse();
				else if (jumping)
					sendJumpResponse();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}
}
