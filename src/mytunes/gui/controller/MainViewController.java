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
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mytunes.be.Song;
import mytunes.bll.SongManager;
import mytunes.gui.model.SongModel;

/**
 *
 * @author Simon Birkedal
 */
public class MainViewController implements Initializable
{
    
    private SongManager songManager;
    ObservableList<Song> songs = FXCollections.observableArrayList();
    private SongModel songModel;
    private Song selectedSong;
    private boolean isPlaying;

    @FXML
    private Button btnPlay;
    @FXML
    private TableView<Song> tableSongs;
    @FXML
    private ProgressBar barMediaTimer;
    @FXML
    private TableColumn<Song, String> colName;
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
    private ListView<Song> listPlayList;
    @FXML
    private Slider sliderVolume;
    @FXML
    private MenuBar menuBar;
    @FXML
    private ContextMenu contextSong;
    @FXML
    private Button btnUpdate;

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

            }
            changePlayButton(isPlaying);
            processTimeInfo();
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
                lblSongPlaying.setText(selectedSong.getTitle() + " is playing");
            }
            else
            {
                return;
            }
        }
        else
        {
            songManager.pauseSong();
            lblSongPlaying.setText(selectedSong.getTitle() + " is paused");
        }

        changePlayButton(isPlaying);
        processTimeInfo();
    }

    @FXML
    private void handleSliderVolumeOnMouseDragDetected(MouseEvent event)
    {
        if (event.isPrimaryButtonDown())
        {
            songManager.getMediaPlayer().setVolume(sliderVolume.getValue() / 100);
        }
    }

    @FXML
    private void handleSliderVolumeOnClick(MouseEvent event)
    {
        songManager.getMediaPlayer().setVolume(sliderVolume.getValue() / 100);
    }
    
    public void initialize(URL url, ResourceBundle rb)
    {
        songManager = new SongManager();
        songModel = SongModel.getInstance();
        colName.setCellValueFactory(new PropertyValueFactory<>("title"));
        colArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colRating.setCellValueFactory(new PropertyValueFactory<>("rating"));
        setSongs();
        tableSongs.setItems(songs);
        initialLoad();
        isPlaying = false;
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
        Stage primStage = (Stage) listPlayList.getScene().getWindow();
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
        songs = (ObservableList<Song>) songModel.getSongs();
    }

    private void changePlayButton(boolean playing)
    {
        if (playing)
        {
            btnPlay.setText("Play");
            isPlaying = false;
        }
        else
        {
            btnPlay.setText("Pause");
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
            });
        }
        catch (Exception e)
        {

        }

        lblSongDuration.setText(songManager.getCurrentlyPlayingSong().getDuration());
        lblSongPlaying.setText(songManager.getCurrentlyPlayingSong().getTitle());
    }

    public void nextSong()
    {
        songManager.playNextSong(songs);
        processTimeInfo();
    }
    
    public void prevSong()
    {
        songManager.playPrevSong(songs);
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
    private void handleDeleteSong(ActionEvent event) {
        tableSongs.getItems().remove(selectedSong);
                	
    }

    
    private void handleContextSong(){
        
        songModel.setContextSong(selectedSong);
        
    }

    @FXML
    private void handleAdSongToPlaylist(ActionEvent event) {
    }
    
    @FXML
    private void update(){
       
    }
    
    private void initialLoad()
    {
                try
        {
            songModel.loadSongData();
        } catch (FileNotFoundException ex)
        {
            //Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }


   
}
