package ste.app.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(
name = "Call",
urlPatterns = "/call", 
loadOnStartup = -1,
initParams = { 
		@WebInitParam(name = "urltocall", description="URL", value = "https://www.google.it")
		}
)
public class Call extends HttpServlet
{
	private static final long serialVersionUID = 2553000367973521799L;
	
    private String urlToCall = null;
    private final Pattern urlRegExp = Pattern.compile("https?://([^/]+)(/.+)?");

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        System.out.println("Entro nel metodo");
        System.out.println("Call.doGet(): start");
        String dummy = request.getParameter("forceHost");
        String destinationfromUrl=request.getParameter("url");
        boolean forceHost = (dummy != null) && (dummy.equalsIgnoreCase("true"));
        System.out.println("Call.doGet(): forceHost = " + forceHost);
        System.out.println("Url:" + destinationfromUrl);
        PrintWriter out = response.getWriter();

        Matcher matcher = this.urlRegExp.matcher(destinationfromUrl);
        if (matcher.matches())
        {
            double start = System.nanoTime();
            try
            {
                System.out.println("Call.doGet(): The url to call " + destinationfromUrl + " is valid");
                URL url = new URL(destinationfromUrl);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(30000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setRequestProperty("Accept-Encoding", "identity");
                conn.setRequestProperty("Accept", "*");

                if (forceHost)
                {
                    String host = matcher.group(1);
                    System.out.println("Call.doGet(): host = " + host);
                    if (host != null) conn.setRequestProperty("Host", host);
                }

                System.out.println("Call.doGet(): Connection to the server ...");
                conn.connect();
                System.out.println("Call.doGet(): Waiting for the server response ...");
                int responseCode = conn.getResponseCode();
                System.out.println("Call.doGet(): responseCode = " + responseCode);
                double elapsed = (System.nanoTime() - start) / 1000000.0D;
                System.out.println(String.format("Call.doGet(): Elapsed = %.3f ms", new Object[] { Double.valueOf(elapsed) }));

                if (responseCode == 200)
                {
                    System.out.println("Call.doGet(): ********************************************");
                    InputStream in = conn.getInputStream();
                    byte[] b = new byte[16384];
                    int bytes = 0;
                    while ((bytes = in.read(b)) != -1) out.write(new String(b, 0, bytes));
                    out.println();
                    System.out.println("Call.doGet(): ********************************************");
                }
                else
                {
                    out.println("<html><head><title>wget</title></head><body>");
                    out.println("ERROR: responseCode = " + responseCode);
                    out.println("</body></html>");
                }

                conn.disconnect();
            }
            catch (Exception err)
            {
                System.out.println(String.format("Call.doGet(): ERROR: %S [%S]", new Object[] { err.getMessage(), err.getClass().getName() }));
                double elapsed = (System.nanoTime() - start) / 1000000.0D;
                System.out.println(String.format("Call.doGet(): Elapsed = %.3f ms", new Object[] { Double.valueOf(elapsed) }));
                out.println("<html><head><title>wget</title></head><body>");
                out.println("ERROR: " + err.getMessage());
                out.println("</body></html>");
                err.printStackTrace();
            }
        }

        System.out.println("Call.doGet(): end");
    }

    public void init(ServletConfig config)
            throws ServletException
    {
        System.out.println("Call.init(): start");
        this.urlToCall = config.getInitParameter("urltocall");
        System.out.println("Call.init(): urlToCall = " + this.urlToCall);
        System.out.println("Call.init(): end");
    }
}