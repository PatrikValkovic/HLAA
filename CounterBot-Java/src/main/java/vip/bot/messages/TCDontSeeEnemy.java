package vip.bot.messages;

import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.teamcomm.mina.messages.TCMessageData;
import cz.cuni.amis.utils.token.IToken;
import cz.cuni.amis.utils.token.Tokens;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class TCDontSeeEnemy extends TCMessageData {

    private static final long serialVersionUID = 74131964316571331L;

    public static final IToken MESSAGE_TYPE = Tokens.get(TCDontSeeEnemy.class.getSimpleName());

    @Getter
    UnrealId enemyId;

    @Getter
    UnrealId allyId;
}
