/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.observer;

/**
 *
 * @author Fabio
 */

public enum AppEventType {
    TRACK_ADDED_TO_PLAYLIST,
    TRACK_REMOVED_FROM_PLAYLIST,
    PLAYLIST_CREATED,
    TRACK_ADDED_TO_DB,
    TRACK_DELETED_FROM_DB,
    PLAYBACK_TIME_TICK, 
    PLAYBACK_STATE_CHANGED,
    TRACK_UPDATED
}
