package vip.bot.messages;

import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.teamcomm.mina.messages.TCMessageData;
import cz.cuni.amis.utils.token.IToken;
import cz.cuni.amis.utils.token.Tokens;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class TCSeeItem extends TCMessageData {

    private static final long serialVersionUID = 7354764654768463L;

    public static final IToken MESSAGE_TYPE = Tokens.get(TCSeeItem.class.getSimpleName());

    @Getter
    public UnrealId itemId;

}
