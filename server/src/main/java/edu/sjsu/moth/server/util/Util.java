package edu.sjsu.moth.server.util;

import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * simple utility methods
 */
public class Util {
    public static final SimpleDateFormat jsonDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    /* prepend 13 random bytes and append "frog" to the end. iterate 10,000 times (to make it slower) using SHA256*/
    private final static Pbkdf2PasswordEncoder PASSWORD_ENCODER = new Pbkdf2PasswordEncoder("frog", 13, 10_000,
                                                                                            Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);

    static {
        PASSWORD_ENCODER.setEncodeHashAsBase64(true);
    }

    /**
     * super gross code to convert and enumeration to a stream. (should be built into java!)
     *
     * @param en  the enumeration to stream
     * @param <T> the type of objects being streamed
     * @return a stream of the enumeration
     */
    public static <T> Stream<T> enumerationToStream(Enumeration<T> en) {
        return StreamSupport.stream(
                Spliterators.spliterator(en.asIterator(), Long.MAX_VALUE, Spliterator.IMMUTABLE | Spliterator.NONNULL),
                false);
    }

    public static String now() {return jsonDateFormat.format(new Date());}

    /**
     * return a salted and hashed password
     */
    public static String encodePassword(String password) {
        return PASSWORD_ENCODER.encode(password);
    }

    /*
     * simple helper method to encapsulate the annoying exception handling with Mono and URI
     */
    public static Mono<URI> getMonoURI(String uri) {
        try {
            return Mono.just(new URI(uri));
        } catch (URISyntaxException e) {
            return Mono.error(e);
        }
    }

    /*
     * URL encode a parameter
     */
    public static String URLencode(String str) {
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }

    /**
     * check a password against a salted and hashed password
     */
    public static boolean checkPassword(String password, String encodedPassword) {
        try {
            return PASSWORD_ENCODER.matches(password, encodedPassword);
        } catch (Exception ignore) {
            // strange exceptions can be thrown if the encoded password is messed up
            return false;
        }
    }
}