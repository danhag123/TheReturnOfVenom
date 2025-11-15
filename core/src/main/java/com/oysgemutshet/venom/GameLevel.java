package com.oysgemutshet.venom;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public interface GameLevel {
    // Text shown at the top, e.g. "LEVEL 1 - VENOM'S HUNT"
    String getTitle();

    // Path inside assets/ to the background texture
    String getBackgroundTexturePath();

    // Fill the platforms list with this level's platforms
    void buildPlatforms(Array<Rectangle> platforms);

    // Where Venom starts
    Vector2 getPlayerStart();
}
