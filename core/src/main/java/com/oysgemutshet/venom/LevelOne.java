// core/src/main/java/com/oysgemutshet/venom/LevelOne.java
package com.oysgemutshet.venom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class LevelOne extends LevelController {

    private static final float GROUND_TOP_Y = 80f;
    private static final float GROUND_HEIGHT = 30f;

    private enum Phase {
        INTRO,
        GAMEPLAY
    }

    // Data for intro dialogue lines
    private static class IntroLine {
        final String text;
        final float start;     // seconds from level start
        final float duration;  // how long this line is visible
        final boolean center;  // true = center of screen, false = above Venom's head
        final boolean big;     // true = bigger font

        IntroLine(String text, float start, float duration, boolean center, boolean big) {
            this.text = text;
            this.start = start;
            this.duration = duration;
            this.center = center;
            this.big = big;
        }
    }

    // Timeline for the intro
    private static final IntroLine[] INTRO_LINES = new IntroLine[] {
        new IntroLine("They're after us... again...",                       5.0f, 3.0f, false, false),
        new IntroLine("The police... the military...",                      9.0f, 3.0f, false, false),
        new IntroLine("and that goddamn Spider-Man...",                     12.5f, 3.5f, false, false),
        new IntroLine("We protect this city... in our own way... ",        18.0f, 4.0f, false, false),
        new IntroLine("but they won't ever understand that...",                        22.5f, 3.0f, false, false),
        new IntroLine("Well... Let's make it through the night... ",       28.8f, 4.0f, false, false),
        new IntroLine("so we can save this city from itself...",            33.6f, 4.6f, false, false),
        new IntroLine("LEVEL 1 - MAKING IT THROUGH THE NIGHT",               40.8f, 4.5f, true,  true)
    };

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

    // Named platforms (for drawing buildings and truck)
    private Rectangle groundPlatform;
    private Rectangle leftRoofPlatform;
    private Rectangle rightRoofPlatform;
    private Rectangle truckPlatform;

    // Venom projectiles
    private final Array<BlobProjectile> blobs = new Array<>();
    private float blobCooldown = 0f;

    // Enemies & bullets & cars
    private final Array<PoliceOfficer> police = new Array<>();
    private final Array<PoliceBullet> policeBullets = new Array<>();
    private final Array<PoliceCar> policeCars = new Array<>();
    private float spawnTimer = 0f;
    private float policeCarTimer = 40f;

    // Health
    private float playerHealth = 1.0f;

    // Music
    private Music levelMusic;
    private boolean musicStarted = false;
    private float musicDelay = 3f;
    private static final float MUSIC_START_TIME = 62f;
    private boolean musicFileExists = true; // Flag to track if music file exists

    // Damage cooldown
    private float damageCooldown = 0f;
    private static final float DAMAGE_COOLDOWN_TIME = 1f; // 1 second between damage

    // Phase & intro timing
    private Phase phase = Phase.INTRO;
    private float introTime = 0f;

    // Police spawn difficulty tuning
    private static final float EASY_POLICE_SPAWN_INTERVAL = 6f;   // before first car
    private static final float HARD_POLICE_SPAWN_INTERVAL = 2.4f; // after first car (slightly more often)

    private float policeSpawnInterval = EASY_POLICE_SPAWN_INTERVAL;
    private boolean firstPoliceCarSpawned = false;

    // Screen boundaries
    private static final float LEFT_WALL = 0f;
    private final float RIGHT_WALL; // Will be set in constructor based on camera

    public LevelOne(VengeanceOfVenomGame game) {
        this.game = game;
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, 1280, 720);

        // Set right wall based on camera viewport
        this.RIGHT_WALL = camera.viewportWidth;

        this.shapes = new ShapeRenderer();
        this.cityBackground = new Texture(Gdx.files.internal("backgrounds/city_night.png"));

        // Initialize music with error handling
        try {
            FileHandle musicFile = Gdx.files.internal("music/boaz_and_the_dogs.mp3");
            if (musicFile.exists()) {
                this.levelMusic = Gdx.audio.newMusic(musicFile);
                levelMusic.setLooping(false);
                levelMusic.setVolume(0.6f);

                levelMusic.setOnCompletionListener(new Music.OnCompletionListener() {
                    @Override
                    public void onCompletion(Music music) {
                        music.play();
                        music.setPosition(MUSIC_START_TIME);
                    }
                });
            } else {
                this.levelMusic = null;
                this.musicFileExists = false;
                Gdx.app.log("LevelOne", "Music file not found: music/boaz_and_the_dogs.mp3");
            }
        } catch (Exception e) {
            this.levelMusic = null;
            this.musicFileExists = false;
            Gdx.app.error("LevelOne", "Error loading music file", e);
        }

        musicStarted = false;
        musicDelay = 3f;

        // Use your TTF font here – change name if needed
        FileHandle fontFile = Gdx.files.internal("fonts/RussoOne-Regular.ttf");
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 28;
        param.color = Color.WHITE;
        this.font = generator.generateFont(param);
        generator.dispose();

        this.layout = new GlyphLayout();

        rightRoofPlatform = new Rectangle(850, 380, 200, 20);
        platforms.add(rightRoofPlatform);

        // Venom starts on top of the highest roof, near the right edge, facing right
        float venomStartX = rightRoofPlatform.x + rightRoofPlatform.width - 50f; // 50 is a small margin
        float venomStartY = rightRoofPlatform.y + rightRoofPlatform.height;
        this.venom = new VenomPlayer(venomStartX, venomStartY);

        this.spawnTimer = 0f;
        this.playerHealth = 1.0f;

        // Ground
        groundPlatform = new Rectangle(
            0,
            GROUND_TOP_Y - GROUND_HEIGHT,
            1280,
            GROUND_HEIGHT
        );
        platforms.add(groundPlatform);

        // Truck collider: solid block from street up to the roof
        float truckX      = 180f;
        float truckWidth  = 200f;
        float truckHeight = 100f; // adjust to taste; controls how tall the truck is

        truckPlatform = new Rectangle(truckX, GROUND_TOP_Y, truckWidth, truckHeight);
        platforms.add(truckPlatform);

        // Mid / high rooftops
        leftRoofPlatform = new Rectangle(500, 280, 220, 20);
        platforms.add(leftRoofPlatform);
    }

    @Override
    public void update(float delta) {
        if (phase == Phase.INTRO) {
            updateIntro(delta);
        } else {
            updateGameplay(delta);
        }
    }

    @Override
    public void render(OrthographicCamera camera, ShapeRenderer shapes, SpriteBatch batch, BitmapFont font, GlyphLayout layout) {
        float vw = camera.viewportWidth;
        float vh = camera.viewportHeight;

        // 1) Draw background
        batch.begin();
        batch.draw(cityBackground, 0, 0, vw, vh); // stretch to fill screen
        batch.end();

        // 2) Draw world: platforms, Venom, blobs, police, bullets, health bar
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Ground street
        renderGround(shapes, groundPlatform);

        // Rooftops
        renderBuildingRoof(shapes, leftRoofPlatform);
        renderBuildingRoof(shapes, rightRoofPlatform);

        // Big city object: truck
        renderTruckPlatform(shapes, truckPlatform);

        // Venom (he sets his own colors)
        venom.render(shapes);

        // Symbiote blobs
        for (BlobProjectile blob : blobs) {
            blob.render(shapes);
        }

        // Police officers
        for (PoliceOfficer officer : police) {
            officer.render(shapes);
        }

        // Police bullets
        for (PoliceBullet bullet : policeBullets) {
            bullet.render(shapes);
        }

        // Police cars
        for (PoliceCar car : policeCars) {
            car.render(shapes);
        }

        // Health bar (top-left)
        float barWidth  = 200f;
        float barHeight = 18f;
        float barX = 20f;
        float barY = vh - 40f;

        // Bar background
        shapes.setColor(0.2f, 0.2f, 0.25f, 1f);
        shapes.rect(barX, barY, barWidth, barHeight);

        // Current health (playerHealth = 0..1)
        shapes.setColor(0.7f, 0.1f, 0.1f, 1f);
        shapes.rect(barX, barY, barWidth * playerHealth, barHeight);

        shapes.end();

        batch.begin();

        if (phase == Phase.GAMEPLAY) {
            // show help only during gameplay
            font.getData().setScale(0.8f);
            String help = "Move: A / D or Arrows   Jump: SPACE or UP  F: Blob   G: Tongue   ESC: Back to Main Menu";
            layout.setText(font, help);
            font.draw(batch, layout, (vw - layout.width) / 2f, 30);
        } else {
            // INTRO: draw cinematic text above Venom / center
            renderIntroText(batch, vw, vh);
        }

        batch.end();
    }

    @Override
    public void dispose() {
        shapes.dispose();
        font.dispose();
        cityBackground.dispose();
        if (levelMusic != null) {
            levelMusic.dispose();
        }
    }

    @Override
    public void hide() {
        if (levelMusic != null && levelMusic.isPlaying()) {
            levelMusic.stop();
        }
    }

    // All the private helper methods remain the same...
    private void renderIntroText(SpriteBatch batch, float vw, float vh) {
        if (phase != Phase.INTRO) return;

        IntroLine active = null;
        float alpha = 0f;

        // Find which line should be visible and its alpha (for fade in/out)
        for (IntroLine line : INTRO_LINES) {
            float t = introTime - line.start;
            if (t < 0f || t > line.duration) continue;

            float fade = line.big ? 1.0f : 0.7f; // longer fade for big center title
            float a;
            if (t < fade) {
                a = t / fade; // fade in
            } else if (t > line.duration - fade) {
                a = (line.duration - t) / fade; // fade out
            } else {
                a = 1f; // fully visible
            }

            if (a > alpha) {
                alpha = a;
                active = line;
            }
        }

        if (active == null || alpha <= 0f) return;

        // Set font style based on line type
        if (active.big) {
            font.getData().setScale(2.0f);
        } else {
            font.getData().setScale(0.9f);
        }
        font.setColor(1f, 1f, 1f, alpha);

        layout.setText(font, active.text);

        float x, y;

        if (active.center) {
            // Center of the screen
            x = (vw - layout.width) / 2f;
            y = vh * 0.6f;
        } else {
            // Above Venom's head
            Rectangle vb = venom.getBounds();
            float centerX = vb.x + vb.width * 0.5f;
            x = centerX - layout.width * 0.5f;
            y = vb.y + vb.height + 45f;
        }

        font.draw(batch, layout, x, y);

        // reset color
        font.setColor(1f, 1f, 1f, 1f);
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

    // Draw a tall building with a rooftop platform
    private void renderBuildingRoof(ShapeRenderer shapes, Rectangle platform) {
        // Building body from ground up to the platform
        float buildingX = platform.x + platform.width * 0.1f;
        float buildingW = platform.width * 0.8f;
        float buildingBottomY = GROUND_TOP_Y;
        float buildingHeight = platform.y - buildingBottomY;

        // Body: dark bluish-gray
        shapes.setColor(0.6f, 0.6f, 0.6f, 1f);
        shapes.rect(buildingX, buildingBottomY, buildingW, buildingHeight);

        // Windows: simple grid
        shapes.setColor(0.9f, 0.9f, 0.8f, 1f);
        int cols = 4;
        int rows = Math.max(3, (int)(buildingHeight / 60f));
        float marginX = buildingW * 0.12f;
        float marginY = buildingHeight * 0.10f;
        float cellW = (buildingW - marginX * 2f) / cols;
        float cellH = (buildingHeight - marginY * 2f) / rows;
        float windowW = cellW * 0.55f;
        float windowH = cellH * 0.45f;

        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                float wx = buildingX + marginX + c * cellW + cellW * 0.2f;
                float wy = buildingBottomY + marginY + r * cellH + cellH * 0.2f;
                shapes.rect(wx, wy, windowW, windowH);
            }
        }

        // Rooftop itself = the platform top
        shapes.setColor(0.3f, 0.32f, 0.38f, 1f);
        shapes.rect(platform.x, platform.y, platform.width, platform.height);

        // Rooftop lip / railing
        shapes.setColor(0.75f, 0.78f, 0.85f, 1f);
        shapes.rect(platform.x, platform.y + platform.height - 3f, platform.width, 3f);
    }

    // Draw a big truck that Venom can stand on
    private void renderTruckPlatform(ShapeRenderer shapes, Rectangle platform) {
        // Truck from street (GROUND_TOP_Y) up to platform top (roof)
        float truckBottomY = GROUND_TOP_Y;
        float truckTopY    = platform.y + platform.height; // roof level
        float truckHeight  = truckTopY - truckBottomY;

        float truckX = platform.x;
        float truckW = platform.width;

        // --- Wheels (partly under the body) ---
        float wheelR       = truckHeight * 0.22f;
        float wheelCenterY = truckBottomY + wheelR * 0.4f; // tucked into body a bit

        shapes.setColor(Color.BLACK);
        // front wheel
        shapes.circle(truckX + truckW * 0.22f, wheelCenterY, wheelR);
        // back wheel
        shapes.circle(truckX + truckW * 0.78f, wheelCenterY, wheelR);

        // --- Body base ---
        float bodyBottomY = truckBottomY + wheelR * 0.6f; // sits above wheels
        float bodyTopY    = truckTopY;
        float bodyHeight  = bodyTopY - bodyBottomY;

        // --- CAB (small front part) ---
        float cabW = truckW * 0.26f;
        float cabX = truckX + truckW * 0.06f;
        float cabY = bodyBottomY;
        float cabH = bodyHeight * 0.85f;

        // cab body
        shapes.setColor(0.97f, 0.9f, 0.9f, 1f);
        shapes.rect(cabX, cabY, cabW, cabH);

        // cab bumper
        shapes.setColor(0.3f, 0.3f, 0.35f, 1f);
        shapes.rect(cabX - cabW * 0.15f, cabY + cabH * 0.15f,
            cabW * 0.15f, cabH * 0.5f);

        // cab window
        shapes.setColor(0.6f, 0.8f, 1f, 1f);
        shapes.rect(cabX + cabW * 0.12f, cabY + cabH * 0.4f,
            cabW * 0.7f, cabH * 0.45f);

        // --- CARGO BOX (big, taller than cab, no windows) ---
        float boxX = cabX + cabW + truckW * 0.02f;
        float boxW = truckX + truckW - boxX;
        float boxY = bodyBottomY;
        float boxH = bodyHeight; // as tall as full body → taller than cab

        // solid color cargo box
        shapes.setColor(0.95f, 0.6f, 0.2f, 1f); // orange-ish
        shapes.rect(boxX, boxY, boxW, boxH);

        // subtle horizontal stripes
        shapes.setColor(0.85f, 0.5f, 0.15f, 1f);
        int stripes = 4;
        float stripeGap = boxH / (stripes + 2);
        for (int i = 1; i <= stripes; i++) {
            float y = boxY + i * stripeGap;
            shapes.rect(boxX, y, boxW, 2f);
        }
    }

    private void updateIntro(float delta) {
        introTime += delta;

        // Allow skipping intro with SPACE (optional; remove if you don't want this)
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            skipToGameplay();
            return;
        }

        // When intro is done, switch to gameplay
        IntroLine last = INTRO_LINES[INTRO_LINES.length - 1];
        float endTime = last.start + last.duration;
        if (introTime > endTime + 0.5f) {
            skipToGameplay();
        }
    }

    private void skipToGameplay() {
        phase = Phase.GAMEPLAY;

        // reset timers so gameplay starts fresh
        spawnTimer = 0f;
        policeCarTimer = 38f;

        // difficulty reset
        firstPoliceCarSpawned = false;
        policeSpawnInterval = EASY_POLICE_SPAWN_INTERVAL;

        // music
        musicStarted = false;
        musicDelay = 1.0f;
    }

    private void updateGameplay(float delta) {
        // Delayed music start - only if music file exists
        if (!musicStarted && levelMusic != null && musicFileExists) {
            musicDelay -= delta;
            if (musicDelay <= 0f) {
                try {
                    levelMusic.play();
                    levelMusic.setPosition(MUSIC_START_TIME); // jump to 1:02 right after starting
                    musicStarted = true;
                } catch (Exception e) {
                    Gdx.app.error("LevelOne", "Error playing music", e);
                    musicFileExists = false; // Disable music if playback fails
                }
            }
        }

        venom.update(delta, platforms);

        // ENFORCE SCREEN BOUNDARIES - Add this section
        enforceScreenBoundaries();

        // Blob shooting cooldown
        if (blobCooldown > 0f) {
            blobCooldown -= delta;
        }

        // Damage cooldown (needed so we don't run out of health as soon as e.g. touching police)
        if (damageCooldown > 0f) {
            damageCooldown -= delta;
        }

        // Spawn policemen (interval depends on difficulty)
        spawnTimer -= delta;
        if (spawnTimer <= 0f) {
            spawnTimer = policeSpawnInterval;

            boolean fromLeft = Math.random() < 0.5;
            int dir = fromLeft ? 1 : -1;
            float startX = fromLeft ? -40f : camera.viewportWidth + 40f;
            float groundY = GROUND_TOP_Y;

            // Before first police car: 1 bullet per burst; after: 3 bullets
            int burstSize = firstPoliceCarSpawned ? 3 : 1;

            police.add(new PoliceOfficer(startX, groundY, dir, burstSize));
        }

        // Update police & their bullets
        for (int i = police.size - 1; i >= 0; i--) {
            PoliceOfficer o = police.get(i);
            o.update(delta, policeBullets);
            if (!o.isAlive() || o.isOffscreen(camera.viewportWidth)) {
                police.removeIndex(i);
            }
        }

        for (int i = policeBullets.size - 1; i >= 0; i--) {
            PoliceBullet b = policeBullets.get(i);
            b.update(delta);
            if (!b.isAlive()) {
                policeBullets.removeIndex(i);
            }
        }

        // Spawn police car every 38 seconds (first one after ~40s)
        policeCarTimer -= delta;
        if (policeCarTimer <= 0f) {
            policeCarTimer = 38f;

            boolean fromLeft = Math.random() < 0.5;
            int dir = fromLeft ? 1 : -1;
            float startX = fromLeft ? -150f : camera.viewportWidth + 150f;

            policeCars.add(new PoliceCar(startX, GROUND_TOP_Y, dir));

            // First police car just spawned → step up difficulty
            if (!firstPoliceCarSpawned) {
                firstPoliceCarSpawned = true;
                policeSpawnInterval = HARD_POLICE_SPAWN_INTERVAL;
            }
        }

        // Update police cars
        for (int i = policeCars.size - 1; i >= 0; i--) {
            PoliceCar car = policeCars.get(i);
            car.update(delta);
            if (!car.isAlive() || car.isOffscreen(camera.viewportWidth)) {
                policeCars.removeIndex(i);
            }
        }

        Rectangle venomBounds = venom.getBounds();

        // Police bullets hit Venom
        for (int i = policeBullets.size - 1; i >= 0; i--) {
            PoliceBullet b = policeBullets.get(i);
            if (b.isAlive() && b.getBounds().overlaps(venomBounds)) {
                b.destroy();
                playerHealth -= 0.05f;
                if (playerHealth < 0f) playerHealth = 0f;
            }
        }

        // Police car hits Venom = instant death
        for (PoliceCar car : policeCars) {
            if (car.isAlive() && car.getBounds().overlaps(venomBounds)) {
                playerHealth = 0f; // instant kill
                break;
            }
        }

        // Venom touches police
        for (PoliceOfficer o : police) {
            if (o.isAlive() && o.getBounds().overlaps(venomBounds) && damageCooldown <= 0f) {
                playerHealth -= 0.05f;
                if (playerHealth < 0f) playerHealth = 0f;
                damageCooldown = DAMAGE_COOLDOWN_TIME; // Reset cooldown
                break; // Only take damage from one officer per frame
            }
        }

        // Venom blobs hit police
        for (int i = blobs.size - 1; i >= 0; i--) {
            BlobProjectile blob = blobs.get(i);
            if (!blob.isAlive()) continue;

            Rectangle blobRect = blob.getBounds();
            for (PoliceOfficer o : police) {
                if (o.isAlive() && o.getBounds().overlaps(blobRect)) {
                    o.registerHit();
                    blob.destroy();
                    break;
                }
            }
        }

        // Venom tongue hits police
        Rectangle tongueRect = venom.getTongueHitbox();
        if (tongueRect != null) {
            for (PoliceOfficer o : police) {
                if (o.isAlive() && o.getBounds().overlaps(tongueRect)) {
                    o.registerHit();
                }
            }
        }

        // Check game over
        if (playerHealth <= 0f) {
            game.setScreen(new GameOverScreen(game));
            return;
        }

        // Shoot blob with F
        if (Gdx.input.isKeyJustPressed(Input.Keys.F) && blobCooldown <= 0f) {
            float mx = venom.getMouthX();
            float my = venom.getMouthY();
            int dir = venom.isFacingRight() ? 1 : -1;

            blobs.add(new BlobProjectile(mx, my, dir));
            blobCooldown = 0.25f; // 4 blobs per second max
        }

        // Update blobs and remove dead ones
        for (int i = blobs.size - 1; i >= 0; i--) {
            BlobProjectile b = blobs.get(i);
            b.update(delta);
            if (!b.isAlive()) {
                blobs.removeIndex(i);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreenWithFade(new IntroScreen(game));
        }
    }

    private void enforceScreenBoundaries() {
        Rectangle venomBounds = venom.getBounds();

        // Left boundary - prevent going off left side of screen
        if (venomBounds.x < LEFT_WALL) {
            venomBounds.x = LEFT_WALL;
            // Stop horizontal movement when hitting the wall
            venom.getVelocity().x = 0;
        }

        // Right boundary - prevent going off right side of screen
        if (venomBounds.x + venomBounds.width > RIGHT_WALL) {
            venomBounds.x = RIGHT_WALL - venomBounds.width;
            // Stop horizontal movement when hitting the wall
            venom.getVelocity().x = 0;
        }
    }
}
