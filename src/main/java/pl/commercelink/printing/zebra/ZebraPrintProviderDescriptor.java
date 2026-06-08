package pl.commercelink.printing.zebra;

import pl.commercelink.printing.api.PrintProvider;
import pl.commercelink.printing.api.PrintProviderDescriptor;
import pl.commercelink.provider.api.ProviderField;

import java.util.List;
import java.util.Map;

import static pl.commercelink.provider.api.ProviderField.FieldType.NUMBER;
import static pl.commercelink.provider.api.ProviderField.FieldType.TEXT;

public class ZebraPrintProviderDescriptor implements PrintProviderDescriptor {

    @Override
    public String name() {
        return "zebra";
    }

    @Override
    public String displayName() {
        return "Zebra ZPL";
    }

    @Override
    public List<ProviderField> configurationFields() {
        return List.of(
                new ProviderField("deviceId", "ID drukarki (Browser Print)", TEXT, true, ""),
                new ProviderField("labelWidthMm", "Szerokość etykiety (mm)", NUMBER, true, "100"),
                new ProviderField("labelHeightMm", "Wysokość etykiety (mm)", NUMBER, true, "50"),
                new ProviderField("dpi", "DPI", NUMBER, false, "203"),
                new ProviderField("darkness", "Zaciemnienie (0-30)", NUMBER, false, ""),
                new ProviderField("printSpeed", "Prędkość druku", NUMBER, false, ""));
    }

    @Override
    public PrintProvider create(Map<String, String> configuration) {
        return new ZebraPrintProvider(configuration);
    }
}
