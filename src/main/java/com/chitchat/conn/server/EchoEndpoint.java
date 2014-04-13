package com.chitchat.conn.server;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.chitchat.conn.model.PlayerRequest;
import com.chitchat.conn.request.PlayerRequestListener;
import com.json.parsers.JSONParser;
import com.json.parsers.JsonParserFactory;

@ServerEndpoint("/echo")
public class EchoEndpoint {

	private static final PlayerRequestListener pl = new PlayerRequestListener();

	private static final List<PlayerRequest> clients = new LinkedList<PlayerRequest>();

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
		@SuppressWarnings("rawtypes")
		Map jsonMap = parser.parseJson(message);

		String value = (String) jsonMap.get("type");

		if (value.contains("join")) {
			pl.sendNewJoinerResponse(session, (String) jsonMap.get("name"));
			pl.startMovement(session);
		} else if (value.contains("chat")) {
			pl.sendTextResponse(session, (String) jsonMap.get("message"));
		} else if (value.equals("move")) {
			pl.sendMovementResponse(session,
					Integer.parseInt(jsonMap.get("pos").toString()));
		} else if (value.equals("stop")) {
			pl.sendStopResponse(session);
		} else if (value.equals("shoot")) {
			pl.sendShootResponse(session);
		}
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		System.out.println(String.format("Session %s closed because of %s",
				session.getId(), closeReason));
		pl.sendQuitterResponse(session);
		System.out.println("Still active: " + clients.size());
	}
}