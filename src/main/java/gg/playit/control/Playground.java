package gg.playit.control;

import gg.playit.api.ApiClient;

import java.io.IOException;

public class Playground {
    public static void main(String[] args) throws IOException {
        var api = new ApiClient("");
        var res = api.getStatus();
        System.out.println(res);

//        try () {


//            var createTunnel = new CreateTunnel();
//            createTunnel.agentId = "a5a4a35b-f689-4721-a39b-0178f9ab7fee";
//            createTunnel.tunnelType = TunnelType.MinecraftJava;
//            createTunnel.portType = PortType.TCP;
//            createTunnel.portCount = 1;
//            createTunnel.localIp = InetAddress.getByName("127.0.0.1");
//
//            var res = api.createTunnel(createTunnel);
//            System.out.println(res.id);
//        }


//        var channel = PlayitControlChannel.setup("");
//
//        while (true) {
//            var feed = channel.update();
//            if (feed.isPresent()) {
//                var value = feed.get();
//                System.out.println("Got msg: " + value);
//            }
//        }
    }
}
