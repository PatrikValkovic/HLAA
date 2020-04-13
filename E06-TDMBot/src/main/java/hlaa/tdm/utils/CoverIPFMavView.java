package hlaa.tdm.utils;

import cz.cuni.amis.pathfinding.map.IPFMapView;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.visibility.Visibility;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import hlaa.tdm.KnowledgeBase;
import java.util.ArrayList;
import java.util.Collection;

public class CoverIPFMavView implements IPFMapView<NavPoint> {

    private static final double COST_WEIGHTS = 5000.0;
    private static final double LOG_MULTIPLIER = 0.01;

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
        return (int)Math.round(_visibility.getVisibleNavPointsFrom(navPoint.getLocation())
                          .stream()
                          .mapToDouble(p -> {
                                return (10 - Math.log(LOG_MULTIPLIER * p.getLocation().getDistance(navPoint.getLocation()) + 0.01))
                                        *
                                        _knowledge.getProbAtNavpoint(p)
                                        *
                                        COST_WEIGHTS;
                          })
                          .sum());
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
