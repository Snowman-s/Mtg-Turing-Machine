package snowesamosc.mtgturing;

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
import java.util.Objects;
import java.util.function.Function;

public class TokenLoader {
    private static final BufferedImage errorImage = new BufferedImage(223, 311, Image.SCALE_DEFAULT);

    private TokenLoader() {

    }

    private static Path getSaveFile() throws IOException {
        var dir = Path.of("./data/");
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        return dir.resolve("token.png");
    }

    public static <I> I loadTokenImage(Function<Image, I> mapper) {
        Objects.requireNonNull(mapper);

        try {
            var image = loadImageFromFile(mapper);
            System.out.println("TokenLoader: Token image was loaded from tmp file.");
            return image;
        } catch (IOException e) {
            var image = loadImageFromWeb(mapper);
            System.out.println("TokenLoader: Token image was loaded from Web.");
            return image;
        }
    }

    private static <I> I loadImageFromFile(Function<Image, I> mapper) throws IOException {
        var saveDir = getSaveFile();
        Image image;
        try (InputStream in = Files.newInputStream(saveDir)) {
            image = ImageIO.read(in);
            if (image == null) throw new IOException("could not read image from file");
        }

        return mapper.apply(image);
    }

    private static <I> I loadImageFromWeb(Function<Image, I> mapper) {
        var request = new Request.Builder().url("https://media.wizards.com/2021/stx/en_mYnFHwNAcP.png").build();
        var call = new OkHttpClient().newCall(request);

        BufferedImage image;
        try (InputStream in = Objects.requireNonNull(call.execute().body()).byteStream()) {
            image = ImageIO.read(in);
        } catch (IOException e) {
            e.printStackTrace();
            image = errorImage;
        }
        try {
            var saveDir = getSaveFile();
            try (OutputStream os = Files.newOutputStream(saveDir)) {
                ImageIO.write(image, "png", os);
            }
        } catch (IOException e) {
            e.printStackTrace();
            //一時ファイルとしての保存なので失敗しても支障はない。
        }

        return mapper.apply(image);
    }
}
