package ScoutSpawnner;

import java.util.Random;
import java.util.ArrayList;

import battlecode.common.*;

public strictfp class RobotPlayer {
	static TreeInfo[] nearbyTrees;
	static RobotController rc;
	static Direction[] dirList = new Direction[8];
	static Direction goingDir = Direction.getNorth();
	static Direction archonGo = Direction.getNorth();
	static boolean archonGoTest = false;
	static Random rand;
	static ArrayList<MapLocation> oldLocations = new ArrayList<MapLocation>();
	static int gardeners = 0;
	static int soldiersSpawn = 0;
	static int intialSoldiers = 0;
	static int scouts = 0;
	static boolean enoughGard = false;
	static int fourGard = 0;
	static int archon = 1;
	static int archonMoved = 0;
	static Direction[] plantDirList = new Direction[6];
	static ArrayList<MapLocation> ourBots;
	static ArrayList<ArrayList<Integer>> outer = new ArrayList<ArrayList<Integer>>();
	static boolean swarm = false;

	// Keep broadcasting channels
	static final int GARDENER_CHANNEL = 5;
	static final int LUMBERJACK_CHANNEL = 6;
	static final int SOLDIER_CHANNEL = 7;
	static final int SCOUT_CHANNEL = 8;
	static final int TANK_CHANNEL = 9;
	static final int ARCHON_CHANNEL = 100;
	static int SWARM_CHANNEL = 100;

	// Keep important numbers here
	static final int GARDENER_MAX = 8;
	static final int LUMBERJACK_MAX = 5;
	static final int TANK_MAX = 100;
	static int ENEMY_ARCHON_CHANNEL = 50;
	static int ENEMY_ARCHON_SPOTTED = 54;

	public static void run(RobotController rc) throws GameActionException {
		RobotPlayer.rc = rc;
		initDirList();
		initPlantDirList();

		if (rc.getRoundNum() % 1000 == 0) {
			archonMoved = 0;
			archonGoTest = false;
		}
		if (rc.getRoundNum() % 3 == 0) {
			rc.broadcast(10, 0);
			rc.broadcast(11, 0);
		}
		if (rc.getRoundNum() % 10 == 0) {
			rc.broadcast(201, 0);
			rc.broadcast(202, 0);
		}
		rand = new Random(rc.getID());
		goingDir = dirList[(int) rand.nextInt(8)];
		switch (rc.getType()) {
		case ARCHON:
			runArchon();

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
			runTanks();
			break;
		default:
			break;
		}
	}

	public static void initPlantDirList() {
		for (int i = 0; i < 6; i++) {
			float radians = (float) (-Math.PI + 2 * Math.PI * ((float) i / 6));
			plantDirList[i] = new Direction(radians);
		}
	}

	public static void clearArray(ArrayList<?> a) {
		if (a.size() > 2)
			a.clear();
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
				goingDir =  rc.getLocation().directionTo(rc.getInitialArchonLocations(rc.getTeam().opponent())[0]);
				if (fourGard < 4)
					tryToBuild(RobotType.GARDENER, RobotType.GARDENER.bulletCost);

				archon = 0;
				fourGard = 0;
				RobotInfo[] bots = rc.senseNearbyRobots();
				for (RobotInfo b : bots) {
					if (b.getType() == RobotType.GARDENER)
						fourGard++;

				}
				if (fourGard == 3 && archonGoTest == false) {
					for (int x = 0; x < 8; x++) {
						if (rc.canMove(dirList[x]))
							archonGo = dirList[x];
					}
				}
				if (fourGard == 3)
					if (rc.canMove(archonGo) && archonMoved < 8) {
						rc.move(archonGo);
						archonMoved++;
					}
		
				enemySpot();
				if (rc.getRobotCount() > 100 && rc.getTeamBullets() > 100) {
					rc.donate(10 * rc.getVictoryPointCost());
				}
				if (rc.getTeamBullets() >= 1000 && rc.getRoundNum() > 750)
					rc.donate(50 * rc.getVictoryPointCost());
				// if (gardeners < 8)
				// tryToBuild(RobotType.GARDENER,
				// RobotType.GARDENER.bulletCost);

				MapLocation myLocation = rc.getLocation();
				rc.broadcast(0, (int) myLocation.x);
				rc.broadcast(1, (int) myLocation.y);
				// if (rc.canMove(goingDir)) {
				// rc.move(goingDir);
				// }
			} catch (Exception e) {
				e.printStackTrace();

			}
		}

	}

	public static void runGardener() {
		while (true) {
			try {

				if (rc.getRoundNum() > 300) {
					tryToBuild(RobotType.TANK, RobotType.TANK.bulletCost);
				}
				RobotInfo[] bots = rc.senseNearbyRobots();
				for (RobotInfo b : bots) {
					if (b.getTeam() != rc.getTeam())
						tryToBuild(RobotType.SOLDIER, RobotType.SOLDIER.bulletCost);
				}
				if (scouts < 2) {
					tryToBuild(RobotType.SCOUT, RobotType.SCOUT.bulletCost);
					scouts++;
				}

				soldiersSpawn++;
				// garMove();
				enemySpot();
				int prev = rc.readBroadcast(GARDENER_CHANNEL);
				if (rc.getHealth() < 5) {
					rc.broadcast(GARDENER_CHANNEL, prev - 1);
				}
				rc.broadcast(GARDENER_CHANNEL, prev + 1);
				// doCirclesCollide(rc.getLocation().add(dirList[x]),
				// (float)1.0, rc.getLocation().add(dirList[x], 1) , (float)1.0)
				// != true)
				int avTreeSpots = 0;
				// rc.canMove(arg0, arg1)
				for (int x = 0; x < 8; x++) {
					if (rc.canPlantTree(dirList[x])) {
						avTreeSpots++;
					}
				}
				/*
				 * if (avTreeSpots < 3) { rc.move(goingDir); }
				 */

				if (rc.getRoundNum() > 50 && intialSoldiers < 4) {
					tryToBuild(RobotType.SOLDIER, RobotType.SOLDIER.bulletCost);
					intialSoldiers++;
				} else if (avTreeSpots > 2) {
					tryToPlant();
				} else if (soldiersSpawn % 2 == 0) {
					tryToBuild(RobotType.LUMBERJACK, RobotType.LUMBERJACK.bulletCost);

				} else {
					tryToBuild(RobotType.SOLDIER, RobotType.SOLDIER.bulletCost);
				}
				tryToWater();
				// if (rc.canMove(goingDir)) {
				// rc.move(goingDir);
				// } else {
				// goingDir = randomDir();
				// }
				// if (rc.getRoundNum() < 300 &&
				// rc.readBroadcast(LUMBERJACK_CHANNEL) < LUMBERJACK_MAX) {
				// build(RobotType.LUMBERJACK, LUMBERJACK_CHANNEL,
				// LUMBERJACK_MAX);
				// }
				// tryToBuild(RobotType.TANK, RobotType.TANK.bulletCost);
			} catch (Exception e) {
				e.printStackTrace();

			}
		}

	}

	public static void runSoldier() {
		while (true) {
			try {
				dodge();
				enemySpot();
				canShoot(RobotType.SOLDIER, "pentad");
				canShoot(RobotType.SOLDIER, "triad");
				canShoot(RobotType.SOLDIER, "single");
				// towardEnemyArchon();
				if (!rc.hasAttacked()) {
					if (!towardEnemy()) {
						goingDir = randomDir();
						rc.move(goingDir);
					}

				}

			} catch (Exception e) {
				e.printStackTrace();

			}
		}
	}

	public static void runTanks() {
		while (true) {
			try {
				dodge();
				int prev = rc.readBroadcast(TANK_CHANNEL);
				if (rc.getHealth() < 5) {
					rc.broadcast(TANK_CHANNEL, prev - 1);
				}
				rc.broadcast(TANK_CHANNEL, prev + 1);

				enemySpot();

				canShoot(RobotType.TANK, "pentad");
				canShoot(RobotType.TANK, "triad");
				if (rc.readBroadcast(201) != 0 && rc.readBroadcast(202) != 0)
					towardEnemyArchon();

				towardEnemy();
				if (!rc.hasAttacked()) {
					if (rc.canMove(goingDir)) {
						rc.move(goingDir);
					} else {
						goingDir = randomDir();
					}

				}

			} catch (Exception e) {
				e.printStackTrace();

			}
		}
	}

	public static void runScout() {
		while (true) {
			try {
				dodge();
				enemySpot();
				TreeInfo[] nearbyTrees = rc.senseNearbyTrees();

				for (TreeInfo t : nearbyTrees) {
					if (t.containedBullets > 1 && rc.canShake() && t.getTeam() == Team.NEUTRAL) {
						MapLocation shakeT = t.getLocation();
						if (rc.canMove(shakeT)) {
							rc.move(shakeT);
							rc.shake(t.getID());
						}
					}
				}
				if (enemySpot() == RobotType.GARDENER)
					towardEnemy();
				canShoot(RobotType.SOLDIER, "single");
				if (!rc.hasAttacked()) {
					if (rc.canMove(goingDir)) {
						rc.move(goingDir);
					} else {
						goingDir = randomDir();
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void runLumberJack() {
		while (true) {
			try {
				dodge();
				if (enemySpot() != RobotType.ARCHON)
					towardEnemy();

				// COUNTS LUMBERJACKS
				int prev = rc.readBroadcast(LUMBERJACK_CHANNEL);
				rc.broadcast(LUMBERJACK_CHANNEL, prev + 1);

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

				TreeInfo[] trees = rc.senseNearbyTrees(GameConstants.LUMBERJACK_STRIKE_RADIUS);
				for (TreeInfo t : trees) {
					if (t.getTeam() != rc.getTeam() && rc.canChop(t.getID())) {
						rc.chop(t.getID());
						break;
					}
				}
				if (!rc.hasAttacked()) {
					if (rc.canMove(goingDir)) {
						rc.move(goingDir);
					} else {
						goingDir = randomDir();
					}
					if (rc.canMove(goingDir)) {
						rc.move(goingDir);
					} else {
						goingDir = randomDir();
					}

				}
			} catch (Exception e) {
				e.printStackTrace();

			}
		}
	}

	public static void canShoot(RobotType shooter, String typeOfShot) throws GameActionException {

		RobotInfo[] bots = rc.senseNearbyRobots();

		if (rc.getType() == RobotType.SCOUT) {
			for (RobotInfo b : bots) {
				if (b.getTeam() != rc.getTeam() && shooter.canAttack()
						&& rc.getTeamBullets() >= GameConstants.SINGLE_SHOT_COST && b.getType() == RobotType.GARDENER) {
					Direction dir = rc.getLocation().directionTo(b.getLocation());
					rc.fireSingleShot(dir);
					if (b.getType() == RobotType.LUMBERJACK && rc.canMove(dir.opposite())) {
						rc.move(dir.opposite());
					}
				}
				break;
			}

		} else {
			for (RobotInfo b : bots) {
				if (b.getType() != RobotType.ARCHON && b.getTeam() != rc.getTeam() && shooter.canAttack()
						&& rc.getTeamBullets() >= GameConstants.SINGLE_SHOT_COST
						&& rc.getLocation().distanceTo(b.getLocation()) > 5) {
					Direction dir = rc.getLocation().directionTo(b.getLocation());
					rc.fireSingleShot(dir);

					if (b.getType() == RobotType.LUMBERJACK && rc.canMove(dir.opposite())) {
						rc.move(dir.opposite());
						break;
					}
				} else if (b.getType() != RobotType.ARCHON && b.getTeam() != rc.getTeam() && shooter.canAttack()
						&& rc.getTeamBullets() >= GameConstants.TRIAD_SHOT_COST
						&& rc.getLocation().distanceTo(b.getLocation()) > 2.5
						&& rc.getLocation().distanceTo(b.getLocation()) < 5) {
					Direction dir = rc.getLocation().directionTo(b.getLocation());
					rc.fireTriadShot(dir);

					if (b.getType() == RobotType.LUMBERJACK && rc.canMove(dir.opposite())) {
						rc.move(dir.opposite());
						break;
					}
				} else if (b.getTeam() != rc.getTeam() && shooter.canAttack()
						&& rc.getTeamBullets() >= GameConstants.PENTAD_SHOT_COST) {
					Direction dir = rc.getLocation().directionTo(b.getLocation());
					rc.firePentadShot(dir);
					if (b.getType() == RobotType.LUMBERJACK && rc.canMove(dir.opposite())) {
						rc.move(dir.opposite());
						break;

					}

				}
			}
		}
	}

	public static void tryMoveOrRandom() throws GameActionException {
		if (rc.canMove(goingDir)) {
			rc.move(goingDir);
		} else {
			goingDir = randomDir();
		}
	}

	public static void build(RobotType type, int channel, int max) throws GameActionException {
		int prevNumLumb = rc.readBroadcast(channel);
		if (prevNumLumb < max) {
			tryToBuild(type, type.bulletCost);
			rc.broadcast(channel, prevNumLumb++);
		}
	}

	public static void tryToBuild(RobotType type, int moneyNeeded) throws GameActionException {

		if (rc.getTeamBullets() > moneyNeeded) {
			if (type == RobotType.GARDENER) {
				for (int i = 0; i < 8; i += 2) {
					if (rc.canBuildRobot(type, dirList[i]) && rc.canMove(rc.getLocation().add(dirList[i], 3))) {
						// if((rc.isLocationOccupiedByTree(rc.getLocation().add(dirList[i],
						// RobotType.GARDENER.bodyRadius * 3)) == false) ){
						gardeners++;
						rc.buildRobot(type, dirList[i]);
						break;
						// }
					}
				}
			} else {
				for (int i = 0; i < 8; i++) {
					if (rc.canBuildRobot(type, dirList[i])) {
						rc.buildRobot(type, dirList[i]);
						break;
					}
				}
			}

		}
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
		MapLocation leftGoal = rc.getLocation().add(towards.rotateLeftDegrees(90), rc.getType().bodyRadius);
		MapLocation rightGoal = rc.getLocation().add(towards.rotateRightDegrees(90), rc.getType().bodyRadius);

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

	static boolean willCollideWithMe(BulletInfo bullet) {
		MapLocation myLocation = rc.getLocation();

		// Get relevant bullet information
		Direction propagationDirection = bullet.dir;
		MapLocation bulletLocation = bullet.location;

		// Calculate bullet relations to this robot
		Direction directionToRobot = bulletLocation.directionTo(myLocation);
		float distToRobot = bulletLocation.distanceTo(myLocation);
		float theta = propagationDirection.radiansBetween(directionToRobot);

		// If theta > 90 degrees, then the bullet is traveling away from us and
		// we can break early
		if (Math.abs(theta) > Math.PI / 2) {
			return false;
		}

		// distToRobot is our hypotenuse, theta is our angle, and we want to
		// know this length of the opposite leg.
		// This is the distance of a line that goes from myLocation and
		// intersects perpendicularly with propagationDirection.
		// This corresponds to the smallest radius circle centered at our
		// location that would intersect with the
		// line that is the path of the bullet.
		float perpendicularDist = (float) Math.abs(distToRobot * Math.sin(theta)); // soh
																					// cah
																					// toa
																					// :)

		return (perpendicularDist <= rc.getType().bodyRadius);
	}

	// public static void tryToWater() throws GameActionException {
	// TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
	// float tempLowestHP = GameConstants.BULLET_TREE_MAX_HEALTH;
	// for (int i = 0; i < nearbyTrees.length; i++) {
	// if (rc.canWater(nearbyTrees[i].getID())) {
	// if (nearbyTrees[i].getHealth() < tempLowestHP) {
	// tempLowestHP = nearbyTrees[i].getID();
	// }
	// }
	// }
	// rc.water((int) tempLowestHP);
	// }
	public static void tryToWater() throws GameActionException {
		TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
		int toWater = 0;
		for (int i = 0; i < nearbyTrees.length; i++) {
			float lowestTree = GameConstants.BULLET_TREE_MAX_HEALTH;
			if (nearbyTrees[i].getHealth() < lowestTree - 5 && rc.canWater(nearbyTrees[i].getLocation())) {
				lowestTree = nearbyTrees[i].getHealth();
				toWater = nearbyTrees[i].getID();
			}
		}
		rc.water(toWater);
	}

	public static void tryToPlant() throws GameActionException {
		if (rc.getTeamBullets() > GameConstants.BULLET_TREE_COST) {
			for (int i = 0; i < 6; i++) {
				if (rc.canPlantTree(plantDirList[i])) {
					rc.plantTree(plantDirList[i]);
					break;
				}
			}
		}
	}

	/*
	 * public static void tryToPlant() throws GameActionException { // try to
	 * build gardeners // can you build a gardener? if (rc.getTeamBullets() >
	 * GameConstants.BULLET_TREE_COST) {// have // enough // bullets. //
	 * assuming // we // haven't // built // already. for (int i = 0; i < 8;
	 * i++) { // only plant trees on a sub-grid
	 * 
	 * MapLocation p = rc.getLocation().add(dirList[i],GameConstants.
	 * GENERAL_SPAWN_OFFSET+GameConstants.BULLET_TREE_RADIUS+rc.
	 * getType().bodyRadius);
	 * 
	 * if(modGood(p.x,1,0.1f)&&modGood(p.y,1,0.1f)) { if
	 * (rc.canPlantTree(dirList[i])) { rc.plantTree(dirList[i]); break; } } } }
	 */

	// public static void tryToPlant() throws GameActionException {
	// if (rc.getTeamBullets() > GameConstants.BULLET_TREE_COST) {
	// for (int x = 0; x < 8; x++) {
	// if (rc.canPlantTree(dirList[x])) {
	// rc.plantTree(dirList[x]);
	// break;
	// }
	// }
	// }
	//
	// }
	public static void getTrees() {

	}

	public static void garMove() throws GameActionException {

		MapLocation archonLocation = new MapLocation(rc.readBroadcast(0), rc.readBroadcast(1));
		Direction going = new Direction(rc.readBroadcast(0), rc.readBroadcast(1));
		if (rc.getLocation().distanceTo(archonLocation) < 4) {

			if (rc.getLocation().directionTo(archonLocation) == Direction.getEast()) {
				going = Direction.getWest();
			} else if (rc.getLocation().directionTo(archonLocation) == Direction.getWest()) {
				going = Direction.getEast();
			} else if (rc.getLocation().directionTo(archonLocation) == Direction.getNorth()) {
				going = Direction.getSouth();
			} else if (rc.getLocation().directionTo(archonLocation) == Direction.getSouth()) {
				going = Direction.getNorth();
			} else {
				going = randomDir();
			}
			if (rc.canMove(going))
				rc.move(going, 5);
		}
	}

	static RobotType enemySpot() throws GameActionException {
		RobotInfo[] r = rc.senseNearbyRobots();

		for (RobotInfo ri : r) {

			if (ri.getTeam() != rc.getTeam()) {
				if (ri.getTeam() != rc.getTeam() && ri.getType() == RobotType.ARCHON) {

					MapLocation enemyLoc = ri.getLocation();
					rc.broadcast(201, (int) enemyLoc.x);
					rc.broadcast(202, (int) enemyLoc.y);

					return ri.getType();
				} else if (ri.getTeam() != rc.getTeam()) {
					MapLocation enemyLoc = ri.getLocation();
					rc.broadcast(10, (int) enemyLoc.x);
					rc.broadcast(11, (int) enemyLoc.y);
					if (!rc.canSenseRobot(ri.getID())) {
						rc.broadcast(10, 0);
						rc.broadcast(11, 0);

					}
					return ri.getType();
				} else {
					rc.broadcast(201, 0);
					rc.broadcast(202, 0);
					rc.broadcast(10, 0);
					rc.broadcast(11, 0);
				}
			}

		}
		return null;

	}

	static void towardEnemyArchon() throws GameActionException {
		MapLocation go = new MapLocation(rc.readBroadcast(10), rc.readBroadcast(11));
		if (rc.readBroadcast(201) != 0 && rc.readBroadcast(202) != 0)
			if (rc.canMove(go))
				moveToTarget(go);
	}

	static boolean towardEnemy() throws GameActionException {
		nearbyTrees = rc.senseNearbyTrees();
		MapLocation go = new MapLocation(rc.readBroadcast(10), rc.readBroadcast(11));
		if (rc.readBroadcast(10) != 0 && rc.readBroadcast(11) != 0) {
			if (rc.canMove(go)) {
				// if
				// (!rc.isCircleOccupied(go.add(rc.getLocation().directionTo(go),
				// 2), 1)) {
				moveToTarget(go);
				return true;
			}

		}
		return false;

	}

	public static boolean modGood(float number, float spacing, float fraction) {
		return (number % spacing) < spacing * fraction;
	}
	  static int[] convertMapLocation(MapLocation map) {
	        float xcoord = map.x;
	        float ycoord = map.y;
	        int[] returnarray = new int[4];
	        returnarray[0] = Math.round(xcoord - (xcoord % 1));
	        returnarray[1] = Math.toIntExact(Math.round((xcoord % 1)*Math.pow(10,6)));
	        returnarray[2] = Math.round(ycoord - (ycoord % 1));
	        returnarray[3] = Math.toIntExact(Math.round((ycoord % 1)*Math.pow(10,6)));
	        return(returnarray);
	    }

	    /**
	     *
	     * @param arr An array arr such that:
	     *          arr[0] - integer part of x
	     *          arr[1] - decimal part of x * 10^6 and rounded
	     *          arr[2] - integer part of y
	     *          arr[3] - decimal part of y * 10^6 and rounded
	     * @return A MapLocation instantiated from the coordinates given by array
	     */
	    static MapLocation convertLocationInts(int[] arr) {
	        float xcoord = (float)(arr[0] + arr[1]/Math.pow(10,6));
	        float ycoord = (float)(arr[2] + arr[3]/Math.pow(10,6));
	        return(new MapLocation(xcoord,ycoord));
	    }

	    static MapLocation readLocation(int firstChannel) throws GameActionException{
	        int[] array = new int[4];
	        array[0] = rc.readBroadcast(firstChannel);
	        array[1] = rc.readBroadcast(firstChannel+1);
	        array[2] = rc.readBroadcast(firstChannel+2);
	        array[3] = rc.readBroadcast(firstChannel+3);
	        return convertLocationInts(array);
	    }

	    static void writeLocation(MapLocation map, int firstChannel) throws GameActionException{
	        int[] arr = convertMapLocation(map);
	        rc.broadcast(firstChannel, arr[0]);
	        rc.broadcast(firstChannel + 1, arr[1]);
	        rc.broadcast(firstChannel+2, arr[2]);
	        rc.broadcast(firstChannel+3, arr[3]);
	    }

	    // this is the slugs "tail" imagine leaving a trail of sticky goo on the map that you don't want to step in that slowly dissapates over time
	   


	    private static boolean slugMoveToTarget(MapLocation target, float strideRadius) throws GameActionException{

	        // when trying to move, let's look forward, then incrementing left and right.
	        float[] toTry = {0, (float)Math.PI/4, (float)-Math.PI/4, (float)Math.PI/2, (float)-Math.PI/2, 3*(float)Math.PI/4, -3*(float)Math.PI/4, -(float)Math.PI};

	        MapLocation ourLoc = rc.getLocation();
	        Direction toMove = ourLoc.directionTo(target);

	        // let's try to find a place to move!
	        for (int i = 0; i < toTry.length; i++) {
	            Direction dirToTry = toMove.rotateRightDegrees(toTry[i]);
	            if (rc.canMove(dirToTry, strideRadius)) {
	                // if that location is free, let's see if we've already moved there before (aka, it's in our tail)
	                MapLocation newLocation = ourLoc.add(dirToTry, strideRadius);
	                boolean haveWeMovedThereBefore = false;
	                for (int j = 0; j < oldLocations.size(); j++) {
	                    if (newLocation.distanceTo(oldLocations.get(j)) < strideRadius * strideRadius) {
	                        haveWeMovedThereBefore = true;
	                        break;
	                    }
	                }
	                if (!haveWeMovedThereBefore) {
	                    oldLocations.add(newLocation);
	                    if (oldLocations.size() > 10) {
	                        // remove the head and chop the list down to size 10 (or whatever you want to use)
	                    }
	                    if (! rc.hasMoved() && rc.canMove(dirToTry, strideRadius)) {
	                        rc.move(dirToTry, strideRadius);
	                    }
	                    return(true);
	                }

	            }
	        }
	        //looks like we can't move anywhere
	        return(false);

	    }

	    private static boolean moveToTarget(MapLocation location) throws GameActionException{
	        // try to take a big step
	        if (slugMoveToTarget(location, rc.getType().strideRadius)) {
	            return(true);
	        }
	        // try to take a smaller step
	        if (slugMoveToTarget(location, rc.getType().strideRadius/2)) {
	            return(true);
	        }
	        // try to take a baby step
	        if (slugMoveToTarget(location, rc.getType().strideRadius/4)) {
	            return(true);
	        }
	        else {
	        	goingDir =  rc.getLocation().directionTo(rc.getInitialArchonLocations(rc.getTeam().opponent())[0]);
	            return(false);
	        }
	        // insert move randomly code here

	    }

	    public static void swarm(MapLocation location, int quantity) throws GameActionException{
	        int rollcall = 0;
	        if (swarm) {
	            moveToTarget(rc.getInitialArchonLocations(rc.getTeam().opponent())[0]);
	            return;
	        }
	        RobotInfo[] bots = rc.senseNearbyRobots();
	        for (RobotInfo b : bots) {
	            if (b.getTeam() == rc.getTeam() && b.getType() == RobotType.GARDENER) {
	                rollcall += 1;
	            }
	        }
	        if (rollcall >= quantity) {
	            swarm = true;
	            moveToTarget(rc.getInitialArchonLocations(rc.getTeam().opponent())[0]);
	        }
	        else {
	            moveToTarget(location);
	        }
	    }

	    public static void wander() throws GameActionException {
	    	Direction dir =  rc.getLocation().directionTo(rc.getInitialArchonLocations(rc.getTeam().opponent())[0]);
	        tryMove(dir);
	    }


}
