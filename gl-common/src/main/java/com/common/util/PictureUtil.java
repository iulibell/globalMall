package com.common.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
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
        if (!trimmed.startsWith("data:")) {
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
