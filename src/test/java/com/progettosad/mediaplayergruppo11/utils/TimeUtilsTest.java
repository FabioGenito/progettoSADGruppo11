package com.progettosad.mediaplayergruppo11.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Classe di test per TimeUtils.
 * Verifica la corretta formattazione dei tempi e la gestione dei casi limite.
 */
class TimeUtilsTest {

    @Test
    @DisplayName("Dovrebbe formattare correttamente i secondi inferiori a un minuto")
    void testFormatSecondsToMinutes_LessThanMinute() {
        assertEquals("00:45", TimeUtils.formatSecondsToMinutes(45));
        assertEquals("00:05", TimeUtils.formatSecondsToMinutes(5));
    }

    @Test
    @DisplayName("Dovrebbe formattare correttamente i secondi superiori a un minuto")
    void testFormatSecondsToMinutes_MoreThanMinute() {
        assertEquals("02:05", TimeUtils.formatSecondsToMinutes(125));
        assertEquals("10:30", TimeUtils.formatSecondsToMinutes(630));
    }

    @Test
    @DisplayName("Dovrebbe formattare esattamente un minuto")
    void testFormatSecondsToMinutes_ExactlyOneMinute() {
        assertEquals("01:00", TimeUtils.formatSecondsToMinutes(60));
    }

    @Test
    @DisplayName("Dovrebbe gestire correttamente il valore zero (Limite inferiore)")
    void testFormatSecondsToMinutes_Zero() {
        assertEquals("00:00", TimeUtils.formatSecondsToMinutes(0));
    }

    @Test
    @DisplayName("Dovrebbe restituire 00:00 per valori negativi (Gestione Errori)")
    void testFormatSecondsToMinutes_NegativeValues() {
        assertEquals("00:00", TimeUtils.formatSecondsToMinutes(-1));
        assertEquals("00:00", TimeUtils.formatSecondsToMinutes(-999));
    }

    @Test
    @DisplayName("Dovrebbe formattare correttamente valori molto grandi (oltre 59 minuti)")
    void testFormatSecondsToMinutes_LargeValues() {
        assertEquals("60:05", TimeUtils.formatSecondsToMinutes(3605)); 
    }

    @Test
    @DisplayName("Dovrebbe lanciare eccezione se si tenta di istanziare la classe (Utility Class)")
    void testPrivateConstructor() throws Exception {
        // Questo test usa la reflection per coprire al 100% il codice, 
        // verificando che il costruttore privato lanci effettivamente l'eccezione
        Constructor<TimeUtils> constructor = TimeUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        
        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () -> {
            constructor.newInstance();
        });
        
        assertTrue(thrown.getCause() instanceof UnsupportedOperationException);
        assertEquals("Classe di utilità non istanziabile", thrown.getCause().getMessage());
    }
}