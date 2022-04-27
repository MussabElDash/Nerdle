import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Constants {
	public static String ZSTD_FILE_EXTENSION = ".zstd";
	public static String TXT_FILE_EXTENSION = ".txt";
	public static String TXT_FILE_PREFIX = "Permutations";
	public static String ZSTD_FILE_PREFIX = "Compressed";
	public static String RESOURCES_FOLDER_NAME = "resources";
	public static Set<String> FILE_TO_IGNORE = new HashSet<>(Arrays.asList(new String[] { ".DS_Store" }));

	public static Path getZstdFilePath(Path filePath, boolean asResource) {
		return getZstdFilePath(getSize(filePath), asResource);
	}

	public static Path getZstdFilePath(int size, boolean asResource) {
		String path = String.format("%s/%s/%s%d%s", TXT_FILE_PREFIX, ZSTD_FILE_PREFIX, ZSTD_FILE_PREFIX, size,
				ZSTD_FILE_EXTENSION);
		if (!asResource) {
			path = RESOURCES_FOLDER_NAME + "/" + path;
		}
		return Path.of(path);
	}

	public static Path getTxtFilePath(Path filePath, boolean asResource) {
		return getTxtFilePath(getSize(filePath), asResource);
	}

	public static Path getTxtFilePath(int size, boolean asResource) {
		String path = String.format("%s/%s%d%s", TXT_FILE_PREFIX, TXT_FILE_PREFIX, size, TXT_FILE_EXTENSION);
		if (!asResource) {
			path = RESOURCES_FOLDER_NAME + "/" + path;
		}
		return Path.of(path);
	}

	private static int getSize(Path filePath) {
		int size = 0;
		if (filePath.endsWith(TXT_FILE_EXTENSION)) {
			size = Integer.parseInt(
					filePath.getFileName().toString().replace(TXT_FILE_PREFIX, "").replace(TXT_FILE_EXTENSION, ""));
		} else if (filePath.endsWith(ZSTD_FILE_EXTENSION)) {
			size = Integer.parseInt(
					filePath.getFileName().toString().replace(ZSTD_FILE_PREFIX, "").replace(ZSTD_FILE_EXTENSION, ""));
		}
		return size;
	}

	public static String getOutputZSTD(String file) {
		return file.replace(TXT_FILE_EXTENSION, ZSTD_FILE_EXTENSION).replace(TXT_FILE_PREFIX, ZSTD_FILE_PREFIX);
	}

	public static String getOutputTXT(String file) {
		return file.replace(ZSTD_FILE_EXTENSION, TXT_FILE_EXTENSION).replace(ZSTD_FILE_PREFIX, TXT_FILE_PREFIX);
	}

	private Constants() {
	}

}
