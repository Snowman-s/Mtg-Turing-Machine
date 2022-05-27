package snowesamosc.mtgturing;

import snowesamosc.mtgturing.cards.CardSubType;

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
    private static final List<CardSubType> TAPE_CARD_SUB_TYPE_LIST = List.of(
            CardSubType.Aetherborn, CardSubType.Basilisk, CardSubType.Demon,
            CardSubType.Cephalid, CardSubType.Elf, CardSubType.Faerie,
            CardSubType.Giant, CardSubType.Illusion, CardSubType.Harpy,
            CardSubType.Juggernaut, CardSubType.Kavu, CardSubType.Leviathan,
            CardSubType.Myr, CardSubType.Noggle, CardSubType.Pegasus,
            CardSubType.Orc, CardSubType.Rhino, CardSubType.Sliver
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

    private static List<CardSubType> createRandomTape() {
        int tapeLength = 3 + ThreadLocalRandom.current().nextInt(5);

        Stream.Builder<CardSubType> bulider = Stream.builder();

        for (int i = 0; i < tapeLength; i++) {
            bulider.add(TAPE_CARD_SUB_TYPE_LIST.get(ThreadLocalRandom.current().nextInt(TAPE_CARD_SUB_TYPE_LIST.size())));
        }

        return bulider.build().collect(Collectors.toList());
    }

    public static List<CardSubType> loadTape() {
        try {
            var saveFile = getSaveDirectory();

            if (Files.exists(saveFile)) {
                try (var reader = Files.newBufferedReader(saveFile)) {
                    var ret = new ArrayList<CardSubType>();
                    String line = reader.readLine();
                    line.codePoints().forEach(codePoint -> {
                        Optional<CardSubType> type = TAPE_CARD_SUB_TYPE_LIST.stream()
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
