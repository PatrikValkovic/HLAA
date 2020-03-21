package ut2004.exercises.e03.comm;

import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.teamcomm.mina.messages.TCMessageData;
import cz.cuni.amis.utils.token.IToken;
import cz.cuni.amis.utils.token.Tokens;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class TCPursuit extends TCMessageData {

	private static final long serialVersionUID = 78663241237532L;

	public static final IToken MESSAGE_TYPE = Tokens.get("TCPursuit");

	@Getter
	private UnrealId what;

	@Getter
	private double distance;
	
}
