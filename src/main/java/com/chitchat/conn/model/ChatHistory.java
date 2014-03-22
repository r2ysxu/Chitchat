package com.chitchat.conn.model;

import java.util.ArrayList;
import java.util.List;

public class ChatHistory {

	private List<String> chatHistory;

	public ChatHistory() {
		chatHistory = new ArrayList<String>();
	}

	public void addMessage(String message) {
		chatHistory.add(message);
	}

	public String getLastMessage() {
		return getMessageAt(chatHistory.size() - 1);
	}

	public String getMessageAt(int index) {
		return chatHistory.get(index);
	}
}
