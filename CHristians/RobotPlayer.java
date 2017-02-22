
package CHristians;

import java.util.Random;
import java.util.ArrayList;

import battlecode.common.*;

public strictfp class RobotPlayer {
	static RobotController rc;
	static Direction[] dirList = new Direction[8];
	static Direction[] plantDirList = new Direction[6];
	static Direction goingDir = Direction.getNorth();
	static Random rand;
	static int gardeners = 0;
	static int soldiersSpawn = 0;
	static int intialSoldiers = 0;
	static int scouts = 0;
	static boolean enoughGard = false;
	static int fourGard = 0;
	static ArrayList<ArrayList<Integer>> outer = new ArrayList<ArrayList<Integer>>();

	// Keep broadcasting channels
	static final int GARDENER_CHANNEL = 5;
	static final int LUMBERJACK_CHANNEL = 6;
	static final int SOLDIER_CHANNEL = 7;
	static final int SCOUT_CHANNEL = 8;
	static final int TANK_CHANNEL = 9;
	// Keep important numbers here
	static final int GARDENER_MAX = 8;
	static final int LUMBERJACK_MAX = 5;

	public static void run(RobotController rc) throws GameActionException {
		RobotPlayer.rc = rc;
		initDirList();
		initPlantDirList();
		if (rc.getRoundNum() % 5 == 0) {
			rc.broadcast(10, 0);
			rc.broadcast(11, 0);
		}
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
			break;
		default:
			break;
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
	public static void initPlantDirList() {
		for (int i = 0; i < 6; i++) {
			float radians = (float) (-Math.PI + 2 * Math.PI * ((float) i / 6));
			plantDirList[i] = new Direction(radians);
		}
	}
	

	public static void runArchon() {
		while (true) {
			try {
				fourGard = 0;
				RobotInfo[] bots = rc.senseNearbyRobots();
				for (RobotInfo b : bots) {
					if(b.getType() == RobotType.GARDENER)
						fourGard++;
					
				}
				if(fourGard < 4)
					tryToBuild(RobotType.GARDENER, RobotType.GARDENER.bulletCost);
				
				
				enemySpot();
				if(rc.getRobotCount() > 100 && rc.getTeamBullets() > 100){
					rc.donate(100);
				}
				if (rc.getTeamBullets() >= 1000)
					rc.donate(500);
				//if (gardeners < 8)
					//tryToBuild(RobotType.GARDENER, RobotType.GARDENER.bulletCost);

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

				int avSpots = 0;
				for (int x = 0; x < 8; x++) {
					if (rc.canPlantTree(dirList[x])) {
						avSpots++;
					}
				}
				
				if (rc.getRoundNum() > 40 && intialSoldiers < 2) {
					tryToBuild(RobotType.LUMBERJACK, RobotType.LUMBERJACK.bulletCost);
					intialSoldiers++;
				} else if (avSpots > 1) {
					tryToPlant();
				} else if (soldiersSpawn % 3 == 0) {
					tryToBuild(RobotType.SOLDIER, RobotType.SOLDIER.bulletCost);
				} else {
					tryToBuild(RobotType.LUMBERJACK, RobotType.LUMBERJACK.bulletCost);
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
				enemySpot();
				canShoot(RobotType.SOLDIER, "pentad");
				canShoot(RobotType.SOLDIER, "triad");
				canShoot(RobotType.SOLDIER, "single");
				
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

	public static void runScout() {
		while (true) {
			try {
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
					if (rc.canMove(dir))
						rc.move(dir);
				}
				break;
			}

		} else {
			switch (typeOfShot) {
			case "single":
				for (RobotInfo b : bots) {
					if (b.getTeam() != rc.getTeam() && shooter.canAttack()
							&& rc.getTeamBullets() >= GameConstants.SINGLE_SHOT_COST) {
						Direction dir = rc.getLocation().directionTo(b.getLocation());
						rc.fireSingleShot(dir);
						if (b.getType() == RobotType.LUMBERJACK && rc.canMove(dir.opposite())){
							rc.move(dir.opposite());
						}
					}
					break;
				}

			case "triad":
				for (RobotInfo b : bots) {
					if (b.getTeam() != rc.getTeam() && shooter.canAttack()
							&& rc.getTeamBullets() >= GameConstants.TRIAD_SHOT_COST) {
						Direction dir = rc.getLocation().directionTo(b.getLocation());
						rc.fireTriadShot(dir);
//						if (rc.canMove(dir))
//							rc.move(dir);
						if (b.getType() == RobotType.LUMBERJACK && rc.canMove(dir.opposite())){
							rc.move(dir.opposite());
						}
					}
					break;
				}

			case "pentad":
				for (RobotInfo b : bots) {
					if (b.getTeam() != rc.getTeam() && shooter.canAttack()
							&& rc.getTeamBullets() >= GameConstants.PENTAD_SHOT_COST) {
						Direction dir = rc.getLocation().directionTo(b.getLocation());
						rc.firePentadShot(dir);
//						if (rc.canMove(dir))
//							rc.move(dir);
						if (b.getType() == RobotType.LUMBERJACK && rc.canMove(dir.opposite())){
							rc.move(dir.opposite());
						}
					}
					break;
				}
			}
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
					if (rc.canBuildRobot(type, dirList[i])) {
						gardeners++;
						rc.buildRobot(type, dirList[i]);
						break;
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
				MapLocation enemyLoc = ri.getLocation();
				rc.broadcast(10, (int) enemyLoc.x);
				rc.broadcast(11, (int) enemyLoc.y);
				return ri.getType();
			}

		}
		return null;

	}

	static boolean towardEnemy() throws GameActionException {
		MapLocation go = new MapLocation(rc.readBroadcast(10), rc.readBroadcast(11));
		if (rc.readBroadcast(10) != 0 && rc.readBroadcast(11) != 0)
			if (rc.canMove(go)){
				rc.move(go);
				return true;
			}
			return false;
	}

	public static boolean modGood(float number, float spacing, float fraction) {
		return (number % spacing) < spacing * fraction;
	}

}




