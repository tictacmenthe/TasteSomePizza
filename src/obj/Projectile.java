package obj;

import gui.FileLoader;
import maps.Vec;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.state.StateBasedGame;

public class Projectile extends Displayable {
	private double speed;
	private int damage;
	private Turret motherTurret;
	private Enemy target;
	private double precisePosX; // if pos.x should be 16.02; then pos.x=16 and
	// precisePosX=0.02
	private double precisePosY;
	Sound sound;

	Projectile(Enemy e, Turret mt, StateBasedGame sbg, Wave w) throws SlickException {
		super(mt.projectileType, mt.getPos(), mt.sbg, w);
		this.target = e;
		this.typeId = mt.projectileType;
		this.motherTurret = mt;
		this.pos = mt.getPos().copy();
		this.actualWave.aliveProjectiles.add(this);
		assignType(typeId);
		this.precisePosX = 0;
		this.precisePosY = 0;
		this.sound=new Sound(FileLoader.getSoundPath(motherTurret.getProjectileSoundName()));
		sound.play();
	}

	private void assignType(int t) throws SlickException {
		// TODO
		if (t == 1) { // HighFireRate
			this.speed = 0.45;
			this.damage = motherTurret.getDamage();
			this.setTypeId(1);
		} else if (t == 2) { // HighDamage
			this.speed = 0.3;
			this.damage = motherTurret.getDamage();
			this.setTypeId(2);
		} else { // Default
			this.speed = 0.3;
			this.damage = motherTurret.getDamage();
			this.setTypeId(0);
		}
		this.sprite = motherTurret.projectileSprite;
	}

	private boolean move(Vec p, int i) {
		// return true if projectile has arrived at the position pos

		// The projectile will move forward of a distance "speed"
		// The trajectory will be a line between target position pos
		// and projectile current position
		double distance = this.pos.distanceDouble(p);

		// If the target is closer than speed, then projectile is immediately
		// put on the location of the target and return true
		if (distance < speed / actualWave.getVit()  || distance <= 2) {
			this.pos.setX(p.getX());
			this.pos.setY(p.getY());
			return true;
		} else {
			int x = this.pos.getX(); // initial position of the projectile
			int y = this.pos.getY();

			double moveX = speed * i / distance * (p.getX() - x) / actualWave.getVit() + precisePosX;
			double moveY = speed * i / distance * (p.getY() - y) / actualWave.getVit() + precisePosY;

			if (Math.abs(moveX) < 1) { // If it should move less than one pixel,
				// keeps in mind the position
				this.precisePosX = moveX;
			} else {
				this.precisePosX = moveX - (int) (moveX);
			}

			if (Math.abs(moveY) < 1)
				this.precisePosY = moveY;
			else
				this.precisePosY = moveY - (int) (moveY);

			this.pos.setX(x + (int) (moveX));
			this.pos.setY(y + (int) (moveY));
			return false;
		}
	}

	public void update(int i) {
		if (target == null || target.isAlive() == false) {
			motherTurret.setLastFire(0);	// reset the timer
			this.disappear();
			// searchAnotherEnemy(); // will search for an other enemy alive,
			// and if there isn't any
			// it calls disappear(), move() and hit(newTarget)
		}

		else {
			this.sprite.setCenterOfRotation(this.sprite.getCenterOfRotationX(), this.sprite.getCenterOfRotationY());
			this.sprite.setRotation(aimingAtDegre(target.getPos()));
			if (move(target.getPos(), i)) { // return true if the projectile
				// hits the enemy
				hit(target);
			}
		}
	}

	private void hit(Enemy tgt) {
		tgt.setHp(tgt.getHp() - damage); // damage are made, if the enemy isn't
		// alive after, it doesn't matter
		if (!tgt.isAlive()) {
			config.addMoney(this.target.getReward());
		}
		this.disappear(); // true because hit the enemy
	}

	// GETTERS AND SETTERS
	public int getTypeId() {	return typeId;	}
	public void setTypeId(int typeId) {	this.typeId = typeId;	}
	public Turret getMotherTurret() {	return motherTurret;	}
}