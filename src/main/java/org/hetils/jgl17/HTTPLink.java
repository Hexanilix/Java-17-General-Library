package org.hetils.jgl17;

import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPLink {
    public HTTPLink() {}

    public @Nullable File downloadFile(String fileURL, String destF) {
        HttpURLConnection http = null;
        int code;
        try {
            http = (HttpURLConnection) new URL(fileURL).openConnection();
        } catch (IOException e) {
            System.out.println("Improper URL \"" + fileURL + "\", reason: " + e.getMessage());
        }
        try {
            code = http.getResponseCode();
        } catch (IOException e) {
            System.out.println("Improper URL \"" + fileURL + "\", reason: " + e.getMessage());
            return null;
        }
        if (code != HttpURLConnection.HTTP_OK) {
            System.out.println("No file to download. Server replied with HTTP code: " + code);
            return null;
        }
        try (InputStream inS = http.getInputStream(); FileOutputStream out = new FileOutputStream(destF);
             BufferedInputStream in = new BufferedInputStream(inS)) {
            byte[] buffer = new byte[4096];
            int byter;
            while ((byter = in.read(buffer)) != -1) {
                out.write(buffer, 0, byter);
            }
            System.out.println("File downloaded: " + destF);
        } catch (IOException e) {
            e.printStackTrace();
        }
        http.disconnect();
        return new File(destF);
    }
}
