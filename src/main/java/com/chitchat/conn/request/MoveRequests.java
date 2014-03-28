package com.chitchat.conn.request;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.websocket.EncodeException;
import javax.websocket.Session;

import com.chitchat.conn.model.PlayerRequest;

public class MoveRequests extends Thread {
	private static double xSpeed = 0.01;
	// private static Map<String, PlayerRequest> clients;
	private static RequestQueue queue;
	private PlayerRequest sender;
	private int pos;
	private boolean moving;
	private boolean jumping;
	private boolean leftright;
	private boolean closed;

	/*
	 * public MoveRequests(Map<String, PlayerRequest> clients, PlayerRequest
	 * sender, int pos) { MoveRequests.clients = clients; this.sender = sender;
	 * this.closed = false;
	 * 
	 * switch (pos) { // Jump case 0: jumping = true; break; // Left case 1:
	 * moving = true; leftright = true; break; // Right case 2: moving = true;
	 * leftright = false; break; default: moving = false; jumping = false; } }
	 */

	public MoveRequests(RequestQueue queue, PlayerRequest sender, int pos) {
		MoveRequests.queue = queue;
		this.sender = sender;
		this.closed = false;
		this.pos = pos;

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
		double offset = 0.01;
		if (leftright) {
			offset *= -1;
		}
		sender.addxPos(offset);
		queue.enqueue(sender.jsonMoveResponse(pos));
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
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}
}