package net.reentityoutliner.config;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TabsConfigScreen extends Screen {
    private TabManager tabManager;
    private TabNavigationBar tabNavigationBar;
    

    public TabsConfigScreen(Screen screen) {
        super(Component.literal("Test"));
    }

    @Override
    protected void init() {
        this.tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);

        var tabA = new GridLayoutTab(Component.literal("Test1"));
        var tabB = new GridLayoutTab(Component.literal("Test2"));

        this.tabNavigationBar = TabNavigationBar.builder(this.tabManager, 200)
                .addTabs(tabA, tabB)
                .build();

        this.addRenderableWidget(this.tabNavigationBar);
        this.tabNavigationBar.selectTab(0, false);
    }
}
