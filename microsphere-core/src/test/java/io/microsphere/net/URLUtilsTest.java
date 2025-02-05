/**
 *
 */
package io.microsphere.net;

import io.microsphere.constants.FileConstants;
import io.microsphere.util.ClassLoaderUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.microsphere.net.URLUtils.attachURLStreamHandlerFactory;
import static io.microsphere.net.URLUtils.buildMatrixString;
import static io.microsphere.net.URLUtils.getURLStreamHandlerFactory;
import static io.microsphere.net.URLUtils.resolveMatrixParameters;
import static io.microsphere.net.URLUtils.resolveQueryParameters;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * {@link URLUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @version 1.0.0
 * @see URLUtilsTest
 * @since 1.0.0
 */
public class URLUtilsTest {

    @After
    public void after() {
        URLUtils.clearURLStreamHandlerFactory();
    }

    @Test
    public void testEncodeAndDecode() {
        String path = "/abc/def";

        String encodedPath = URLUtils.encode(path);
        String decodedPath = URLUtils.decode(encodedPath);
        assertEquals(path, decodedPath);

        encodedPath = URLUtils.encode(path, "GBK");
        decodedPath = URLUtils.decode(encodedPath, "GBK");
        assertEquals(path, decodedPath);
    }

    @Test
    public void testResolvePath() {
        String path = null;
        String expectedPath = null;
        String resolvedPath = null;

        resolvedPath = URLUtils.normalizePath(path);
        assertEquals(expectedPath, resolvedPath);

        path = "";
        expectedPath = "";
        resolvedPath = URLUtils.normalizePath(path);
        assertEquals(expectedPath, resolvedPath);

        path = "/abc/";
        expectedPath = "/abc/";
        resolvedPath = URLUtils.normalizePath(path);
        assertEquals(expectedPath, resolvedPath);

        path = "//abc///";
        expectedPath = "/abc/";
        resolvedPath = URLUtils.normalizePath(path);
        assertEquals(expectedPath, resolvedPath);


        path = "//\\abc///";
        expectedPath = "/abc/";
        resolvedPath = URLUtils.normalizePath(path);
        assertEquals(expectedPath, resolvedPath);
    }

    @Test
    public void testResolveRelativePath() throws MalformedURLException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL resourceURL = ClassLoaderUtils.getClassResource(classLoader, String.class);
        String expectedPath = "java/lang/String.class";
        String relativePath = URLUtils.resolveRelativePath(resourceURL);
        assertEquals(expectedPath, relativePath);

        File rtJarFile = new File(SystemUtils.JAVA_HOME, "lib/rt.jar");
        resourceURL = rtJarFile.toURI().toURL();
        relativePath = URLUtils.resolveRelativePath(resourceURL);
        assertNull(relativePath);
    }

    @Test
    public void testResolveArchiveFile() {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL resourceURL = ClassLoaderUtils.getClassResource(classLoader, String.class);
        File archiveFile = URLUtils.resolveArchiveFile(resourceURL, FileConstants.JAR_EXTENSION);
        assertTrue(archiveFile.exists());
    }

    @Test
    public void testResolveQueryParameters() {
        String url = "https://www.google.com.hk/search?q=java&oq=java&sourceid=chrome&es_sm=122&ie=UTF-8";
        Map<String, List<String>> parametersMap = resolveQueryParameters(url);
        Map<String, List<String>> expectedParametersMap = new LinkedHashMap<>();
        expectedParametersMap.put("q", Arrays.asList("java"));
        expectedParametersMap.put("oq", Arrays.asList("java"));
        expectedParametersMap.put("sourceid", Arrays.asList("chrome"));
        expectedParametersMap.put("es_sm", Arrays.asList("122"));
        expectedParametersMap.put("ie", Arrays.asList("UTF-8"));

        assertEquals(expectedParametersMap, parametersMap);

        url = "https://www.google.com.hk/search";
        parametersMap = resolveQueryParameters(url);
        expectedParametersMap = Collections.emptyMap();
        assertEquals(expectedParametersMap, parametersMap);

        url = "https://www.google.com.hk/search?";
        parametersMap = resolveQueryParameters(url);
        expectedParametersMap = Collections.emptyMap();
        assertEquals(expectedParametersMap, parametersMap);
    }

    @Test
    public void testResolveMatrixParameters() {
        String url = "https://www.google.com.hk/search;q=java;oq=java;sourceid=chrome;es_sm=122;ie=UTF-8";
        Map<String, List<String>> parametersMap = resolveMatrixParameters(url);
        Map<String, List<String>> expectedParametersMap = new LinkedHashMap<>();
        expectedParametersMap.put("q", Arrays.asList("java"));
        expectedParametersMap.put("oq", Arrays.asList("java"));
        expectedParametersMap.put("sourceid", Arrays.asList("chrome"));
        expectedParametersMap.put("es_sm", Arrays.asList("122"));
        expectedParametersMap.put("ie", Arrays.asList("UTF-8"));

        assertEquals(expectedParametersMap, parametersMap);

        url = "https://www.google.com.hk/search";
        parametersMap = resolveMatrixParameters(url);
        expectedParametersMap = Collections.emptyMap();
        assertEquals(expectedParametersMap, parametersMap);

        url = "https://www.google.com.hk/search;";
        parametersMap = resolveMatrixParameters(url);
        expectedParametersMap = Collections.emptyMap();
        assertEquals(expectedParametersMap, parametersMap);
    }

    @Test
    public void testBuildMatrixString() {
        String matrixString = buildMatrixString("n", "1");
        assertEquals(";n=1", matrixString);

        matrixString = buildMatrixString("n", "1", "2");
        assertEquals(";n=1;n=2", matrixString);

        matrixString = buildMatrixString("n", "1", "2", "3");
        assertEquals(";n=1;n=2;n=3", matrixString);
    }

    @Test
    public void testIsDirectoryURL() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resourceURL = ClassLoaderUtils.getClassResource(classLoader, StringUtils.class);
        assertFalse(URLUtils.isDirectoryURL(resourceURL));

        String externalForm = null;
        externalForm = StringUtils.substringBeforeLast(resourceURL.toExternalForm(), StringUtils.class.getSimpleName() + ".class");
        resourceURL = new URL(externalForm);
        assertTrue(URLUtils.isDirectoryURL(resourceURL));

        resourceURL = ClassLoaderUtils.getClassResource(classLoader, String.class);
        assertFalse(URLUtils.isDirectoryURL(resourceURL));

        resourceURL = ClassLoaderUtils.getClassResource(classLoader, getClass());
        assertFalse(URLUtils.isDirectoryURL(resourceURL));

        externalForm = StringUtils.substringBeforeLast(resourceURL.toExternalForm(), getClass().getSimpleName() + ".class");
        resourceURL = new URL(externalForm);
        assertTrue(URLUtils.isDirectoryURL(resourceURL));
    }

    @Test
    public void testAttachURLStreamHandlerFactory() {
        URLStreamHandlerFactory factory = new StandardURLStreamHandlerFactory();
        attachURLStreamHandlerFactory(factory);
        assertSame(factory, getURLStreamHandlerFactory());

        attachURLStreamHandlerFactory(factory);
        CompositeURLStreamHandlerFactory compositeFactory = (CompositeURLStreamHandlerFactory) getURLStreamHandlerFactory();
        assertNotSame(factory, compositeFactory);
        assertEquals(1, compositeFactory.getFactories().size());
        assertSame(factory, compositeFactory.getFactories().get(0));
        assertEquals(CompositeURLStreamHandlerFactory.class, compositeFactory.getClass());

    }

}
