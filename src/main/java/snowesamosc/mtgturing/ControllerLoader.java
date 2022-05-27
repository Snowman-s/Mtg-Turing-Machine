package snowesamosc.mtgturing;

import snowesamosc.mtgturing.cards.CardColor;
import snowesamosc.mtgturing.cards.CardSubType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ControllerLoader {
    private static final List<Table2Element> defaultTable2 = List.of(
            new Table2Element(CardKind.RotlungReanimator, CardSubType.Aetherborn, CardColor.White, CardSubType.Sliver, true),
            new Table2Element(CardKind.RotlungReanimator, CardSubType.Basilisk, CardColor.Green, CardSubType.Elf, true),
            new Table2Element(CardKind.RotlungReanimator, CardSubType.Cephalid, CardColor.White, CardSubType.Sliver, true),
            new Table2Element(CardKind.RotlungReanimator, CardSubType.Demon, CardColor.Green, CardSubType.Aetherborn, true),
            new Table2Element(CardKind.RotlungReanimator, CardSubType.Elf, CardColor.White, CardSubType.Demon, true),
            new Table2Element(CardKind.RotlungReanimator, CardSubType.Faerie, CardColor.Green, CardSubType.Harpy, true),
            new Table2Element(CardKind.RotlungReanimator, CardSubType.Giant, CardColor.Green, CardSubType.Juggernaut, true),
            new Table2Element(CardKind.RotlungReanimator, CardSubType.Harpy, CardColor.White, CardSubType.Faerie, true),
            new Table2Element(CardKind.RotlungReanimator, CardSubType.Illusion, CardColor.Green, CardSubType.Faerie, true),
            new Table2Element(CardKind.RotlungReanimator, CardSubType.Juggernaut, CardColor.White, CardSubType.Illusion, true),
            new Table2Element(CardKind.XathridNecromancer, CardSubType.Kavu, CardColor.White, CardSubType.Leviathan, true),
            new Table2Element(CardKind.XathridNecromancer, CardSubType.Leviathan, CardColor.White, CardSubType.Illusion, true),
            new Table2Element(CardKind.XathridNecromancer, CardSubType.Myr, CardColor.White, CardSubType.Basilisk, true),
            new Table2Element(CardKind.RotlungReanimator, CardSubType.Noggle, CardColor.Green, CardSubType.Orc, true),
            new Table2Element(CardKind.RotlungReanimator, CardSubType.Orc, CardColor.White, CardSubType.Pegasus, true),
            new Table2Element(CardKind.XathridNecromancer, CardSubType.Pegasus, CardColor.Green, CardSubType.Rhino, true),
            new Table2Element(CardKind.RotlungReanimator, CardSubType.Rhino, CardColor.Blue, CardSubType.Assassin, true),
            new Table2Element(CardKind.RotlungReanimator, CardSubType.Sliver, CardColor.Green, CardSubType.Cephalid, true),

            new Table2Element(CardKind.RotlungReanimator, CardSubType.Aetherborn, CardColor.Green, CardSubType.Cephalid, false),
            new Table2Element(CardKind.RotlungReanimator, CardSubType.Basilisk, CardColor.Green, CardSubType.Cephalid, false),
            new Table2Element(CardKind.RotlungReanimator, CardSubType.Cephalid, CardColor.White, CardSubType.Basilisk, false),
            new Table2Element(CardKind.RotlungReanimator, CardSubType.Demon, CardColor.Green, CardSubType.Elf, false),
            new Table2Element(CardKind.RotlungReanimator, CardSubType.Elf, CardColor.White, CardSubType.Aetherborn, false),
            new Table2Element(CardKind.XathridNecromancer, CardSubType.Faerie, CardColor.Green, CardSubType.Kavu, false),
            new Table2Element(CardKind.RotlungReanimator, CardSubType.Giant, CardColor.Green, CardSubType.Harpy, false),
            new Table2Element(CardKind.RotlungReanimator, CardSubType.Harpy, CardColor.White, CardSubType.Giant, false),
            new Table2Element(CardKind.RotlungReanimator, CardSubType.Illusion, CardColor.Green, CardSubType.Juggernaut, false),
            new Table2Element(CardKind.RotlungReanimator, CardSubType.Juggernaut, CardColor.White, CardSubType.Giant, false),
            new Table2Element(CardKind.XathridNecromancer, CardSubType.Kavu, CardColor.Green, CardSubType.Faerie, false),
            new Table2Element(CardKind.RotlungReanimator, CardSubType.Leviathan, CardColor.Green, CardSubType.Juggernaut, false),
            new Table2Element(CardKind.RotlungReanimator, CardSubType.Myr, CardColor.Green, CardSubType.Orc, false),
            new Table2Element(CardKind.RotlungReanimator, CardSubType.Noggle, CardColor.White, CardSubType.Orc, false),
            new Table2Element(CardKind.RotlungReanimator, CardSubType.Orc, CardColor.Green, CardSubType.Noggle, false),
            new Table2Element(CardKind.RotlungReanimator, CardSubType.Pegasus, CardColor.White, CardSubType.Sliver, false),
            new Table2Element(CardKind.XathridNecromancer, CardSubType.Rhino, CardColor.White, CardSubType.Sliver, false),
            new Table2Element(CardKind.XathridNecromancer, CardSubType.Sliver, CardColor.White, CardSubType.Myr, false)
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
                                var dieType = Arrays.stream(CardSubType.values())
                                        .filter(k -> k.name().equals(array[1].trim()))
                                        .findAny();
                                if (dieType.isEmpty()) return;
                                var createColor = Arrays.stream(CardColor.values())
                                        .filter(k -> k.name().equals(array[2].trim()))
                                        .findAny();
                                if (createColor.isEmpty()) return;
                                var createType = Arrays.stream(CardSubType.values())
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

    public record Table2Element(CardKind kind, CardSubType dieType, CardColor createColor, CardSubType createType,
                                boolean phaseIn) {
    }
}
