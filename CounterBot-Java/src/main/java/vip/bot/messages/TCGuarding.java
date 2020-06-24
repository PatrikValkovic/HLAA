package vip.bot.messages;

import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.teamcomm.mina.messages.TCMessageData;
import cz.cuni.amis.utils.token.IToken;
import cz.cuni.amis.utils.token.Tokens;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class TCGuarding extends TCMessageData {

    private static final long serialVersionUID = 48614563127457513L;

    public static final IToken MESSAGE_TYPE = Tokens.get(TCGoingToPick.class.getSimpleName());

    @Getter
    public UnrealId playerId;

    @Getter
    public boolean guarding;
}
