package snowesamosc;

import io.magicthegathering.javasdk.api.CardAPI;
import io.magicthegathering.javasdk.resource.ForeignData;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ImageLoader {
    private static Image errorImage = new BufferedImage(223, 311, Image.SCALE_DEFAULT);

    private ImageLoader() {

    }

    public static Image loadCardImage(String englishCardName, String language) {
        Objects.requireNonNull(language);
        Objects.requireNonNull(englishCardName);

        var cards = CardAPI.getAllCards(List.of("name=" + englishCardName));

        if (cards.isEmpty()) return errorImage;
        var card = cards.get(0);

        var myLangInfo =
                Arrays.stream(card.getForeignNames())
                        .filter(info -> language.equals(info.getLanguage()))
                        .findAny();
        var imageUrl = myLangInfo.map(ForeignData::getImageUrl).orElseGet(card::getImageUrl);

        var request = new Request.Builder().url(imageUrl).build();
        var call = new OkHttpClient().newCall(request);
        try (InputStream in = call.execute().body().byteStream()) {
            return ImageIO.read(in);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return errorImage;
        }
    }
}
