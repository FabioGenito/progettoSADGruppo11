/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.command;

/**
 *
 * @author gcucc
 */
import org.junit.jupiter.api.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UndoManagerTest {

    /**
     * Comando fittizio che registra quante volte execute() e undo()
     * sono stati chiamati, senza dipendenze esterne.
     */
    static class FakeCommand implements Command {
        final String name;
        int executeCount = 0;
        int undoCount    = 0;

        FakeCommand(String name) { 
            this.name = name; 
        }

        @Override public void execute(){
            executeCount++; 
        }
        
        @Override public void undo(){
            undoCount++;   
        }
    }

    // ------------------------------------------------------------------ //
    //  Reset del singleton prima di ogni test
    // ------------------------------------------------------------------ //

    /**
     * Azzera l'istanza singleton tramite reflection in modo che ogni test
     * parta con un UndoManager pulito, senza stato residuo dai test precedenti.
     */
    @BeforeEach
    void resetSingleton() throws Exception {
        Field instanceField = UndoManager.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    // ------------------------------------------------------------------ //
    //  getInstance
    // ------------------------------------------------------------------ //

    @Test
    void testGetInstanceReturnsSameInstance() {
        UndoManager a = UndoManager.getInstance();
        UndoManager b = UndoManager.getInstance();
        assertSame(a, b, "getInstance() deve restituire sempre il medesimo oggetto");
    }

    // ------------------------------------------------------------------ //
    //  saveCommand
    // ------------------------------------------------------------------ //

    @Test
    void testSaveCommandAddsToHistory() {
        UndoManager manager = UndoManager.getInstance();
        FakeCommand cmd = new FakeCommand("cmd1");

        manager.saveCommand(cmd);

        // Se la history non fosse vuota, undoLastCommand() chiamerà undo()
        manager.undoLastCommand();
        assertEquals(1, cmd.undoCount, "Il comando salvato deve essere annullabile");
    }

    @Test
    void testSaveCommandLIFOOrder() {
        UndoManager manager = UndoManager.getInstance();
        FakeCommand first  = new FakeCommand("first");
        FakeCommand second = new FakeCommand("second");

        manager.saveCommand(first);
        manager.saveCommand(second);

        // L'ultimo salvato deve essere il primo ad essere annullato
        manager.undoLastCommand();
        assertEquals(1, second.undoCount, "Il secondo comando deve essere annullato per primo");
        assertEquals(0, first.undoCount,  "Il primo comando non deve ancora essere annullato");
    }

    @Test
    void testSaveCommandMaxTenCommands() {
        UndoManager manager = UndoManager.getInstance();
        List<FakeCommand> commands = new ArrayList<>();

        for (int i = 0; i < 11; i++) {
            FakeCommand cmd = new FakeCommand("cmd" + i);
            commands.add(cmd);
            manager.saveCommand(cmd);
        }

        // Contiamo quanti vengono effettivamente annullati
        int undoCount = 0;
        for (int i = 0; i < 11; i++) {
            int before = commands.stream().mapToInt(c -> c.undoCount).sum();
            manager.undoLastCommand();
            int after = commands.stream().mapToInt(c -> c.undoCount).sum();
            if (after > before) undoCount++;
            else break; // La stack è vuota
        }

        assertEquals(10, undoCount,
                "La history non deve contenere più di 10 comandi");
    }

    @Test
    void testSaveCommandDiscardsOldestWhenFull() {
        UndoManager manager = UndoManager.getInstance();
        FakeCommand oldest = new FakeCommand("oldest");
        manager.saveCommand(oldest);

        // Riempiamo con altri 10 comandi (il primo viene espulso)
        for (int i = 0; i < 10; i++) {
            manager.saveCommand(new FakeCommand("filler" + i));
        }

        // Annulliamo tutti i 10 rimasti
        for (int i = 0; i < 10; i++) {
            manager.undoLastCommand();
        }

        // Il comando più vecchio non deve mai essere stato annullato
        assertEquals(0, oldest.undoCount,
                "Il comando più vecchio deve essere stato scartato dalla history");
    }

    // ------------------------------------------------------------------ //
    //  undoLastCommand
    // ------------------------------------------------------------------ //

    @Test
    void testUndoLastCommandCallsUndo() {
        UndoManager manager = UndoManager.getInstance();
        FakeCommand cmd = new FakeCommand("cmd");

        manager.saveCommand(cmd);
        manager.undoLastCommand();

        assertEquals(1, cmd.undoCount, "undo() deve essere stato chiamato esattamente una volta");
    }

    @Test
    void testUndoLastCommandOnEmptyStackDoesNotThrow() {
        UndoManager manager = UndoManager.getInstance();
        assertDoesNotThrow(manager::undoLastCommand,
                "undoLastCommand() su history vuota non deve lanciare eccezioni");
    }

    @Test
    void testUndoLastCommandRemovesCommandFromHistory() {
        UndoManager manager = UndoManager.getInstance();
        FakeCommand cmd = new FakeCommand("cmd");

        manager.saveCommand(cmd);
        manager.undoLastCommand();
        manager.undoLastCommand(); // stack vuoto: non deve chiamare undo() di nuovo

        assertEquals(1, cmd.undoCount,
                "Il comando deve essere rimosso dalla history dopo l'undo");
    }

    @Test
    void testUndoLastCommandReverseOrder() {
        UndoManager manager = UndoManager.getInstance();
        List<String> order = new ArrayList<>();

        // Creiamo comandi che registrano l'ordine di annullamento
        Command cmd1 = new Command() {
            public void execute() {}
            public void undo() { order.add("undo-cmd1"); }
        };
        Command cmd2 = new Command() {
            public void execute() {}
            public void undo() { order.add("undo-cmd2"); }
        };
        Command cmd3 = new Command() {
            public void execute() {}
            public void undo() { order.add("undo-cmd3"); }
        };

        manager.saveCommand(cmd1);
        manager.saveCommand(cmd2);
        manager.saveCommand(cmd3);

        manager.undoLastCommand();
        manager.undoLastCommand();
        manager.undoLastCommand();

        assertEquals(List.of("undo-cmd3", "undo-cmd2", "undo-cmd1"), order,
                "I comandi devono essere annullati in ordine LIFO");
    }
}