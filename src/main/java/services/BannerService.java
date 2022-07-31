package services;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

@Slf4j
public class BannerService {
    public void printBanner() {
        String fileName = "banner.txt";

        Optional.ofNullable(BannerService.class.getClassLoader().getResourceAsStream(fileName)).ifPresent(resourceAsStream -> {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream))) {
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        });
    }
}
