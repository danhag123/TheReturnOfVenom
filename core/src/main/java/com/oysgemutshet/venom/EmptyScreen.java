package com.oysgemutshet.venom;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

public class EmptyScreen extends ScreenAdapter {

    @Override
    public void render(float delta) {
        // Just clear the screen with a dark gray color for now
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }
}
