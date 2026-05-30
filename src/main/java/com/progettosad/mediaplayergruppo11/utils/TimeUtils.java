
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.utils;

/**
 *
 * @author User
 */

/**
 * Utility per la conversione e formattazione dei dati temporali.
 */
public final class TimeUtils {

    private TimeUtils() {
        throw new UnsupportedOperationException("Classe di utilità non istanziabile");
    }

    
    /**
     * Converte un valore intero di secondi nel formato standard "MM:SS".
     * Restituisce "00:00" in caso di input negativo.
     */
    public static String formatSecondsToMinutes(int totalSeconds) {
        if (totalSeconds < 0) return "00:00";
        int minuti = totalSeconds / 60;
        int secondi = totalSeconds % 60;
        return String.format("%02d:%02d", minuti, secondi);
    }
}
