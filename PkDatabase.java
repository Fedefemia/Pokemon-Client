import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PkDatabase {

    public static void Main() throws IOException {
        System.out.println("Powered by PokeApi");

        String res = Send(23);
        System.out.println(res);

    }

    private static String Send(int pkid) throws IOException {
        String tosend = "https://pokeapi.co/api/v2/pokedex/" + pkid;
        URL url = new URL(tosend);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        
        return inputLine;

    }
}