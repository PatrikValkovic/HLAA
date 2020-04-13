package hlaa.tdm.behavior;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.pogamut.ut2004.teamcomm.bot.UT2004BotTCController;
import cz.cuni.amis.utils.Cooldown;
import hlaa.tdm.KnowledgeBase;
import hlaa.tdm.utils.Inventory;
import hlaa.tdm.utils.Navigation;
import hlaa.tdm.utils.WeaponPrefs;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public class CombatBehavior extends BaseBehavior {

    @AllArgsConstructor
    @RequiredArgsConstructor
    public static class WeaponPref {
        @Getter
        @NonNull
        private double priorityMean;
        @Getter
        @NonNull
        private double priorityStd;
        @Getter
        @NonNull
        private ItemType weapon;
        @Getter
        @NonNull
        private boolean primaryMode;
        @Getter
        private double priority = 1.0;
    }

    private static final double ROCKET_MINUS = 40.0;
    private final Cooldown _sniper_cooldown = new Cooldown(2000);

    public CombatBehavior(UT2004BotTCController bot) {
        super(bot);
    }
    public CombatBehavior(UT2004BotTCController bot, double priority) {
        super(bot, priority);
    }
    public CombatBehavior(UT2004BotTCController bot, KnowledgeBase knowledge) {
        super(bot, knowledge);
    }
    public CombatBehavior(UT2004BotTCController bot, double priority, KnowledgeBase knowledge) {
        super(bot, priority, knowledge);
    }

    @Override
    public boolean isFiring() {
        if(_bot.getPlayers().getVisiblePlayers().size() == 0){
            _bot.getShoot().stopShooting();
        }

        return _bot.getPlayers().getVisiblePlayers().size() > 0;
    }

    @Override
    public void execute() {
        _bot.getNavigation().stopNavigation();
        Location myLocation = _bot.getInfo().getLocation();
        Player opponent = _bot.getPlayers().getNearestVisiblePlayer();
        Location opponentLocation = opponent.getLocation();
        double playerDistance = myLocation.getDistance(opponentLocation);
        _bot.getLog().info("See player " + opponent.getName() + " " + playerDistance + " away");

        Set<ItemType> exception = new HashSet<>();
        if(!_sniper_cooldown.tryUse()) {
            //System.out.println("Exception sniper");
            exception.add(UT2004ItemType.SNIPER_RIFLE);
            exception.add(UT2004ItemType.LIGHTNING_GUN);
        }

        WeaponPref pref = Inventory.bestWeaponForDistance(_bot.getWeaponry(), WeaponPrefs.WEAPON_PREFS, playerDistance, exception);
        _bot.getLog().info("Decided for " + pref.getWeapon().getName() + " using " + (pref.isPrimaryMode() ? "primary" : "secondary"));

        Location shootTarget = opponentLocation;
        if(pref.getWeapon().equals(UT2004ItemType.ROCKET_LAUNCHER)){
            shootTarget = shootTarget.sub(new Location(0,0,ROCKET_MINUS));
            if(_bot.getLevelGeometry() != null && !Navigation.canSee(_bot.getLevelGeometry(), myLocation, shootTarget)){
                shootTarget = opponentLocation;
                exception.add(UT2004ItemType.ROCKET_LAUNCHER);
                pref = Inventory.bestWeaponForDistance(_bot.getWeaponry(), WeaponPrefs.WEAPON_PREFS, playerDistance, exception);
                _bot.getLog().info("Change to " + pref.getWeapon().getName() + " because dont see floor");
            }
        }

        _bot.getShoot().shoot(
                _bot.getWeaponry().getWeapon(pref.getWeapon()),
                pref.isPrimaryMode(),
                shootTarget
        );
    }

    @Override
    public void terminate() {
        _bot.getShoot().stopShooting();
    }
}
