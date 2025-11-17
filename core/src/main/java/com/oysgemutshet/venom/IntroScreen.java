package com.oysgemutshet.venom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.audio.Music;

public class IntroScreen extends ScreenAdapter {

    private final VengeanceOfVenomGame game;
    private final OrthographicCamera camera;
    private final BitmapFont font;
    private final GlyphLayout layout;

    private Music introMusic;
    private float introMusicTime = 0f;
    private boolean musicExists = false; // Flag to track if music file exists

    private static final float INTRO_MUSIC_MAX_TIME = 60f;
    private static final float INTRO_MUSIC_TARGET_VOLUME = 0.6f;
    private static final float INTRO_MUSIC_FADE_SPEED = 0.5f;

    // Red color for selected option
    private static final Color SELECTED_RED = new Color(0.9f, 0.25f, 0.25f, 1f);

    // Menu state
    private enum MenuState {
        MAIN_MENU,
        LEVEL_SELECT
    }

    private MenuState currentState = MenuState.MAIN_MENU;
    private int mainMenuSelection = 0; // 0: Levels, 1: Settings
    private int levelSelection = 0; // 0: Level 1, 1: Test Level

    public IntroScreen(VengeanceOfVenomGame game) {
        this.game = game;
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, 1280, 720);

        // FONT SETUP (unchanged)
        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/RussoOne-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 32;
        param.color = Color.WHITE;
        this.font = generator.generateFont(param);
        generator.dispose();

        this.layout = new GlyphLayout();
    }

    @Override
    public void show() {
        // Check if music file exists before trying to load it
        FileHandle musicFile = Gdx.files.internal("music/boaz_and_the_dogs.mp3");
        musicExists = musicFile.exists();

        if (musicExists) {
            // Create music lazily if needed
            if (introMusic == null) {
                introMusic = Gdx.audio.newMusic(musicFile);
                introMusic.setLooping(false);
            }

            introMusic.setVolume(0f);  // start silent for fade-in
            introMusic.setPosition(0f); // just to be safe, restart from the beginning
            introMusic.play();
        } else {
            Gdx.app.log("IntroScreen", "Music file not found: music/boaz_and_the_dogs.mp3");
        }

        introMusicTime = 0f;  // reset loop timer

        // Reset menu state
        currentState = MenuState.MAIN_MENU;
        mainMenuSelection = 0;
        levelSelection = 0;
    }

    @Override
    public void render(float delta) {
        // Only update music timing and fading if music exists and is playing
        if (musicExists && introMusic != null) {
            introMusicTime += delta;

            // Loop first minute
            if (introMusicTime > INTRO_MUSIC_MAX_TIME) {
                introMusicTime = 0f;
                introMusic.stop();
                introMusic.setPosition(0f);
                introMusic.play();
            }

            // Fade in
            float v = introMusic.getVolume();
            if (v < INTRO_MUSIC_TARGET_VOLUME) {
                v = Math.min(INTRO_MUSIC_TARGET_VOLUME, v + INTRO_MUSIC_FADE_SPEED * delta);
                introMusic.setVolume(v);
            }
        }

        Gdx.gl.glClearColor(0.02f, 0.02f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        SpriteBatch batch = game.batch;
        batch.setProjectionMatrix(camera.combined);

        float vw = camera.viewportWidth;
        float vh = camera.viewportHeight;

        batch.begin();

        // Title
        String title = "THE RETURN OF VENOM";
        font.getData().setScale(2.5f);
        layout.setText(font, title);
        float titleX = (vw - layout.width) / 2f;
        float titleY = vh * 0.75f;
        font.draw(batch, layout, titleX, titleY);

        // Story line
        String story = "A poisonous cure";
        font.getData().setScale(1.0f);
        layout.setText(font, story);
        float storyX = (vw - layout.width) / 2f;
        float storyY = vh * 0.65f;
        font.draw(batch, layout, storyX, storyY);

        if (currentState == MenuState.MAIN_MENU) {
            renderMainMenu(batch, vw, vh);
        } else if (currentState == MenuState.LEVEL_SELECT) {
            renderLevelSelect(batch, vw, vh);
        }

        batch.end();

        handleInput();
    }

    private void renderMainMenu(SpriteBatch batch, float vw, float vh) {
        font.getData().setScale(1.2f);

        // Calculate center position for menu items
        float centerX = vw / 2f;

        // Levels option
        String levelsText = "Levels";
        layout.setText(font, levelsText);
        float levelsX = centerX - layout.width / 2f;
        float levelsY = vh * 0.45f;

        // Set color based on selection
        if (mainMenuSelection == 0) {
            font.setColor(SELECTED_RED);
        } else {
            font.setColor(Color.WHITE);
        }
        font.draw(batch, levelsText, levelsX, levelsY);

        // Settings option
        String settingsText = "Settings";
        layout.setText(font, settingsText);
        float settingsX = centerX - layout.width / 2f;
        float settingsY = vh * 0.35f;

        // Set color based on selection
        if (mainMenuSelection == 1) {
            font.setColor(SELECTED_RED);
        } else {
            font.setColor(Color.WHITE);
        }
        font.draw(batch, settingsText, settingsX, settingsY);

        // Reset color to white for future text
        font.setColor(Color.WHITE);
    }

    private void renderLevelSelect(SpriteBatch batch, float vw, float vh) {
        font.getData().setScale(1.2f);

        // Calculate center position for menu items
        float centerX = vw / 2f;

        // Level 1 option
        String level1Text = "Making it through the night";
        layout.setText(font, level1Text);
        float level1X = centerX - layout.width / 2f;
        float level1Y = vh * 0.50f;

        // Set color based on selection
        if (levelSelection == 0) {
            font.setColor(SELECTED_RED);
        } else {
            font.setColor(Color.WHITE);
        }
        font.draw(batch, level1Text, level1X, level1Y);

        // Test Level option
        String testLevelText = "Test Level";
        layout.setText(font, testLevelText);
        float testLevelX = centerX - layout.width / 2f;
        float testLevelY = vh * 0.40f;

        // Set color based on selection
        if (levelSelection == 1) {
            font.setColor(SELECTED_RED);
        } else {
            font.setColor(Color.WHITE);
        }
        font.draw(batch, testLevelText, testLevelX, testLevelY);

        // Reset color to white for future text
        font.setColor(Color.WHITE);
    }

    private void handleInput() {
        if (currentState == MenuState.MAIN_MENU) {
            handleMainMenuInput();
        } else if (currentState == MenuState.LEVEL_SELECT) {
            handleLevelSelectInput();
        }
    }

    private void handleMainMenuInput() {
        // Navigation
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            mainMenuSelection = (mainMenuSelection - 1 + 2) % 2;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            mainMenuSelection = (mainMenuSelection + 1) % 2;
        }

        // Selection
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (mainMenuSelection == 0) {
                // Levels selected
                currentState = MenuState.LEVEL_SELECT;
            } else if (mainMenuSelection == 1) {
                // Settings selected (placeholder for now)
                // TODO: Implement settings screen
            }
        }
    }

    private void handleLevelSelectInput() {
        // Navigation
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            levelSelection = (levelSelection - 1 + 2) % 2;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            levelSelection = (levelSelection + 1) % 2;
        }

        // Selection
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (levelSelection == 0) {
                // Level 1 selected
                LevelOne levelOne = new LevelOne(game);
                LevelScreen levelScreen = new LevelScreen(game, levelOne);
                game.setScreenWithFade(levelScreen);
            } else if (levelSelection == 1) {
                // Test Level selected
                TestLevel testLevel = new TestLevel(game);
                LevelScreen levelScreen = new LevelScreen(game, testLevel);
                game.setScreenWithFade(levelScreen);
            }
        }

        // Back to main menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            currentState = MenuState.MAIN_MENU;
        }
    }

    @Override
    public void hide() {
        if (introMusic != null && musicExists) {
            introMusic.stop();
        }
    }

    @Override
    public void dispose() {
        if (introMusic != null) {
            introMusic.dispose();
        }
        font.dispose();
    }
}
