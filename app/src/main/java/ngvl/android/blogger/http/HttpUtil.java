package ngvl.android.blogger.http;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpUtil {


    public static boolean hasConnectionAvailable(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        InputStream stream = conn.getInputStream();
        return stream;
    }

    public static String streamToString(InputStream is) throws IOException {

        byte[] bytes = new byte[1024];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int lidos;
        while ((lidos = is.read(bytes)) > 0) {
            baos.write(bytes, 0, lidos);
        }
        return new String(baos.toByteArray());
    }

    public static String formatHtml(Context ctx, String title, String html) {
        String newHtml = html;

        try {
            // searching for youtube iframe tags
            boolean findAgain;
            do {
                findAgain = false;
                // finding first <iframe> tag
                int iframeStartIndex = newHtml.indexOf("<iframe");

                // if found...
                if (iframeStartIndex > -1) {

                    // find the close tag </iframe>
                    String iframeCloseTag = "</iframe>";
                    int iframeCloseTagLength = iframeCloseTag.length();
                    int iframeEndIndex = newHtml.indexOf(iframeCloseTag, iframeStartIndex);
                    if (iframeEndIndex == -1) {
                        iframeEndIndex = newHtml.indexOf("/>", iframeStartIndex);
                        iframeCloseTagLength = 0;
                    }

                    // get iframe complete tag
                    String iframeTag = newHtml.substring(iframeStartIndex, iframeEndIndex + iframeCloseTagLength);

                    // if the iframe tag has youtube...
                    if (iframeTag.contains("youtube")) {

                        // finding video id
                        String pattern = "(?:embed\\/|v=)([\\w-]+)";

                        try {
                            Pattern compiledPattern = Pattern.compile(pattern);
                            Matcher matcher = compiledPattern.matcher(iframeTag);

                            if (matcher.find()) {
                                String videoId = matcher.group(1);

                                String videoLink =
                                        "<a href='http://www.youtube.com/watch?v=" + videoId + "'>" +
                                                "<img src='http://img.youtube.com/vi/" + videoId + "/0.jpg'>" +
                                                "</a>";

                                newHtml = newHtml.replace(iframeTag, videoLink);
                                findAgain = true;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } while (findAgain);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String template = streamToString(ctx.getAssets().open("template.xml"));
            newHtml = template.replaceAll("#TITLE#", title).replaceAll("#CONTEUDO#", newHtml);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return newHtml;
    }
}
