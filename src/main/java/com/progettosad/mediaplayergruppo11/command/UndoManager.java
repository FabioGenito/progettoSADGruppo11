/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.command;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 *
 * @author gcucc
 */

/**
 * Invoker: gestisce la history dei comandi eseguiti.
 * Implementato come Singleton per centralizzare la gestione dell'Undo.
 */
public class UndoManager {
    private static UndoManager instance;
    private final Deque<Command> commandStack = new ArrayDeque<>();

    private UndoManager() {}

    public static synchronized UndoManager getInstance() {
        if (instance == null) {
            instance = new UndoManager();
        }
        return instance;
    }

    public void saveCommand(Command cmd) {
        commandStack.push(cmd);
        if (commandStack.size() > 10) {
    commandStack.removeLast(); // Mantiene solo gli ultimi 10 comandi in memoria
}
    }

    public void undoLastCommand() {
        if (!commandStack.isEmpty()) {
            Command cmd = commandStack.pop();
            cmd.undo();
        } else {
            System.out.println("Nessun comando da annullare nella history.");
        }
    }
}