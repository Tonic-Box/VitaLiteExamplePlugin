package com.tonic.woodcutter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.tonic.Logger;
import com.tonic.Static;
import com.tonic.model.ui.components.FancyButton;
import com.tonic.model.ui.components.FancyCard;
import com.tonic.model.ui.components.FancyDropdown;
import com.tonic.services.breakhandler.BreakHandler;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public class SidePanel extends PluginPanel
{
    private final ExamplePluginConfig config;
    private final JLabel timerLabel;
    private final JButton startStopButton;
    private final FancyDropdown<DropStrategy> strategyDropdown;
    private final Timer timer;
    @Inject
    private BreakHandler breakHandler;

    private long startTime;
    private boolean isRunning = false;

    @Inject
    public SidePanel(ExamplePlugin plugin, ExamplePluginConfig config)
    {
        this.config = config;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.insets = new Insets(0, 0, 10, 0);

        FancyCard card = new FancyCard("Wood Chopper", "A sample VitaLite automation plugin that power-chops trees.");
        add(card, c);
        c.gridy++;

        JPanel timerPanel = new JPanel();
        timerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        timerPanel.setBorder(new EmptyBorder(15, 10, 15, 10));
        timerPanel.setLayout(new BorderLayout());

        timerLabel = new JLabel("00:00:00", SwingConstants.CENTER);
        Color TIMER_COLOR = new Color(0, 255, 0);
        timerLabel.setForeground(TIMER_COLOR);
        timerLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
        timerPanel.add(timerLabel, BorderLayout.CENTER);

        add(timerPanel, c);
        c.gridy++;

        strategyDropdown = new FancyDropdown<>("Dropping Strategy", DropStrategy.class);
        strategyDropdown.setSelectedItem(config.getDropStrategy());
        strategyDropdown.addSelectionListener(e -> config.setDropStrategy(strategyDropdown.getSelectedItem()));

        add(strategyDropdown, c);
        c.gridy++;

        startStopButton = new FancyButton("Start");
        startStopButton.setFocusable(false);
        startStopButton.addActionListener(e -> {
            toggle(plugin);
        });

        add(startStopButton, c);
        c.gridy++;

        timer = new Timer(100, e -> updateTimerDisplay());

        c.weighty = 1;
        add(new JPanel(), c);
    }

    private void toggle(ExamplePlugin plugin)
    {
        Client client = Static.getClient();
        if(client.getGameState() != GameState.LOGGED_IN && client.getGameState() != GameState.LOADING)
        {
            Logger.warn("Cannot start when not logged in.");
            return;
        }
        if (isRunning)
        {
            isRunning = false;
            timer.stop();
            startStopButton.setText("Start");
            breakHandler.stop(plugin);
        }
        else
        {
            isRunning = true;
            startTime = System.currentTimeMillis();
            timer.start();
            startStopButton.setText("Stop");
            breakHandler.start(plugin);
        }
    }

    private void updateTimerDisplay()
    {
        if (!isRunning)
        {
            return;
        }

        Client client = Static.getClient();
        if(client.getGameState() != GameState.LOGGED_IN && client.getGameState() != GameState.LOADING)
        {
            isRunning = false;
            timer.stop();
            startStopButton.setText("Start");
            return;
        }

        long elapsedMillis = System.currentTimeMillis() - startTime;
        String formattedTime = formatTime(elapsedMillis);

        SwingUtilities.invokeLater(() -> timerLabel.setText(formattedTime));
    }

    private String formatTime(long millis)
    {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds = seconds % 60;
        minutes = minutes % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public boolean isRunning()
    {
        return isRunning;
    }

    public DropStrategy getSelectedStrategy()
    {
        return strategyDropdown.getSelectedItem();
    }

    public void shutdown()
    {
        if (timer != null)
        {
            timer.stop();
        }
    }
}
