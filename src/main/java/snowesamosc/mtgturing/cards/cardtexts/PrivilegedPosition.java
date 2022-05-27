package snowesamosc.mtgturing.cards.cardtexts;

import snowesamosc.mtgturing.ContinuousEffect;
import snowesamosc.mtgturing.ContinuousEffectAdapter;
import snowesamosc.mtgturing.Player;
import snowesamosc.mtgturing.cards.RealCard;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PrivilegedPosition extends CardText {
    @Override
    public Optional<ContinuousEffect> getStaticEffect() {
        return Optional.of(new ContinuousEffectAdapter() {
            @Override
            public Set<RealCard> getHexproofCard() {
                return PrivilegedPosition.this.getOwner().getController()
                        .map(Player::field)
                        .orElse(List.of())
                        .stream()
                        .filter(RealCard::isPhaseIn)
                        .filter(card -> card != PrivilegedPosition.this.getOwner())
                        .collect(Collectors.toSet());
            }
        });
    }
}
