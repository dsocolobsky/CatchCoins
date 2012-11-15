package com.dysoco.gdxgame;

import java.util.Iterator;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class GdxGame implements ApplicationListener {
	Texture shipImage; // Texture for ship
	Texture coinImage; // Texture for coins
	Texture enemyImage; // Texture for enemies
	
	BitmapFont font; // Font to display
	SpriteBatch batch; // Batch helper
	OrthographicCamera camera; // A camera
	
	Ship player; // The main player
	Array<Coin> coinList; // Store coins
	Array<Enemy> enemyList; // Store enemies
	
	long lastSpawnTime; // Last time coin has spawned
	long compareTo;
	int speed; // Speed for coins and enemies
	
	int hits; // Coins we got
	int fails; // Coins we missed
	int score; // hits - fails	
	int prob; // Probability to spawn enemy
	
	@Override
	public void create() {
		// Load images
		shipImage = new Texture(Gdx.files.local("ship.png"));
		coinImage = new Texture(Gdx.files.local("coin.png"));
		enemyImage = new Texture(Gdx.files.local("badcoin.png"));
		
		// Create and set camera
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 600);
		
		// Set SpriteBatch
		batch = new SpriteBatch();
		
		// Set font
		font = new BitmapFont();
		
		// Set speed
		speed = 200;
		
		// Set some score related vars
		hits = 0;
		fails = 0;
		score = 0;
		prob = 5; // 1 out of 5 times spawn enemy
		compareTo = 1000000000;
		
		// Create player
		player = new Ship();
		
		player.x = 800/2 - 48/2;
		player.y = 20;
		player.width = 48;
		player.height = 48;
		
		// Create coins and enemies
		coinList = new Array<Coin>();
		enemyList = new Array<Enemy>();
		spawnCoin();
	}
	
	public void spawnCoin(){
		Coin coin = new Coin();
		
		coin.x = MathUtils.random(0, 800-48);
		coin.y = 480;
		coin.width = 48;
		coin.height = 48;

		coinList.add(coin);
		lastSpawnTime = TimeUtils.nanoTime();
	}
	
	public void spawnEnemy(){
		Enemy enemy = new Enemy();
		
		enemy.x = MathUtils.random(0, 800-48);
		enemy.y = 480;
		enemy.width = 48;
		enemy.height = 48;
		
		enemyList.add(enemy);
		lastSpawnTime = TimeUtils.nanoTime();
	}
	
	@Override
	public void render(){
		// Clear the screen to blue
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		// Update the camera
		camera.update();
		
		// Set up SpriteBatch
		batch.setProjectionMatrix(camera.combined);
		
		// START BATCH
		//
		batch.begin();
		
			// Draw player
			batch.draw(shipImage, player.x, player.y);
			
			// Draw coins
			for(Coin coin: coinList){
				batch.draw(coinImage, coin.x, coin.y);
			}
			
			// Draw enemies
			for(Enemy enemy: enemyList){
				batch.draw(enemyImage, enemy.x, enemy.y);
			}
			
			// Print info to screen
			font.draw(batch, "Score: " + score, 20, 590);
			font.draw(batch, "Hits: " + hits, 120, 590);
			font.draw(batch, "Misses: " + fails, 220, 590);
			
		batch.end();
		//
		// END BATCH
		
		// Get input from touchscreen/mouse
		if(Gdx.input.isTouched()){
			Vector3 touchPos = new Vector3();
			
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			player.x = touchPos.x - 48/2;
		}
		
		// Get input from keyboard
		if(Gdx.input.isKeyPressed(Keys.LEFT)){
			player.x -= 200 * Gdx.graphics.getDeltaTime();
		} else if(Gdx.input.isKeyPressed(Keys.RIGHT)){
			player.x += 200 * Gdx.graphics.getDeltaTime();
		} else if(Gdx.input.isKeyPressed(Keys.SPACE)){
			score = 19;
		}
		
		// Bound Checking
		if(player.x < 0) player.x = 0;
		if(player.x > 800 - 48) player.x = 800 - 48;
		
		// Calculate score
		score = hits - fails;
		if(score < 0) score = 0;
		
		
		
		// Spawn new coin or enemy if needed
		if(TimeUtils.nanoTime() - lastSpawnTime > compareTo){
			if(MathUtils.random(1, prob) == 1){
				spawnEnemy();
			} else {
				spawnCoin();
			}
		}
		
		// Update the coins and enemies
		Iterator<Coin> iter = coinList.iterator();
		while(iter.hasNext()){
			Coin coin = iter.next();

			coin.y -= speed * Gdx.graphics.getDeltaTime(); // Move coin down

			// If coin falls
			if(coin.y + 48 < 0){ 
				iter.remove(); // Delete it
				fails++; // Augment fails
			}
			
			// If player gets coin
			if(coin.overlaps(player)){ 
				iter.remove(); // Remove coin
				hits++; // Augment hits
			}
		}
		
		// Update enemies
		Iterator<Enemy> eIter = enemyList.iterator();
		while(eIter.hasNext()){
			Enemy enemy = eIter.next();
			
			enemy.y -= speed * Gdx.graphics.getDeltaTime(); // Move enemy down
			
			// If enemy falls
			if(enemy.y + 48 < 0){
				eIter.remove(); // Delete it
			}
			
			// If player hits enemy
			if(enemy.overlaps(player)){ 
				eIter.remove(); // Remove enemy
				// Reset all score
				hits = 0;
				fails = 0;
				score = 0; 
			}
		}
		
		// Switch between levels
		switch(score){
		case 5:
			speed += 2;
			compareTo = 900000000;
			score++;
			break;
		case 10:
			speed += 2;
			compareTo = 800000000;
			score++;
			break;
		case 20:
			speed += 2;
			compareTo = 700000000;
			score++;
			break;
		case 30:
			speed += 2;
			compareTo = 600000000;
			score++;
			break;
		case 100:
			score++;
			compareTo = 500000000;
			break;
		}
		
		
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	// Called when game is paused (Home screen or so)
	// Good method to save state in
	public void pause() {
	}

	@Override
	// Only used in Android, resume from pause
	public void resume() {
	}

	
	// Cleanup
	@Override
	public void dispose() {
		shipImage.dispose();
		enemyImage.dispose();
		coinImage.dispose();
		font.dispose();
	}
}
