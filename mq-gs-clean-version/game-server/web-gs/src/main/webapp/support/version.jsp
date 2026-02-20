<%@ page import="com.google.gson.Gson" %>
<%@ page import="org.w3c.dom.Document" %>
<%@ page import="org.w3c.dom.Element" %>
<%@ page import="javax.xml.parsers.DocumentBuilder" %>
<%@ page import="javax.xml.parsers.DocumentBuilderFactory" %>
<%@ page import="javax.xml.transform.Transformer" %>
<%@ page import="javax.xml.transform.TransformerFactory" %>
<%@ page import="javax.xml.transform.dom.DOMSource" %>
<%@ page import="javax.xml.transform.stream.StreamResult" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.io.StringWriter" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.jar.Manifest" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%--
  Author: svvedenskiy
  Date: 9/10/19
--%>
<%!
    private final Map<String, String> manifestKeys = new HashMap<>();
%>
<%
    manifestKeys.put("Built-By", "Build by");
    manifestKeys.put("Specification-Version", "Release version");
    manifestKeys.put("Build-Date", "Build date");
    manifestKeys.put("Implementation-Version", "Revision");

    Map<String, String> manifestData = readManifestData(application);
    ManifestFormatter formatter = createManifestFormatter(request.getParameter("format"));
    response.getWriter().write(formatter.format(manifestData));
%>

<%!
    Map<String, String> readManifestData(ServletContext application) throws IOException {
        Map<String, String> manifestData = new HashMap<>();
        InputStream inputStream = application.getResourceAsStream("/META-INF/MANIFEST.MF");
        Manifest manifest = new Manifest(inputStream);
        for (Map.Entry<Object, Object> entry : manifest.getMainAttributes().entrySet()) {
            if (manifestKeys.keySet().contains(String.valueOf(entry.getKey()))) {
                manifestData.put(String.valueOf(manifestKeys.get(String.valueOf(entry.getKey()))), String.valueOf(entry.getValue()));
            }
        }
        return manifestData;
    }

    interface ManifestFormatter {
        String format(Map<String, String> manifestData);
    }

    ManifestFormatter createManifestFormatter(String format) {
        if (format == null) {
            return new HtmlManifestFormatter();
        }
        switch (format) {
            case "html":
                return new HtmlManifestFormatter();
            case "txt":
                return new TextManifestFormatter();
            case "json":
                return new JsonManifestFormatter();
            case "xml":
                return new XmlManifestFormatter();
            default:
                return new HtmlManifestFormatter();
        }
    }

    class HtmlManifestFormatter implements ManifestFormatter {
        public String format(Map<String, String> manifestData) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : manifestData.entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("<br/>");
            }
            return sb.toString();
        }
    }

    class TextManifestFormatter implements ManifestFormatter {
        public String format(Map<String, String> manifestData) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : manifestData.entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            return sb.toString();
        }
    }

    class JsonManifestFormatter implements ManifestFormatter {
        public String format(Map<String, String> manifestData) {
            Gson gson = new Gson();
            return gson.toJson(manifestData);
        }
    }

    class XmlManifestFormatter implements ManifestFormatter {
        public String format(Map<String, String> manifestData) {
            try {
                DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
                Document document = documentBuilder.newDocument();
                Element root = document.createElement("manifest");
                document.appendChild(root);
                for (Map.Entry<String, String> entry : manifestData.entrySet()) {
                    Element textNode = document.createElement(entry.getKey().replace(" ", "_"));
                    textNode.setTextContent(entry.getValue());
                    root.appendChild(textNode);
                }
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer transformer = tf.newTransformer();
                StringWriter writer = new StringWriter();
                transformer.transform(new DOMSource(document), new StreamResult(writer));
                return writer.getBuffer().toString();
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }
%>
