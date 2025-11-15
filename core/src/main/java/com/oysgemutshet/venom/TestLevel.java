// core/src/main/java/com/oysgemutshet/venom/TestLevel.java
package com.oysgemutshet.venom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class TestLevel extends LevelController {

    private static final float GROUND_TOP_Y = 80f;
    private static final float GROUND_HEIGHT = 30f;

    // Core
    private final VengeanceOfVenomGame game;
    private final OrthographicCamera camera;
    private final ShapeRenderer shapes;
    private final BitmapFont font;
    private final GlyphLayout layout;
    private final Texture cityBackground;

    // Player & world
    private final VenomPlayer venom;
    private final Array<Rectangle> platforms = new Array<>();

    // Named platforms
    private Rectangle groundPlatform;

    // Health
    private float playerHealth = 1.0f;

    public TestLevel(VengeanceOfVenomGame game) {
        this.game = game;
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, 1280, 720);

        this.shapes = new ShapeRenderer();
        this.cityBackground = new Texture(Gdx.files.internal("backgrounds/city_night.png"));

        // Use your TTF font
        this.font = game.getDefaultFont(); // Or create new font like in LevelOne
        this.layout = new GlyphLayout();

        // Ground
        groundPlatform = new Rectangle(
            0,
            GROUND_TOP_Y - GROUND_HEIGHT,
            1280,
            GROUND_HEIGHT
        );
        platforms.add(groundPlatform);

        // Venom starts on ground
        float venomStartX = 100f;
        float venomStartY = GROUND_TOP_Y;
        this.venom = new VenomPlayer(venomStartX, venomStartY);

        this.playerHealth = 1.0f;
    }

    @Override
    public void update(float delta) {
        venom.update(delta, platforms);

        // Simple test level logic - just check for ESC to go back
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreenWithFade(new IntroScreen(game));
        }

        // Simple health drain for testing (remove this in final version)
        playerHealth -= 0.001f * delta;
        if (playerHealth <= 0f) {
            playerHealth = 0f;
            // Could add game over logic here if desired
        }
    }

    @Override
    public void render(OrthographicCamera camera, ShapeRenderer shapes, SpriteBatch batch, BitmapFont font, GlyphLayout layout) {
        float vw = camera.viewportWidth;
        float vh = camera.viewportHeight;

        // Draw background
        batch.begin();
        batch.draw(cityBackground, 0, 0, vw, vh);
        batch.end();

        // Draw world
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Ground
        renderGround(shapes, groundPlatform);

        // Venom
        venom.render(shapes);

        // Health bar
        float barWidth = 200f;
        float barHeight = 18f;
        float barX = 20f;
        float barY = vh - 40f;

        // Bar background
        shapes.setColor(0.2f, 0.2f, 0.25f, 1f);
        shapes.rect(barX, barY, barWidth, barHeight);

        // Current health
        shapes.setColor(0.7f, 0.1f, 0.1f, 1f);
        shapes.rect(barX, barY, barWidth * playerHealth, barHeight);

        shapes.end();

        // Draw UI text
        batch.begin();

        // Test level title
        font.getData().setScale(1.5f);
        String title = "TEST LEVEL";
        layout.setText(font, title);
        font.draw(batch, layout, (vw - layout.width) / 2f, vh - 50f);

        // Instructions
        font.getData().setScale(0.8f);
        String help = "Move: A/D or Arrows   Jump: SPACE or UP   F: Blob   G: Tongue";
        layout.setText(font, help);
        font.draw(batch, layout, (vw - layout.width) / 2f, 60f);

        String escHelp = "ESC: Back to Main Menu";
        layout.setText(font, escHelp);
        font.draw(batch, layout, (vw - layout.width) / 2f, 30f);

        batch.end();
    }

    // Draw the main ground as a street
    private void renderGround(ShapeRenderer shapes, Rectangle p) {
        // Street base
        shapes.setColor(0.18f, 0.18f, 0.22f, 1f);
        shapes.rect(p.x, p.y, p.width, p.height);

        // Road stripe
        float stripeY = p.y + p.height * 0.4f;
        shapes.setColor(0.9f, 0.9f, 0.6f, 1f);
        float dashWidth = 40f;
        float gap = 30f;
        for (float x = p.x; x < p.x + p.width; x += dashWidth + gap) {
            shapes.rect(x, stripeY, dashWidth, p.height * 0.15f);
        }
    }

    @Override
    public void dispose() {
        shapes.dispose();
        cityBackground.dispose();
        // Don't dispose font here since it's shared or created by LevelScreen
    }

    @Override
    public void hide() {
        // No music to stop in test level
    }
}
