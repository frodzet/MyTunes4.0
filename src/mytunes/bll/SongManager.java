package mytunes.bll;

import java.io.File;
import javafx.collections.ObservableList;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import mytunes.be.Song;

/**
 * This is the class which contains all the operational logic for the songs.
 * Whenever a new instance of SongManager is instantiated the developer can call
 * it and perform tasks such as play, pause & stop.
 *
 * @author Simon Birkedal
 */
public class SongManager {

    private Song currentSong;
    private MediaPlayer player;

    /**
     * Plays the specified song if any is found.
     *
     * @param song The song to be played.
     */
    public void playSong(Song song, boolean overwrite) {
        if (currentSong == null || overwrite) {
            currentSong = song;
            File soundFile = new File(currentSong.getPath());
            Media media = new Media(soundFile.toURI().toString());
            player = new MediaPlayer(media);
        }
        player.play();

    }

    /**
     * Pauses the currently playing song.
     */
    public void pauseSong() {
        if (currentSong != null) {
            player.pause();
        }
    }
    /**
     * Gets the currently playing song.
     *
     * @return Returns a song object representing the song currently playing.
     */
    public Song getCurrentlyPlayingSong() {
        return this.currentSong;
    }

    /**
     * Gets the songs length.
     *
     * @return Returns the length of the song.
     */
    public Duration getSongLength() {
        return player.getTotalDuration();
    }

    public Duration getSongTimeElapsed() {
        return player.getCurrentTime();
    }

    public MediaPlayer getMediaPlayer() {
        return player;
    }

    public void playNextSong(ObservableList<Song> songs) {
        Song nextSong = null;
        for (int i = 0; i < songs.size() - 1; i++) {
            if (currentSong.getTitle().equals(songs.get(i).getTitle()) && i != songs.size()) {
                nextSong = songs.get(i + 1);

            }

            if (nextSong == null) {
                nextSong = songs.get(0);

            }
        }
        pauseSong();
        playSong(nextSong, true);

    }
    
        public void playPrevSong(ObservableList<Song> songs) {
        Song prevSong = null;
        for (int i = songs.size() - 1; i > 0; i--) {
            if (currentSong.getTitle().equals(songs.get(i).getTitle()) && i != 0) {
                prevSong = songs.get(i - 1);

            }

            if (prevSong == null) {
                prevSong = songs.get(0);

            }
        }
        pauseSong();
        playSong(prevSong, true);

    }

  

    

}
