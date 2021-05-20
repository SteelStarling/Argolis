package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.utils.ScreenUtils;
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

public class WorldScreen implements Screen {

    final ArgolisGame game;

    //setup final values to relate to each direction
    public final int N = 0;
    public final int NE = 1;
    public final int E = 2;
    public final int SE = 3;
    public final int S = 4;
    public final int SW = 5;
    public final int W = 6;
    public final int NW = 7;

    private final float CamSpeed = 0.1f;

    private SpriteBatch batch;
    private Texture player;
    private TiledMapTileSet dungeonTileMap;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;

    public WorldScreen(final ArgolisGame game){
        this.game = game;

        batch = new SpriteBatch();
        player = new Texture(Gdx.files.internal("BasicLad.png"));

        //get game size
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        //create tilemap
        Texture tileMapDungeon = new Texture(Gdx.files.internal("Dungeon_Tileset_Fixed_Extra.png")); //get tileMap for dungeon
        TextureRegion[][] splitTiles = TextureRegion.split(tileMapDungeon, 16, 16); //split tileMap
        dungeonTileMap = new TiledMapTileSet(); //setup tileMap

        //fill tilemap
        int tileID = 0;
        for(int row = 0; row < 11; row++) {
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
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(getRandomCenterFloor()); //set cell to random tile
                layer.setCell(x, y, cell); //set cell on layer
            }
        }
        layers.add(layer); //add layer to layers

        renderer = new OrthogonalTiledMapRenderer(map, 1/16f);

        //create camera
        camera = new OrthographicCamera(30,30 * (h / w)); //change height to match
        camera.update();

        interpretWorld(	"##############################\n" +
                "#  ##  ##   ##  ##  ##   ### #\n" +
                "#  ##  ##   ##  ##  ##   ### #\n" +
                "#  ##  ##   ##  ##  ##   ### #\n" +
                "#  ##  ##   ##  ##  ##   ### #\n" +
                "## ### ### #### ## ###   ### #\n" +
                "## ### ### #### ## ###   ### #\n" +
                "#                            #\n" +
                "#                            #\n" +
                "            ######           #\n" +
                "#           ######           #\n" +
                "#           ##               #\n" +
                "#                            #\n" +
                "##############################");
    }

    @Override
    public void show() {

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


        map = new TiledMap();
        MapLayers mapLayers = map.getLayers();
        int mapWidth = worldChars[0].length;
        int mapHeight = worldChars.length;
        int tileSize = 16;
        TiledMapTileLayer layer = new TiledMapTileLayer(mapWidth, mapHeight, tileSize, tileSize); //create layer 100x100, 16x16 tiles
        TiledMapTileLayer collisionLayer = new TiledMapTileLayer(mapWidth, mapHeight, tileSize, tileSize); //create layer 100x100, 16x16 tiles
        //loop through and fill layer
        for(int row = 0; row < mapHeight; row++){
            for(int col = 0; col < mapWidth; col++){
                boolean isWall = (worldChars[row][col] == '#'); //get if tile is wall

                boolean isNWall = true;
                boolean isWWall = true;
                boolean isNWWall = true;
                boolean isNEWall = true;
                boolean isSWWall = true;
                boolean isSEWall = true;
                boolean isSWall = true;
                boolean isEWall = true;

                if(row > 0){ //if not at north edge, check north
                    isNWall = (worldChars[row - 1][col] == '#');
                    if(col > 0){ //if not at west, check nw
                        isNWWall = (worldChars[row - 1][col - 1] == '#');
                    }
                    if(col < mapWidth - 1){ //if not at east, check ne
                        isNEWall = (worldChars[row - 1][col + 1] == '#');
                    }
                }
                if(col > 0){ //if not at west edge, check west
                    isWWall = (worldChars[row][col - 1] == '#');
                }
                if(row < mapHeight - 1){ //if not at south edge, check south
                    isSWall = (worldChars[row + 1][col] == '#');
                    if(col > 0){ //if not at west, check sw
                        isSWWall = (worldChars[row + 1][col - 1] == '#');
                    }
                    if(col < mapWidth - 1){ //if not at east, check se
                        isSEWall = (worldChars[row + 1][col + 1] == '#');
                    }
                }
                if(col < mapWidth - 1){ //if not at east edge, check east
                    isEWall = (worldChars[row][col + 1] == '#');
                }

                int tileValue = 0; //bit value to store in tile lookup
                final int IS_WALL =     0b000000001; // BITMASK - 000000001
                final int IS_N_WALL =   0b000000010; // BITMASK - 000000010
                final int IS_E_WALL =   0b000000100; // BITMASK - 000000100
                final int IS_S_WALL =   0b000001000; // BITMASK - 000001000
                final int IS_W_WALL =   0b000010000; // BITMASK - 000010000
                final int IS_NE_WALL =  0b000100000; // BITMASK - 000100000
                final int IS_NW_WALL =  0b001000000; // BITMASK - 001000000
                final int IS_SE_WALL =  0b010000000; // BITMASK - 010000000
                final int IS_SW_WALL =  0b100000000; // BITMASK - 100000000
                final int CHECK_PATH =  0b111100000; // BITMASK to clear corner check values, for checking paths
                final int CHECK_SOUTH = 0b111110110; // BITMASK to clear all values other than south for checking self-standing south walls

                final int CENTER_PATH = 0b111100000; // PATHS CAN ONLY BE CHECKED IF CHECK_PATH IS APPLIED!
                final int N_PATH =      0b111100010;
                final int E_PATH =      0b111100100;
                final int S_PATH =      0b111101000;
                final int W_PATH =      0b111110000;
                final int NS_PATH =     0b111110100;
                final int WE_PATH =     0b111101010;
                final int N_END_PATH =  0b111110110;
                final int E_END_PATH =  0b111111010;
                final int S_END_PATH =  0b111111100;
                final int W_END_PATH =  0b111110110;
                final int SINGLE_PATH = 0b111111110;
                final int NE_PATH =     0b111100110;
                final int NW_PATH =     0b111110010;
                final int SE_PATH =     0b111101100;
                final int SW_PATH =     0b111111000;
                final int N_WALL =      0b111111101;
                final int E_WALL =      0b111111011;
                final int E_WALL_TOP =  0b101111111;
                final int S_WALL =      0b111110111;
                final int W_WALL =      0b111101111;
                final int W_WALL_TOP =  0b011111111;
                final int NE_WALL =     0b111111001; // NE and NW Walls must also have CHECK_PATH applied!
                final int NW_WALL =     0b111101101;
                final int SE_WALL =     0b110111111;
                final int SW_WALL =     0b111011111;
                final int CORE_WALL =   0b111111111;

                if(isWall){
                    tileValue = tileValue | IS_WALL;
                }
                if(isNWall){
                    tileValue = tileValue | IS_N_WALL;
                }
                if(isEWall){
                    tileValue = tileValue | IS_E_WALL;
                }
                if(isSWall){
                    tileValue = tileValue | IS_S_WALL;
                }
                if(isWWall){
                    tileValue = tileValue | IS_W_WALL;
                }
                if(isNEWall){
                    tileValue = tileValue | IS_NE_WALL;
                }
                if(isNWWall){
                    tileValue = tileValue | IS_NW_WALL;
                }
                if(isSEWall){
                    tileValue = tileValue | IS_SE_WALL;
                }
                if(isSWWall){
                    tileValue = tileValue | IS_SW_WALL;
                }

                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell(); //create cell

                TiledMapTileLayer.Cell objCell = new TiledMapTileLayer.Cell(); //create cell

                if((tileValue & CORE_WALL) == CORE_WALL){
                    cell.setTile(getRandomCenterWall()); //set cell tile
                }
                else if((tileValue & W_WALL_TOP) == W_WALL_TOP){
                    cell.setTile(getRandomWWall()); //set cell tile
                }
                else if((tileValue & E_WALL_TOP) == E_WALL_TOP){
                    cell.setTile(getRandomEWall()); //set cell tile
                }
                else if((tileValue & SW_WALL) == SW_WALL){
                    cell.setTile(getRandomSWWall()); //set cell tile
                }
                else if((tileValue & SE_WALL) == SE_WALL){
                    cell.setTile(getRandomSEWall()); //set cell tile
                }
                else if(((tileValue | CHECK_PATH) & N_WALL) == N_WALL){
                    cell.setTile(getRandomNWall()); //set cell tile
                }
                else if(((tileValue | CHECK_PATH) & E_WALL) == E_WALL){
                    cell.setTile(getRandomEWall()); //set cell tile
                }
                else if(((tileValue | CHECK_PATH) & W_WALL) == W_WALL){
                    cell.setTile(getRandomWWall()); //set cell tile
                }
                else if(((tileValue | CHECK_PATH) & NW_WALL) == NW_WALL){
                    cell.setTile(getRandomNWWall()); //set cell tile
                }
                else if(((tileValue | CHECK_PATH) & NE_WALL) == NE_WALL){
                    cell.setTile(getRandomNEWall()); //set cell tile
                }
                else if(((tileValue | CHECK_SOUTH) & S_WALL) == S_WALL){
                    cell.setTile(getRandomSWall()); //set cell tile
                }
                else if(((tileValue | CHECK_PATH) & NS_PATH) == NS_PATH){
                    cell.setTile(getRandomNSFloor()); //set cell tile
                }
                else if(((tileValue | CHECK_PATH) & WE_PATH) == WE_PATH){
                    cell.setTile(getRandomWEFloor()); //set cell tile
                }
                else if(((tileValue | CHECK_PATH) & N_END_PATH) == N_END_PATH){
                    cell.setTile(getRandomNEndFloor()); //set cell tile
                }
                else if(((tileValue | CHECK_PATH) & W_END_PATH) == W_END_PATH){
                    cell.setTile(getRandomWEndFloor()); //set cell tile
                }
                else if(((tileValue | CHECK_PATH) & S_END_PATH) == S_END_PATH){
                    cell.setTile(getRandomSEndFloor()); //set cell tile
                }
                else if(((tileValue | CHECK_PATH) & E_END_PATH) == E_END_PATH){
                    cell.setTile(getRandomEEndFloor()); //set cell tile
                }
                else if(((tileValue | CHECK_PATH) & SINGLE_PATH) == SINGLE_PATH){
                    cell.setTile(getRandomSingleFloor()); //set cell tile
                }
                else if(((tileValue | CHECK_PATH) & NE_PATH) == NE_PATH){
                    cell.setTile(getRandomNEFloor()); //set cell tile
                }
                else if(((tileValue | CHECK_PATH) & NW_PATH) == NW_PATH){
                    cell.setTile(getRandomNWFloor()); //set cell tile
                }
                else if(((tileValue | CHECK_PATH) & SE_PATH) == SE_PATH){
                    cell.setTile(getRandomSEFloor()); //set cell tile
                }
                else if(((tileValue | CHECK_PATH) & SW_PATH) == SW_PATH){
                    cell.setTile(getRandomSWFloor()); //set cell tile
                }
                else if(((tileValue | CHECK_PATH) & N_PATH) == N_PATH){
                    cell.setTile(getRandomNFloor()); //set cell tile
                }
                else if(((tileValue | CHECK_PATH) & E_PATH) == E_PATH){
                    cell.setTile(getRandomEFloor()); //set cell tile
                }
                else if(((tileValue | CHECK_PATH) & S_PATH) == S_PATH){
                    cell.setTile(getRandomSFloor()); //set cell tile
                }
                else if(((tileValue | CHECK_PATH) & W_PATH) == W_PATH){
                    cell.setTile(getRandomWFloor()); //set cell tile
                }
                else if(((tileValue | CHECK_PATH) & CENTER_PATH) == CENTER_PATH){
                    cell.setTile(getRandomCenterFloor()); //set cell tile
                }
                else{ //if nothing triggers, print error
                    System.out.println("ERROR: BAD PATH TYPE");
                }


                layer.setCell(col, worldChars.length - 1 - row, cell); //set cell on layer
            }
        }
        mapLayers.add(layer); //add layer to layers

        renderer = new OrthogonalTiledMapRenderer(map, 1/16f);
    }

    @Override
    public void render(float delta) {
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
    public void resize(int width, int height) {
        camera.viewportWidth = 30f;
        camera.viewportHeight = 30f * height/width;
        camera.update();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    public void dispose(){
        batch.dispose();
        player.dispose();
    }

    private void controlCamera(){
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            camera.translate(-CamSpeed, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            camera.translate(CamSpeed, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            camera.translate(0, -CamSpeed, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            camera.translate(0, CamSpeed, 0);
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

    public TiledMapTile getRandomCenterWall(){
        int tile = 57; //get random ID value in range of center walls

        return dungeonTileMap.getTile(tile); //return specified texture
    }

    public TiledMapTile getRandomSWall(){
        int tile = 33 + (int)(Math.random() * 4); //get random ID value in range of north walls

        return dungeonTileMap.getTile(tile); //return specified texture
    }

    public TiledMapTile getRandomNWall(){
        int tile = 40 + (int)(Math.random() * 6); //get random ID value in range of south walls

        return dungeonTileMap.getTile(tile); //return specified texture
    }

    public TiledMapTile getRandomEWall(){
        int tile = 46 + (int)(Math.random() * 4); //get random ID value in range of west walls

        return dungeonTileMap.getTile(tile); //return specified texture
    }

    public TiledMapTile getRandomWWall(){
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

    public TiledMapTile getRandomNEndFloor(){
        int tile = 106; //get random ID value in range of center walls

        return dungeonTileMap.getTile(tile); //return specified texture
    }

    public TiledMapTile getRandomEEndFloor(){
        int tile = 105; //get random ID value in range of center walls

        return dungeonTileMap.getTile(tile); //return specified texture
    }

    public TiledMapTile getRandomSEndFloor(){
        int tile = 107; //get random ID value in range of center walls

        return dungeonTileMap.getTile(tile); //return specified texture
    }

    public TiledMapTile getRandomWEndFloor(){
        int tile = 104; //get random ID value in range of center walls

        return dungeonTileMap.getTile(tile); //return specified texture
    }

    public TiledMapTile getRandomNSFloor(){
        int tile = 100 + (int)(Math.random() * 2); //get random ID value in range of NS paths

        return dungeonTileMap.getTile(tile); //return specified texture
    }

    public TiledMapTile getRandomWEFloor(){
        int tile = 102 + (int)(Math.random() * 2); //get random ID value in range of WE paths

        return dungeonTileMap.getTile(tile); //return specified texture
    }

    public TiledMapTile getRandomSingleFloor(){
        int tile = 108 + (int)(Math.random() * 2); //get random ID value in range of WE paths

        return dungeonTileMap.getTile(tile); //return specified texture
    }
}
