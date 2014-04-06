package com.chitchat.conn.request;

import com.chitchat.conn.model.PlayerRequest;

public class MoveRequests extends Thread {
	private static double xSpeed = 0.01;
	// private static Map<String, PlayerRequest> clients;
	private static RequestQueue queue;
	private PlayerRequest sender;
	private boolean moving;
	private boolean jumping;
	private boolean landed;
	private boolean leftright;
	private boolean closed;
	private double vVel = 0.001;
	private double hVel = 0.01;
	private double startYPos;

	private static final double baseYPlate = -1.82;
	private static final double maxJumpHeight = 0.6;
	private static final double maxSceneWidth = 3;

	/*
	 * public MoveRequests(Map<String, PlayerRequest> clients, PlayerRequest
	 * sender, int pos) { MoveRequests.clients = clients; this.sender = sender;
	 * this.closed = false;
	 * 
	 * switch (pos) { // Jump case 0: jumping = true; break; // Left case 1:
	 * moving = true; leftright = true; break; // Right case 2: moving = true;
	 * leftright = false; break; default: moving = false; jumping = false; } }
	 */

	public MoveRequests(RequestQueue queue, PlayerRequest sender) {
		MoveRequests.queue = queue;
		this.sender = sender;
		this.closed = false;
		this.moving = false;
		this.leftright = true;
		this.landed = false;
	}

	public void close() {
		this.closed = true;
	}

	public void stopMoving() {
		this.moving = false;
	}

	private void sendMovementResponse() {
		int pos = 1;
		if (!leftright)
			pos = 2;
		queue.enqueue(sender.jsonMoveResponse(pos, !landed));
	}

	private void sendLandedResponse() {
		int pos = 1;
		if (!leftright)
			pos = 2;
		queue.enqueue(sender.jsonMoveResponse(pos, !landed));
	}

	private void handleMovement() {
		double offset = 0.0;
		if (leftright && sender.getxPos() > -maxSceneWidth) {
			offset = -hVel;
		} else if (sender.getxPos() < maxSceneWidth) {
			offset = hVel;
		}
		sender.addxPos(offset);
	}

	public void jumpUp() {
		if (landed && !jumping) {
			landed = false;
			jumping = true;
			vVel = 0.06;
			startYPos = sender.getyPos();
		}
	}

	public void moveLeft() {
		moving = true;
		leftright = true;
	}

	public void moveRight() {
		moving = true;
		leftright = false;
	}

	private void handleJumping() {
		// Fall Detection
		if (sender.getyPos() < baseYPlate) { // Landed
			sender.setyPos(baseYPlate);
			landed = true;
			jumping = false;
			sendLandedResponse();
		} else if (!this.jumping && !this.landed) { // Falling
			sender.addyPos(-vVel);
			vVel += 0.001;
		} else if (this.jumping
				&& ((sender.getyPos() - startYPos) < maxJumpHeight)) { // Jumping
			sender.addyPos(vVel);
			vVel -= 0.001;
		} else {
			this.jumping = false;
		}
	}

	@Override
	public void run() {
		try {
			while (!closed) {
				Thread.sleep(15);
				handleJumping();
				if (moving)
					handleMovement();
				if (moving || jumping || !landed)
					sendMovementResponse();

			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (closed)
			return;
	}
}
