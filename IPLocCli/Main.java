import javax.imageio.ImageIO;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

public class Main {
	private void go(String inpath, String outpath, String ownIP, int maxVal) throws IOException {
		BufferedReader r = new BufferedReader(new FileReader(inpath));
		String date = Arrays.asList(r.readLine().split(" ")).stream().filter(e->!e.isEmpty()).limit(3).collect(Collectors.joining(" "));
		DefaultPieDataset dataset = new DefaultPieDataset();
		IntWrapper other = new IntWrapper(-maxVal); //max eintraege im chart + ges eintraege in der other Kategorie
		long gesammt = r.lines()
				.map(l -> l.replaceFirst("(.+?)(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})(.*)", "$2"))
				.filter(i -> i.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"))
				.filter(i -> i!= ownIP)
				//.peek(System.out::println)
				.distinct()
				.map(i -> { try {
					return new BufferedReader(new InputStreamReader(new URL("https://api.memium.de/ip?ip="+i).openStream()))
							.readLine().split(",")[7].substring(15);
				} catch (Exception e) {return "";}})
				.filter(i->!i.isEmpty())
				.map(i->i.substring(1,i.length()-1))
				//.peek(System.out::println)
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
				.entrySet().stream()
				.sorted((v1, v2)->(int)(v2.getValue()-v1.getValue()))
				.filter(e->{
					if(other.val<=0) { other.increment(); return true; }
					else { other.val+=e.getValue(); return false; }
				})
				.peek(e->dataset.setValue(e.getKey()+": "+e.getValue(), e.getValue()))
				.reduce(new Long(0), (in, val)->in+=val.getValue(), (i1, i2)->i1+i2);
		r.close();
		dataset.setValue("Others: "+other.val, other.val);

		JFreeChart chart = ChartFactory.createPieChart("IP logs (unique requests) since "+date+"      Σ"+gesammt,dataset,false,true,false);
		chart.setBackgroundPaint(new Color(255, 255, 255));
		chart.setAntiAlias(true);
		chart.setTextAntiAlias(true);

		ImageIO.write(chart.createBufferedImage(1200, 1000), "png", new File(outpath));
	}

	private void uglygo(String inpath, String outpath, String ownIP, int maxVal) throws IOException {
		BufferedReader r = new BufferedReader(new FileReader(inpath));
		String date = Arrays.asList(r.readLine().split(" ")).stream().filter(e->!e.isEmpty()).limit(3).collect(Collectors.joining(" "));
		DefaultPieDataset dataset = new DefaultPieDataset();
		IntWrapper other = new IntWrapper(-maxVal); //max eintraege im chart + ges eintraege in der other Kategorie
		long gesammt = r.lines()
				.map(l -> l.replaceFirst("(.+?)(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})(.*)", "$2"))
				.filter(i -> i.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"))
				.filter(i -> i!= ownIP)
				//.peek(System.out::println)
				.map(i -> { try {
					return new BufferedReader(new InputStreamReader(new URL("https://api.memium.de/ip?ip="+i).openStream()))
							.readLine().split(",")[7].substring(15);
				} catch (Exception e) {return "";}})
				.filter(i->!i.isEmpty())
				.map(i->i.substring(1,i.length()-1))
				//.peek(System.out::println)
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
				.entrySet().stream()
				.sorted((v1, v2)->(int)(v2.getValue()-v1.getValue()))
				.filter(e->{
					if(other.val<=0) { other.increment(); return true; }
					else { other.val+=e.getValue(); return false; }
				})
				.peek(e->dataset.setValue(e.getKey()+": "+e.getValue(), e.getValue()))
				.reduce(new Long(0), (in, val)->in+=val.getValue(), (i1, i2)->i1+i2);
		r.close();
		dataset.setValue("Others: "+other.val, other.val);

		JFreeChart chart = ChartFactory.createPieChart("IP logs (total requests) since "+date+"      Σ"+gesammt,dataset,false,true,false);
		chart.setBackgroundPaint(new Color(255, 255, 255));
		chart.setAntiAlias(true);
		chart.setTextAntiAlias(true);

		ImageIO.write(chart.createBufferedImage(1200, 1000), "png", new File(outpath));
	}


	class IntWrapper {
		private int val;
		public IntWrapper(int val) { this.val=val; }
		public void increment() { val++; }
	}

	public static void main(String[] args) {
		//Arrays.asList(args).forEach(System.out::println);
		try {
			String infile, outfile, ownIP; int max; 
			switch(args.length) {
			case 1: 
				if(args[0].equals("default")) {
					infile = "/var/log/ufw.log";
					outfile = "/var/www/html/ipchart.png";
					max = 10;
					ownIP = getOwnIP();
				} else throw new SyntaxException();
				break;
			case 2: 
				infile = args[0];
				outfile = args[1];
				max = 10;
				ownIP = getOwnIP();
				break;
			case 3:
				infile = args[0];
				outfile = args[1];
				max = Integer.parseInt(args[2]);;
				ownIP = getOwnIP();
				break;
			case 4:
				infile = args[0];
				outfile = args[1];
				max = Integer.parseInt(args[2]);
				ownIP = args[3];
				break;
			default:
				throw new SyntaxException();
			}

			if(!infile.endsWith(".log")) throw new SyntaxException("Please select a .log file as first argment");
			if(!outfile.endsWith(".png")) throw new SyntaxException("Please select a .png file as second argument");
			if(max < 0) throw new SyntaxException("Thrid argument must be positive!");
			if(!ownIP.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")) throw new SyntaxException("Please select an IP as fourth argument");

			//System.out.println(infile+", "+outfile+", "+max+", "+ownIP);
			new Main().go(infile, outfile, ownIP, max);

			String outfile2 = outfile.substring(0, outfile.length()-4)+"-total.png";
			new Main().uglygo(infile, outfile2, ownIP, max);

			System.out.println("Done");

		} catch (SyntaxException e) {
			System.err.println((e.getMessage()==null)?"Arguments: infile outfile [number-of-displayed-entries [own-ip] ]":e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	static String getOwnIP() throws MalformedURLException, IOException {
		return new BufferedReader(new InputStreamReader(new URL("https://api.memium.de/ip").openStream()))
				.readLine().split(",")[0].replaceAll(".*(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}).*", "$1");
	}

	static class SyntaxException extends Exception {
		private static final long serialVersionUID = 7126841404859812876L;
		public SyntaxException() {}
		public SyntaxException(String msg) {
			super(msg);
		}
	}
}