package com.example.calendar.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * .env 파일을 읽어 System property로 주입한다. 이미 설정된 실제 OS 환경변수가 있으면 그 값을 우선한다.
 * 작업 디렉터리(프로젝트 루트)에 .env가 없으면 아무 것도 하지 않는다.
 */
public final class DotenvLoader {

	private DotenvLoader() {
	}

	public static void load() {
		Path envFile = Path.of(".env");
		if (!Files.isRegularFile(envFile)) {
			return;
		}
		List<String> lines;
		try {
			lines = Files.readAllLines(envFile);
		} catch (IOException exception) {
			return;
		}
		for (String line : lines) {
			String trimmed = line.trim();
			if (trimmed.isEmpty() || trimmed.startsWith("#")) {
				continue;
			}
			int separator = trimmed.indexOf('=');
			if (separator <= 0) {
				continue;
			}
			String key = trimmed.substring(0, separator).trim();
			String value = trimmed.substring(separator + 1).trim();
			if (value.isEmpty()) {
				continue;
			}
			if (System.getenv(key) == null && System.getProperty(key) == null) {
				System.setProperty(key, value);
			}
		}
	}

}
