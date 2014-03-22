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
import com.json.parsers.JSONParser;
import com.json.parsers.JsonParserFactory;

@ServerEndpoint("/echo")
public class EchoEndpoint {

	private static final List<PlayerRequest> clients = new LinkedList<PlayerRequest>();

	@OnOpen
	public void onOpen(Session session) {
		System.out.println("Connected: " + session.getId());
		PlayerRequest newPlayer = new PlayerRequest(session, clients.size());
		clients.add(newPlayer);
		sendConnectionResponse(newPlayer);
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
			clients.get(getPlayerIndex(session)).setName(
					(String) jsonMap.get("name"));
			sendNewJoinerResponse(getPlayerRequest(session));
			sendOldJoinerResponse(getPlayerRequest(session));
		} else if (value.contains("chat")) {
			sendTextResponse(getPlayerRequest(session),
					(String) jsonMap.get("message"));
		} else if (value.equals("move")) {
			sendMovementResponse((String) jsonMap.get("pos"),
					getPlayerRequest(session));
		} else if (value.equals("stop")) {
			sendStopResponse(getPlayerRequest(session),
					(String) jsonMap.get("eta"));
		}
	}

	private void sendConnectionResponse(PlayerRequest sender) {
		try {
			sender.getSession().getBasicRemote()
					.sendObject(sender.jsonConnResponse(clients.size()));
		} catch (IOException | EncodeException e) {
			e.printStackTrace();
		}
	}

	private void sendNewJoinerResponse(PlayerRequest sender) {
		for (PlayerRequest client : clients) {
			Session clientSession = client.getSession();
			try {
				clientSession.getBasicRemote().sendObject(
						sender.jsonJoinResponse(clients.size()));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (EncodeException e) {
				e.printStackTrace();
			}
		}
	}

	private void sendOldJoinerResponse(PlayerRequest sender) {
		for (PlayerRequest client : clients) {
			try {
				sender.getSession().getBasicRemote()
						.sendObject(client.jsonJoinResponse(clients.size()));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (EncodeException e) {
				e.printStackTrace();
			}
		}
	}

	private void sendQuitterResponse(PlayerRequest sender) {
		for (PlayerRequest client : clients) {
			Session clientSession = client.getSession();
			try {
				clientSession.getBasicRemote().sendObject(
						sender.jsonQuitResponse(clients.size()));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (EncodeException e) {
				e.printStackTrace();
			}
		}
	}

	private void sendMovementResponse(String pos, PlayerRequest sender) {
		for (PlayerRequest client : clients) {
			Session clientSession = client.getSession();
			try {
				clientSession.getBasicRemote().sendObject(
						sender.jsonMoveResponse(pos));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (EncodeException e) {
				e.printStackTrace();
			}
		}
	}

	private void sendStopResponse(PlayerRequest sender, String etaStr) {
		for (PlayerRequest client : clients) {
			long eta = Long.parseLong(etaStr);
			Session clientSession = client.getSession();
			try {
				clientSession.getBasicRemote().sendObject(
						sender.jsonStopResponse(eta));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (EncodeException e) {
				e.printStackTrace();
			}
		}
	}

	private void sendTextResponse(PlayerRequest sender, String message) {
		for (PlayerRequest client : clients) {
			Session clientSession = client.getSession();
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

	private PlayerRequest getPlayerRequest(Session session) {
		int index = 0;
		for (PlayerRequest client : clients) {
			if (client.getSession().equals(session))
				break;
			index++;
		}
		return clients.get(index);
	}

	private int getPlayerIndex(Session session) {
		int index = 0;
		for (PlayerRequest client : clients) {
			if (client.getSession().equals(session))
				break;
			index++;
		}
		return index;
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		System.out.println(String.format("Session %s closed because of %s",
				session.getId(), closeReason));
		clients.remove(getPlayerIndex(session));
		sendQuitterResponse(getPlayerRequest(session));
		System.out.println("Still active: " + clients.size());
	}
}