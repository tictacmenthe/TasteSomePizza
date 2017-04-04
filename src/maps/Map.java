package maps;

import gui.Buttons.TurretButton;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.gui.GUIContext;
import org.newdawn.slick.state.StateBasedGame;
import states.GameConfig;

import java.util.ArrayList;
import java.util.LinkedList;


//TODO: Méthode plus légit(moins dépendante de slick) pour écouter les inputs (Jpanels, mouselisteners, canvas...)
/**
 * Created by tic-tac on 15/02/17.
 */
public class Map {
    private int taille;
    private Case[][] cases;
    private GUIContext gc;
    private StateBasedGame sbg;
    private Graphics g;
    public Vec posBase;
    public Vec spawn;
    public int baseHP;
    private GameConfig config;

    public Map(StateBasedGame sbg, int taille, GameConfig conf) throws SlickException{   //Initialise une map vide de taille taillextaille
        this.taille=taille;
        this.gc=sbg.getContainer();
        this.sbg=sbg;
        this.g=sbg.getContainer().getGraphics();
        cases = new Case[taille][taille];
        this.config=conf;
        for(int i=0;i<taille;i++){
            for(int j=0;j<taille;j++){
                cases[i][j]=new Case(sbg, 1+ i*720/taille, 1+ j*720/taille,this, this.config);
            }
        }
        
        spawn = new Vec(1, 337);
        posBase = new Vec(673, 337);
        baseHP = 100;
        
    }

    public void render() { //Affiche chaque case
        for(Case[] cs : cases){
            for(Case c : cs){
                c.render();
            }
        }
    }

    public void update() throws InterruptedException {
        for(Case[] cs : cases){
            for(Case c : cs){
                c.update();
            }
        }
    }

    public Case[][] getCases(){
        return this.cases;
    }
    public int getTaille(){
        return this.taille;
    }
    
    int[][] toMatrix() {
    	int[][] matrix = new int[this.taille][this.taille];
    	for (int i=0; i<this.taille; i++) {
    		for (int j=0; j<this.taille; j++) {
    			if (this.cases[i][j].getTurret() == null) {
    				matrix[i][j]=0;
    			}
    			else {
    				matrix[i][j]=1;
    			}
    		}
    	}
    	return matrix;
    }
    
    static void cross(int[][] m, int[] coordinates) {
		m[coordinates[0]][coordinates[1]]=2;
	}
    
    static public boolean corEquals(int[] cor1, int[] cor2) {
		return (cor1[0]==cor2[0] && cor1[1]==cor2[1]);
	}
	
	static public int[] getCor(int[] corU) {
		return (new int[] {corU[0], corU[1]});
	}
	
    public LinkedList<Vec> computePath(){
    			int[][] currentMap = this.toMatrix();
    			LinkedList<int[]> mainList = new LinkedList<int[]>();
    			LinkedList<int[]> untestedList = new LinkedList<int[]>();
    			int n=0; //cost
    			int[] origin = new int[] {(this.spawn.getX()-1)/48, (this.spawn.getY()-1)/48, n}; //division to convert in integer coordinates
    			int[] end = new int[] {(this.posBase.getX()-1)/48, (this.posBase.getY()-1)/48};
    			untestedList.add(origin);
    			LinkedList<int[]> nextUntestedList = (LinkedList<int[]>) untestedList.clone();
    			boolean found = false;
    			while (! found && ! nextUntestedList.isEmpty()) {
    				n++;
    				untestedList = (LinkedList<int[]>) nextUntestedList.clone();
    				nextUntestedList.clear();
    				for (int[] cor : untestedList) {
    					cross(currentMap, cor);
    					mainList.add(cor);
    					if (corEquals(cor, end)) {
    						found=true;
    						break;
    					}
    					if (0 <= cor[1]+1 && cor[1]+1 < currentMap[0].length && currentMap[cor[0]][cor[1]+1] == 0) {
    						nextUntestedList.add(new int[] {cor[0],cor[1]+1, n});
    					}
    					if (0 <= cor[0]+1 && cor[0]+1 < currentMap.length && currentMap[cor[0]+1][cor[1]] == 0) {
    						nextUntestedList.add(new int[] {cor[0]+1,cor[1], n});
    					}
    					if (0 <= cor[0]-1 && cor[0]-1 < currentMap.length && currentMap[cor[0]-1][cor[1]] == 0) {
    						nextUntestedList.add(new int[] {cor[0]-1,cor[1], n});
    					}
    					if (0 <= cor[1]-1 && cor[1]-1 < currentMap[0].length && currentMap[cor[0]][cor[1]-1] == 0) {
    						nextUntestedList.add(new int[] {cor[0],cor[1]-1, n});
    					}
    				}
    			}
    			if (mainList.getLast()[0]!=end[0] || mainList.getLast()[1]!=end[1]) {
    				return null;
    			}
    			else {
    				LinkedList<Vec> computedPath = new LinkedList<Vec>();
    				computedPath.addFirst(new Vec(1+mainList.getLast()[0]*720/taille, 1+mainList.removeLast()[1]*720/taille));
    				while (! corEquals(computedPath.getFirst().toList(), origin)){
    					while (mainList.size()>0 &&
    							!corEquals(getCor(mainList.getLast()), new int[] {computedPath.getFirst().toList()[0],computedPath.getFirst().toList()[1]-1}) &&
    							!corEquals(getCor(mainList.getLast()), new int[] {computedPath.getFirst().toList()[0]+1,computedPath.getFirst().toList()[1]}) &&
    							!corEquals(getCor(mainList.getLast()), new int[] {computedPath.getFirst().toList()[0]-1,computedPath.getFirst().toList()[1]}) &&
    							!corEquals(getCor(mainList.getLast()), new int[] {computedPath.getFirst().toList()[0],computedPath.getFirst().toList()[1]+1})){
    							mainList.removeLast();
    					}
    					n=mainList.getLast()[2];
    					computedPath.addFirst(new Vec(1+mainList.getLast()[0]*720/taille, 1+mainList.removeLast()[1]*720/taille));
    					while (mainList.size()>0 && mainList.getLast()[2]>=n) {
    						mainList.removeLast();
    					}
    				}
    				return computedPath;
    			}
	}
}
