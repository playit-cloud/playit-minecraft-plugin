package gg.playit.control.example;

import gg.playit.api2.model.request.AgentVersion;
import gg.playit.control.PlayitControlChannel;
import gg.playit.messages.ControlFeedReader;

import java.io.IOException;

/**
 * Example demonstrating how to set up an authenticated playit control channel.
 * <p>
 * Run with: PLAYIT_AGENT_SECRET=your_secret java -cp ... gg.playit.control.example.ControlExample
 */
public class ControlExample {
    public static void main(String[] args) {
        String secret = System.getenv("PLAYIT_AGENT_SECRET");
        if (secret == null || secret.isBlank()) {
            System.err.println("Set PLAYIT_AGENT_SECRET environment variable");
            System.exit(1);
        }

        var version = new AgentVersion("f4e73f52-f35c-4f18-9ab2-3aaa5c4488c1", 0, 2, 0);

        try (PlayitControlChannel channel = PlayitControlChannel.setup(secret, version)) {
            System.out.println("Authenticated control channel established:");
            System.out.println("  " + channel);
            System.out.println();

            /* Run a few update cycles to demonstrate the channel is alive */
            System.out.println("Running 5 update cycles (Ctrl+C to exit)...");
            for (int i = 0; i < 5; i++) {
                var msg = channel.update();
                if (msg.isPresent()) {
                    var feed = msg.get();
                    if (feed instanceof ControlFeedReader.Pong) {
                        System.out.println("  [" + i + "] Received pong");
                    } else if (feed instanceof ControlFeedReader.NewClient nc) {
                        System.out.println("  [" + i + "] New client: " + nc.peerAddr);
                    } else if (feed instanceof ControlFeedReader.AgentRegistered) {
                        System.out.println("  [" + i + "] Agent re-registered");
                    }
                }
                Thread.sleep(1100);
            }
            System.out.println("Done.");
        } catch (IOException e) {
            System.err.println("Failed to setup channel: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted");
            System.exit(1);
        }
    }
}
