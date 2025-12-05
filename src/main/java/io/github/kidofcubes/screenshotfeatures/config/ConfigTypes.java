package io.github.kidofcubes.screenshotfeatures.config;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.kidofcubes.screenshotfeatures.ScreenshotFeatures;

import java.util.Locale;

public class ConfigTypes {
    public static abstract class SimpleOptions<T extends SimpleOptions<?>> implements IConfigOptionListEntry {
        protected final String name;
        protected final int index;
        public SimpleOptions(String name, int index){
            this.name=name;
            this.index=index;
        }

        public abstract T[] values();

        @Override
        public String getStringValue() {
            return name.toLowerCase(Locale.ROOT);
        }

        @Override
        public String getDisplayName() {
            return StringUtils.translate(ScreenshotFeatures.MOD_ID+".text.types."+getStringValue());
        }

        @Override
        public IConfigOptionListEntry cycle(boolean forward) {
            return values()[(index+(forward ? 1 : -1)) % values().length];
        }

        @Override
        public IConfigOptionListEntry fromString(String name) {
            for (T x: values()) {
                if (x.getStringValue().equalsIgnoreCase(name)) {
                    return x;
                }
            }
            return values()[values().length-1];
        }
    }

    public static class OptionalBoolean extends SimpleOptions<OptionalBoolean> {
        private OptionalBoolean(String name, int index) {
            super(name, index);
        }
        public static final OptionalBoolean TRUE = new OptionalBoolean("TRUE", 0);
        public static final OptionalBoolean FALSE = new OptionalBoolean("FALSE", 1);
        public static final OptionalBoolean NO_OP = new OptionalBoolean("NO_OP", 2);

        @Override
        public OptionalBoolean[] values() {
            return new OptionalBoolean[]{
                    TRUE,
                    FALSE,
                    NO_OP
            };
        }
    }

    public static class WeatherTypes extends SimpleOptions<WeatherTypes> {
        private WeatherTypes(String name, int index) {
            super(name, index);
        }
        public static final WeatherTypes RAIN = new WeatherTypes("RAIN", 0);
        public static final WeatherTypes THUNDER = new WeatherTypes("THUNDER", 1);
        public static final WeatherTypes CLEAR = new WeatherTypes("CLEAR", 2);

        @Override
        public WeatherTypes[] values() {
            return new WeatherTypes[]{
                    RAIN,
                    THUNDER,
                    CLEAR
            };
        }
    }

    public static class PrecipitationType extends SimpleOptions<PrecipitationType> {
        private PrecipitationType(String name, int index) {
            super(name, index);
        }
        public static final PrecipitationType RAIN = new PrecipitationType("RAIN",0);
        public static final PrecipitationType SNOW = new PrecipitationType("SNOW",1);
        public static final PrecipitationType CLEAR = new PrecipitationType("CLEAR",2);
        public static final PrecipitationType NO_OP = new PrecipitationType("NO_OP",3);

        @Override
        public PrecipitationType[] values() {
            return new PrecipitationType[]{
                    RAIN,
                    SNOW,
                    CLEAR,
                    NO_OP
            };
        }
    }
}
