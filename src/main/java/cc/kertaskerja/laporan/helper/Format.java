package cc.kertaskerja.laporan.helper;

public class Format {

    public static Integer parseInteger(Object value) {
        if (value == null) return null;
        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

