package de.xelantro.teleInfo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;
import java.util.stream.Collectors;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class Main {
	public Main(String path, String outpath) throws IOException {
		this.createtable(outpath, this.readandcompute(path));
	}

	private ArrayList<String[]> readandcompute(String path) throws IOException {
		//OutputArr time, countTyp1, countTyp4,
		Collector<String[],?,String[]> collector = Collector.of(
				(Supplier<String[]>)()->new String[] {"","0","0"},
				(BiConsumer<String[], String[]>)(res,ele)->{
					String[] time=ele[6].split(":");
					res[0]=""+(Integer.parseInt(time[0])*60+Integer.parseInt(time[1]));
					if (ele[0]=="1") {res[1]=""+(Integer.parseInt(res[1])+1);}
					else res[2]=""+(Integer.parseInt(res[2])+1);}, 
				(BinaryOperator<String[]>)(res,res2)->{
					res[0]=""+(Integer.parseInt(res[0])+Integer.parseInt(res2[0]));
					res[1]+=res2[1];
					res[2]+=res2[2];
					return res;},
				Characteristics.UNORDERED);
		BufferedReader r = new BufferedReader(new FileReader(path));
		Map<String, String[]> csv = r.lines()
				.skip(2)
				.map(l->l.split(";"))
				.filter(a->a[0].matches("[14]"))
				.collect(Collectors.groupingBy((String[] a)->{return (a[3].isEmpty())?a[2]:a[3];},collector));
		r.close();
		ArrayList<String[]> out = new ArrayList<String[]>(csv.size());
		csv.forEach((key,val)->out.add(new String[] {key,val[0],val[1],val[2]}));
		out.forEach(a->{System.out.println("");for(String s:a) {System.out.print(s+", ");}});
		return out;
	}

	private void createtable(String outpath, ArrayList<String[]> data) throws FileNotFoundException {
		Document doc = new Document();
		try {
			PdfWriter.getInstance(doc, new FileOutputStream(outpath));
			doc.open();

			PdfPTable table = new PdfPTable(4);
			table.addCell("Nummer");
			table.addCell("Anz. ausgehende Anrufe");
			table.addCell("Anz. eingehende Anrufe");
			table.addCell("Gesammte Gesprächszeit");

			PdfPCell dCell = table.getDefaultCell();
			dCell.setBorder(Rectangle.BOX);
			dCell.setBorderColor(BaseColor.GRAY);
			dCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			dCell.setPaddingRight(4);

			data.forEach(l->{
				table.addCell(l[0]);table.addCell(l[2]);table.addCell(l[3]);table.addCell(l[1]);
				table.completeRow();
			});
			table.setWidthPercentage(85);
			table.setWidths(new float[] {2f,1f,1f,2f});

			doc.add(table);
		} catch (DocumentException e) {e.printStackTrace();}
		finally {doc.close();}
	}
}
