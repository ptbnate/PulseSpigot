package me.nate.spigot.config.serdes;

import eu.okaeri.configs.schema.GenericsPair;
import eu.okaeri.configs.serdes.BidirectionalTransformer;
import eu.okaeri.configs.serdes.SerdesContext;
import org.bukkit.Warning;

public class WarningStateTransformer extends BidirectionalTransformer<String, Warning.WarningState> {

    @Override
    public GenericsPair<String, Warning.WarningState> getPair() {
        return this.genericsPair(String.class, Warning.WarningState.class);
    }

    @Override
    public Warning.WarningState leftToRight(String data, SerdesContext serdesContext) {
        return Warning.WarningState.value(data);
    }

    @Override
    public String rightToLeft(Warning.WarningState data, SerdesContext serdesContext) {
        return data.name().toLowerCase();
    }

}
