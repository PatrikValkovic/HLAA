package vip.bot.messages;

import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.teamcomm.mina.messages.TCMessageData;
import cz.cuni.amis.utils.token.IToken;
import cz.cuni.amis.utils.token.Tokens;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class TCGoingToPick extends TCMessageData {

    private static final long serialVersionUID = 6426843434051001L;

    public static final IToken MESSAGE_TYPE = Tokens.get(TCGoingToPick.class.getSimpleName());

    @Getter
    public UnrealId itemId;

    @Getter
    public double distance;

    @Getter
    public UnrealId botId;

}
