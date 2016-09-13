package assignment_7;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public class Util {
	private static String IP_ADDRESS="localhost";
	public static Client transportClientBuilder() {
		Client client = null;
		try {
			Settings settings = Settings.settingsBuilder().build();
			client = TransportClient.builder().settings(settings).build()
					.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(IP_ADDRESS), 9300));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return client;
	}
	
	public static void writeToFile(String fileName,String contentToFile) {

		String outputFile = fileName;

		File file = new File(outputFile);
			try {
				//if file doesnt exists, then create it
				if (!file.exists()) 
					file.createNewFile();
				FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(contentToFile);
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}


	
}
