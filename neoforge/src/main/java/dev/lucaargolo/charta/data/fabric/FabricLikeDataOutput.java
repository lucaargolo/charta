package dev.lucaargolo.charta.data.fabric;

import net.minecraft.data.PackOutput;

import java.nio.file.Path;

public final class FabricLikeDataOutput extends PackOutput {

    private final boolean strictValidation;

    public FabricLikeDataOutput(Path path, boolean strictValidation) {
        super(path);
        this.strictValidation = strictValidation;
    }

    public boolean isStrictValidationEnabled() {
        return strictValidation;
    }

}
