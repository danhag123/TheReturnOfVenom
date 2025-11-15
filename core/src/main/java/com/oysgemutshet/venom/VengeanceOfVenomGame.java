package com.oysgemutshet.venom;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator; // Add this import
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

public class VengeanceOfVenomGame extends Game {

    public SpriteBatch batch;

    // Fade transition
    private enum FadeState { NONE, FADING_OUT, FADING_IN }

    private FadeState fadeState = FadeState.NONE;
    private float fadeAlpha = 0f;
    private static final float FADE_DURATION = 0.5f; // seconds for fade-out and fade-in

    private Screen pendingScreen = null;
    private Texture fadeTexture;

    @Override
    public void create() {
        batch = new SpriteBatch();

        // 1x1 white texture for fade overlay
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        fadeTexture = new Texture(pixmap);
        pixmap.dispose();

        // Start on splash screen (with logo) normally
        super.setScreen(new SplashScreen(this));

    }

    /**
     * Call this instead of setScreen(...) to get a smooth fade transition.
     */
    public void setScreenWithFade(Screen newScreen) {
        if (fadeState != FadeState.NONE) return; // already transitioning, ignore
        pendingScreen = newScreen;
        fadeState = FadeState.FADING_OUT;
        fadeAlpha = 0f;
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        // Render current screen as usual
        super.render();

        // Handle fade transition on top
        if (fadeState != FadeState.NONE) {
            updateFade(delta);
            drawFadeOverlay();
        }
    }

    private void updateFade(float delta) {
        switch (fadeState) {
            case FADING_OUT:
                fadeAlpha += delta / FADE_DURATION;
                if (fadeAlpha >= 1f) {
                    fadeAlpha = 1f;
                    // Fully black now: switch screens
                    if (pendingScreen != null) {
                        super.setScreen(pendingScreen);
                        pendingScreen = null;
                    }
                    // Start fading in
                    fadeState = FadeState.FADING_IN;
                }
                break;

            case FADING_IN:
                fadeAlpha -= delta / FADE_DURATION;
                if (fadeAlpha <= 0f) {
                    fadeAlpha = 0f;
                    fadeState = FadeState.NONE;
                }
                break;

            case NONE:
            default:
                break;
        }
    }

    private void drawFadeOverlay() {
        if (fadeAlpha <= 0f) return;

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        batch.begin();
        batch.setColor(0f, 0f, 0f, fadeAlpha);
        batch.draw(fadeTexture, 0f, 0f, w, h);
        batch.setColor(Color.WHITE);
        batch.end();
    }

    // In VengeanceOfVenomGame.java
    private BitmapFont defaultFont;

    public BitmapFont getDefaultFont() {
        if (defaultFont == null) {
            // Create the font here once
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/RussoOne-Regular.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
            param.size = 28;
            param.color = Color.WHITE;
            defaultFont = generator.generateFont(param);
            generator.dispose();
        }
        return defaultFont;
    }

    @Override
    public void dispose() {
        batch.dispose();
        if (defaultFont != null) {
            defaultFont.dispose();
        }
    }
}
