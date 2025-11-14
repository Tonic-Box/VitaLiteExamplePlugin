package com.tonic.woodcutter;

import com.google.inject.Provides;
import com.tonic.Logger;
import com.tonic.api.entities.PlayerAPI;
import com.tonic.api.entities.TileObjectAPI;
import com.tonic.data.wrappers.PlayerEx;
import com.tonic.data.wrappers.TileObjectEx;
import com.tonic.util.ClickManagerUtil;
import com.tonic.util.VitaPlugin;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import javax.inject.Inject;
import java.awt.image.BufferedImage;

@PluginDescriptor(
        name = "Vita Wood Chopper Pro",
        description = "A sample VitaLite Plugin",
        tags = {"vita", "sample", "woodcutter", "wood", "chopper", "pro"}
)
public class ExamplePlugin extends VitaPlugin
{
    @Inject
    private ClientToolbar clientToolbar;
    @Inject
    private Client client;
    private SidePanel panel;
    private NavigationButton navButton;
    private ExamplePluginConfig config;

    @Provides
    ExamplePluginConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(ExamplePluginConfig.class);
    }

    @Override
    protected void startUp()
    {
        panel = injector.getInstance(SidePanel.class);

        final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "icon.png");

        navButton = NavigationButton.builder()
                .tooltip("Example Vita Woodcutting Plugin")
                .icon(icon)
                .priority(5)
                .panel(panel)
                .build();

        clientToolbar.addNavigation(navButton);
    }

    @Override
    protected void shutDown()
    {
        clientToolbar.removeNavigation(navButton);
        panel.shutdown();
    }

    @Override
    public void loop()
    {
        if(panel == null || !panel.isRunning())
            return;

        if(!PlayerEx.getLocal().isIdle())
            return;

        DropStrategy strategy = panel.getSelectedStrategy();
        if(strategy.process())
        {
            return;
        }

        TileObjectAPI.search()
                .withName("Tree")
                .sortNearest()
                .firstOrElse(
                        tree -> {
                            ClickManagerUtil.queueClickBox(tree);
                            tree.interact("Chop down");
                        },
                        () -> Logger.warn("Could not find a tree!")
                );
    }
}
