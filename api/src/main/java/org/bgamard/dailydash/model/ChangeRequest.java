package org.bgamard.dailydash.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

public class ChangeRequest {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    public LocalDateTime clientTimestamp = LocalDateTime.now(ZoneOffset.UTC);
    public List<String> nodes = List.of();
    public RequestHeader requestHeader;

    public ChangeRequest(RequestHeader requestHeader) {
        this.requestHeader = requestHeader;
    }

    public static class RequestHeader {
        public List<Capability> capabilities;
        public String clientLocale = "en_US";
        public String clientPlatform = "ANDROID";
        public String clientSessionId;
        public Map<String, Object> clientVersion = Map.of(
                "build", 62,
                "major", 5,
                "minor", 23,
                "revision", "0"
        );
        public String noteSupportedModelFeatures = "";
        
        public RequestHeader(String clientSessionId) {
            capabilities = List.of(
                    new Capability("TR"),
                    new Capability("EC"),
                    new Capability("SH"),
                    new Capability("RB"),
                    new Capability("LB"),
                    new Capability("DR"),
                    new Capability("AN"),
                    new Capability("PI"),
                    new Capability("EX"),
                    new Capability("CO"),
                    new Capability("MI"),
                    new Capability("SNB"),
                    new Capability("IN"),
                    new Capability("PS"),
                    new Capability("NC")
            );
            this.clientSessionId = clientSessionId;
        }
    }

    public static class Capability {
        public String type;

        public Capability(String type) {
            this.type = type;
        }
    }
}
