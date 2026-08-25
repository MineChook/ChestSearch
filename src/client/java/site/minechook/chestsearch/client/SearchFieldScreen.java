package site.minechook.chestsearch.client;

import net.minecraft.client.input.KeyEvent;

public interface SearchFieldScreen {
    boolean chestsearch$isSearchFocused();

    void chestsearch$handleSearchKey(KeyEvent event);
}
