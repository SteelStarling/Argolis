package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.maps.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import java.lang.Math;

public class ArgolisGame extends ApplicationAdapter {

	//setup final values to relate to each direction
	public final int N = 0;
	public final int NE = 1;
	public final int E = 2;
	public final int SE = 3;
	public final int S = 4;
	public final int SW = 5;
	public final int W = 6;
	public final int NW = 7;

	private SpriteBatch batch;
	private Texture player;
	private TiledMapTileSet dungeonTileMap;
	private TiledMap map;
	private OrthogonalTiledMapRenderer renderer;
	private OrthographicCamera camera;

	@Override
	public void create () {
		batch = new SpriteBatch();
		player = new Texture(Gdx.files.internal("BasicLad.png"));

		//get game size
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		//create tilemap
		Texture tileMapDungeon = new Texture(Gdx.files.internal("Dungeon_Tileset_Fixed.png")); //get tileMap for dungeon
		TextureRegion[][] splitTiles = TextureRegion.split(tileMapDungeon, 16, 16); //split tileMap
		dungeonTileMap = new TiledMapTileSet(); //setup tileMap

        //fill tilemap
        int tileID = 0;
        for(int row = 0; row < 10; row++) {
            for(int col = 0; col < 10; col++) {
                TiledMapTile t = new StaticTiledMapTile(splitTiles[row][col]); //fill with tiles
                t.setId(tileID); //set tile ID
                dungeonTileMap.putTile(tileID, t); //add tile to map
                tileID++; //inc ID
            }
        }

        map = new TiledMap();
		MapLayers layers = map.getLayers();
		int mapWidth = 100;
		int mapHeight = 100;
		int tileSize = 16;
		TiledMapTileLayer layer = new TiledMapTileLayer(mapWidth, mapHeight, tileSize, tileSize); //create layer 100x100, 16x16 tiles
		//loop through and fill layer
		for(int x = 0; x < mapWidth; x++){
			for(int y = 0; y < mapHeight; y++){
				Cell cell = new Cell();
				cell.setTile(getRandomCenterFloor()); //set cell to random tile
				layer.setCell(x, y, cell); //set cell on layer
			}
		}
		layers.add(layer); //add layer to layers

		renderer = new OrthogonalTiledMapRenderer(map, 1/16f);

		//create camera
		camera = new OrthographicCamera(30,30 * (h / w)); //change height to match
		camera.update();
	}

	public void interpretWorld(String str){
		String[] layers = str.split("\\n"); //split by layer
		char[][] baseChars = new char[layers.length][]; //create new empty array to store chars
		//fill world with chars
		for(int i = 0; i < layers.length; i++){
			baseChars[i] = layers[i].toCharArray();
		}

		//convert to worldChars (rectangular array from ragged)
		int maxLen = 0;
		for(char[] c : baseChars){ //get max length
			if(c.length > maxLen){
				maxLen = c.length; //update
			}
		}

		char[][] worldChars = new char[layers.length][maxLen]; //create char array of correct size
		for(int row = 0; row < worldChars.length; row++){ //loop through and fill
			for(int col = 0; col < worldChars[0].length; col++){
				if((row < baseChars.length) && (col < baseChars[row].length)){ //if in bounds
					worldChars[row][col] = baseChars[row][col];
				}
				else{ //if out of bounds, replace with wall background
					worldChars[row][col] = '#'; //replace with wall
				}
			}
		}

		int[][] worldTiles = new int[worldChars.length][worldChars[0].length]; //create int array
        for(int row = 0; row < worldTiles.length; row++){
            for(int col = 0; col < worldTiles[0].length; col++){
				boolean isWall = (worldChars[row][col] == '#'); //get if tile is wall

				boolean isNWall = true;
				boolean isWWall = true;
				boolean isNWWall = true;
				boolean isNEWall = true;
				boolean isSWall = true;
				boolean isEWall = true;

				if(row > 0){ //if not at north edge, check north
					isNWall = (worldChars[row - 1][col] == '#');
					if(col > 0){ //if not at west, check nw
						isNWWall = (worldChars[row - 1][col - 1] == '#');
					}
					if(col < worldTiles.length - 1){ //if not at east, check ne
						isNEWall = (worldChars[row - 1][col + 1] == '#');
					}
				}
				if(col > 0){ //if not at west edge, check west
					isWWall = (worldChars[row][col - 1] == '#');
				}
				if(row < worldTiles.length - 1){ //if not at south edge, check south
					isSWall = (worldChars[row + 1][col] == '#');
				}
				if(col < worldTiles.length - 1){ //if not at east edge, check east
					isEWall = (worldChars[row][col + 1] == '#');
				}

				int tileValue = 0; //bit value to store in tile lookup
				final int IS_WALL = 1;     // BITMASK - 0000001
				final int IS_N_WALL = 2;   // BITMASK - 0000010
				final int IS_E_WALL = 4;   // BITMASK - 0000100
				final int IS_S_WALL = 8;   // BITMASK - 0001000
				final int IS_W_WALL = 16;  // BITMASK - 0010000
				final int IS_NE_WALL = 32; // BITMASK - 0100000
				final int IS_NW_WALL = 64; // BITMASK - 1000000

				final int CENTER_PATH = 1; // BITMASK - 0000001
				final int N_PATH = 2;      // BITMASK - 0000010
				final int E_PATH = 4;      // BITMASK - 0000100
				final int S_PATH = 8;      // BITMASK - 0001000
				final int W_PATH = 16;     // BITMASK - 0010000
				final int NE_PATH = 32;    // BITMASK - 0100000
				final int NW_PATH = 64;    // BITMASK - 1000000
				final int SE_PATH = 32;    // BITMASK - 0100000
				final int SW_PATH = 64;    // BITMASK - 1000000
				final int N_WALL = 2;      // BITMASK - 0000010
				final int E_WALL = 4;      // BITMASK - 0000100
				final int S_WALL = 8;      // BITMASK - 0001000
				final int W_WALL = 16;     // BITMASK - 0010000
				final int NE_I_PATH = 32;  // BITMASK - 0100000
				final int NW_I_PATH = 64;  // BITMASK - 1000000
				final int NE_E_PATH = 32;  // BITMASK - 0100000
				final int NW_E_PATH = 64;  // BITMASK - 1000000

				/*
				 * Values:
				 * 0 - Center Wall
				 * 1 - N Wall
				 *
				 * 2 - E Wall
				 * 3 - S Wall
				 * 4 - W Wall
				 */

				if(isNWall && !isWWall && !isSWall && !isEWall){ //if only north
					if(isWall){

					}
					else{

					}
				}
				else if(!isNWall && isWWall && !isSWall && !isEWall){ //if only west

				}
				else if(!isNWall && !isWWall && isSWall && !isEWall){ //if only south

				}
				else if(!isNWall && !isWWall && !isSWall && isEWall){ //if only east

				}
            }
        }

		map = new TiledMap();
		MapLayers mapLayers = map.getLayers();
		int mapWidth = worldTiles[0].length;
		int mapHeight = worldTiles.length;
		int tileSize = 16;
		TiledMapTileLayer layer = new TiledMapTileLayer(mapWidth, mapHeight, tileSize, tileSize); //create layer 100x100, 16x16 tiles
		//loop through and fill layer
		for(int x = 0; x < mapWidth; x++){
			for(int y = 0; y < mapHeight; y++){
				Cell cell = new Cell();
				cell.setTile(getRandomCenterFloor()); //set cell to random tile
				layer.setCell(x, y, cell); //set cell on layer
			}
		}
		mapLayers.add(layer); //add layer to layers

		renderer = new OrthogonalTiledMapRenderer(map, 1/16f);
	}

	@Override
	public void resize(int width, int height) {
		camera.viewportWidth = 30f;
		camera.viewportHeight = 30f * height/width;
		camera.update();
	}

    @Override
    public void render () {
        ScreenUtils.clear(0, 0, 0, 1);
        controlCamera();
        camera.update();
        renderer.setView(camera);
        renderer.render();
        batch.begin();
        batch.draw(player, 0, 0);
        batch.end();
    }

    @Override
    public void dispose () {
        batch.dispose();
        player.dispose();
    }

    private void controlCamera(){
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            camera.translate(-1, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            camera.translate(1, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            camera.translate(0, -1, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            camera.translate(0, 1, 0);
        }
    }

	public TiledMapTile getRandomCenterFloor(){
		int tile = (int)(Math.random() * 18); //get random ID value in range of standard floors
		return dungeonTileMap.getTile(tile); //return specified texture
	}

	public TiledMapTile getRandomNFloor(){
		int tile = 20 + (int)(Math.random() * 6); //get random ID value in range of north floors

		return dungeonTileMap.getTile(tile); //return specified texture
	}

	public TiledMapTile getRandomSFloor(){
		int tile = 18 + (int)(Math.random() * 2); //get random ID value in range of south floors

		return dungeonTileMap.getTile(tile); //return specified texture
	}

	public TiledMapTile getRandomEFloor(){
		int tile = 27; //get random ID value in range of east floors

		return dungeonTileMap.getTile(tile); //return specified texture
	}

	public TiledMapTile getRandomWFloor(){
		int tile = 26; //get random ID value in range of west floors

		return dungeonTileMap.getTile(tile); //return specified texture
	}

	public TiledMapTile getRandomNWFloor(){
		int tile = 28; //get random ID value in range of NW floors

		return dungeonTileMap.getTile(tile); //return specified texture
	}

	public TiledMapTile getRandomNEFloor(){
		int tile = 29; //get random ID value in range of NE floors

		return dungeonTileMap.getTile(tile); //return specified texture
	}

	public TiledMapTile getRandomSWFloor(){
		int tile = 30; //get random ID value in range of SW floors

		return dungeonTileMap.getTile(tile); //return specified texture
	}

	public TiledMapTile getRandomSEFloor(){
		int tile = 31; //get random ID value in range of SE floors

		return dungeonTileMap.getTile(tile); //return specified texture
	}

	public TiledMapTile getRandomNWall(){
		int tile = 33 + (int)(Math.random() * 4); //get random ID value in range of north walls

		return dungeonTileMap.getTile(tile); //return specified texture
	}

	public TiledMapTile getRandomSWall(){
		int tile = 40 + (int)(Math.random() * 6); //get random ID value in range of south walls

		return dungeonTileMap.getTile(tile); //return specified texture
	}

	public TiledMapTile getRandomWWall(){
		int tile = 46 + (int)(Math.random() * 4); //get random ID value in range of west walls

		return dungeonTileMap.getTile(tile); //return specified texture
	}

	public TiledMapTile getRandomEWall(){
		int tile = 50 + (int)(Math.random() * 4); //get random ID value in range of east walls

		return dungeonTileMap.getTile(tile); //return specified texture
	}

	/**
	 * Gets a random Northeast corner wall facing outwards
	 * @return Gets a random Northeast corner wall facing outwards
	 */
	public TiledMapTile getRandomNEWall(){
		int tile = 55 + (int)(Math.random() * 2); //get random ID value in range of NE walls

		return dungeonTileMap.getTile(tile); //return specified texture
	}

	/**
	 * Gets a random Northwest corner wall facing outwards
	 * @return Gets a random Northwest corner wall facing outwards
	 */
	public TiledMapTile getRandomNWWall(){
		int tile = 38 + (int)(Math.random() * 2); //get random ID value in range of NW walls

		return dungeonTileMap.getTile(tile); //return specified texture
	}

	/**
	 * Gets a random southeast corner wall facing inwards
	 * @return Gets a random southeast corner wall facing inwards
	 */
	public TiledMapTile getRandomSEWall(){
		int tile = 37; //get random ID value in range of SE walls

		return dungeonTileMap.getTile(tile); //return specified texture
	}

	/**
	 * Gets a random southwest corner wall facing inwards
	 * @return Gets a random southwest corner wall facing inwards
	 */
	public TiledMapTile getRandomSWWall(){
		int tile = 54; //get random ID value in range of SW walls

		return dungeonTileMap.getTile(tile); //return specified texture
	}

    public TiledMapTile getRandomCenterWall(){
		int tile = 57; //get random ID value in range of center walls

		return dungeonTileMap.getTile(tile); //return specified texture
    }
}
