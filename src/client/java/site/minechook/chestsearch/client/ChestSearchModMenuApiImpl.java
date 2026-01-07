package site.minechook.chestsearch.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ChestSearchModMenuApiImpl implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> getModConfigScreen(parent);
    }

    public Screen getModConfigScreen(Screen parent) {
        return createConfigScreen(parent);
    }

    private Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setTitle(Text.of("Chest Search"))
                .setParentScreen(parent);

        builder.setSavingRunnable(ChestSearchClient::saveConfig);

        ConfigCategory main = builder.getOrCreateCategory(Text.of("Main"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        main.addEntry(entryBuilder.startBooleanToggle(Text.of("Enable"), true).setDefaultValue(true)
                .setSaveConsumer(enabled -> ChestSearchClient.enabled = enabled)
                .build());

        main.addEntry(entryBuilder.startAlphaColorField(Text.of("Color"), ChestSearchClient.color)
                .setDefaultValue(0x506EEB85)
                .setSaveConsumer(color -> ChestSearchClient.color = color)
                .build());

        return builder.build();
    }
}
