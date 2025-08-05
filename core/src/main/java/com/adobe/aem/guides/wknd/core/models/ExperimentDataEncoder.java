package com.adobe.aem.guides.wknd.core.models;

import com.day.cq.wcm.api.Page;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.commons.Externalizer;

import javax.annotation.PostConstruct;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Sling Model for encoding data (Base64 and URL encoding)
 * Automatically injects values from the current AEM context
 */
@Model(adaptables = {SlingHttpServletRequest.class, Resource.class}, 
       defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ExperimentDataEncoder {
    
    @SlingObject
    private SlingHttpServletRequest request;
    
    @SlingObject
    private ResourceResolver resourceResolver;
    
    @ScriptVariable
    private Page currentPage;
    
    @ValueMapValue
    private String imsOrg;
    
    private static final Logger LOG = LoggerFactory.getLogger(ExperimentDataEncoder.class);
    
    // Constants for default values
    private static final String DEFAULT_TRIGGER_SOURCE = "ue";
    private static final String DEFAULT_ENV = "prod";
    private static final String DEFAULT_LOCALE = "en-US";
    
    private String encodedResult;
    private String jsonData;
    
    @PostConstruct
    protected void init() {
        // Build JSON data from injected properties and request context
        jsonData = buildJsonData();
        
        if (jsonData != null && !jsonData.isEmpty()) {
            try {
                // First encode to Base64
                String base64Encoded = Base64.getEncoder().encodeToString(jsonData.getBytes(StandardCharsets.UTF_8));
                
                // Then URL encode for safe usage in URLs
                encodedResult = URLEncoder.encode(base64Encoded, StandardCharsets.UTF_8.toString());
            } catch (Exception e) {
                // Log error and return empty string as fallback
                encodedResult = "";
            }
        } else {
            encodedResult = "";
        }
    }
    
    /**
     * Builds JSON data from injected properties and request context
     * @return JSON string containing all the properties
     */
    private String buildJsonData() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        // Get values from request context
        String imsToken = getImsToken();
        String origin = getOrigin();
        String pageUrl = getPageUrl();
        
        appendJsonProperty(json, "imsToken", imsToken, true);
        appendJsonProperty(json, "imsOrg", imsOrg, false);
        appendJsonProperty(json, "origin", origin, false);
        appendJsonProperty(json, "pageUrl", pageUrl, false);
        appendJsonProperty(json, "triggerSource", DEFAULT_TRIGGER_SOURCE, false);
        appendJsonProperty(json, "env", DEFAULT_ENV, false);
        appendJsonProperty(json, "locale", DEFAULT_LOCALE, false);
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * Gets the IMS token from the Authorization header
     * @return Authorization header value or empty string
     */
    private String getImsToken() {
        if (request != null) {
            String authHeader = request.getHeader("authorization");
            if (authHeader != null && authHeader.toLowerCase().startsWith("bearer ")) {
                return authHeader.substring("bearer ".length());
            }
        }
        return "";
    }
    
    /**
     * Gets the origin from the Referer header
     * @return Referer header value or empty string
     */
    private String getOrigin() {
        if (request != null) {
            String referer = request.getHeader("referer");
            return referer != null ? referer : "";
        }
        return "";
    }
    
    /**
     * Gets the current page URL path
     * @return Current page path or empty string
     */
    private String getPageUrl() {
        String url = "";
        if (request == null) {
            url = "";
        }
        String requestUrl = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        String fullUrl = queryString != null ? requestUrl + "?" + queryString : requestUrl;

        // Check if this is an author instance (editor.html in URL)
        int editorIndex = fullUrl.indexOf("/sidepanel.content.html");
        // By default, pathStart is the index after the domain, i.e., the start of the path
        int pathStart = 0;
        String domainPrefix = "://";
        int domainIndex = fullUrl.indexOf(domainPrefix);
        if (domainIndex != -1) {
            int afterDomain = fullUrl.indexOf('/', domainIndex + domainPrefix.length());
            if (afterDomain != -1) {
                pathStart = afterDomain;
            } else {
                pathStart = fullUrl.length();
            }
        }
        if (editorIndex != -1) {
            pathStart = editorIndex + "/sidepanel.content.html".length();
        }
        int pathEnd = fullUrl.indexOf('?', pathStart);
        if (pathEnd == -1) {
            url = fullUrl.substring(pathStart);
        } else {
            url = fullUrl.substring(pathStart, pathEnd);
        }
        // Use AEM Externalizer to create the externalized URL
        String externalizedUrl = url;
        try {
            Externalizer externalizer = request.getResourceResolver()
                .adaptTo(Externalizer.class);
            if (externalizer != null) {
                externalizedUrl = externalizer.externalLink(
                    request.getResourceResolver(),
                    "publish",
                    url.startsWith("/") ? url : "/" + url
                );
            }
        } catch (Exception e) {
            // fallback to original url if externalizer fails
            externalizedUrl = url;
        }
        return externalizedUrl;
    }
    
    /**
     * Helper method to append a JSON property
     * @param json StringBuilder to append to
     * @param key Property key
     * @param value Property value
     * @param isFirst Whether this is the first property (no comma needed)
     */
    private void appendJsonProperty(StringBuilder json, String key, String value, boolean isFirst) {
        if (!isFirst) {
            json.append(",");
        }
        json.append("\"").append(key).append("\":\"");
        if (value != null) {
            // Escape quotes in the value
            json.append(value.replace("\"", "\\\""));
        }
        json.append("\"");
    }
    
    /**
     * Encodes data to Base64 and then URL encodes it for use in URLs
     * @return Base64 + URL encoded string
     */
    public String getEncodeForUrl() {
        return encodedResult;
    }
    
    /**
     * Only Base64 encode the data
     * @return Base64 encoded string
     */
    public String getBase64Encoded() {
        if (jsonData == null || jsonData.isEmpty()) {
            return "";
        }
        
        try {
            return Base64.getEncoder().encodeToString(jsonData.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Only URL encode the data
     * @return URL encoded string
     */
    public String getUrlEncoded() {
        if (jsonData == null || jsonData.isEmpty()) {
            return "";
        }
        
        try {
            return URLEncoder.encode(jsonData, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Get the raw JSON data built from properties
     * @return JSON string
     */
    public String getJsonData() {
        return jsonData != null ? jsonData : "";
    }
    
}