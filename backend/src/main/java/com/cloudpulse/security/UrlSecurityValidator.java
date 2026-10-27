package com.cloudpulse.security;

import com.cloudpulse.exception.SecurityValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Set;

@Component
public class UrlSecurityValidator {

    private static final Logger log = LoggerFactory.getLogger(UrlSecurityValidator.class);

    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

    // Known cloud provider metadata and dangerous link-local IP addresses
    private static final Set<String> BLOCKED_IP_LITERALS = Set.of(
            "169.254.169.254", // AWS, GCP, Azure, OpenStack IMDS
            "169.254.170.2",   // AWS ECS Task Metadata
            "100.100.100.200", // Alibaba Cloud metadata
            "0.0.0.0",
            "127.0.0.1",
            "localhost"
    );

    @Value("${cloudpulse.security.allow-private-ips:false}")
    private boolean allowPrivateIps;

    @Value("${cloudpulse.security.allow-localhost:false}")
    private boolean allowLocalhost;

    /**
     * Validates a target URL against SSRF (Server-Side Request Forgery) attacks.
     * Prevents requests to cloud instance metadata, local loopback, and internal private network IPs.
     *
     * @param urlString The candidate URL to validate
     * @throws SecurityValidationException if the URL violates security policies
     */
    public void validateUrl(String urlString) {
        if (urlString == null || urlString.isBlank()) {
            throw new SecurityValidationException("Monitoring URL cannot be empty.");
        }

        URI uri;
        try {
            uri = URI.create(urlString.trim());
            String scheme = uri.getScheme();

            if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.toLowerCase())) {
                throw new SecurityValidationException("Invalid protocol: '" + scheme + "'. Only HTTP and HTTPS are permitted.");
            }

            URL url = uri.toURL();
            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                throw new SecurityValidationException("URL must specify a valid host.");
            }

            // Reject user info (e.g., http://admin:pass@host)
            if (uri.getUserInfo() != null) {
                throw new SecurityValidationException("URLs with embedded user credentials are not permitted.");
            }

            // Direct check for dangerous literals
            if (BLOCKED_IP_LITERALS.contains(host.toLowerCase())) {
                if (host.equalsIgnoreCase("localhost") || host.equals("127.0.0.1")) {
                    if (!allowLocalhost) {
                        throw new SecurityValidationException("SSRF Protection: Monitoring localhost/127.0.0.1 is prohibited unless explicitly configured in dev mode.");
                    }
                } else {
                    throw new SecurityValidationException("SSRF Protection: Monitoring cloud metadata or reserved IP (" + host + ") is strictly prohibited.");
                }
            }

            // DNS Resolution & IP inspection to prevent DNS rebinding and internal network probing
            resolveAndValidateAddresses(host);

        } catch (IllegalArgumentException | java.net.MalformedURLException e) {
            throw new SecurityValidationException("Malformed monitoring URL: " + e.getMessage());
        }
    }

    private void resolveAndValidateAddresses(String host) {
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress address : addresses) {
                String ipString = address.getHostAddress();

                if (BLOCKED_IP_LITERALS.contains(ipString)) {
                    if ((address.isLoopbackAddress() || ipString.equals("127.0.0.1")) && allowLocalhost) {
                        continue;
                    }
                    throw new SecurityValidationException("SSRF Protection: Host resolved to prohibited address (" + ipString + ").");
                }

                // Check for loopback
                if (address.isLoopbackAddress() && !allowLocalhost) {
                    throw new SecurityValidationException("SSRF Protection: Loopback address (" + ipString + ") is not allowed.");
                }

                // Check for link-local (169.254.0.0/16 or fe80::/10)
                if (address.isLinkLocalAddress()) {
                    throw new SecurityValidationException("SSRF Protection: Link-local address (" + ipString + ") is not allowed.");
                }

                // Check for private / site-local (10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16)
                if (address.isSiteLocalAddress() && !allowPrivateIps) {
                    throw new SecurityValidationException("SSRF Protection: Private internal network IP (" + ipString + ") is forbidden.");
                }

                // Any local address (0.0.0.0)
                if (address.isAnyLocalAddress()) {
                    throw new SecurityValidationException("SSRF Protection: Wildcard local address (" + ipString + ") is forbidden.");
                }

                // Multicast addresses
                if (address.isMulticastAddress()) {
                    throw new SecurityValidationException("SSRF Protection: Multicast address (" + ipString + ") is forbidden.");
                }
            }
        } catch (UnknownHostException e) {
            log.warn("Host could not be resolved during security validation: {}", host);
            // We allow the check to proceed to health check executor, where network connection will fail cleanly with standard DNS error
        }
    }

    // Setters for unit testing without full Spring Context
    public void setAllowPrivateIps(boolean allowPrivateIps) {
        this.allowPrivateIps = allowPrivateIps;
    }

    public void setAllowLocalhost(boolean allowLocalhost) {
        this.allowLocalhost = allowLocalhost;
    }
}
