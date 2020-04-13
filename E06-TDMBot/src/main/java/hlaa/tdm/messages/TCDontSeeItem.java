package hlaa.tdm.messages;

import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.teamcomm.mina.messages.TCMessageData;
import cz.cuni.amis.utils.token.IToken;
import cz.cuni.amis.utils.token.Tokens;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class TCDontSeeItem extends TCMessageData {

    private static final long serialVersionUID = 1761431864324746L;

    public static final IToken MESSAGE_TYPE = Tokens.get(TCDontSeeItem.class.getSimpleName());

    @Getter
    public UnrealId itemId;

}
