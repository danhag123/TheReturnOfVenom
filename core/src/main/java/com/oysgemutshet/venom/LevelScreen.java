// core/src/main/java/com/oysgemutshet/venom/LevelScreen.java
package com.oysgemutshet.venom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class LevelScreen extends ScreenAdapter {

    private final VengeanceOfVenomGame game;
    private final LevelController controller;

    private final OrthographicCamera camera;
    private final ShapeRenderer shapes;
    private final BitmapFont font;
    private final GlyphLayout layout;

    public LevelScreen(VengeanceOfVenomGame game, LevelController controller) {
        this.game = game;
        this.controller = controller;

        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, 1280, 720);

        this.shapes = new ShapeRenderer();

        // Generic game font (you can reuse your Anton or whatever)
        FileHandle fontFile = Gdx.files.internal("fonts/RussoOne-Regular.ttf"); // change if needed
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 28;
        param.color = Color.WHITE;
        this.font = generator.generateFont(param);
        generator.dispose();

        this.layout = new GlyphLayout();
    }

    @Override
    public void render(float delta) {
        controller.update(delta);

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        ShapeRenderer sr = shapes;
        SpriteBatch batch = game.batch;

        sr.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        controller.render(camera, sr, batch, font, layout);
    }

    @Override
    public void hide() {
        // This gets called automatically when switching away from this screen
        controller.hide();
    }

    @Override
    public void dispose() {
        shapes.dispose();
        font.dispose();
        controller.dispose();
    }
}
