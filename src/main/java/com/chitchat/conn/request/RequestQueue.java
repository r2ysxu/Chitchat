package com.chitchat.conn.request;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.websocket.Session;

import com.chitchat.conn.model.PlayerRequest;

public class RequestQueue extends Thread {
	private volatile ConcurrentLinkedQueue<String> requests = new ConcurrentLinkedQueue<String>();
	private final Map<String, PlayerRequest> clients;
	private boolean closed = false;

	public RequestQueue(Map<String, PlayerRequest> clients) {
		this.clients = clients;
	}

	public void enqueue(String req) {
		requests.add(req);
	}

	@Override
	public void run() {
		while (!closed) {
			if (!requests.isEmpty()) {
				String response = requests.remove();
				for (Entry<String, PlayerRequest> client : clients.entrySet()) {
					PlayerRequest clientValue = client.getValue();
					Session clientSession = clientValue.getSession();
					try {
						clientSession.getBasicRemote().sendText(response);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
