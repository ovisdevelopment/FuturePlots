package ovis.futureplots.components.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import ovis.futureplots.FuturePlots;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {

    private final String GITHUB_API_URL_TAGS = "https://api.github.com/repos/ovisdevelopment/FuturePlots/tags";
    private final String GITHUB_API_URL_LATEST_RELEASE = "https://api.github.com/repos/ovisdevelopment/FuturePlots/releases/latest";
    private final String GITHUB_API_URL_COMMITS = "https://api.github.com/repos/ovisdevelopment/FuturePlots/commits";

    private String version;
    private String commitHash;
    private String date;

    private final boolean devBuilds;

    @Getter
    private boolean updateAvailable = false;

    public UpdateChecker(boolean devBuilds) {
        this.devBuilds = devBuilds;
        try (InputStream stream = UpdateChecker.class.getResourceAsStream("/plugin.properties")) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
                this.version = br.readLine().split("=")[1];
                this.commitHash = br.readLine().split("=")[1];
                this.date = br.readLine().split("=")[1];
            }
        } catch (IOException throwable) {
            if(FuturePlots.getSettings().isDebugEnabled()) {
                throwable.printStackTrace();
            }
        }
    }

    public void check() {
        if(this.version == null || this.commitHash == null || this.date == null) {
            FuturePlots.getInstance().getLogger().error("An error occurred. Unable to check for further updates.");
            return;
        }
        FuturePlots.getInstance().getLogger().info("Checking for available updates...");
        try {
            if (this.devBuilds) {
                String latestCommitHash = getLatestCommitHash();
                if (latestCommitHash.equals(this.commitHash)) {
                    FuturePlots.getInstance().getLogger().info("You have the latest unofficial version of FuturePlots installed.");
                } else {
                    this.updateAvailable = true;
                    FuturePlots.getInstance().getLogger().warning("----------");
                    FuturePlots.getInstance().getLogger().warning("An update is available!");
                    FuturePlots.getInstance().getLogger().warning("Current commit: " + this.commitHash);
                    FuturePlots.getInstance().getLogger().warning("Newest commit: " + latestCommitHash);
                    FuturePlots.getInstance().getLogger().warning("----------");
                }
            } else {
                String[] latestRelease = getLatestRelease();
                String latestReleaseTag = latestRelease[0];
                String latestReleaseUrl = latestRelease[1];
                String tag = getGitTagFromCommitHash(this.commitHash);
                if (tag != null) {
                    if (tag.equals(latestReleaseTag)) {
                        FuturePlots.getInstance().getLogger().info("You have the latest version of FuturePlots installed.");

                    } else {
                        this.updateAvailable = true;
                        FuturePlots.getInstance().getLogger().warning("----------");
                        FuturePlots.getInstance().getLogger().warning("An update is available!");
                        FuturePlots.getInstance().getLogger().warning("Current version: " + tag);
                        FuturePlots.getInstance().getLogger().warning("Newest version: " + latestReleaseTag);
                        FuturePlots.getInstance().getLogger().warning("Download: " + latestReleaseUrl);
                        FuturePlots.getInstance().getLogger().warning("----------");
                    }
                } else {
                    FuturePlots.getInstance().getLogger().error("The release tag could not be retrieved. Is an unofficial version possibly being used?");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getGitTagFromCommitHash(String commitHash) {
        try {
            String urlString = String.format(GITHUB_API_URL_TAGS);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JsonArray tags = JsonParser.parseString(response.toString()).getAsJsonArray();

            for (JsonElement element : tags) {
                JsonObject tag = element.getAsJsonObject();
                String sha = tag.getAsJsonObject("commit").get("sha").getAsString();

                if (sha.equals(commitHash)) {
                    return tag.get("name").getAsString();
                }
            }
        } catch (Exception e) {
            // nothing
        }
        return null;
    }

    private String[] getLatestRelease() {
        try {
            String urlString = String.format(GITHUB_API_URL_LATEST_RELEASE);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JsonObject release = JsonParser.parseString(response.toString()).getAsJsonObject();
            String tag = release.get("tag_name").getAsString();
            String urlRelease = release.get("html_url").getAsString();

            return new String[]{tag, urlRelease};
        } catch (Exception e) {
            // nothing
        }
        return new String[]{"", ""};
    }


    private String getLatestCommitHash() {
        try {
            String urlString = String.format(GITHUB_API_URL_COMMITS);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JsonArray commits = JsonParser.parseString(response.toString()).getAsJsonArray();
            JsonObject latestCommit = commits.get(0).getAsJsonObject();
            String latestCommitHash = latestCommit.get("sha").getAsString();

            return latestCommitHash;
        } catch (Exception e) {
            // nothing
        }
        return null;
    }
}
