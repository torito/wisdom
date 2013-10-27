package org.ow2.chameleon.wisdom.template.thymeleaf;

import org.apache.commons.io.FilenameUtils;
import org.ow2.chameleon.wisdom.api.http.MimeTypes;
import org.ow2.chameleon.wisdom.api.http.Renderable;
import org.ow2.chameleon.wisdom.api.templates.Template;
import org.ow2.chameleon.wisdom.template.thymeleaf.impl.WisdomTemplateEngine;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

/**
 * Template implementation for ThymeLeaf template.
 */
public class ThymeLeafTemplateImplementation implements Template {
    public static final String THYME_LEAF_ENGINE_NAME = "thymeleaf";
    private final URL url;
    private final String name;
    private WisdomTemplateEngine templateEngine;

    public ThymeLeafTemplateImplementation(WisdomTemplateEngine templateEngine, File templateFile) throws MalformedURLException {
        this(templateEngine, templateFile.toURI().toURL());
    }

    public ThymeLeafTemplateImplementation(WisdomTemplateEngine templateEngine, URL templateURL) {
        this.templateEngine = templateEngine;
        this.url = templateURL;
        name = FilenameUtils.getBaseName(templateURL.getFile());
    }

    public URL getURL() {
        return url;
    }

    /**
     * @return the template name, usually the template file name without the extension.
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * @return the template full name. For example, for a file, it will be the file name (including extension).
     */
    @Override
    public String fullName() {
        return url.toExternalForm();
    }

    /**
     * @return the name of the template engine, generally the name of the technology.
     */
    @Override
    public String engine() {
        return THYME_LEAF_ENGINE_NAME;
    }

    /**
     * @return the mime type of the document produced by the template.
     */
    @Override
    public String mimetype() {
        return MimeTypes.HTML;
    }

    /**
     * Renders the template
     *
     * @param variables the parameters
     * @return the rendered object.
     */
    @Override
    public Renderable render(Map<String, Object> variables) {
        return templateEngine.process(this, variables);
    }

    @Override
    public Renderable render() {
        return templateEngine.process(this, Collections.<String, Object>emptyMap());
    }

    public Dictionary<String, ?> getServiceProperties() {
        Hashtable<String, String> props = new Hashtable<>();
        props.put("name", name());
        props.put("fullName", fullName());
        props.put("mimetype", mimetype());
        props.put("engine", engine());
        return props;
    }
}
