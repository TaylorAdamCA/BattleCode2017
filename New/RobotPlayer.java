
package New;

import java.util.Random;
import java.util.ArrayList;

import battlecode.common.*;

public strictfp class RobotPlayer {
	static int numArchons;
	static MapLocation eArchonMap;
	static TreeInfo[] nearbyTrees;
	static RobotController rc;
	static Direction[] dirList = new Direction[8];
	static Direction goingDir = Direction.getNorth();
	static Direction archonGo = Direction.getNorth();
	static boolean archonGoTest = false;
	static Random rand;
	static int gardeners = 0;
	static int soldiersSpawn = 0;
	static int intialSoldiers = 0;
	static int scouts = 0;
	static boolean enoughGard = false;
	static int fourGard = 0;
	static int archon = 1;
	static int archonMoved = 0;
	static Direction[] plantDirList = new Direction[6];
	static Direction[] soldierSpawn = new Direction[24];
	static ArrayList<MapLocation> ourBots;
	static ArrayList<ArrayList<Integer>> outer = new ArrayList<ArrayList<Integer>>();
	static boolean spawnedOneGardener = false;
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
	static final int readyToSpawn = 12;
	static final int gardYes = 13;
	static final int SCOUT_SOLDIER_TREE = 14;
	static int buildGardener = 0;
	static int isStuck = 0;
	static int numLumStuck = 0;
	static final int SMALLMAP = 1000;
	static float distanceToEnemyArchon = 0;
	static float closestToEnemyArchon;

	public static void run(RobotController rc) throws GameActionException {
		RobotPlayer.rc = rc;
		initDirList();
		initPlantDirList();
		initSoldierSpawn();
		eArchonMap = rc.getInitialArchonLocations(rc.getTeam().opponent())[0];
		if (rc.getRoundNum() % 1000 == 0) {
			archonMoved = 0;
			archonGoTest = false;
		}
		if (rc.getRoundNum() % 10 == 0) {
			rc.broadcastFloat(10, 0);
			rc.broadcastFloat(11, 0);
		}
		if (rc.getRoundNum() % 10 == 0) {
			rc.broadcastFloat(201, 0);
			rc.broadcastFloat(202, 0);
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

	public static void initSoldierSpawn() {
		for (int i = 0; i < 24; i++) {
			float radians = (float) (-Math.PI + 2 * Math.PI * ((float) i / 24));
			soldierSpawn[i] = new Direction(radians);
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

				enemySpot();

				if (buildGardener < 1 || (rc.getTeamBullets() >= 200 && rc.getRobotCount() < 40)
						|| (rc.getRobotCount() < 3 && rc.getRoundNum() > 20)) {
					if (tryToBuild(RobotType.GARDENER, RobotType.GARDENER.bulletCost)) {
						buildGardener++;
					}
				}

				RobotInfo[] r = rc.senseNearbyRobots(RobotType.ARCHON.sensorRadius, rc.getTeam().opponent());
				if (r.length > 0)
					rc.broadcastBoolean(SMALLMAP, true);
				numArchons = GameConstants.NUMBER_OF_ARCHONS_MAX;
				closestToEnemyArchon = rc.getLocation().distanceTo(eArchonMap);
				enemySpot();
				if (closestToEnemyArchon < 26 && rc.getRoundNum() < 10)
					rc.broadcastBoolean(SMALLMAP, true);

				RobotInfo[] bots = rc.senseNearbyRobots(6);
				for (RobotInfo b : bots) {
					if (b.getType() == RobotType.GARDENER && b.getTeam() == rc.getTeam()) {
						if (rc.canMove(rc.getLocation().directionTo(b.getLocation()).opposite())) {
							rc.move(rc.getLocation().directionTo(b.getLocation()).opposite());
						}
					}
					if (b.getTeam() != rc.getTeam()) {
						if (rc.canMove(rc.getLocation().directionTo(b.getLocation()).opposite())) {
							rc.move(rc.getLocation().directionTo(b.getLocation()).opposite());
						}
					}
				}

				Direction ranMove = randomDir();
				if (rc.canMove(ranMove)) {
					rc.move(ranMove);
				}

				goingDir = rc.getLocation().directionTo(rc.getInitialArchonLocations(rc.getTeam().opponent())[0]);
				int shouldIDodoge = 0;

				archon = 0;
				fourGard = 0;

				for (RobotInfo b : bots) {
					if (b.getTeam() != rc.getTeam()) {
						shouldIDodoge++;
					}
				}

				if (rc.getRobotCount() > 100 && rc.getTeamBullets() > 100) {
					rc.donate(10 * rc.getVictoryPointCost());
				}
				if (rc.getTeamBullets() >= 1000 && rc.getRoundNum() > 750)
					rc.donate(50 * rc.getVictoryPointCost());

				MapLocation myLocation = rc.getLocation();
				rc.broadcast(0, (int) myLocation.x);
				rc.broadcast(1, (int) myLocation.y);
				// if (rc.canMove(goingDir)) {
				// rc.move(goingDir);
				if (shouldIDodoge > 3) {
					dodge();
				}
			} catch (Exception e) {
				e.printStackTrace();

			}
		}

	}

	public static void runGardener() {
		while (true) {
			try {
				if (rc.getRobotCount() < 60) {
					if (rc.getHealth() < 5)
						buildGardener--;
					dodge();
					if (rc.readBroadcastBoolean(SMALLMAP)) {
						if (rc.getRoundNum() < 15) {
							tryToBuild(RobotType.SOLDIER, RobotType.SOLDIER.bulletCost);
						}
					}
					enemySpot();
					int robotID = rc.getID();
					boolean otherGardener = false;
					if (!gardenerFoundSpotID.contains(robotID)) {
						RobotInfo[] bots = rc.senseNearbyRobots(6, rc.getTeam());
						for (RobotInfo b : bots) {
							if (b.getType() == RobotType.GARDENER) {
								otherGardener = true;
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
					if ((avSpots() == 6 && !otherGardener) || gardenerFoundSpotID.contains(rc.getID())) {
						if (!gardenerFoundSpotID.contains(robotID)) {
							gardenerFoundSpotID.add(robotID);
						}

						int scoutSoldierTree = rc.readBroadcast(SCOUT_SOLDIER_TREE);
						if (scoutSoldierTree == 0) {
							if (tryToBuild(RobotType.SCOUT, RobotType.SCOUT.bulletCost)) {
								rc.broadcast(SCOUT_SOLDIER_TREE, 1);
							}
						} else if (scoutSoldierTree == 1) {
							int lum = rand.nextInt(9);
							if (lum == 8) {
								if (tryToBuild(RobotType.LUMBERJACK, RobotType.LUMBERJACK.bulletCost)) {
									rc.broadcast(SCOUT_SOLDIER_TREE, 2);
								}
							}
							if (tryToBuild(RobotType.SOLDIER, RobotType.SOLDIER.bulletCost)) {
								rc.broadcast(SCOUT_SOLDIER_TREE, 2);
							}
						} else if (scoutSoldierTree == 2 || scoutSoldierTree == 3) {
							if (tryToBuild(RobotType.SOLDIER, RobotType.SOLDIER.bulletCost)) {

							} else if (tryToPlant()) {
								if (scoutSoldierTree == 3) {
									rc.broadcast(SCOUT_SOLDIER_TREE, 1);
								} else {
									rc.broadcast(SCOUT_SOLDIER_TREE, 3);
								}
							}
						}
						tryToWater();
						dodge();
					} else {
						if (isStuck < 5) {
							isStuck++;
						} else {
							if (tryToBuild(RobotType.LUMBERJACK, RobotType.LUMBERJACK.bulletCost)) {
								isStuck = 0;
								numLumStuck++;
							}
							if (numLumStuck > 4) {
								gardenerFoundSpotID.add(robotID);
							}
						}
					}
				} else {
					tryToWater();
					tryToPlant();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void runSoldier() {
		while (true) {
			try {
				dodge();

				canShoot(RobotType.SOLDIER);
				if (!rc.hasAttacked()) {
					towardEnemy();
					MapLocation go = new MapLocation(rc.readBroadcast(201), rc.readBroadcast(202));

					if (rc.canMove(go)) {
						rc.move(go);

					} else {
						if (rc.canMove(eArchonMap)) {
							rc.move(eArchonMap);
						}
					}

					if (rc.canMove(goingDir)) {
						rc.move(goingDir);
					} else {
						goingDir = randomDir();
					}

				}
				enemySpot();

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

				canShoot(RobotType.TANK);

				// if (rc.readBroadcast(201) != 0 && rc.readBroadcast(202) != 0)
				// towardEnemyArchon();

				if (!rc.hasAttacked()) {
					towardEnemy();
					MapLocation go = new MapLocation(rc.readBroadcast(201), rc.readBroadcast(202));

					if (rc.canMove(go)) {
						rc.move(go);

					} else {
						if (rc.canMove(eArchonMap)) {
							rc.move(eArchonMap);
						}
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

	public static void runScout() {
		while (true) {
			try {
				dodge();
				// enemySpot();
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
				canShoot(RobotType.SCOUT);
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
					if (t.containedRobot != null && rc.canChop(t.getID())) {
						rc.chop(t.getID());
						break;
					} else if (t.getTeam() != rc.getTeam() && rc.canChop(t.getID())) {
						rc.chop(t.getID());
						break;
					}
				}
				if (!rc.hasAttacked()) {
					towardEnemy();
					MapLocation go = new MapLocation(rc.readBroadcast(201), rc.readBroadcast(202));

					if (rc.canMove(go)) {
						rc.move(go);

					} else {
						if (rc.canMove(eArchonMap)) {
							rc.move(eArchonMap);
						}
					}

					if (rc.canMove(goingDir)) {
						rc.move(goingDir);
					} else {
						goingDir = randomDir();
					}

				}
				enemySpot();

				// }
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

	public static void canShoot(RobotType shooter) throws GameActionException {
		int botsInLOS = 0;
		RobotInfo[] bots = rc.senseNearbyRobots(RobotType.SOLDIER.sensorRadius, rc.getTeam().opponent());
		RobotInfo[] fbots = rc.senseNearbyRobots(2);
		boolean archon = false;
		int friendlys = 0;
		botsInLOS = 0;
		for (RobotInfo r : bots) {
			if (r.getTeam() != rc.getTeam()
					&& rc.getLocation().distanceTo(r.getLocation()) < RobotType.SOLDIER.bulletSightRadius) {
				botsInLOS++;
			}
			if (r.getTeam() != rc.getTeam() && r.getType() == RobotType.ARCHON)
				archon = true;
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

		} else if (friendlys >= 5) {
			for (RobotInfo b : bots) {

				if (b.getTeam() != rc.getTeam() && shooter.canAttack() && b.getType() != RobotType.ARCHON
						&& rc.getTeamBullets() >= GameConstants.SINGLE_SHOT_COST) {
					Direction dir = rc.getLocation().directionTo(b.getLocation());
					if (dontShootAllies(dir))
						rc.fireSingleShot(dir);

					if (b.getType() == RobotType.LUMBERJACK && rc.canMove(dir.opposite())) {
						rc.move(dir.opposite());
						break;
					}
				} else if (b.getTeam() != rc.getTeam() && shooter.canAttack()
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
				if (archon) {
					if (b.getTeam() != rc.getTeam() && shooter.canAttack() && b.getType() != RobotType.ARCHON
							&& rc.getTeamBullets() >= GameConstants.SINGLE_SHOT_COST
							&& rc.getLocation().distanceTo(b.getLocation()) > 6 && botsInLOS < 2) {
						Direction dir = rc.getLocation().directionTo(b.getLocation());
						if (dontShootAllies(dir))
							rc.fireSingleShot(dir);

						if (b.getType() == RobotType.LUMBERJACK && rc.canMove(dir.opposite())) {
							rc.move(dir.opposite());
							break;
						}
					} else if (b.getTeam() != rc.getTeam() && shooter.canAttack() && b.getType() != RobotType.ARCHON
							&& rc.getTeamBullets() >= GameConstants.TRIAD_SHOT_COST
							&& rc.getLocation().distanceTo(b.getLocation()) > 4) {
						Direction dir = rc.getLocation().directionTo(b.getLocation());
						if (dontShootAllies(dir))
							rc.fireTriadShot(dir);

						if (b.getType() == RobotType.LUMBERJACK && rc.canMove(dir.opposite())) {
							rc.move(dir.opposite());
							break;
						}
					} else if (b.getTeam() != rc.getTeam() && shooter.canAttack() && b.getType() != RobotType.ARCHON
							&& rc.getTeamBullets() >= GameConstants.PENTAD_SHOT_COST && friendlys < 5) {
						Direction dir = rc.getLocation().directionTo(b.getLocation());
						if (dontShootAllies(dir))
							rc.firePentadShot(dir);
						if (b.getType() == RobotType.LUMBERJACK && rc.canMove(dir.opposite())) {
							rc.move(dir.opposite());
							break;

						}

					}
				} else if(archon && bots.length < 1) {

					if (b.getTeam() != rc.getTeam() && shooter.canAttack()
							&& rc.getTeamBullets() >= GameConstants.SINGLE_SHOT_COST
							&& rc.getLocation().distanceTo(b.getLocation()) > 3 && botsInLOS < 2) {
						Direction dir = rc.getLocation().directionTo(b.getLocation());
						if (dontShootAllies(dir))
							rc.fireSingleShot(dir);

						if (b.getType() == RobotType.LUMBERJACK && rc.canMove(dir.opposite())) {
							rc.move(dir.opposite());
							break;
						}
					} else if (b.getTeam() != rc.getTeam() && shooter.canAttack()
							&& rc.getTeamBullets() >= GameConstants.TRIAD_SHOT_COST
							&& rc.getLocation().distanceTo(b.getLocation()) > 1) {
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

				}else {

					if (b.getTeam() != rc.getTeam() && shooter.canAttack()
							&& rc.getTeamBullets() >= GameConstants.SINGLE_SHOT_COST
							&& rc.getLocation().distanceTo(b.getLocation()) > 3 && botsInLOS < 2) {
						Direction dir = rc.getLocation().directionTo(b.getLocation());
						if (dontShootAllies(dir))
							rc.fireSingleShot(dir);

						if (b.getType() == RobotType.LUMBERJACK && rc.canMove(dir.opposite())) {
							rc.move(dir.opposite());
							break;
						}
					} else if (b.getTeam() != rc.getTeam() && shooter.canAttack()
							&& rc.getTeamBullets() >= GameConstants.TRIAD_SHOT_COST
							&& rc.getLocation().distanceTo(b.getLocation()) > 1) {
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
	}



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
				for (int i = 0; i < 8; i++) {
					if (rc.canBuildRobot(type, dirList[i]) && rc.canMove(rc.getLocation().add(dirList[i], 3))) {
						// if((rc.isLocationOccupiedByTree(rc.getLocation().add(dirList[i],
						// RobotType.GARDENER.bodyRadius * 3)) == false) ){
						gardeners++;

						rc.buildRobot(type, dirList[i]);
						return true;
						// }
					}
				}
			} else {
				for (int i = 0; i < 6; i++) {
					rc.setIndicatorDot(rc.getLocation().add(dirList[i]), 1, 1, 1);
					if (rc.canBuildRobot(type, plantDirList[i])) {
						rc.buildRobot(type, plantDirList[i]);
						return true;
					}
				}
			}
			for (int i = 0; i < 24; i++) {
				if (rc.canBuildRobot(type, soldierSpawn[i])) {
					rc.buildRobot(type, soldierSpawn[i]);
					return true;
				}

			}

		}
		return false;
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

	public static int avSpots() throws GameActionException {
		int availableSpots = 0;
		for (int i = 0; i < 6; i++) {
			if (rc.canPlantTree(plantDirList[i])) {
				availableSpots++;
			}
		}
		return availableSpots;
	}

	public static boolean tryToPlant() throws GameActionException {
		// this could be better by putting trees in an array

		if (rc.getTeamBullets() > GameConstants.BULLET_TREE_COST && avSpots() > 1) {
			for (int i = 0; i < 6; i++) {
				if (rc.canPlantTree(plantDirList[i])) {
					rc.plantTree(plantDirList[i]);
					return true;
				}
			}
		}
		return false;
	}

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
				}
			}

		}
		return null;

	}

	static void towardEnemyArchon() throws GameActionException {

		MapLocation go = new MapLocation(rc.readBroadcast(201), rc.readBroadcast(202));
		if (rc.readBroadcast(201) != 0 && rc.readBroadcast(202) != 0) {
			if (rc.canMove(go)) {
				rc.move(go);
			}
		} else {
			if (rc.canMove(eArchonMap)) {
				rc.move(eArchonMap);
			}
		}
	}

	static void towardEnemy() throws GameActionException {
		nearbyTrees = rc.senseNearbyTrees();

		MapLocation go = new MapLocation(rc.readBroadcast(10), rc.readBroadcast(11));
		MapLocation m = rc.getLocation().add(rc.getLocation().directionTo(go), (float) 1.6);
		if (rc.readBroadcast(10) != 0 && rc.readBroadcast(11) != 0) {
			if (!rc.isCircleOccupiedExceptByThisRobot(m, 1))
				rc.move(go);
			else {
				for (TreeInfo t : nearbyTrees) {
					if (rc.isCircleOccupiedExceptByThisRobot(m, 1)) {
						if (rc.isCircleOccupiedExceptByThisRobot(rc.getLocation()
								.add(rc.getLocation().directionTo(go).rotateLeftDegrees(30), (float) 1.6), 1)) {
							if (rc.isCircleOccupiedExceptByThisRobot(rc.getLocation()
									.add(rc.getLocation().directionTo(go).rotateRightDegrees(30), (float) 1.6), 1)) {
								if (rc.isCircleOccupiedExceptByThisRobot(rc.getLocation()
										.add(rc.getLocation().directionTo(go).rotateLeftDegrees(60), (float) 1.6), 1)) {
									if (rc.isCircleOccupiedExceptByThisRobot(rc.getLocation().add(
											rc.getLocation().directionTo(go).rotateRightDegrees(60), (float) 1.6), 1)) {
										if (rc.isCircleOccupiedExceptByThisRobot(rc.getLocation().add(
												rc.getLocation().directionTo(go).rotateLeftDegrees(90), (float) 1.6),
												1)) {
											if (rc.isCircleOccupiedExceptByThisRobot(rc.getLocation().add(
													rc.getLocation().directionTo(go).rotateRightDegrees(90),
													(float) 1.6), 1)) {
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
													rc.getLocation().directionTo(go).rotateLeftDegrees(90),
													(float) 1.6));

									} else
										rc.move(rc.getLocation().add(
												rc.getLocation().directionTo(go).rotateRightDegrees(60), (float) 1.6));

								} else
									rc.move(rc.getLocation().add(rc.getLocation().directionTo(go).rotateLeftDegrees(60),
											(float) 1.6));

							} else
								rc.move(rc.getLocation().add(rc.getLocation().directionTo(go).rotateRightDegrees(30),
										(float) 1.6));

						} else
							rc.move(rc.getLocation().add(rc.getLocation().directionTo(go).rotateLeftDegrees(30),
									(float) 1.6));

					}

				}

			}
		}

		MapLocation eA = new MapLocation(rc.readBroadcast(201), rc.readBroadcast(202));
		if (rc.readBroadcast(201) != 0 && rc.readBroadcast(202) != 0) {
			if (rc.canMove(eA)) {
				rc.move(eA);
			}
		} else {
			if (rc.canMove(eArchonMap)) {
				rc.move(eArchonMap);
			} else
				rc.move(randomDir());
		}

	}

	public static boolean modGood(float number, float spacing, float fraction) {
		return (number % spacing) < spacing * fraction;
	}

}
