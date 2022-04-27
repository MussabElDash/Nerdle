package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.luben.zstd.Zstd;
import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;

public class Compression {

	private Compression() {
	}

	public static BufferedReader getBufferedReader(File zstdFile) throws FileNotFoundException, IOException {
		ZstdInputStream zis = new ZstdInputStream(new FileInputStream(zstdFile));
		return new BufferedReader(new InputStreamReader(zis, StandardCharsets.UTF_8));
	}

	public static PrintWriter getPrintWriter(File file) throws FileNotFoundException, IOException {
		ZstdOutputStream zos = new ZstdOutputStream(new FileOutputStream(file, false), Zstd.maxCompressionLevel());
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(zos, StandardCharsets.UTF_8));
		return new PrintWriter(bw, false);
	}

	public static void compressFiles() throws IOException, URISyntaxException {
		Set<Path> files = Files.list(Paths.get(Compression.class.getResource(Constants.TXT_FILE_PREFIX).toURI()))
				.filter(p -> !Constants.FILE_TO_IGNORE.contains(p.getFileName().toString()))
				.filter(p -> !p.toFile().isDirectory()).filter(p -> p.toString().contains("3"))
				.collect(Collectors.toSet());
		System.out.println(files);
		for (Path file : files) {
			System.out.println(file);
			List<String> lines = Files.readAllLines(file);

			Path filePath = Constants.getZstdFilePath(file, false);

			PrintWriter pw = getPrintWriter(filePath.toFile());
			for (String line : lines) {
				pw.println(line);
			}
			pw.close();
		}

	}

	public static void main(String[] args) throws IOException, URISyntaxException {
//		compressFiles();
		compare(8);
	}

	public static void compare(int size) throws IOException, URISyntaxException {
		Path zstdFile = Constants.getZstdFilePath(size, false);
		Path txtFile = Constants.getTxtFilePath(size, true);

		List<String> lines1 = Files.readAllLines(Paths.get(Compression.class.getResource(txtFile.toString()).toURI()));

		Set<String> zstd = new HashSet<String>();
		BufferedReader bf = getBufferedReader(zstdFile.toFile());
		String line;
		while ((line = bf.readLine()) != null) {
			zstd.add(line);
		}
		bf.close();

		Set<String> txt = new HashSet<String>(lines1);

		System.out.println(Math.abs(zstd.size() - txt.size()));
		System.out.println(zstd.stream().filter(s -> !txt.contains(s)).collect(Collectors.toSet()));
		System.out.println(txt.stream().filter(s -> !zstd.contains(s)).collect(Collectors.toSet()));
	}

}
