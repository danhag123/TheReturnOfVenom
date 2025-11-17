package com.oysgemutshet.venom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.audio.Music;

public class GameOverScreen extends ScreenAdapter {

    private final VengeanceOfVenomGame game;
    private final OrthographicCamera camera;
    private final BitmapFont font;
    private final GlyphLayout layout;

    private Music gameOverMusic;
    private float gameOverMusicTime = 0f;
    private boolean musicFileExists = true;

    private static final float GO_MUSIC_MAX_TIME = 60f;
    private static final float GO_MUSIC_TARGET_VOLUME = 0.6f;
    private static final float GO_MUSIC_FADE_SPEED = 0.5f;

    public GameOverScreen(VengeanceOfVenomGame game) {
        this.game = game;
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, 1280, 720);

        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/russoone-regular.ttf"));
        FreeTypeFontParameter params = new FreeTypeFontParameter();
        params.size = 32;
        params.color = Color.WHITE;

        this.font = generator.generateFont(params);
        generator.dispose();

        this.layout = new GlyphLayout();

        // Initialize music with error handling
        try {
            com.badlogic.gdx.files.FileHandle musicFile = Gdx.files.internal("music/boaz_and_the_dogs.mp3");
            if (musicFile.exists()) {
                this.gameOverMusic = Gdx.audio.newMusic(musicFile);
                gameOverMusic.setLooping(false);
            } else {
                this.gameOverMusic = null;
                this.musicFileExists = false;
                Gdx.app.log("GameOverScreen", "Music file not found: music/boaz_and_the_dogs.mp3");
            }
        } catch (Exception e) {
            this.gameOverMusic = null;
            this.musicFileExists = false;
            Gdx.app.error("GameOverScreen", "Error loading music file", e);
        }
    }

    @Override
    public void show() {
        if (gameOverMusic != null && musicFileExists) {
            try {
                gameOverMusic.setVolume(0f);
                gameOverMusic.setPosition(0f);
                gameOverMusic.play();
                gameOverMusicTime = 0f;
            } catch (Exception e) {
                Gdx.app.error("GameOverScreen", "Error playing music", e);
                musicFileExists = false;
            }
        }
    }

    @Override
    public void render(float delta) {
        // Music handling - only if music file exists and is playing
        if (gameOverMusic != null && musicFileExists && gameOverMusic.isPlaying()) {
            gameOverMusicTime += delta;

            // Loop first minute
            if (gameOverMusicTime > GO_MUSIC_MAX_TIME) {
                gameOverMusicTime = 0f;
                gameOverMusic.stop();
                gameOverMusic.setPosition(0f);
                gameOverMusic.play();
            }

            // Fade in
            float v = gameOverMusic.getVolume();
            if (v < GO_MUSIC_TARGET_VOLUME) {
                v = Math.min(GO_MUSIC_TARGET_VOLUME, v + GO_MUSIC_FADE_SPEED * delta);
                gameOverMusic.setVolume(v);
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

        // Big title
        font.getData().setScale(2.0f);
        String title = "GAME OVER";
        layout.setText(font, title);
        font.draw(batch, layout, (vw - layout.width) / 2f, vh * 0.62f);

        // Venom-y line
        font.getData().setScale(1.0f);
        String msg = "We were overwhelmed.";
        layout.setText(font, msg);
        font.draw(batch, layout, (vw - layout.width) / 2f, vh * 0.48f);

        // Bojacky line
        String msg2 = "But every cloud has a silver lining. Even mushroom clouds.";
        layout.setText(font, msg2);
        font.draw(batch, layout, (vw - layout.width) / 2f, vh * 0.42f);

        // Instructions
        String retry = "Press R to replay Level 1   |   Press ESC to return to intro";
        layout.setText(font, retry);
        font.draw(batch, layout, (vw - layout.width) / 2f, vh * 0.30f);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            // Restart Level 1
            game.setScreenWithFade(new LevelScreen(game, new LevelOne(game)));
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            // Back to intro
            game.setScreenWithFade(new IntroScreen(game));
        }
    }

    @Override
    public void hide() {
        if (gameOverMusic != null && gameOverMusic.isPlaying()) {
            gameOverMusic.stop();
        }
    }

    @Override
    public void dispose() {
        if (gameOverMusic != null) {
            gameOverMusic.dispose();
        }
        font.dispose();
    }
}
