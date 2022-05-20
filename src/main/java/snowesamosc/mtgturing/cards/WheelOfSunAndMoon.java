package snowesamosc.mtgturing.cards;

import snowesamosc.mtgturing.CardKind;
import snowesamosc.mtgturing.Player;

public class WheelOfSunAndMoon extends RealCard {
    private Player enchantedPlayer = null;

    public void setEnchantedPlayer(Player enchantedPlayer) {
        this.enchantedPlayer = enchantedPlayer;
    }

    @Override
    public CardKind getType() {
        return CardKind.WheelOfSunAndMoon;
    }
}
