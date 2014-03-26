package com.chitchat.conn.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.chitchat.conn.model.PlayerRequest;
import com.chitchat.conn.request.MoveRequests;
import com.chitchat.conn.request.PlayerRequestListener;
import com.json.parsers.JSONParser;
import com.json.parsers.JsonParserFactory;

@ServerEndpoint("/echo")
public class EchoEndpoint {

	private static final PlayerRequestListener pl = new PlayerRequestListener();

	private static final List<PlayerRequest> clients = new LinkedList<PlayerRequest>();
	private List<Thread> moveThreads = new ArrayList<Thread>();

	@OnOpen
	public void onOpen(Session session) {
		System.out.println("Connected: " + session.getId());
		pl.addPlayerRequest(session, "");
		pl.sendConnectionResponse(session);
		System.out.println("Current Connections: " + clients.size());
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		// System.out.println("Message Rcv: " + message);

		JsonParserFactory factory = JsonParserFactory.getInstance();
		JSONParser parser = factory.newJsonParser();
		Map jsonMap = parser.parseJson(message);

		String value = (String) jsonMap.get("type");

		if (value.contains("join")) {
			pl.sendNewJoinerResponse(session, (String) jsonMap.get("name"));
		} else if (value.contains("chat")) {
			pl.sendTextResponse(session, (String) jsonMap.get("message"));
		} else if (value.equals("move")) {
			pl.sendMovementResponse(session,
					Integer.parseInt(jsonMap.get("pos").toString()));
		} else if (value.equals("stop")) {
			pl.sendStopResponse(session);
		}
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		System.out.println(String.format("Session %s closed because of %s",
				session.getId(), closeReason));
		pl.removePlayerRequest(session);
		pl.sendQuitterResponse(session);
		System.out.println("Still active: " + clients.size());
	}
}