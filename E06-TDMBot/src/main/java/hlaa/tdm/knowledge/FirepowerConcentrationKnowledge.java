package hlaa.tdm.knowledge;

import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.pogamut.ut2004.teamcomm.bot.UT2004BotTCController;
import cz.cuni.amis.pogamut.ut2004.teamcomm.bot.UT2004TCClient;
import hlaa.tdm.messages.TCDontSeeEnemy;
import hlaa.tdm.messages.TCSeeEnemy;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class FirepowerConcentrationKnowledge {

    private static final double MS_TO_BE_VALID = 600;

    @AllArgsConstructor
    private static class Helper {
        @Getter
        UnrealId enemy;
        @Getter
        Instant when;

        public boolean removeIf(){
            return Duration.between(when, Instant.now()).toMillis() > MS_TO_BE_VALID;
        }
    }

    private final UT2004BotTCController _bot;

    private final Set<UnrealId> _isee = new HashSet<>();
    private final Map<UnrealId, Set<Helper>> _teamSee = new HashMap<>();


    public FirepowerConcentrationKnowledge(UT2004BotTCController bot) {
        _bot = bot;
    }

    public void update(){
        UT2004TCClient cli = _bot.getTCClient();

        // update what I dont see anymore
        _isee.removeAll(_bot.getPlayers().getVisibleEnemies().keySet());
        _isee.forEach(playerId -> cli.sendToTeam(new TCDontSeeEnemy(playerId, _bot.getInfo().getId())));

        // update what I see
        _isee.clear();
        _isee.addAll(_bot.getPlayers().getVisibleEnemies().keySet());
        _isee.forEach(playerId -> _bot.getTCClient().sendToTeam(new TCSeeEnemy(playerId, _bot.getInfo().getId())));

    }


    public void reset() {
        UT2004TCClient cli = _bot.getTCClient();

        _isee.forEach(playerId -> cli.sendToTeam(new TCDontSeeEnemy(playerId, _bot.getInfo().getId())));
        _isee.clear();
        _teamSee.clear();
    }

    public void teamSeePlayer(UnrealId who, UnrealId enemy){
        if(!_teamSee.containsKey(who))
            _teamSee.put(who, new HashSet<>());

        Set<Helper> hset = _teamSee.get(who);
        hset.removeIf(h -> h.removeIf() || h.enemy.equals(enemy));
        hset.add(new Helper(enemy, Instant.now()));
    }

    public void teamDontSeePlayer(UnrealId who, UnrealId enemy){
        if(!_teamSee.containsKey(who))
            return;

        Set<Helper> hset = _teamSee.get(who);
        hset.removeIf(h -> h.removeIf() || h.enemy.equals(enemy));
    }

    public UnrealId getTargetEnemyId(){
        // remove old entries
        _teamSee.forEach((k, s) -> s.removeIf(Helper::removeIf));

        // how many times is seen by others
        Map<UnrealId, Integer> areSeen = new HashMap<>();
        _teamSee.values()
                .stream()
                .forEach(helperset -> {
                    helperset.forEach(helper ->
                            areSeen.put(helper.enemy, areSeen.getOrDefault(helper.enemy, 0) + 1)
                    );
                });

        // keep only enemies I see
        Set<UnrealId> inMap = areSeen.keySet();
        inMap.retainAll(_isee);

        // get the one that is seen by most users
        UnrealId targetId =
                areSeen.entrySet()
                       .stream()
                       .filter(i -> inMap.contains(i.getKey()))
                       .max(Comparator.comparingInt(h -> h.getValue()))
                       .orElseGet(() -> new AbstractMap.SimpleEntry<>(_bot.getPlayers().getNearestVisibleEnemy().getId(), 0))
                       .getKey();

        //System.out.println(String.format(
        //        "Out of {%s} I picked up: %s",
        //        _isee.stream().map(see -> _bot.getPlayers().getPlayer(see).getName()).collect(Collectors.joining(",")),
        //        _bot.getPlayers().getPlayer(targetId).getName()
        //));

        return targetId;
    }

    public Player getTargetEnemy(){
        return _bot.getPlayers().getPlayer(this.getTargetEnemyId());
    }
}
