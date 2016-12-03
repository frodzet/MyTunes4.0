/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mytunes.gui.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
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
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mytunes.be.Playlist;
import mytunes.be.Song;
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
    private ObservableList<Song> songs = FXCollections.observableArrayList();
    private ObservableList<Playlist> playLists = FXCollections.observableArrayList();
    private SongModel songModel;
    private Song selectedSong;
    private boolean isPlaying;
    private boolean isMuted;
    private PlaylistModel plModel;
    private Playlist selectedPlaylist;

    @FXML
    private Button btnPlay;
    @FXML
    private TableView<Song> tableSongs;
    @FXML
    private ProgressBar barMediaTimer;
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
    private Button btnNext;
    @FXML
    private TableView<Playlist> tablePlaylists;
    @FXML
    private TableColumn<Playlist, String> colPlaylist;
    @FXML
    private ImageView imgMute;
    @FXML
    private ImageView imgPlay;
    @FXML
    private Button btnPrev;
    @FXML
    private ImageView imgPrev;
    @FXML
    private ImageView imgNext;

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
                processTimeInfo();
            }
        }
    }

    @FXML
    public void handlePlayPauseButton()
    {
        selectedSong = tableSongs.selectionModelProperty().getValue().getSelectedItem();
        //Play button pressed
        if (!isPlaying)
        {
            if (selectedSong != null)
            {
                songManager.playSong(selectedSong, false);
                
            }
        }
        else
        {
            songManager.pauseSong();
        }

        changePlayButton(isPlaying);
        processTimeInfo();
    }

    public void initialize(URL url, ResourceBundle rb)
    {
        songManager = new SongManager();
        songModel = SongModel.getInstance();
        plModel = PlaylistModel.getInstance();
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colRating.setCellValueFactory(new PropertyValueFactory<>("rating"));
        colPlaylist.setCellValueFactory(new PropertyValueFactory<>("title"));
        setSongs();
        tableSongs.setItems(songs);
        setPlaylists();
        tablePlaylists.setItems(playLists);
        initialLoad();
        isPlaying = false;
        sliderVolume.valueProperty().addListener(listener -> 
        {
            songManager.getMediaPlayer().setVolume(sliderVolume.getValue() / 100);
            Image image = new Image(getClass().getResourceAsStream("/mytunes/images/unmute.png"));
            imgMute.setImage(image);            
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

    public ObservableList<Song> getSongs()
    {
        return songs;
    }

    public TableView<Song> getTableSongs()
    {
        return tableSongs;
    }

    public void setSongs()
    {
        songs = songModel.getSongs();
    }
    
    public ObservableList<Playlist> getPlaylists()
    {
        return playLists;
    }
    
    public TableView<Playlist> getTablePlaylists()
    {
        return tablePlaylists;
    }
    
    public void setPlaylists()
    {
        playLists = plModel.getPlaylists();
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

    private void processTimeInfo()
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

                if (!isPlaying)
                    lblSongPlaying.setText(selectedSong.getTitle() + " is paused");
                else
                    lblSongPlaying.setText(selectedSong.getTitle() + " is playing");
            });

            lblSongDuration.setText(songManager.getCurrentlyPlayingSong().getDuration());

        }
        catch (Exception e)
        {

        }
    }

    @FXML
    public void handleNextSong()
    {
        // Request tableSongs to get focused whenever 
        tableSongs.requestFocus();
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

        changePlayButton(false);
        processTimeInfo();
    }

    @FXML
    public void handlePreviousSong()
    {
        // Request tableSongs to get focused whenever 
        tableSongs.requestFocus();
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

        changePlayButton(false);
        processTimeInfo();
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
        tableSongs.getItems().remove(selectedSong);

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
            selectedPlaylist.getSongList().add(selectedSong);
    }


    private void initialLoad()
    {
        try
        {
            songModel.loadSongData();
        }
        catch (FileNotFoundException ex)
        {
            //Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    private void handleMuteSound(MouseEvent event)
    {
        Image image;
        if (!isMuted)
        {
            image = new Image(getClass().getResourceAsStream("/mytunes/images/mute.png"));
            songManager.getMediaPlayer().setVolume(0.0);
            isMuted = true;            
        }
        else
        {
            image = new Image(getClass().getResourceAsStream("/mytunes/images/unmute.png"));
            songManager.getMediaPlayer().setVolume(sliderVolume.getValue()/100);
            isMuted = false;
        }
        imgMute.setImage(image);
    }

}
