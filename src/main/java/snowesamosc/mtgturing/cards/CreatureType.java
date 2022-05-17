package snowesamosc.mtgturing.cards;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public enum CreatureType {
    AssemblyWorker,
    Incarnation,
    Lhurgoyf,
    Rat,
    Aetherborn,
    Basilisk,
    Demon,
    Cephalid,
    Elf,
    Faerie,
    Giant,
    Illusion,
    Harpy,
    Juggernaut,
    Kavu,
    Leviathan,
    Myr,
    Noggle,
    Pegasus,
    Orc,
    Rhino,
    Sliver,
    Assassin;

    public static List<CreatureType> translateCreatureType(List<String> englishCardTypeStrings) {
        return englishCardTypeStrings.stream()
                .map(s -> Arrays.stream(values()).filter(v -> v.name().equals(s)).findAny())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
