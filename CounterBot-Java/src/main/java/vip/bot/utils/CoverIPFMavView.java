package vip.bot.utils;

import cz.cuni.amis.pathfinding.map.IPFMapView;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.visibility.Visibility;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import vip.bot.KnowledgeBase;
import java.util.ArrayList;
import java.util.Collection;

public class CoverIPFMavView implements IPFMapView<NavPoint> {

    private static final double COST_WEIGHTS = 6000.0;
    private static final double EXP_BASE = 0.998;

    private final Visibility _visibility;
    private KnowledgeBase _knowledge;

    public CoverIPFMavView(KnowledgeBase _knowledge, Visibility visibility) {
        this._knowledge = _knowledge;
        this._visibility = visibility;
    }

    @Override
    public Collection<NavPoint> getExtraNeighbors(NavPoint navPoint, Collection<NavPoint> collection) {
        return new ArrayList<>();
    }

    @Override
    public int getNodeExtraCost(NavPoint navPoint, int i) {
        return (int) Math.round(
                _visibility.getVisibleNavPointsFrom(navPoint)
                           .stream()
                           .mapToDouble(p -> {
                               double distance = Navigation.directDistance(navPoint, p);
                               double prob = _knowledge.getEnemyPositionsKnowledge().getCumulativeProbAtNavpoint(p);
                               return Math.pow(EXP_BASE, distance) * prob * COST_WEIGHTS;
                           })
                           .sum()
        );
    }

    @Override
    public int getArcExtraCost(NavPoint navPoint, NavPoint node1, int i) {
        return 0;
    }

    @Override
    public boolean isNodeOpened(NavPoint navPoint) {
        return true;
    }

    @Override
    public boolean isArcOpened(NavPoint navPoint, NavPoint node1) {
        return true;
    }
}
