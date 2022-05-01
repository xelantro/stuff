package de.xelantro.teleInfo;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.TransferHandler;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class UI extends JFrame{
	private static final long serialVersionUID = -7860027026830226652L;
	private String inPath;

	private JFileChooser outChoose;

	public UI() {
		this.setMinimumSize(new Dimension(300,400));
		this.setTitle("Anrufliste");
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		JPanel drop = new JPanel();
		drop.setMinimumSize(new Dimension(0,200));
		drop.setTransferHandler(new FileDropHandler());
		drop.add(new JLabel("Drop csv here."));
		drop.setBorder(BorderFactory.createBevelBorder(0));
		this.add(drop);
		outChoose = new JFileChooser();
		outChoose.setFileFilter(new FileNameExtensionFilter("PDF Datei", ".pdf"));
		JButton btnchoose = new JButton("Choose Outputfile");
		btnchoose.addActionListener(a->outChoose.showSaveDialog(this));
		JPanel tmp = new JPanel();
		tmp.add(btnchoose);
		this.add(tmp);
		JButton btnRun = new JButton("Run");
		btnRun.addActionListener(a->{
			System.out.println(1);
			if(outChoose.getSelectedFile()!=null&&inPath!=null) {
				System.out.println(2);
				try {
					if(outChoose.getSelectedFile().canWrite()||outChoose.getSelectedFile().createNewFile()) {
						System.out.println(3);
						btnRun.setEnabled(false);
						new Main(inPath, outChoose.getSelectedFile().getAbsolutePath());} 
				} catch (IOException e) {e.printStackTrace();}}});
		JPanel tmp2 = new JPanel();
		tmp2.add(btnRun);
		this.add(tmp2);

		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}





	final class FileDropHandler extends TransferHandler {
		private static final long serialVersionUID = -108083826315999460L;

		@Override
		public boolean canImport(TransferHandler.TransferSupport support) {
			for (DataFlavor flavor : support.getDataFlavors()) {
				if (flavor.isFlavorJavaFileListType()) {
					return true;
				}
			}
			return false;
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean importData(TransferHandler.TransferSupport support) {
			if (!this.canImport(support))
				return false;

			List<File> files;
			try {
				files = (List<File>) support.getTransferable()
						.getTransferData(DataFlavor.javaFileListFlavor);
			} catch (UnsupportedFlavorException | IOException ex) {
				// should never happen (or JDK is buggy)
				return false;
			}

			inPath = files.get(0).getAbsolutePath();
			Optional.ofNullable(files.get(0).getParentFile()).ifPresent(f->outChoose.setCurrentDirectory(f));
			return true;
		}
	}

	public static void main(String[] args) {
		new UI();
	}
}
