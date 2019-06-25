import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class LeagueFunctions {
    //  Need to find a way to update key automatically
    static String riotKey = tokens.riotAPIKey;

    //  URL's to get summoner info through Riot API
    static String summonerV4 = "https://na1.api.riotgames.com/lol/summoner/v4/summoners/by-name/";
    static String leagueV4 = "https://na1.api.riotgames.com/lol/league/v4/entries/by-summoner/";
    static String champMasteryV4 = "https://na1.api.riotgames.com/lol/champion-mastery/v4/champion-masteries/by-summoner/";
    static String matchV4 = "https://na1.api.riotgames.com/lol/match/v4/matchlists/by-account/";
    static String matchInfoV4 = "https://na1.api.riotgames.com/lol/match/v4/matches/";

    //  Update later to get current patch link
    static String champList = "https://ddragon.leagueoflegends.com/cdn/9.12.1/data/en_US/champion.json";

    /*  Calls 3 functions to help collect info on summoner
     *  Returns info as a string to caller
     */
    public static String getSummonerInfo(String summoner){
        //  Formats summoner name correctly for URL usage
        String name = summoner;
        name = name.replace(" ", "%20");

        String accountID = "";

        String urlString, tempString;
        String temp[];

        String level;

        String info = "";

        //  Calls function to retrieve account ID and level
        urlString =  name + "?api_key=";
        temp = getSummonerAccountInfo(urlString);
        switch (temp[0]) {
            case "Error":
                return "Summoner was not found.";
            default:
                level = temp[1];
                accountID = temp[2];

                info = "Summoner: " + summoner + "\n" +
                        "Level: " + level + "\n";
        }

        //  Calls function to retrieve tier and rank info
        urlString = accountID + "?api_key=";
        tempString = getSummonerRankInfo(urlString);
        switch (tempString) {
            case "Error":
                return "Summoner was not found.";
            default:
                info = info + tempString + "\n";
        }

        //  Calls function to retrieve info about summoners mastery on their top 3 champs
        urlString = accountID + "?api_key=";
        tempString = getSummonerChampInfo(urlString);
        switch (temp[0]) {
            case "Error":
                return "Summoner was not found.";
            default:
                info = info + tempString + "\n";
        }

        return info;
    }

    /** Gets summoner account info
     *  @return data as String array
     *      String[0] = name
     *      String[1] = level
     *      String[2] = account id
     *      String[3] = summoner id
     *  @error
     *      If there is a problem reading data,
     *      String[0] = "error"
     */
    private static String[] getSummonerAccountInfo(String urlString) {
        String accountInfo[] = new String[4];
        urlString = summonerV4 + urlString + riotKey;

        try {
            URL url = new URL(urlString);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));

            String line = bufferedReader.readLine();
            bufferedReader.close();

            // Name
            String temp[] = line.split("\"name\":\"");
            accountInfo[0] = temp[1].substring(0, temp[1].indexOf("\""));

            // Level
            temp = line.split("\"summonerLevel\":");
            accountInfo[1] = temp[1].substring(0, temp[1].indexOf("}"));

            // Account id
            temp = line.split("\"accountId\":\"");
            accountInfo[2] = temp[1].substring(0, temp[1].indexOf("\""));

            // Summoner id
            temp = line.split("\"id\":\"");
            accountInfo[3] = temp[1].substring(0, temp[1].indexOf("\""));
        }
        catch (MalformedURLException e){
            accountInfo[0] = "Error";
        }
        catch (IOException e) {
            accountInfo[0] = "Error";
        }

        return accountInfo;
    }

    /*  Gets rank and tier
     *  Returns as formatted string of rank list
     */
    private static String getSummonerRankInfo(String urlString) {
        String info = "";
        String queueType, tier, division, lp;

        urlString = leagueV4 + urlString + riotKey;

        try {
            URL url = new URL(urlString);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));

            String line = bufferedReader.readLine();
            bufferedReader.close();

            String tempString = line;

            //  Counts how many queue summoner currently has a rank in
            int count = 0;
            int index = tempString.indexOf("queueType");
            while (index != -1) {
                count++;
                tempString = tempString.substring(index + 1);
                index = tempString.indexOf("queueType");
            }

            //  If summoner is unranked
            if (count == 0)
                return "Unranked \n";
                //  If summoner has at least one rank
            else {
                tempString = line;

                //  Gets all the queue ranks
                for (int i = 0; i < count; i++) {
                    //  Gets queue type
                    String temp[] = tempString.split("\"queueType\":\"");
                    queueType = temp[1].substring(0, temp[1].indexOf("\""));

                    //  Gets tier
                    temp = tempString.split("\"tier\":\"");
                    tier = temp[1].substring(0, temp[1].indexOf("\""));

                    //  Gets division
                    temp = tempString.split("\"rank\":\"");
                    division = temp[1].substring(0, temp[1].indexOf("\""));

                    //  Gets lp
                    temp = tempString.split("\"leaguePoints\":");
                    lp = temp[1].substring(0, temp[1].indexOf(","));

                    info = info + queueType + ": " + tier + " " + division + " " + lp + "LP " + "\n";

                    index = tempString.indexOf("}");
                    tempString = tempString.substring(index + 1);
                }
            }
        }
        catch (MalformedURLException e){
            return "Error";
        }
        catch (IOException e) {
            return "Error";
        }

        return info;
    }

    /*  Gets summoner's top 3 champs and mastery points
     *  Returns as formatted string of list of champs
     */
    private static String getSummonerChampInfo(String urlString) {
        String info = "";
        String champID[] = new String[3];
        String champMP[] = new String[3];

        urlString = champMasteryV4 + urlString + riotKey;

        //  Gets Riot data about top 3 champs
        try {
            URL url = new URL(urlString);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));

            String line = bufferedReader.readLine();
            bufferedReader.close();

            String tempString = line;

            //  Parse the JSON data given from Riot API
            int index = 0;
            for (int i = 0; i < 3; i++) {
                index = tempString.indexOf("championID\":");
                index = tempString.indexOf(":", index);
                //  Get value of champion id
                champID[i] = tempString.substring(index + 1, tempString.indexOf(",", index));

                index = tempString.indexOf("championPoints\":");
                index = tempString.indexOf(":", index);
                //  Get value of champion mastery points
                champMP[i] = tempString.substring(index + 1, tempString.indexOf(",", index));

                //  Get string ready for next search
                tempString = tempString.substring(tempString.indexOf("}", index) + 1);
            }
        }
        catch (MalformedURLException e){
            return "Error";
        }
        catch (IOException e) {
            return "Error";
        }

        //  Gets name of champions from id's
        String champNames[] = new String[3];
        try {
            URL url = new URL(champList);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));

            String line = bufferedReader.readLine();
            bufferedReader.close();

            String tempString = line;
            int index = 0;

            for (int i = 0; i < 3; i++) {
                String searchTerm = "key\":\"" + champID[i] + "\"";

                index = tempString.indexOf(searchTerm);
                index = tempString.indexOf("name\":\"", index);
                index += 7;
                champNames[i] = tempString.substring(index, tempString.indexOf("\"", index));
            }
        }
        catch (MalformedURLException e){
            return "Error";
        }
        catch (IOException e) {
            return "Error";
        }

        //  Add champ names and mastery points to info string
        for (int i = 0; i < 3; i++)
            info = info + champNames[i] + " - " + champMP[i] + "\n";

        return info;
    }

    /**
     * Gets win rate for past 10 games.
     * @param summoner
     * @return
     *     returns as a string with win rate and some text.
     */
    public static String isSummonerTilted(String summoner) {
        int numberOfMatches = 10;
        String name = summoner;
        name = name.replace(" ", "%20");

        String urlString =  name + "?api_key=";

        String accountID;

        //  Gets account id
        String temp[] = getSummonerAccountInfo(urlString);
        switch (temp[0]) {
            case "Error":
                return "Summoner was not found.";
            default:
                name = temp[0];
                accountID = temp[2];
                urlString = accountID + "?api_key=";
        }

        //  Gets match history game id's
        String matchId[] = getMatchID(urlString, numberOfMatches);
        if (matchId[0] == "Error")
            return "Error getting match id's.";

        //  Gets match info from game id's
        boolean matchWon[] = new boolean[numberOfMatches];
        for (int i = 0; i < numberOfMatches; i++) {
            matchWon[i] = wasMatchWon(matchId[i], name);

        }

        //  Calculates win ratio
        int wins = 0;
        System.out.println("Wins = " + wins);
        for (int i = 0; i < numberOfMatches; i++) {
            System.out.println("matchWon[" + i + "] = " + matchWon[i]);
            if (matchWon[i] == true) {
                wins++;
                System.out.println("Wins = " + wins);
            }
        }

        double ratio = (double) wins / (double) numberOfMatches;
        ratio *= 100;

        String info = ratio + "% win rate over the past 10 games.";
        return info;
    }

    /**
     * Gets match id's of past numberOfMatches games.
     * @param urlString
     * @param numberOfMatches
     * @return
     */
    private static String[] getMatchID(String urlString, int numberOfMatches) {
        urlString = matchV4 + urlString + riotKey;

        String matchId[] = new String[numberOfMatches];

        try {
            URL url = new URL(urlString);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));

            String line = bufferedReader.readLine();
            bufferedReader.close();

            String tempLine = line;
            int index;

            //  Gets game id's of past numberOfMatches games
            for (int i = 0; i < numberOfMatches; i++) {
                index = tempLine.indexOf("gameId\":");
                index = tempLine.indexOf(":", index + 1);
                matchId[i] = tempLine.substring(index + 1, tempLine.indexOf(",", index + 2));

                //  Gets the line ready for next search
                tempLine = tempLine.substring(index);
            }
        }
        catch (MalformedURLException e){
            matchId[0] = "Error";
        }
        catch (IOException e) {
            matchId[0] = "Error";
        }

        return matchId;
    }

    /**
     * Gets the result of a match given the matchId
     * @param matchId
     * @param name
     * @return
     *     returns a boolean for a win or lose
     */
    private static boolean wasMatchWon(String matchId, String name) {
        String urlString = matchInfoV4 + matchId + "?api_key=" + riotKey;
        boolean wasWon = false;

        // only takes into account SR not TT
        try {
            URL url = new URL(urlString);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));

            String line = bufferedReader.readLine();
            bufferedReader.close();

            //  Finding participant id
            int participantID = 0;
            String tempString = line;
            int index = tempString.indexOf(name);
            index = tempString.indexOf("participantId", index);
            index = tempString.indexOf(":", index);

            if (index == -1)
                participantID = 10;
            else
                participantID = Integer.valueOf(tempString.substring(index + 1, tempString.indexOf(",", index))) - 1;

            String searchTerm;
            //  Team 1
            if (participantID < 6)
                searchTerm = "teamId\":100";
            //  Team 2
            else
                searchTerm = "teamId\":200";

            //  Find if summoner's team won
            index = tempString.indexOf(searchTerm);
            index = tempString.indexOf(",", index);
            index = tempString.indexOf(":", index);

            String word = tempString.substring(index + 2, tempString.indexOf("\"", index + 3));
            //  Was a win
            if (word.equals("Win"))
                return true;
            //  Was a loss
            else
                return false;

        }
        catch (MalformedURLException e){
            return true;
        }
        catch (IOException e) {
            return true;
        }
    }
}
