package assignment_3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class RobotParser {
	final static String DISALLOW = "Disallow";

	public static void main(String[] args) throws MalformedURLException {
		// TODO Auto-generated method stub

		//String url = "http://timesmachine.nytimes.com/timesmachine/1920/10/16/102902890.html";
		String url ="en.wikipedia.org/wiki/Military_history_of_the_United_States_during_World_War_II";
		System.out.println(robotSafe(url));
	}

	public static boolean robotSafe(String urlString) throws MalformedURLException {
		
		URL urlRobot;
		String strHost = "";
		try {
			URL url = new URL(urlString);
			strHost = url.getHost();
			String strRobot = url.getProtocol()+"://" + strHost + "/robots.txt";
			urlRobot = new URL(strRobot);
			
		} catch (MalformedURLException e) {
			return true;
		}
		
		StringBuilder strCommands= new StringBuilder();
		
		try {			
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(urlRobot.openStream()));
			
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains("User-agent: *")) {
					if (line.indexOf("Disallow:") == 0) {
						String disallowPath = line.substring("Disallow:"
								.length());

						// Check disallow path for comments and
						// remove if present.
						int commentIndex = disallowPath.indexOf("#");
						if (commentIndex != -1) {
							disallowPath = disallowPath.substring(0,
									commentIndex);
						}

						// Remove leading or trailing spaces from
						// disallow path.
						disallowPath = disallowPath.trim();
						//System.out.println(disallowPath);
						strCommands.append(disallowPath);
						// Add disallow path to list.
					}
				}
			}
			
		} catch (IOException e) {
			return true; // if there is no robots.txt file, it is OK to search
		}

			String[] split = strCommands.toString().split("\n");
			ArrayList<RobotRule> robotRules = new ArrayList<>();
			String mostRecentUserAgent = null;
			for (int i = 0; i < split.length; i++) {
				String line = split[i].trim();
				if (line.toLowerCase().startsWith("user-agent")) {
					int start = line.indexOf(":") + 1;
					int end = line.length();
					mostRecentUserAgent = line.substring(start, end).trim();
				} else if (line.startsWith(DISALLOW)) {
					if (mostRecentUserAgent != null && mostRecentUserAgent.contains("*")) {
						RobotRule r = new RobotRule();
						r.userAgent = mostRecentUserAgent;
						int start = line.indexOf(":") + 1;
						int end = line.length();
						r.rule = line.substring(start, end).trim();
						robotRules.add(r);
						//System.out.println(r.rule);
					}
				}
			}

			for (RobotRule robotRule : robotRules) {
				if (urlString.contains(strHost+robotRule.rule))
					return false;
			}
			return true;
	}
	
}


class RobotRule 
{
    public String userAgent;
    public String rule;

    RobotRule() {

    }

    @Override public String toString() 
    {
        StringBuilder result = new StringBuilder();
        String NEW_LINE = System.getProperty("line.separator");
        result.append(this.getClass().getName() + " Object {" + NEW_LINE);
        result.append("   userAgent: " + this.userAgent + NEW_LINE);
        result.append("   rule: " + this.rule + NEW_LINE);
        result.append("}");
        return result.toString();
    }    
}