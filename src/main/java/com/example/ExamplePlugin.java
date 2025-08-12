package com.example.stopwatcheq;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.WorldChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.config.ConfigManager;
import org.apache.commons.lang3.time.DurationFormatUtils;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

@PluginDescriptor(
    name = "Stopwatch Equation"
)
public class StopwatchEquationPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private StopwatchEquationConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private StopwatchEquationOverlay overlay;

    private long startTime;
    private long elapsedTime;
    private boolean running;

    @Override
    protected void startUp()
    {
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
    }

    public void startStopwatch()
    {
        startTime = System.currentTimeMillis() - elapsedTime;
        running = true;
    }

    public void stopStopwatch()
    {
        elapsedTime = System.currentTimeMillis() - startTime;
        running = false;
    }

    public void resetStopwatch()
    {
        elapsedTime = 0;
        startTime = 0;
        running = false;
    }

    public long getElapsedMillis()
    {
        return running ? (System.currentTimeMillis() - startTime) : elapsedTime;
    }

    public double getEquationResult()
    {
        double seconds = getElapsedMillis() / 1000.0;
        try
        {
            Expression e = new ExpressionBuilder(config.equation())
                    .variables("elapsedSeconds")
                    .build()
                    .setVariable("elapsedSeconds", seconds);
            return e.evaluate();
        }
        catch (Exception ex)
        {
            return 0; // if equation is invalid
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        switch (event.getGameState())
        {
            case LOGIN_SCREEN:
                stopStopwatch();
                break;
        }
    }

    @Subscribe
    public void onWorldChanged(WorldChanged event)
    {
        stopStopwatch();
    }

    @Provides
    StopwatchEquationConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(StopwatchEquationConfig.class);
    }
}
