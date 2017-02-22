package DontShoot;

import java.util.Random;
import java.util.ArrayList;

import battlecode.common.*;

/// if enemy archon is close, < 20 units away spawn 2 soldiers imideteally
///Could have gardeners be atleast  8 units away from each other
//soldiers not shoot if ally infront of them
public strictfp class RobotPlayer {
	static TreeInfo[] nearbyTrees;
	static RobotController rc;
	static Direction[] dirList = new Direction[8];
	static Direction goingDir = Direction.getNorth();
	static Direction archonGo = Direction.getNorth();
	static Direction[] gardenerSpawn = new Direction[16];
	static float closestToEnemyArchon;
	static boolean archonGoTest = false;
	static boolean spawnedOneGardener = false;
	static Random rand;
	static Direction noTree;
	static int gardeners = 0;
	static int soldiersSpawn = 0;
	static int intialSoldiers = 0;
	static int scouts = 0;
	static boolean enoughGard = false;
	static boolean needSoldiers = false;
	static int hasSpawnedTwo = 0;

	static int fourGard = 0;
	static int archon = 1;
	static float distanceToEnemyArchon = 0;

	static int numArchons;
	static int archonMoved = 0;
	static Direction[] bigList = new Direction[18];
	static Direction[] plantDirList = new Direction[6];
	static ArrayList<MapLocation> ourBots;
	static ArrayList<ArrayList<Integer>> outer = new ArrayList<ArrayList<Integer>>();
	// static boolean foundSpot = false;
	// Keep broadcasting channels
	static final int GARDENER_CHANNEL = 5;
	static final int LUMBERJACK_CHANNEL = 6;
	static final int SOLDIER_CHANNEL = 7;
	static final int SCOUT_CHANNEL = 8;
	static final int TANK_CHANNEL = 9;
	static final int ARCHON_CHANNEL = 100;
	static final int GARDENER_FOUNDSPOT = 10;
	static ArrayList<Integer> gardenerFoundSpotID = new ArrayList<Integer>();
	// Keep important numbers here
	static final int GARDENER_MAX = 8;
	static final int LUMBERJACK_MAX = 5;
	static final int TANK_MAX = 100;
	static int ENEMY_ARCHON_CHANNEL = 50;
	static int ENEMY_ARCHON_SPOTTED = 54;
	static final int isStuck = 11;
	static boolean buildGardener = false;

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
			// if (archon == 1) {
			runArchon();
			// } else
			// runArchonTwoandThree();
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

	public static void initBigList() {
		for (int i = 0; i < 18; i++) {
			float radians = (float) (-Math.PI + 2 * Math.PI * ((float) i / 18));
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

	public static float distanceToEnemyArchon() {
		float closest = rc.getLocation().distanceTo(rc.getInitialArchonLocations(rc.getTeam().opponent())[0]);
		for (int i = 0; i < numArchons; i++) {
			if (rc.getLocation().distanceTo(rc.getInitialArchonLocations(rc.getTeam().opponent())[i]) < closest)
				closest = rc.getLocation().distanceTo(rc.getInitialArchonLocations(rc.getTeam().opponent())[i]);
		}
		return closest;
	}

	public static void runArchon() {
		while (true) {
			try {
				int nearbyGards = 0;
				RobotInfo[] bots = rc.senseNearbyRobots(6);
				for (RobotInfo r : bots) {
					if (r.getTeam() == rc.getTeam() && r.getType() == RobotType.GARDENER)
						nearbyGards++;
				}
				while (!spawnedOneGardener) {
					if (rc.canBuildRobot(RobotType.GARDENER, randomDir())) {
						tryToBuild(RobotType.GARDENER, RobotType.GARDENER.bulletCost);
						spawnedOneGardener = true;
					} else if (rc.canMove(randomDir()))
						rc.move(randomDir());
				}
				if (rc.getRoundNum() % 100 == 0 || nearbyGards < 1) {
					tryToBuild(RobotType.GARDENER, RobotType.GARDENER.bulletCost);
				}

				numArchons = GameConstants.NUMBER_OF_ARCHONS_MAX;
				closestToEnemyArchon = rc.getLocation()
						.directionTo(rc.getInitialArchonLocations(rc.getTeam().opponent())[0]).getAngleDegrees();

				distanceToEnemyArchon = distanceToEnemyArchon();
				// enemyArchon =
				// rc.getInitialArchonLocations(rc.getTeam().opponent())[0];
				// int readyToSpawnNum = rc.readBroadcast(readyToSpawn);
				// int gardenerSaysYes = rc.readBroadcast(gardYes);
				// if(readyToSpawnNum == gardenerSaysYes){
				// if(tryToBuild(RobotType.GARDENER,
				// RobotType.GARDENER.bulletCost)){
				// rc.broadcast(readyToSpawn, 2);
				// rc.broadcast(gardYes, 0);
				// //System.out.println("Built a gardener.
				// rc.readBroadcast(readyToSpawn) = " +
				// rc.readBroadcast(readyToSpawn)+"rc.readBroadcast(gardYes)
				// "+
				// rc.readBroadcast(gardYes));
				// }
				// }
				Direction ranMove = randomDir();
				// if (avGardenerSpots() < 1) {
				//
				// if (rc.canMove(ranMove)) {
				// rc.move(ranMove);
				// }
				//
				// } else {

				for (RobotInfo b : bots) {
					if ((b.getType() == RobotType.GARDENER || b.getType() == RobotType.ARCHON)
							&& b.getTeam() == rc.getTeam()) {
						if (rc.canMove(rc.getLocation().directionTo(b.getLocation()).opposite())) {
							rc.move(rc.getLocation().directionTo(b.getLocation()).opposite());
							break;
						}
					}
				}

				if (rc.canMove(ranMove)) {
					rc.move(ranMove);
				} else {

					//
					//
					// if (fourGard < 4)
					// tryToBuild(RobotType.GARDENER,
					// RobotType.GARDENER.bulletCost);

					goingDir = rc.getLocation().directionTo(rc.getInitialArchonLocations(rc.getTeam().opponent())[0]);
					int shouldIDodoge = 0;

					archon = 0;
					fourGard = 0;

					for (RobotInfo b : bots) {
						if (b.getTeam() != rc.getTeam()) {
							shouldIDodoge++;
						}
					}
					// if (fourGard == 3 && archonGoTest == false) {
					// for (int x = 0; x < 8; x++) {
					// if (rc.canMove(dirList[x]))
					// archonGo = dirList[x];
					// }
					// }
					// if (fourGard == 3)
					// if (rc.canMove(archonGo) && archonMoved < 8) {
					// rc.move(archonGo);
					// archonMoved++;
					// }

					enemySpot();
					if (rc.getRobotCount() > 100 && rc.getTeamBullets() > 2000) {
						rc.donate(150 * rc.getVictoryPointCost());
					} else if (rc.getRobotCount() > 100 && rc.getTeamBullets() > 100) {
						rc.donate(10 * rc.getVictoryPointCost());
					} else if (rc.getTeamBullets() >= 1000 && rc.getRoundNum() > 750)
						rc.donate(50 * rc.getVictoryPointCost());
					else {
						// if (gardeners < 8)
						// tryToBuild(RobotType.GARDENER,
						// RobotType.GARDENER.bulletCost);

						MapLocation myLocation = rc.getLocation();
						rc.broadcast(0, (int) myLocation.x);
						rc.broadcast(1, (int) myLocation.y);
						// if (rc.canMove(goingDir)) {
						// rc.move(goingDir);
						if (shouldIDodoge > 3) {
							dodge();
						}
					}

				}
			} catch (Exception e) {
				e.printStackTrace();

			}
		}

	}

	public static int avGardenerSpots() throws GameActionException {
		// float main = Float.MAX_VALUE;
		int availableGardenerSpots = 0;
		// float first = Math.abs(plantDirList[0].getAngleDegrees() -
		// closestToEnemyArchon);
		for (int i = 0; i < 16; i++) {
			if (rc.canBuildRobot(RobotType.GARDENER, gardenerSpawn[i])) {
				rc.setIndicatorDot(rc.getLocation().add(gardenerSpawn[i]), 1, 1, 1);
				availableGardenerSpots++;
				// float temp = plantDirList[i].getAngleDegrees();
				// if(Math.abs(temp - closestToEnemyArchon) < first){
				// main = temp;
				// noTree = plantDirList[i];
			}

		}
		return availableGardenerSpots;
	}

	// public static void runArchon() {
	// while (true) {
	// try {
	// noTree =
	// rc.getLocation().directionTo(rc.getInitialArchonLocations(rc.getTeam().opponent())[0]);
	// if (fourGard < 4){
	// if(tryToBuild(RobotType.GARDENER, RobotType.GARDENER.bulletCost)){
	//
	// }else
	// for (int x = 0; x < 8; x++) {
	// if (rc.canMove(dirList[x]))
	// archonGo = dirList[x];
	// }
	// }
	//
	//
	// goingDir =
	// rc.getLocation().directionTo(rc.getInitialArchonLocations(rc.getTeam().opponent())[0]);
	// int shouldIDodoge = 0;
	// numArchons = GameConstants.NUMBER_OF_ARCHONS_MAX;
	// archon = 0;
	// fourGard = 0;
	// RobotInfo[] bots = rc.senseNearbyRobots();
	// for (RobotInfo b : bots) {
	// if (b.getTeam() != rc.getTeam())
	// shouldIDodoge++;
	// if (b.getType() == RobotType.GARDENER)
	// fourGard++;
	//
	// }
	// if (fourGard == 3 && archonGoTest == false) {
	// for (int x = 0; x < 8; x++) {
	// if (rc.canMove(dirList[x]))
	// archonGo = dirList[x];
	// }
	// }
	// if (fourGard == 3)
	// if (rc.canMove(archonGo) && archonMoved < 8) {
	// rc.move(archonGo);
	// archonMoved++;
	// }
	//
	// enemySpot();
	// if (rc.getRoundNum() > 200 && rc.getTeamBullets() > 500) {
	// rc.donate(40 * rc.getVictoryPointCost());
	// }
	// if (rc.getTeamBullets() >= 1000 && rc.getRoundNum() > 750)
	// rc.donate(50 * rc.getVictoryPointCost());
	// // if (gardeners < 8)
	// // tryToBuild(RobotType.GARDENER,
	// // RobotType.GARDENER.bulletCost);
	//
	// MapLocation myLocation = rc.getLocation();
	// rc.broadcast(0, (int) myLocation.x);
	// rc.broadcast(1, (int) myLocation.y);
	// // if (rc.canMove(goingDir)) {
	// // rc.move(goingDir);
	// if (shouldIDodoge > 3) {
	// dodge();
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	//
	// }
	// }
	//
	// }

	public static void runArchonTwoandThree() {
		while (true) {
			try {
				dodge();
				archon = 1;
				fourGard = 0;
				RobotInfo[] bots = rc.senseNearbyRobots();
				for (RobotInfo b : bots) {
					if (b.getType() == RobotType.GARDENER)
						fourGard++;

				}
				if (fourGard < 4)
					tryToBuild(RobotType.GARDENER, RobotType.GARDENER.bulletCost);

				enemySpot();
				if (rc.getRobotCount() > 100 && rc.getTeamBullets() > 100) {
					rc.donate(100);
				}
				if (rc.getTeamBullets() >= 1000)
					rc.donate(500);
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
				
				
				if (distanceToEnemyArchon < 35 && hasSpawnedTwo < 3) {
					spawnTwo();
				}
				TreeInfo[] trees = rc.senseNearbyTrees(2);
				int treesNear = 0;
				for (TreeInfo t : trees) {
					if (t.getTeam() != rc.getTeam())
						treesNear++;
				}
				if (treesNear > 4) {
					tryToBuild(RobotType.LUMBERJACK, RobotType.LUMBERJACK.bulletCost);
				} else {
					int robotID = rc.getID();
					boolean otherGardener = true;
					if (!gardenerFoundSpotID.contains(robotID)) {
						RobotInfo[] bots = rc.senseNearbyRobots(5, rc.getTeam());
						for (RobotInfo b : bots) {
							if (b.getType() == RobotType.GARDENER) {
								otherGardener = false;
								if (rc.canMove(rc.getLocation().directionTo(b.getLocation()).opposite())) {
									rc.move(rc.getLocation().directionTo(b.getLocation()).opposite());
								}
							}
						}
						Direction ranMove = randomDir();
						if (rc.canMove(ranMove)) {
							rc.move(ranMove);
						}

					}
					if ((!rc.isCircleOccupiedExceptByThisRobot(rc.getLocation(), RobotType.GARDENER.bodyRadius * 2)
							&& otherGardener) || gardenerFoundSpotID.contains(rc.getID())) {
						if (!gardenerFoundSpotID.contains(robotID)) {
							gardenerFoundSpotID.add(robotID);
						}

						if (rc.getRoundNum() > 300) {
							tryToBuild(RobotType.TANK, RobotType.TANK.bulletCost);
						}
						RobotInfo[] bots = rc.senseNearbyRobots();
						for (RobotInfo c : bots) {
							if (c.getTeam() != rc.getTeam())
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
						// (float)1.0, rc.getLocation().add(dirList[x], 1) ,
						// (float)1.0)
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

						if (rc.getRoundNum() > 100 && intialSoldiers < 4) {
							tryToBuild(RobotType.LUMBERJACK, RobotType.LUMBERJACK.bulletCost);
							intialSoldiers++;
						} else if (avTreeSpots > 2) {
							tryToPlant();
						} else if (soldiersSpawn % 2 == 0 || soldiersSpawn % 20 == 0) {
							tryToBuild(RobotType.SOLDIER, RobotType.SOLDIER.bulletCost);
						} else if (rc.getRoundNum() > 300) {
							tryToBuild(RobotType.TANK, RobotType.TANK.bulletCost);

						} else {
							tryToBuild(RobotType.LUMBERJACK, RobotType.LUMBERJACK.bulletCost);
						}
						tryToWater();
						dodge();

						// if (rc.canMove(goingDir)) {
						// rc.move(goingDir);
						// } else {
						// goingDir = randomDir();
						// }
						// if (rc.getRoundNum() < 300 &&
						// rc.readBroadcast(LUMBERJACK_CHANNEL) <
						// LUMBERJACK_MAX) {
						// build(RobotType.LUMBERJACK, LUMBERJACK_CHANNEL,
						// LUMBERJACK_MAX);
						// }
						// tryToBuild(RobotType.TANK,
						// RobotType.TANK.bulletCost);
					} else {
						int isStuckStill = rc.readBroadcast(isStuck);
						if (isStuckStill < 5) {
							rc.broadcast(isStuck, isStuckStill++);
						} else {
							if (tryToBuild(RobotType.LUMBERJACK, RobotType.LUMBERJACK.bulletCost))
								rc.broadcast(isStuck, 0);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void spawnTwo() throws GameActionException {
		Direction towards =rc.getLocation().directionTo(rc.getInitialArchonLocations(rc.getTeam().opponent())[0]);
		if(rc.canBuildRobot(RobotType.GARDENER, towards)){
			rc.buildRobot(RobotType.SOLDIER, towards);
			//hasSpawnedTwo++;
		}else if (rc.canBuildRobot(RobotType.GARDENER, towards.rotateLeftDegrees(30))){
			rc.buildRobot(RobotType.GARDENER, towards.rotateLeftDegrees(30));
			//hasSpawnedTwo++;
		}else if (rc.canBuildRobot(RobotType.GARDENER, towards.rotateRightDegrees(30))){
			rc.buildRobot(RobotType.GARDENER, towards.rotateRightDegrees(30));
			//hasSpawnedTwo++;
		}else if (rc.canBuildRobot(RobotType.GARDENER, towards.rotateLeftDegrees(60))){
			rc.buildRobot(RobotType.GARDENER, towards.rotateLeftDegrees(60));
			//hasSpawnedTwo++;
		}else if (rc.canBuildRobot(RobotType.GARDENER, towards.rotateRightDegrees(60))){
			rc.buildRobot(RobotType.GARDENER, towards.rotateRightDegrees(60));
			//hasSpawnedTwo++;
		}else if (rc.canBuildRobot(RobotType.GARDENER, towards.rotateLeftDegrees(90))){
			rc.buildRobot(RobotType.GARDENER, towards.rotateLeftDegrees(90));
			//hasSpawnedTwo++;
		}else if (rc.canBuildRobot(RobotType.GARDENER, towards.rotateRightDegrees(90))){
			rc.buildRobot(RobotType.GARDENER, towards.rotateRightDegrees(90));
			//hasSpawnedTwo++;
		}else
		tryToBuild(RobotType.SOLDIER, RobotType.SOLDIER.bulletCost);
		hasSpawnedTwo++;
		System.out.println("hasSpawnnd: " + hasSpawnedTwo);
	}

	public static void runSoldier() {
		while (true) {
			try {

				dodge();
				enemySpot();

				canShoot(RobotType.SOLDIER, "pentad");
				canShoot(RobotType.SOLDIER, "triad");
				canShoot(RobotType.SOLDIER, "single");
				if (!rc.hasAttacked()) {
					// towardEnemy();
					moveAroundTrees();
					towardEnemyArchon();
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

				moveAroundTrees();
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
					moveAroundTrees();

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

	public static boolean dontShootAllies(Direction dir) {
		RobotInfo[] bots = rc.senseNearbyRobots(5);
		for (RobotInfo r : bots) {
			if (dir.degreesBetween(rc.getLocation().directionTo(r.getLocation())) < 15)
				return true;
		}
		return false;

	}

	public static void canShoot(RobotType shooter, String typeOfShot) throws GameActionException {
		int botsInLOS = 0;
		RobotInfo[] bots = rc.senseNearbyRobots();
		RobotInfo[] fbots = rc.senseNearbyRobots(2);
		int friendlys = 0;
		botsInLOS = 0;
		for (RobotInfo r : bots) {
			if (r.getTeam() != rc.getTeam()
					&& rc.getLocation().distanceTo(r.getLocation()) < RobotType.SOLDIER.bulletSightRadius)
				botsInLOS++;
		}
		for (RobotInfo r : fbots) {
			if (r.getTeam() == rc.getTeam())
				friendlys++;
		}
		if (rc.getType() == RobotType.SCOUT) {
			for (RobotInfo b : bots) {
				if (b.getTeam() != rc.getTeam() && shooter.canAttack()
						&& rc.getTeamBullets() >= GameConstants.SINGLE_SHOT_COST && b.getType() == RobotType.GARDENER) {
					Direction dir = rc.getLocation().directionTo(b.getLocation());
					if (dontShootAllies(dir))
						rc.fireSingleShot(dir);
					if (b.getType() == RobotType.LUMBERJACK && rc.canMove(dir.opposite())) {
						rc.move(dir.opposite());
					}
				}
				break;
			}

		} else if (friendlys >= 1) {
			for (RobotInfo b : bots) {

				if (b.getTeam() != rc.getTeam() && shooter.canAttack()
						&& rc.getTeamBullets() >= GameConstants.SINGLE_SHOT_COST) {
					Direction dir = rc.getLocation().directionTo(b.getLocation());
					if (dontShootAllies(dir))
						rc.fireSingleShot(dir);

					if (b.getType() == RobotType.LUMBERJACK && rc.canMove(dir.opposite())) {
						rc.move(dir.opposite());
						break;
					}
				}
			}

		} else {
			for (RobotInfo b : bots) {

				if (b.getTeam() != rc.getTeam() && shooter.canAttack()
						&& rc.getTeamBullets() >= GameConstants.SINGLE_SHOT_COST
						&& rc.getLocation().distanceTo(b.getLocation()) > 5 && botsInLOS < 1) {
					Direction dir = rc.getLocation().directionTo(b.getLocation());
					if (dontShootAllies(dir))
						rc.fireSingleShot(dir);

					if (b.getType() == RobotType.LUMBERJACK && rc.canMove(dir.opposite())) {
						rc.move(dir.opposite());
						break;
					}
				} else if (b.getTeam() != rc.getTeam() && shooter.canAttack()
						&& rc.getTeamBullets() >= GameConstants.TRIAD_SHOT_COST
						&& rc.getLocation().distanceTo(b.getLocation()) > 2.5
						&& rc.getLocation().distanceTo(b.getLocation()) < 5 && botsInLOS < 3) {
					Direction dir = rc.getLocation().directionTo(b.getLocation());
					if (dontShootAllies(dir))
						rc.fireTriadShot(dir);

					if (b.getType() == RobotType.LUMBERJACK && rc.canMove(dir.opposite())) {
						rc.move(dir.opposite());
						break;
					}
				} else if (b.getTeam() != rc.getTeam() && shooter.canAttack()
						&& rc.getTeamBullets() >= GameConstants.PENTAD_SHOT_COST && friendlys < 5) {
					Direction dir = rc.getLocation().directionTo(b.getLocation());
					if (dontShootAllies(dir))
						rc.firePentadShot(dir);
					if (b.getType() == RobotType.LUMBERJACK && rc.canMove(dir.opposite())) {
						rc.move(dir.opposite());
						break;

					}

				}
			}
		}
	}

	/*
	 * public static void canShoot(RobotType shooter, String typeOfShot) throws
	 * GameActionException { RobotInfo[] bots = rc.senseNearbyRobots(); if
	 * (rc.getType() == RobotType.SCOUT) { for (RobotInfo b : bots) { if
	 * (b.getTeam() != rc.getTeam() && shooter.canAttack() &&
	 * rc.getTeamBullets() >= GameConstants.SINGLE_SHOT_COST && b.getType() ==
	 * RobotType.GARDENER) { Direction dir =
	 * rc.getLocation().directionTo(b.getLocation()); rc.fireSingleShot(dir); if
	 * (b.getType() == RobotType.LUMBERJACK && rc.canMove(dir.opposite())) {
	 * rc.move(dir.opposite()); } } break; }
	 * 
	 * } else { switch (typeOfShot) { case "single": for (RobotInfo b : bots) {
	 * if (b.getTeam() != rc.getTeam() && shooter.canAttack() &&
	 * rc.getTeamBullets() >= GameConstants.SINGLE_SHOT_COST) { Direction dir =
	 * rc.getLocation().directionTo(b.getLocation()); rc.fireSingleShot(dir);
	 * 
	 * if (b.getType() == RobotType.LUMBERJACK && rc.canMove(dir.opposite())) {
	 * rc.move(dir.opposite()); } } break; }
	 * 
	 * case "triad": for (RobotInfo b : bots) { if (b.getTeam() != rc.getTeam()
	 * && shooter.canAttack() && rc.getTeamBullets() >=
	 * GameConstants.TRIAD_SHOT_COST) { Direction dir =
	 * rc.getLocation().directionTo(b.getLocation()); rc.fireTriadShot(dir);
	 * 
	 * if (b.getType() == RobotType.LUMBERJACK && rc.canMove(dir.opposite())) {
	 * rc.move(dir.opposite()); } } break; }
	 * 
	 * case "pentad": for (RobotInfo b : bots) { if (b.getTeam() != rc.getTeam()
	 * && shooter.canAttack() && rc.getTeamBullets() >=
	 * GameConstants.PENTAD_SHOT_COST) { Direction dir =
	 * rc.getLocation().directionTo(b.getLocation()); rc.firePentadShot(dir); if
	 * (b.getType() == RobotType.LUMBERJACK && rc.canMove(dir.opposite())) {
	 * rc.move(dir.opposite());
	 * 
	 * } break; } } } } }
	 */

	public static void build(RobotType type, int channel, int max) throws GameActionException {
		int prevNumLumb = rc.readBroadcast(channel);
		if (prevNumLumb < max) {
			tryToBuild(type, type.bulletCost);
			rc.broadcast(channel, prevNumLumb++);
		}
	}

	public static boolean tryToBuild(RobotType type, int moneyNeeded) throws GameActionException {

		if (rc.getTeamBullets() > moneyNeeded) {
			if (type == RobotType.GARDENER) {
				for (int i = 0; i < 8; i += 2) {
					if (rc.canBuildRobot(type, dirList[i]) && rc.canMove(rc.getLocation().add(dirList[i], 3))) {
						// if((rc.isLocationOccupiedByTree(rc.getLocation().add(dirList[i],
						// RobotType.GARDENER.bodyRadius * 3)) == false) ){
						gardeners++;
						rc.buildRobot(type, dirList[i]);
						return true;
						// }

					}
				}
				for (int i = 0; i < 18; i += 2) {
					if (rc.canBuildRobot(type, bigList[i]) && rc.canMove(rc.getLocation().add(bigList[i], 3))) {
						// if((rc.isLocationOccupiedByTree(rc.getLocation().add(dirList[i],
						// RobotType.GARDENER.bodyRadius * 3)) == false) ){
						gardeners++;
						rc.buildRobot(type, bigList[i]);
						return true;
					}
				}
			} else {
				for (int i1 = 0; i1 < 8; i1++) {
					if (rc.canBuildRobot(type, dirList[i1])) {
						rc.buildRobot(type, dirList[i1]);
						return true;
					}
				}
				for (int i = 0; i < 18; i += 2) {
					if (rc.canBuildRobot(type, bigList[i]) && rc.canMove(rc.getLocation().add(bigList[i], 3))) {
						// if((rc.isLocationOccupiedByTree(rc.getLocation().add(dirList[i],
						// RobotType.GARDENER.bodyRadius * 3)) == false) ){
						gardeners++;
						rc.buildRobot(type, bigList[i]);
						return true;
					}
				}
			}

		}
		return false;
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

	public static int avSpots() throws GameActionException {

		int availableSpots = 0;

		for (int i = 0; i < 6; i++) {
			if (rc.canPlantTree(plantDirList[i])) {
				availableSpots++;

			}

		}
		return availableSpots;
	}

	// public static boolean tryToPlant() throws GameActionException {
	// // this could be better by putting trees in an array
	//
	// if (rc.getTeamBullets() > GameConstants.BULLET_TREE_COST && avSpots() >
	// 1) {
	// for (int i = 0; i < 6; i++) {
	// // rc.setIndicatorDot(enemyArchon, 1, 1, 1);
	// if (rc.canPlantTree(plantDirList[i])
	// && Math.abs(plantDirList[i].getAngleDegrees() - noTree.getAngleDegrees())
	// > 20) {
	// rc.plantTree(plantDirList[i]);
	// return true;
	// }
	// }
	// }
	// return false;
	// }
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
					return ri.getType();
				} else if (rc.getRoundNum() % 10 == 0) {
					rc.broadcast(10, 0);
					rc.broadcast(11, 0);
				} else {
					rc.broadcast(201, 0);
					rc.broadcast(202, 0);
				}
			}

		}
		return null;

	}

	// static void towardEnemyArchon() throws GameActionException {
	// MapLocation go = new MapLocation(rc.readBroadcast(201),
	// rc.readBroadcast(202));
	// if (rc.readBroadcast(201) != 0 && rc.readBroadcast(202) != 0)
	// if (rc.canMove(go))
	// rc.move(go);
	// }
	//
	// static void towardEnemy() throws GameActionException {
	// nearbyTrees = rc.senseNearbyTrees();
	// MapLocation go = new MapLocation(rc.readBroadcast(10),
	// rc.readBroadcast(11));
	// if (rc.readBroadcast(10) != 0 && rc.readBroadcast(11) != 0) {
	// if (rc.canMove(go)) {
	// // if
	// // (!rc.isCircleOccupied(go.add(rc.getLocation().directionTo(go),
	// // 2), 1)) {
	// rc.move(rc.getLocation().directionTo(go));
	//
	// /*
	// * }else if
	// * (!rc.isCircleOccupiedExceptByThisRobot(go.add(rc.getLocation(
	// * ).directionTo(go).rotateLeftDegrees(90), 1), 1)){
	// * rc.move(rc.getLocation().directionTo(go).rotateRightDegrees(
	// * 90)); }else if
	// * (!rc.isCircleOccupiedExceptByThisRobot(go.add(rc.getLocation(
	// * ).directionTo(go).rotateRightDegrees(90), 1), 1)){
	// * rc.move(rc.getLocation().directionTo(go).rotateLeftDegrees(90
	// * )); }else{
	// * rc.move(rc.getLocation().directionTo(go).rotateLeftDegrees(
	// * 180)); }
	// */
	// // Try if isCircleOccupied and if it is turn left or right
	// }
	//
	// }
	//
	// }

	public static boolean modGood(float number, float spacing, float fraction) {
		return (number % spacing) < spacing * fraction;
	}

	static void towardEnemyArchon() throws GameActionException {
		MapLocation go = new MapLocation(rc.readBroadcast(201), rc.readBroadcast(202));
		if (rc.readBroadcast(201) != 0 && rc.readBroadcast(202) != 0)
			if (rc.canMove(go))
				rc.move(go);
	}

	static void tryLastMove() throws GameActionException {
		MapLocation go = new MapLocation(rc.readBroadcast(rc.getID()), rc.readBroadcast(rc.getID() * 10));
		Direction last = rc.getLocation().directionTo(go);
		if (rc.canMove(go))
			rc.move(goingDir);
	}

	static void moveAroundTrees() throws GameActionException {

		nearbyTrees = rc.senseNearbyTrees();
		TreeInfo[] trees = rc.senseNearbyTrees(4);
		RobotInfo[] enemys = rc.senseNearbyRobots();
		for (RobotInfo r : enemys) {
			if (r.getTeam() != rc.getTeam() && r.getType() == RobotType.ARCHON && r.getHealth() < 10 && numArchons > 1)
				goingDir = rc.getLocation().directionTo(rc.getInitialArchonLocations(rc.getTeam().opponent())[1]);
		}

		MapLocation go = new MapLocation(rc.readBroadcast(10), rc.readBroadcast(11));
		MapLocation m = rc.getLocation().add(rc.getLocation().directionTo(go), (float) 1.6);
		if (rc.readBroadcast(10) != 0 && rc.readBroadcast(11) != 0) {
			for (TreeInfo t : trees) {
				if (rc.isCircleOccupiedExceptByThisRobot(m, 1)) {
					if (rc.isCircleOccupiedExceptByThisRobot(
							rc.getLocation().add(rc.getLocation().directionTo(go).rotateLeftDegrees(30), (float) 1.6),
							1)) {
						if (rc.isCircleOccupiedExceptByThisRobot(rc.getLocation()
								.add(rc.getLocation().directionTo(go).rotateRightDegrees(30), (float) 1.6), 1)) {
							if (rc.isCircleOccupiedExceptByThisRobot(rc.getLocation()
									.add(rc.getLocation().directionTo(go).rotateLeftDegrees(60), (float) 1.6), 1)) {
								if (rc.isCircleOccupiedExceptByThisRobot(rc.getLocation().add(
										rc.getLocation().directionTo(go).rotateRightDegrees(60), (float) 1.6), 1)) {
									if (rc.isCircleOccupiedExceptByThisRobot(rc.getLocation().add(
											rc.getLocation().directionTo(go).rotateLeftDegrees(90), (float) 1.6), 1)) {
										if (rc.isCircleOccupiedExceptByThisRobot(rc.getLocation().add(
												rc.getLocation().directionTo(go).rotateRightDegrees(90), (float) 1.6),
												1)) {
											if (rc.isCircleOccupiedExceptByThisRobot(rc.getLocation().add(
													rc.getLocation().directionTo(go).rotateRightDegrees(180),
													(float) 1.6), 1)) {

											} else
												rc.move((rc.getLocation().add(
														rc.getLocation().directionTo(go).rotateRightDegrees(180),
														(float) 1.6)));

										} else
											rc.move((rc.getLocation().add(
													rc.getLocation().directionTo(go).rotateRightDegrees(90),
													(float) 1.6)));

									} else
										rc.move(rc.getLocation().add(
												rc.getLocation().directionTo(go).rotateLeftDegrees(90), (float) 1.6));

								} else
									rc.move(rc.getLocation()
											.add(rc.getLocation().directionTo(go).rotateRightDegrees(60), (float) 1.6));

							} else
								rc.move(rc.getLocation().add(rc.getLocation().directionTo(go).rotateLeftDegrees(60),
										(float) 1.6));

						} else
							rc.move(rc.getLocation().add(rc.getLocation().directionTo(go).rotateRightDegrees(30),
									(float) 1.6));

					} else
						rc.move(rc.getLocation().add(rc.getLocation().directionTo(go).rotateLeftDegrees(30),
								(float) 1.6));

				} else

					rc.move(go);

			}

		}
		if (rc.canMove(go)) {
			rc.move(rc.getLocation().directionTo(go));
		} else if (rc.canMove(goingDir))
			rc.move(goingDir);
	}

	static void towardEnemy() throws GameActionException {
		nearbyTrees = rc.senseNearbyTrees();
		MapLocation go = new MapLocation(rc.readBroadcast(10), rc.readBroadcast(11));
		if (rc.readBroadcast(10) != 0 && rc.readBroadcast(11) != 0) {
			if (rc.canMove(go)) {
				// if
				// (!rc.isCircleOccupied(go.add(rc.getLocation().directionTo(go),
				// 2), 1)) {
				rc.move(rc.getLocation().directionTo(go));

				/*
				 * }else if
				 * (!rc.isCircleOccupiedExceptByThisRobot(go.add(rc.getLocation(
				 * ).directionTo(go).rotateLeftDegrees(90), 1), 1)){
				 * rc.move(rc.getLocation().directionTo(go).rotateRightDegrees(
				 * 90)); }else if
				 * (!rc.isCircleOccupiedExceptByThisRobot(go.add(rc.getLocation(
				 * ).directionTo(go).rotateRightDegrees(90), 1), 1)){
				 * rc.move(rc.getLocation().directionTo(go).rotateLeftDegrees(90
				 * )); }else{
				 * rc.move(rc.getLocation().directionTo(go).rotateLeftDegrees(
				 * 180)); }
				 */
				// Try if isCircleOccupied and if it is turn left or right
			}

		}

	}

	// static MapLocation convertLocationInts(int[] arr) {
	// float xcoord = (float) (arr[0] + arr[1] / Math.pow(10, 6));
	// float ycoord = (float) (arr[2] + arr[3] / Math.pow(10, 6));
	// return (new MapLocation(xcoord, ycoord));
	// }
	//
	// static MapLocation readLocation(int firstChannel) throws
	// GameActionException {
	// int[] array = new int[4];
	// array[0] = rc.readBroadcast(firstChannel);
	// array[1] = rc.readBroadcast(firstChannel + 1);
	// array[2] = rc.readBroadcast(firstChannel + 2);
	// array[3] = rc.readBroadcast(firstChannel + 3);
	// return convertLocationInts(array);
	// }
	//
	// static int[] convertMapLocation(MapLocation map) {
	// float xcoord = map.x;
	// float ycoord = map.y;
	// int[] returnarray = new int[4];
	// returnarray[0] = Math.round(xcoord - (xcoord % 1));
	// returnarray[1] = Math.toIntExact(Math.round((xcoord % 1) * Math.pow(10,
	// 6)));
	// returnarray[2] = Math.round(ycoord - (ycoord % 1));
	// returnarray[3] = Math.toIntExact(Math.round((ycoord % 1) * Math.pow(10,
	// 6)));
	// return (returnarray);
	// }
	//
	// static void writeLocation(MapLocation map, int firstChannel) throws
	// GameActionException {
	// int[] arr = convertMapLocation(map);
	// rc.broadcast(firstChannel, arr[0]);
	// rc.broadcast(firstChannel + 1, arr[1]);
	// rc.broadcast(firstChannel + 2, arr[2]);
	// rc.broadcast(firstChannel + 3, arr[3]);
	// }
	//
	// // this is the slugs "tail" imagine leaving a trail of sticky goo on the
	// map
	// // that you don't want to step in that slowly dissapates over time
	// static ArrayList<MapLocation> oldLocations = new
	// ArrayList<MapLocation>();
	//
	// private static boolean pathFinder(MapLocation target, float strideRadius)
	// throws GameActionException {
	//
	// // when trying to move, let's look forward, then incrementing left and
	// // right.
	// float[] toTry = { 0, (float) Math.PI / 4, (float) -Math.PI / 4, (float)
	// Math.PI / 2, (float) -Math.PI / 2,
	// 3 * (float) Math.PI / 4, -3 * (float) Math.PI / 4, -(float) Math.PI };
	//
	// MapLocation ourLoc = rc.getLocation();
	// Direction toMove = ourLoc.directionTo(target);
	//
	// // let's try to find a place to move!
	// for (int i = 0; i < toTry.length; i++) {
	// Direction dirToTry = toMove.rotateRightDegrees(toTry[i]);
	// if (rc.canMove(dirToTry, strideRadius)
	// && rc.isCircleOccupiedExceptByThisRobot(rc.getLocation().add(dirToTry,
	// strideRadius), (float) .5)) {
	// // if that location is free, let's see if we've already moved
	// // there before (aka, it's in our tail)
	//
	// rc.move(dirToTry, strideRadius);
	//
	// return (true);
	//
	// }
	// }
	//
	// // looks like we can't move anywhere
	// return (false);
	//
	// }

}
