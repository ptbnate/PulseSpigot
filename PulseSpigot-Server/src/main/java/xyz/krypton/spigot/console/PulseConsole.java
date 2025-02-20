package xyz.krypton.spigot.console;

import java.nio.file.Paths;
import net.minecraft.server.DedicatedServer;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

public final class PulseConsole extends SimpleTerminalConsole {

    private final DedicatedServer server;

    public PulseConsole(DedicatedServer server) {
        this.server = server;
    }

    @Override
    protected LineReader buildReader(LineReaderBuilder builder) {
        return super.buildReader(builder
                .appName("PulseSpigot")
                .variable(LineReader.HISTORY_FILE, Paths.get(".console_history"))
                .completer(new PulseConsoleCommandCompleter(this.server))
        );
    }

    @Override
    protected boolean isRunning() {
        return !this.server.isStopped() && this.server.isRunning();
    }

    @Override
    protected void runCommand(String command) {
        this.server.issueCommand(command, this.server);
    }

    @Override
    protected void shutdown() {
        this.server.safeShutdown();
    }

}
