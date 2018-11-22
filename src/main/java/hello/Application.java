package hello;

import hello.storage.StorageProperties;
import hello.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@RestController
@EnableConfigurationProperties(StorageProperties.class)
public class Application {

    private final StorageService storageService;

    @Autowired
    public Application(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/")
    public List<String> home(@RequestParam("file") MultipartFile[] files, @RequestParam("baseOrUrl") String[] baseOrUrls) {

        ArrayList<String> result = new ArrayList<>();

        for(MultipartFile file : files){
            if(!file.isEmpty()){
                storageService.store(file);
            }
        }

        for(String s : baseOrUrls){
            if(!s.isEmpty()){
                if (s.startsWith("http")) {
                    //TODO: store to queue and than download from url
                } else {
                    //TODO: convert base64 to file
                }
            }
        }

        // TODO: generate
        return result;
    }

    @GetMapping("/")
    public String test(){
        return "<html>\n" +
                "<head>\n" +
                "<title>Upload Multiple File Request Page</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "\t<form method=\"POST\" action=\"\" enctype=\"multipart/form-data\">\n" +
                "\t\tFile1 to upload: <input type=\"file\" name=\"file\"><br /> \n" +
                "\t\tFile2 to upload: <input type=\"file\" name=\"file\"><br /> \n" +
                "\t\tFile3 to upload: <input type=\"file\" name=\"file\"><br /> \n" +
                "\t\tFile4 to upload: <input type=\"file\" name=\"file\"><br /> \n" +
                "\t\tFile4 to upload: <input type=\"file\" name=\"file\"><br /> \n" +
                "\t\t Base64 or URL 1: <input type=\"text\" name=\"baseOrUrl\"><br /> <br /> \n" +
                "\t\tBase64 or URL 2: <input type=\"text\" name=\"baseOrUrl\"><br /> " +
                "\t\tBase64 or URL 3: <input type=\"text\" name=\"baseOrUrl\"><br /> " +
                "\t\tBase64 or URL 4: <input type=\"text\" name=\"baseOrUrl\"><br /> " +
                "\t\tBase64 or URL 5: <input type=\"text\" name=\"baseOrUrl\"><br /> " +
                "<br />\n" +
                "\t\t<input type=\"submit\" value=\"Upload\"> Press here to upload the file!\n" +
                "\t</form>\n" +
                "</body>\n" +
                "</html>";
    }


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner init(StorageService storageService) {
        return (args) -> {
            storageService.deleteAll();
            storageService.init();
        };
    }
}
