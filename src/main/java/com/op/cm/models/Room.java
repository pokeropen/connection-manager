package com.op.cm.models;

import com.op.cm.services.ConnectionManager;
import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by jalagari on 14/01/20.
 */
public class Room {

    private String name;
    private List<Player> connectedPlayers = new ArrayList<Player>();
    private Collection<WebSocket> clients = new ArrayList<>();

    public Room(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Player> getConnectedPlayers() {
        return connectedPlayers;
    }

    public void addPlayer(Player player) {
        connectedPlayers.add(player);
        clients.add(player.getSession());
    }

    public void removePlayer(Player player) {
        Stream<Player> players = connectedPlayers.stream().filter((player1 -> player1.getUsername().equals(player1.getUsername())));
        players.forEach(player1 -> {
                connectedPlayers.remove(player1);
                clients.remove(player1.getSession());
        });
    }

    public void broadcast(String message) {
        ConnectionManager.getInstance().broadcast(message, clients);
    }
}
