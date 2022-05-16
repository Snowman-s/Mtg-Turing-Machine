package snowesamosc.mtgturing;

import snowesamosc.mtgturing.cards.RealCard;

import java.util.List;

public record Player(List<RealCard> deck,
                     List<RealCard> hands,
                     List<RealCard> field) {
}
