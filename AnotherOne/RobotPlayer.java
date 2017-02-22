package AnotherOne;

import java.util.ArrayList;
import java.util.Random;

import battlecode.common.*;

public strictfp class RobotPlayer {
	static RobotController rc;
	static Direction[] dirList = new Direction[8];
	static Direction goingDir = Direction.getNorth();
	static Random rand;
	static ArrayList<Integer> ourTreeIDs = new ArrayList<Integer>();
	// Keep broadcasting channels
	static final int GARDENER_CHANNEL = 5;
	static final int LUMBERJACK_CHANNEL = 6;
	static final int TREE_CHANNEL = 8;
	// Keep important numbers here
	static final int GARDENER_MAX = 5;
	static final int LUMBERJACK_MAX = 5;
	//static final int TREE_MAX = 10;

	public static void run(RobotController rc) throws GameActionException {
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

	private static void runTank() {
		while (true) {
			try {

			} catch (Exception e) {
				e.printStackTrace();

			}
		}
		// TODO Auto-generated method stub

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

				build(RobotType.GARDENER, GARDENER_CHANNEL, GARDENER_MAX);
				/*
				 * if (rc.canMove(goingDir)) { rc.move(goingDir); }
				 */Clock.yield();
			} catch (Exception e) {
				e.printStackTrace();

			}
		}

	}

	public static void runGardener() {
		while (true) {
			try {
				if(rc.getHealth() < RobotType.GARDENER.maxHealth)
					rc.broadcast(GARDENER_CHANNEL, rc.readBroadcast(GARDENER_CHANNEL) - 1);
				else
					rc.broadcast(GARDENER_CHANNEL, rc.readBroadcast(GARDENER_CHANNEL) + 1);

				tryToPlant();
				tryToWater();
				// TODO count gardeners

				if (rc.canMove(goingDir)) {
					rc.move(goingDir);
				} else {
					goingDir = randomDir();
				}
				build(RobotType.LUMBERJACK, LUMBERJACK_CHANNEL, LUMBERJACK_MAX);
				if(rc.getRoundNum() > 700){
					tryToBuild(RobotType.TANK, RobotType.TANK.bulletCost);
				}
				if (rc.getRoundNum() > 400) {
					tryToBuild(RobotType.SOLDIER, RobotType.SOLDIER.bulletCost);
				}
				if (rc.getRoundNum() > 600) {
					tryToBuild(RobotType.SCOUT, RobotType.SCOUT.bulletCost);
				}
				Clock.yield();
				// tryToBuild(RobotType.TANK, RobotType.TANK.bulletCost);
			} catch (Exception e) {
				e.printStackTrace();

			}
		}

	}

	public static void runSoldier() {
		while (true) {
			try {
				canShoot(RobotType.SOLDIER, "single");
				if (rc.canMove(goingDir)) {
					rc.move(goingDir);
				} else {
					goingDir = randomDir();
				}

				Clock.yield();
			} catch (Exception e) {
				e.printStackTrace();

			}
		}
	}

	public static void runScout() {
		while (true) {
			try {
				canShoot(RobotType.SOLDIER, "single");
				if (rc.canMove(goingDir)) {
					rc.move(goingDir);
				} else {
					goingDir = randomDir();
				}

				Clock.yield();
			} catch (Exception e) {
				e.printStackTrace();

			}
		}
	}

	public static void runLumberJack() {
		while (true) {
			try {
				if(rc.getHealth() < RobotType.LUMBERJACK.maxHealth)
					rc.broadcast(LUMBERJACK_CHANNEL, rc.readBroadcast(LUMBERJACK_CHANNEL)-1);

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
					System.out.println(ourTreeIDs + " :" + t.getID());
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

				}

				Clock.yield();
			} catch (Exception e) {
				e.printStackTrace();

			}
		}
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
						&& rc.getTeamBullets() >= GameConstants.TRIAD_SHOT_COST) {
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
						&& rc.getTeamBullets() >= GameConstants.PENTAD_SHOT_COST) {
					Direction dir = rc.getLocation().directionTo(b.getLocation());
					rc.firePentadShot(dir);
					if (rc.canMove(dir))
						rc.move(dir);
				}
				break;
			}
		}
	}

	public static void build(RobotType type, int channel, int max) throws GameActionException {
		int prevNumb = rc.readBroadcast(channel);
		if (prevNumb < max) {
			tryToBuild(type, type.bulletCost);
			rc.broadcast(channel, prevNumb++);
				}
			}
		
			
			
		
	

	public static void tryToBuild(RobotType type, int moneyNeeded) throws GameActionException {
		if (rc.getTeamBullets() > moneyNeeded) {
			for (int i = 0; i < 8; i++) {
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
		//int prevTrees = rc.readBroadcast(TREE_CHANNEL);
		if (rc.getTeamBullets() > GameConstants.BULLET_TREE_COST) {
			for (int i = 0; i < 8; i++) {

				MapLocation treeLocations = rc.getLocation().add(dirList[i], GameConstants.GENERAL_SPAWN_OFFSET
						+ GameConstants.BULLET_TREE_RADIUS + rc.getType().bodyRadius);
				if (modGood(treeLocations.x, 6, .2f) && modGood(treeLocations.y, 6, .2f))
					if (rc.canPlantTree(dirList[i]) ) {
						rc.plantTree(dirList[i]);
						//rc.broadcast(TREE_CHANNEL, prevTrees + 1);
					}

						break;
					}
			}

		}
	

	public static boolean modGood(float number, float spacing, float fraction) {
		return (number % spacing) < spacing * fraction;
	}

	public static void treeDie() throws GameActionException {
		int prevTrees = rc.readBroadcast(TREE_CHANNEL);
		if (rc.getRoundNum() % 10 == 0)
			rc.broadcast(TREE_CHANNEL, prevTrees - 1);
	}

}
