package net.minecraft.server;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import javax.imageio.ImageIO;

import me.nate.spigot.config.PulseConfig;
import org.bukkit.event.server.ServerLoadEvent;
import me.nate.spigot.async.AsyncPriority;
import me.nate.spigot.chunk.AsyncChunkProvider;
import me.nate.spigot.config.Config;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// CraftBukkit start

import joptsimple.OptionSet;

import co.aikar.timings.SpigotTimings; // Spigot

// CraftBukkit end

public abstract class MinecraftServer implements Runnable, ICommandListener, IAsyncTaskHandler, IMojangStatistics { // PulseSpigot

    public static final Logger LOGGER = LogManager.getLogger();
    public static final File a = new File("usercache.json");
    private static MinecraftServer l;
    public Convertable convertable;
    private final MojangStatisticsGenerator n = new MojangStatisticsGenerator("server", this, az());
    public File universe;
    private final List<IUpdatePlayerListBox> p = Lists.newArrayList();
    protected final ICommandHandler b;
    public final MethodProfiler methodProfiler = new MethodProfiler();
    private ServerConnection q; // Spigot
    public final ServerPing r = new ServerPing(); // PulseSpigot - make public
    private final Random s = new Random();
    private String serverIp;
    private int u = -1;
    public WorldServer[] worldServer;
    private PlayerList v;
    private boolean isRunning = true;
    private boolean isStopped;
    private int ticks;
    protected final Proxy e;
    public String f;
    public int g;
    private boolean onlineMode;
    private boolean spawnAnimals;
    private boolean spawnNPCs;
    private boolean pvpMode;
    private boolean allowFlight;
    private String motd;
    private int F;
    private int G = 0;
    public final long[] h = new long[100];
    public long[][] i;
    private KeyPair H;
    private String I;
    private String J;
    private boolean demoMode;
    private boolean M;
    private boolean N;
    private String O = "";
    private String P = "";
    private boolean Q;
    private long R;
    private String S;
    private boolean T;
    private boolean U;
    private final YggdrasilAuthenticationService V;
    private final MinecraftSessionService W;
    private long X = 0L;
    private final GameProfileRepository Y;
    private final UserCache Z;
    protected final Queue<FutureTask<?>> j = new com.destroystokyo.paper.util.CachedSizeConcurrentLinkedQueue<>(); // Spigot, PAIL: Rename // Paper - Make size() constant-time
    private Thread serverThread;
    private long ab = az();

    // CraftBukkit start
    public List<WorldServer> worlds = new ArrayList<WorldServer>();
    public org.bukkit.craftbukkit.CraftServer server;
    public OptionSet options;
    public org.bukkit.command.ConsoleCommandSender console;
    public org.bukkit.command.RemoteConsoleCommandSender remoteConsole;
    //public ConsoleReader reader; // Paper
    public static int currentTick = 0; // PaperSpigot - Further improve tick loop
    public final Thread primaryThread;
    public java.util.Queue<Runnable> processQueue = new java.util.concurrent.ConcurrentLinkedQueue<Runnable>();
    public java.util.Queue<Runnable> priorityProcessQueue = new java.util.concurrent.ConcurrentLinkedQueue<Runnable>(); // PulseSpigot
    public int autosavePeriod;
    // CraftBukkit end

    public MinecraftServer(OptionSet options, Proxy proxy, File file1) {
        io.netty.util.ResourceLeakDetector.setEnabled( false ); // Spigot - disable
        this.e = proxy;
        MinecraftServer.l = this;
        // this.universe = file; // CraftBukkit
        // this.q = new ServerConnection(this); // Spigot
        this.Z = new UserCache(this, file1);
        this.b = this.h();
        // this.convertable = new WorldLoaderServer(file); // CraftBukkit - moved to DedicatedServer.init
        this.V = new YggdrasilAuthenticationService(proxy, UUID.randomUUID().toString());
        this.W = this.V.createMinecraftSessionService();
        this.Y = this.V.createProfileRepository();
        // CraftBukkit start
        this.options = options;
        // Paper start - Handled by TerminalConsoleAppender
        /*// Try to see if we're actually running in a terminal, disable jline if not
        if (System.console() == null && System.getProperty("jline.terminal") == null) {
            System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
            Main.useJline = false;
        }

        try {
            reader = new ConsoleReader(System.in, System.out);
            reader.setExpandEvents(false); // Avoid parsing exceptions for uncommonly used event designators
        } catch (Throwable e) {
            try {
                // Try again with jline disabled for Windows users without C++ 2008 Redistributable
                System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
                System.setProperty("user.language", "en");
                Main.useJline = false;
                reader = new ConsoleReader(System.in, System.out);
                reader.setExpandEvents(false);
            } catch (IOException ex) {
                LOGGER.warn((String) null, ex);
            }
        }*/
        // Paper end
        Runtime.getRuntime().addShutdownHook(new org.bukkit.craftbukkit.util.ServerShutdownThread(this));

        this.serverThread = primaryThread = new Thread(this, "Server thread"); // Moved from main // PulseSpigot - we assign above
    }

    public abstract PropertyManager getPropertyManager();
    // CraftBukkit end

    protected CommandDispatcher h() {
        return new CommandDispatcher();
    }

    protected abstract boolean init() throws IOException;

    protected void a(String s) {
        if (this.getConvertable().isConvertable(s)) {
            MinecraftServer.LOGGER.info("Converting map!");
            this.b("menu.convertingLevel");
            this.getConvertable().convert(s, new IProgressUpdate() {
                private long b = System.currentTimeMillis();

                public void a(String s) {}

                public void a(int i) {
                    if (System.currentTimeMillis() - this.b >= 1000L) {
                        this.b = System.currentTimeMillis();
                        MinecraftServer.LOGGER.info("Converting... " + i + "%");
                    }

                }

                public void c(String s) {}
            });
        }

    }

    protected synchronized void b(String s) {
        this.S = s;
    }

    protected void a(String s, String s1, long i, WorldType worldtype, String s2) {
        this.a(s);
        this.b("menu.loadingLevel");
        this.worldServer = new WorldServer[3];
        /* CraftBukkit start - Remove ticktime arrays and worldsettings
        this.i = new long[this.worldServer.length][100];
        IDataManager idatamanager = this.convertable.a(s, true);

        this.a(this.U(), idatamanager);
        WorldData worlddata = idatamanager.getWorldData();
        WorldSettings worldsettings;

        if (worlddata == null) {
            if (this.X()) {
                worldsettings = DemoWorldServer.a;
            } else {
                worldsettings = new WorldSettings(i, this.getGamemode(), this.getGenerateStructures(), this.isHardcore(), worldtype);
                worldsettings.setGeneratorSettings(s2);
                if (this.M) {
                    worldsettings.a();
                }
            }

            worlddata = new WorldData(worldsettings, s1);
        } else {
            worlddata.a(s1);
            worldsettings = new WorldSettings(worlddata);
        }
        */
        int worldCount = 3;

        for (int j = 0; j < worldCount; ++j) {
            WorldServer world;
            byte dimension = 0;

            if (j == 1) {
                if (getAllowNether()) {
                    dimension = -1;
                } else {
                    continue;
                }
            }

            if (j == 2) {
                if (server.getAllowEnd()) {
                    dimension = 1;
                } else {
                    continue;
                }
            }

            String worldType = org.bukkit.World.Environment.getEnvironment(dimension).toString().toLowerCase();
            String name = (dimension == 0) ? s : s + "_" + worldType;

            org.bukkit.generator.ChunkGenerator gen = this.server.getGenerator(name);
            WorldSettings worldsettings = new WorldSettings(i, this.getGamemode(), this.getGenerateStructures(), this.isHardcore(), worldtype);
            worldsettings.setGeneratorSettings(s2);

            if (j == 0) {
                IDataManager idatamanager = new ServerNBTManager(server.getWorldContainer(), s1, true);
                WorldData worlddata = idatamanager.getWorldData();
                if (worlddata == null) {
                    worlddata = new WorldData(worldsettings, s1);
                }
                worlddata.checkName(s1); // CraftBukkit - Migration did not rewrite the level.dat; This forces 1.8 to take the last loaded world as respawn (in this case the end)
                if (this.X()) {
                    world = (WorldServer) (new DemoWorldServer(this, idatamanager, worlddata, dimension, this.methodProfiler, org.bukkit.World.Environment.getEnvironment(dimension), gen)).b();
                } else {
                    world = (WorldServer) (new WorldServer(this, idatamanager, worlddata, dimension, this.methodProfiler, org.bukkit.World.Environment.getEnvironment(dimension), gen)).b();
                }

                world.a(worldsettings);
                this.server.scoreboardManager = new org.bukkit.craftbukkit.scoreboard.CraftScoreboardManager(this, world.getScoreboard());
            } else {
                String dim = "DIM" + dimension;

                File newWorld = new File(new File(name), dim);
                File oldWorld = new File(new File(s), dim);

                if ((!newWorld.isDirectory()) && (oldWorld.isDirectory())) {
                    MinecraftServer.LOGGER.info("---- Migration of old " + worldType + " folder required ----");
                    MinecraftServer.LOGGER.info("Unfortunately due to the way that Minecraft implemented multiworld support in 1.6, Bukkit requires that you move your " + worldType + " folder to a new location in order to operate correctly.");
                    MinecraftServer.LOGGER.info("We will move this folder for you, but it will mean that you need to move it back should you wish to stop using Bukkit in the future.");
                    MinecraftServer.LOGGER.info("Attempting to move " + oldWorld + " to " + newWorld + "...");

                    if (newWorld.exists()) {
                        MinecraftServer.LOGGER.warn("A file or folder already exists at " + newWorld + "!");
                        MinecraftServer.LOGGER.info("---- Migration of old " + worldType + " folder failed ----");
                    } else if (newWorld.getParentFile().mkdirs()) {
                        if (oldWorld.renameTo(newWorld)) {
                            MinecraftServer.LOGGER.info("Success! To restore " + worldType + " in the future, simply move " + newWorld + " to " + oldWorld);
                            // Migrate world data too.
                            try {
                                com.google.common.io.Files.copy(new File(new File(s), "level.dat"), new File(new File(name), "level.dat"));
                            } catch (IOException exception) {
                                MinecraftServer.LOGGER.warn("Unable to migrate world data.");
                            }
                            MinecraftServer.LOGGER.info("---- Migration of old " + worldType + " folder complete ----");
                        } else {
                            MinecraftServer.LOGGER.warn("Could not move folder " + oldWorld + " to " + newWorld + "!");
                            MinecraftServer.LOGGER.info("---- Migration of old " + worldType + " folder failed ----");
                        }
                    } else {
                        MinecraftServer.LOGGER.warn("Could not create path for " + newWorld + "!");
                        MinecraftServer.LOGGER.info("---- Migration of old " + worldType + " folder failed ----");
                    }
                }

                IDataManager idatamanager = new ServerNBTManager(server.getWorldContainer(), name, true);
                // world =, b0 to dimension, s1 to name, added Environment and gen
                WorldData worlddata = idatamanager.getWorldData();
                if (worlddata == null) {
                    worlddata = new WorldData(worldsettings, name);
                }
                worlddata.checkName(name); // CraftBukkit - Migration did not rewrite the level.dat; This forces 1.8 to take the last loaded world as respawn (in this case the end)
                world = (WorldServer) new SecondaryWorldServer(this, idatamanager, dimension, this.worlds.get(0), this.methodProfiler, worlddata, org.bukkit.World.Environment.getEnvironment(dimension), gen).b();
            }

            this.server.getPluginManager().callEvent(new org.bukkit.event.world.WorldInitEvent(world.getWorld()));

            world.addIWorldAccess(new WorldManager(this, world));
            if (!this.T()) {
                world.getWorldData().setGameType(this.getGamemode());
            }

            worlds.add(world);
            getPlayerList().setPlayerFileData(worlds.toArray(new WorldServer[worlds.size()]));
        }

        // CraftBukkit end
        this.a(this.getDifficulty());
        this.k();
    }

    protected void k() {
        boolean flag = true;
        boolean flag1 = true;
        boolean flag2 = true;
        boolean flag3 = true;
        int i = 0;

        this.b("menu.generatingTerrain");
        byte b0 = 0;

        // CraftBukkit start - fire WorldLoadEvent and handle whether or not to keep the spawn in memory
        for (int m = 0; m < worlds.size(); m++) {
            WorldServer worldserver = this.worlds.get(m);
            LOGGER.info("Preparing start region for level " + m + " (Seed: " + worldserver.getSeed() + ")");

            if (!worldserver.getWorld().getKeepSpawnInMemory()) {
                continue;
            }

            BlockPosition blockposition = worldserver.getSpawn();
            long j = az();
            i = 0;

            for (int k = -192; k <= 192 && this.isRunning(); k += 16) {
                for (int l = -192; l <= 192 && this.isRunning(); l += 16) {
                    long i1 = az();

                    if (i1 - j > 1000L) {
                        this.a_("Preparing spawn area", i * 100 / 625);
                        j = i1;
                    }

                    ++i;
                    worldserver.chunkProviderServer.getChunkAt(blockposition.getX() + k >> 4, blockposition.getZ() + l >> 4, true, true, AsyncPriority.URGENT, null); // PulseSpigot - use new methods
                }
            }
        }

        for (WorldServer world : this.worlds) {
            this.server.getPluginManager().callEvent(new org.bukkit.event.world.WorldLoadEvent(world.getWorld()));
        }
        // CraftBukkit end
        this.s();
        this.server.getPluginManager().callEvent(new ServerLoadEvent(ServerLoadEvent.LoadType.STARTUP)); // PulseSpigot
    }

    protected void a(String s, IDataManager idatamanager) {
        File file = new File(idatamanager.getDirectory(), "resources.zip");

        if (file.isFile()) {
            this.setResourcePack("level://" + s + "/" + file.getName(), "");
        }

    }

    public abstract boolean getGenerateStructures();

    public abstract WorldSettings.EnumGamemode getGamemode();

    public abstract EnumDifficulty getDifficulty();

    public abstract boolean isHardcore();

    public abstract int p();

    public abstract boolean q();

    public abstract boolean r();

    protected void a_(String s, int i) {
        this.f = s;
        this.g = i;
        MinecraftServer.LOGGER.info(s + ": " + i + "%");
    }

    protected void s() {
        this.f = null;
        this.g = 0;

        this.server.enablePlugins(org.bukkit.plugin.PluginLoadOrder.POSTWORLD); // CraftBukkit
    }

    protected void saveChunks(boolean flag) throws ExceptionWorldConflict { // CraftBukkit - added throws
        if (!this.N) {
            WorldServer[] aworldserver = this.worldServer;
            int i = aworldserver.length;

            // CraftBukkit start
            for (int j = 0; j < worlds.size(); ++j) {
                WorldServer worldserver = worlds.get(j);
                // CraftBukkit end

                if (worldserver != null) {
                    if (!flag) {
                        MinecraftServer.LOGGER.info("Saving chunks for level \'" + worldserver.getWorldData().getName() + "\'/" + worldserver.worldProvider.getName());
                    }

                    try {
                        worldserver.save(true, (IProgressUpdate) null);
                        worldserver.saveLevel(); // CraftBukkit
                    } catch (ExceptionWorldConflict exceptionworldconflict) {
                        MinecraftServer.LOGGER.warn(exceptionworldconflict.getMessage());
                    }
                }
            }

        }
    }

    // CraftBukkit start
    private boolean hasStopped = false;
    private final Object stopLock = new Object();
    // CraftBukkit end

    public void stop() throws ExceptionWorldConflict { // CraftBukkit - added throws
        // CraftBukkit start - prevent double stopping on multiple threads
        synchronized(stopLock) {
            if (hasStopped) return;
            new org.bukkit.event.server.ServerShutdownEvent(server).callEvent(); // PulseSpigot
            hasStopped = true;
        }
        AsyncChunkProvider.stop(this); // Paper
        // CraftBukkit end
        if (!this.N) {
            MinecraftServer.LOGGER.info("Stopping server");
            SpigotTimings.stopServer(); // Spigot

            // CraftBukkit start
            if (this.server != null) {
                this.server.disablePlugins();
            }
            // CraftBukkit end
            if (this.aq() != null) {
                this.aq().b();
            }

            if (this.v != null) {
                MinecraftServer.LOGGER.info("Saving players");
                this.v.savePlayers();
                this.v.u();
                try { Thread.sleep(100); } catch (InterruptedException ex) {} // CraftBukkit - SPIGOT-625 - give server at least a chance to send packets
            }

            if (this.worldServer != null) {
                MinecraftServer.LOGGER.info("Saving worlds");
                this.saveChunks(false);

                /* CraftBukkit start - Handled in saveChunks
                for (int i = 0; i < this.worldServer.length; ++i) {
                    WorldServer worldserver = this.worldServer[i];

                    worldserver.saveLevel();
                }
                // CraftBukkit end */
            }

            if (this.n.d()) {
                this.n.e();
            }
            // Spigot start
            if( Config.get().settings.saveUserCacheOnStopOnly )
            {
                LOGGER.info("Saving usercache.json");
                this.Z.c();
            }
            //Spigot end
        }
    }

    public String getServerIp() {
        return this.serverIp;
    }

    public void c(String s) {
        this.serverIp = s;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void safeShutdown() {
        this.isRunning = false;
    }

    // PaperSpigot start - Further improve tick loop
    // PulseSpigot start - New Tick Loop System
    private long lastTick = 0;
    private long catchupTime = 0;
    // PulseSpigot end
    private static final int TPS = 20;
    private static final long SEC_IN_NANO = 1000000000;
    private static final long TICK_TIME = SEC_IN_NANO / TPS;
    private static final long MAX_CATCHUP_BUFFER = TICK_TIME * TPS * 60L;
    private static final int SAMPLE_INTERVAL = 20;
    public final RollingAverage tps1 = new RollingAverage(60);
    public final RollingAverage tps5 = new RollingAverage(60 * 5);
    public final RollingAverage tps15 = new RollingAverage(60 * 15);
    public double[] recentTps = new double[ 3 ]; // PaperSpigot - Fine have your darn compat with bad plugins

    public static class RollingAverage {
        private final int size;
        private long time;
        private java.math.BigDecimal total;
        private int index = 0;
        private final java.math.BigDecimal[] samples;
        private final long[] times;

        RollingAverage(int size) {
            this.size = size;
            this.time = size * SEC_IN_NANO;
            this.total = dec(TPS).multiply(dec(SEC_IN_NANO)).multiply(dec(size));
            this.samples = new java.math.BigDecimal[size];
            this.times = new long[size];
            for (int i = 0; i < size; i++) {
                this.samples[i] = dec(TPS);
                this.times[i] = SEC_IN_NANO;
            }
        }

        private static java.math.BigDecimal dec(long t) {
            return new java.math.BigDecimal(t);
        }
        public void add(java.math.BigDecimal x, long t) {
            time -= times[index];
            total = total.subtract(samples[index].multiply(dec(times[index])));
            samples[index] = x;
            times[index] = t;
            time += t;
            total = total.add(x.multiply(dec(t)));
            if (++index == size) {
                index = 0;
            }
        }

        public double getAverage() {
            return total.divide(dec(time), 30, java.math.RoundingMode.HALF_UP).doubleValue();
        }
    }
    private static final java.math.BigDecimal TPS_BASE = new java.math.BigDecimal(1E9).multiply(new java.math.BigDecimal(SAMPLE_INTERVAL));

    // PulseSpigot start
    private boolean canSleepForTick() {
        return az() < this.ab;
    }
    // PulseSpigot end
    // PaperSpigot End

    public void run() {
        try {
            if (this.init()) {
                this.ab = az();

                this.r.setMOTD(new ChatComponentText(this.motd));
                this.r.setServerInfo(new ServerPing.ServerData("1.8.8", 47));
                this.a(this.r);

                // Spigot start
                // PaperSpigot start - Further improve tick loop
                Arrays.fill( recentTps, 20 );
                //long lastTick = System.nanoTime(), catchupTime = 0, curTime, wait, tickSection = lastTick;
                // PulseSpigot start - Modern tick loop
                long start = System.nanoTime(), curTime, tickSection = start;
                lastTick = start - TICK_TIME;
                // PulseSpigot end
                // PaperSpigot end
                while (this.isRunning) {
                    // PulseSpigot start - New Tick Loop System
                    long i = ((curTime = System.nanoTime()) / (1000L * 1000L)) - this.ab; // Spigot // Paper
                    if (i > 5000L && this.ab - this.R >= 30000L) { // CraftBukkit
                        long j = i / 50L;
                        if (server.getWarnOnOverload()) { // CraftBukkit
                            LOGGER.info("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", i, j);
                        }
                        this.ab += j * 50L;
                        this.R = this.ab;
                    }

                    /*curTime = System.nanoTime();
                    // PaperSpigot start - Further improve tick loop
                    wait = TICK_TIME - (curTime - lastTick);
                    if (wait > 0) {
                        if (catchupTime < 2E6) {
                            wait += Math.abs(catchupTime);
                            // Nate start - fix the tickloop bug
                        } else if (wait < catchupTime) {
                            catchupTime -= wait;
                            wait = 0;
                        } else {
                            wait -= catchupTime;
                            catchupTime = 0;
                            // Nate end
                        }
                    }
                    curTime = System.nanoTime();
                    wait = TICK_TIME - (curTime - lastTick);

                    catchupTime = Math.min(MAX_CATCHUP_BUFFER, catchupTime - wait);*/
                    if ( ++MinecraftServer.currentTick % SAMPLE_INTERVAL == 0 )
                    {
                        final long diff = curTime - tickSection;
                        java.math.BigDecimal currentTps = TPS_BASE.divide(new java.math.BigDecimal(diff), 30, java.math.RoundingMode.HALF_UP); // PaperSpigot
                        tps1.add(currentTps, diff);
                        tps5.add(currentTps, diff);
                        tps15.add(currentTps, diff);
                        // Backwards compat with bad plugins
                        recentTps[0] = tps1.getAverage();
                        recentTps[1] = tps5.getAverage();
                        recentTps[2] = tps15.getAverage();
                        tickSection = curTime;
                        // PaperSpigot end
                    }
                    lastTick = curTime;

                    // Nate start
                    this.A();
                    this.ab += 50L;
                    while(this.canSleepForTick()) {
                        java.util.concurrent.locks.LockSupport.parkNanos(1000000);
                    }
                    // Nate end
                }
                // Spigot end
            } else {
                this.a((CrashReport) null);
            }
        } catch (Throwable throwable) {
            MinecraftServer.LOGGER.error("Encountered an unexpected exception", throwable);
            // Spigot Start
            if ( throwable.getCause() != null )
            {
                MinecraftServer.LOGGER.error( "\tCause of unexpected exception was", throwable.getCause() );
            }
            // Spigot End
            CrashReport crashreport = null;

            if (throwable instanceof ReportedException) {
                crashreport = this.b(((ReportedException) throwable).a());
            } else {
                crashreport = this.b(new CrashReport("Exception in server tick loop", throwable));
            }

            File file = new File(new File(this.y(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");

            if (crashreport.a(file)) {
                MinecraftServer.LOGGER.error("This crash report has been saved to: " + file.getAbsolutePath());
            } else {
                MinecraftServer.LOGGER.error("We were unable to save this crash report to disk.");
            }

            this.a(crashreport);
        } finally {
            try {
                org.spigotmc.WatchdogThread.doStop();
                this.isStopped = true;
                this.stop();
            } catch (Throwable throwable1) {
                MinecraftServer.LOGGER.error("Exception stopping the server", throwable1);
            } finally {
                // CraftBukkit start - Restore terminal to original settings
                try {
                    net.minecrell.terminalconsole.TerminalConsoleAppender.close(); // Paper - Use TerminalConsoleAppender
                } catch (Exception ignored) {
                }
                // CraftBukkit end
                this.z();
            }

        }

    }

    private void a(ServerPing serverping) {
        File file = this.d("server-icon.png");

        if (file.isFile()) {
            ByteBuf bytebuf = Unpooled.buffer();
            ByteBuf bytebuf1 = null; // Paper - cleanup favicon bytebuf

            try {
                BufferedImage bufferedimage = ImageIO.read(file);

                Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide", new Object[0]);
                Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high", new Object[0]);
                ImageIO.write(bufferedimage, "PNG", new ByteBufOutputStream(bytebuf));
                /*ByteBuf */ bytebuf1 = Base64.encode(bytebuf); // Paper - cleanup favicon bytebuf

                serverping.setFavicon("data:image/png;base64," + bytebuf1.toString(Charsets.UTF_8));
            } catch (Exception exception) {
                MinecraftServer.LOGGER.error("Couldn\'t load server icon", exception);
            } finally {
                bytebuf.release();
                if (bytebuf1 != null) bytebuf1.release(); // Paper - cleanup favicon bytebuf
            }
        }

    }

    public File y() {
        return new File(".");
    }

    protected void a(CrashReport crashreport) {}

    protected void z() {}

    // PulseSpigot start - Modern tick loop
    protected void A() throws ExceptionWorldConflict { // CraftBukkit - added throws
        co.aikar.timings.TimingsManager.FULL_SERVER_TICK.startTiming(); // Spigot
        long i = System.nanoTime();
        this.server.getPluginManager().callEvent(new ServerTickStartEvent(this.ticks + 1));

        ++this.ticks;
        if (this.T) {
            this.T = false;
            this.methodProfiler.a = true;
        }

        this.B();
        if (i - this.X >= 5000000000L) {
            this.X = i;
            this.r.setPlayerSample(new ServerPing.ServerPingPlayerSample(this.J(), this.I()));
            GameProfile[] agameprofile = new GameProfile[Math.min(this.I(), 12)];
            int j = MathHelper.nextInt(this.s, 0, this.I() - agameprofile.length);

            for (int k = 0; k < agameprofile.length; ++k) {
                agameprofile[k] = ((EntityPlayer) this.v.v().get(j + k)).getProfile();
            }

            Collections.shuffle(Arrays.asList(agameprofile));
            this.r.b().a(agameprofile);
        }

        if (autosavePeriod > 0 && this.ticks % autosavePeriod == 0) { // CraftBukkit
            this.v.savePlayers();
            // Spigot Start
            // We replace this with saving each individual world as this.saveChunks(...) is broken,
            // and causes the main thread to sleep for random amounts of time depending on chunk activity
            // Also pass flag to only save modified chunks
            server.playerCommandState = true;
            for (World world : worlds) {
                world.getWorld().save(false);
            }
            server.playerCommandState = false;
            // this.saveChunks(true);
            // Spigot End
        }

        // PulseSpigot start - Modern tick loop
        long endTime = System.nanoTime();
        long remaining = (TICK_TIME - (endTime - lastTick)) - catchupTime;
        this.server.getPluginManager().callEvent(new com.destroystokyo.paper.event.server.ServerTickEndEvent(this.ticks, ((double)(endTime - lastTick) / 1000000D), remaining));
        // PulseSpigot end
        this.h[this.ticks % 100] = System.nanoTime() - i;
        if (getSnooperEnabled() && !this.n.d() && this.ticks > 100) {  // Spigot
            this.n.a();
        }

        if (getSnooperEnabled() && this.ticks % 6000 == 0) { // Spigot
            this.n.b();
        }

        org.spigotmc.WatchdogThread.tick(); // Spigot
    }

    public void B() {
        Queue queue = this.j;

        // Spigot start
        FutureTask<?> entry;
        int count = this.j.size();
        while (count-- > 0 && (entry = this.j.poll()) != null) {
            SystemUtils.a(entry, MinecraftServer.LOGGER);
         }
        // Spigot end

        AsyncChunkProvider.processMainThreadQueue(this); // Paper

        // CraftBukkit start
        this.server.getScheduler().mainThreadHeartbeat(this.ticks);
        // Run tasks that are waiting on processing
        while (!processQueue.isEmpty()) {
            processQueue.remove().run();
        }

        org.bukkit.craftbukkit.chunkio.ChunkIOExecutor.tick(); // PulseSpigot

        // Send time updates to everyone, it will get the right time from the world the player is in.
        // Paper start - optimize time updates
        int i;

        if ((this.ticks % this.server.pulseConfig.optimizations.timeUpdateFrequency) == 0) // PulseSpigot
        {
            for (i = 0; i < this.worlds.size(); ++i) {
                WorldServer world = this.worlds.get(i);

                final boolean doDaylight = world.getGameRules().getBoolean("doDaylightCycle");
                final long dayTime = world.getDayTime();
                long worldTime = world.getTime();
                final PacketPlayOutUpdateTime worldPacket = new PacketPlayOutUpdateTime(worldTime, dayTime, doDaylight);
                for (EntityHuman entityhuman : world.players) {
                    if (!(entityhuman instanceof EntityPlayer)) {//|| (ticks + entityhuman.getId()) % 20 != 0
                        continue;
                    }

                    if (entityhuman.world == world)
                    {
                        EntityPlayer entityplayer = (EntityPlayer) entityhuman;
                        long playerTime = entityplayer.getPlayerTime();
                        PacketPlayOutUpdateTime packet = (playerTime == dayTime) ? worldPacket : new PacketPlayOutUpdateTime(worldTime, playerTime, doDaylight);
                        entityplayer.playerConnection.sendPacket(packet); // Add support for per player time
                    }
                }
            }
        }
        // Paper end

        for (i = 0; i < this.worlds.size(); ++i) {
            long j = System.nanoTime();

            // if (i == 0 || this.getAllowNether()) {
                WorldServer worldserver = this.worlds.get(i);
                AsyncChunkProvider.processMainThreadQueue(worldserver); // Paper
                worldserver.hasPhysicsEvent = org.bukkit.event.block.BlockPhysicsEvent.getHandlerList().getRegisteredListeners().length > 0; // Paper
                worldserver.hasEntityMoveEvent = io.papermc.paper.event.entity.EntityMoveEvent.getHandlerList().getRegisteredListeners().length > 0; // Paper

                /* Drop global time updates
                if (this.ticks % 20 == 0) {
                    this.v.a(new PacketPlayOutUpdateTime(worldserver.getTime(), worldserver.getDayTime(), worldserver.getGameRules().getBoolean("doDaylightCycle")), worldserver.worldProvider.getDimension());
                }
                // CraftBukkit end */

                CrashReport crashreport;

                try {
                    worldserver.doTick();
                } catch (Throwable throwable) {
                    // Spigot Start
                    try {
                    crashreport = CrashReport.a(throwable, "Exception ticking world");
                    } catch (Throwable t){
                        throw new RuntimeException("Error generating crash report", t);
                    }
                    // Spigot End
                    worldserver.a(crashreport);
                    throw new ReportedException(crashreport);
                }

                try {
                    worldserver.tickEntities();
                } catch (Throwable throwable1) {
                    // Spigot Start
                    try {
                    crashreport = CrashReport.a(throwable1, "Exception ticking world entities");
                    } catch (Throwable t){
                        throw new RuntimeException("Error generating crash report", t);
                    }
                    // Spigot End
                    worldserver.a(crashreport);
                    throw new ReportedException(crashreport);
                }

                // Tuinity start - controlled flush for entity tracker packets
                java.util.Set<NetworkManager> disabledFlushes = new java.util.HashSet<>(worldserver.players.size()); // InsanePaper - List -> Set
                for (EntityHuman player : worldserver.players) {
                    if (!(player instanceof EntityPlayer)) continue; // skip non-player entities
                    PlayerConnection connection = ((EntityPlayer) player).playerConnection;
                    if (connection != null) {
                        connection.networkManager.disableAutomaticFlush();
                        disabledFlushes.add(connection.networkManager);
                    }
                }
                try {
                    // Tuinity end
                    worldserver.getTracker().updatePlayers();
                    // Tuinity start - controlled flush for entity tracker packets
                } finally {
                    for (NetworkManager networkManager : disabledFlushes) {
                        networkManager.enableAutomaticFlush();
                    }
                }
                // Tuinity end
                worldserver.explosionDensityCache.clear(); // PaperSpigot - Optimize explosions
                // PulseSpigot start
                if (PulseConfig.get().optimizations.optimizedMovementCacheFlushing) {
                  worldserver.movementCache.clear();
                }
                // PulseSpigot end
            // } // CraftBukkit

            // this.i[i][this.ticks % 100] = System.nanoTime() - j; // CraftBukkit
        }

        // PulseSpigot start
        while (!priorityProcessQueue.isEmpty()) {
            priorityProcessQueue.remove().run();
        }
        // PulseSpigot end

        this.aq().c();
        this.v.tick();

        for (i = 0; i < this.p.size(); ++i) {
            ((IUpdatePlayerListBox) this.p.get(i)).c();
        }
    }

    public boolean getAllowNether() {
        return true;
    }

    public void a(IUpdatePlayerListBox iupdateplayerlistbox) {
        this.p.add(iupdateplayerlistbox);
    }

    public static void main(final OptionSet options) { // CraftBukkit - replaces main(String[] astring)
        DispenserRegistry.c();

        try {
            /* CraftBukkit start - Replace everything
            boolean flag = true;
            String s = null;
            String s1 = ".";
            String s2 = null;
            boolean flag1 = false;
            boolean flag2 = false;
            int i = -1;

            for (int j = 0; j < astring.length; ++j) {
                String s3 = astring[j];
                String s4 = j == astring.length - 1 ? null : astring[j + 1];
                boolean flag3 = false;

                if (!s3.equals("nogui") && !s3.equals("--nogui")) {
                    if (s3.equals("--port") && s4 != null) {
                        flag3 = true;

                        try {
                            i = Integer.parseInt(s4);
                        } catch (NumberFormatException numberformatexception) {
                            ;
                        }
                    } else if (s3.equals("--singleplayer") && s4 != null) {
                        flag3 = true;
                        s = s4;
                    } else if (s3.equals("--universe") && s4 != null) {
                        flag3 = true;
                        s1 = s4;
                    } else if (s3.equals("--world") && s4 != null) {
                        flag3 = true;
                        s2 = s4;
                    } else if (s3.equals("--demo")) {
                        flag1 = true;
                    } else if (s3.equals("--bonusChest")) {
                        flag2 = true;
                    }
                } else {
                    flag = false;
                }

                if (flag3) {
                    ++j;
                }
            }

            final DedicatedServer dedicatedserver = new DedicatedServer(new File(s1));

            if (s != null) {
                dedicatedserver.i(s);
            }

            if (s2 != null) {
                dedicatedserver.setWorld(s2);
            }

            if (i >= 0) {
                dedicatedserver.setPort(i);
            }

            if (flag1) {
                dedicatedserver.b(true);
            }

            if (flag2) {
                dedicatedserver.c(true);
            }

            if (flag && !GraphicsEnvironment.isHeadless()) {
                dedicatedserver.aQ();
            }

            dedicatedserver.D();
            Runtime.getRuntime().addShutdownHook(new Thread("Server Shutdown Thread") {
                public void run() {
                    dedicatedserver.stop();
                }
            });
            */

            DedicatedServer dedicatedserver = new DedicatedServer(options);

            if (options.has("port")) {
                int port = (Integer) options.valueOf("port");
                if (port > 0) {
                    dedicatedserver.setPort(port);
                }
            }

            if (options.has("universe")) {
                dedicatedserver.universe = (File) options.valueOf("universe");
            }

            if (options.has("world")) {
                dedicatedserver.setWorld((String) options.valueOf("world"));
            }

            dedicatedserver.primaryThread.start();
            // CraftBukkit end
        } catch (Exception exception) {
            MinecraftServer.LOGGER.fatal("Failed to start the minecraft server", exception);
        }

    }

    public void C() {
        /* CraftBukkit start - prevent abuse
        this.serverThread = new Thread(this, "Server thread");
        this.serverThread.start();
        // CraftBukkit end */
    }

    public File d(String s) {
        return new File(this.y(), s);
    }

    public void info(String s) {
        MinecraftServer.LOGGER.info(s);
    }

    public void warning(String s) {
        MinecraftServer.LOGGER.warn(s);
    }

    public WorldServer getWorldServer(int i) {
        // CraftBukkit start
        for (WorldServer world : worlds) {
            if (world.dimension == i) {
                return world;
            }
        }
        return worlds.get(0);
        // CraftBukkit end
    }

    public String E() {
        return this.serverIp;
    }

    public int F() {
        return this.u;
    }

    public String G() {
        return this.motd;
    }

    public String getVersion() {
        return "1.8.8";
    }

    public int I() {
        return this.v.getPlayerCount();
    }

    public int J() {
        return this.v.getMaxPlayers();
    }

    public String[] getPlayers() {
        return this.v.f();
    }

    public GameProfile[] L() {
        return this.v.g();
    }

    public boolean isDebugging() {
        return this.getPropertyManager().getBoolean("debug", false); // CraftBukkit - don't hardcode
    }

    public void g(String s) {
        MinecraftServer.LOGGER.error(s);
    }

    public void h(String s) {
        if (this.isDebugging()) {
            MinecraftServer.LOGGER.info(s);
        }

    }

    public String getServerModName() {
        return "PulseSpigot"; // PulseSpigot - PulseSpigot > // PaperSpigot - PaperSpigot > // Spigot - Spigot > // CraftBukkit - cb > vanilla!
    }

    public CrashReport b(CrashReport crashreport) {
        crashreport.g().a("Profiler Position", new Callable() {
            public String a() throws Exception {
                return "N/A (disabled)"; // PulseSpigot
            }

            public Object call() throws Exception {
                return this.a();
            }
        });
        if (this.v != null) {
            crashreport.g().a("Player Count", new Callable() {
                public String a() {
                    return MinecraftServer.this.v.getPlayerCount() + " / " + MinecraftServer.this.v.getMaxPlayers() + "; " + MinecraftServer.this.v.v();
                }

                public Object call() throws Exception {
                    return this.a();
                }
            });
        }

        return crashreport;
    }

    public List<String> tabCompleteCommand(ICommandListener icommandlistener, String s, BlockPosition blockposition) {
        /* CraftBukkit start - Allow tab-completion of Bukkit commands
        ArrayList arraylist = Lists.newArrayList();

        if (s.startsWith("/")) {
            s = s.substring(1);
            boolean flag = !s.contains(" ");
            List list = this.b.a(icommandlistener, s, blockposition);

            if (list != null) {
                Iterator iterator = list.iterator();

                while (iterator.hasNext()) {
                    String s1 = (String) iterator.next();

                    if (flag) {
                        arraylist.add("/" + s1);
                    } else {
                        arraylist.add(s1);
                    }
                }
            }

            return arraylist;
        } else {
            String[] astring = s.split(" ", -1);
            String s2 = astring[astring.length - 1];
            String[] astring1 = this.v.f();
            int i = astring1.length;

            for (int j = 0; j < i; ++j) {
                String s3 = astring1[j];

                if (CommandAbstract.a(s2, s3)) {
                    arraylist.add(s3);
                }
            }

            return arraylist;
        }
        */
        return server.tabComplete(icommandlistener, s, blockposition); // PaperSpigot - add Location argument
        // CraftBukkit end
    }

    public static MinecraftServer getServer() {
        return MinecraftServer.l;
    }

    public boolean O() {
        return true; // CraftBukkit
    }

    public String getName() {
        return "Server";
    }

    public void sendMessage(IChatBaseComponent ichatbasecomponent) {
        MinecraftServer.LOGGER.info(org.bukkit.craftbukkit.util.CraftChatMessage.fromComponent(ichatbasecomponent, net.minecraft.server.EnumChatFormat.WHITE));
    }

    public boolean a(int i, String s) {
        return true;
    }

    public ICommandHandler getCommandHandler() {
        return this.b;
    }

    public KeyPair Q() {
        return this.H;
    }

    public int R() {
        return this.u;
    }

    public void setPort(int i) {
        this.u = i;
    }

    public String S() {
        return this.I;
    }

    public void i(String s) {
        this.I = s;
    }

    public boolean T() {
        return this.I != null;
    }

    public String U() {
        return this.J;
    }

    public void setWorld(String s) {
        this.J = s;
    }

    public void a(KeyPair keypair) {
        this.H = keypair;
    }

    public void a(EnumDifficulty enumdifficulty) {
        // CraftBukkit start
        for (int i = 0; i < this.worlds.size(); ++i) {
            WorldServer worldserver = this.worlds.get(i);
            // CraftBukkit end

            if (worldserver != null) {
                if (worldserver.getWorldData().isHardcore()) {
                    worldserver.getWorldData().setDifficulty(EnumDifficulty.HARD);
                    worldserver.setSpawnFlags(true, true);
                } else if (this.T()) {
                    worldserver.getWorldData().setDifficulty(enumdifficulty);
                    worldserver.setSpawnFlags(worldserver.getDifficulty() != EnumDifficulty.PEACEFUL, true);
                } else {
                    worldserver.getWorldData().setDifficulty(enumdifficulty);
                    worldserver.setSpawnFlags(this.getSpawnMonsters(), this.spawnAnimals);
                }
            }
        }

    }

    protected boolean getSpawnMonsters() {
        return true;
    }

    public boolean X() {
        return this.demoMode;
    }

    public void b(boolean flag) {
        this.demoMode = flag;
    }

    public void c(boolean flag) {
        this.M = flag;
    }

    public Convertable getConvertable() {
        return this.convertable;
    }

    public void aa() {
        this.N = true;
        this.getConvertable().d();

        // CraftBukkit start
        for (int i = 0; i < this.worlds.size(); ++i) {
            WorldServer worldserver = this.worlds.get(i);
            // CraftBukkit end
            
            if (worldserver != null) {
                worldserver.saveLevel();
            }
        }

        this.getConvertable().e(this.worlds.get(0).getDataManager().g()); // CraftBukkit
        this.safeShutdown();
    }

    public String getResourcePack() {
        return this.O;
    }

    public String getResourcePackHash() {
        return this.P;
    }

    public void setResourcePack(String s, String s1) {
        this.O = s;
        this.P = s1;
    }

    public void a(MojangStatisticsGenerator mojangstatisticsgenerator) {
        mojangstatisticsgenerator.a("whitelist_enabled", Boolean.valueOf(false));
        mojangstatisticsgenerator.a("whitelist_count", Integer.valueOf(0));
        if (this.v != null) {
            mojangstatisticsgenerator.a("players_current", Integer.valueOf(this.I()));
            mojangstatisticsgenerator.a("players_max", Integer.valueOf(this.J()));
            mojangstatisticsgenerator.a("players_seen", Integer.valueOf(this.v.getSeenPlayers().length));
        }

        mojangstatisticsgenerator.a("uses_auth", Boolean.valueOf(this.onlineMode));
        mojangstatisticsgenerator.a("gui_state", this.as() ? "enabled" : "disabled");
        mojangstatisticsgenerator.a("run_time", Long.valueOf((az() - mojangstatisticsgenerator.g()) / 60L * 1000L));
        mojangstatisticsgenerator.a("avg_tick_ms", Integer.valueOf((int) (MathHelper.a(this.h) * 1.0E-6D)));
        int i = 0;

        if (this.worldServer != null) {
            // CraftBukkit start
            for (int j = 0; j < this.worlds.size(); ++j) {
                WorldServer worldserver = this.worlds.get(j);
                if (worldserver != null) {
                    // CraftBukkit end
                    WorldData worlddata = worldserver.getWorldData();

                    mojangstatisticsgenerator.a("world[" + i + "][dimension]", Integer.valueOf(worldserver.worldProvider.getDimension()));
                    mojangstatisticsgenerator.a("world[" + i + "][mode]", worlddata.getGameType());
                    mojangstatisticsgenerator.a("world[" + i + "][difficulty]", worldserver.getDifficulty());
                    mojangstatisticsgenerator.a("world[" + i + "][hardcore]", Boolean.valueOf(worlddata.isHardcore()));
                    mojangstatisticsgenerator.a("world[" + i + "][generator_name]", worlddata.getType().name());
                    mojangstatisticsgenerator.a("world[" + i + "][generator_version]", Integer.valueOf(worlddata.getType().getVersion()));
                    mojangstatisticsgenerator.a("world[" + i + "][height]", Integer.valueOf(this.F));
                    mojangstatisticsgenerator.a("world[" + i + "][chunks_loaded]", Integer.valueOf(worldserver.N().getLoadedChunks()));
                    ++i;
                }
            }
        }

        mojangstatisticsgenerator.a("worlds", Integer.valueOf(i));
    }

    public void b(MojangStatisticsGenerator mojangstatisticsgenerator) {
        mojangstatisticsgenerator.b("singleplayer", Boolean.valueOf(this.T()));
        mojangstatisticsgenerator.b("server_brand", this.getServerModName());
        mojangstatisticsgenerator.b("gui_supported", GraphicsEnvironment.isHeadless() ? "headless" : "supported");
        mojangstatisticsgenerator.b("dedicated", Boolean.valueOf(this.ae()));
    }

    public boolean getSnooperEnabled() {
        return true;
    }

    public abstract boolean ae();

    public boolean getOnlineMode() {
        return server.getOnlineMode(); // CraftBukkit
    }

    public void setOnlineMode(boolean flag) {
        this.onlineMode = flag;
    }

    public boolean getSpawnAnimals() {
        return this.spawnAnimals;
    }

    public void setSpawnAnimals(boolean flag) {
        this.spawnAnimals = flag;
    }

    public boolean getSpawnNPCs() {
        return this.spawnNPCs;
    }

    public abstract boolean ai();

    public void setSpawnNPCs(boolean flag) {
        this.spawnNPCs = flag;
    }

    public boolean getPVP() {
        return this.pvpMode;
    }

    public void setPVP(boolean flag) {
        this.pvpMode = flag;
    }

    public boolean getAllowFlight() {
        return this.allowFlight;
    }

    public void setAllowFlight(boolean flag) {
        this.allowFlight = flag;
    }

    public abstract boolean getEnableCommandBlock();

    public String getMotd() {
        return this.motd;
    }

    public void setMotd(String s) {
        this.motd = s;
    }

    public int getMaxBuildHeight() {
        return this.F;
    }

    public void c(int i) {
        this.F = i;
    }

    public boolean isStopped() {
        return this.isStopped;
    }

    public PlayerList getPlayerList() {
        return this.v;
    }

    public void a(PlayerList playerlist) {
        this.v = playerlist;
    }

    public void setGamemode(WorldSettings.EnumGamemode worldsettings_enumgamemode) {
        // CraftBukkit start
        for (int i = 0; i < this.worlds.size(); ++i) {
            getServer().worlds.get(i).getWorldData().setGameType(worldsettings_enumgamemode);
        }

    }

    // Spigot Start
    public ServerConnection getServerConnection()
    {
        return this.q;
    }
    // Spigot End
    public ServerConnection aq() {
        return this.q == null ? this.q = new ServerConnection(this) : this.q; // Spigot
    }

    public boolean as() {
        return false;
    }

    public abstract String a(WorldSettings.EnumGamemode worldsettings_enumgamemode, boolean flag);

    public int at() {
        return this.ticks;
    }

    public void au() {
        this.T = true;
    }

    public BlockPosition getChunkCoordinates() {
        return BlockPosition.ZERO;
    }

    public Vec3D d() {
        return new Vec3D(0.0D, 0.0D, 0.0D);
    }

    public World getWorld() {
        return this.worlds.get(0); // CraftBukkit
    }

    public Entity f() {
        return null;
    }

    public int getSpawnProtection() {
        return 16;
    }

    public boolean a(World world, BlockPosition blockposition, EntityHuman entityhuman) {
        return false;
    }

    public void setForceGamemode(boolean flag) {
        this.U = flag;
    }

    public boolean getForceGamemode() {
        return this.U;
    }

    public Proxy ay() {
        return this.e;
    }

    public static long az() {
        return SystemUtils.getMonotonicMillis(); // PulseSpigot
    }

    public int getIdleTimeout() {
        return this.G;
    }

    public void setIdleTimeout(int i) {
        this.G = i;
    }

    public IChatBaseComponent getScoreboardDisplayName() {
        return new ChatComponentText(this.getName());
    }

    public boolean aB() {
        return true;
    }

    public MinecraftSessionService getMinecraftSessionService() { return this.aD(); } // PandaSpigot - OBFHELPER
    public MinecraftSessionService aD() {
        return this.W;
    }

    public GameProfileRepository getGameProfileRepository() {
        return this.Y;
    }

    public UserCache getUserCache() {
        return this.Z;
    }

    public ServerPing aG() {
        return this.r;
    }

    public void aH() {
        this.X = 0L;
    }

    public Entity a(UUID uuid) {
        WorldServer[] aworldserver = this.worldServer;
        int i = aworldserver.length;

        // CraftBukkit start
        for (int j = 0; j < worlds.size(); ++j) {
            WorldServer worldserver = worlds.get(j);
            // CraftBukkit end

            if (worldserver != null) {
                Entity entity = worldserver.getEntity(uuid);

                if (entity != null) {
                    return entity;
                }
            }
        }

        return null;
    }

    public boolean getSendCommandFeedback() {
        return getServer().worlds.get(0).getGameRules().getBoolean("sendCommandFeedback");
    }

    public void a(CommandObjectiveExecutor.EnumCommandResult commandobjectiveexecutor_enumcommandresult, int i) {}

    public int aI() {
        return 29999984;
    }

    public <V> ListenableFuture<V> a(Callable<V> callable) {
        Validate.notNull(callable);
        if (!this.isMainThread()) { // CraftBukkit && !this.isStopped()) {
            ListenableFutureTask listenablefuturetask = ListenableFutureTask.create(callable);
            Queue queue = this.j;

            // Spigot start
            this.j.add(listenablefuturetask);
            return listenablefuturetask;
            // Spigot end
        } else {
            try {
                return Futures.immediateFuture(callable.call());
            } catch (Exception exception) {
                return Futures.immediateFailedCheckedFuture(exception);
            }
        }
    }

    public ListenableFuture<Object> postToMainThread(Runnable runnable) {
        Validate.notNull(runnable);
        return this.a(Executors.callable(runnable));
    }

    public boolean isMainThread() {
        return Thread.currentThread() == this.serverThread;
    }

    public int aK() {
        return 256;
    }

    public long aL() {
        return this.ab;
    }

    public Thread aM() {
        return this.serverThread;
    }
}
