package com.chitchat.conn.model;

import com.chitchat.conn.request.RequestQueue;

public class MoveRequest extends Thread {
	private static RequestQueue queue;
	private PlayerRequest sender;
	private boolean jumping;
	private boolean landed;
	private boolean closed;
	private double vVel = 0.001;
	private double hVel = 0.0;
	private double jumphVel = 0.0;
	private int pos = 1;

	private static final double baseYPlate = -1.82;
	private static final double initialVerticalVelocity = 0.04;
	private static final double initialHorizontalVelocity = 0.01;
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

	public MoveRequest(RequestQueue queue, PlayerRequest sender) {
		MoveRequest.queue = queue;
		this.sender = sender;
		this.closed = false;
		this.landed = false;
	}

	public void close() {
		this.closed = true;
	}

	public void stopMoving() {
		hVel = 0.0;
	}

	private void sendMovementResponse() {
		queue.enqueue(sender.jsonMoveResponse(pos, !landed));
	}

	private void sendLandedResponse() {
		queue.enqueue(sender.jsonMoveResponse(pos, !landed));
	}

	private void handleMovement() {
		if (hVel < 0 && sender.getxPos() <= -maxSceneWidth) {
			hVel = 0.0;
		} else if (hVel > 0 && sender.getxPos() >= maxSceneWidth) {
			hVel = 0.0;
		}
		sender.addxPos(hVel);
	}

	public void jumpUp() {
		if (landed && !jumping) {
			landed = false;
			jumping = true;
			vVel = initialVerticalVelocity;
			jumphVel = hVel;
		}
	}

	public void moveLeft() {
		pos = 1;
		hVel = -initialHorizontalVelocity;
	}

	public void moveRight() {
		pos = 2;
		hVel = initialHorizontalVelocity;
	}

	private void handleJumping() {
		// Fall Detection
		if (sender.getyPos() < baseYPlate) { // Landed
			sender.setyPos(baseYPlate);
			landed = true;
			jumping = false;
			jumphVel = 0.0;
			sendLandedResponse();
		} else if (!this.jumping && !this.landed) { // Falling
			sender.addyPos(vVel);
			vVel -= 0.001;
			sender.addxPos(jumphVel);
		} else if (this.jumping && (vVel >= 0)) { // Jumping
			sender.addyPos(vVel);
			vVel -= 0.001;
			sender.addxPos(jumphVel);
		} else {
			jumping = false;
		}
	}

	@Override
	public void run() {
		try {
			while (!closed) {
				Thread.sleep(15);
				handleJumping();
				handleMovement();
				if (hVel != 0.0 || jumping || !landed)
					sendMovementResponse();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (closed)
			return;
	}
}
