package com.newrelic.agent.Obfuscation;

import com.newrelic.agent.compile.Log;
import com.newrelic.agent.compile.RewriterAgent;
import com.newrelic.agent.compile.visitor.NewRelicClassVisitor;
import com.google.common.io.BaseEncoding;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

public class Proguard {
    private static final String NR_PROPERTIES = "newrelic.properties";
    private static final String PROP_NR_APP_TOKEN = "com.newrelic.application_token";
    private static final String PROP_UPLOADING_ENABLED = "com.newrelic.enable_proguard_upload";
    private static final String PROP_MAPPING_API_HOST = "com.newrelic.mapping_upload_host";
    private static final String MAPPING_FILENAME = "mapping.txt";
    private static final String DEFAULT_MAPPING_API_HOST = "mobile-symbol-upload.newrelic.com";
    private static final String MAPPING_API_PATH = "/symbol";
    private static final String LICENSE_KEY_HEADER = "X-APP-LICENSE-KEY";
    private final Log log;
    private String projectRoot;
    private String licenseKey = null;
    private boolean uploadingEnabled = true;
    private String mappingApiHost = null;

    public Proguard(Log log) {
        this.log = log;
    }

    public void findAndSendMapFile() {
        String mappingString = "";

        if (getProjectRoot() != null) {
            if (!fetchConfiguration()) {
                return;
            }

            File projectRoot = new File(getProjectRoot());
            IOFileFilter fileFilter = FileFilterUtils.nameFileFilter("mapping.txt");
            Collection<File> files = FileUtils.listFiles(projectRoot, fileFilter, TrueFileFilter.INSTANCE);

            if (files.isEmpty()) {
                this.log.error("While evidence of ProGuard was detected, New Relic failed to find your mapping.txt file.");
                this.log.error("To de-obfuscate your builds, you'll need to upload your mapping.txt manually.");
            }

            for (File file : files) {
                this.log.info("Found mapping.txt: " + file.getPath());
                try {
                    FileWriter fileWriter = new FileWriter(file, true);
                    fileWriter.write("# NR_BUILD_ID: " + NewRelicClassVisitor.getBuildId());
                    fileWriter.close();

                    mappingString = mappingString + FileUtils.readFileToString(file);
                } catch (FileNotFoundException e) {
                    this.log.error("Unable to open your mapping.txt file: " + e.getLocalizedMessage());
                    this.log.error("To de-obfuscate your builds, you'll need to upload your mapping.txt manually.");
                } catch (IOException e) {
                    this.log.error("Unable to open your mapping.txt file: " + e.getLocalizedMessage());
                    this.log.error("To de-obfuscate your builds, you'll need to upload your mapping.txt manually.");
                }
            }

            if (this.uploadingEnabled)
                sendMapping(mappingString);
        }
    }

    private String getProjectRoot() {
        if (this.projectRoot == null) {
            String encodedProjectRoot = (String) RewriterAgent.getAgentOptions().get("projectRoot");

            if (encodedProjectRoot == null) {
                this.log.info("Unable to determine project root, falling back to CWD.");
                this.projectRoot = System.getProperty("user.dir");
            } else {
                this.projectRoot = new String(BaseEncoding.base64().decode(encodedProjectRoot));
                this.log.info("Project root: " + this.projectRoot);
            }
        }

        return this.projectRoot;
    }

    private boolean fetchConfiguration() {
        try {
            Reader propsReader = new BufferedReader(new FileReader(getProjectRoot() + File.separator + "newrelic.properties"));
            Properties newRelicProps = new Properties();
            newRelicProps.load(propsReader);

            this.licenseKey = newRelicProps.getProperty("com.newrelic.application_token");
            this.uploadingEnabled = newRelicProps.getProperty("com.newrelic.enable_proguard_upload", "true").equals("true");
            this.mappingApiHost = newRelicProps.getProperty("com.newrelic.mapping_upload_host");

            if (this.licenseKey == null) {
                this.log.error("Unable to find a value for com.newrelic.application_token in your newrelic.properties");
                this.log.error("To de-obfuscate your builds, you'll need to upload your mapping.txt manually.");

                return false;
            }

            propsReader.close();
        } catch (FileNotFoundException e) {
            this.log.error("Unable to find your newrelic.properties in the project root (" + getProjectRoot() + "): " + e.getLocalizedMessage());
            this.log.error("To de-obfuscate your builds, you'll need to upload your mapping.txt manually.");

            return false;
        } catch (IOException e) {
            this.log.error("Unable to read your newrelic.properties in the project root (" + getProjectRoot() + "): " + e.getLocalizedMessage());
            this.log.error("To de-obfuscate your builds, you'll need to upload your mapping.txt manually.");

            return false;
        }

        return true;
    }

    private void sendMapping(String mapping) {
        StringBuilder requestBody = new StringBuilder();

        requestBody.append("proguard=" + URLEncoder.encode(mapping));
        requestBody.append("&buildId=" + NewRelicClassVisitor.getBuildId());
        try {
            String host = "mobile-symbol-upload.newrelic.com";
            if (this.mappingApiHost != null) {
                host = this.mappingApiHost;
            }

            URL url = new URL("https://" + host + "/symbol");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("X-APP-LICENSE-KEY", this.licenseKey);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", Integer.toString(requestBody.length()));

            DataOutputStream request = new DataOutputStream(connection.getOutputStream());
            request.writeBytes(requestBody.toString());
            request.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == 400) {
                InputStream inputStream = connection.getErrorStream();
                String response = convertStreamToString(inputStream);
                this.log.error("Unable to send your ProGuard mapping.txt to New Relic as the params are incorrect: " + response);
                this.log.error("To de-obfuscate your builds, you'll need to upload your mapping.txt manually.");
            } else if (responseCode > 400) {
                InputStream inputStream = connection.getErrorStream();
                String response = convertStreamToString(inputStream);
                this.log.error("Unable to send your ProGuard mapping.txt to New Relic - received status " + responseCode + ": " + response);
                this.log.error("To de-obfuscate your builds, you'll need to upload your mapping.txt manually.");
            }

            this.log.info("Successfully sent mapping.txt to New Relic.");

            connection.disconnect();
        } catch (IOException e) {
            this.log.error("Encountered an error while uploading your ProGuard mapping to New Relic", e);
            this.log.error("To de-obfuscate your builds, you'll need to upload your mapping.txt manually.");
        }
    }

    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null)
                sb.append(line + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}

/* Location:           /home/cw/class-rewriter/class-rewriter-4.120.0.jar
 * Qualified Name:     com.newrelic.agent.Obfuscation.Proguard
 * JD-Core Version:    0.6.2
 */