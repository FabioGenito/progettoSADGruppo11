/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.command;

/**
 *
 * @author gcucc
 */

/**
 * Interfaccia base per il pattern Command.
 */
public interface Command {
    void execute();
    void undo(); 
}