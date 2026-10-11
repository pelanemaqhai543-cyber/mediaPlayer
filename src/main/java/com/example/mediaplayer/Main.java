
package com.example.mediaplayer;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.List;

public class Main extends Application {

    private final ObservableList<File> playlist =
            FXCollections.observableArrayList();

    private final ListView<File> playlistView =
            new ListView<>(playlist);

    private final MediaView mediaView = new MediaView();

    private final ImageView artworkView = new ImageView();

    private final StackPane mediaArea = new StackPane();

    private final Label artworkPlaceholder =
            new Label("SELECT A SONG");

    private final Label currentFileLabel =
            new Label("No media loaded");

    private final Label timeLabel =
            new Label("00:00 / 00:00");

    private final Label statusLabel =
            new Label("Ready");

    private final Label volumeLabel =
            new Label("Volume 70%");

    private final Slider progressSlider =
            new Slider(0, 100, 0);

    private final Slider volumeSlider =
            new Slider(0, 100, 70);

    private MediaPlayer mediaPlayer;

    private boolean muted = false;

    private double previousVolume = 70;

    private boolean userSeeking = false;

    private boolean programmaticSelection = false;

    private final Button playPauseButton =
            new Button();

    private final Button stopButton =
            new Button();

    private final Button previousButton =
            new Button();

    private final Button nextButton =
            new Button();

    private final Button muteButton =
            new Button();

    private final Button addButton =
            new Button("Add Media");

    private final Button removeButton =
            new Button("Remove");

    
    private static final String ICON_PLAY = "/icons/play.png";
    private static final String ICON_PAUSE = "/icons/pause.png";
    private static final String ICON_STOP = "/icons/stop.png";
    private static final String ICON_PREVIOUS = "/icons/previous.png";
    private static final String ICON_NEXT = "/icons/previous.png";
    private static final String ICON_VOLUME = "/icons/volume.png";
    private static final String ICON_MUTED = "/icons/mute.png";
    private static final String ICON_PLUS = "/icons/plus.png";
    private static final String ICON_REMOVE = "/icons/remove.png";

    private ImageView createIcon(String resourcePath, boolean mirrored) {
        var resource = getClass().getResource(resourcePath);

       
        if (resource == null && resourcePath.startsWith("/")) {
            resource = getClass().getResource(resourcePath.substring(1));
        }

        if (resource == null) {
            throw new IllegalStateException(
                    "Missing icon resource: " + resourcePath
                            + "\nCopy icons to src/main/resources/icons/"
            );
        }

        ImageView icon = new ImageView(new Image(resource.toExternalForm(), true));
        icon.setFitWidth(22);
        icon.setFitHeight(22);
        icon.setPreserveRatio(true);
        icon.setSmooth(true);
        icon.setMouseTransparent(true);
        if (mirrored) {
            icon.setScaleX(-1);
        }
        icon.getStyleClass().add("icon-image");
        return icon;
    }

    private void setIcon(Button button, String resourcePath) {
        ImageView icon = createIcon(resourcePath, button == nextButton);
        double size = button == playPauseButton
                ? 24
                : (button == addButton || button == removeButton ? 17 : 20);
        icon.setFitWidth(size);
        icon.setFitHeight(size);
        button.setGraphic(icon);
        button.setAccessibleText(button == muteButton ? "Mute or unmute" : button.getAccessibleText());
    }


    @Override
    public void start(Stage stage) {

        stage.setTitle("Neon Media Player");

        BorderPane root = new BorderPane();

        root.getStyleClass().add("root");

        VBox playerPanel = createPlayerPanel();

        VBox playlistPanel = createPlaylistPanel();

        root.setCenter(playerPanel);

        root.setRight(playlistPanel);

        Scene scene = new Scene(root, 1280, 800);

        scene.getStylesheets().add(
                getClass()
                        .getResource("style.css")
                        .toExternalForm()
        );

        setupKeyboardControls(scene);

        setupPlaylistSelection();

        setupProgressControls();

        setupVolumeControls();

        stage.setMinWidth(900);

        stage.setMinHeight(620);

        stage.setScene(scene);

        stage.show();

        loadMediaFolder();

        playlistView.requestFocus();
    }


    private VBox createPlayerPanel() {

        Label appTitle =
                new Label("NEON MEDIA PLAYER");

        appTitle.getStyleClass().add("app-title");


        currentFileLabel.getStyleClass()
                .add("current-file");

        currentFileLabel.setMaxWidth(Double.MAX_VALUE);

        currentFileLabel.setAlignment(Pos.CENTER);


        createMediaArea();

        setIcon(previousButton, ICON_PREVIOUS);
        setIcon(playPauseButton, ICON_PLAY);
        setIcon(stopButton, ICON_STOP);
        setIcon(nextButton, ICON_NEXT);
        setIcon(muteButton, ICON_VOLUME);
        setIcon(addButton, ICON_PLUS);
        setIcon(removeButton, ICON_REMOVE);
        previousButton.setAccessibleText("Previous track");
        playPauseButton.setAccessibleText("Play or pause");
        stopButton.setAccessibleText("Stop playback");
        nextButton.setAccessibleText("Next track");
        addButton.setAccessibleText("Add media");
        removeButton.setAccessibleText("Remove selected media");

        progressSlider.setMaxWidth(Double.MAX_VALUE);

        progressSlider.getStyleClass()
                .add("progress-slider");


        timeLabel.getStyleClass()
                .add("time-label");


        HBox timeBox =
                new HBox(timeLabel);

        timeBox.setAlignment(Pos.CENTER_RIGHT);


        HBox controls =
                new HBox(
                        10,
                        previousButton,
                        playPauseButton,
                        stopButton,
                        nextButton
                );

        controls.setAlignment(Pos.CENTER);

        controls.getStyleClass()
                .add("control-bar");


        playPauseButton.getStyleClass()
                .addAll(
                        "control-button",
                        "play-button"
                );

        previousButton.getStyleClass()
                .add("control-button");

        nextButton.getStyleClass()
                .add("control-button");

        stopButton.getStyleClass()
                .addAll(
                        "control-button",
                        "stop-button"
                );


        playPauseButton.setOnAction(
                e -> togglePlayPause()
        );

        stopButton.setOnAction(
                e -> stopMedia()
        );

        previousButton.setOnAction(
                e -> playPrevious()
        );

        nextButton.setOnAction(
                e -> playNext()
        );


        HBox volumeBox =
                new HBox(
                        12,
                        muteButton,
                        volumeSlider,
                        volumeLabel
                );

        volumeBox.setAlignment(Pos.CENTER);

        volumeBox.getStyleClass()
                .add("volume-bar");


        volumeSlider.setPrefWidth(220);


        muteButton.getStyleClass()
                .add("small-button");


        volumeLabel.getStyleClass()
                .add("volume-label");


        muteButton.setOnAction(
                e -> toggleMute()
        );


        statusLabel.getStyleClass()
                .add("status-label");


        VBox panel =
                new VBox(
                        8,
                        appTitle,
                        currentFileLabel,
                        mediaArea,
                        progressSlider,
                        timeBox,
                        controls,
                        volumeBox,
                        statusLabel
                );

        panel.setPadding(new Insets(24, 28, 28, 28));

        panel.setAlignment(Pos.TOP_CENTER);

       
        VBox.setMargin(controls, new Insets(10, 0, 0, 0));
        VBox.setMargin(volumeBox, new Insets(4, 0, 0, 0));
        VBox.setMargin(statusLabel, new Insets(4, 0, 0, 0));

        panel.getStyleClass()
                .add("player-panel");

        // Let the media area take the flexible middle space.
        VBox.setVgrow(mediaArea, Priority.ALWAYS);
        panel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        statusLabel.setMaxHeight(24);
        statusLabel.setAlignment(Pos.CENTER);

        return panel;
    }


    private void createMediaArea() {

        mediaArea.getStyleClass()
                .add("media-area");

        // Flexible sizing prevents the media area from overflowing smaller windows.
        mediaArea.setMinSize(0, 0);
        mediaArea.setPrefSize(800, 620);
        mediaArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);


        /*
         * Preserve the original aspect ratio. The media is fitted inside the
         * available area instead of being stretched or cropped.
         */
        mediaView.setPreserveRatio(true);

        mediaView.setSmooth(true);

        mediaView.setVisible(false);

        mediaView.getStyleClass()
                .add("media-view");


      
        artworkView.setPreserveRatio(true);

        artworkView.setSmooth(true);

        artworkView.setVisible(false);

        artworkView.getStyleClass()
                .add("artwork");


        artworkPlaceholder.getStyleClass()
                .add("artwork-placeholder");

        artworkPlaceholder.setVisible(true);


        mediaArea.getChildren().addAll(
                mediaView,
                artworkView,
                artworkPlaceholder
        );


       
        mediaArea.widthProperty().addListener(
                (obs, oldValue, newValue)
                        -> resizeMediaView()
        );

        mediaArea.heightProperty().addListener(
                (obs, oldValue, newValue)
                        -> resizeMediaView()
        );
    }


    
    private void resizeMediaView() {

        double width =
                Math.max(
                        0,
                        mediaArea.getWidth()
                );

        double height =
                Math.max(
                        0,
                        mediaArea.getHeight()
                );


        if (width <= 0 || height <= 0) {
            return;
        }

        mediaView.setFitWidth(width);
        mediaView.setFitHeight(height);
        artworkView.setFitWidth(width);
        artworkView.setFitHeight(height);
    }


    private VBox createPlaylistPanel() {

        Label title =
                new Label("PLAYLIST");

        title.getStyleClass()
                .add("playlist-title");


        playlistView.getStyleClass()
                .add("playlist");


        playlistView.setCellFactory(
                list -> new ListCell<>() {

                    @Override
                    protected void updateItem(
                            File file,
                            boolean empty
                    ) {

                        super.updateItem(
                                file,
                                empty
                        );

                        setText(
                                empty || file == null
                                        ? null
                                        : file.getName()
                        );
                    }
                }
        );


        HBox buttons =
                new HBox(
                        8,
                        addButton,
                        removeButton
                );

        buttons.setAlignment(Pos.CENTER);


        addButton.getStyleClass()
                .add("action-button");

        removeButton.getStyleClass()
                .add("action-button");


        addButton.setMaxWidth(
                Double.MAX_VALUE
        );

        removeButton.setMaxWidth(
                Double.MAX_VALUE
        );


        HBox.setHgrow(
                addButton,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                removeButton,
                Priority.ALWAYS
        );


        addButton.setOnAction(
                e -> addMediaFiles()
        );

        removeButton.setOnAction(
                e -> removeSelectedMedia()
        );


        Label hint =
                new Label(
                        "Space Play/Pause   •   S Stop   •   N Next   •   P Previous\n"
                                + "↑/↓ Volume   •   M Mute   •   A Add   •   R Remove"
                );

        hint.getStyleClass()
                .add("playlist-hint");

        hint.setWrapText(true);


        VBox panel =
                new VBox(
                        14,
                        title,
                        playlistView,
                        buttons,
                        hint
                );

        panel.setPadding(
                new Insets(20)
        );

        panel.setPrefWidth(340);
        panel.setMinWidth(270);
        panel.setMaxHeight(Double.MAX_VALUE);
        panel.setFillWidth(true);

        panel.getStyleClass()
                .add("playlist-panel");


        VBox.setVgrow(
                playlistView,
                Priority.ALWAYS
        );

        return panel;
    }


    private void setupKeyboardControls(
            Scene scene
    ) {

        // Use a key-event filter so shortcuts work even when the playlist
        // or another control currently has keyboard focus.
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode key = event.getCode();
            boolean handled = true;

            switch (key) {
                case SPACE -> togglePlayPause();
                case S -> stopMedia();
                case N -> playNext();
                case P -> playPrevious();
                case UP -> changeVolume(5);
                case DOWN -> changeVolume(-5);
                case M -> toggleMute();
                case A -> addMediaFiles();
                case R -> removeSelectedMedia();
                default -> handled = false;
            }

            if (handled) {
                event.consume();
            }
        });
    }


    private void setupPlaylistSelection() {

        playlistView
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, oldFile, newFile) -> {

                            if (
                                    newFile != null
                                            && !programmaticSelection
                            ) {

                                int index =
                                        playlist.indexOf(
                                                newFile
                                        );

                                if (index >= 0) {

                                    loadMedia(
                                            newFile,
                                            index,
                                            true
                                    );
                                }
                            }
                        }
                );
    }


    private void setupProgressControls() {

        progressSlider.setOnMousePressed(
                e -> userSeeking = true
        );


        progressSlider.setOnMouseReleased(
                e -> {

                    userSeeking = false;

                    seekToSliderPosition();
                }
        );


        progressSlider.valueProperty()
                .addListener(
                        (obs, oldValue, newValue) -> {

                            if (userSeeking) {

                                updateTimeLabelFromSlider();
                            }
                        }
                );
    }


    private void setupVolumeControls() {

        volumeSlider.valueProperty()
                .addListener(
                        (obs, oldValue, newValue) -> {
                            double value = newValue.doubleValue();

                            // Moving the slider is an explicit volume
                            // change. It also unmutes when raised above zero.
                            if (value <= 0.5) {
                                muted = true;
                                if (mediaPlayer != null) {
                                    mediaPlayer.setVolume(0);
                                }
                                setIcon(muteButton, ICON_MUTED);
                            } else {
                                muted = false;
                                if (mediaPlayer != null) {
                                    mediaPlayer.setVolume(value / 100.0);
                                }
                                setIcon(muteButton, ICON_VOLUME);
                            }

                            updateVolumeLabel();
                        }
                );
    }


    private void loadMediaFolder() {

        File folder =
                new File("media");

        if (!folder.exists()) {

            folder.mkdirs();
        }


        File[] files =
                folder.listFiles(
                        file ->
                                file.isFile()
                                        && isSupportedMedia(file)
                );


        if (files == null) {

            return;
        }


        playlist.clear();


        for (File file : files) {

            playlist.add(file);
        }


        if (!playlist.isEmpty()) {

            loadMedia(
                    playlist.get(0),
                    0,
                    false
            );

            programmaticSelection = true;

            playlistView
                    .getSelectionModel()
                    .select(0);

            programmaticSelection = false;
        }
    }


    private void addMediaFiles() {

        FileChooser chooser =
                new FileChooser();

        chooser.setTitle(
                "Add Media Files"
        );


        chooser.getExtensionFilters()
                .add(
                        new FileChooser.ExtensionFilter(
                                "Media Files",
                                "*.mp3",
                                "*.mp4",
                                "*.wav",
                                "*.m4a",
                                "*.aac",
                                "*.aiff",
                                "*.flac"
                        )
                );


        List<File> selected =
                chooser.showOpenMultipleDialog(
                        mediaArea
                                .getScene()
                                .getWindow()
                );


        if (
                selected == null
                        || selected.isEmpty()
        ) {

            return;
        }


        int firstNewIndex = -1;


        for (File file : selected) {

            if (
                    isSupportedMedia(file)
                            && !playlist.contains(file)
            ) {

                playlist.add(file);

                if (firstNewIndex == -1) {

                    firstNewIndex =
                            playlist.size() - 1;
                }
            }
        }


        if (firstNewIndex >= 0) {

            playlistView
                    .getSelectionModel()
                    .select(firstNewIndex);

            playlistView.scrollTo(
                    firstNewIndex
            );
        }
    }


    private void removeSelectedMedia() {

        int index =
                playlistView
                        .getSelectionModel()
                        .getSelectedIndex();


        if (index < 0) {

            return;
        }


        boolean removingCurrent =
                mediaPlayer != null;


        playlist.remove(index);


        if (removingCurrent) {

            disposePlayer();


            if (playlist.isEmpty()) {

                showEmptyPlayer();

            } else {

                int newIndex =
                        Math.min(
                                index,
                                playlist.size() - 1
                        );

                playlistView
                        .getSelectionModel()
                        .select(newIndex);
            }
        }
    }


    private void loadMedia(
            File file,
            int index,
            boolean autoPlay
    ) {

        if (
                file == null
                        || !file.exists()
        ) {

            return;
        }


        disposePlayer();


        try {

            Media media =
                    new Media(
                            file.toURI().toString()
                    );


            mediaPlayer =
                    new MediaPlayer(media);


            mediaPlayer.setVolume(
                    muted
                            ? 0
                            : volumeSlider.getValue() / 100.0
            );


            mediaView.setMediaPlayer(
                    mediaPlayer
            );


            currentFileLabel.setText(
                    file.getName()
            );


            statusLabel.setText(
                    "Loading • "
                            + (index + 1)
                            + " / "
                            + playlist.size()
            );


            boolean video =
                    isVideo(file);


            mediaView.setVisible(video);

            artworkView.setVisible(!video);

            artworkPlaceholder
                    .setVisible(!video);


            if (!video) {

                loadArtwork();


                if (
                        artworkView.getImage()
                                != null
                ) {

                    artworkView.setVisible(
                            true
                    );

                    artworkPlaceholder
                            .setVisible(false);
                }
            }


            /*
             * Make sure the media gets the current
             * available size immediately after loading.
             */
            resizeMediaView();


            mediaPlayer.setOnReady(() -> {

                Duration total =
                        mediaPlayer
                                .getTotalDuration();


                progressSlider.setDisable(false);

                progressSlider.setValue(0);


                updateTimeLabel(
                        Duration.ZERO,
                        total
                );


                statusLabel.setText(
                        "Ready • "
                                + formatTime(total)
                );


                
                resizeMediaView();


                if (autoPlay) {

                    mediaPlayer.play();
                }
            });


            mediaPlayer.currentTimeProperty()
                    .addListener(
                            (obs, oldTime, newTime) -> {

                                if (!userSeeking) {

                                    Duration total =
                                            mediaPlayer
                                                    .getTotalDuration();


                                    if (
                                            total != null
                                                    && total.greaterThan(
                                                    Duration.ZERO
                                            )
                                    ) {

                                        progressSlider
                                                .setValue(
                                                        newTime.toMillis()
                                                                / total.toMillis()
                                                                * 100.0
                                                );
                                    }


                                    updateTimeLabel(
                                            newTime,
                                            total
                                    );
                                }
                            }
                    );


            mediaPlayer.setOnPlaying(() -> {

                setIcon(playPauseButton, ICON_PAUSE);

                statusLabel.setText(
                        "Playing • "
                                + file.getName()
                );
            });


            mediaPlayer.setOnPaused(() -> {

                setIcon(playPauseButton, ICON_PLAY);

                statusLabel.setText(
                        "Paused • "
                                + file.getName()
                );
            });


            mediaPlayer.setOnEndOfMedia(
                    this::playNext
            );


            mediaPlayer.setOnError(() -> {

                if (
                        mediaPlayer.getError()
                                != null
                ) {

                    statusLabel.setText(
                            "Media error: "
                                    + mediaPlayer
                                    .getError()
                                    .getMessage()
                    );
                }
            });

        } catch (Exception e) {

            statusLabel.setText(
                    "Could not load: "
                            + file.getName()
            );
        }
    }


    private void loadArtwork() {

        File artworkFile =
                new File("media/artwork.jpg");


        if (!artworkFile.exists()) {

            artworkFile =
                    new File("media/artwork.png");
        }


        if (artworkFile.exists()) {

            artworkView.setImage(
                    new Image(
                            artworkFile
                                    .toURI()
                                    .toString(),
                            true
                    )
            );

        } else {

            artworkView.setImage(null);
        }
    }


    private void showEmptyPlayer() {

        currentFileLabel.setText(
                "No media loaded"
        );

        statusLabel.setText(
                "Playlist is empty"
        );

        progressSlider.setValue(0);

        timeLabel.setText(
                "00:00 / 00:00"
        );

        mediaView.setVisible(false);

        artworkView.setVisible(false);

        artworkPlaceholder.setVisible(true);
    }


    private void togglePlayPause() {

        if (mediaPlayer == null) {

            return;
        }


        if (
                mediaPlayer.getStatus()
                        == MediaPlayer.Status.PLAYING
        ) {
            // Update immediately; the onPaused callback confirms the state.
            setIcon(playPauseButton, ICON_PLAY);
            mediaPlayer.pause();

        } else {
            // Update immediately; the onPlaying callback confirms the state.
            setIcon(playPauseButton, ICON_PAUSE);
            mediaPlayer.play();
        }
    }


    private void stopMedia() {

        if (mediaPlayer != null) {

            mediaPlayer.stop();
            setIcon(playPauseButton, ICON_PLAY);

            progressSlider.setValue(0);
        }
    }


    private void playNext() {

        if (playlist.isEmpty()) {

            return;
        }


        int current =
                playlistView
                        .getSelectionModel()
                        .getSelectedIndex();


        int next =
                (current + 1)
                        % playlist.size();


        playlistView
                .getSelectionModel()
                .select(next);
    }


    private void playPrevious() {

        if (playlist.isEmpty()) {

            return;
        }


        int current =
                playlistView
                        .getSelectionModel()
                        .getSelectedIndex();


        int previous =
                current <= 0
                        ? playlist.size() - 1
                        : current - 1;


        playlistView
                .getSelectionModel()
                .select(previous);
    }


    private void changeVolume(
            double amount
    ) {

        double value =
                Math.max(
                        0,
                        Math.min(
                                100,
                                volumeSlider.getValue()
                                        + amount
                        )
                );


        volumeSlider.setValue(value);


        if (value > 0) {

            muted = false;

            setIcon(muteButton, ICON_VOLUME);
        }
    }


    private void toggleMute() {

        if (mediaPlayer == null) {

            return;
        }


        if (!muted) {

            previousVolume =
                    volumeSlider.getValue() <= 0
                            ? 70
                            : volumeSlider.getValue();

            // Set the state before changing the slider so the listener cannot
            // briefly restore sound during the mute operation.
            muted = true;
            volumeSlider.setValue(0);
            mediaPlayer.setVolume(0);
            setIcon(muteButton, ICON_MUTED);

        } else {

            double restoredVolume =
                    previousVolume <= 0
                            ? 70
                            : previousVolume;

            muted = false;
            volumeSlider.setValue(restoredVolume);
            mediaPlayer.setVolume(restoredVolume / 100.0);
            setIcon(muteButton, ICON_VOLUME);
        }
    }


    private void updateVolumeLabel() {

        volumeLabel.setText(
                "Volume "
                        + Math.round(
                        volumeSlider.getValue()
                )
                        + "%"
        );
    }


    private void seekToSliderPosition() {

        if (mediaPlayer == null) {

            return;
        }


        Duration total =
                mediaPlayer
                        .getTotalDuration();


        if (
                total == null
                        || total.isUnknown()
                        || total.lessThanOrEqualTo(
                        Duration.ZERO
                )
        ) {

            return;
        }


        double fraction =
                progressSlider.getValue()
                        / 100.0;


        mediaPlayer.seek(
                total.multiply(fraction)
        );
    }


    private void updateTimeLabelFromSlider() {

        if (mediaPlayer == null) {

            return;
        }


        Duration total =
                mediaPlayer.getTotalDuration();


        if (
                total == null
                        || total.isUnknown()
        ) {

            return;
        }


        updateTimeLabel(
                total.multiply(
                        progressSlider.getValue()
                                / 100.0
                ),
                total
        );
    }


    private void updateTimeLabel(
            Duration current,
            Duration total
    ) {

        if (
                current == null
                        || current.isUnknown()
        ) {

            current = Duration.ZERO;
        }


        if (
                total == null
                        || total.isUnknown()
        ) {

            total = Duration.ZERO;
        }


        timeLabel.setText(
                formatTime(current)
                        + " / "
                        + formatTime(total)
        );
    }


    private String formatTime(
            Duration duration
    ) {

        int seconds =
                (int) Math.floor(
                        duration.toSeconds()
                );


        int minutes =
                seconds / 60;


        int remaining =
                seconds % 60;


        return String.format(
                "%02d:%02d",
                minutes,
                remaining
        );
    }


    private boolean isSupportedMedia(
            File file
    ) {

        String name =
                file.getName()
                        .toLowerCase();


        return name.endsWith(".mp3")
                || name.endsWith(".mp4")
                || name.endsWith(".wav")
                || name.endsWith(".m4a")
                || name.endsWith(".aac")
                || name.endsWith(".aiff")
                || name.endsWith(".flac");
    }


    private boolean isVideo(File file) {

        String name =
                file.getName()
                        .toLowerCase();


        return name.endsWith(".mp4");
    }


    private void disposePlayer() {

        if (mediaPlayer != null) {

            mediaPlayer.stop();

            mediaPlayer.dispose();

            mediaPlayer = null;
        }
    }


    @Override
    public void stop() {

        disposePlayer();
    }


    public static void main(
            String[] args
    ) {

        launch(args);
    }

}
