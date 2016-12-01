/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mytunes.gui.model;

import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mytunes.be.Playlist;
import mytunes.gui.controller.MainViewController;

/**
 *
 * @author James
 */
public class PlaylistModel {

    private static PlaylistModel instance;

    ObservableList<Playlist> playlists = FXCollections.observableArrayList();

    public static PlaylistModel getInstance()
    {
        if (instance == null)
        {
            instance = new PlaylistModel();
        }
        return instance;
    }

    private PlaylistModel()
    {
    }

    public void addPlaylist(Playlist playlist)
    {
        playlists.add(playlist);
    }

    public List<Playlist> getPlaylists()
    {
        return playlists;
    }
    

    public List<String> getPlaylistTitles()
    {
        List<String> playlistTitles = null;
        for (Playlist playlist : playlists)
        {
            playlistTitles.add(playlist.getTitle());
        }
        return playlistTitles;
    }

    public void updatePlaylistView()
    {
        
    }

}
