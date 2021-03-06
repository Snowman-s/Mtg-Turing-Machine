package snowesamosc.mtgturing;

import io.magicthegathering.javasdk.api.CardAPI;
import io.magicthegathering.javasdk.resource.ForeignData;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import snowesamosc.mtgturing.cards.CardColor;
import snowesamosc.mtgturing.cards.CardSubType;
import snowesamosc.mtgturing.cards.CardType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CardLoader {
    private static final BufferedImage errorImage = new BufferedImage(223, 311, Image.SCALE_DEFAULT);

    private CardLoader() {

    }

    private static Path getSaveDirectory(String lang) throws IOException {
        var dir = Path.of("./data/cards/" + lang);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        return dir;
    }

    private static Path getCardTextSaveDirectory(String lang) throws IOException {
        var dir = getSaveDirectory(lang).resolve("./texts/");
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        return dir;
    }

    public static <I> EnumMap<CardKind, CardInfo<I>> loadAllCard
            (EnumSet<CardKind> englishCardNames, String language, Function<Image, I> mapper) {
        var count = new CountDownLatch(englishCardNames.size());
        EnumMap<CardKind, CardInfo<I>> map = new EnumMap<>(CardKind.class);

        for (var card : englishCardNames) {
            new Thread(() -> {
                map.put(card, loadCard(card.getOriginalName(), language, mapper));
                count.countDown();
            }).start();
        }

        try {
            count.await();
        } catch (InterruptedException e) {
            System.err.println("Failed to load card information. Terminate the program.");
            System.exit(-1);
        }

        return map;
    }

    public static <I> CardInfo<I> loadCard(String englishCardName, String language, Function<Image, I> mapper) {
        Objects.requireNonNull(language);
        Objects.requireNonNull(englishCardName);

        try {
            var info = loadCardInfoFromFile(englishCardName, language, mapper);
            System.out.println("CardLoader: " + englishCardName + " was loaded from tmp file.");
            return info;
        } catch (IOException e) {
            var info = loadCardInfoFromCardAPI(englishCardName, language, mapper);
            System.out.println("CardLoader: " + englishCardName + " was loaded from API.");
            return info;
        }
    }

    private static <I> CardInfo<I> loadCardInfoFromFile(String englishCardName, String language, Function<Image, I> mapper) throws IOException {
        var saveDir = getSaveDirectory(language);
        var textDir = getCardTextSaveDirectory(language);
        Image image;
        String name;
        String txt;
        Set<CardColor> colors = new HashSet<>();
        Set<CardType> types = new HashSet<>();
        Set<CardSubType> cardSubTypes = new HashSet<>();
        int power, toughness;
        try (InputStream in = Files.newInputStream(saveDir.resolve(englishCardName + ".png"))) {
            image = ImageIO.read(in);
            if (image == null) throw new IOException("could not read image from file");
        }
        try (BufferedReader reader = Files.newBufferedReader(textDir.resolve(englishCardName + ".txt"))) {
            var textLines = reader.lines().collect(Collectors.toList());
            name = textLines.remove(0);
            var colorArray = textLines.remove(0).split(",");
            for (var e : colorArray) {
                if (e.length() == 0) continue;
                colors.add(
                        Arrays.stream(CardColor.values())
                                .filter(color -> color.name().equals(e))
                                .findAny().orElseThrow(IOException::new)
                );
            }
            var typeArray = textLines.remove(0).split(",");
            for (var e : typeArray) {
                if (e.length() == 0) continue;
                types.add(
                        Arrays.stream(CardType.values())
                                .filter(type -> type.name().equals(e))
                                .findAny().orElseThrow(IOException::new)
                );
            }
            var subTypeArray = textLines.remove(0).split(",");
            for (var e : subTypeArray) {
                if (e.length() == 0) continue;
                cardSubTypes.add(
                        Arrays.stream(CardSubType.values())
                                .filter(type -> type.name().equals(e))
                                .findAny().orElseThrow(IOException::new)
                );
            }

            var pt = textLines.remove(0).split("/");
            try {
                power = Integer.parseInt(pt[0]);
                toughness = Integer.parseInt(pt[1]);
            } catch (Exception e) {
                throw new IOException(e);
            }
            txt = String.join("\n", textLines);
        }

        return new CardInfo<>(txt, name, mapper.apply(image), colors, types, cardSubTypes, power, toughness);
    }

    private static <I> CardInfo<I> loadCardInfoFromCardAPI(String englishCardName, String language, Function<Image, I> mapper) {
        var cards = CardAPI.getAllCards(List.of("name=" + englishCardName));

        var optionalCard = cards.stream().filter(c -> c.getName().equals(englishCardName)).findAny();
        if (optionalCard.isEmpty())
            return new CardInfo<>("Load Failed", "Load Failed", mapper.apply(errorImage),
                    Set.of(), Set.of(), Set.of(), 0, 0);
        var card = optionalCard.get();

        var myLangInfo = Optional.ofNullable(card.getForeignNames())
                .flatMap(
                        array -> Arrays.stream(array)
                                .filter(info -> language.equals(info.getLanguage()))
                                .findAny()
                );
        var imageUrl = myLangInfo.map(ForeignData::getImageUrl).orElseGet(card::getImageUrl);
        var cardText = myLangInfo.map(ForeignData::getText).orElseGet(card::getText);
        var cardName = myLangInfo.map(ForeignData::getName).orElseGet(card::getName);

        var power = card.getPower() == null ? 0 : Integer.parseInt(card.getPower());
        var toughness = card.getToughness() == null ? 0 : Integer.parseInt(card.getToughness());

        Set<CardColor> colors = card.getColors() == null ?
                Set.of() :
                Arrays.stream(card.getColors())
                        .map(str -> Arrays.stream(CardColor.values())
                                .filter(color -> color.name().equals(str))
                                .findAny()
                        )
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toSet());
        Set<CardType> types = Arrays.stream(card.getTypes())
                .map(str -> Arrays.stream(CardType.values())
                        .filter(color -> color.name().equals(str))
                        .findAny()
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        Set<CardSubType> cardSubTypes = card.getSubtypes() == null ?
                Set.of() :
                Arrays.stream(card.getSubtypes())
                        .map(str -> Arrays.stream(CardSubType.values())
                                .filter(color -> color.name().equals(str))
                                .findAny()
                        )
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toSet());

        if (imageUrl == null) {
            imageUrl = getCardImageUrlCannotGetUsualWay(englishCardName, language, card.getMultiverseid());
        }

        var request = new Request.Builder().url(imageUrl).build();
        var call = new OkHttpClient().newCall(request);

        BufferedImage image;
        try (InputStream in = Objects.requireNonNull(call.execute().body()).byteStream()) {
            image = ImageIO.read(in);
        } catch (IOException e) {
            e.printStackTrace();
            image = errorImage;
        }
        try {
            var saveDir = getSaveDirectory(language);
            try (OutputStream os = Files.newOutputStream(saveDir.resolve(englishCardName + ".png"))) {
                ImageIO.write(image, "png", os);
            }
            var textSaveDir = getCardTextSaveDirectory(language);
            try (BufferedWriter writer = Files.newBufferedWriter(textSaveDir.resolve(englishCardName + ".txt"))) {
                writer.write(cardName + "\n");
                writer.write(colors.stream().map(Enum::name).collect(Collectors.joining(",")) + "\n");
                writer.write(types.stream().map(Enum::name).collect(Collectors.joining(",")) + "\n");
                writer.write(cardSubTypes.stream().map(Enum::name).collect(Collectors.joining(",")) + "\n");
                writer.write(power + "/" + toughness + "\n");
                writer.write(cardText);
            }
        } catch (IOException e) {
            e.printStackTrace();
            //??????????????????????????????????????????????????????????????????????????????
        }

        return new CardInfo<>(cardText, cardName, mapper.apply(image), colors, types, cardSubTypes, power, toughness);
    }

    private static String getCardImageUrlCannotGetUsualWay(String englishCardName, String language, int multiverseId) {
        //?????????????????????????????????????????????????????????

        var map = Map.of(
                "Wheel of Sun and Moon", Map.of(
                        "Japanese", "https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=174118&type=card",
                        "English", "https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=146740&type=card"
                )
        );
        return map
                .getOrDefault(englishCardName, Collections.emptyMap())
                .getOrDefault(language, "https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=" + multiverseId + "&type=card");
    }

    public static record CardInfo<I>(String cardText, String cardName, I mappedImage,
                                     Set<CardColor> colors, Set<CardType> types, Set<CardSubType> cardSubTypes,
                                     int power, int toughness) {

    }
}
