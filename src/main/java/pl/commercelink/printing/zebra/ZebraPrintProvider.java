package pl.commercelink.printing.zebra;

import pl.commercelink.printing.api.PrintJob;
import pl.commercelink.printing.api.PrintProvider;
import pl.commercelink.printing.api.WarehouseLabel;

import java.util.Map;

public class ZebraPrintProvider implements PrintProvider {

    private static final int DEFAULT_DPI = 203;
    private static final int DEFAULT_WIDTH_MM = 100;
    private static final int DEFAULT_HEIGHT_MM = 50;

    private final int widthDots;
    private final int heightDots;
    private final Integer darkness;
    private final Integer printSpeed;

    public ZebraPrintProvider(Map<String, String> configuration) {
        int dpi = intValue(configuration.get("dpi"), DEFAULT_DPI);
        this.widthDots = toDots(intValue(configuration.get("labelWidthMm"), DEFAULT_WIDTH_MM), dpi);
        this.heightDots = toDots(intValue(configuration.get("labelHeightMm"), DEFAULT_HEIGHT_MM), dpi);
        this.darkness = optionalInt(configuration.get("darkness"));
        this.printSpeed = optionalInt(configuration.get("printSpeed"));
    }

    @Override
    public PrintJob renderWarehouseLabel(WarehouseLabel label) {
        StringBuilder zpl = new StringBuilder();
        zpl.append("^XA");
        zpl.append("^CI28");
        zpl.append("^PW").append(widthDots);
        zpl.append("^LL").append(heightDots);
        if (darkness != null) {
            zpl.append("^MD").append(darkness);
        }
        if (printSpeed != null) {
            zpl.append("^PR").append(printSpeed);
        }

        int margin = 16;
        int leftWidth = Math.max(80, (int) (widthDots * 0.9) - margin);
        int availableHeight = Math.max(40, heightDots - 2 * margin);

        boolean hasDistributor = label.hasDistributor();
        int nameLines = 2;
        int distributorLines = hasDistributor ? 2 : 0;

        int nameFont = 26;
        int bodyFont = 22;
        int gap = 6;

        int needed = nameLines * (nameFont + gap)
                + 5 * (bodyFont + gap);
        if (needed > availableHeight) {
            double scale = (double) availableHeight / needed;
            nameFont = Math.max(8, (int) (nameFont * scale));
            bodyFont = Math.max(8, (int) (bodyFont * scale));
            gap = Math.max(2, (int) (gap * scale));
        }

        int y = margin;
        zpl.append(wrappedField(margin, y, nameFont, leftWidth, nameLines, label.name()));
        y += nameLines * (nameFont + gap);

        zpl.append(field(margin, y, bodyFont, "EAN " + safe(label.ean())));
        y += bodyFont + gap;
        zpl.append(field(margin, y, bodyFont, "MFN " + safe(label.mfn())));
        y += bodyFont + gap;

        if (hasDistributor) {
            y += bodyFont + gap;
            zpl.append(wrappedField(margin, y, bodyFont, leftWidth, distributorLines,
                    "Dystrybutor: " + buildDistributorLine(label.distributor())));
            y += distributorLines * (bodyFont + gap);
        }

        if (label.deliveryId() != null && !label.deliveryId().isBlank()) {
            String delivery = sanitize(label.deliveryId());
            int stripFont = availableHeight / Math.max(1, delivery.length());
            stripFont = Math.max(14, Math.min(stripFont, 40));
            int deliveryX = Math.max(margin, widthDots - stripFont - margin);
            zpl.append("^FO").append(deliveryX).append(",").append(margin)
                    .append("^A0B,").append(stripFont).append(",").append(stripFont)
                    .append("^FD").append(delivery).append("^FS");
        }

        zpl.append("^XZ");
        return new PrintJob(zpl.toString(), "application/zpl");
    }

    private static String field(int x, int y, int fontSize, String data) {
        return "^FO" + x + "," + y + "^A0N," + fontSize + "," + fontSize + "^FD" + sanitize(data) + "^FS";
    }

    private static String wrappedField(int x, int y, int fontSize, int blockWidth, int maxLines, String data) {
        return "^FO" + x + "," + y + "^A0N," + fontSize + "," + fontSize
                + "^FB" + blockWidth + "," + maxLines + ",0,L,0"
                + "^FD" + sanitize(data) + "^FS";
    }

    private static String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return removeDiacritics(value).replace("^", " ").replace("~", " ").replace("\\", " ").trim();
    }

    private static String removeDiacritics(String value) {
        return value
                .replace('ą', 'a').replace('Ą', 'A')
                .replace('ć', 'c').replace('Ć', 'C')
                .replace('ę', 'e').replace('Ę', 'E')
                .replace('ł', 'l').replace('Ł', 'L')
                .replace('ń', 'n').replace('Ń', 'N')
                .replace('ó', 'o').replace('Ó', 'O')
                .replace('ś', 's').replace('Ś', 'S')
                .replace('ż', 'z').replace('Ż', 'Z')
                .replace('ź', 'z').replace('Ź', 'Z');
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String join(String first, String second) {
        return (safe(first) + " " + safe(second)).trim();
    }

    private static String buildDistributorLine(WarehouseLabel.Distributor distributor) {
        StringBuilder builder = new StringBuilder();
        appendPart(builder, distributor.companyName());
        appendPart(builder, distributor.street());
        appendPart(builder, join(distributor.postalCode(), distributor.city()));
        appendPart(builder, distributor.country());
        return builder.toString();
    }

    private static void appendPart(StringBuilder builder, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(", ");
        }
        builder.append(value.trim());
    }

    private static int toDots(int millimeters, int dpi) {
        return Math.round(millimeters * dpi / 25.4f);
    }

    private static int intValue(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static Integer optionalInt(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
