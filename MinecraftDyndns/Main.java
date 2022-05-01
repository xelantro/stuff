package de.xelantro.minecraft;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.NoSuchElementException;

public class Main {
	public static void main(String[] argssss) throws IOException, InterruptedException, URISyntaxException {
		System.out.println("Syntax: (java 8) java -jar MinecraftPortUpdater.jar \"title-of-minecraft-window,path-of-java-11-installation,optional-dyndns-update-url-with-ip-as-~s\"\n"
				+"      Example of Arguments: \"'All the Mods 3','C:/Program Files/Java/jdk-11.0.1/','http://xelantro.de:password@dyndns.strato.com/nic/update?system=dyndns&hostname=test.xelantro.de&myip=~s'\"\n"
				+"Make sure you got the portmapper-2.2.0.jar in the same folder as the MinecraftPortUpdater.jar!");

		String[] args = argssss[0].split(",");
		args[2]=args[2].replace("~", "%");
		//DEFAULT
		String windowtitle = (args.length>0)?args[0].substring(1,args[0].length()-1):"All the Mods 3";
		String javapath = (args.length>1)?args[1].substring(1,args[1].length()-1)+(!args[1].endsWith("/")?"/":""):"C:/Program Files/Java/jdk-11.0.1/";
		//DEFAULT
		try {
			String pid = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("tasklist /FI \"WINDOWTITLE eq "+windowtitle+"\" /FI \"IMAGENAME eq javaw.exe\" /FO:csv /NH").getInputStream()))
					.lines().findFirst().get().split("\"",5)[3];
			System.out.println("Minecraft Process detected - PID: "+pid);
			String port = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("netstat -ano -p tcp").getInputStream()))
					.lines().skip(4).map(s->s.replaceAll("\\s{2,}", " ").trim().split(" ")).filter(a->a[4].equals(pid)&&a[2].equals("0.0.0.0:0"))
					.map(a->a[1].substring(8)).findFirst().get();
			System.out.println("LAN Server detected - Port: "+port);
			System.out.println("Starting UPnP Engine...");
			Runtime.getRuntime().exec("\""+javapath+"bin/java.exe\" -jar portmapper-2.2.0.jar -add -externalPort 25565 -internalPort "+port+" -protocol tcp");
			Thread.sleep(1000);
			System.out.println("Forwarding complete!");
			if(args.length<3) {System.exit(0);}
			String extIP = new BufferedReader(new InputStreamReader(new URL("https://memium.de/ip.php").openStream())).readLine();
			System.out.println("Externeal IP: "+extIP);
			System.out.println("Updating DynDns...");
			String URI = String.format(args[2].substring(1,args[2].length()-1), extIP);
			Desktop.getDesktop().browse(new URI(URI));
			System.out.println(URI);

		} catch (NoSuchElementException e) {
			System.err.println("Minecraft hasn't started or wasn't opened to LAN yet!");
		}
		//		new BufferedReader(new InputStreamReader(.getInputStream()))
		//		.lines().forEach(System.out::println);
		/*.forEachOrdered(a->{for(String s:a) {System.out.print(s+"-");}System.out.println();});*/
	}
	//ele: 6 (int port) + 12 (ext Addr) + 18 (stat) + 23 (pid)
	//substrings: 2 proto, 9 int ip port, 32 ext ip, 54 stat, 70 pid
	//arr: 0 proto, 1 int ip port, 2 ext ip port, 3 stat, 4 pid
}
