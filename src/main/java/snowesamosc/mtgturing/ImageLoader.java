package snowesamosc.mtgturing;

import io.magicthegathering.javasdk.api.CardAPI;
import io.magicthegathering.javasdk.resource.ForeignData;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

public class ImageLoader {
    private static final Image errorImage = new BufferedImage(223, 311, Image.SCALE_DEFAULT);

    private ImageLoader() {

    }

    private static Path getSaveDirectory(String lang) throws IOException {
        var dir = Path.of("./cards/" + lang);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        return dir;
    }

    public static <T> EnumMap<CardType, T> loadAllCardImage
            (EnumSet<CardType> englishCardNames, String language, Function<Image, T> mapper) {
        var count = new CountDownLatch(englishCardNames.size());
        EnumMap<CardType, T> map = new EnumMap<>(CardType.class);

        for (var card : englishCardNames) {
            new Thread(() -> {
                map.put(card, mapper.apply(loadCardImage(card.getOriginalName(), language)));
                count.countDown();
            }).start();
        }

        try {
            count.await();
        } catch (InterruptedException e) {
            System.err.println("画像ロードに失敗しました。プログラムを終了します。");
            System.exit(-1);
        }

        return map;
    }

    public static Image loadCardImage(String englishCardName, String language) {
        Objects.requireNonNull(language);
        Objects.requireNonNull(englishCardName);

        try {
            var image = loadImageFromFile(englishCardName, language);
            System.out.println("ImageLoader: " + englishCardName + " was loaded from tmp file.");
            return image;
        } catch (IOException e) {
            var image = loadImageFromCardAPI(englishCardName, language);
            System.out.println("ImageLoader: " + englishCardName + " was loaded from API.");
            return image;
        }
    }

    private static Image loadImageFromFile(String englishCardName, String language) throws IOException {
        var saveDir = getSaveDirectory(language);
        try (InputStream in = Files.newInputStream(saveDir.resolve(englishCardName + ".png"))) {
            var image = ImageIO.read(in);
            if (image == null) throw new IOException("could not read image from file");
            return image;
        }
    }

    private static Image loadImageFromCardAPI(String englishCardName, String language) {
        var cards = CardAPI.getAllCards(List.of("name=" + englishCardName));

        var optionalCard = cards.stream().filter(c->c.getName().equals(englishCardName)).findAny();
        if (optionalCard.isEmpty()) return errorImage;
        var card = optionalCard.get();

        var myLangInfo = Optional.ofNullable(card.getForeignNames())
                .flatMap(
                        array -> Arrays.stream(array)
                                .filter(info -> language.equals(info.getLanguage()))
                                .findAny()
                );
        var imageUrl = myLangInfo.map(ForeignData::getImageUrl).orElseGet(card::getImageUrl);

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
            return errorImage;
        }
        try {
            var saveDir = getSaveDirectory(language);
            try (OutputStream os = Files.newOutputStream(saveDir.resolve(englishCardName + ".png"))) {
                ImageIO.write(image, "png", os);
            }
        } catch (IOException e) {
            e.printStackTrace();
            //一時ファイルとしての保存なので失敗しても支障はない。
        }

        return image;
    }

    private static String getCardImageUrlCannotGetUsualWay(String englishCardName, String language, int multiverseId) {
        //間に合わせで無理やり画像を提示する場所

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
}
