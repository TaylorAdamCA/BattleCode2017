package LumberJackWithSpotting;

import java.util.Random;

import battlecode.common.*;
import java.util.ArrayList;
public strictfp class RobotPlayer {
	static RobotController rc;
	static Direction[] dirList = new Direction[8];
	static Direction goingDir = Direction.getNorth();
	static Random rand;
	static Direction enemyDir;
	static ArrayList<TreeInfo> ourTrees = new ArrayList<TreeInfo>();
	// Keep broadcasting channels
	static final int GARDENER_CHANNEL = 5;
	static final int LUMBERJACK_CHANNEL = 6;
	static final int SOLDIER_CHANNEL = 7;
	static final int SCOUT_CHANNEL = 8;
	static final int TANK_CHANNEL = 9;
	static final int ENEMYSPOTTED_CHANNEL = 10;
	// Keep important numbers here
	static final int GARDENER_MAX = 3;
	static final int LUMBERJACK_MAX = 100;
	static final int SOLDIER_MAX = 100;
	static final int TANK_MAX = 200;

	public static void run(RobotController rc) throws GameActionException {
		// TODO Track any enemy any unit sees and alert other LUMBERJACKS and
		// SOLDIERS
		RobotPlayer.rc = rc;
		initDirList();
		rand = new Random(rc.getID());
		goingDir = dirList[(int) rand.nextInt(8)];
		switch (rc.getType()) {
		case ARCHON:
			runArchon();
			break;
		case GARDENER:
			runGardener();
			break;
		case SOLDIER:
			runSoldier();
			break;
		case LUMBERJACK:
			runLumberJack();
			break;
		case SCOUT:
			runScout();
			break;
		case TANK:
			runTank();
			break;
		default:
			break;
		}
	}

	private static void runScout() {
		while (true) {
			try {
				enemySpot();

				dodge();
				int prev = rc.readBroadcast(SCOUT_CHANNEL);
				rc.broadcast(SCOUT_CHANNEL, prev + 1);
				canShoot(RobotType.SCOUT, "Pentad");
				canShoot(RobotType.SCOUT, "triad");
				canShoot(RobotType.SCOUT, "single");
				if (!rc.hasAttacked())
					moveTowardsEnemy();
			} catch (Exception e) {

			}
		}

	}

	public static Direction randomDir() {
		return dirList[(int) rand.nextInt(8)];
	}

	public static void initDirList() {
		for (int i = 0; i < 8; i++) {
			float radians = (float) (-Math.PI + 2 * Math.PI * ((float) i / 8));
			dirList[i] = new Direction(radians);
		}
	}

	public static void runArchon() {
		while (true) {
			try {
				// enemySpot();
				// enemySpot();
				if (rc.readBroadcast(GARDENER_CHANNEL) < GARDENER_MAX)
					tryToBuild(RobotType.GARDENER, RobotType.GARDENER.bulletCost);

				if (rc.canMove(goingDir)) {
					rc.move(goingDir);
				}
			} catch (Exception e) {
				e.printStackTrace();

			}
		}

	}

	public static void runGardener() {
		while (true) {
			try {
				// enemySpot();
				// enemySpot();
				int prev = rc.readBroadcast(GARDENER_CHANNEL);
				rc.broadcast(GARDENER_CHANNEL, prev + 1);
				dodge();
				if (rc.getHealth() < RobotType.GARDENER.maxHealth)
					rc.broadcast(GARDENER_CHANNEL, prev - 1);
				if (rc.getRoundNum() < 500)
					tryToBuild(RobotType.LUMBERJACK, RobotType.LUMBERJACK.bulletCost);

				tryToBuild(RobotType.SOLDIER, RobotType.SOLDIER.bulletCost);
				// tryToBuild(RobotType.TANK, RobotType.TANK.bulletCost);

				// tryToPlant();
				// tryToWater();
				// TODO count gardeners

				if (rc.canMove(goingDir)) {
					rc.move(goingDir);
				} else {
					goingDir = randomDir();
				}
				// if (rc.getRoundNum() < 300 &&
				// rc.readBroadcast(LUMBERJACK_CHANNEL) < LUMBERJACK_MAX) {

				// }

			} catch (Exception e) {
				e.printStackTrace();

			}
		}

	}

	public static void runSoldier() {
		while (true) {
			try {
				//enemySpot();

				dodge();
				int prev = rc.readBroadcast(SOLDIER_CHANNEL);
				rc.broadcast(SOLDIER_CHANNEL, prev + 1);
				canShoot(RobotType.SOLDIER, "Pentad");
				canShoot(RobotType.SOLDIER, "triad");
				canShoot(RobotType.SOLDIER, "single");

				if (rc.canMove(goingDir)) {
					rc.move(goingDir);
				} else {
					goingDir = randomDir();
				}
				// {
				/*
				 * if (rc.canMove(goingDir)) { rc.move(goingDir); } else {
				 * goingDir = randomDir(); }
				 * 
				 * }
				 */

			} catch (Exception e) {
				e.printStackTrace();

			}
		}
	}

	public static void runLumberJack() {
		while (true) {
			try {
				//enemySpot();
				//int treesCut = 0;
				// Direction towardEnemy = enemySpot();
				// moveTowardsEnemy(towardEnemy, ENEMYSPOTTED_CHANNEL);
				// COUNTS LUMBERJACKS
				int prev = rc.readBroadcast(LUMBERJACK_CHANNEL);
				rc.broadcast(LUMBERJACK_CHANNEL, prev + 1);

				if (rc.getHealth() < RobotType.LUMBERJACK.maxHealth) {
					rc.broadcast(LUMBERJACK_CHANNEL, prev - 1);
				}
				dodge();

				// CHECKS IF SHOULD ATTACK OR CHOP
				RobotInfo[] bots = rc.senseNearbyRobots();
				for (RobotInfo b : bots) {

					if (b.getTeam() != rc.getTeam() && rc.canStrike()
							&& rc.getLocation().distanceTo(b.getLocation()) <= GameConstants.LUMBERJACK_STRIKE_RADIUS
									+ rc.getType().bodyRadius) {
						rc.strike();
						Direction dir = rc.getLocation().directionTo(b.getLocation());
						if (rc.canMove(dir))
							rc.move(dir);
					}
					break;
				}
				/*if (treesCut < 50) {
					TreeInfo[] trees = rc.senseNearbyTrees(GameConstants.LUMBERJACK_STRIKE_RADIUS);
					for (TreeInfo t : trees) {
						if (rc.canChop(t.getID())) {
							rc.chop(t.getID());
							treesCut++;
							break;
						}
					}
				}*/

				if (rc.canMove(goingDir)) {
					rc.move(goingDir);
				} else {
					goingDir = randomDir();
				}

			} catch (Exception e) {
				e.printStackTrace();

			}
		}
	}

	public static void runTank() {
		while (true) {
			try {
				//enemySpot();
				// Direction towardEnemy = enemySpot();
				// moveTowardsEnemy(towardEnemy, ENEMYSPOTTED_CHANNEL);

				canShoot(RobotType.TANK, "Pentad");
				canShoot(RobotType.TANK, "triad");
				canShoot(RobotType.TANK, "single");

				if (rc.canMove(goingDir)) {
					rc.move(goingDir);
				} else {
					goingDir = randomDir();
				}

			} catch (Exception e) {
				e.printStackTrace();

			}
		}
	}

	public static void enemySpot() throws GameActionException {
		RobotInfo[] bots = rc.senseNearbyRobots();
		Direction d = new Direction(null, null);

		for (RobotInfo b : bots) {
			d = rc.getLocation().directionTo(b.getLocation());
			enemyDir = d;
		}
	}

	public static void moveTowardsEnemy() throws GameActionException {

		if (rc.canMove(enemyDir)) {
			rc.move(enemyDir);
		} else if (rc.canMove(goingDir))
			rc.move(goingDir);
		else
			rc.move(randomDir());
	}

	public static void canShoot(RobotType shooter, String typeOfShot) throws GameActionException {
		RobotInfo[] bots = rc.senseNearbyRobots();
		switch (typeOfShot) {
		case "single":
			for (RobotInfo b : bots) {
				if (b.getTeam() != rc.getTeam() && shooter.canAttack()
						&& rc.getTeamBullets() >= GameConstants.SINGLE_SHOT_COST) {
					Direction dir = rc.getLocation().directionTo(b.getLocation());
					rc.fireSingleShot(dir);
					if (rc.canMove(dir))
						rc.move(dir);
				}
				break;
			}

		case "triad":
			for (RobotInfo b : bots) {
				if (b.getTeam() != rc.getTeam() && shooter.canAttack()
						&& rc.getTeamBullets() >= GameConstants.TRIAD_SHOT_COST
						&& (b.type == RobotType.TANK || b.type == RobotType.GARDENER || b.type == RobotType.ARCHON)) {
					Direction dir = rc.getLocation().directionTo(b.getLocation());
					rc.fireTriadShot(dir);
					if (rc.canMove(dir))
						rc.move(dir);
				}
				break;
			}

		case "pentad":
			for (RobotInfo b : bots) {
				if (b.getTeam() != rc.getTeam() && shooter.canAttack()
						&& rc.getTeamBullets() >= GameConstants.PENTAD_SHOT_COST
						&& (b.type == RobotType.ARCHON || b.type == RobotType.TANK)) {
					Direction dir = rc.getLocation().directionTo(b.getLocation());
					rc.firePentadShot(dir);
					if (rc.canMove(dir))
						rc.move(dir);
				}
				break;
			}
		}
	}

	/*
	 * public static void build(RobotType type, int channel, int max) throws
	 * GameActionException {
	 * 
	 * int prevNumLumb = rc.readBroadcast(channel); if (prevNumLumb < max) {
	 * tryToBuild(type, type.bulletCost); rc.broadcast(channel, prevNumLumb++);
	 * } }
	 */

	public static void tryToBuild(RobotType type, int moneyNeeded) throws GameActionException {
		if (rc.getTeamBullets() > moneyNeeded) {
			for (int i = 0; i < 4; i++) {
				if (rc.canBuildRobot(type, dirList[i])) {
					rc.buildRobot(type, dirList[i]);
					break;
				}
			}

		}
	}

	public static void tryToWater() throws GameActionException {
		TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
		for (int i = 0; i < nearbyTrees.length; i++) {
			// TODO instead of just not full check for lowest health
			if (rc.canWater(nearbyTrees[i].getID())) {
				if (nearbyTrees[i].getHealth() < GameConstants.BULLET_TREE_MAX_HEALTH
						- GameConstants.WATER_HEALTH_REGEN_RATE)
					rc.water(nearbyTrees[i].getID());
				break;
			}
		}
	}

	public static void tryToPlant() throws GameActionException {
		if (rc.getTeamBullets() > GameConstants.BULLET_TREE_COST) {
			for (int i = 0; i < 4; i++) {

				MapLocation treeLocations = rc.getLocation().add(dirList[i], GameConstants.GENERAL_SPAWN_OFFSET
						+ GameConstants.BULLET_TREE_RADIUS + rc.getType().bodyRadius);
				if (modGood(treeLocations.x, 6, .2f) && modGood(treeLocations.y, 6, .2f))
					if (rc.canPlantTree(dirList[i])) {
						rc.plantTree(dirList[i]);
						;
						break;
					}
			}

		}
	}

	static boolean willCollideWithMe(BulletInfo bullet) {
		MapLocation myLocation = rc.getLocation();

		// Get relevant bullet information
		Direction propagationDirection = bullet.dir;
		MapLocation bulletLocation = bullet.location;

		// Calculate bullet relations to this robot
		Direction directionToRobot = bulletLocation.directionTo(myLocation);
		float distToRobot = bulletLocation.distanceTo(myLocation);
		float theta = propagationDirection.radiansBetween(directionToRobot);

		if (Math.abs(theta) > Math.PI / 2) {
			return false;
		}

		float perpendicularDist = (float) Math.abs(distToRobot * Math.sin(theta));

		return (perpendicularDist <= rc.getType().bodyRadius);
	}

	static boolean tryMove(Direction dir) throws GameActionException {
		return tryMove(dir, 20, 3);
	}

	static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

		// First, try intended direction
		if (!rc.hasMoved() && rc.canMove(dir)) {
			rc.move(dir);
			return true;
		}

		// Now try a bunch of similar angles
		// boolean moved = rc.hasMoved();
		int currentCheck = 1;

		while (currentCheck <= checksPerSide) {
			// Try the offset of the left side
			if (!rc.hasMoved() && rc.canMove(dir.rotateLeftDegrees(degreeOffset * currentCheck))) {
				rc.move(dir.rotateLeftDegrees(degreeOffset * currentCheck));
				return true;
			}
			// Try the offset on the right side
			if (!rc.hasMoved() && rc.canMove(dir.rotateRightDegrees(degreeOffset * currentCheck))) {
				rc.move(dir.rotateRightDegrees(degreeOffset * currentCheck));
				return true;
			}
			// No move performed, try slightly further
			currentCheck++;
		}

		// A move never happened, so return false.
		return false;
	}

	static boolean trySidestep(BulletInfo bullet) throws GameActionException {
		Direction towards = bullet.getDir();
		return (tryMove(towards.rotateRightDegrees(90)) || tryMove(towards.rotateLeftDegrees(90)));
	}

	static void dodge() throws GameActionException {
		BulletInfo[] bullets = rc.senseNearbyBullets();
		for (BulletInfo bi : bullets) {
			if (willCollideWithMe(bi)) {
				trySidestep(bi);
			}
		}

	}

	public static boolean modGood(float number, float spacing, float fraction) {
		return (number % spacing) < spacing * fraction;
	}

}
