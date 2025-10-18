package cc.kertaskerja.laporan.utils;

import java.util.Optional;
import java.util.function.Consumer;

// Utilization class for patching JSON objects
// untuk mengupdate sebagaian data dari DTO
public class PatchUtil {
    public static <T> void apply(T value, Consumer<T> consumer) {
        Optional.ofNullable(value).ifPresent(consumer);
    }
}
