package mobbinbottesting;

import java.util.ArrayList;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.TreeInfo;

public class test {
	public static void main(String[] args){
		ArrayList a = new ArrayList();
		RobotPlayer.clearArray(a);
	}
}

/*static void towardEnemy() throws GameActionException {
	nearbyTrees = rc.senseNearbyTrees();
	MapLocation go = new MapLocation(rc.readBroadcast(10), rc.readBroadcast(11));
	if (rc.readBroadcast(10) != 0 && rc.readBroadcast(11) != 0) {
		for (TreeInfo t : nearbyTrees) {
			Direction dir = rc.getLocation().directionTo(go);
			if (dir != rc.getLocation().directionTo(t.getLocation()) && rc.canMove(dir)) {
				rc.move(dir);

			} else {
				if (rc.canMove(goingDir)) {
					rc.move(goingDir);
				} else {
					goingDir = randomDir();
				}
			}

		}
	}
}

}*/