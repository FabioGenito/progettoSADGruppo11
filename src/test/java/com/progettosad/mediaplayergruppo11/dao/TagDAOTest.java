/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.dao;

import static org.junit.jupiter.api.Assertions.*;
import com.progettosad.mediaplayergruppo11.model.Tag;
import com.progettosad.mediaplayergruppo11.model.Track;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

/**
 * Unit Test automatizzato per la logica dei Tag eseguito interamente in memoria.
 * Sfrutta i Fake DAO per isolare i componenti senza l'uso di librerie esterne.
 * * @author Fabio
 */
public class TagDAOTest {

    private TagDAOInterface tagDAO;
    private TrackDAOInterface trackDAO;
    private Track testTrack;

    @BeforeEach
    public void setUp() {
        trackDAO = new FakeTrackDAO();
        tagDAO = new FakeTagDAO();

        // Configurazione dello scenario: inseriamo una traccia finta per ottenere un ID valido
        Track track = new Track();
        track.setTitle("Fake Song");
        track.setArtist("Fake Artist");
        
        testTrack = trackDAO.insertTrack(track);
    }

    @Test
    public void testInsertTag_Success() {
        Tag tag = new Tag();
        tag.setName("Pop");
        tag.setTrackId(testTrack.getId());

        Tag result = tagDAO.insertTag(tag);

        assertNotNull(result);
        assertTrue(result.getId() > 0, "L'ID del tag dovrebbe essere auto-incrementato");
        assertEquals("Pop", result.getName());
    }

    @Test
    public void testGetTagsByTrackId_SortingAndFilter() {
        // Inserimento disordinato per testare l'ordinamento alfabetico simulato (A-Z)
        tagDAO.insertTag(new Tag(0, "Rock", testTrack.getId()));
        tagDAO.insertTag(new Tag(0, "Blues", testTrack.getId()));

        List<Tag> tags = tagDAO.getTagsByTrackId(testTrack.getId());

        assertEquals(2, tags.size());
        assertEquals("Blues", tags.get(0).getName(), "Il primo tag deve essere 'Blues' (Ordinamento alfabetico)");
        assertEquals("Rock", tags.get(1).getName());
    }

    @Test
    public void testDeleteTag_Success() {
        Tag tag = tagDAO.insertTag(new Tag(0, "Jazz", testTrack.getId()));
        
        boolean deleted = tagDAO.deleteTag(tag.getId());
        
        assertTrue(deleted);
        assertTrue(tagDAO.getTagsByTrackId(testTrack.getId()).isEmpty());
    }

    @Test
    public void testInsertTag_Exception_EmptyName() {
        Tag tag = new Tag();
        tag.setName("   "); // Stringa vuota composta da soli spazi
        tag.setTrackId(testTrack.getId());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            tagDAO.insertTag(tag);
        });

        assertTrue(exception.getMessage().contains("vuoto o nullo"));
    }

    @Test
    public void testInsertTag_Exception_TooLongName() {
        Tag tag = new Tag();
        // Genera un nome di 51 caratteri usando i metodi standard Java (supera il limite di 50)
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 51; i++) {
            longName.append("a");
        }
        tag.setName(longName.toString());
        tag.setTrackId(testTrack.getId());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            tagDAO.insertTag(tag);
        });

        assertTrue(exception.getMessage().contains("superare i 50 caratteri"));
    }

    @Test
    public void testInsertTag_Exception_DuplicateTag() {
        Tag tag1 = new Tag(0, "Chill", testTrack.getId());
        tagDAO.insertTag(tag1);

        // Tentativo di inserimento duplicato (case-insensitive) sullo stesso brano
        Tag tag2 = new Tag(0, "chill", testTrack.getId());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            tagDAO.insertTag(tag2);
        });

        assertTrue(exception.getMessage().contains("già associato a questa traccia"));
    }
}