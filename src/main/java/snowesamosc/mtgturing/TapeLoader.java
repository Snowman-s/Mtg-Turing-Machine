package snowesamosc.mtgturing;

import snowesamosc.mtgturing.cards.CreatureType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TapeLoader {
    private static final List<CreatureType> tapeCreatureTypeList = List.of(
            CreatureType.Aetherborn, CreatureType.Basilisk, CreatureType.Demon,
            CreatureType.Cephalid, CreatureType.Elf, CreatureType.Faerie,
            CreatureType.Giant, CreatureType.Illusion, CreatureType.Harpy,
            CreatureType.Juggernaut, CreatureType.Kavu, CreatureType.Leviathan,
            CreatureType.Myr, CreatureType.Noggle, CreatureType.Pegasus,
            CreatureType.Orc, CreatureType.Rhino, CreatureType.Sliver
    );

    private TapeLoader() {

    }

    private static Path getSaveDirectory() throws IOException {
        var dir = Path.of("./data/");
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        return dir.resolve("tape.txt");
    }

    private static List<CreatureType> createRandomTape() {
        int tapeLength = 3 + ThreadLocalRandom.current().nextInt(5);

        Stream.Builder<CreatureType> bulider = Stream.builder();

        for (int i = 0; i < tapeLength; i++) {
            bulider.add(tapeCreatureTypeList.get(ThreadLocalRandom.current().nextInt(tapeCreatureTypeList.size())));
        }

        return bulider.build().collect(Collectors.toList());
    }

    public static List<CreatureType> loadTape() {
        try {
            var saveFile = getSaveDirectory();

            if (Files.exists(saveFile)) {
                try (var reader = Files.newBufferedReader(saveFile)) {
                    var ret = new ArrayList<CreatureType>();
                    String line = reader.readLine();
                    line.codePoints().forEach(codePoint -> {
                        Optional<CreatureType> type = tapeCreatureTypeList.stream()
                                .filter(c -> c.name().codePointAt(0) == codePoint)
                                .findAny();
                        type.ifPresent(ret::add);
                    });
                    return ret;
                }
            } else {
                var ret = createRandomTape();

                try (var writer = Files.newBufferedWriter(saveFile)) {
                    for (var element : ret) {
                        writer.write(element.name().codePointAt(0));
                    }
                }

                return ret;
            }
        } catch (IOException e) {
            return createRandomTape();
        }
    }
}
