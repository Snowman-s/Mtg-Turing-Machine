package snowesamosc.mtgturing;

import snowesamosc.mtgturing.cards.CardColor;
import snowesamosc.mtgturing.cards.CreatureType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ControllerLoader {
    private static final List<Table2Element> defaultTable2 = List.of(
            new Table2Element(CardKind.RotlungReanimator, CreatureType.Aetherborn, CardColor.White, CreatureType.Sliver, true),
            new Table2Element(CardKind.RotlungReanimator, CreatureType.Basilisk, CardColor.Green, CreatureType.Elf, true),
            new Table2Element(CardKind.RotlungReanimator, CreatureType.Cephalid, CardColor.White, CreatureType.Sliver, true),
            new Table2Element(CardKind.RotlungReanimator, CreatureType.Demon, CardColor.Green, CreatureType.Aetherborn, true),
            new Table2Element(CardKind.RotlungReanimator, CreatureType.Elf, CardColor.White, CreatureType.Demon, true),
            new Table2Element(CardKind.RotlungReanimator, CreatureType.Faerie, CardColor.Green, CreatureType.Harpy, true),
            new Table2Element(CardKind.RotlungReanimator, CreatureType.Giant, CardColor.Green, CreatureType.Juggernaut, true),
            new Table2Element(CardKind.RotlungReanimator, CreatureType.Harpy, CardColor.White, CreatureType.Faerie, true),
            new Table2Element(CardKind.RotlungReanimator, CreatureType.Illusion, CardColor.Green, CreatureType.Faerie, true),
            new Table2Element(CardKind.RotlungReanimator, CreatureType.Juggernaut, CardColor.White, CreatureType.Illusion, true),
            new Table2Element(CardKind.XathridNecromancer, CreatureType.Kavu, CardColor.White, CreatureType.Leviathan, true),
            new Table2Element(CardKind.XathridNecromancer, CreatureType.Leviathan, CardColor.White, CreatureType.Illusion, true),
            new Table2Element(CardKind.XathridNecromancer, CreatureType.Myr, CardColor.White, CreatureType.Basilisk, true),
            new Table2Element(CardKind.RotlungReanimator, CreatureType.Noggle, CardColor.Green, CreatureType.Orc, true),
            new Table2Element(CardKind.RotlungReanimator, CreatureType.Orc, CardColor.White, CreatureType.Pegasus, true),
            new Table2Element(CardKind.XathridNecromancer, CreatureType.Pegasus, CardColor.Green, CreatureType.Rhino, true),
            new Table2Element(CardKind.RotlungReanimator, CreatureType.Rhino, CardColor.Blue, CreatureType.Assassin, true),
            new Table2Element(CardKind.RotlungReanimator, CreatureType.Sliver, CardColor.Green, CreatureType.Cephalid, true),

            new Table2Element(CardKind.RotlungReanimator, CreatureType.Aetherborn, CardColor.Green, CreatureType.Cephalid, false),
            new Table2Element(CardKind.RotlungReanimator, CreatureType.Basilisk, CardColor.Green, CreatureType.Cephalid, false),
            new Table2Element(CardKind.RotlungReanimator, CreatureType.Cephalid, CardColor.White, CreatureType.Basilisk, false),
            new Table2Element(CardKind.RotlungReanimator, CreatureType.Demon, CardColor.Green, CreatureType.Elf, false),
            new Table2Element(CardKind.RotlungReanimator, CreatureType.Elf, CardColor.White, CreatureType.Aetherborn, false),
            new Table2Element(CardKind.XathridNecromancer, CreatureType.Faerie, CardColor.Green, CreatureType.Kavu, false),
            new Table2Element(CardKind.RotlungReanimator, CreatureType.Giant, CardColor.Green, CreatureType.Harpy, false),
            new Table2Element(CardKind.RotlungReanimator, CreatureType.Harpy, CardColor.White, CreatureType.Giant, false),
            new Table2Element(CardKind.RotlungReanimator, CreatureType.Illusion, CardColor.Green, CreatureType.Juggernaut, false),
            new Table2Element(CardKind.RotlungReanimator, CreatureType.Juggernaut, CardColor.White, CreatureType.Giant, false),
            new Table2Element(CardKind.XathridNecromancer, CreatureType.Kavu, CardColor.Green, CreatureType.Faerie, false),
            new Table2Element(CardKind.RotlungReanimator, CreatureType.Leviathan, CardColor.Green, CreatureType.Juggernaut, false),
            new Table2Element(CardKind.RotlungReanimator, CreatureType.Myr, CardColor.Green, CreatureType.Orc, false),
            new Table2Element(CardKind.RotlungReanimator, CreatureType.Noggle, CardColor.White, CreatureType.Orc, false),
            new Table2Element(CardKind.RotlungReanimator, CreatureType.Orc, CardColor.Green, CreatureType.Noggle, false),
            new Table2Element(CardKind.RotlungReanimator, CreatureType.Pegasus, CardColor.White, CreatureType.Sliver, false),
            new Table2Element(CardKind.XathridNecromancer, CreatureType.Rhino, CardColor.White, CreatureType.Sliver, false),
            new Table2Element(CardKind.XathridNecromancer, CreatureType.Sliver, CardColor.White, CreatureType.Myr, false)
    );

    private ControllerLoader() {

    }

    private static Path getSaveDirectory() throws IOException {
        var dir = Path.of("./data/");
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        return dir.resolve("table2.csv");
    }

    public static List<Table2Element> loadTable2() {
        try {
            var saveFile = getSaveDirectory();

            if (Files.exists(saveFile)) {
                try (var reader = Files.newBufferedReader(saveFile)) {
                    var ret = new ArrayList<Table2Element>();
                    reader.lines().forEach(
                            line -> {
                                var array = line.split(",");
                                if (array.length < 5) return;
                                var kind = Arrays.stream(CardKind.values())
                                        .filter(k -> k.getOriginalName().equals(array[0].trim()))
                                        .findAny();
                                if (kind.isEmpty()) return;
                                var dieType = Arrays.stream(CreatureType.values())
                                        .filter(k -> k.name().equals(array[1].trim()))
                                        .findAny();
                                if (dieType.isEmpty()) return;
                                var createColor = Arrays.stream(CardColor.values())
                                        .filter(k -> k.name().equals(array[2].trim()))
                                        .findAny();
                                if (createColor.isEmpty()) return;
                                var createType = Arrays.stream(CreatureType.values())
                                        .filter(k -> k.name().equals(array[3].trim()))
                                        .findAny();
                                if (createType.isEmpty()) return;

                                var phaseIn = array[4].trim().equals(Boolean.toString(true)) ? Boolean.TRUE :
                                        array[4].trim().equals(Boolean.toString(false)) ? Boolean.FALSE :
                                                null;
                                if (phaseIn == null) return;

                                ret.add(new Table2Element(kind.get(), dieType.get(), createColor.get(), createType.get(), phaseIn));
                            }
                    );
                    return ret;
                }
            } else {
                var ret = defaultTable2;

                try (var writer = Files.newBufferedWriter(saveFile)) {
                    for (var element : ret) {
                        writer.write(element.kind().getOriginalName() + "," +
                                element.dieType().name() + "," +
                                element.createColor().name() + "," +
                                element.createType().name() + "," +
                                element.phaseIn);
                        writer.newLine();
                    }
                }

                return ret;
            }
        } catch (IOException e) {
            return defaultTable2;
        }
    }

    public record Table2Element(CardKind kind, CreatureType dieType, CardColor createColor, CreatureType createType,
                                boolean phaseIn) {
    }
}
