package snowesamosc.mtgturing.cards;

import snowesamosc.mtgturing.CardKind;
import snowesamosc.mtgturing.Player;

import java.util.List;

public class WheelOfSunAndMoon extends RealCard {
    private Player enchantedPlayer = null;

    public void setEnchantedPlayer(Player enchantedPlayer) {
        this.enchantedPlayer = enchantedPlayer;
    }

    @Override
    public List<CardColor> getOriginalColors() {
        return List.of(CardColor.Green, CardColor.White);
    }

    @Override
    public CardKind getType() {
        return CardKind.WheelOfSunAndMoon;
    }
}
