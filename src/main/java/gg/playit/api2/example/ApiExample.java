package gg.playit.api2.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import gg.playit.api2.ApiClient;
import gg.playit.api2.model.ApiResultNoFail;

public class ApiExample {
    public static void main(String[] args) throws Exception {
        String secret = System.getenv("PLAYIT_AGENT_SECRET");
        if (secret == null || secret.isBlank()) {
            System.err.println("Set PLAYIT_AGENT_SECRET environment variable");
            System.exit(1);
        }

        var client = new ApiClient(secret);
        var mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ApiResultNoFail<?> result = client.v1AgentsRundata();
        String json = mapper.writeValueAsString(result);
        System.out.println(json);
    }
}
