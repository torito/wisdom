package org.wisdom.engine.wrapper.cookies;

import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.cookies.Cookie;
import org.wisdom.api.cookies.FlashCookie;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Result;
import org.wisdom.api.utils.CookieDataCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Flash scope: A client side cookie that can be used to transfer information
 * from one request to another.
 * <p/>
 * Stuff in a flash cookie gets deleted after the next request.
 * <p/>
 * Please note also that flash cookies are not signed.
 */
public class FlashCookieImpl implements FlashCookie {

    public static final String FLASH_SUFFIX = "_FLASH";
    private static Logger logger = LoggerFactory.getLogger(FlashCookieImpl.class);
    private Map<String, String> currentFlashCookieData = new HashMap<String, String>();
    private Map<String, String> outgoingFlashCookieData = new HashMap<String, String>();
    private final String applicationCookiePrefix;

    public FlashCookieImpl(ApplicationConfiguration configuration) {
        applicationCookiePrefix = configuration.getWithDefault(Cookie.applicationCookiePrefix, "wisdom");
    }

    @Override
    public void init(Context context) {
        // get flash cookie:
        Cookie flashCookie = context.request().cookie(applicationCookiePrefix
                + FLASH_SUFFIX);

        if (flashCookie != null) {
            try {
                CookieDataCodec.decode(currentFlashCookieData, flashCookie.value());
            } catch (UnsupportedEncodingException e) {
                logger.error("Encoding exception - this must not happen", e);
            }
        }

    }

    @Override
    public void save(Context context, Result result) {

        if (outgoingFlashCookieData.isEmpty()) {

            if (context.hasCookie(applicationCookiePrefix
                    + FLASH_SUFFIX)) {
                // Cleat the cookie.
                Cookie.Builder cookie = Cookie.builder(applicationCookiePrefix
                        + FLASH_SUFFIX, "");
                cookie.setPath("/");
                cookie.setSecure(false);
                cookie.setMaxAge(0);
                result.addCookie(cookie.build());

            }
        } else {
            try {

                String flashData = CookieDataCodec.encode(outgoingFlashCookieData);

                Cookie.Builder cookie = Cookie.builder(applicationCookiePrefix
                        + FLASH_SUFFIX, flashData);
                cookie.setPath("/");
                cookie.setSecure(false);
                // "-1" does not set "Expires" for that cookie
                // => Cookie will live as long as the browser is open theoretically
                cookie.setMaxAge(-1);
                result.addCookie(cookie.build());

            } catch (Exception e) {
                logger.error("Encoding exception - this must not happen", e);
            }
        }
    }

    @Override
    public void put(String key, String value) {
        if (key.contains(":")) {
            throw new IllegalArgumentException(
                    "Character ':' is invalid in a flash key.");
        }
        currentFlashCookieData.put(key, value);
        outgoingFlashCookieData.put(key, value);
    }

    @Override
    public void put(String key, Object value) {
        if (value == null) {
            put(key, null);
        }
        put(key, value + "");
    }

    @Override
    public void now(String key, String value) {
        if (key.contains(":")) {
            throw new IllegalArgumentException(
                    "Character ':' is invalid in a flash key.");
        }
        currentFlashCookieData.put(key, value);
    }

    @Override
    public void error(String value) {
        put("error", value);
    }

    @Override
    public void success(String value) {
        put("success", value);
    }

    @Override
    public void discard(String key) {
        outgoingFlashCookieData.remove(key);
    }

    @Override
    public void discard() {
        outgoingFlashCookieData.clear();
    }

    @Override
    public void keep(String key) {
        if (currentFlashCookieData.containsKey(key)) {
            outgoingFlashCookieData.put(key, currentFlashCookieData.get(key));
        }
    }

    @Override
    public void keep() {
        outgoingFlashCookieData.putAll(currentFlashCookieData);
    }

    @Override
    public String get(String key) {
        return currentFlashCookieData.get(key);
    }

    @Override
    public boolean remove(String key) {
        return currentFlashCookieData.remove(key) != null;
    }

    @Override
    public void clearCurrentFlashCookieData() {
        currentFlashCookieData.clear();
    }

    @Override
    public boolean contains(String key) {
        return currentFlashCookieData.containsKey(key);
    }

    @Override
    public Map<String, String> getCurrentFlashCookieData() {
        return currentFlashCookieData;
    }

    @Override
    public Map<String, String> getOutgoingFlashCookieData() {
        return outgoingFlashCookieData;
    }
}