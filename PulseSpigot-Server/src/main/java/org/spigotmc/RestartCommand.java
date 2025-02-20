package org.spigotmc;

import java.io.File;
import java.util.List;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import xyz.krypton.spigot.config.Config;

public class RestartCommand extends Command
{

    public RestartCommand(String name)
    {
        super( name );
        this.description = "Restarts the server";
        this.usageMessage = "/restart";
        this.setPermission( "bukkit.command.restart" );
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args)
    {
        if ( testPermission( sender ) )
        {
            MinecraftServer.getServer().processQueue.add( new Runnable()
            {
                @Override
                public void run()
                {
                    restart();
                }
            } );
        }
        return true;
    }

    private static File RESTART_SCRIPT;

    public static void restart()
    {
        if (RESTART_SCRIPT == null) {
            RESTART_SCRIPT = new File(Config.get().settings.restartScript);
        }
        restart(RESTART_SCRIPT);
    }

    public static void restart(final File script)
    {
        AsyncCatcher.enabled = false; // Disable async catcher incase it interferes with us
        try
        {
            if ( script.isFile() )
            {
                System.out.println( "Attempting to restart with " + Config.get().settings.restartScript );

                // Disable Watchdog
                WatchdogThread.doStop();

                // Kick all players
                for ( EntityPlayer p : (List< EntityPlayer>) MinecraftServer.getServer().getPlayerList().players )
                {
                    p.playerConnection.disconnect(Config.get().messages.restart);
                }
                // Give the socket a chance to send the packets
                try
                {
                    Thread.sleep( 100 );
                } catch ( InterruptedException ex )
                {
                }
                // Close the socket so we can rebind with the new process
                MinecraftServer.getServer().getServerConnection().b();

                // Give time for it to kick in
                try
                {
                    Thread.sleep( 100 );
                } catch ( InterruptedException ex )
                {
                }

                // Actually shutdown
                try
                {
                    MinecraftServer.getServer().stop();
                } catch ( Throwable t )
                {
                }

                // This will be done AFTER the server has completely halted
                Thread shutdownHook = new Thread()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            String os = System.getProperty( "os.name" ).toLowerCase();
                            if ( os.contains( "win" ) )
                            {
                                Runtime.getRuntime().exec( "cmd /c start " + script.getPath() );
                            } else
                            {
                                Runtime.getRuntime().exec( new String[]
                                {
                                    "sh", script.getPath()
                                } );
                            }
                        } catch ( Exception e )
                        {
                            e.printStackTrace();
                        }
                    }
                };

                shutdownHook.setDaemon( true );
                Runtime.getRuntime().addShutdownHook( shutdownHook );
            } else
            {
                System.out.println( "Startup script '" + Config.get().settings.restartScript + "' does not exist! Stopping server." );
            }
            System.exit( 0 );
        } catch ( Exception ex )
        {
            ex.printStackTrace();
        }
    }
}
