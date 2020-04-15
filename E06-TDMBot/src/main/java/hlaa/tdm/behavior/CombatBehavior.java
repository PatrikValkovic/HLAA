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

public class CombatBehavior extends BaseBehavior {

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
        return _bot.getPlayers().getVisibleEnemies().size() > 0;
    }

    @Override
    public void execute() {
        // query values
        Location myLocation = _bot.getInfo().getLocation();
        Player opponent = _knowledge.getFirepowerConcentrationKnowledge().getTargetEnemy();
        Location opponentLocation = opponent.getLocation();
        double playerDistance = myLocation.getDistance(opponentLocation);
        _bot.getLog().info("See player " + opponent.getName() + " " + playerDistance + " away");

        // do not query lighting gun if is cool down
        Set<ItemType> exception = new HashSet<>();
        if(!_sniper_cooldown.tryUse()) {
            //System.out.println("Exception sniper");
            exception.add(UT2004ItemType.SNIPER_RIFLE);
            exception.add(UT2004ItemType.LIGHTNING_GUN);
        }

        // decide which weapon to use
        WeaponPrefs.WeaponPref pref = Inventory.bestWeaponForDistance(_bot.getWeaponry(), WeaponPrefs.WEAPON_PREFS, playerDistance, exception);
        _bot.getLog().info("Decided for " + pref.getWeapon().getName() + " using " + (pref.isPrimaryMode() ? "primary" : "secondary"));

        // handle safe rocket shooting
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

        // fire
        _bot.getWeaponry().changeWeapon(pref.getWeapon());
        if (_bot.getWeaponry().getCurrentWeapon().getType().equals(pref.getWeapon()))
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
