package snowesamosc.mtgturing.cards.cardtexts;

import snowesamosc.mtgturing.ContinuousEffect;
import snowesamosc.mtgturing.ContinuousEffectAdapter;
import snowesamosc.mtgturing.Game;
import snowesamosc.mtgturing.cards.RealCard;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class CloakOfInvisibility extends CardText {
    @Override
    public Optional<ContinuousEffect> getStaticEffect() {
        return Optional.of(new ContinuousEffectAdapter() {
            @Override
            public Set<RealCard> getPhasingCard() {
                var optional = Game.getInstance().attachedCard(CloakOfInvisibility.this.getOwner());
                return optional.map(Collections::singleton).orElse(Collections.emptySet());
            }
        });
    }
}
