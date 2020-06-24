package vip.bot.messages;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.teamcomm.mina.messages.TCMessageData;
import cz.cuni.amis.utils.token.IToken;
import cz.cuni.amis.utils.token.Tokens;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class TCAllyLocation extends TCMessageData {

    private static final long serialVersionUID = 761547131846249L;

    public static final IToken MESSAGE_TYPE = Tokens.get(TCAllyLocation.class.getSimpleName());

    @Getter
    UnrealId playerId;

    @Getter
    Location location;

}
