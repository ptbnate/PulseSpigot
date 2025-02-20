package xyz.krypton.spigot.config.serdes;

import eu.okaeri.configs.annotation.Exclude;
import eu.okaeri.configs.schema.GenericsPair;
import eu.okaeri.configs.serdes.ObjectTransformer;
import eu.okaeri.configs.serdes.SerdesContext;
import eu.okaeri.configs.serdes.SimpleObjectTransformer;
import org.bukkit.ChatColor;

public class DecolorTransformer extends ObjectTransformer<String, String> {

    @Exclude
    private static final ObjectTransformer<String, String> TRANSFORMER = SimpleObjectTransformer.of(String.class, String.class, ChatColor::decolor);

    @Override
    public GenericsPair<String, String> getPair() {
        return TRANSFORMER.getPair();
    }

    @Override
    public String transform(String input, SerdesContext context) {
        return TRANSFORMER.transform(input, context);
    }

}
