// core/src/main/java/com/oysgemutshet/venom/LevelController.java
package com.oysgemutshet.venom;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public abstract class LevelController {
    public abstract void update(float delta);
    public abstract void render(OrthographicCamera camera, ShapeRenderer shapes, SpriteBatch batch, BitmapFont font, GlyphLayout layout);
    public abstract void dispose();
    public void hide() {} // Optional method for cleanup when screen is hidden
}
