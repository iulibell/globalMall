package com.common.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.common.constant.UrlConstant;
import org.springframework.stereotype.Component;
/**
 * 将前端上传的 data URL 图片落盘到 {@link UrlConstant#PIC_URL}，并返回可入库的文件名；
 * 亦可按文件名删除同目录下的图片。
 */
@Component
public class PictureUtil {

    private static final Pattern DATA_URL = Pattern.compile(
            "^data:([^;]+);base64,(.+)$",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Set<String> DEFAULT_ALLOWED_URL_HOSTS = Set.of("localhost", "127.0.0.1");

    private static final long MAX_BYTES = 5L * 1024 * 1024;

    /**
     * @param picture 前端 {@code readAsDataURL} 结果，或已是短路径/文件名则原样返回
     * @return 保存后的文件名（仅文件名，不含目录），便于写入 {@code goods_review.picture}（长度 ≤255）
     */
    public static String persistPictureField(String picture) throws IOException {
        return persistPictureField(picture, Path.of(UrlConstant.PIC_URL));
    }

    /**
     * 与 {@link #persistPictureField(String)} 相同，但指定落盘根目录（须与读取、删除时使用同一目录）。
     */
    public static String persistPictureField(String picture, Path baseDirectory) throws IOException {
        if (picture == null || picture.isBlank()) {
            throw new IllegalArgumentException("商品图片不能为空");
        }
        String trimmed = picture.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            validateRemotePictureUrl(trimmed);
            return trimmed;
        }
        if (!trimmed.startsWith("data:")) {
            validatePictureName(trimmed);
            return trimmed;
        }
        Matcher m = DATA_URL.matcher(trimmed);
        if (!m.matches()) {
            throw new IllegalArgumentException("图片格式无效，请使用 data URL 上传");
        }
        String mime = m.group(1).trim().toLowerCase();
        String b64 = m.group(2).replaceAll("\\s", "");
        byte[] bytes = Base64.getDecoder().decode(b64);
        if (bytes.length > MAX_BYTES) {
            throw new IllegalArgumentException("图片大小不能超过 5MB");
        }
        String ext = extensionForMime(mime);
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path dir = baseDirectory.toAbsolutePath().normalize();
        Files.createDirectories(dir);
        Files.write(dir.resolve(fileName), bytes);
        return fileName;
    }

    /**
     * 删除 {@link UrlConstant#PIC_URL}（gl-mall-frontend {@code src/assets/picture}）下与
     * {@link #persistPictureField} 返回值同名的文件。
     *
     * @param pictureName 仅文件名，不得含路径或 {@code ..}
     * @return 若该路径存在且为普通文件并已删除则 true；名为空或文件不存在则 false
     */
    public static boolean deletePictureByName(String pictureName) throws IOException {
        return deletePictureByName(pictureName, Path.of(UrlConstant.PIC_URL));
    }

    public static boolean deletePictureByName(String pictureName, Path baseDirectory) throws IOException {
        if (pictureName == null || pictureName.isBlank()) {
            return false;
        }
        String name = pictureName.trim();
        if (name.contains("..") || name.indexOf('/') >= 0 || name.indexOf('\\') >= 0) {
            throw new IllegalArgumentException("非法图片名");
        }
        Path base = baseDirectory.toAbsolutePath().normalize();
        Path target = base.resolve(name).normalize();
        if (!target.startsWith(base)) {
            throw new IllegalArgumentException("非法图片名");
        }
        if (!Files.isRegularFile(target)) {
            return false;
        }
        Files.delete(target);
        return true;
    }

    /**
     * 读取上架商品图（仅文件名）；目录外或不存在则返回 null。
     */
    public static byte[] readPictureBytes(String pictureName, Path baseDirectory) throws IOException {
        if (pictureName == null || pictureName.isBlank()) {
            return null;
        }
        String name = pictureName.trim();
        if (name.contains("..") || name.indexOf('/') >= 0 || name.indexOf('\\') >= 0) {
            return null;
        }
        Path base = baseDirectory.toAbsolutePath().normalize();
        Path target = base.resolve(name).normalize();
        if (!target.startsWith(base) || !Files.isRegularFile(target)) {
            return null;
        }
        return Files.readAllBytes(target);
    }

    private static void validatePictureName(String pictureName) {
        if (pictureName.isBlank()
                || pictureName.contains("..")
                || pictureName.indexOf('/') >= 0
                || pictureName.indexOf('\\') >= 0) {
            throw new IllegalArgumentException("非法图片名");
        }
    }

    private static void validateRemotePictureUrl(String pictureUrl) {
        final URI uri;
        try {
            uri = new URI(pictureUrl);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("图片 URL 格式非法");
        }
        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException("仅支持 http/https 图片 URL");
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("图片 URL 缺少主机名");
        }
        String normalizedHost = host.toLowerCase(Locale.ROOT);
        Set<String> allowHosts = loadAllowedUrlHosts();
        if (!allowHosts.contains(normalizedHost)) {
            throw new IllegalArgumentException("图片 URL 域名不在白名单内");
        }
    }

    private static Set<String> loadAllowedUrlHosts() {
        Set<String> hosts = new LinkedHashSet<>(DEFAULT_ALLOWED_URL_HOSTS);
        String configured = System.getProperty("picture.url.whitelist", System.getenv("PICTURE_URL_WHITELIST"));
        if (configured == null || configured.isBlank()) {
            return hosts;
        }
        for (String raw : configured.split(",")) {
            String h = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
            if (!h.isEmpty()) {
                hosts.add(h);
            }
        }
        return hosts;
    }

    private static String extensionForMime(String mime) {
        if (mime.contains("jpeg") || mime.contains("jpg")) {
            return "jpg";
        }
        if (mime.contains("png")) {
            return "png";
        }
        if (mime.contains("gif")) {
            return "gif";
        }
        if (mime.contains("webp")) {
            return "webp";
        }
        return "bin";
    }
}
