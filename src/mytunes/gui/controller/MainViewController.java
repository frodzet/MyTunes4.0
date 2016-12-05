/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mytunes.gui.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import mytunes.be.Playlist;
import mytunes.be.Song;
import mytunes.bll.PlaylistManager;
import mytunes.bll.Search;
import mytunes.bll.SongManager;
import mytunes.gui.model.PlaylistModel;
import mytunes.gui.model.SongModel;

/**
 *
 * @author Simon Birkedal
 */
public class MainViewController implements Initializable
{
    private SongManager songManager;
    private ObservableList<Song> songsLibrary;
    private ObservableList<Song> currentSongsInView;
    private ObservableList<Playlist> playlists;
    private SongModel songModel;
    private Song selectedSong;
    private boolean isPlaying;
    private boolean isMuted;
    private double sliderVolumeValue;
    private PlaylistModel playlistModel;
    private Playlist selectedPlaylist;

    @FXML
    private Button btnPlay;
    @FXML
    private Button btnPrev;
    @FXML
    private Button btnNext;
    @FXML
    private ImageView imgMute;
    @FXML
    private ImageView imgPlay;
    @FXML
    private ImageView imgPrev;
    @FXML
    private ImageView imgNext;
    @FXML
    private TextField txtSearch;
    @FXML
    private Label lblSongPlaying;
    @FXML
    private Label lblSongDuration;
    @FXML
    private Label lblTimeElapsed;
    @FXML
    private Slider sliderVolume;
    @FXML
    private MenuBar menuBar;
    @FXML
    private ContextMenu contextSong;
    @FXML
    private ProgressBar barMediaTimer;
    @FXML
    private TableView<Song> tableSongs;
    @FXML
    private TableColumn<Song, String> colTitle;
    @FXML
    private TableColumn<Song, String> colArtist;
    @FXML
    private TableColumn<Song, String> colGenre;
    @FXML
    private TableColumn<Song, Double> colDuration;
    @FXML
    private TableColumn<Song, Double> colRating;
    @FXML
    private TableView<Playlist> tablePlaylists;
    @FXML
    private TableColumn<Playlist, String> colPlaylist;
    private PlaylistManager playlistManager;
    @FXML
    private Hyperlink hlinkBrowse;
    @FXML
    private Label lblClearSearch;

    public MainViewController()
    {
        this.songsLibrary = FXCollections.observableArrayList();
        this.currentSongsInView = FXCollections.observableArrayList();
        this.playlists = FXCollections.observableArrayList();
    }


    @FXML
    public void handleAddSongButton() throws IOException
    {
        loadAddSongView();
    }

    @FXML
    private void handleAddPlaylistButton() throws IOException
    {
        loadPlaylistSongView();
    }

    @FXML
    public void handleOnMousePressed(MouseEvent event)
    {
        selectedSong = tableSongs.selectionModelProperty().getValue().getSelectedItem();
        if (event.isPrimaryButtonDown() && event.getClickCount() == 2)
        {
            if (selectedSong != null)
            {
                songManager.pauseSong();
                songManager.playSong(selectedSong, true);

                changePlayButton(false);
                processMediaInfo();
            }
        }
    }

    @FXML
    public void handlePlay()
    {        
        // Making sure the song is never null before trying to play a song.
        if (selectedSong == null)
            tableSongs.selectionModelProperty().get().select(0);

        selectedSong = tableSongs.selectionModelProperty().getValue().getSelectedItem();
        //Play button pressed
        if (!isPlaying)
            songManager.playSong(selectedSong, false);
        else
            songManager.pauseSong();

        changePlayButton(isPlaying);
        processMediaInfo();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        songManager = new SongManager();
        playlistManager = new PlaylistManager();
        songModel = SongModel.getInstance();
        playlistModel = PlaylistModel.getInstance();
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colRating.setCellValueFactory(new PropertyValueFactory<>("rating"));
        colPlaylist.setCellValueFactory(new PropertyValueFactory<>("title"));
        setSongs();
        tableSongs.setItems(songModel.getSongs());
        currentSongsInView = songsLibrary;
        setPlaylists();
        tablePlaylists.setItems(playlists);
        initialLoad();
        isPlaying = false;
        sliderVolume.valueProperty().addListener(listener -> 
        {
            songManager.getMediaPlayer().setVolume(sliderVolume.getValue() / 100);
            Image image = new Image(getClass().getResourceAsStream("/mytunes/images/unmute.png"));
            imgMute.setImage(image);
            isMuted = false;
        });

        txtSearch.textProperty().addListener((listener, oldVal, newVal) -> {
            ObservableList<Song> searchedSongs = FXCollections.observableArrayList();
            searchedSongs.clear();
            for (Song song : songModel.getSongs())
            {
                if (song.getTitle().trim().toLowerCase().contains(newVal) 
                        || song.getArtist().trim().toLowerCase().contains(newVal) 
                        || song.getGenre().trim().toLowerCase().contains(newVal)
                        || String.valueOf(song.getRating()).trim().toLowerCase().contains(newVal)
                        && !searchedSongs.contains(song))
                {                        
                    searchedSongs.add(song);                        
                }
            }

            tableSongs.setItems(searchedSongs);
        });
    }
    
    private void loadAddSongView() throws IOException
    {
        Stage primStage = (Stage) tableSongs.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mytunes/gui/view/AddSongView.fxml"));
        Parent root = loader.load();

        AddSongViewController addSongViewController = loader.getController();

        Stage addSongViewStage = new Stage();
        addSongViewStage.setScene(new Scene(root));

        addSongViewStage.initModality(Modality.WINDOW_MODAL);
        addSongViewStage.initOwner(primStage);

        addSongViewStage.show();

    }

    private void loadPlaylistSongView() throws IOException
    {
        Stage primStage = (Stage) tablePlaylists.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mytunes/gui/view/AddPlaylistView.fxml"));
        Parent root = loader.load();

        Stage addPlaylistViewStage = new Stage();
        addPlaylistViewStage.setScene(new Scene(root));

        addPlaylistViewStage.initModality(Modality.WINDOW_MODAL);
        addPlaylistViewStage.initOwner(primStage);

        addPlaylistViewStage.show();

    }

    public TableView<Song> getTableSongs()
    {
        return tableSongs;
    }

    public void setSongs()
    {
        songsLibrary.addAll(songModel.getSongs());
    }
    
    public ObservableList<Playlist> getPlaylists()
    {
        return playlists;
    }
    
    public TableView<Playlist> getTablePlaylists()
    {
        return tablePlaylists;
    }
    
    public void setPlaylists()
    {
        playlists = playlistModel.getPlaylists();
    }

    private void changePlayButton(boolean playing)
    {
        Image image;
        if (playing)
        {
            image = new Image(getClass().getResourceAsStream("/mytunes/images/play.png"));
            imgPlay.setImage(image);
            isPlaying = false;
        }
        else
        {
            image = new Image(getClass().getResourceAsStream("/mytunes/images/pause.png"));
            imgPlay.setImage(image);
            isPlaying = true;
        }
    }

    private void processMediaInfo()
    {
        try
        {
            songManager.getMediaPlayer().currentTimeProperty().addListener((listener, oldVal, newVal) -> 
            {
                long minutes = (long) newVal.toMinutes();
                long seconds = (long) (newVal.toSeconds() % 60);
                this.lblTimeElapsed.setText(String.format("%02d:%02d", minutes, seconds));

                double timeElapsed = newVal.toMillis() / songManager.getSongLength().toMillis();
                this.barMediaTimer.setProgress(timeElapsed);
            });
            
            if (!isPlaying)
                lblSongPlaying.setText(songManager.getCurrentlyPlayingSong().getTitle() + " is paused");
            else
                lblSongPlaying.setText(songManager.getCurrentlyPlayingSong().getTitle() + " is playing");
            
            lblSongDuration.setText(songManager.getCurrentlyPlayingSong().getDuration());
        }
        finally
        {

        }
    }

    @FXML
    public void handleNextSong()
    {
        TableViewSelectionModel<Song> selectionModel = tableSongs.selectionModelProperty().getValue();
        int selectedSongIndex = selectionModel.getSelectedIndex();
        int tableSongsTotalItems = tableSongs.getItems().size() - 1;
        
        if (selectedSongIndex == tableSongsTotalItems || selectedSong == null)
        {
            selectionModel.clearAndSelect(0);
        }
        else
        {
            selectionModel.clearAndSelect(selectedSongIndex + 1);
        }

        selectedSong = selectionModel.getSelectedItem();

        songManager.pauseSong();
        songManager.playSong(selectedSong, true);
        songManager.getMediaPlayer().setVolume(sliderVolume.getValue()/100);

        changePlayButton(false);
        processMediaInfo();
    }

    @FXML
    public void handlePreviousSong()
    {
        TableViewSelectionModel<Song> selectionModel = tableSongs.selectionModelProperty().getValue();
        int selectedSongIndex = selectionModel.getSelectedIndex();
        int tableSongsTotalItems = tableSongs.getItems().size() - 1;
        
        if (songManager.getSongTimeElapsed().toMillis() <= 3500.0)
        {
            if (selectedSongIndex == 0 || selectedSong == null)
            {
                selectionModel.clearAndSelect(tableSongsTotalItems);
            }
            else
            {
                selectionModel.clearAndSelect(selectedSongIndex - 1);
            }
        }
        else
        {
            selectionModel.clearAndSelect(selectedSongIndex);
        }

        selectedSong = selectionModel.getSelectedItem();

        songManager.pauseSong();
        songManager.playSong(selectedSong, true);
        songManager.getMediaPlayer().setVolume(sliderVolume.getValue()/100);

        changePlayButton(false);
        processMediaInfo();
    }

    @FXML
    private void handleEditSong(ActionEvent event) throws IOException
    {
        handleContextSong();
        Stage primStage = (Stage) tableSongs.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mytunes/gui/view/EditSongView.fxml"));
        Parent root = loader.load();

        EditSongViewController editSongViewController = loader.getController();

        Stage editSongViewStage = new Stage();
        editSongViewStage.setScene(new Scene(root));

        editSongViewStage.initModality(Modality.WINDOW_MODAL);
        editSongViewStage.initOwner(primStage);

        editSongViewStage.show();

    }

    @FXML
    private void handleDeleteSong(ActionEvent event)
    {
        songModel.getSongs().remove(selectedSong);
        tableSongs.getItems().remove(selectedSong);
//        for (Playlist playlist : playlists)
//        {
//            for (Song song : playlist.getSongList())
//            {
//                if (!songModel.getSongs().contains(song))
//                {
//                    playlist.getSongList().remove(song);
//                }
//            }
//        }

    }

    private void handleContextSong()
    {

        songModel.setContextSong(selectedSong);

    }

    @FXML
    private void handleAddSongToPlaylist(ActionEvent event)
    {
        selectedPlaylist = tablePlaylists.getSelectionModel().getSelectedItem();
        if (selectedPlaylist != null)
            playlistManager.addSong(selectedPlaylist, selectedSong);
    }
    
    @FXML
    private void handleOnSelectedPlaylist(MouseEvent event)
    {
        selectedPlaylist = tablePlaylists.getSelectionModel().getSelectedItem();
        if (selectedPlaylist != null)
        {
            if (event.isPrimaryButtonDown() && event.getClickCount() == 2)
            {
                currentSongsInView.clear();
            }
            
            for (Song song : selectedPlaylist.getSongList())
            {
                if (!currentSongsInView.contains(song))
                    currentSongsInView.add(song);
                if (!songModel.getSongs().contains(song))
                    currentSongsInView.remove(song);
            }
        }
        tableSongs.setItems(currentSongsInView);
    }

    private void initialLoad()
    {
        try
        {
            songModel.loadSongData();
            playlistModel.loadPlaylistData();
        }
        catch (Exception e)
        {
            //Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
        tableSongs.setItems(songModel.getSongs());
    }
    
    @FXML
    private void handleMuteSound(MouseEvent event)
    {
        Image image;        
        if (!isMuted)
        {
            sliderVolumeValue = sliderVolume.getValue();
            image = new Image(getClass().getResourceAsStream("/mytunes/images/mute.png"));
            sliderVolume.setValue(0.0);
            isMuted = true;            
        }
        else
        {
            image = new Image(getClass().getResourceAsStream("/mytunes/images/unmute.png"));
            sliderVolume.setValue(sliderVolumeValue);
            isMuted = false;
        }
        
        imgMute.setImage(image);
    }

    @FXML
    private void handleClearSearch(MouseEvent event)
    {
        txtSearch.setText("");
        
    }

    @FXML
    private void handleSetTimeElapsed(MouseEvent event)
    {
        
    }

    @FXML
    private void handleBrowseOnAction(ActionEvent event)
    {
        tableSongs.requestFocus();
        tableSongs.setItems(songModel.getSongs());
        
    }
}
