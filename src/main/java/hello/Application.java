package hello;

import hello.storage.StorageProperties;
import hello.storage.StorageService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Base64;

@SpringBootApplication
@RestController
@EnableConfigurationProperties(StorageProperties.class)
public class Application {

    private final StorageService storageService;

    @Autowired
    StorageProperties properties;

    @Autowired
    public Application(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/")
    public List<String> home(@RequestParam("file") MultipartFile[] files, @RequestParam("baseOrUrl") String[] baseOrUrls) {

        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> fileList = new ArrayList<>();

        for(MultipartFile file : files){
            if(!file.isEmpty()){
                String pathFile = properties.getLocation()+ java.nio.file.FileSystems.getDefault().getSeparator() +storageService.store(file);
                fileList.add(pathFile);
            }
        }

        for(String s : baseOrUrls){
            if(!s.isEmpty()){
                if (s.startsWith("http")) {
                    try(InputStream in = new URL(s).openStream()){
                        String[] tmp = s.split("/");
                        String filename =tmp[tmp.length-1];
                        String pathFile = properties.getLocation()+ java.nio.file.FileSystems.getDefault().getSeparator() +filename;
                        Files.copy(in, Paths.get(pathFile), StandardCopyOption.REPLACE_EXISTING);
                        fileList.add(pathFile);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                } else {
                    byte[]  imageByteArray = Base64.getDecoder().decode(s);
                    Date dt = new Date();
                    String filename = dt.getTime()+".jpeg";
                    String pathFile = properties.getLocation()+ java.nio.file.FileSystems.getDefault().getSeparator() +filename;
                    try (FileOutputStream fos = new FileOutputStream(pathFile)) {
                        fos.write(imageByteArray);
                        fos.flush();
                        fileList.add(pathFile);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }

        for(String fn : fileList){

            String ext = FilenameUtils.getExtension(fn);
            String tempPathName = fn.replace("."+ext,"");
            StringBuilder sb = new StringBuilder(tempPathName);
            sb.append("_thumbnail.").append(ext);


            String thumbnail = sb.toString();
            saveScaledImage(fn, thumbnail, ext);
            result.add(encode(thumbnail));
        }

        return result;
    }

    private String encode(String filename){
        try {
            File file = new File(filename);
            byte[] fileContent = Files.readAllBytes(file.toPath());
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (Exception e){
            e.printStackTrace();
            return "";
        }
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


    private  void saveScaledImage(String filePath,String outputFile, String ext){
        try {

            BufferedImage sourceImage = ImageIO.read(new File(filePath));
            int width = sourceImage.getWidth();
            int height = sourceImage.getHeight();

            if(width>height){
                float extraSize=    height-100;
                float percentHight = (extraSize/height)*100;
                float percentWidth = width - ((width/100)*percentHight);
                BufferedImage img = new BufferedImage((int)percentWidth, 100, BufferedImage.TYPE_INT_RGB);
                Image scaledImage = sourceImage.getScaledInstance((int)percentWidth, 100, Image.SCALE_SMOOTH);
                img.createGraphics().drawImage(scaledImage, 0, 0, null);
                BufferedImage img2 = new BufferedImage(100, 100 ,BufferedImage.TYPE_INT_RGB);
                img2 = img.getSubimage((int)((percentWidth-100)/2), 0, 100, 100);

                ImageIO.write(img2, ext, new File(outputFile));
            }else{
                float extraSize=    width-100;
                float percentWidth = (extraSize/width)*100;
                float  percentHight = height - ((height/100)*percentWidth);
                BufferedImage img = new BufferedImage(100, (int)percentHight, BufferedImage.TYPE_INT_RGB);
                Image scaledImage = sourceImage.getScaledInstance(100,(int)percentHight, Image.SCALE_SMOOTH);
                img.createGraphics().drawImage(scaledImage, 0, 0, null);
                BufferedImage img2 = new BufferedImage(100, 100 ,BufferedImage.TYPE_INT_RGB);
                img2 = img.getSubimage(0, (int)((percentHight-100)/2), 100, 100);

                ImageIO.write(img2, ext, new File(outputFile));
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
