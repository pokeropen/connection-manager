package com.op.cm.models;

import com.op.cm.services.ConnectionManager;
import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by jalagari on 14/01/20.
 */
public class Room {

    private String name;
    private List<Player> connectedPlayers = new ArrayList<Player>();
    private Collection<WebSocket> connections = new ArrayList<WebSocket>();

    public Room(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Player> getConnectedPlayers() {
        return connectedPlayers;
    }

    public Player getPlayer(WebSocket client) {
        return connectedPlayers.stream()
                                .filter((player -> player.getConnection() == client))
                                .findAny()
                                .orElse(null);
    }

    public int getConnectionSize() {
        return connections.size();
    }

    public void addPlayer(Player player) {
        connectedPlayers.add(player);
        connections.add(player.getConnection());
    }

    public void removePlayer(Player player) {
        Predicate<Player> isQualified = ply -> ply.getUsername().equals(player.getUsername());

        List<Player> playerToRemove = connectedPlayers.stream()
                                                        .filter(isQualified)
                                                        .collect(Collectors.toList());

        // Remove Client connections
        playerToRemove.stream()
                    .forEach(ply -> connections.remove(ply.getConnection()));

        // Remove players
        connectedPlayers.removeAll(playerToRemove);
    }

    public void broadcast(String message) {
        ConnectionManager.getInstance().broadcast(message, connections);
    }
}
